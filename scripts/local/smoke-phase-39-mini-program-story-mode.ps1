$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$BaseUrl = $env:PHASE39_PUBLIC_BASE_URL
if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
  $BaseUrl = 'http://localhost:8080/api/v1'
}
$BaseUrl = $BaseUrl.TrimEnd('/')

$StorylineCode = $env:PHASE39_STORYLINE_CODE
if ([string]::IsNullOrWhiteSpace($StorylineCode)) {
  $StorylineCode = 'east_west_war_and_coexistence'
}

$Locale = $env:PHASE39_LOCALE
if ([string]::IsNullOrWhiteSpace($Locale)) {
  $Locale = 'zh-Hant'
}

$DevIdentity = $env:PHASE39_TRAVELER_DEV_IDENTITY
$EscapedLocale = [Uri]::EscapeDataString($Locale)
$FlagshipNameNeedle = [string]::Concat(
  [char]0x6771,
  [char]0x897F,
  [char]0x65B9,
  [char]0x6587,
  [char]0x660E
)

function ConvertTo-CompactJson {
  param($Value)
  if ($null -eq $Value) { return '' }
  return ($Value | ConvertTo-Json -Depth 100 -Compress)
}

function Invoke-JsonRequest {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url,
    $Body = $null,
    [string]$Token = $null
  )

  $headers = @{
    Accept = 'application/json'
  }
  if (-not [string]::IsNullOrWhiteSpace($Token)) {
    $headers.Authorization = "Bearer $Token"
  }

  $parameters = @{
    Method = $Method
    Uri = $Url
    Headers = $headers
    ErrorAction = 'Stop'
    TimeoutSec = 45
  }

  if ($null -ne $Body) {
    $parameters.Body = ConvertTo-CompactJson -Value $Body
    $parameters.ContentType = 'application/json; charset=utf-8'
  }

  return Invoke-RestMethod @parameters
}

function Assert-True {
  param(
    [Parameter(Mandatory = $true)][bool]$Condition,
    [Parameter(Mandatory = $true)][string]$Message
  )
  if (-not $Condition) {
    throw $Message
  }
}

function Assert-Any {
  param(
    [Parameter(Mandatory = $true)]$Items,
    [Parameter(Mandatory = $true)][scriptblock]$Predicate,
    [Parameter(Mandatory = $true)][string]$Context
  )
  $matches = @($Items | Where-Object $Predicate)
  Assert-True -Condition ($matches.Count -ge 1) -Message $Context
}

function Assert-NoBannedFields {
  param(
    [Parameter(Mandatory = $true)]$Object,
    [Parameter(Mandatory = $true)][string]$Context
  )

  $raw = ConvertTo-CompactJson -Value $Object
  $bannedFields = @(
    'promptText',
    'scriptText',
    'localPath',
    'providerApiKey',
    'apiKey',
    'secret',
    'estimatedCost',
    'actualCost',
    'qaNote',
    'cosObjectKey'
  )

  foreach ($field in $bannedFields) {
    $propertyPattern = '"' + [regex]::Escape($field) + '"\s*:'
    if ($raw -match $propertyPattern) {
      throw "$Context leaked banned field: $field"
    }
  }
}

function Ensure-Success {
  param(
    [Parameter(Mandatory = $true)]$Response,
    [Parameter(Mandatory = $true)][string]$Context
  )

  if ($null -eq $Response) {
    throw "$Context returned an empty response"
  }

  if ($Response.PSObject.Properties.Name -contains 'code') {
    if ($Response.code -ne 0 -and $Response.code -ne 200) {
      throw "$Context failed: code=$($Response.code), message=$($Response.message)"
    }
    return $Response.data
  }

  return $Response
}

function Get-Array {
  param($Value)
  if ($null -eq $Value) { return @() }
  return @($Value)
}

function Collect-RuntimeMediaAssets {
  param($Runtime)

  $assets = @()
  if ($Runtime.storyline) {
    foreach ($asset in (Get-Array $Runtime.storyline.attachmentAssets)) {
      if ($asset) { $assets += $asset }
    }
  }

  foreach ($chapterRuntime in (Get-Array $Runtime.chapters)) {
    if ($chapterRuntime.chapter.primaryMediaAsset) {
      $assets += $chapterRuntime.chapter.primaryMediaAsset
    }
    foreach ($block in (Get-Array $chapterRuntime.chapter.contentBlocks)) {
      if ($block.primaryAsset) {
        $assets += $block.primaryAsset
      }
      foreach ($asset in (Get-Array $block.attachmentAssets)) {
        if ($asset) { $assets += $asset }
      }
    }
    foreach ($step in (Get-Array $chapterRuntime.compiledSteps)) {
      if ($step.mediaAsset) {
        $assets += $step.mediaAsset
      }
    }
  }

  return $assets
}

function Find-FlagshipStoryline {
  param($Storylines)

  $items = Get-Array $Storylines
  $storyline = @($items | Where-Object { [string]$_.code -eq $StorylineCode } | Select-Object -First 1)
  if ($storyline.Count -gt 0) {
    return $storyline[0]
  }

  $fallback = @($items | Where-Object { ([string]$_.name).Contains($FlagshipNameNeedle) } | Select-Object -First 1)
  if ($fallback.Count -gt 0) {
    return $fallback[0]
  }

  return $null
}

function Get-FirstChapter {
  param($Runtime)
  $runtimeChapters = Get-Array $Runtime.chapters
  $chapters = @($runtimeChapters | Sort-Object { [int]$_.chapterOrder })
  Assert-True -Condition ($chapters.Count -gt 0) -Message 'Runtime did not return any chapters'
  return $chapters[0]
}

function Get-RepresentativeStep {
  param($Runtime)
  $allSteps = @()
  foreach ($chapter in (Get-Array $Runtime.chapters)) {
    $allSteps += Get-Array $chapter.compiledSteps
  }
  $steps = @($allSteps | Where-Object { $_.elementId -or $_.elementCode } | Select-Object -First 1)
  if ($steps.Count -gt 0) {
    return $steps[0]
  }
  return $null
}

function New-StoryEventBody {
  param(
    [Parameter(Mandatory = $true)][string]$EventType,
    [Parameter(Mandatory = $true)][string]$ClientEventId,
    [Parameter(Mandatory = $true)]$StoryId,
    [Parameter(Mandatory = $true)]$ChapterId,
    $Step = $null
  )

  $payload = @{
    storylineId = [long]$StoryId
    chapterId = [long]$ChapterId
    eventType = $EventType
  }

  $body = @{
    eventType = $EventType
    eventSource = 'phase39_smoke'
    clientEventId = $ClientEventId
    payloadJson = ConvertTo-CompactJson -Value $payload
    occurredAt = (Get-Date).ToUniversalTime().ToString('s')
  }

  if ($Step) {
    if ($Step.elementId) {
      $body.elementId = [long]$Step.elementId
    }
    if (-not [string]::IsNullOrWhiteSpace([string]$Step.elementCode)) {
      $body.elementCode = [string]$Step.elementCode
    }
  }

  return $body
}

$storylines = Ensure-Success -Context 'public storylines list' -Response (
  Invoke-JsonRequest -Method GET -Url "$BaseUrl/story-lines?locale=$EscapedLocale"
)

$storyline = Find-FlagshipStoryline -Storylines $storylines
Assert-True -Condition ($null -ne $storyline) -Message "Storyline $StorylineCode was not found"
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$storyline.id)) -Message 'Storyline id was not returned'
$storyId = $storyline.id

$runtime = Ensure-Success -Context 'public storyline runtime' -Response (
  Invoke-JsonRequest -Method GET -Url "$BaseUrl/storylines/$storyId/runtime?locale=$EscapedLocale"
)

Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$runtime.runtimeVersion)) -Message 'Runtime did not include runtimeVersion'
$chapters = Get-Array $runtime.chapters
Assert-True -Condition ($chapters.Count -ge 5) -Message 'Runtime should include at least five chapters'
Assert-Any -Items $chapters -Context 'Expected at least one chapter with compiledSteps' -Predicate { (Get-Array $_.compiledSteps).Count -ge 1 }
Assert-Any -Items $chapters -Context 'Expected at least one chapter with contentBlocks' -Predicate { (Get-Array $_.chapter.contentBlocks).Count -ge 1 }

$mediaAssets = @(Collect-RuntimeMediaAssets -Runtime $runtime)
Assert-Any -Items $mediaAssets -Context 'Expected at least one runtime media asset with availability' -Predicate { $_.PSObject.Properties.Name -contains 'availability' }

$rawRuntime = ConvertTo-CompactJson -Value $runtime
Assert-True -Condition ($rawRuntime -match '"(image|audio|video|lottie)"') -Message 'Runtime should include at least one image, audio, video, or lottie marker'
Assert-True -Condition ($rawRuntime.Contains('unsupported')) -Message 'Runtime should include unsupported fallback metadata or placeholder state'
Assert-NoBannedFields -Object $runtime -Context 'Storyline runtime'

if ([string]::IsNullOrWhiteSpace($DevIdentity)) {
  Write-Host 'Authenticated smoke skipped: set PHASE39_TRAVELER_DEV_IDENTITY to enable local/dev session checks.'
  Write-Host 'Phase 39 mini-program story-mode smoke passed.'
  exit 0
}

$login = Ensure-Success -Context 'public dev-bypass login' -Response (
  Invoke-JsonRequest -Method POST -Url "$BaseUrl/user/login/dev-bypass" -Body @{
    devIdentity = $DevIdentity
    nickname = 'Phase39 Smoke Traveler'
    localeCode = 'zh-Hant'
    interfaceMode = 'standard'
  }
)
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$login.accessToken)) -Message 'Dev-bypass login did not return accessToken'
$Token = [string]$login.accessToken

$session = Ensure-Success -Context 'storyline session start' -Response (
  Invoke-JsonRequest -Method POST -Url "$BaseUrl/storylines/$storyId/sessions/start" -Token $Token
)
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$session.sessionId)) -Message 'Session start did not return sessionId'
$sessionId = [string]$session.sessionId

$firstChapter = Get-FirstChapter -Runtime $runtime
$chapterId = $firstChapter.chapterId
$representativeStep = Get-RepresentativeStep -Runtime $runtime

$chapterStartedBody = New-StoryEventBody `
  -EventType 'chapter_started' `
  -ClientEventId "phase39-smoke:${storyId}:${sessionId}:chapter-started" `
  -StoryId $storyId `
  -ChapterId $chapterId `
  -Step $representativeStep

$chapterStarted = Ensure-Success -Context 'chapter_started event' -Response (
  Invoke-JsonRequest -Method POST -Url "$BaseUrl/storylines/$storyId/sessions/$sessionId/events" -Token $Token -Body $chapterStartedBody
)
Assert-True -Condition (($true -eq $chapterStarted.accepted) -or ($chapterStarted.eventId -ne $null)) -Message 'chapter_started was not accepted'

$duplicateChapterStarted = Ensure-Success -Context 'duplicate chapter_started event' -Response (
  Invoke-JsonRequest -Method POST -Url "$BaseUrl/storylines/$storyId/sessions/$sessionId/events" -Token $Token -Body $chapterStartedBody
)
Assert-True -Condition (($true -eq $duplicateChapterStarted.duplicate) -or ($duplicateChapterStarted.eventId -ne $null)) -Message 'Duplicate chapter_started was not accepted idempotently'

foreach ($eventType in @('content_viewed', 'media_completed', 'pickup_interacted', 'task_completed', 'reward_acquired', 'unsupported_viewed')) {
  $eventBody = New-StoryEventBody `
    -EventType $eventType `
    -ClientEventId "phase39-smoke:${storyId}:${sessionId}:$eventType" `
    -StoryId $storyId `
    -ChapterId $chapterId `
    -Step $representativeStep
  $event = Ensure-Success -Context "$eventType event" -Response (
    Invoke-JsonRequest -Method POST -Url "$BaseUrl/storylines/$storyId/sessions/$sessionId/events" -Token $Token -Body $eventBody
  )
  Assert-True -Condition (($true -eq $event.accepted) -or ($event.eventId -ne $null)) -Message "$eventType was not accepted"
}

$exploration = Ensure-Success -Context 'user storyline exploration' -Response (
  Invoke-JsonRequest -Method GET -Url "$BaseUrl/users/me/exploration?scopeType=storyline&scopeId=$storyId&locale=$EscapedLocale" -Token $Token
)
if ($exploration -and ($exploration.PSObject.Properties.Name -contains 'scopeType')) {
  Assert-True -Condition ([string]$exploration.scopeType -eq 'storyline') -Message 'Exploration scopeType should be storyline'
}

$exit = Ensure-Success -Context 'storyline session exit' -Response (
  Invoke-JsonRequest -Method POST -Url "$BaseUrl/storylines/$storyId/sessions/$sessionId/exit" -Token $Token
)
Assert-True -Condition ([string]$exit.sessionId -eq $sessionId) -Message 'Session exit returned the wrong sessionId'

$exitAgain = Ensure-Success -Context 'duplicate storyline session exit' -Response (
  Invoke-JsonRequest -Method POST -Url "$BaseUrl/storylines/$storyId/sessions/$sessionId/exit" -Token $Token
)
Assert-True -Condition (($true -eq $exitAgain.duplicateExit) -or ($true -eq $exitAgain.exitClearedTemporaryState) -or ([string]$exitAgain.sessionId -eq $sessionId)) -Message 'Duplicate session exit was not retry-safe'

Write-Host 'Phase 39 mini-program story-mode smoke passed.'
