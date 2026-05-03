param(
  [string]$AdminBaseUrl = '',
  [switch]$SkipActions
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
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

function Assert-True {
  param([Parameter(Mandatory = $true)][bool]$Condition, [Parameter(Mandatory = $true)][string]$Message)
  if (-not $Condition) { throw $Message }
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
    $envelope = $raw | ConvertFrom-Json
    if ($envelope.code -ne 0 -and $envelope.code -ne 200) {
      throw "$Method $Url failed: code=$($envelope.code), message=$($envelope.message)"
    }
    return $envelope.data
  } finally {
    if ($request) { $request.Dispose() }
    $client.Dispose()
  }
}

function Get-AdminToken {
  param([Parameter(Mandatory = $true)][string]$BaseUrl)

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE37_ADMIN_BEARER_TOKEN')
  if ($bearer) { return $bearer }

  $fallbackBearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE36_ADMIN_BEARER_TOKEN')
  if ($fallbackBearer) { return $fallbackBearer }

  $username = Get-EnvValue -Name 'PHASE37_ADMIN_USERNAME'
  $password = Get-EnvValue -Name 'PHASE37_ADMIN_PASSWORD'
  if (-not $username) { $username = Get-EnvValue -Name 'PHASE36_ADMIN_USERNAME' }
  if (-not $password) { $password = Get-EnvValue -Name 'PHASE36_ADMIN_PASSWORD' }
  if (-not $username) { $username = 'admin' }
  if (-not $password) { $password = 'admin123' }

  $login = Invoke-Api -Method POST -Url "$BaseUrl/api/admin/v1/auth/login" -Body @{
    username = $username
    password = $password
  }
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$login.token)) -Message 'Admin login did not return a token.'
  return [string]$login.token
}

function Get-FirstPageItem {
  param([Parameter(Mandatory = $true)]$Page, [Parameter(Mandatory = $true)][string]$Context)
  Assert-True -Condition ($Page.PSObject.Properties.Name -contains 'list') -Message "$Context did not return PageResponse.list"
  $items = @($Page.list)
  Assert-True -Condition ($items.Count -gt 0) -Message "$Context returned no items"
  return $items[0]
}

$AdminBaseUrl = Resolve-Default -Value $AdminBaseUrl -EnvName 'PHASE37_ADMIN_BASE_URL' -Fallback 'http://127.0.0.1:8081'
$token = Get-AdminToken -BaseUrl $AdminBaseUrl
$packageCode = 'east_west_war_and_coexistence_package'

$packagePage = Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/material-packages?pageNum=1&pageSize=20&keyword=$packageCode" -Token $token
$package = @($packagePage.list | Where-Object { [string]$_.code -eq $packageCode } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $package) -Message "Package not found: $packageCode"

$overview = Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)/qa/overview" -Token $token
Assert-True -Condition ([int]$overview.totalItems -gt 0) -Message '/qa/overview returned zero totalItems'
Assert-True -Condition ($overview.assetKindCounters.PSObject.Properties.Name.Count -gt 0) -Message '/qa/overview returned empty assetKindCounters'

$imageItems = Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)/qa/items?pageNum=1&pageSize=5&assetKind=image" -Token $token
Assert-True -Condition ($imageItems.total -gt 0) -Message 'QA image filter returned no items'
$audioItems = Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)/qa/items?pageNum=1&pageSize=5&assetKind=audio" -Token $token
Assert-True -Condition ($audioItems.total -gt 0) -Message 'QA audio filter returned no items'
$videoItems = Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)/qa/items?pageNum=1&pageSize=5&assetKind=video" -Token $token
Assert-True -Condition ($videoItems.total -gt 0) -Message 'QA video filter returned no items'
$usableItems = Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)/qa/items?pageNum=1&pageSize=5&healthState=usable" -Token $token
Assert-True -Condition ($usableItems.total -gt 0) -Message 'QA usable health filter returned no items'

$sampleItem = Get-FirstPageItem -Page $imageItems -Context 'QA image items'
$detail = Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)/qa/items/$($sampleItem.id)" -Token $token
Assert-True -Condition (@($detail.versions).Count -gt 0) -Message 'QA detail returned no versions'
Assert-True -Condition ($detail.item.id -eq $sampleItem.id) -Message 'QA detail returned wrong item'

$consistency = Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)/qa/consistency-check" -Token $token -Body @{
  includeCosHead = $false
  includeLocalFileCheck = $false
  maxCosChecks = 0
  runtimeOnly = $false
}
Assert-True -Condition ($null -ne $consistency.blockingCount) -Message '/qa/consistency-check missing blockingCount'
Assert-True -Condition ($null -ne $consistency.warningCount) -Message '/qa/consistency-check missing warningCount'
Assert-True -Condition ($null -ne $consistency.infoCount) -Message '/qa/consistency-check missing infoCount'

$knownKey = 'story_cover_copper_mirror'
$assetSearch = Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/assets?pageNum=1&pageSize=20&keyword=$knownKey" -Token $token
$assetMatch = @($assetSearch.list | Where-Object { [string]$_.materialItemKey -eq $knownKey -or [string]$_.materialPackageCode -eq $packageCode } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $assetMatch) -Message 'Content asset search did not return material package metadata'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$assetMatch.materialPackageCode)) -Message 'Asset metadata missing materialPackageCode'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$assetMatch.materialItemKey)) -Message 'Asset metadata missing materialItemKey'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$assetMatch.materialPromotionStatus)) -Message 'Asset metadata missing materialPromotionStatus'

if (-not $SkipActions) {
  $actionItem = @($usableItems.list | Where-Object { $_.currentVersionId -and $_.assetId } | Select-Object -First 1)[0]
  if ($actionItem) {
    $approve = Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)/qa/items/$($actionItem.id)/approve" -Token $token -Body @{
      versionId = $actionItem.currentVersionId
      targetStatus = 'approved'
      note = 'Phase 37 smoke approve idempotent check'
      confirmedImpact = $false
    }
    Assert-True -Condition ([string]$approve.targetStatus -eq 'approved') -Message 'QA approve action failed'
  } else {
    Write-Host 'QA action flow skipped because no safe currentVersionId + assetId item was found.'
  }
}

Write-Host 'Material QA smoke passed'
