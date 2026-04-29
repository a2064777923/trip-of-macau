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

function Assert-True {
    param(
        [Parameter(Mandatory = $true)][bool]$Condition,
        [Parameter(Mandatory = $true)][string]$Label
    )
    if (-not $Condition) {
        throw $Label
    }
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

function Invoke-ApiJson {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Url,
        [object]$Body,
        [hashtable]$Headers
    )

    $params = @{
        Method = $Method
        Uri = $Url
        TimeoutSec = 30
    }
    if ($Headers) {
        $params.Headers = $Headers
    }
    if ($null -ne $Body) {
        $jsonBody = $Body | ConvertTo-Json -Depth 20
        $params.ContentType = 'application/json; charset=utf-8'
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes($jsonBody)
    }

    $response = Invoke-RestMethod @params
    Assert-Equals -Actual $response.code -Expected 0 -Label "API success for $Method $Url"
    return $response.data
}

function Invoke-MySqlFile {
    param(
        [Parameter(Mandatory = $true)][string]$Path
    )

    $previousPassword = $env:MYSQL_PWD
    try {
        $env:MYSQL_PWD = $MySqlPassword
        & mysql `
            --default-character-set=utf8mb4 `
            --protocol=TCP `
            --host=$MySqlHost `
            --port=$MySqlPort `
            --user=$MySqlUser `
            --database=$MySqlDatabase `
            --execute="source $Path"
        if ($LASTEXITCODE -ne 0) {
            throw "MySQL source failed: $Path"
        }
    } finally {
        if ($null -eq $previousPassword) {
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
        } else {
            $env:MYSQL_PWD = $previousPassword
        }
    }
}

function Convert-ToCollectiblePayload {
    param(
        [Parameter(Mandatory = $true)]$Detail
    )

    return [ordered]@{
        collectibleCode = $Detail.collectibleCode
        nameZh = $Detail.nameZh
        nameEn = $Detail.nameEn
        nameZht = $Detail.nameZht
        namePt = $Detail.namePt
        descriptionZh = $Detail.descriptionZh
        descriptionEn = $Detail.descriptionEn
        descriptionZht = $Detail.descriptionZht
        descriptionPt = $Detail.descriptionPt
        collectibleType = $Detail.collectibleType
        rarity = $Detail.rarity
        coverAssetId = $Detail.coverAssetId
        iconAssetId = $Detail.iconAssetId
        animationAssetId = $Detail.animationAssetId
        imageUrl = $Detail.imageUrl
        animationUrl = $Detail.animationUrl
        seriesId = $Detail.seriesId
        acquisitionSource = $Detail.acquisitionSource
        popupPresetCode = $Detail.popupPresetCode
        popupConfigJson = $Detail.popupConfigJson
        displayPresetCode = $Detail.displayPresetCode
        displayConfigJson = $Detail.displayConfigJson
        triggerPresetCode = $Detail.triggerPresetCode
        triggerConfigJson = $Detail.triggerConfigJson
        exampleContentZh = $Detail.exampleContentZh
        exampleContentEn = $Detail.exampleContentEn
        exampleContentZht = $Detail.exampleContentZht
        exampleContentPt = $Detail.exampleContentPt
        isRepeatable = $Detail.isRepeatable
        isLimited = $Detail.isLimited
        maxOwnership = $Detail.maxOwnership
        status = $Detail.status
        sortOrder = $Detail.sortOrder
        storylineBindings = @($Detail.storylineBindings)
        cityBindings = @($Detail.cityBindings)
        subMapBindings = @($Detail.subMapBindings)
        indoorBuildingBindings = @($Detail.indoorBuildingBindings)
        indoorFloorBindings = @($Detail.indoorFloorBindings)
        attachmentAssetIds = @($Detail.attachmentAssetIds)
    }
}

function Convert-ToBadgePayload {
    param(
        [Parameter(Mandatory = $true)]$Detail
    )

    return [ordered]@{
        badgeCode = $Detail.badgeCode
        nameZh = $Detail.nameZh
        nameEn = $Detail.nameEn
        nameZht = $Detail.nameZht
        namePt = $Detail.namePt
        descriptionZh = $Detail.descriptionZh
        descriptionEn = $Detail.descriptionEn
        descriptionZht = $Detail.descriptionZht
        descriptionPt = $Detail.descriptionPt
        badgeType = $Detail.badgeType
        rarity = $Detail.rarity
        isHidden = $Detail.isHidden
        coverAssetId = $Detail.coverAssetId
        iconAssetId = $Detail.iconAssetId
        animationAssetId = $Detail.animationAssetId
        iconUrl = $Detail.iconUrl
        imageUrl = $Detail.imageUrl
        animationUnlock = $Detail.animationUnlock
        popupPresetCode = $Detail.popupPresetCode
        popupConfigJson = $Detail.popupConfigJson
        displayPresetCode = $Detail.displayPresetCode
        displayConfigJson = $Detail.displayConfigJson
        triggerPresetCode = $Detail.triggerPresetCode
        triggerConfigJson = $Detail.triggerConfigJson
        exampleContentZh = $Detail.exampleContentZh
        exampleContentEn = $Detail.exampleContentEn
        exampleContentZht = $Detail.exampleContentZht
        exampleContentPt = $Detail.exampleContentPt
        status = $Detail.status
        storylineBindings = @($Detail.storylineBindings)
        cityBindings = @($Detail.cityBindings)
        subMapBindings = @($Detail.subMapBindings)
        indoorBuildingBindings = @($Detail.indoorBuildingBindings)
        indoorFloorBindings = @($Detail.indoorFloorBindings)
        attachmentAssetIds = @($Detail.attachmentAssetIds)
    }
}

function Convert-ToRewardPayload {
    param(
        [Parameter(Mandatory = $true)]$Detail
    )

    return [ordered]@{
        code = $Detail.code
        nameZh = $Detail.nameZh
        nameEn = $Detail.nameEn
        nameZht = $Detail.nameZht
        namePt = $Detail.namePt
        subtitleZh = $Detail.subtitleZh
        subtitleEn = $Detail.subtitleEn
        subtitleZht = $Detail.subtitleZht
        subtitlePt = $Detail.subtitlePt
        descriptionZh = $Detail.descriptionZh
        descriptionEn = $Detail.descriptionEn
        descriptionZht = $Detail.descriptionZht
        descriptionPt = $Detail.descriptionPt
        highlightZh = $Detail.highlightZh
        highlightEn = $Detail.highlightEn
        highlightZht = $Detail.highlightZht
        highlightPt = $Detail.highlightPt
        stampCost = $Detail.stampCost
        inventoryTotal = $Detail.inventoryTotal
        inventoryRedeemed = $Detail.inventoryRedeemed
        coverAssetId = $Detail.coverAssetId
        popupPresetCode = $Detail.popupPresetCode
        popupConfigJson = $Detail.popupConfigJson
        displayPresetCode = $Detail.displayPresetCode
        displayConfigJson = $Detail.displayConfigJson
        triggerPresetCode = $Detail.triggerPresetCode
        triggerConfigJson = $Detail.triggerConfigJson
        exampleContentZh = $Detail.exampleContentZh
        exampleContentEn = $Detail.exampleContentEn
        exampleContentZht = $Detail.exampleContentZht
        exampleContentPt = $Detail.exampleContentPt
        status = $Detail.status
        sortOrder = $Detail.sortOrder
        publishStartAt = $Detail.publishStartAt
        publishEndAt = $Detail.publishEndAt
        storylineBindings = @($Detail.storylineBindings)
        cityBindings = @($Detail.cityBindings)
        subMapBindings = @($Detail.subMapBindings)
        indoorBuildingBindings = @($Detail.indoorBuildingBindings)
        indoorFloorBindings = @($Detail.indoorFloorBindings)
        attachmentAssetIds = @($Detail.attachmentAssetIds)
    }
}

Write-Host '[phase-14] applying carryover seed SQL...'
Invoke-MySqlFile -Path (Join-Path $PSScriptRoot 'mysql\init\19-phase-14-collection-carryover.sql')
Invoke-MySqlFile -Path (Join-Path $PSScriptRoot 'mysql\init\20-phase-14-collection-showcase-seed.sql')
Invoke-MySqlFile -Path (Join-Path $PSScriptRoot 'mysql\init\21-phase-14-progress-and-settings.sql')
Invoke-MySqlFile -Path (Join-Path $PSScriptRoot 'mysql\init\22-phase-14-carryover-verification-seed.sql')

Write-Host '[phase-14] checking health endpoints...'
$adminHealth = Invoke-RestMethod -Uri "$AdminBaseUrl/api/v1/health" -Method Get -TimeoutSec 15
$publicHealth = Invoke-RestMethod -Uri "$PublicBaseUrl/api/v1/health" -Method Get -TimeoutSec 15
Assert-Equals -Actual $adminHealth.code -Expected 200 -Label 'admin health code'
Assert-Equals -Actual $publicHealth.code -Expected 0 -Label 'public health code'

Write-Host '[phase-14] logging in as admin...'
$auth = Invoke-ApiJson -Method 'POST' -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
    username = $AdminUsername
    password = $AdminPassword
}
$headers = @{ Authorization = "Bearer $($auth.token)" }

Write-Host '[phase-14] verifying carryover system settings...'
$carryoverSettings = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/system/carryover-settings" -Headers $headers
Assert-Equals -Actual $carryoverSettings.translationDefaultLocale -Expected 'zh-Hant' -Label 'carryover translationDefaultLocale'
Assert-Equals -Actual ((@($carryoverSettings.translationEnginePriority) -join ',')) -Expected 'google,bing,tencent' -Label 'carryover translationEnginePriority'
Assert-Equals -Actual $carryoverSettings.mediaUploadDefaultPolicyCode -Expected 'compressed' -Label 'carryover mediaUploadDefaultPolicyCode'
Assert-Equals -Actual $carryoverSettings.mapZoomDefaultMinScale -Expected 8 -Label 'carryover mapZoomDefaultMinScale'
Assert-Equals -Actual $carryoverSettings.mapZoomDefaultMaxScale -Expected 18 -Label 'carryover mapZoomDefaultMaxScale'
Assert-Equals -Actual $carryoverSettings.indoorZoomDefaultMinScale -Expected 20 -Label 'carryover indoorZoomDefaultMinScale'
Assert-Equals -Actual $carryoverSettings.indoorZoomDefaultMaxScale -Expected 0.5 -Label 'carryover indoorZoomDefaultMaxScale'
Invoke-ApiJson -Method 'PUT' -Url "$AdminBaseUrl/api/admin/v1/system/carryover-settings" -Body $carryoverSettings -Headers $headers | Out-Null

Write-Host '[phase-14] verifying traveler progress detail...'
$userPage = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/users?pageNum=1&pageSize=20&keyword=Phase%2014%20Carryover" -Headers $headers
$user = @($userPage.list | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $user) -Label 'Phase 14 carryover user fixture missing'
$userDetail = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/users/$($user.userId)" -Headers $headers
Assert-True -Condition ($null -ne $userDetail.cityProgress) -Label 'cityProgress missing'
Assert-True -Condition ($null -ne $userDetail.subMapProgress) -Label 'subMapProgress missing'
Assert-True -Condition ($null -ne $userDetail.collectibleProgress) -Label 'collectibleProgress missing'
Assert-True -Condition ($null -ne $userDetail.badgeProgress) -Label 'badgeProgress missing'
Assert-True -Condition ($null -ne $userDetail.rewardProgress) -Label 'rewardProgress missing'
Assert-True -Condition (@($userDetail.recentCheckIns).Count -ge 1) -Label 'recentCheckIns missing'
Assert-True -Condition (@($userDetail.recentTriggerLogs).Count -ge 1) -Label 'recentTriggerLogs missing'
Assert-Equals -Actual $userDetail.cityProgress.completedCount -Expected 1 -Label 'cityProgress completedCount'
Assert-Equals -Actual $userDetail.cityProgress.totalCount -Expected 4 -Label 'cityProgress totalCount'
Assert-Equals -Actual $userDetail.subMapProgress.completedCount -Expected 1 -Label 'subMapProgress completedCount'
Assert-Equals -Actual $userDetail.subMapProgress.totalCount -Expected 3 -Label 'subMapProgress totalCount'
Assert-Equals -Actual $userDetail.collectibleProgress.completedCount -Expected 4 -Label 'collectibleProgress completedCount'
Assert-Equals -Actual $userDetail.collectibleProgress.totalCount -Expected 4 -Label 'collectibleProgress totalCount'
Assert-Equals -Actual $userDetail.badgeProgress.completedCount -Expected 1 -Label 'badgeProgress completedCount'
Assert-Equals -Actual $userDetail.badgeProgress.totalCount -Expected 3 -Label 'badgeProgress totalCount'
Assert-Equals -Actual $userDetail.rewardProgress.completedCount -Expected 0 -Label 'rewardProgress completedCount'
Assert-Equals -Actual $userDetail.rewardProgress.totalCount -Expected 6 -Label 'rewardProgress totalCount'
Assert-Equals -Actual @($userDetail.recentCheckIns).Count -Expected 3 -Label 'recentCheckIns count'
Assert-Equals -Actual @($userDetail.recentTriggerLogs).Count -Expected 3 -Label 'recentTriggerLogs count'

Write-Host '[phase-14] verifying admin collection carryover surfaces...'
$adminCollectibles = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/collectibles/items?pageNum=1&pageSize=20&keyword=collectible_lisboeta_night_pass" -Headers $headers
$collectible = @($adminCollectibles.list | Where-Object { $_.collectibleCode -eq 'collectible_lisboeta_night_pass' } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $collectible) -Label 'phase 14 collectible showcase missing'
Assert-True -Condition (@($collectible.indoorBuildingBindings).Count -gt 0) -Label 'collectible indoorBuildingBindings missing'
Assert-True -Condition (@($collectible.indoorFloorBindings).Count -gt 0) -Label 'collectible indoorFloorBindings missing'
Assert-Equals -Actual $collectible.popupPresetCode -Expected 'story-modal' -Label 'collectible popupPresetCode'
Assert-Equals -Actual $collectible.displayPresetCode -Expected 'map-keepsake' -Label 'collectible displayPresetCode'
Assert-Equals -Actual $collectible.triggerPresetCode -Expected 'poi-arrival' -Label 'collectible triggerPresetCode'

$collectibleDetail = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/collectibles/items/$($collectible.id)" -Headers $headers
Invoke-ApiJson -Method 'PUT' -Url "$AdminBaseUrl/api/admin/v1/collectibles/items/$($collectible.id)" -Body (Convert-ToCollectiblePayload -Detail $collectibleDetail) -Headers $headers | Out-Null
$collectibleAfter = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/collectibles/items/$($collectible.id)" -Headers $headers
Assert-Equals -Actual ((@($collectibleAfter.storylineBindings) -join ',')) -Expected ((@($collectibleDetail.storylineBindings) -join ',')) -Label 'collectible storylineBindings roundtrip'
Assert-Equals -Actual ((@($collectibleAfter.indoorBuildingBindings) -join ',')) -Expected ((@($collectibleDetail.indoorBuildingBindings) -join ',')) -Label 'collectible indoorBuildingBindings roundtrip'
Assert-Equals -Actual ((@($collectibleAfter.indoorFloorBindings) -join ',')) -Expected ((@($collectibleDetail.indoorFloorBindings) -join ',')) -Label 'collectible indoorFloorBindings roundtrip'
Assert-Equals -Actual ((@($collectibleAfter.attachmentAssetIds) -join ',')) -Expected ((@($collectibleDetail.attachmentAssetIds) -join ',')) -Label 'collectible attachmentAssetIds roundtrip'

$adminBadges = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/collectibles/badges?pageNum=1&pageSize=20" -Headers $headers
$badge = @($adminBadges.list | Where-Object { $_.badgeCode -eq 'badge_lisboeta_pathfinder' } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $badge) -Label 'phase 14 badge showcase missing'
Assert-Equals -Actual $badge.popupPresetCode -Expected 'achievement-toast' -Label 'badge popupPresetCode'
Assert-Equals -Actual $badge.displayPresetCode -Expected 'badge-ribbon' -Label 'badge displayPresetCode'
Assert-Equals -Actual $badge.triggerPresetCode -Expected 'chapter-completion' -Label 'badge triggerPresetCode'
$badgeDetail = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/collectibles/badges/$($badge.id)" -Headers $headers
Invoke-ApiJson -Method 'PUT' -Url "$AdminBaseUrl/api/admin/v1/collectibles/badges/$($badge.id)" -Body (Convert-ToBadgePayload -Detail $badgeDetail) -Headers $headers | Out-Null
$badgeAfter = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/collectibles/badges/$($badge.id)" -Headers $headers
Assert-Equals -Actual ((@($badgeAfter.storylineBindings) -join ',')) -Expected ((@($badgeDetail.storylineBindings) -join ',')) -Label 'badge storylineBindings roundtrip'
Assert-Equals -Actual ((@($badgeAfter.indoorBuildingBindings) -join ',')) -Expected ((@($badgeDetail.indoorBuildingBindings) -join ',')) -Label 'badge indoorBuildingBindings roundtrip'
Assert-Equals -Actual ((@($badgeAfter.indoorFloorBindings) -join ',')) -Expected ((@($badgeDetail.indoorFloorBindings) -join ',')) -Label 'badge indoorFloorBindings roundtrip'
Assert-Equals -Actual ((@($badgeAfter.attachmentAssetIds) -join ',')) -Expected ((@($badgeDetail.attachmentAssetIds) -join ',')) -Label 'badge attachmentAssetIds roundtrip'

$adminRewards = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/collectibles/rewards?pageNum=1&pageSize=20&status=published" -Headers $headers
$reward = @($adminRewards.list | Where-Object { $_.code -eq 'reward_lisboeta_secret_cut' } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $reward) -Label 'phase 14 reward showcase missing'
Assert-Equals -Actual $reward.popupPresetCode -Expected 'reward-modal' -Label 'reward popupPresetCode'
Assert-Equals -Actual $reward.displayPresetCode -Expected 'inventory-card' -Label 'reward displayPresetCode'
Assert-Equals -Actual $reward.triggerPresetCode -Expected 'reward-redemption' -Label 'reward triggerPresetCode'
Invoke-ApiJson -Method 'PUT' -Url "$AdminBaseUrl/api/admin/v1/collectibles/rewards/$($reward.id)" -Body (Convert-ToRewardPayload -Detail $reward) -Headers $headers | Out-Null
$rewardAfter = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/collectibles/rewards?pageNum=1&pageSize=20&status=published" -Headers $headers
$rewardAfterItem = @($rewardAfter.list | Where-Object { $_.id -eq $reward.id } | Select-Object -First 1)[0]
Assert-Equals -Actual ((@($rewardAfterItem.storylineBindings) -join ',')) -Expected ((@($reward.storylineBindings) -join ',')) -Label 'reward storylineBindings roundtrip'
Assert-Equals -Actual ((@($rewardAfterItem.indoorBuildingBindings) -join ',')) -Expected ((@($reward.indoorBuildingBindings) -join ',')) -Label 'reward indoorBuildingBindings roundtrip'
Assert-Equals -Actual ((@($rewardAfterItem.indoorFloorBindings) -join ',')) -Expected ((@($reward.indoorFloorBindings) -join ',')) -Label 'reward indoorFloorBindings roundtrip'
Assert-Equals -Actual ((@($rewardAfterItem.attachmentAssetIds) -join ',')) -Expected ((@($reward.attachmentAssetIds) -join ',')) -Label 'reward attachmentAssetIds roundtrip'

Write-Host '[phase-14] verifying public collection payloads...'
$publicCollectibles = Invoke-ApiJson -Method 'GET' -Url "$PublicBaseUrl/api/v1/collectibles?locale=zh-Hant"
$publicCollectible = @($publicCollectibles | Where-Object { $_.code -eq 'collectible_lisboeta_night_pass' } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $publicCollectible) -Label 'public collectible showcase missing'
Assert-True -Condition (@($publicCollectible.relatedIndoorBuildings).Count -gt 0) -Label 'public collectible indoor building binding missing'
Assert-True -Condition (@($publicCollectible.relatedIndoorFloors).Count -gt 0) -Label 'public collectible indoor floor binding missing'
Assert-Equals -Actual $publicCollectible.popupPresetCode -Expected 'story-modal' -Label 'public collectible popupPresetCode'

$publicBadges = Invoke-ApiJson -Method 'GET' -Url "$PublicBaseUrl/api/v1/badges?locale=zh-Hant"
$publicBadge = @($publicBadges | Where-Object { $_.code -eq 'badge_lisboeta_pathfinder' } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $publicBadge) -Label 'public badge showcase missing'
Assert-True -Condition (@($publicBadge.relatedIndoorBuildings).Count -gt 0) -Label 'public badge indoor building binding missing'
Assert-Equals -Actual $publicBadge.displayPresetCode -Expected 'badge-ribbon' -Label 'public badge displayPresetCode'

$publicRewards = Invoke-ApiJson -Method 'GET' -Url "$PublicBaseUrl/api/v1/rewards?locale=zh-Hant"
$publicReward = @($publicRewards | Where-Object { $_.code -eq 'reward_lisboeta_secret_cut' } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $publicReward) -Label 'public reward showcase missing'
Assert-True -Condition (@($publicReward.relatedIndoorFloors).Count -gt 0) -Label 'public reward indoor floor binding missing'
Assert-True -Condition (@($publicReward.attachmentAssetUrls).Count -gt 0) -Label 'public reward attachment assets missing'
Assert-Equals -Actual $publicReward.popupPresetCode -Expected 'reward-modal' -Label 'public reward popupPresetCode'

Write-Host '[phase-14] running mini-program build alignment check...'
Push-Location (Join-Path $PSScriptRoot '..\..\packages\client')
try {
    npm run build:weapp
    if ($LASTEXITCODE -ne 0) {
        throw 'Mini-program build failed'
    }
} finally {
    Pop-Location
}

Write-Host '[phase-14] smoke verification passed.'
Write-Host ("[phase-14] user={0}, collectible={1}, badge={2}, reward={3}" -f $user.userId, $publicCollectible.code, $publicBadge.code, $publicReward.code)
