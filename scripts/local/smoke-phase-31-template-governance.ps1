param(
  [string]$AdminBaseUrl = ''
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Add-Type -AssemblyName System.Net.Http

function Get-EnvValue {
  param([Parameter(Mandatory = $true)][string]$Name)
  $value = [Environment]::GetEnvironmentVariable($Name)
  if ([string]::IsNullOrWhiteSpace($value)) { return $null }
  return $value.Trim()
}

function Resolve-Default {
  param([string]$Value, [string]$EnvName, [string]$Fallback)
  if (-not [string]::IsNullOrWhiteSpace($Value)) { return $Value.TrimEnd('/') }
  $envValue = Get-EnvValue -Name $EnvName
  if ($envValue) { return $envValue.TrimEnd('/') }
  return $Fallback.TrimEnd('/')
}

function Normalize-BearerToken {
  param([string]$Token)
  if ([string]::IsNullOrWhiteSpace($Token)) { return $null }
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
    if ([string]::IsNullOrWhiteSpace($raw)) { return $null }
    return $raw | ConvertFrom-Json
  } finally {
    if ($request) { $request.Dispose() }
    $client.Dispose()
  }
}

function Ensure-Success {
  param([Parameter(Mandatory = $true)]$Response, [Parameter(Mandatory = $true)][string]$Context)
  if ($null -eq $Response) { throw "$Context returned an empty response" }
  if ($Response.code -ne 0 -and $Response.code -ne 200) {
    throw "$Context failed: code=$($Response.code), message=$($Response.message)"
  }
  return $Response.data
}

function Assert-True {
  param([Parameter(Mandatory = $true)][bool]$Condition, [Parameter(Mandatory = $true)][string]$Message)
  if (-not $Condition) { throw $Message }
}

function Assert-Contains {
  param([Parameter(Mandatory = $true)]$Items, [Parameter(Mandatory = $true)][string]$Expected, [Parameter(Mandatory = $true)][string]$Context)
  Assert-True -Condition (@($Items | Where-Object { [string]$_ -eq $Expected }).Count -ge 1) -Message "$Context missing expected value: $Expected"
}

function Get-PageItems {
  param([Parameter(Mandatory = $true)]$Data, [Parameter(Mandatory = $true)][string]$Context)
  Assert-True -Condition ($Data.PSObject.Properties.Name -contains 'list') -Message "$Context did not return a PageResponse list"
  return ,@($Data.list)
}

function Get-AdminCredentialsFromFile {
  param([Parameter(Mandatory = $true)][string]$ProjectRoot)
  $path = Join-Path $ProjectRoot 'tmp-admin-login.json'
  if (-not (Test-Path -LiteralPath $path)) { return $null }
  return Get-Content -LiteralPath $path -Raw -Encoding UTF8 | ConvertFrom-Json
}

function Get-AdminToken {
  param([Parameter(Mandatory = $true)][string]$BaseUrl, [Parameter(Mandatory = $true)][string]$ProjectRoot)
  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE31_ADMIN_BEARER')
  if ($bearer) { return $bearer }

  $fileCredentials = Get-AdminCredentialsFromFile -ProjectRoot $ProjectRoot
  if ($fileCredentials) {
    $fileTokenCandidate = $null
    if ($fileCredentials.PSObject.Properties.Name -contains 'token') {
      $fileTokenCandidate = [string]$fileCredentials.token
    } elseif ($fileCredentials.PSObject.Properties.Name -contains 'accessToken') {
      $fileTokenCandidate = [string]$fileCredentials.accessToken
    }
    $fileToken = Normalize-BearerToken -Token $fileTokenCandidate
    if ($fileToken) { return $fileToken }
  }

  $username = Get-EnvValue -Name 'PHASE31_ADMIN_USERNAME'
  $password = Get-EnvValue -Name 'PHASE31_ADMIN_PASSWORD'
  if ((-not $username -or -not $password) -and $fileCredentials) {
    $username = [string]$fileCredentials.username
    $password = [string]$fileCredentials.password
  }
  if (-not $username) { $username = 'admin' }
  if (-not $password) { $password = 'admin123' }

  $login = Ensure-Success -Context 'admin login' -Response (
    Invoke-Api -Method POST -Url "$BaseUrl/api/admin/v1/auth/login" -Body @{ username = $username; password = $password }
  )
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$login.token)) -Message 'Admin login token is missing'
  return [string]$login.token
}

$AdminBaseUrl = Resolve-Default -Value $AdminBaseUrl -EnvName 'PHASE31_ADMIN_BASE_URL' -Fallback 'http://127.0.0.1:8081'
$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$adminToken = Get-AdminToken -BaseUrl $AdminBaseUrl -ProjectRoot $ProjectRoot

$presets = Ensure-Success -Context 'template presets' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/experience/templates/presets" -Token $adminToken
)
$presetCodes = @($presets | ForEach-Object { [string]$_.presetCode })
Assert-Contains -Items $presetCodes -Expected 'presentation.fullscreen_media' -Context 'template presets'
Assert-Contains -Items $presetCodes -Expected 'task_gameplay.quiz' -Context 'template presets'
Assert-Contains -Items $presetCodes -Expected 'trigger_effect.grant_badge_title' -Context 'template presets'

$taskTemplatesPage = Ensure-Success -Context 'task gameplay templates' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/experience/templates?templateType=task_gameplay&pageNum=1&pageSize=20" -Token $adminToken
)
$taskTemplates = Get-PageItems -Data $taskTemplatesPage -Context 'task gameplay templates'
Assert-True -Condition (@($taskTemplates).Count -ge 1) -Message 'Expected at least one task_gameplay template'

$templatePage = Ensure-Success -Context 'fullscreen media template search' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/experience/templates?keyword=presentation.fullscreen_media&pageNum=1&pageSize=20" -Token $adminToken
)
$templateItems = Get-PageItems -Data $templatePage -Context 'fullscreen media template search'
$fullscreenTemplate = @($templateItems | Where-Object { $_.code -eq 'presentation.fullscreen_media' }) | Select-Object -First 1
Assert-True -Condition ($null -ne $fullscreenTemplate) -Message 'Seeded presentation.fullscreen_media template was not found'

$usagePath = 'templates/{0}/usage' -f $fullscreenTemplate.id
$usage = Ensure-Success -Context 'template usage' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/experience/$usagePath" -Token $adminToken
)
Assert-True -Condition ([int]$usage.usageCount -gt 0) -Message 'Expected presentation.fullscreen_media to have usageCount > 0'

$conflictItemsPage = Ensure-Success -Context 'governance conflict items' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/experience/governance/items?conflictOnly=true&pageNum=1&pageSize=20" -Token $adminToken
)
$conflictItems = Get-PageItems -Data $conflictItemsPage -Context 'governance conflict items'
Assert-True -Condition (@($conflictItems).Count -ge 1) -Message 'Expected at least one governance conflict item'

$conflicts = Ensure-Success -Context 'governance conflict check' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/experience/governance/check" -Token $adminToken -Body @{}
)
$findingTypes = @($conflicts | ForEach-Object { [string]$_.findingType })
Assert-Contains -Items $findingTypes -Expected 'overlapping_fullscreen_effect' -Context 'governance conflict check'
Assert-Contains -Items $findingTypes -Expected 'required_step_disabled' -Context 'governance conflict check'

Write-Host 'Phase 31 template governance smoke passed'
