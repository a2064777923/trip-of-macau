param(
    [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
    [string]$PublicBaseUrl = 'http://127.0.0.1:8080',
    [string]$AdminUsername = 'admin',
    [string]$AdminPassword = 'admin123',
    [string]$MySqlHost = '127.0.0.1',
    [int]$MySqlPort = 3306,
    [string]$MySqlDatabase = 'aoxiaoyou',
    [string]$MySqlUser = 'root',
    [string]$MySqlPassword = 'Abc123456'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http

function Wait-JsonEndpoint {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [int]$TimeoutSeconds = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            return Invoke-RestMethod -Uri $Url -Method Get -TimeoutSec 10
        } catch {
            Start-Sleep -Seconds 2
        }
    }

    throw "Endpoint did not become reachable: $Url"
}

function Assert-Equals {
    param(
        [Parameter(Mandatory = $true)]$Actual,
        [Parameter(Mandatory = $true)]$Expected,
        [Parameter(Mandatory = $true)][string]$Label
    )

    if ("$Actual" -ne "$Expected") {
        throw "$Label expected '$Expected' but received '$Actual'."
    }
}

function Assert-True {
    param(
        [Parameter(Mandatory = $true)][bool]$Condition,
        [Parameter(Mandatory = $true)][string]$Label
    )

    if (-not $Condition) {
        throw $Label
    }
}

function Invoke-ApiJson {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Url,
        [object]$Body,
        [hashtable]$Headers
    )

    $invokeParams = @{
        Method = $Method
        Uri = $Url
        TimeoutSec = 30
    }
    if ($Headers) {
        $invokeParams.Headers = $Headers
    }
    if ($null -ne $Body) {
        $invokeParams.ContentType = 'application/json'
        $invokeParams.Body = ($Body | ConvertTo-Json -Depth 20)
    }

    $response = Invoke-RestMethod @invokeParams
    Assert-Equals -Actual $response.code -Expected 0 -Label "API success for $Method $Url"
    return $response.data
}

function Invoke-MySqlScalar {
    param(
        [Parameter(Mandatory = $true)][string]$Query
    )

    $previousPassword = $env:MYSQL_PWD
    try {
        $env:MYSQL_PWD = $MySqlPassword
        $result = & mysql `
            --protocol=TCP `
            --host=$MySqlHost `
            --port=$MySqlPort `
            --user=$MySqlUser `
            --database=$MySqlDatabase `
            --batch `
            --raw `
            --skip-column-names `
            --execute=$Query
        if ($LASTEXITCODE -ne 0) {
            throw "MySQL query failed: $Query"
        }
        return ($result | Out-String).Trim()
    } finally {
        if ($null -eq $previousPassword) {
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
        } else {
            $env:MYSQL_PWD = $previousPassword
        }
    }
}

function Invoke-AssetUpload {
    param(
        [Parameter(Mandatory = $true)][string]$Url,
        [Parameter(Mandatory = $true)][string]$Token,
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string]$AssetKind,
        [string]$LocaleCode = 'en',
        [string]$Status = 'published'
    )

    $client = [System.Net.Http.HttpClient]::new()
    try {
        $client.Timeout = [TimeSpan]::FromSeconds(60)
        $client.DefaultRequestHeaders.Authorization = [System.Net.Http.Headers.AuthenticationHeaderValue]::new('Bearer', $Token)

        $content = [System.Net.Http.MultipartFormDataContent]::new()
        $fileBytes = [System.IO.File]::ReadAllBytes($FilePath)
        $fileContent = [System.Net.Http.ByteArrayContent]::new($fileBytes)
        $fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse('image/png')
        $content.Add($fileContent, 'file', [System.IO.Path]::GetFileName($FilePath))
        $content.Add([System.Net.Http.StringContent]::new($AssetKind), 'assetKind')
        if ($LocaleCode) {
            $content.Add([System.Net.Http.StringContent]::new($LocaleCode), 'localeCode')
        }
        if ($Status) {
            $content.Add([System.Net.Http.StringContent]::new($Status), 'status')
        }

        $response = $client.PostAsync($Url, $content).GetAwaiter().GetResult()
        $payload = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
        if (-not $response.IsSuccessStatusCode) {
            throw "Upload request failed with status $($response.StatusCode): $payload"
        }

        $json = $payload | ConvertFrom-Json
        Assert-Equals -Actual $json.code -Expected 0 -Label 'asset upload response code'
        return $json.data
    } finally {
        $client.Dispose()
    }
}

function Build-RuntimeSettingBody {
    param(
        [Parameter(Mandatory = $true)]$RuntimeItem,
        [Parameter(Mandatory = $true)][string]$ValueJson
    )

    $publishedAt = $null
    if ($null -ne $RuntimeItem.publishedAt -and "$($RuntimeItem.publishedAt)" -ne '') {
        if ($RuntimeItem.publishedAt -is [DateTime]) {
            $publishedAt = $RuntimeItem.publishedAt.ToString('yyyy-MM-ddTHH:mm:ss')
        } else {
            $publishedAt = [string]$RuntimeItem.publishedAt
        }
    }

    return @{
        settingGroup = $RuntimeItem.settingGroup
        settingKey = $RuntimeItem.settingKey
        localeCode = $RuntimeItem.localeCode
        titleEn = $RuntimeItem.titleEn
        valueJson = $ValueJson
        descriptionEn = $RuntimeItem.descriptionEn
        status = $RuntimeItem.status
        sortOrder = $RuntimeItem.sortOrder
        publishedAt = $publishedAt
    }
}

$marker = "phase6-$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())"
$openId = "$marker-user"
$pngBase64 = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO2Z0ioAAAAASUVORK5CYII='
$tempFile = Join-Path $env:TEMP "$marker.png"
$runtimeSettingOriginal = $null
$runtimeSettingId = $null
$assetId = $null
$runtimeSettingMutated = $false

try {
    Write-Host '[phase-06] reapplying Phase 6 seed...'
    & (Join-Path $PSScriptRoot 'apply-phase-06-mock-seed.ps1') `
        -MySqlHost $MySqlHost `
        -MySqlPort $MySqlPort `
        -MySqlDatabase $MySqlDatabase `
        -MySqlUser $MySqlUser `
        -MySqlPassword $MySqlPassword

    Write-Host '[phase-06] waiting for public backend...'
    $publicHealth = Wait-JsonEndpoint -Url "$PublicBaseUrl/api/v1/health"
    Assert-Equals -Actual $publicHealth.code -Expected 0 -Label 'public health response code'
    Assert-Equals -Actual $publicHealth.data.status -Expected 'UP' -Label 'public health status'
    Assert-Equals -Actual $publicHealth.data.publishedCatalog.cities -Expected 3 -Label 'public catalog cities'
    Assert-Equals -Actual $publicHealth.data.publishedCatalog.storylines -Expected 4 -Label 'public catalog storylines'
    Assert-Equals -Actual $publicHealth.data.publishedCatalog.storyChapters -Expected 14 -Label 'public catalog chapters'
    Assert-True -Condition ([bool]$publicHealth.data.discoverCuratedCardsConfigured) -Label 'discover curated cards should be configured'
    Assert-True -Condition ([bool]$publicHealth.data.travelRecommendationProfilesConfigured) -Label 'travel recommendation profiles should be configured'

    Write-Host '[phase-06] waiting for admin backend...'
    $adminHealth = Wait-JsonEndpoint -Url "$AdminBaseUrl/api/v1/health"
    Assert-Equals -Actual $adminHealth.code -Expected 200 -Label 'admin health response code'
    Assert-Equals -Actual $adminHealth.data.status -Expected 'UP' -Label 'admin health status'
    Assert-Equals -Actual $adminHealth.data.contentSummary.publishedRuntimeSettings -Expected 8 -Label 'admin runtime setting count'
    Assert-True -Condition ([bool]$adminHealth.data.integrationHealth.publicApi.healthy) -Label 'admin integration public API should be healthy'
    Assert-True -Condition ([bool]$adminHealth.data.integrationHealth.cos.healthy) -Label 'admin integration COS should be healthy'
    Assert-Equals -Actual $adminHealth.data.integrationHealth.seedMigration.status -Expected 'completed' -Label 'seed migration status'

    Write-Host '[phase-06] logging in to admin backend...'
    $session = Invoke-ApiJson -Method 'Post' -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
        username = $AdminUsername
        password = $AdminPassword
    }
    $adminToken = $session.token
    Assert-True -Condition ([bool]$adminToken) -Label 'admin token should not be empty'
    $adminHeaders = @{ Authorization = "Bearer $adminToken" }

    Write-Host '[phase-06] verifying dashboard and public catalog endpoints...'
    $dashboard = Invoke-ApiJson -Method 'Get' -Url "$AdminBaseUrl/api/admin/v1/dashboard/stats" -Headers $adminHeaders
    Assert-True -Condition ($dashboard.totalUsers -ge 0) -Label 'dashboard totalUsers should be present'
    Assert-Equals -Actual $dashboard.contentSummary.publishedCities -Expected 3 -Label 'dashboard city count'
    Assert-True -Condition ([bool]$dashboard.integrationHealth.database.healthy) -Label 'dashboard database integration should be healthy'

    $cities = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/cities?locale=en"
    $storylines = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/story-lines?locale=en"
    $discoverCards = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/discover/cards?locale=en"
    $travelRuntime = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/runtime/travel?locale=en"
    Assert-Equals -Actual @($cities).Count -Expected 3 -Label 'public cities count'
    Assert-Equals -Actual @($storylines).Count -Expected 4 -Label 'public storyline count'
    Assert-Equals -Actual @($discoverCards).Count -Expected 3 -Label 'public discover card count'
    Assert-Equals -Actual @($travelRuntime.settings.recommendation_profiles).Count -Expected 3 -Label 'travel recommendation profile count'

    Write-Host '[phase-06] verifying COS upload pipeline...'
    [System.IO.File]::WriteAllBytes($tempFile, [Convert]::FromBase64String($pngBase64))
    $asset = Invoke-AssetUpload -Url "$AdminBaseUrl/api/admin/v1/content/assets/upload" -Token $adminToken -FilePath $tempFile -AssetKind 'image' -LocaleCode 'en' -Status 'published'
    $assetId = [int]$asset.id
    Assert-True -Condition ($assetId -gt 0) -Label 'asset id must be positive'
    Assert-Equals -Actual $asset.mimeType -Expected 'image/png' -Label 'asset mime type'
    Assert-Equals -Actual $asset.widthPx -Expected 1 -Label 'asset width'
    Assert-Equals -Actual $asset.heightPx -Expected 1 -Label 'asset height'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM content_assets WHERE id = $assetId;") -Expected 1 -Label 'content_assets row count'
    $assetResponse = Invoke-WebRequest -Uri $asset.canonicalUrl -Method Get -UseBasicParsing -TimeoutSec 30
    Assert-Equals -Actual $assetResponse.StatusCode -Expected 200 -Label 'asset canonical URL status'

    Write-Host '[phase-06] proving admin runtime writes are visible through public APIs...'
    $runtimePage = Invoke-ApiJson -Method 'Get' -Url "$AdminBaseUrl/api/admin/v1/content/runtime-settings?settingGroup=discover&pageSize=50" -Headers $adminHeaders
    $runtimeItem = @($runtimePage.list | Where-Object { $_.settingKey -eq 'curated_cards' -and $_.localeCode -eq 'en' }) | Select-Object -First 1
    Assert-True -Condition ($null -ne $runtimeItem) -Label 'discover curated_cards runtime setting should exist'
    $runtimeSettingId = [int]$runtimeItem.id
    $runtimeSettingOriginal = $runtimeItem.valueJson
    $cards = $runtimeItem.valueJson | ConvertFrom-Json
    $cards[0].title = "$($cards[0].title) [$marker]"
    $cards[0].tag = $marker
    $mutatedJson = $cards | ConvertTo-Json -Depth 12 -Compress
    $updateBody = Build-RuntimeSettingBody -RuntimeItem $runtimeItem -ValueJson $mutatedJson
    $updatedRuntime = Invoke-ApiJson -Method 'Put' -Url "$AdminBaseUrl/api/admin/v1/content/runtime-settings/$runtimeSettingId" -Headers $adminHeaders -Body $updateBody
    $runtimeSettingMutated = $true
    Assert-True -Condition ($updatedRuntime.valueJson -like "*$marker*") -Label 'updated runtime setting should contain marker'
    Start-Sleep -Seconds 2
    $discoverAfterUpdate = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/discover/cards?locale=en"
    Assert-True -Condition ((@($discoverAfterUpdate | Where-Object { $_.tag -eq $marker }).Count) -eq 1) -Label 'public discover cards should reflect admin runtime write'

    Write-Host '[phase-06] exercising public user writes...'
    $rewardBefore = [int](Invoke-MySqlScalar "SELECT inventory_redeemed FROM rewards WHERE id = 1;")
    $publicSession = Invoke-ApiJson -Method 'Post' -Url "$PublicBaseUrl/api/v1/user/login" -Body @{
        openId = $openId
        nickname = 'Phase 6 Smoke'
        localeCode = 'en'
        interfaceMode = 'standard'
    }
    $publicHeaders = @{ Authorization = "Bearer $($publicSession.accessToken)" }
    Assert-True -Condition ([bool]$publicSession.accessToken) -Label 'public access token should not be empty'

    foreach ($poiId in 1, 2, 3, 4) {
        $checkin = Invoke-ApiJson -Method 'Post' -Url "$PublicBaseUrl/api/v1/user/checkins?locale=en" -Headers $publicHeaders -Body @{
            poiId = $poiId
            triggerMode = 'manual'
            distanceMeters = 15
            gpsAccuracy = 6
            latitude = 22.19
            longitude = 113.54
        }
        Assert-Equals -Actual $checkin.poiId -Expected $poiId -Label "checkin poiId $poiId"
        Assert-True -Condition ($checkin.experienceGained -ge 0) -Label "checkin should return experience for poi $poiId"
    }

    $redeem = Invoke-ApiJson -Method 'Post' -Url "$PublicBaseUrl/api/v1/user/rewards/1/redeem?locale=en" -Headers $publicHeaders
    Assert-Equals -Actual $redeem.rewardId -Expected 1 -Label 'reward redeem id'

    $finalState = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/user/state?locale=en" -Headers $publicHeaders
    $userId = [int]$finalState.profile.id
    Assert-Equals -Actual $finalState.profile.totalStamps -Expected 0 -Label 'final total stamps after redeem'
    Assert-Equals -Actual (@($finalState.progress.checkinHistory).Count) -Expected 4 -Label 'final checkin history count'
    Assert-Equals -Actual (@($finalState.progress.redeemedRewardIds).Count) -Expected 1 -Label 'final redeemed reward count'

    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM user_profiles WHERE id = $userId AND open_id = '$openId';") -Expected 1 -Label 'user_profiles row count'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM user_checkins WHERE user_id = $userId;") -Expected 4 -Label 'user_checkins row count'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM reward_redemptions WHERE user_id = $userId AND reward_id = 1;") -Expected 1 -Label 'reward_redemptions row count'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT total_stamps FROM user_profiles WHERE id = $userId;") -Expected 0 -Label 'user_profiles.total_stamps'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT inventory_redeemed FROM rewards WHERE id = 1;") -Expected ($rewardBefore + 1) -Label 'reward inventory delta'

    Write-Host '[phase-06] verifying admin can inspect the live traveler writes...'
    $adminUsers = Invoke-ApiJson -Method 'Get' -Url "$AdminBaseUrl/api/admin/v1/users?pageNum=1&pageSize=20&keyword=$openId" -Headers $adminHeaders
    Assert-Equals -Actual $adminUsers.total -Expected 1 -Label 'admin user search total'
    $adminUser = $adminUsers.list[0]
    Assert-Equals -Actual $adminUser.userId -Expected $userId -Label 'admin user id'
    $adminUserDetail = Invoke-ApiJson -Method 'Get' -Url "$AdminBaseUrl/api/admin/v1/users/$userId" -Headers $adminHeaders
    Assert-Equals -Actual @($adminUserDetail.recentCheckIns).Count -Expected 4 -Label 'admin user detail recent checkins'
    Assert-Equals -Actual $adminUserDetail.progress.totalStamps -Expected 0 -Label 'admin user detail total stamps'

    Write-Host '[phase-06] smoke checks passed.'
    Write-Host "[phase-06] verified userId=$userId openId=$openId runtimeSettingId=$runtimeSettingId assetId=$assetId"
} finally {
    if ($runtimeSettingMutated -and $runtimeSettingId -and $null -ne $runtimeSettingOriginal) {
        try {
            $loginResponse = Invoke-RestMethod -Method Post -Uri "$AdminBaseUrl/api/admin/v1/auth/login" -ContentType 'application/json' -Body (@{ username = $AdminUsername; password = $AdminPassword } | ConvertTo-Json)
            if ($loginResponse.code -eq 0 -and $loginResponse.data.token) {
                $restoreHeaders = @{ Authorization = "Bearer $($loginResponse.data.token)" }
                $runtimePage = Invoke-ApiJson -Method 'Get' -Url "$AdminBaseUrl/api/admin/v1/content/runtime-settings?settingGroup=discover&pageSize=50" -Headers $restoreHeaders
                $runtimeItem = @($runtimePage.list | Where-Object { $_.id -eq $runtimeSettingId }) | Select-Object -First 1
                if ($runtimeItem) {
                    $restoreBody = Build-RuntimeSettingBody -RuntimeItem $runtimeItem -ValueJson $runtimeSettingOriginal
                    Invoke-ApiJson -Method 'Put' -Url "$AdminBaseUrl/api/admin/v1/content/runtime-settings/$runtimeSettingId" -Headers $restoreHeaders -Body $restoreBody | Out-Null
                }
            }
        } catch {
        }
    }

    if ($assetId) {
        try {
            $loginResponse = Invoke-RestMethod -Method Post -Uri "$AdminBaseUrl/api/admin/v1/auth/login" -ContentType 'application/json' -Body (@{ username = $AdminUsername; password = $AdminPassword } | ConvertTo-Json)
            if ($loginResponse.code -eq 0 -and $loginResponse.data.token) {
                $cleanupHeaders = @{ Authorization = "Bearer $($loginResponse.data.token)" }
                Invoke-ApiJson -Method 'Delete' -Url "$AdminBaseUrl/api/admin/v1/content/assets/$assetId" -Headers $cleanupHeaders | Out-Null
            }
        } catch {
        }
    }

    Remove-Item -LiteralPath $tempFile -ErrorAction SilentlyContinue
}
