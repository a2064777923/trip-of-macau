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
        $invokeParams.Body = ($Body | ConvertTo-Json -Depth 12)
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

function Set-RewardCoverAsset {
    param(
        [Parameter(Mandatory = $true)][int]$RewardId,
        [AllowNull()]$AssetId
    )

    $value = if ($null -eq $AssetId -or "$AssetId" -eq '') { 'NULL' } else { $AssetId }
    Invoke-MySqlScalar "UPDATE rewards SET cover_asset_id = $value, updated_at = NOW() WHERE id = $RewardId;"
}

$pngBase64 = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO2Z0ioAAAAASUVORK5CYII='
$tempFile = Join-Path $env:TEMP "phase5-cos-smoke-$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()).png"
$originalRewardCoverAsset = $null
$shouldRestoreRewardCover = $false

try {
    [System.IO.File]::WriteAllBytes($tempFile, [Convert]::FromBase64String($pngBase64))

    Write-Host '[phase-05] waiting for admin backend...'
    $adminHealth = Wait-JsonEndpoint -Url "$AdminBaseUrl/api/v1/health"
    Assert-Equals -Actual $adminHealth.data.status -Expected 'UP' -Label 'admin health status'

    Write-Host '[phase-05] waiting for public backend...'
    $publicHealth = Wait-JsonEndpoint -Url "$PublicBaseUrl/actuator/health"
    Assert-Equals -Actual $publicHealth.status -Expected 'UP' -Label 'public actuator status'

    Write-Host '[phase-05] logging in to admin backend...'
    $session = Invoke-ApiJson -Method 'Post' -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
        username = $AdminUsername
        password = $AdminPassword
    }
    $token = $session.token
    Assert-True -Condition ([bool]$token) -Label 'admin login token should not be empty'

    Write-Host '[phase-05] uploading smoke asset to COS...'
    $asset = Invoke-AssetUpload -Url "$AdminBaseUrl/api/admin/v1/content/assets/upload" -Token $token -FilePath $tempFile -AssetKind 'image' -LocaleCode 'en' -Status 'published'
    $assetId = [int]$asset.id
    Assert-True -Condition ($assetId -gt 0) -Label 'uploaded asset id must be positive'

    Assert-Equals -Actual $asset.assetKind -Expected 'image' -Label 'assetKind'
    Assert-Equals -Actual $asset.mimeType -Expected 'image/png' -Label 'mimeType'
    Assert-Equals -Actual $asset.widthPx -Expected 1 -Label 'widthPx'
    Assert-Equals -Actual $asset.heightPx -Expected 1 -Label 'heightPx'
    Assert-Equals -Actual $asset.status -Expected 'published' -Label 'status'

    Write-Host '[phase-05] verifying content_assets row...'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM content_assets WHERE id = $assetId;") -Expected 1 -Label 'content_assets row count'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT bucket_name FROM content_assets WHERE id = $assetId;") -Expected $asset.bucketName -Label 'bucket_name'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT region FROM content_assets WHERE id = $assetId;") -Expected $asset.region -Label 'region'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT object_key FROM content_assets WHERE id = $assetId;") -Expected $asset.objectKey -Label 'object_key'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT canonical_url FROM content_assets WHERE id = $assetId;") -Expected $asset.canonicalUrl -Label 'canonical_url'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT mime_type FROM content_assets WHERE id = $assetId;") -Expected 'image/png' -Label 'mime_type'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT width_px FROM content_assets WHERE id = $assetId;") -Expected 1 -Label 'width_px'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT height_px FROM content_assets WHERE id = $assetId;") -Expected 1 -Label 'height_px'
    Assert-Equals -Actual (Invoke-MySqlScalar "SELECT status FROM content_assets WHERE id = $assetId;") -Expected 'published' -Label 'db status'
    Assert-True -Condition ((Invoke-MySqlScalar "SELECT LENGTH(checksum) FROM content_assets WHERE id = $assetId;") -eq '64') -Label 'checksum should be sha256 length'

    Write-Host '[phase-05] probing canonical URL...'
    $assetResponse = Invoke-WebRequest -Uri $asset.canonicalUrl -Method Get -UseBasicParsing -TimeoutSec 30
    Assert-Equals -Actual $assetResponse.StatusCode -Expected 200 -Label 'canonical URL GET status'

    Write-Host '[phase-05] verifying public API canonical URL semantics...'
    $rewardId = 1
    $rewardOriginalValue = Invoke-MySqlScalar "SELECT cover_asset_id FROM rewards WHERE id = $rewardId;"
    if ([string]::IsNullOrWhiteSpace($rewardOriginalValue) -or $rewardOriginalValue -eq 'NULL') {
        $originalRewardCoverAsset = $null
    } else {
        $originalRewardCoverAsset = [int]$rewardOriginalValue
    }

    $shouldRestoreRewardCover = $true
    Set-RewardCoverAsset -RewardId $rewardId -AssetId $assetId
    Start-Sleep -Seconds 1
    $rewards = Invoke-ApiJson -Method 'Get' -Url "$PublicBaseUrl/api/v1/rewards?locale=en"
    $reward = $rewards | Where-Object { $_.id -eq $rewardId } | Select-Object -First 1
    Assert-True -Condition ($null -ne $reward) -Label 'reward should exist in public API response'
    Assert-Equals -Actual $reward.coverImageUrl -Expected $asset.canonicalUrl -Label 'public reward coverImageUrl'

    Write-Host '[phase-05] smoke checks passed.'
    Write-Host "[phase-05] verified assetId=$assetId canonicalUrl=$($asset.canonicalUrl)"
} finally {
    if ($shouldRestoreRewardCover) {
        Set-RewardCoverAsset -RewardId 1 -AssetId $originalRewardCoverAsset
    }
    Remove-Item -LiteralPath $tempFile -ErrorAction SilentlyContinue
}
