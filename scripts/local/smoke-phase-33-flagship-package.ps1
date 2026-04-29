param(
  [string]$AdminBaseUrl = '',
  [string]$MySqlExe = '',
  [string]$MySqlDatabase = ''
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
      $json = $Body | ConvertTo-Json -Depth 100 -Compress
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

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE33_ADMIN_BEARER')
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

  $username = Get-EnvValue -Name 'PHASE33_ADMIN_USERNAME'
  $password = Get-EnvValue -Name 'PHASE33_ADMIN_PASSWORD'
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

function Resolve-MySqlExe {
  param([string]$Candidate)
  if (-not [string]::IsNullOrWhiteSpace($Candidate) -and (Test-Path -LiteralPath $Candidate)) { return $Candidate }
  $envCandidate = Get-EnvValue -Name 'PHASE33_MYSQL_EXE'
  if ($envCandidate -and (Test-Path -LiteralPath $envCandidate)) { return $envCandidate }
  $localCandidate = 'D:\Software\mysql-8.0.41-winx64\bin\mysql.exe'
  if (Test-Path -LiteralPath $localCandidate) { return $localCandidate }
  return 'mysql'
}

function Invoke-MySqlScalar {
  param(
    [Parameter(Mandatory = $true)][string]$Sql,
    [Parameter(Mandatory = $true)][string]$Context
  )

  $queryPath = Join-Path ([System.IO.Path]::GetTempPath()) ("phase33-smoke-{0}.sql" -f ([Guid]::NewGuid().ToString('N')))
  [System.IO.File]::WriteAllText($queryPath, "SET NAMES utf8mb4;`n$Sql`n", (New-Object System.Text.UTF8Encoding($false)))
  try {
    $hostName = Get-EnvValue -Name 'PHASE33_MYSQL_HOST'
    if (-not $hostName) { $hostName = '127.0.0.1' }
    $port = Get-EnvValue -Name 'PHASE33_MYSQL_PORT'
    if (-not $port) { $port = '3306' }
    $user = Get-EnvValue -Name 'PHASE33_MYSQL_USER'
    if (-not $user) { $user = 'root' }
    $pwd = Get-EnvValue -Name 'PHASE33_MYSQL_PWD'
    if (-not $pwd) { $pwd = Get-EnvValue -Name 'MYSQL_PWD' }
    if (-not $pwd) { $pwd = 'Abc123456' }

    $previousPwd = [Environment]::GetEnvironmentVariable('MYSQL_PWD')
    [Environment]::SetEnvironmentVariable('MYSQL_PWD', $pwd)
    try {
      $output = & $script:ResolvedMySqlExe `
        --default-character-set=utf8mb4 `
        --batch `
        --raw `
        --skip-column-names `
        --user=$user `
        --host=$hostName `
        --port=$port `
        --database=$script:ResolvedMySqlDatabase `
        --execute="SOURCE $($queryPath.Replace('\', '/'))" 2>&1
      if ($LASTEXITCODE -ne 0) {
        throw "$Context MySQL query failed: $output"
      }
      return (($output | Out-String).Trim())
    } finally {
      [Environment]::SetEnvironmentVariable('MYSQL_PWD', $previousPwd)
    }
  } finally {
    if (Test-Path -LiteralPath $queryPath) {
      Remove-Item -LiteralPath $queryPath -Force
    }
  }
}

function Assert-ScalarNumber {
  param(
    [Parameter(Mandatory = $true)][string]$Sql,
    [Parameter(Mandatory = $true)][string]$Context,
    [Parameter(Mandatory = $true)][scriptblock]$Predicate,
    [Parameter(Mandatory = $true)][string]$FailureMessage
  )

  $raw = Invoke-MySqlScalar -Sql $Sql -Context $Context
  $value = [int]$raw
  Assert-True -Condition (& $Predicate $value) -Message "$FailureMessage Actual=$value"
  return $value
}

$AdminBaseUrl = Resolve-Default -Value $AdminBaseUrl -EnvName 'PHASE33_ADMIN_BASE_URL' -Fallback 'http://127.0.0.1:8081'
$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$script:ResolvedMySqlExe = Resolve-MySqlExe -Candidate $MySqlExe
$script:ResolvedMySqlDatabase = if ([string]::IsNullOrWhiteSpace($MySqlDatabase)) {
  $envDb = Get-EnvValue -Name 'PHASE33_MYSQL_DATABASE'
  if ($envDb) { $envDb } else { 'aoxiaoyou' }
} else {
  $MySqlDatabase
}

$packageCode = 'east_west_war_and_coexistence_package'
$storylineCode = 'east_west_war_and_coexistence'
$adminToken = Get-AdminToken -BaseUrl $AdminBaseUrl -ProjectRoot $ProjectRoot

$packagePage = Ensure-Success -Context 'story material package search' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/material-packages?pageNum=1&pageSize=50&keyword=$packageCode" -Token $adminToken
)
$packages = Get-PageItems -Data $packagePage -Context 'story material package search'
$package = @($packages | Where-Object { [string]$_.code -eq $packageCode }) | Select-Object -First 1
Assert-True -Condition ($null -ne $package) -Message "Package $packageCode was not found through admin API"

$detail = Ensure-Success -Context 'story material package detail' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/content/material-packages/$($package.id)" -Token $adminToken
)
$items = @($detail.items)
Assert-True -Condition ($items.Count -ge 40) -Message "Expected at least 40 package items, got $($items.Count)"

$kindValues = @($items | ForEach-Object { @([string]$_.assetKind, [string]$_.itemType) } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
Assert-True -Condition (@($kindValues | Where-Object { $_ -eq 'lottie' }).Count -ge 1) -Message 'Package detail is missing a lottie item'
Assert-True -Condition (@($kindValues | Where-Object { $_ -eq 'audio' }).Count -ge 1) -Message 'Package detail is missing an audio item'
Assert-True -Condition (@($kindValues | Where-Object { $_ -eq 'image' -or $_ -eq 'icon' }).Count -ge 1) -Message 'Package detail is missing an image/icon item'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$detail.historicalBasisZht) -or -not [string]::IsNullOrWhiteSpace([string]$detail.historicalBasisZh)) -Message 'Package detail is missing historical basis text'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$detail.literaryDramatizationZht) -or -not [string]::IsNullOrWhiteSpace([string]$detail.literaryDramatizationZh)) -Message 'Package detail is missing literary dramatization text'

Assert-ScalarNumber -Context 'package item count' -FailureMessage 'Expected at least 40 package items in MySQL.' -Predicate { param($value) $value -ge 40 } -Sql @"
SELECT COUNT(*)
FROM story_material_package_items i
JOIN story_material_packages p ON p.id = i.package_id AND p.deleted = 0
WHERE p.code = '$packageCode' AND i.deleted = 0;
"@ | Out-Null

Assert-ScalarNumber -Context 'package asset item count' -FailureMessage 'Expected at least 40 asset-backed package items in MySQL.' -Predicate { param($value) $value -ge 40 } -Sql @"
SELECT COUNT(*)
FROM story_material_package_items i
JOIN story_material_packages p ON p.id = i.package_id AND p.deleted = 0
WHERE p.code = '$packageCode' AND i.deleted = 0 AND i.asset_id IS NOT NULL;
"@ | Out-Null

Assert-ScalarNumber -Context 'published chapter count' -FailureMessage 'Expected exactly five published chapters.' -Predicate { param($value) $value -eq 5 } -Sql @"
SELECT COUNT(*)
FROM story_chapters sc
JOIN storylines sl ON sl.id = sc.storyline_id AND sl.deleted = 0
WHERE sl.code = '$storylineCode' AND sc.deleted = 0 AND sc.status = 'published';
"@ | Out-Null

Assert-ScalarNumber -Context 'chapter experience flow null count' -FailureMessage 'Every chapter must have a non-null experience_flow_id.' -Predicate { param($value) $value -eq 0 } -Sql @"
SELECT COUNT(*)
FROM story_chapters sc
JOIN storylines sl ON sl.id = sc.storyline_id AND sl.deleted = 0
WHERE sl.code = '$storylineCode' AND sc.deleted = 0 AND sc.experience_flow_id IS NULL;
"@ | Out-Null

Assert-ScalarNumber -Context 'storyline exploration elements' -FailureMessage 'Expected at least 20 exploration elements for the storyline.' -Predicate { param($value) $value -ge 20 } -Sql @"
SELECT COUNT(*)
FROM exploration_elements ee
JOIN storylines sl ON sl.id = ee.storyline_id AND sl.deleted = 0
WHERE sl.code = '$storylineCode' AND ee.deleted = 0 AND ee.status = 'published';
"@ | Out-Null

Assert-ScalarNumber -Context 'required rewards' -FailureMessage 'Required finale reward/title rows are missing or have empty display text.' -Predicate { param($value) $value -eq 4 } -Sql @"
SELECT COUNT(*)
FROM game_rewards
WHERE deleted = 0
  AND code IN (
    'title_east_west_harbour_witness_final',
    'title_east_west_history_restoration_master',
    'title_east_west_harbour_history_grandmaster',
    'fragment_east_west_complete_copper_mirror'
  )
  AND status = 'published'
  AND COALESCE(name_zht, '') <> ''
  AND name_zht NOT LIKE '%�%'
  AND name_zht NOT LIKE '%?%';
"@ | Out-Null

Write-Host 'Phase 33 flagship package smoke passed'
