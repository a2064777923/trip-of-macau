param(
  [string]$AdminBaseUrl = '',
  [string]$PublicBaseUrl = '',
  [string]$Locale = 'zh-Hant',
  [long]$PoiId = 9,
  [long]$StorylineId = 8
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http

# Phase 28 smoke uses HttpClient instead of Invoke-RestMethod so utf8 request and response handling is explicit.
# Auth material is read from PHASE28_ADMIN_BEARER, PHASE28_ADMIN_USERNAME/PHASE28_ADMIN_PASSWORD,
# PHASE28_PUBLIC_BEARER, or local ignored files such as tmp-admin-login.json. No tracked secrets belong here.
# Required public probes include /api/v1/experience/poi/9?locale=zh-Hant and /api/v1/storylines/8/runtime?locale=zh-Hant.

function Get-EnvValue {
  param([Parameter(Mandatory = $true)][string]$Name)

  $value = [Environment]::GetEnvironmentVariable($Name)
  if ([string]::IsNullOrWhiteSpace($value)) {
    return $null
  }
  return $value.Trim()
}

function Resolve-Default {
  param(
    [string]$Value,
    [string]$EnvName,
    [string]$Fallback
  )

  if (-not [string]::IsNullOrWhiteSpace($Value)) {
    return $Value.TrimEnd('/')
  }
  $envValue = Get-EnvValue -Name $EnvName
  if ($envValue) {
    return $envValue.TrimEnd('/')
  }
  return $Fallback.TrimEnd('/')
}

function Normalize-BearerToken {
  param([string]$Token)

  if ([string]::IsNullOrWhiteSpace($Token)) {
    return $null
  }
  $trimmed = $Token.Trim()
  if ($trimmed.StartsWith('Bearer ', [System.StringComparison]::OrdinalIgnoreCase)) {
    return $trimmed.Substring(7).Trim()
  }
  return $trimmed
}

function Invoke-Api {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url,
    [string]$Token,
    $Body
  )

  $client = New-Object System.Net.Http.HttpClient
  $request = New-Object System.Net.Http.HttpRequestMessage((New-Object System.Net.Http.HttpMethod($Method)), $Url)

  try {
    if ($Token) {
      $request.Headers.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue('Bearer', (Normalize-BearerToken -Token $Token))
    }

    if ($null -ne $Body) {
      $json = $Body | ConvertTo-Json -Depth 50 -Compress
      $request.Content = New-Object System.Net.Http.StringContent($json, [System.Text.Encoding]::UTF8, 'application/json')
    }

    $response = $client.SendAsync($request).GetAwaiter().GetResult()
    $bytes = $response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
    $raw = [System.Text.Encoding]::UTF8.GetString($bytes)

    if (-not $response.IsSuccessStatusCode) {
      throw "$Method $Url failed with status $([int]$response.StatusCode): $raw"
    }

    if ([string]::IsNullOrWhiteSpace($raw)) {
      return $null
    }
    return $raw | ConvertFrom-Json
  } finally {
    if ($request) {
      $request.Dispose()
    }
    $client.Dispose()
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
  if ($Response.code -ne 0 -and $Response.code -ne 200) {
    throw "$Context failed: code=$($Response.code), message=$($Response.message)"
  }
  return $Response.data
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

function Assert-Contains {
  param(
    [Parameter(Mandatory = $true)]$Items,
    [Parameter(Mandatory = $true)][string]$Expected,
    [Parameter(Mandatory = $true)][string]$Context
  )

  Assert-True -Condition (@($Items | Where-Object { [string]$_ -eq $Expected }).Count -ge 1) -Message "$Context missing expected value: $Expected"
}

function Assert-NotContains {
  param(
    [Parameter(Mandatory = $true)]$Items,
    [Parameter(Mandatory = $true)][string]$Unexpected,
    [Parameter(Mandatory = $true)][string]$Context
  )

  Assert-True -Condition (@($Items | Where-Object { [string]$_ -eq $Unexpected }).Count -eq 0) -Message "$Context exposed unexpected value: $Unexpected"
}

function Assert-NoStatusField {
  param(
    [Parameter(Mandatory = $true)]$Object,
    [Parameter(Mandatory = $true)][string]$Context
  )

  Assert-True -Condition (-not ($Object.PSObject.Properties.Name -contains 'status')) -Message "$Context exposes admin-only status in public runtime"
}

function Get-PageItems {
  param(
    [Parameter(Mandatory = $true)]$Data,
    [Parameter(Mandatory = $true)][string]$Context
  )

  Assert-True -Condition ($Data.PSObject.Properties.Name -contains 'list') -Message "$Context did not return a PageResponse list"
  return ,@($Data.list)
}

function Find-ByCode {
  param(
    [Parameter(Mandatory = $true)]$Items,
    [Parameter(Mandatory = $true)][string]$Code
  )

  return @($Items | Where-Object { $_.code -eq $Code -or $_.elementCode -eq $Code }) | Select-Object -First 1
}

function Get-AdminCredentialsFromFile {
  param([Parameter(Mandatory = $true)][string]$ProjectRoot)

  $path = Join-Path $ProjectRoot 'tmp-admin-login.json'
  if (-not (Test-Path -LiteralPath $path)) {
    return $null
  }
  return Get-Content -LiteralPath $path -Raw -Encoding UTF8 | ConvertFrom-Json
}

function Get-AdminToken {
  param(
    [Parameter(Mandatory = $true)][string]$BaseUrl,
    [Parameter(Mandatory = $true)][string]$ProjectRoot
  )

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE28_ADMIN_BEARER')
  if ($bearer) {
    return $bearer
  }

  $username = Get-EnvValue -Name 'PHASE28_ADMIN_USERNAME'
  $password = Get-EnvValue -Name 'PHASE28_ADMIN_PASSWORD'
  if (-not $username -or -not $password) {
    $fileCredentials = Get-AdminCredentialsFromFile -ProjectRoot $ProjectRoot
    if ($fileCredentials) {
      $username = [string]$fileCredentials.username
      $password = [string]$fileCredentials.password
    }
  }

  if (-not $username -or -not $password) {
    throw 'Admin auth is required. Set PHASE28_ADMIN_BEARER or PHASE28_ADMIN_USERNAME/PHASE28_ADMIN_PASSWORD, or provide a local tmp-admin-login.json file.'
  }

  $login = Ensure-Success -Context 'admin login' -Response (
    Invoke-Api -Method POST -Url "$BaseUrl/api/admin/v1/auth/login" -Body @{
      username = $username
      password = $password
    }
  )
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$login.token)) -Message 'Admin login token is missing'
  return [string]$login.token
}

function Get-PublicToken {
  param([Parameter(Mandatory = $true)][string]$BaseUrl)

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE28_PUBLIC_BEARER')
  if ($bearer) {
    return $bearer
  }

  $devIdentity = Get-EnvValue -Name 'PHASE28_PUBLIC_DEV_IDENTITY'
  if (-not $devIdentity) {
    $devIdentity = 'phase28-smoke-local'
  }

  try {
    $login = Ensure-Success -Context 'public dev-bypass login' -Response (
      Invoke-Api -Method POST -Url "$BaseUrl/api/v1/user/login/dev-bypass" -Body @{
        devIdentity = $devIdentity
        nickname = 'Phase 28 Smoke'
        localeCode = $Locale
        interfaceMode = 'standard'
      }
    )
    Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$login.accessToken)) -Message 'Public dev-bypass token is missing'
    return [string]$login.accessToken
  } catch {
    throw "Public auth is required. Set PHASE28_PUBLIC_BEARER, or run the public backend with local/dev dev-bypass enabled. Detail: $($_.Exception.Message)"
  }
}

$AdminBaseUrl = Resolve-Default -Value $AdminBaseUrl -EnvName 'PHASE28_ADMIN_BASE_URL' -Fallback 'http://127.0.0.1:8081'
$PublicBaseUrl = Resolve-Default -Value $PublicBaseUrl -EnvName 'PHASE28_PUBLIC_BASE_URL' -Fallback 'http://127.0.0.1:8080'
$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$clientEventId = Get-EnvValue -Name 'PHASE28_CLIENT_EVENT_ID'
if (-not $clientEventId) {
  $clientEventId = 'phase28-smoke-ama-story-ch1-complete'
}

$adminToken = Get-AdminToken -BaseUrl $AdminBaseUrl -ProjectRoot $ProjectRoot
$publicToken = Get-PublicToken -BaseUrl $PublicBaseUrl

$templates = Get-PageItems -Context 'admin templates' -Data (Ensure-Success -Context 'admin templates' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/experience/templates?pageNum=1&pageSize=100&status=published" -Token $adminToken
))
$flows = Get-PageItems -Context 'admin flows' -Data (Ensure-Success -Context 'admin flows' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/experience/flows?pageNum=1&pageSize=100&status=published" -Token $adminToken
))
$bindings = Get-PageItems -Context 'admin bindings' -Data (Ensure-Success -Context 'admin bindings' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/experience/bindings?pageNum=1&pageSize=100&ownerType=poi&ownerId=$PoiId" -Token $adminToken
))
$overrides = Get-PageItems -Context 'admin overrides' -Data (Ensure-Success -Context 'admin overrides' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/experience/overrides?pageNum=1&pageSize=100&ownerType=story_chapter" -Token $adminToken
))
$explorationElements = Get-PageItems -Context 'admin exploration elements' -Data (Ensure-Success -Context 'admin exploration elements' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/experience/exploration-elements?pageNum=1&pageSize=100&status=published" -Token $adminToken
))
$governance = Ensure-Success -Context 'admin governance overview' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/experience/governance/overview" -Token $adminToken
)

Assert-True -Condition ($templates.Count -ge 5) -Message 'Admin templates are unexpectedly empty'
Assert-True -Condition ($flows.Count -ge 2) -Message 'Admin flows are unexpectedly empty'
Assert-True -Condition ($bindings.Count -ge 1) -Message 'Admin POI default bindings are missing'
Assert-True -Condition ($overrides.Count -ge 1) -Message 'Admin story overrides are missing'
Assert-True -Condition ($explorationElements.Count -ge 5) -Message 'Admin exploration elements are unexpectedly empty'
Assert-True -Condition ($null -ne $governance) -Message 'Admin governance overview is missing'

Assert-True -Condition ($null -ne (Find-ByCode -Items $templates -Code 'tpl_poi_intro_modal')) -Message 'A-Ma intro template is missing'
Assert-True -Condition ($null -ne (Find-ByCode -Items $templates -Code 'tpl_proximity_fullscreen_media')) -Message 'Proximity media template is missing'
Assert-True -Condition ($null -ne (Find-ByCode -Items $flows -Code 'poi_ama_default_walk_in')) -Message 'A-Ma default flow is missing'
Assert-True -Condition ($null -ne (Find-ByCode -Items $flows -Code 'story_macau_fire_chapter_1_override')) -Message 'Story chapter override flow is missing'
Assert-True -Condition ($null -ne (Find-ByCode -Items $explorationElements -Code 'ama_story_ch1_complete')) -Message 'Story chapter exploration element is missing'

$poiFlow = Ensure-Success -Context 'public POI experience runtime' -Response (
  Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/experience/poi/$($PoiId)?locale=$Locale"
)
Assert-True -Condition ($poiFlow.code -eq 'poi_ama_default_walk_in') -Message "Unexpected POI flow code: $($poiFlow.code)"
Assert-NoStatusField -Object $poiFlow -Context 'public POI flow'
$poiStepCodes = @($poiFlow.steps | ForEach-Object { $_.stepCode })
Assert-Contains -Items $poiStepCodes -Expected 'tap_intro' -Context 'public POI flow steps'
Assert-Contains -Items $poiStepCodes -Expected 'arrival_intro_media' -Context 'public POI flow steps'
foreach ($step in @($poiFlow.steps)) {
  Assert-NoStatusField -Object $step -Context "public POI step $($step.stepCode)"
}

$storyRuntime = Ensure-Success -Context 'public storyline runtime' -Response (
  Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/storylines/$($StorylineId)/runtime?locale=$Locale"
)
Assert-True -Condition ($null -ne $storyRuntime.storyline) -Message 'Storyline runtime is missing storyline summary'
Assert-True -Condition (@($storyRuntime.chapters).Count -ge 1) -Message 'Storyline runtime has no chapters'
Assert-True -Condition ($storyRuntime.storyModeConfig.schemaVersion -eq 1) -Message 'Storyline runtime story_mode_config_json was not compiled'

$chapterOne = @($storyRuntime.chapters | Where-Object { $_.chapterOrder -eq 1 -or $_.chapter.chapterOrder -eq 1 }) | Select-Object -First 1
Assert-True -Condition ($null -ne $chapterOne) -Message 'Storyline runtime chapter 1 is missing'
$chapterAnchorType = if ($chapterOne.anchorType) { $chapterOne.anchorType } else { $chapterOne.chapter.anchorType }
$chapterAnchorTargetId = if ($chapterOne.anchorTargetId) { $chapterOne.anchorTargetId } else { $chapterOne.chapter.anchorTargetId }
$chapterOverridePolicy = if ($chapterOne.overridePolicy) { $chapterOne.overridePolicy } else { $chapterOne.chapter.overridePolicy }
$chapterStoryModeConfig = if ($chapterOne.storyModeConfig) { $chapterOne.storyModeConfig } else { $chapterOne.chapter.storyModeConfig }
Assert-True -Condition ($chapterAnchorType -eq 'poi') -Message "Chapter 1 anchor type is not poi: $chapterAnchorType"
Assert-True -Condition ($chapterAnchorTargetId -eq $PoiId) -Message "Chapter 1 anchor target id is not $PoiId"
Assert-True -Condition ($chapterOverridePolicy.schemaVersion -eq 1) -Message 'Chapter override_policy_json was not exposed'
Assert-True -Condition ($chapterStoryModeConfig.hideUnrelatedContent -eq $true) -Message 'Chapter story mode should hide unrelated content'
$compiledCodes = @($chapterOne.compiledSteps | ForEach-Object { $_.stepCode })
Assert-NotContains -Items $compiledCodes -Unexpected 'arrival_intro_media' -Context 'chapter compiled steps'
Assert-Contains -Items $compiledCodes -Expected 'chapter_core_media' -Context 'chapter compiled steps'
Assert-Contains -Items $compiledCodes -Expected 'main_overlay_collect_3' -Context 'chapter compiled steps'

$payloadJson = @{
  schemaVersion = 1
  source = 'phase28-smoke'
  locale = $Locale
} | ConvertTo-Json -Depth 10 -Compress
$eventBody = @{
  elementCode = 'ama_story_ch1_complete'
  eventType = 'story_chapter_complete'
  eventSource = 'phase28_smoke'
  clientEventId = $clientEventId
  payloadJson = $payloadJson
  occurredAt = (Get-Date).ToString('yyyy-MM-ddTHH:mm:ss')
}

$eventOne = Ensure-Success -Context 'public experience event first post' -Response (
  Invoke-Api -Method POST -Url "$PublicBaseUrl/api/v1/experience/events" -Token $publicToken -Body $eventBody
)
$eventTwo = Ensure-Success -Context 'public experience event duplicate post' -Response (
  Invoke-Api -Method POST -Url "$PublicBaseUrl/api/v1/experience/events" -Token $publicToken -Body $eventBody
)
Assert-True -Condition ($eventOne.accepted -eq $true) -Message 'First event was not accepted'
Assert-True -Condition ($eventTwo.accepted -eq $true) -Message 'Duplicate event was not accepted idempotently'
Assert-True -Condition ($eventOne.eventId -eq $eventTwo.eventId) -Message 'client_event_id idempotency failed: duplicate event ids differ'
Assert-True -Condition ($eventTwo.elementCode -eq 'ama_story_ch1_complete') -Message 'Duplicate event returned the wrong element code'

$exploration = Ensure-Success -Context 'public user exploration' -Response (
  Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/users/me/exploration?locale=$Locale&scopeType=storyline&scopeId=$StorylineId" -Token $publicToken
)
Assert-True -Condition ($exploration.availableWeight -gt 0) -Message 'Exploration denominator is empty'
Assert-True -Condition ($exploration.completedWeight -gt 0) -Message 'Exploration completed weight did not change after event'
Assert-True -Condition ($exploration.progressPercent -gt 0) -Message 'Exploration progress percent did not change after event'
$completedStoryElement = @($exploration.elements | Where-Object { $_.elementCode -eq 'ama_story_ch1_complete' -and $_.completed -eq $true }) | Select-Object -First 1
Assert-True -Condition ($null -ne $completedStoryElement) -Message 'Completed story exploration element is not visible in exploration summary'

Write-Host 'Phase 28 experience smoke passed: admin foundation, public compiled runtime, client_event_id idempotency, and dynamic exploration are aligned.'
