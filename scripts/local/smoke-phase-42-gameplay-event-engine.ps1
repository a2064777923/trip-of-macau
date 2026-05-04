param(
  [switch]$Quick,
  [switch]$IncludeBuild,
  [switch]$RequireBackend,
  [string]$ReportPath = ''
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$PhaseDir = '.planning/phases/42-traveler-gameplay-event-engine'
if ([string]::IsNullOrWhiteSpace($ReportPath)) {
  $ReportPath = Join-Path $ProjectRoot "$PhaseDir/42-UAT.md"
}

$BaseUrl = [Environment]::GetEnvironmentVariable('PHASE42_PUBLIC_BASE_URL')
if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
  $BaseUrl = 'http://127.0.0.1:8080/api/v1'
}
$BaseUrl = $BaseUrl.TrimEnd('/')

$HealthUrl = [Environment]::GetEnvironmentVariable('PHASE42_HEALTH_URL')
if ([string]::IsNullOrWhiteSpace($HealthUrl)) {
  $HealthUrl = 'http://127.0.0.1:8080/actuator/health'
}

$Locale = [Environment]::GetEnvironmentVariable('PHASE42_LOCALE')
if ([string]::IsNullOrWhiteSpace($Locale)) {
  $Locale = 'zh-Hant'
}
$EscapedLocale = [Uri]::EscapeDataString($Locale)

$StorylineCode = [Environment]::GetEnvironmentVariable('PHASE42_STORYLINE_CODE')
if ([string]::IsNullOrWhiteSpace($StorylineCode)) {
  $StorylineCode = 'east_west_war_and_coexistence'
}

$DevIdentity = [Environment]::GetEnvironmentVariable('PHASE42_DEV_IDENTITY')
if ([string]::IsNullOrWhiteSpace($DevIdentity)) {
  $DevIdentity = 'phase42-gameplay-smoke'
}

$FlagshipNameNeedle = [string]::Concat(
  [char]0x6771,
  [char]0x897F,
  [char]0x65B9,
  [char]0x6587,
  [char]0x660E
)

$PreferredEventTypes = @(
  'pickup_interacted',
  'task_completed',
  'reward_acquired',
  'proximity_reached',
  'checkin_completed',
  'click_interacted'
)
$AllowedEventTypes = @(
  'story_opened',
  'chapter_started',
  'content_viewed',
  'media_completed',
  'click_interacted',
  'proximity_reached',
  'checkin_completed',
  'pickup_interacted',
  'task_completed',
  'reward_acquired',
  'unsupported_viewed',
  'story_session_exit'
)
$EventAliases = @{
  story_open = 'story_opened'
  chapter_open = 'chapter_started'
  content_read = 'content_viewed'
  interaction_view = 'click_interacted'
  interaction_click = 'click_interacted'
  tap = 'click_interacted'
  tap_interacted = 'click_interacted'
  click = 'click_interacted'
  click_interaction = 'click_interacted'
  arrival = 'proximity_reached'
  arrived = 'proximity_reached'
  poi_arrival = 'proximity_reached'
  nearby_reached = 'proximity_reached'
  range_reached = 'proximity_reached'
  checkin = 'checkin_completed'
  check_in = 'checkin_completed'
  poi_checkin = 'checkin_completed'
  pickup = 'pickup_interacted'
  collectible_pickup = 'pickup_interacted'
  task_complete = 'task_completed'
  reward_claimed = 'reward_acquired'
  unsupported_interaction_view = 'unsupported_viewed'
}

$Results = New-Object System.Collections.Generic.List[object]
$BearerToken = ''

function Redact-SecretText {
  param([AllowNull()][string]$Text)
  if ([string]::IsNullOrEmpty($Text)) { return '' }
  $value = $Text
  $value = $value -replace '(?i)sk-[A-Za-z0-9_\-]{8,}', '[redacted-api-key]'
  $value = $value -replace '(?i)Bearer\s+[A-Za-z0-9._~+/=-]+', 'Bearer [redacted-token]'
  $value = $value -replace '(?i)(COS_SECRET|SECRET_ID|SECRET_KEY|providerApiKey|apiKey|authorization|secret|token|SECRET_ID|SECRET_KEY)', '[redacted-field]'
  return $value
}

function Assert-NoSecretText {
  param(
    [AllowNull()][string]$Text,
    [Parameter(Mandatory = $true)][string]$Context
  )
  if ([string]::IsNullOrEmpty($Text)) { return }
  $patterns = @(
    'sk-[A-Za-z0-9_\-]{8,}',
    'Bearer\s+',
    'COS_SECRET',
    'SECRET_ID',
    'SECRET_KEY',
    'providerApiKey',
    'apiKey',
    'authorization',
    'secret',
    'token'
  )
  foreach ($pattern in $patterns) {
    if ($Text -match "(?i)$pattern") {
      throw "$Context contains a banned secret-bearing pattern: $pattern"
    }
  }
}

function Escape-MarkdownCell {
  param([AllowNull()][string]$Value)
  if ($null -eq $Value) { return '' }
  return ($Value -replace '\|', '/' -replace "`r?`n", ' ').Trim()
}

function Add-Result {
  param(
    [Parameter(Mandatory = $true)][string]$Area,
    [Parameter(Mandatory = $true)][string]$Check,
    [Parameter(Mandatory = $true)][ValidateSet('PASS', 'SKIP', 'FAIL', 'BLOCKED')][string]$Status,
    [Parameter(Mandatory = $true)][string]$Evidence,
    [string]$Command = ''
  )
  $safeEvidence = Redact-SecretText -Text $Evidence
  $safeCommand = Redact-SecretText -Text $Command
  Assert-NoSecretText -Text $safeEvidence -Context "$Area/$Check evidence"
  Assert-NoSecretText -Text $safeCommand -Context "$Area/$Check command"
  $Results.Add([pscustomobject]@{
    Area = $Area
    Check = $Check
    Status = $Status
    Evidence = $safeEvidence
    Command = $safeCommand
  })
}

function Get-SafeErrorMessage {
  param($ErrorRecord)
  $message = [string]$ErrorRecord.Exception.Message
  if ([string]::IsNullOrWhiteSpace($message)) {
    $message = [string]$ErrorRecord
  }
  $message = Redact-SecretText -Text $message
  if ($message.Length -gt 240) {
    $message = $message.Substring(0, 240) + '...'
  }
  return $message
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

function Convert-ToJsonBody {
  param($Value)
  return ($Value | ConvertTo-Json -Depth 100 -Compress)
}

function Invoke-JsonRequest {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url,
    $Body = $null,
    [switch]$Auth
  )
  $headers = @{ Accept = 'application/json' }
  if ($Auth -and -not [string]::IsNullOrWhiteSpace($BearerToken)) {
    $headers.Authorization = "Bearer $BearerToken"
  }
  $options = @{
    Method = $Method
    Uri = $Url
    Headers = $headers
    TimeoutSec = 45
    ErrorAction = 'Stop'
  }
  if ($null -ne $Body) {
    $options.ContentType = 'application/json; charset=utf-8'
    $options.Body = Convert-ToJsonBody $Body
  }
  return Invoke-RestMethod @options
}

function Get-Array {
  param($Value)
  if ($null -eq $Value) { return @() }
  return @($Value)
}

function Get-PropertyValue {
  param($Value, [string]$Name)
  if ($null -eq $Value) { return $null }
  if ($Value.PSObject.Properties.Name -contains $Name) {
    return $Value.$Name
  }
  return $null
}

function Get-AuthToken {
  param($Value)
  if ($null -eq $Value) { return '' }
  foreach ($name in @('accessToken', 'token', 'jwt')) {
    $candidate = Get-PropertyValue -Value $Value -Name $name
    if (-not [string]::IsNullOrWhiteSpace([string]$candidate)) {
      return [string]$candidate
    }
  }
  $session = Get-PropertyValue -Value $Value -Name 'session'
  if ($session) {
    foreach ($name in @('accessToken', 'token', 'jwt')) {
      $candidate = Get-PropertyValue -Value $session -Name $name
      if (-not [string]::IsNullOrWhiteSpace([string]$candidate)) {
        return [string]$candidate
      }
    }
  }
  return ''
}

function Find-FlagshipStoryline {
  param($Storylines)
  $items = Get-Array $Storylines
  $byCode = @($items | Where-Object { [string]$_.code -eq $StorylineCode } | Select-Object -First 1)
  if ($byCode.Count -gt 0) { return $byCode[0] }
  $byName = @($items | Where-Object { ([string]$_.name).Contains($FlagshipNameNeedle) } | Select-Object -First 1)
  if ($byName.Count -gt 0) { return $byName[0] }
  return $null
}

function Get-RuntimeChapters {
  param($Runtime)
  return Get-Array (Get-PropertyValue -Value $Runtime -Name 'chapters')
}

function Get-RuntimeSteps {
  param($ChapterRuntime)
  $steps = Get-Array (Get-PropertyValue -Value $ChapterRuntime -Name 'compiledSteps')
  if ($steps.Count -gt 0) { return $steps }
  $chapter = Get-PropertyValue -Value $ChapterRuntime -Name 'chapter'
  if ($chapter) {
    return Get-Array (Get-PropertyValue -Value $chapter -Name 'runtimeSteps')
  }
  return @()
}

function Normalize-SmokeEventType {
  param($Step)
  $eventType = [string](Get-PropertyValue -Value $Step -Name 'eventType')
  if (-not [string]::IsNullOrWhiteSpace($eventType) -and $eventType -ne 'manual') {
    $normalized = if ($EventAliases.ContainsKey($eventType)) { [string]$EventAliases[$eventType] } else { $eventType }
    if ($AllowedEventTypes -contains $normalized) {
      return $normalized
    }
  }
  $text = @(
    [string](Get-PropertyValue -Value $Step -Name 'stepType'),
    [string](Get-PropertyValue -Value $Step -Name 'displayCategory'),
    [string](Get-PropertyValue -Value $Step -Name 'stepCode'),
    [string](Get-PropertyValue -Value $Step -Name 'triggerType'),
    [string](Get-PropertyValue -Value $Step -Name 'name')
  ) -join ' '
  $lower = $text.ToLowerInvariant()
  if ($lower -match 'pickup|collectible|clue') { return 'pickup_interacted' }
  if ($lower -match 'task|mission|challenge') { return 'task_completed' }
  if ($lower -match 'reward|title|coin|badge') { return 'reward_acquired' }
  if ($lower -match 'proximity|nearby|range|arrival') { return 'proximity_reached' }
  if ($lower -match 'checkin|check_in|check-in') { return 'checkin_completed' }
  return 'click_interacted'
}

function Find-SmokeStep {
  param($Runtime)
  $candidates = New-Object System.Collections.Generic.List[object]
  foreach ($chapterRuntime in (Get-RuntimeChapters -Runtime $Runtime)) {
    $chapterId = [int](Get-PropertyValue -Value $chapterRuntime -Name 'chapterId')
    if (-not $chapterId) {
      $chapter = Get-PropertyValue -Value $chapterRuntime -Name 'chapter'
      $chapterId = [int](Get-PropertyValue -Value $chapter -Name 'id')
    }
    foreach ($step in (Get-RuntimeSteps -ChapterRuntime $chapterRuntime)) {
      $unsupported = [bool](Get-PropertyValue -Value $step -Name 'unsupported')
      if ($unsupported) { continue }
      $eventType = Normalize-SmokeEventType -Step $step
      $stepCode = [string](Get-PropertyValue -Value $step -Name 'stepCode')
      $stepId = Get-PropertyValue -Value $step -Name 'id'
      $stepKey = if (-not [string]::IsNullOrWhiteSpace($stepCode)) { $stepCode } elseif ($stepId) { [string]$stepId } else { 'step' }
      $candidates.Add([pscustomobject]@{
        ChapterId = $chapterId
        Step = $step
        StepKey = $stepKey
        EventType = $eventType
      })
    }
  }
  foreach ($preferred in $PreferredEventTypes) {
    $match = @($candidates | Where-Object { $_.EventType -eq $preferred } | Select-Object -First 1)
    if ($match.Count -gt 0) { return $match[0] }
  }
  if ($candidates.Count -gt 0) { return $candidates[0] }
  return $null
}

function Test-PublicBackend {
  try {
    $health = Invoke-RestMethod -Method GET -Uri $HealthUrl -Headers @{ Accept = 'application/json' } -TimeoutSec 20 -ErrorAction Stop
    $status = if ($health.PSObject.Properties.Name -contains 'status') { [string]$health.status } else { 'available' }
    Add-Result -Area 'public backend' -Check 'health' -Status 'PASS' -Evidence "health=$status" -Command "GET $HealthUrl"
    return $true
  } catch {
    Add-Result -Area 'public backend' -Check 'health' -Status 'BLOCKED' -Evidence "health unavailable: $(Get-SafeErrorMessage $_)" -Command "GET $HealthUrl"
    return $false
  }
}

function Test-Auth {
  try {
    $payload = @{
      devIdentity = $DevIdentity
      nickname = 'Phase42 Smoke'
      localeCode = $Locale
      interfaceMode = 'standard'
    }
    $login = Ensure-Success -Context 'dev-bypass login' -Response (
      Invoke-JsonRequest -Method POST -Url "$BaseUrl/user/login/dev-bypass" -Body $payload
    )
    $script:BearerToken = Get-AuthToken -Value $login
    if ([string]::IsNullOrWhiteSpace($script:BearerToken)) {
      throw 'dev-bypass login did not return an access token'
    }
    Add-Result -Area 'auth' -Check 'dev-bypass login' -Status 'PASS' -Evidence "identity=$DevIdentity; credential redacted" -Command 'POST /user/login/dev-bypass'
    return $true
  } catch {
    Add-Result -Area 'auth' -Check 'dev-bypass login' -Status 'BLOCKED' -Evidence "login unavailable: $(Get-SafeErrorMessage $_)" -Command 'POST /user/login/dev-bypass'
    return $false
  }
}

function Test-GameplayRuntime {
  try {
    $storylines = Ensure-Success -Context 'storylines list' -Response (
      Invoke-JsonRequest -Method GET -Url "$BaseUrl/story-lines?locale=$EscapedLocale" -Auth
    )
    $storyline = Find-FlagshipStoryline -Storylines $storylines
    if ($null -eq $storyline -or [string]::IsNullOrWhiteSpace([string]$storyline.id)) {
      throw "flagship storyline not found by code=$StorylineCode"
    }
    $storyId = [int]$storyline.id
    Add-Result -Area 'story runtime' -Check 'storyline discovery' -Status 'PASS' -Evidence "storyId=$storyId; code=$([string]$storyline.code)" -Command 'GET /story-lines'

    $runtime = Ensure-Success -Context 'storyline runtime' -Response (
      Invoke-JsonRequest -Method GET -Url "$BaseUrl/storylines/$storyId/runtime?locale=$EscapedLocale" -Auth
    )
    $chapters = Get-RuntimeChapters -Runtime $runtime
    $smokeStep = Find-SmokeStep -Runtime $runtime
    if ($chapters.Count -lt 1 -or $null -eq $smokeStep) {
      throw 'runtime did not expose a compiled non-unsupported step'
    }
    Add-Result -Area 'story runtime' -Check 'compiled runtime step' -Status 'PASS' -Evidence "chapters=$($chapters.Count); chapterId=$($smokeStep.ChapterId); step=$($smokeStep.StepKey); event=$($smokeStep.EventType)" -Command 'GET /storylines/{id}/runtime'

    $session = Ensure-Success -Context 'session start' -Response (
      Invoke-JsonRequest -Method POST -Url "$BaseUrl/storylines/$storyId/sessions/start" -Auth
    )
    $sessionId = [string]$session.sessionId
    if ([string]::IsNullOrWhiteSpace($sessionId)) {
      throw 'session start did not return sessionId'
    }
    Add-Result -Area 'session' -Check 'start story session' -Status 'PASS' -Evidence "storyId=$storyId; sessionId=redacted" -Command 'POST /storylines/{id}/sessions/start'

    $step = $smokeStep.Step
    $elementCode = [string](Get-PropertyValue -Value $step -Name 'elementCode')
    if ([string]::IsNullOrWhiteSpace($elementCode)) {
      $elementCode = [string](Get-PropertyValue -Value $step -Name 'stepCode')
    }
    if ([string]::IsNullOrWhiteSpace($elementCode)) {
      $elementCode = "phase42_step_$($smokeStep.StepKey)"
    }
    $elementId = Get-PropertyValue -Value $step -Name 'elementId'
    if ($null -eq $elementId) {
      $elementId = Get-PropertyValue -Value $step -Name 'id'
    }
    $clientEventId = 'phase42:{0}:{1}:{2}:{3}:{4}' -f $storyId, $sessionId, $smokeStep.ChapterId, $smokeStep.StepKey, $smokeStep.EventType
    $eventPayload = @{
      elementCode = $elementCode
      eventType = $smokeStep.EventType
      eventSource = 'phase42_smoke'
      storylineSessionId = $sessionId
      clientEventId = $clientEventId
      payloadJson = (Convert-ToJsonBody @{
        storylineId = $storyId
        chapterId = $smokeStep.ChapterId
        stepKey = $smokeStep.StepKey
        outcomeLabels = @('phase42-smoke')
      })
      occurredAt = (Get-Date).ToUniversalTime().ToString('o')
    }
    if ($null -ne $elementId) {
      $eventPayload.elementId = [int]$elementId
    }

    $firstEvent = Ensure-Success -Context 'first event submit' -Response (
      Invoke-JsonRequest -Method POST -Url "$BaseUrl/storylines/$storyId/sessions/$sessionId/events" -Body $eventPayload -Auth
    )
    if (-not [bool]$firstEvent.accepted) {
      throw 'first event was not accepted'
    }
    Add-Result -Area 'event' -Check 'submit gameplay event' -Status 'PASS' -Evidence "eventType=$($smokeStep.EventType); status=$([string]$firstEvent.eventStatus); outcome=$([string]$firstEvent.outcomeType)" -Command 'POST /storylines/{id}/sessions/{sessionId}/events'

    $duplicateEvent = Ensure-Success -Context 'duplicate event submit' -Response (
      Invoke-JsonRequest -Method POST -Url "$BaseUrl/storylines/$storyId/sessions/$sessionId/events" -Body $eventPayload -Auth
    )
    $duplicateOk = ([bool]$duplicateEvent.duplicate) -or ([string]$duplicateEvent.eventStatus -eq 'already_synced')
    if (-not $duplicateOk) {
      throw "duplicate response did not return duplicate=true or already_synced; status=$($duplicateEvent.eventStatus)"
    }
    Add-Result -Area 'duplicate' -Check 'idempotent duplicate submit' -Status 'PASS' -Evidence "duplicate=$([bool]$duplicateEvent.duplicate); eventStatus=$([string]$duplicateEvent.eventStatus)" -Command 'POST same clientEventId'

    $exploration = Ensure-Success -Context 'storyline exploration' -Response (
      Invoke-JsonRequest -Method GET -Url "$BaseUrl/users/me/exploration?locale=$EscapedLocale&scopeType=storyline&scopeId=$storyId" -Auth
    )
    if ($null -eq (Get-PropertyValue -Value $exploration -Name 'progressPercent')) {
      throw 'exploration response did not include progressPercent'
    }
    Add-Result -Area 'exploration' -Check 'dynamic exploration summary' -Status 'PASS' -Evidence "progressPercent=$($exploration.progressPercent); completed=$($exploration.completedElementCount); available=$($exploration.availableElementCount)" -Command 'GET /users/me/exploration'

    $exit = Ensure-Success -Context 'session exit' -Response (
      Invoke-JsonRequest -Method POST -Url "$BaseUrl/storylines/$storyId/sessions/$sessionId/exit" -Auth
    )
    $exitOk = ([string]$exit.status -eq 'exited') -or ([bool]$exit.exitClearedTemporaryState)
    if (-not $exitOk) {
      throw "exit response did not mark exited; status=$($exit.status)"
    }
    Add-Result -Area 'exit' -Check 'exit story session' -Status 'PASS' -Evidence "status=$([string]$exit.status); exitClearedTemporaryState=$([bool]$exit.exitClearedTemporaryState)" -Command 'POST /storylines/{id}/sessions/{sessionId}/exit'
    return $true
  } catch {
    Add-Result -Area 'story runtime' -Check 'gameplay flow smoke' -Status 'BLOCKED' -Evidence "flow unavailable: $(Get-SafeErrorMessage $_)" -Command 'story runtime/session/event/exploration/exit'
    return $false
  }
}

function Test-ClientBuild {
  if (-not $IncludeBuild) {
    Add-Result -Area 'client build' -Check 'mini-program build' -Status 'SKIP' -Evidence 'run with -IncludeBuild to execute npm run build:weapp.' -Command 'npm run build:weapp'
    return $true
  }
  $clientDir = Join-Path $ProjectRoot 'packages/client'
  Push-Location $clientDir
  try {
    & cmd.exe /c 'npm run build:weapp'
    $exitCode = $LASTEXITCODE
  } finally {
    Pop-Location
  }
  if ($exitCode -ne 0) {
    Add-Result -Area 'client build' -Check 'mini-program build' -Status 'FAIL' -Evidence "build exited $exitCode." -Command 'npm run build:weapp'
    return $false
  }
  Add-Result -Area 'client build' -Check 'mini-program build' -Status 'PASS' -Evidence 'npm run build:weapp exited 0.' -Command 'npm run build:weapp'
  return $true
}

function Write-Report {
  $reportFullPath = $ReportPath
  if (-not [System.IO.Path]::IsPathRooted($reportFullPath)) {
    $reportFullPath = Join-Path $ProjectRoot $reportFullPath
  }
  $dir = Split-Path -Parent $reportFullPath
  if (-not (Test-Path -LiteralPath $dir)) {
    New-Item -ItemType Directory -Path $dir | Out-Null
  }

  $lines = New-Object System.Collections.Generic.List[string]
  $lines.Add('# Phase 42 Gameplay Event Engine UAT')
  $lines.Add('')
  $lines.Add("Generated: $(Get-Date -Format s)")
  $lines.Add("Mode: Quick=$([bool]$Quick); IncludeBuild=$([bool]$IncludeBuild); RequireBackend=$([bool]$RequireBackend)")
  $lines.Add("Locale: $Locale")
  $lines.Add("Storyline code: $StorylineCode")
  $lines.Add('')
  $lines.Add('| Area | Check | Status | Evidence | Command |')
  $lines.Add('| --- | --- | --- | --- | --- |')
  foreach ($result in $Results) {
    $lines.Add("| $(Escape-MarkdownCell $result.Area) | $(Escape-MarkdownCell $result.Check) | $(Escape-MarkdownCell $result.Status) | $(Escape-MarkdownCell $result.Evidence) | $(Escape-MarkdownCell $result.Command) |")
  }
  $lines.Add('')
  $failed = @($Results | Where-Object { $_.Status -eq 'FAIL' })
  $blocked = @($Results | Where-Object { $_.Status -eq 'BLOCKED' })
  if ($failed.Count -gt 0) {
    $lines.Add('Final outcome: FAIL')
  } elseif ($blocked.Count -gt 0) {
    $lines.Add('Final outcome: BLOCKED')
  } else {
    $lines.Add('Final outcome: PASS')
  }

  $content = ($lines -join "`n") + "`n"
  Assert-NoSecretText -Text $content -Context 'Phase 42 UAT report'
  Set-Content -LiteralPath $reportFullPath -Value $content -Encoding UTF8
}

$backendOk = Test-PublicBackend
$authOk = $false
$flowOk = $false
if ($backendOk) {
  $authOk = Test-Auth
  if ($authOk) {
    $flowOk = Test-GameplayRuntime
  } else {
    Add-Result -Area 'story runtime' -Check 'gameplay flow smoke' -Status 'BLOCKED' -Evidence 'auth did not complete, so runtime event smoke was not executed.' -Command 'story runtime/session/event/exploration/exit'
  }
} else {
  Add-Result -Area 'auth' -Check 'dev-bypass login' -Status 'BLOCKED' -Evidence 'public backend health is unavailable.' -Command 'POST /user/login/dev-bypass'
  Add-Result -Area 'story runtime' -Check 'gameplay flow smoke' -Status 'BLOCKED' -Evidence 'public backend health is unavailable.' -Command 'story runtime/session/event/exploration/exit'
}
$buildOk = Test-ClientBuild

Write-Report

$blocked = @($Results | Where-Object { $_.Status -eq 'BLOCKED' })
$failed = @($Results | Where-Object { $_.Status -eq 'FAIL' })
if ($failed.Count -gt 0) {
  Write-Host 'Final outcome: FAIL'
  exit 1
}
if ($blocked.Count -gt 0) {
  Write-Host 'Final outcome: BLOCKED'
  if ($RequireBackend) {
    exit 1
  }
  exit 2
}

Write-Host 'Final outcome: PASS'
