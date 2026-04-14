param(
    [string]$BaseUrl = 'http://127.0.0.1:8080',
    [string]$MySqlHost = '127.0.0.1',
    [int]$MySqlPort = 3306,
    [string]$MySqlDatabase = 'aoxiaoyou',
    [string]$MySqlUser = 'root',
    [string]$MySqlPassword = 'Abc123456'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

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
        TimeoutSec = 20
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

Write-Host "[phase-04] waiting for public backend..."
$health = Wait-JsonEndpoint -Url "$BaseUrl/actuator/health"
Assert-Equals -Actual $health.status -Expected 'UP' -Label 'public actuator status'

$reward1Before = [int](Invoke-MySqlScalar "SELECT inventory_redeemed FROM rewards WHERE id = 1;")
$reward3Before = [int](Invoke-MySqlScalar "SELECT inventory_redeemed FROM rewards WHERE id = 3;")

$openId = "phase4-smoke-$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())"
Write-Host "[phase-04] using openId=$openId"

$session = Invoke-ApiJson -Method 'Post' -Url "$BaseUrl/api/v1/user/login" -Body @{
    openId = $openId
    nickname = 'Phase 4 Smoke'
    localeCode = 'en'
    interfaceMode = 'standard'
    bootstrapState = @{
        level = 2
        title = 'Smoke Tester'
        totalStamps = 12
        currentExp = 20
        nextLevelExp = 120
        currentCityCode = 'macau'
        activeStoryId = 1
        collectedStampIds = @(101)
        completedStoryIds = @(1)
        completedChapterIds = @(1011)
        redeemedRewardIds = @(3)
        preferences = @{
            interfaceMode = 'elderly'
            fontScale = 1.2
            highContrast = $true
            voiceGuideEnabled = $true
            seniorMode = $true
            localeCode = 'en'
            emergencyContactName = 'Smoke Contact'
            emergencyContactPhone = '12345'
        }
        checkinHistory = @()
    }
}

$headers = @{ Authorization = "Bearer $($session.accessToken)" }
$stateAfterLogin = Invoke-ApiJson -Method 'Get' -Url "$BaseUrl/api/v1/user/state?locale=en" -Headers $headers
Assert-Equals -Actual (($stateAfterLogin.progress.redeemedRewardIds -join ',')) -Expected '3' -Label 'bootstrap redeemed reward ids'
Assert-Equals -Actual (($stateAfterLogin.rewardRedemptions | ForEach-Object { $_.rewardId }) -join ',') -Expected '3' -Label 'bootstrap reward redemption rows'

$preferences = Invoke-ApiJson -Method 'Put' -Url "$BaseUrl/api/v1/user/preferences" -Headers $headers -Body @{
    interfaceMode = 'standard'
    fontScale = 1.4
    highContrast = $false
    voiceGuideEnabled = $true
    seniorMode = $false
    localeCode = 'en'
    emergencyContactName = 'Updated Contact'
    emergencyContactPhone = '54321'
    runtimeOverrides = @{
        source = 'phase4-smoke'
        checkinRadius = 188
    }
}
Assert-Equals -Actual $preferences.interfaceMode -Expected 'standard' -Label 'preferences.interfaceMode'
Assert-Equals -Actual $preferences.emergencyContactPhone -Expected '54321' -Label 'preferences.emergencyContactPhone'

$profile = Invoke-ApiJson -Method 'Put' -Url "$BaseUrl/api/v1/user/profile/current-city?locale=en" -Headers $headers -Body @{
    cityCode = 'macau'
}
Assert-Equals -Actual $profile.currentCityCode -Expected 'macau' -Label 'profile.currentCityCode'

$checkin = Invoke-ApiJson -Method 'Post' -Url "$BaseUrl/api/v1/user/checkins?locale=en" -Headers $headers -Body @{
    poiId = 1
    triggerMode = 'manual'
    distanceMeters = 32.5
    gpsAccuracy = 18.2
    latitude = 22.1869
    longitude = 113.5385
}
Assert-Equals -Actual $checkin.poiId -Expected 1 -Label 'checkin.poiId'
Assert-True -Condition ($checkin.experienceGained -ge 25) -Label 'checkin should award experience'

$redeem = Invoke-ApiJson -Method 'Post' -Url "$BaseUrl/api/v1/user/rewards/1/redeem?locale=en" -Headers $headers
Assert-Equals -Actual $redeem.rewardId -Expected 1 -Label 'redeem.rewardId'

$finalState = Invoke-ApiJson -Method 'Get' -Url "$BaseUrl/api/v1/user/state?locale=en" -Headers $headers
$userId = [int]$finalState.profile.id
Assert-Equals -Actual $finalState.profile.totalStamps -Expected 7 -Label 'final profile.totalStamps'
Assert-Equals -Actual (($finalState.progress.redeemedRewardIds -join ',')) -Expected '3,1' -Label 'final redeemed reward ids'
Assert-Equals -Actual (@($finalState.progress.checkinHistory).Count) -Expected 1 -Label 'checkin history count'

$profileRowCount = [int](Invoke-MySqlScalar "SELECT COUNT(*) FROM user_profiles WHERE id = $userId AND open_id = '$openId';")
$preferenceRowCount = [int](Invoke-MySqlScalar "SELECT COUNT(*) FROM user_preferences WHERE user_id = $userId AND interface_mode = 'standard' AND emergency_contact_phone = '54321';")
$progressRowCount = [int](Invoke-MySqlScalar "SELECT COUNT(*) FROM user_progress WHERE user_id = $userId;")
$checkinRowCount = [int](Invoke-MySqlScalar "SELECT COUNT(*) FROM user_checkins WHERE user_id = $userId AND poi_id = 1;")
$rewardRowCount = [int](Invoke-MySqlScalar "SELECT COUNT(*) FROM reward_redemptions WHERE user_id = $userId AND reward_id IN (1, 3);")
$reward1After = [int](Invoke-MySqlScalar "SELECT inventory_redeemed FROM rewards WHERE id = 1;")
$reward3After = [int](Invoke-MySqlScalar "SELECT inventory_redeemed FROM rewards WHERE id = 3;")

Assert-Equals -Actual $profileRowCount -Expected 1 -Label 'user_profiles row count'
Assert-Equals -Actual $preferenceRowCount -Expected 1 -Label 'user_preferences row count'
Assert-Equals -Actual $progressRowCount -Expected 2 -Label 'user_progress row count'
Assert-Equals -Actual $checkinRowCount -Expected 1 -Label 'user_checkins row count'
Assert-Equals -Actual $rewardRowCount -Expected 2 -Label 'reward_redemptions row count'
Assert-Equals -Actual $reward1After -Expected ($reward1Before + 1) -Label 'reward 1 inventory_redeemed delta'
Assert-Equals -Actual $reward3After -Expected ($reward3Before + 1) -Label 'reward 3 inventory_redeemed delta'

Write-Host "[phase-04] smoke checks passed."
Write-Host "[phase-04] verified userId=$userId openId=$openId"
