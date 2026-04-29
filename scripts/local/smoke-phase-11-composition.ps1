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
$TempRoot = Join-Path $env:TEMP ("phase11-smoke-" + [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())
New-Item -ItemType Directory -Path $TempRoot -Force | Out-Null

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

function Invoke-ApiEnvelope {
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
        $params.ContentType = 'application/json'
        $params.Body = ($Body | ConvertTo-Json -Depth 20)
    }

    return Invoke-RestMethod @params
}

function Invoke-ApiJson {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Url,
        [object]$Body,
        [hashtable]$Headers
    )

    $response = Invoke-ApiEnvelope -Method $Method -Url $Url -Body $Body -Headers $Headers
    Assert-Equals -Actual $response.code -Expected 0 -Label "API success for $Method $Url"
    return $response.data
}

function Write-Utf8File {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [Parameter(Mandatory = $true)][string]$Content
    )

    $encoding = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $encoding)
}

function Invoke-CurlJson {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Url,
        [string]$Token,
        [string]$JsonPath
    )

    $arguments = @('-sS', '-X', $Method, $Url)
    if ($Token) {
        $arguments += @('-H', "Authorization: Bearer $Token")
    }
    if ($JsonPath) {
        $arguments += @('-H', 'Content-Type: application/json; charset=utf-8', '--data-binary', "@$JsonPath")
    }

    $raw = & curl.exe @arguments
    if (-not $raw) {
        throw "Empty response from $Url"
    }

    $parsed = $raw | ConvertFrom-Json
    Assert-Equals -Actual $parsed.code -Expected 0 -Label "curl success for $Method $Url"
    return $parsed.data
}

function Invoke-MySqlScalar {
    param(
        [Parameter(Mandatory = $true)][string]$Query
    )

    $previousPassword = $env:MYSQL_PWD
    try {
        $env:MYSQL_PWD = $MySqlPassword
        $result = & mysql `
            --default-character-set=utf8mb4 `
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

function To-IsoLocalString {
    param(
        [Parameter(ValueFromPipeline = $true)]$Value
    )

    if ($null -eq $Value -or [string]::IsNullOrWhiteSpace("$Value")) {
        return $null
    }

    if ($Value -is [DateTime]) {
        return $Value.ToString('yyyy-MM-ddTHH:mm:ss')
    }

    return ([DateTime]::Parse("$Value")).ToString('yyyy-MM-ddTHH:mm:ss')
}

Write-Host '[phase-11] checking health endpoints...'
$adminHealth = Invoke-RestMethod -Uri "$AdminBaseUrl/api/v1/health" -Method Get -TimeoutSec 15
$publicHealth = Invoke-RestMethod -Uri "$PublicBaseUrl/api/v1/health" -Method Get -TimeoutSec 15
Assert-Equals -Actual $adminHealth.code -Expected 200 -Label 'admin health code'
Assert-Equals -Actual $publicHealth.code -Expected 0 -Label 'public health code'

Write-Host '[phase-11] verifying phase 11 schema markers in MySQL...'
Assert-Equals -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$MySqlDatabase' AND table_name = 'content_relation_links';") -Expected '1' -Label 'content_relation_links table'
Assert-Equals -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = '$MySqlDatabase' AND table_name = 'story_chapters' AND column_name = 'anchor_type';") -Expected '1' -Label 'story_chapters.anchor_type column'
Assert-Equals -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = '$MySqlDatabase' AND table_name = 'activities' AND column_name = 'activity_type';") -Expected '1' -Label 'activities.activity_type column'
Assert-Equals -Actual (Invoke-MySqlScalar "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = '$MySqlDatabase' AND table_name = 'rewards' AND column_name = 'cover_asset_id';") -Expected '1' -Label 'rewards.cover_asset_id column'

Write-Host '[phase-11] logging in as admin...'
$auth = Invoke-ApiJson -Method 'POST' -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
    username = $AdminUsername
    password = $AdminPassword
}
$headers = @{ Authorization = "Bearer $($auth.token)" }

Write-Host '[phase-11] validating storyline and chapter authoring routes...'
$storylinePage = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/storylines?pageNum=1&pageSize=20" -Headers $headers
$storyline = @($storylinePage.list | Where-Object { @($_.cityBindings).Count -gt 0 -and @($_.subMapBindings).Count -gt 0 } | Select-Object -First 1)[0]
if ($null -eq $storyline) {
    $storyline = @($storylinePage.list | Select-Object -First 1)[0]
}
Assert-True -Condition ($null -ne $storyline) -Label 'No storyline seed was found for phase 11.'
$storylineDetail = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/storylines/$($storyline.storylineId)" -Headers $headers
$storylinePayload = [ordered]@{
    cityId = $storylineDetail.cityId
    cityBindings = @($storylineDetail.cityBindings)
    subMapBindings = @($storylineDetail.subMapBindings)
    attachmentAssetIds = @($storylineDetail.attachmentAssetIds)
    code = $storylineDetail.code
    nameZh = $storylineDetail.nameZh
    nameEn = $storylineDetail.nameEn
    nameZht = $storylineDetail.nameZht
    namePt = $storylineDetail.namePt
    descriptionZh = $storylineDetail.descriptionZh
    descriptionEn = $storylineDetail.descriptionEn
    descriptionZht = $storylineDetail.descriptionZht
    descriptionPt = $storylineDetail.descriptionPt
    estimatedMinutes = $storylineDetail.estimatedMinutes
    difficulty = $storylineDetail.difficulty
    coverAssetId = $storylineDetail.coverAssetId
    bannerAssetId = $storylineDetail.bannerAssetId
    rewardBadgeZh = $storylineDetail.rewardBadgeZh
    rewardBadgeEn = $storylineDetail.rewardBadgeEn
    rewardBadgeZht = $storylineDetail.rewardBadgeZht
    rewardBadgePt = $storylineDetail.rewardBadgePt
    status = $storylineDetail.status
    sortOrder = $storylineDetail.sortOrder
    publishedAt = To-IsoLocalString $storylineDetail.publishedAt
}
$storylinePayloadPath = Join-Path $TempRoot 'storyline.json'
Write-Utf8File -Path $storylinePayloadPath -Content ($storylinePayload | ConvertTo-Json -Depth 20)
$storylineUpdated = Invoke-CurlJson -Method 'PUT' -Url "$AdminBaseUrl/api/admin/v1/storylines/$($storyline.storylineId)" -Token $auth.token -JsonPath $storylinePayloadPath
Assert-Equals -Actual $storylineUpdated.code -Expected $storylineDetail.code -Label 'storyline roundtrip code'
Assert-True -Condition (@($storylineUpdated.cityBindings).Count -gt 0) -Label 'storyline cityBindings should survive admin roundtrip'

$chapterPage = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/storylines/$($storyline.storylineId)/chapters?pageNum=1&pageSize=20" -Headers $headers
$chapter = @($chapterPage.list | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $chapter) -Label 'No chapter seed was found for phase 11.'
$chapterPayload = [ordered]@{
    storylineId = $chapter.storylineId
    chapterOrder = $chapter.chapterOrder
    titleZh = $chapter.titleZh
    titleEn = $chapter.titleEn
    titleZht = $chapter.titleZht
    titlePt = $chapter.titlePt
    summaryZh = $chapter.summaryZh
    summaryEn = $chapter.summaryEn
    summaryZht = $chapter.summaryZht
    summaryPt = $chapter.summaryPt
    detailZh = $chapter.detailZh
    detailEn = $chapter.detailEn
    detailZht = $chapter.detailZht
    detailPt = $chapter.detailPt
    achievementZh = $chapter.achievementZh
    achievementEn = $chapter.achievementEn
    achievementZht = $chapter.achievementZht
    achievementPt = $chapter.achievementPt
    collectibleZh = $chapter.collectibleZh
    collectibleEn = $chapter.collectibleEn
    collectibleZht = $chapter.collectibleZht
    collectiblePt = $chapter.collectiblePt
    locationNameZh = $chapter.locationNameZh
    locationNameEn = $chapter.locationNameEn
    locationNameZht = $chapter.locationNameZht
    locationNamePt = $chapter.locationNamePt
    mediaAssetId = $chapter.mediaAssetId
    anchorType = $chapter.anchorType
    anchorTargetId = $chapter.anchorTargetId
    anchorTargetCode = $chapter.anchorTargetCode
    unlockType = $chapter.unlockType
    unlockParamJson = $chapter.unlockParamJson
    prerequisiteJson = $chapter.prerequisiteJson
    completionJson = $chapter.completionJson
    rewardJson = $chapter.rewardJson
    status = $chapter.status
    sortOrder = $chapter.sortOrder
    publishedAt = To-IsoLocalString $chapter.publishedAt
}
$chapterPayloadPath = Join-Path $TempRoot 'chapter.json'
Write-Utf8File -Path $chapterPayloadPath -Content ($chapterPayload | ConvertTo-Json -Depth 20)
$chapterUpdated = Invoke-CurlJson -Method 'PUT' -Url "$AdminBaseUrl/api/admin/v1/storylines/$($storyline.storylineId)/chapters/$($chapter.id)" -Token $auth.token -JsonPath $chapterPayloadPath
Assert-Equals -Actual $chapterUpdated.anchorType -Expected $chapter.anchorType -Label 'chapter roundtrip anchorType'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace("$($chapterUpdated.rewardJson)")) -Label 'chapter rewardJson should survive admin roundtrip'

Write-Host '[phase-11] validating activity authoring routes...'
$activityPage = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/operations/activities?pageNum=1&pageSize=20" -Headers $headers
$activity = @($activityPage.list | Where-Object { $_.activityType -eq 'official_event' } | Select-Object -First 1)[0]
if ($null -eq $activity) {
    $activity = @($activityPage.list | Select-Object -First 1)[0]
}
Assert-True -Condition ($null -ne $activity) -Label 'No activity seed was found for phase 11.'
$activityDetail = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/operations/activities/$($activity.id)" -Headers $headers
$activityPayload = [ordered]@{
    code = $activityDetail.code
    activityType = $activityDetail.activityType
    titleZh = $activityDetail.titleZh
    titleEn = $activityDetail.titleEn
    titleZht = $activityDetail.titleZht
    titlePt = $activityDetail.titlePt
    summaryZh = $activityDetail.summaryZh
    summaryEn = $activityDetail.summaryEn
    summaryZht = $activityDetail.summaryZht
    summaryPt = $activityDetail.summaryPt
    descriptionZh = $activityDetail.descriptionZh
    descriptionEn = $activityDetail.descriptionEn
    descriptionZht = $activityDetail.descriptionZht
    descriptionPt = $activityDetail.descriptionPt
    htmlZh = $activityDetail.htmlZh
    htmlEn = $activityDetail.htmlEn
    htmlZht = $activityDetail.htmlZht
    htmlPt = $activityDetail.htmlPt
    venueNameZh = $activityDetail.venueNameZh
    venueNameEn = $activityDetail.venueNameEn
    venueNameZht = $activityDetail.venueNameZht
    venueNamePt = $activityDetail.venueNamePt
    addressZh = $activityDetail.addressZh
    addressEn = $activityDetail.addressEn
    addressZht = $activityDetail.addressZht
    addressPt = $activityDetail.addressPt
    organizerName = $activityDetail.organizerName
    organizerContact = $activityDetail.organizerContact
    organizerWebsite = $activityDetail.organizerWebsite
    signupCapacity = $activityDetail.signupCapacity
    signupFeeAmount = $activityDetail.signupFeeAmount
    signupStartAt = To-IsoLocalString $activityDetail.signupStartAt
    signupEndAt = To-IsoLocalString $activityDetail.signupEndAt
    publishStartAt = To-IsoLocalString $activityDetail.publishStartAt
    publishEndAt = To-IsoLocalString $activityDetail.publishEndAt
    isPinned = $activityDetail.isPinned
    coverAssetId = $activityDetail.coverAssetId
    heroAssetId = $activityDetail.heroAssetId
    participationCount = $activityDetail.participationCount
    status = $activityDetail.status
    sortOrder = $activityDetail.sortOrder
    cityBindings = @($activityDetail.cityBindings)
    subMapBindings = @($activityDetail.subMapBindings)
    storylineBindings = @($activityDetail.storylineBindings)
    attachmentAssetIds = @($activityDetail.attachmentAssetIds)
}
$activityPayloadPath = Join-Path $TempRoot 'activity.json'
Write-Utf8File -Path $activityPayloadPath -Content ($activityPayload | ConvertTo-Json -Depth 20)
$activityUpdated = Invoke-CurlJson -Method 'PUT' -Url "$AdminBaseUrl/api/admin/v1/operations/activities/$($activity.id)" -Token $auth.token -JsonPath $activityPayloadPath
Assert-Equals -Actual $activityUpdated.activityType -Expected $activityDetail.activityType -Label 'activity roundtrip type'
Assert-True -Condition (@($activityUpdated.storylineBindings).Count -gt 0) -Label 'activity storylineBindings should survive admin roundtrip'

Write-Host '[phase-11] validating reward composition routes...'
$rewardPage = Invoke-ApiJson -Method 'GET' -Url "$AdminBaseUrl/api/admin/v1/collectibles/rewards?pageNum=1&pageSize=20" -Headers $headers
$reward = @($rewardPage.list | Where-Object { @($_.storylineBindings).Count -gt 0 } | Select-Object -First 1)[0]
if ($null -eq $reward) {
    $reward = @($rewardPage.list | Select-Object -First 1)[0]
}
Assert-True -Condition ($null -ne $reward) -Label 'No reward seed was found for phase 11.'
$rewardPayload = [ordered]@{
    code = $reward.code
    nameZh = $reward.nameZh
    nameEn = $reward.nameEn
    nameZht = $reward.nameZht
    namePt = $reward.namePt
    subtitleZh = $reward.subtitleZh
    subtitleEn = $reward.subtitleEn
    subtitleZht = $reward.subtitleZht
    subtitlePt = $reward.subtitlePt
    descriptionZh = $reward.descriptionZh
    descriptionEn = $reward.descriptionEn
    descriptionZht = $reward.descriptionZht
    descriptionPt = $reward.descriptionPt
    highlightZh = $reward.highlightZh
    highlightEn = $reward.highlightEn
    highlightZht = $reward.highlightZht
    highlightPt = $reward.highlightPt
    stampCost = $reward.stampCost
    inventoryTotal = $reward.inventoryTotal
    inventoryRedeemed = $reward.inventoryRedeemed
    coverAssetId = $reward.coverAssetId
    status = $reward.status
    sortOrder = $reward.sortOrder
    publishStartAt = To-IsoLocalString $reward.publishStartAt
    publishEndAt = To-IsoLocalString $reward.publishEndAt
    storylineBindings = @($reward.storylineBindings)
    cityBindings = @($reward.cityBindings)
    subMapBindings = @($reward.subMapBindings)
}
$rewardPayloadPath = Join-Path $TempRoot 'reward.json'
Write-Utf8File -Path $rewardPayloadPath -Content ($rewardPayload | ConvertTo-Json -Depth 20)
$rewardUpdated = Invoke-CurlJson -Method 'PUT' -Url "$AdminBaseUrl/api/admin/v1/collectibles/rewards/$($reward.id)" -Token $auth.token -JsonPath $rewardPayloadPath
Assert-Equals -Actual $rewardUpdated.code -Expected $reward.code -Label 'reward roundtrip code'
Assert-True -Condition (@($rewardUpdated.storylineBindings).Count -gt 0) -Label 'reward storylineBindings should survive admin roundtrip'

Write-Host '[phase-11] validating public storyline, activity, discover, and reward reads...'
$publicStoryline = Invoke-ApiJson -Method 'GET' -Url "$PublicBaseUrl/api/v1/story-lines/$($storyline.storylineId)?locale=zh-Hant"
Assert-True -Condition (@($publicStoryline.cityBindings).Count -gt 0) -Label 'public storyline cityBindings should exist'
Assert-True -Condition (@($publicStoryline.subMapBindings).Count -gt 0) -Label 'public storyline subMapBindings should exist'
Assert-True -Condition (@($publicStoryline.chapters).Count -gt 0) -Label 'public storyline chapters should exist'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace("$($publicStoryline.chapters[0].anchorType)")) -Label 'public storyline chapter anchorType should exist'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace("$($publicStoryline.chapters[0].rewardJson)")) -Label 'public storyline chapter rewardJson should exist'

$publicActivities = Invoke-ApiJson -Method 'GET' -Url "$PublicBaseUrl/api/v1/activities?locale=zh-Hant"
$publicActivity = @($publicActivities | Where-Object { $_.code -eq $activity.code } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $publicActivity) -Label 'public activity record should exist'
Assert-Equals -Actual $publicActivity.activityType -Expected $activity.activityType -Label 'public activity type'
Assert-True -Condition (@($publicActivity.cityBindings).Count -gt 0) -Label 'public activity cityBindings should exist'
Assert-True -Condition (@($publicActivity.storylineBindings).Count -gt 0) -Label 'public activity storylineBindings should exist'

$publicDiscoverCards = Invoke-ApiJson -Method 'GET' -Url "$PublicBaseUrl/api/v1/discover/cards?locale=zh-Hant"
$publicDiscoverActivity = @($publicDiscoverCards | Where-Object { $_.type -eq 'activity' } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $publicDiscoverActivity) -Label 'discover activity card should exist'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace("$($publicDiscoverActivity.title)")) -Label 'discover activity title should not be empty'

$publicRewards = Invoke-ApiJson -Method 'GET' -Url "$PublicBaseUrl/api/v1/rewards?locale=zh-Hant"
$publicReward = @($publicRewards | Where-Object { $_.code -eq $reward.code } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $publicReward) -Label 'public reward record should exist'
Assert-True -Condition (@($publicReward.relatedStorylines).Count -gt 0) -Label 'public reward relatedStorylines should exist'
Assert-True -Condition (@($publicReward.relatedCities).Count -gt 0) -Label 'public reward relatedCities should exist'
Assert-True -Condition (@($publicReward.relatedSubMaps).Count -gt 0) -Label 'public reward relatedSubMaps should exist'

Write-Host '[phase-11] smoke verification passed.'
Write-Host ("[phase-11] storyline={0}, chapter={1}, activity={2}, reward={3}" -f $storyline.code, $chapter.id, $activity.code, $reward.code)
