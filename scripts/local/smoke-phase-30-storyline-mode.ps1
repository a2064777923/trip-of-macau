param(
  [string]$AdminBaseUrl = '',
  [string]$PublicBaseUrl = '',
  [string]$Locale = 'zh-Hant',
  [string]$StorylineCode = 'east_west_war_and_coexistence'
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http

# Phase 30 smoke uses HttpClient so UTF8 request and response handling is explicit.
# Auth material is read from PHASE30/PHASE29 environment variables or ignored local tmp-admin-login.json.
# No tracked credentials or secrets belong in this file.

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
      $json = $Body | ConvertTo-Json -Depth 100 -Compress
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

function Assert-NoStatusField {
  param(
    [Parameter(Mandatory = $true)]$Object,
    [Parameter(Mandatory = $true)][string]$Context
  )

  if ($null -eq $Object) {
    return
  }
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

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE30_ADMIN_BEARER')
  if ($bearer) {
    return $bearer
  }

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE29_ADMIN_BEARER')
  if ($bearer) {
    return $bearer
  }

  $fileCredentials = Get-AdminCredentialsFromFile -ProjectRoot $ProjectRoot
  if ($fileCredentials) {
    $fileTokenCandidate = $null
    if ($fileCredentials.PSObject.Properties.Name -contains 'token') {
      $fileTokenCandidate = [string]$fileCredentials.token
    } elseif ($fileCredentials.PSObject.Properties.Name -contains 'accessToken') {
      $fileTokenCandidate = [string]$fileCredentials.accessToken
    }
    $fileToken = Normalize-BearerToken -Token $fileTokenCandidate
    if ($fileToken) {
      return $fileToken
    }
  }

  $username = Get-EnvValue -Name 'PHASE30_ADMIN_USERNAME'
  $password = Get-EnvValue -Name 'PHASE30_ADMIN_PASSWORD'
  if ((-not $username -or -not $password) -and $fileCredentials) {
    $username = [string]$fileCredentials.username
    $password = [string]$fileCredentials.password
  }

  if (-not $username -or -not $password) {
    throw 'Admin auth is required. Set PHASE30_ADMIN_BEARER, PHASE29_ADMIN_BEARER, PHASE30_ADMIN_USERNAME/PHASE30_ADMIN_PASSWORD, or provide local tmp-admin-login.json.'
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
  param(
    [Parameter(Mandatory = $true)][string]$BaseUrl,
    [Parameter(Mandatory = $true)][string]$LocaleCode
  )

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE30_PUBLIC_BEARER')
  if ($bearer) {
    return $bearer
  }

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE29_PUBLIC_BEARER')
  if ($bearer) {
    return $bearer
  }

  $devIdentity = Get-EnvValue -Name 'PHASE30_PUBLIC_DEV_IDENTITY'
  if (-not $devIdentity) {
    $devIdentity = Get-EnvValue -Name 'PHASE29_PUBLIC_DEV_IDENTITY'
  }
  if (-not $devIdentity) {
    $devIdentity = 'phase30-smoke-local'
  }

  try {
    $login = Ensure-Success -Context 'public dev-bypass login' -Response (
      Invoke-Api -Method POST -Url "$BaseUrl/api/v1/user/login/dev-bypass" -Body @{
        devIdentity = $devIdentity
        nickname = 'Phase 30 Smoke'
        localeCode = $LocaleCode
        interfaceMode = 'standard'
      }
    )
    if (-not [string]::IsNullOrWhiteSpace([string]$login.accessToken)) {
      return [string]$login.accessToken
    }
  } catch {
    Write-Host "Public bearer/dev-bypass unavailable; continuing with anonymous public runtime probe. Detail: $($_.Exception.Message)"
  }
  return $null
}

function Resolve-StorylineId {
  param(
    [Parameter(Mandatory = $true)][string]$BaseUrl,
    [Parameter(Mandatory = $true)][string]$Token,
    [Parameter(Mandatory = $true)][string]$Code
  )

  $encodedCode = [Uri]::EscapeDataString($Code)
  $page = Ensure-Success -Context 'admin storyline search' -Response (
    Invoke-Api -Method GET -Url "$BaseUrl/api/admin/v1/storylines?pageNum=1&pageSize=20&keyword=$encodedCode&status=published" -Token $Token
  )
  $items = Get-PageItems -Data $page -Context 'admin storyline search'
  $match = @($items | Where-Object { $_.code -eq $Code }) | Select-Object -First 1
  Assert-True -Condition ($null -ne $match) -Message "Storyline with code $Code was not found through admin search"
  $idCandidate = $match.storylineId
  if ($null -eq $idCandidate) {
    $idCandidate = $match.id
  }
  Assert-True -Condition ($null -ne $idCandidate -and [long]$idCandidate -gt 0) -Message "Storyline $Code did not expose a valid storylineId"
  return [long]$idCandidate
}

function Find-ChapterOne {
  param([Parameter(Mandatory = $true)]$Runtimes)

  $chapterOne = @($Runtimes | Where-Object { $_.chapter.chapterOrder -eq 1 }) | Select-Object -First 1
  if ($chapterOne) {
    return $chapterOne
  }
  return $null
}

function Get-StepCodes {
  param($Steps)

  return @($Steps | ForEach-Object { [string]$_.stepCode })
}

$AdminBaseUrl = Resolve-Default -Value $AdminBaseUrl -EnvName 'PHASE30_ADMIN_BASE_URL' -Fallback 'http://127.0.0.1:8081'
$PublicBaseUrl = Resolve-Default -Value $PublicBaseUrl -EnvName 'PHASE30_PUBLIC_BASE_URL' -Fallback 'http://127.0.0.1:8080'
$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

$expectedAnchorTypes = @('poi', 'indoor_building', 'indoor_floor', 'indoor_node', 'task', 'overlay', 'manual')
$expectedOverrideModes = @('inherit', 'disable', 'replace', 'append')
$expectedChapterTitlePrefix = -join ([char[]](0x93E1, 0x6D77, 0x521D, 0x6230))
$expectedStoryStepCodes = @(
  'story_ch01_arrival_immersive_media',
  'story_ch01_mainline_overlays',
  'story_ch01_side_pickups',
  'story_ch01_hidden_challenge',
  'story_ch01_reward_titles'
)

$adminToken = Get-AdminToken -BaseUrl $AdminBaseUrl -ProjectRoot $ProjectRoot
$publicToken = Get-PublicToken -BaseUrl $PublicBaseUrl -LocaleCode $Locale
$storylineId = Resolve-StorylineId -BaseUrl $AdminBaseUrl -Token $adminToken -Code $StorylineCode

$snapshot = Ensure-Success -Context 'admin storyline mode workbench snapshot' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/storylines/$storylineId/mode-workbench" -Token $adminToken
)

Assert-True -Condition ([string]$snapshot.publicRuntimePath -like "*/api/v1/storylines/$storylineId/runtime*") -Message "Unexpected publicRuntimePath: $($snapshot.publicRuntimePath)"
foreach ($anchorType in $expectedAnchorTypes) {
  Assert-Contains -Items $snapshot.availableAnchorTypes -Expected $anchorType -Context 'admin availableAnchorTypes'
}
foreach ($overrideMode in $expectedOverrideModes) {
  Assert-Contains -Items $snapshot.availableOverrideModes -Expected $overrideMode -Context 'admin availableOverrideModes'
}

$chapterOne = Find-ChapterOne -Runtimes $snapshot.chapterRuntimes
Assert-True -Condition ($null -ne $chapterOne) -Message 'Chapter 1 runtime was not found in admin workbench snapshot'
Assert-True -Condition (([string]$chapterOne.chapter.titleZht).Contains($expectedChapterTitlePrefix) -or ([string]$chapterOne.chapter.titleZh).Contains($expectedChapterTitlePrefix)) -Message "Chapter 1 title does not contain expected prefix: $($chapterOne.chapter.titleZht)"
Assert-True -Condition ([string]$chapterOne.anchor.anchorType -eq 'poi') -Message "Chapter 1 anchor type should be poi, got $($chapterOne.anchor.anchorType)"

$inheritedStepCodes = Get-StepCodes -Steps $chapterOne.inheritedFlow.steps
Assert-Contains -Items $inheritedStepCodes -Expected 'arrival_intro_media' -Context 'Chapter 1 inherited flow'

$replaceOverride = @($chapterOne.overrides | Where-Object {
  [string]$_.targetStepCode -eq 'arrival_intro_media' -and [string]$_.overrideMode -eq 'replace'
}) | Select-Object -First 1
Assert-True -Condition ($null -ne $replaceOverride) -Message 'Chapter 1 replace override for arrival_intro_media was not found'

$publicRuntime = Ensure-Success -Context 'public storyline runtime' -Response (
  Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/storylines/$storylineId/runtime?locale=$([Uri]::EscapeDataString($Locale))" -Token $publicToken
)

Assert-True -Condition ($publicRuntime.storyModeConfig.hideUnrelatedContent -eq $true) -Message 'Public storyModeConfig.hideUnrelatedContent should be true'
$nearbyReveal = $publicRuntime.storyModeConfig.nearbyRevealRadiusMeters
if ($null -eq $nearbyReveal) {
  $nearbyReveal = $publicRuntime.storyModeConfig.nearbyRevealMeters
}
Assert-True -Condition ([int]$nearbyReveal -eq 120) -Message "Public nearby reveal radius should be 120, got $nearbyReveal"
Assert-True -Condition ($publicRuntime.storyModeConfig.preservePermanentEvents -eq $true) -Message 'Public storyModeConfig.preservePermanentEvents should be true'

$publicChapterOne = @($publicRuntime.chapters | Where-Object { $_.chapterOrder -eq 1 -or $_.chapterId -eq $chapterOne.chapter.id }) | Select-Object -First 1
Assert-True -Condition ($null -ne $publicChapterOne) -Message 'Chapter 1 runtime was not found in public storyline runtime'
Assert-NoStatusField -Object $publicChapterOne.inheritedFlow -Context 'public inherited flow'
Assert-NoStatusField -Object $publicChapterOne.chapterFlow -Context 'public chapter flow'
foreach ($step in @($publicChapterOne.inheritedFlow.steps)) {
  Assert-NoStatusField -Object $step -Context "public inherited step $($step.stepCode)"
}
foreach ($step in @($publicChapterOne.chapterFlow.steps)) {
  Assert-NoStatusField -Object $step -Context "public chapter step $($step.stepCode)"
}
foreach ($override in @($publicChapterOne.overrides)) {
  Assert-NoStatusField -Object $override -Context "public override $($override.targetStepCode)/$($override.overrideMode)"
}
foreach ($step in @($publicChapterOne.compiledSteps)) {
  Assert-NoStatusField -Object $step -Context "public compiled step $($step.stepCode)"
}

$compiledStepCodes = Get-StepCodes -Steps $publicChapterOne.compiledSteps
foreach ($stepCode in $expectedStoryStepCodes) {
  Assert-Contains -Items $compiledStepCodes -Expected $stepCode -Context 'public compiled storyline steps'
}

$publicOverride = @($publicChapterOne.overrides | Where-Object {
  [string]$_.targetStepCode -eq 'arrival_intro_media' -and [string]$_.overrideMode -eq 'replace'
}) | Select-Object -First 1
Assert-True -Condition ($null -ne $publicOverride) -Message 'Public runtime replace override for arrival_intro_media was not found'

$poiId = $chapterOne.anchor.anchorTargetId
if ($poiId) {
  $poiFlow = Ensure-Success -Context 'public A-Ma POI experience runtime regression' -Response (
    Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/experience/poi/$($poiId)?locale=$([Uri]::EscapeDataString($Locale))" -Token $publicToken
  )
  Assert-True -Condition ($poiFlow.flowType -eq 'default_poi') -Message "Unexpected A-Ma POI flowType: $($poiFlow.flowType)"
  Assert-True -Condition ($poiFlow.mode -eq 'walk_in') -Message "Unexpected A-Ma POI mode: $($poiFlow.mode)"
  Assert-NoStatusField -Object $poiFlow -Context 'public A-Ma POI flow'
  $poiStepCodes = Get-StepCodes -Steps $poiFlow.steps
  Assert-Contains -Items $poiStepCodes -Expected 'arrival_intro_media' -Context 'public A-Ma POI runtime steps'
  foreach ($step in @($poiFlow.steps)) {
    Assert-NoStatusField -Object $step -Context "public A-Ma POI step $($step.stepCode)"
  }
}

Write-Host 'Phase 30 storyline mode smoke passed'
