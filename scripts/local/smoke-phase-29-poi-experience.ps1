param(
  [string]$AdminBaseUrl = '',
  [string]$PublicBaseUrl = '',
  [string]$Locale = 'zh-Hant',
  [long]$PoiId = 9
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Net.Http

# Phase 29 smoke uses HttpClient so UTF8 request and response handling is explicit.
# Auth material is read from PHASE29_ADMIN_BEARER, PHASE29_ADMIN_USERNAME/PHASE29_ADMIN_PASSWORD,
# PHASE29_PUBLIC_BEARER, optional public dev-bypass envs, or local ignored tmp-admin-login.json.
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
      $json = $Body | ConvertTo-Json -Depth 80 -Compress
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

  $username = Get-EnvValue -Name 'PHASE29_ADMIN_USERNAME'
  $password = Get-EnvValue -Name 'PHASE29_ADMIN_PASSWORD'
  if ((-not $username -or -not $password) -and $fileCredentials) {
    $username = [string]$fileCredentials.username
    $password = [string]$fileCredentials.password
  }

  if (-not $username -or -not $password) {
    throw 'Admin auth is required. Set PHASE29_ADMIN_BEARER or PHASE29_ADMIN_USERNAME/PHASE29_ADMIN_PASSWORD, or provide local tmp-admin-login.json.'
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

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE29_PUBLIC_BEARER')
  if ($bearer) {
    return $bearer
  }

  $devIdentity = Get-EnvValue -Name 'PHASE29_PUBLIC_DEV_IDENTITY'
  if (-not $devIdentity) {
    $devIdentity = 'phase29-smoke-local'
  }

  try {
    $login = Ensure-Success -Context 'public dev-bypass login' -Response (
      Invoke-Api -Method POST -Url "$BaseUrl/api/v1/user/login/dev-bypass" -Body @{
        devIdentity = $devIdentity
        nickname = 'Phase 29 Smoke'
        localeCode = $Locale
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

$AdminBaseUrl = Resolve-Default -Value $AdminBaseUrl -EnvName 'PHASE29_ADMIN_BASE_URL' -Fallback 'http://127.0.0.1:8081'
$PublicBaseUrl = Resolve-Default -Value $PublicBaseUrl -EnvName 'PHASE29_PUBLIC_BASE_URL' -Fallback 'http://127.0.0.1:8080'
$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path

$expectedStepCodes = @(
  'tap_intro',
  'start_route_guidance',
  'arrival_intro_media',
  'release_checkin_tasks',
  'pickup_side_clues',
  'hidden_dwell_achievement',
  'completion_reward_title'
)

$adminToken = Get-AdminToken -BaseUrl $AdminBaseUrl -ProjectRoot $ProjectRoot
$publicToken = Get-PublicToken -BaseUrl $PublicBaseUrl

$snapshot = Ensure-Success -Context 'admin POI experience snapshot' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/pois/$($PoiId)/experience/default" -Token $adminToken
)

Assert-True -Condition ([string]$snapshot.publicRuntimePath -eq "/api/v1/experience/poi/$($PoiId)" -or [string]$snapshot.publicRuntimePath -like "*/api/v1/experience/poi/$($PoiId)*") -Message "Unexpected publicRuntimePath: $($snapshot.publicRuntimePath)"
Assert-True -Condition ($snapshot.flow.flowType -eq 'default_poi') -Message "Unexpected admin flowType: $($snapshot.flow.flowType)"
Assert-True -Condition ($snapshot.flow.mode -eq 'walk_in') -Message "Unexpected admin flow mode: $($snapshot.flow.mode)"
Assert-True -Condition ($snapshot.binding.ownerType -eq 'poi') -Message "Unexpected binding ownerType: $($snapshot.binding.ownerType)"
Assert-True -Condition ($snapshot.binding.bindingRole -eq 'default_experience_flow') -Message "Unexpected bindingRole: $($snapshot.binding.bindingRole)"

$adminStepCodes = @($snapshot.steps | ForEach-Object { $_.stepCode })
foreach ($code in $expectedStepCodes) {
  Assert-Contains -Items $adminStepCodes -Expected $code -Context 'admin POI experience steps'
}

$stepForTemplate = @($snapshot.steps | Where-Object { $_.stepCode -eq 'tap_intro' }) | Select-Object -First 1
Assert-True -Condition ($null -ne $stepForTemplate) -Message 'tap_intro step missing for save-template smoke'

$templatePayload = @{
  code = 'phase29_smoke_saved_template'
  templateType = 'presentation'
  category = 'phase29_smoke'
  nameZh = 'Phase 29 smoke saved template'
  nameZht = 'Phase 29 smoke saved template'
  summaryZh = 'Saved from POI default experience smoke.'
  summaryZht = 'Saved from POI default experience smoke.'
  riskLevel = 'normal'
  status = 'published'
  sortOrder = 2900
}

$savedTemplate = Ensure-Success -Context 'admin POI save-template' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/pois/$($PoiId)/experience/steps/$($stepForTemplate.id)/save-template" -Token $adminToken -Body $templatePayload
)
Assert-True -Condition ($savedTemplate.code -eq 'phase29_smoke_saved_template') -Message "Unexpected saved template code: $($savedTemplate.code)"

$templateSearch = Get-PageItems -Context 'admin template search' -Data (Ensure-Success -Context 'admin template search' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/experience/templates?pageNum=1&pageSize=20&keyword=phase29_smoke_saved_template" -Token $adminToken
))
Assert-True -Condition (@($templateSearch | Where-Object { $_.code -eq 'phase29_smoke_saved_template' }).Count -ge 1) -Message 'Saved template was not found through admin template search'

$publicFlow = Ensure-Success -Context 'public POI experience runtime' -Response (
  Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/experience/poi/$($PoiId)?locale=$Locale" -Token $publicToken
)

Assert-True -Condition ($publicFlow.flowType -eq 'default_poi') -Message "Unexpected public flowType: $($publicFlow.flowType)"
Assert-True -Condition ($publicFlow.mode -eq 'walk_in') -Message "Unexpected public flow mode: $($publicFlow.mode)"
Assert-NoStatusField -Object $publicFlow -Context 'public POI flow'

$publicStepCodes = @($publicFlow.steps | ForEach-Object { $_.stepCode })
foreach ($code in $expectedStepCodes) {
  Assert-Contains -Items $publicStepCodes -Expected $code -Context 'public POI runtime steps'
}

foreach ($step in @($publicFlow.steps)) {
  Assert-NoStatusField -Object $step -Context "public POI step $($step.stepCode)"
}

Write-Host 'Phase 29 POI experience smoke passed'
