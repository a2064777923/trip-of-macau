param(
  [string]$AdminBaseUrl = '',
  [string]$PublicBaseUrl = '',
  [string]$MySqlExe = '',
  [string]$MySqlHost = '',
  [int]$MySqlPort = 0,
  [string]$MySqlDatabase = '',
  [string]$MySqlUser = '',
  [string]$MySqlPassword = ''
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
Add-Type -AssemblyName System.Net.Http

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

function ConvertTo-CompactJson {
  param($Value)

  if ($null -eq $Value) {
    return ''
  }
  return ($Value | ConvertTo-Json -Depth 100 -Compress)
}

function Invoke-ApiDetailed {
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
      $request.Headers.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue(
        'Bearer',
        (Normalize-BearerToken -Token $Token)
      )
    }

    if ($null -ne $Body) {
      $json = ConvertTo-CompactJson -Value $Body
      $request.Content = New-Object System.Net.Http.StringContent($json, [System.Text.Encoding]::UTF8, 'application/json')
    }

    $response = $client.SendAsync($request).GetAwaiter().GetResult()
    $bytes = $response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
    $raw = [System.Text.Encoding]::UTF8.GetString($bytes)
    $jsonResponse = $null
    if (-not [string]::IsNullOrWhiteSpace($raw)) {
      try {
        $jsonResponse = $raw | ConvertFrom-Json
      } catch {
        $jsonResponse = $null
      }
    }

    return [pscustomobject]@{
      StatusCode = [int]$response.StatusCode
      Success = $response.IsSuccessStatusCode
      Raw = $raw
      Json = $jsonResponse
    }
  } finally {
    if ($request) {
      $request.Dispose()
    }
    $client.Dispose()
  }
}

function Invoke-Api {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url,
    [string]$Token,
    $Body
  )

  $result = Invoke-ApiDetailed -Method $Method -Url $Url -Token $Token -Body $Body
  if (-not $result.Success) {
    throw "$Method $Url failed with status $($result.StatusCode): $($result.Raw)"
  }
  return $result.Json
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

function Assert-Any {
  param(
    [Parameter(Mandatory = $true)]$Items,
    [Parameter(Mandatory = $true)][scriptblock]$Predicate,
    [Parameter(Mandatory = $true)][string]$Context
  )

  $matches = @($Items | Where-Object $Predicate)
  Assert-True -Condition ($matches.Count -ge 1) -Message $Context
}

function Get-PageItems {
  param(
    [Parameter(Mandatory = $true)]$Data,
    [Parameter(Mandatory = $true)][string]$Context
  )

  Assert-True -Condition ($Data.PSObject.Properties.Name -contains 'list') -Message "$Context did not return a PageResponse list"
  return ,@($Data.list)
}

function Parse-DbUrl {
  param([string]$DbUrl)

  if ([string]::IsNullOrWhiteSpace($DbUrl)) {
    return $null
  }

  $match = [regex]::Match($DbUrl, 'jdbc:mysql://(?<host>[^:/?]+):(?<port>\d+)/(?<db>[^?]+)')
  if (-not $match.Success) {
    return $null
  }

  return [pscustomobject]@{
    Host = $match.Groups['host'].Value
    Port = [int]$match.Groups['port'].Value
    Database = $match.Groups['db'].Value
  }
}

function Get-LocalMySqlDefaults {
  param([Parameter(Mandatory = $true)][string]$ProjectRoot)

  $defaults = [ordered]@{
    Host = '127.0.0.1'
    Port = 3306
    Database = 'aoxiaoyou'
    User = 'root'
    Password = ''
  }

  $adminLocalPath = Join-Path $ProjectRoot 'packages/admin/aoxiaoyou-admin-backend/src/main/resources/application-local.yml'
  if (Test-Path -LiteralPath $adminLocalPath) {
    $content = Get-Content -LiteralPath $adminLocalPath -Raw -Encoding UTF8
    $urlMatch = [regex]::Match($content, 'url:\s*\$\{DB_URL:(?<value>[^}]+)\}')
    if ($urlMatch.Success) {
      $parsed = Parse-DbUrl -DbUrl $urlMatch.Groups['value'].Value
      if ($parsed) {
        $defaults.Host = $parsed.Host
        $defaults.Port = $parsed.Port
        $defaults.Database = $parsed.Database
      }
    }
    $userMatch = [regex]::Match($content, 'username:\s*\$\{DB_USERNAME:(?<value>[^}]+)\}')
    if ($userMatch.Success) {
      $defaults.User = $userMatch.Groups['value'].Value.Trim()
    }
    $passwordMatch = [regex]::Match($content, 'password:\s*\$\{DB_PASSWORD:(?<value>[^}]+)\}')
    if ($passwordMatch.Success) {
      $defaults.Password = $passwordMatch.Groups['value'].Value.Trim()
    }
  }

  if ([string]::IsNullOrWhiteSpace($defaults.Password)) {
    $dockerComposePath = Join-Path $ProjectRoot 'docker-compose.local.yml'
    if (Test-Path -LiteralPath $dockerComposePath) {
      $compose = Get-Content -LiteralPath $dockerComposePath -Raw -Encoding UTF8
      $composePassword = [regex]::Match($compose, 'MYSQL_ROOT_PASSWORD:\s*(?<value>[^\r\n]+)')
      if ($composePassword.Success) {
        $defaults.Password = $composePassword.Groups['value'].Value.Trim()
      }
    }
  }

  return [pscustomobject]$defaults
}

function Resolve-MySqlExe {
  param([string]$Candidate)

  if (-not [string]::IsNullOrWhiteSpace($Candidate) -and (Test-Path -LiteralPath $Candidate)) {
    return $Candidate
  }
  $envCandidate = Get-EnvValue -Name 'PHASE35_MYSQL_EXE'
  if ($envCandidate -and (Test-Path -LiteralPath $envCandidate)) {
    return $envCandidate
  }
  $localCandidate = 'D:\Software\mysql-8.0.41-winx64\bin\mysql.exe'
  if (Test-Path -LiteralPath $localCandidate) {
    return $localCandidate
  }
  return 'mysql'
}

function Resolve-MySqlSettings {
  param(
    [Parameter(Mandatory = $true)][string]$ProjectRoot,
    [string]$MySqlHostValue,
    [int]$MySqlPortValue,
    [string]$MySqlDatabaseValue,
    [string]$MySqlUserValue,
    [string]$MySqlPasswordValue
  )

  $defaults = Get-LocalMySqlDefaults -ProjectRoot $ProjectRoot

  $resolvedHost = if (-not [string]::IsNullOrWhiteSpace($MySqlHostValue)) { $MySqlHostValue.Trim() } elseif (Get-EnvValue -Name 'PHASE35_MYSQL_HOST') { Get-EnvValue -Name 'PHASE35_MYSQL_HOST' } elseif (Get-EnvValue -Name 'DB_HOST') { Get-EnvValue -Name 'DB_HOST' } else { $defaults.Host }
  $resolvedPort = if ($MySqlPortValue -gt 0) { $MySqlPortValue } elseif (Get-EnvValue -Name 'PHASE35_MYSQL_PORT') { [int](Get-EnvValue -Name 'PHASE35_MYSQL_PORT') } elseif (Get-EnvValue -Name 'DB_PORT') { [int](Get-EnvValue -Name 'DB_PORT') } else { $defaults.Port }
  $resolvedDatabase = if (-not [string]::IsNullOrWhiteSpace($MySqlDatabaseValue)) { $MySqlDatabaseValue.Trim() } elseif (Get-EnvValue -Name 'PHASE35_MYSQL_DATABASE') { Get-EnvValue -Name 'PHASE35_MYSQL_DATABASE' } elseif (Get-EnvValue -Name 'DB_NAME') { Get-EnvValue -Name 'DB_NAME' } else { $defaults.Database }
  $resolvedUser = if (-not [string]::IsNullOrWhiteSpace($MySqlUserValue)) { $MySqlUserValue.Trim() } elseif (Get-EnvValue -Name 'PHASE35_MYSQL_USER') { Get-EnvValue -Name 'PHASE35_MYSQL_USER' } elseif (Get-EnvValue -Name 'DB_USERNAME') { Get-EnvValue -Name 'DB_USERNAME' } else { $defaults.User }
  $resolvedPassword = if (-not [string]::IsNullOrWhiteSpace($MySqlPasswordValue)) { $MySqlPasswordValue } elseif (Get-EnvValue -Name 'PHASE35_MYSQL_PASSWORD') { Get-EnvValue -Name 'PHASE35_MYSQL_PASSWORD' } elseif (Get-EnvValue -Name 'MYSQL_PWD') { Get-EnvValue -Name 'MYSQL_PWD' } elseif (Get-EnvValue -Name 'DB_PASSWORD') { Get-EnvValue -Name 'DB_PASSWORD' } else { $defaults.Password }

  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($resolvedUser)) -Message 'MySQL user is required. Set PHASE35_MYSQL_USER or DB_USERNAME.'
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($resolvedPassword)) -Message 'MySQL password is required. Set PHASE35_MYSQL_PASSWORD, MYSQL_PWD, or DB_PASSWORD.'

  return [pscustomobject]@{
    Host = $resolvedHost
    Port = $resolvedPort
    Database = $resolvedDatabase
    User = $resolvedUser
    Password = $resolvedPassword
  }
}

function Invoke-MySqlFile {
  param(
    [Parameter(Mandatory = $true)][pscustomobject]$Settings,
    [Parameter(Mandatory = $true)][string]$Path
  )

  Assert-True -Condition (Test-Path -LiteralPath $Path) -Message "Missing SQL file: $Path"

  $previousPassword = $env:MYSQL_PWD
  try {
    $env:MYSQL_PWD = $Settings.Password
    $normalizedPath = (Resolve-Path -LiteralPath $Path).Path.Replace('\', '/')
    $output = & $script:ResolvedMySqlExe `
      --default-character-set=utf8mb4 `
      --host=$($Settings.Host) `
      --port=$($Settings.Port) `
      --user=$($Settings.User) `
      --database=$($Settings.Database) `
      --execute="SOURCE $normalizedPath" 2>&1
    if ($LASTEXITCODE -ne 0) {
      throw "mysql import failed for $Path`n$($output -join "`n")"
    }
  } finally {
    $env:MYSQL_PWD = $previousPassword
  }
}

function Invoke-MySqlStatement {
  param(
    [Parameter(Mandatory = $true)][pscustomobject]$Settings,
    [Parameter(Mandatory = $true)][string]$Sql
  )

  $previousPassword = $env:MYSQL_PWD
  try {
    $env:MYSQL_PWD = $Settings.Password
    $output = & $script:ResolvedMySqlExe `
      --default-character-set=utf8mb4 `
      --batch `
      --raw `
      --skip-column-names `
      --host=$($Settings.Host) `
      --port=$($Settings.Port) `
      --user=$($Settings.User) `
      --database=$($Settings.Database) `
      --execute=$Sql 2>&1
    if ($LASTEXITCODE -ne 0) {
      throw "mysql statement failed`n$($output -join "`n")"
    }
    return (($output | Where-Object { -not [string]::IsNullOrWhiteSpace([string]$_) } | Select-Object -Last 1 | Out-String).Trim())
  } finally {
    $env:MYSQL_PWD = $previousPassword
  }
}

function Reset-Phase29PoiDefaultFlowSeed {
  param([Parameter(Mandatory = $true)][pscustomobject]$Settings)

  $knownStepCodes = @(
    'tap_intro',
    'start_route_guidance',
    'arrival_intro_media',
    'release_checkin_tasks',
    'pickup_side_clues',
    'hidden_dwell_achievement',
    'completion_reward_title',
    'route_guidance',
    'checkin_task_release',
    'hidden_dwell_title'
  )
  $quotedCodes = ($knownStepCodes | ForEach-Object { "'$($_)'" }) -join ','
  $sql = @"
SET @flow_ama_default_id = (
  SELECT id
  FROM experience_flows
  WHERE code = 'poi_ama_default_walk_in'
  LIMIT 1
);
DELETE FROM experience_flow_steps
WHERE @flow_ama_default_id IS NOT NULL
  AND flow_id = @flow_ama_default_id
  AND step_code IN ($quotedCodes);
"@

  Invoke-MySqlStatement -Settings $Settings -Sql $sql | Out-Null
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

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE35_ADMIN_BEARER_TOKEN')
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

  $username = Get-EnvValue -Name 'PHASE35_ADMIN_USERNAME'
  $password = Get-EnvValue -Name 'PHASE35_ADMIN_PASSWORD'
  if ((-not $username -or -not $password) -and $fileCredentials) {
    $username = [string]$fileCredentials.username
    $password = [string]$fileCredentials.password
  }

  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($username)) -Message 'Admin auth requires PHASE35_ADMIN_BEARER_TOKEN or PHASE35_ADMIN_USERNAME.'
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($password)) -Message 'Admin auth requires PHASE35_ADMIN_BEARER_TOKEN or PHASE35_ADMIN_PASSWORD.'

  $login = Ensure-Success -Context 'admin login' -Response (
    Invoke-Api -Method POST -Url "$BaseUrl/api/admin/v1/auth/login" -Body @{ username = $username; password = $password }
  )
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$login.token)) -Message 'Admin login token is missing'
  return [string]$login.token
}

function Test-PublicBackendReachable {
  param([Parameter(Mandatory = $true)][string]$BaseUrl)

  try {
    $response = Invoke-ApiDetailed -Method GET -Url "$BaseUrl/api/v1/story-lines?locale=zh-Hant"
    return $response.Success
  } catch {
    return $false
  }
}

function Find-StorylineInPublicList {
  param(
    [Parameter(Mandatory = $true)][string]$BaseUrl,
    [Parameter(Mandatory = $true)][string]$Code
  )

  $response = Invoke-Api -Method GET -Url "$BaseUrl/api/v1/story-lines?locale=zh-Hant"
  $items = @(Ensure-Success -Context 'public storylines list' -Response $response)
  return @($items | Where-Object { [string]$_.code -eq $Code }) | Select-Object -First 1
}

$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$AdminBaseUrl = Resolve-Default -Value $AdminBaseUrl -EnvName 'PHASE35_ADMIN_BASE_URL' -Fallback 'http://127.0.0.1:8081'
$PublicBaseUrl = Resolve-Default -Value $PublicBaseUrl -EnvName 'PHASE35_PUBLIC_BASE_URL' -Fallback 'http://127.0.0.1:8080'
$script:ResolvedMySqlExe = Resolve-MySqlExe -Candidate $MySqlExe
$mySqlSettings = Resolve-MySqlSettings -ProjectRoot $ProjectRoot -MySqlHostValue $MySqlHost -MySqlPortValue $MySqlPort -MySqlDatabaseValue $MySqlDatabase -MySqlUserValue $MySqlUser -MySqlPasswordValue $MySqlPassword

$seedFiles = @(
  'scripts/local/mysql/init/38-phase-28-story-content-and-lottie.sql',
  'scripts/local/mysql/init/39-phase-28-experience-orchestration.sql',
  'scripts/local/mysql/init/40-phase-29-poi-default-experience.sql',
  'scripts/local/mysql/init/41-phase-30-storyline-mode-overrides.sql',
  'scripts/local/mysql/init/42-phase-31-interaction-template-governance.sql',
  'scripts/local/mysql/init/43-phase-32-progress-engine.sql',
  'scripts/local/mysql/init/44-phase-32-story-sessions-and-timeline.sql',
  'scripts/local/mysql/init/45-phase-32-progress-repair-and-audit.sql',
  'scripts/local/mysql/init/47-phase-33-story-material-package-model.sql',
  'scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql',
  'scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql',
  'scripts/local/mysql/init/50-phase-35-lifecycle-operations.sql'
)

foreach ($relativePath in $seedFiles) {
  if ($relativePath -eq 'scripts/local/mysql/init/40-phase-29-poi-default-experience.sql') {
    Reset-Phase29PoiDefaultFlowSeed -Settings $mySqlSettings
  }
  Invoke-MySqlFile -Settings $mySqlSettings -Path (Join-Path $ProjectRoot $relativePath)
}

$storylineCode = 'east_west_war_and_coexistence'
$adminToken = Get-AdminToken -BaseUrl $AdminBaseUrl -ProjectRoot $ProjectRoot

$targetTypes = @(Ensure-Success -Context 'lifecycle target types' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/operations/lifecycle/target-types" -Token $adminToken
))
Assert-Any -Items $targetTypes -Context 'Lifecycle target types should include storyline' -Predicate { [string]$_.targetType -eq 'storyline' }
Assert-Any -Items $targetTypes -Context 'Lifecycle target types should include story_chapter' -Predicate { [string]$_.targetType -eq 'story_chapter' }

$statuses = @(Ensure-Success -Context 'lifecycle statuses' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/operations/lifecycle/statuses" -Token $adminToken
))
Assert-Any -Items $statuses -Context 'Lifecycle statuses should include published' -Predicate { [string]$_.canonicalStatus -eq 'published' }
Assert-Any -Items $statuses -Context 'Lifecycle statuses should include unpublished' -Predicate { [string]$_.canonicalStatus -eq 'unpublished' }
Assert-Any -Items $statuses -Context 'Lifecycle statuses should include deleted' -Predicate { [string]$_.canonicalStatus -eq 'deleted' }

$targetsPage = Ensure-Success -Context 'lifecycle target search' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/operations/lifecycle/targets?pageNum=1&pageSize=50&targetType=storyline&keyword=$storylineCode" -Token $adminToken
)
$targets = Get-PageItems -Data $targetsPage -Context 'lifecycle target search'
$target = @($targets | Where-Object { [string]$_.targetCode -eq $storylineCode }) | Select-Object -First 1
Assert-True -Condition ($null -ne $target) -Message "Lifecycle target $storylineCode was not found"
Assert-True -Condition ([string]$target.canonicalStatus -eq 'published') -Message "Expected seeded storyline to start as published, got $($target.canonicalStatus)"

$preview = Ensure-Success -Context 'lifecycle preview' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/operations/lifecycle/preview" -Token $adminToken -Body @{
    targetType = 'storyline'
    targetId = [long]$target.targetId
    targetCode = $storylineCode
    action = 'unpublish'
    executionMode = 'scheduled'
    cascade = $true
    metadata = @{
      source = 'phase35-smoke'
    }
  }
)
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$preview.previewHash)) -Message 'Preview did not return previewHash'
Assert-True -Condition ($null -ne $preview.impactCounters) -Message 'Preview did not return impactCounters'
Assert-True -Condition (($preview.PSObject.Properties.Name -contains 'impacts') -and $null -ne $preview.impacts) -Message 'Preview did not return impacts'

$scheduledAt = (Get-Date).AddSeconds(-5).ToString('s')
$operation = Ensure-Success -Context 'create scheduled lifecycle operation' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/operations/lifecycle/operations" -Token $adminToken -Body @{
    targetType = 'storyline'
    targetId = [long]$target.targetId
    targetCode = $storylineCode
    action = 'unpublish'
    executionMode = 'scheduled'
    scheduledAt = $scheduledAt
    reason = 'Phase 35 smoke verifies dependency preview and scheduled lifecycle operations.'
    previewHash = [string]$preview.previewHash
    confirmedImpact = $true
    cascade = $true
    metadata = @{
      source = 'phase35-smoke'
      restoresTarget = $true
    }
  }
)
Assert-True -Condition ($null -ne $operation.id) -Message 'Scheduled lifecycle operation did not return id'
Assert-True -Condition ([string]$operation.operationStatus -eq 'scheduled') -Message "Expected scheduled operation, got $($operation.operationStatus)"

$dueOperations = @(Ensure-Success -Context 'run due lifecycle operations' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/operations/lifecycle/operations/run-due" -Token $adminToken
))
Assert-Any -Items $dueOperations -Context 'Due runner should apply the scheduled smoke operation' -Predicate { [long]$_.id -eq [long]$operation.id -and [string]$_.operationStatus -eq 'applied' }

$operationsPage = Ensure-Success -Context 'lifecycle operation history' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/operations/lifecycle/operations?pageNum=1&pageSize=20&targetType=storyline&keyword=$storylineCode" -Token $adminToken
)
$operations = Get-PageItems -Data $operationsPage -Context 'lifecycle operation history'
Assert-Any -Items $operations -Context 'Operation history should include the smoke operation' -Predicate { [long]$_.id -eq [long]$operation.id }

$detail = Ensure-Success -Context 'lifecycle operation detail' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/operations/lifecycle/operations/$($operation.id)" -Token $adminToken
)
Assert-True -Condition ($null -ne $detail.summary -and [long]$detail.summary.id -eq [long]$operation.id) -Message 'Operation detail summary is missing'
Assert-True -Condition (($detail.PSObject.Properties.Name -contains 'impacts') -and $null -ne $detail.impacts) -Message 'Operation detail impacts are missing'
Assert-True -Condition ($null -ne $detail.preview) -Message 'Operation detail preview JSON is missing'
Assert-True -Condition ($null -ne $detail.request) -Message 'Operation detail request JSON is missing'

$publicChecked = $false
if (Test-PublicBackendReachable -BaseUrl $PublicBaseUrl) {
  $hiddenStoryline = Find-StorylineInPublicList -BaseUrl $PublicBaseUrl -Code $storylineCode
  Assert-True -Condition ($null -eq $hiddenStoryline) -Message 'Unpublished storyline should not appear in public story list'
  $publicChecked = $true
} else {
  Write-Host 'Skipping public runtime filtering check because the public backend is not reachable.'
}

$restorePreview = Ensure-Success -Context 'restore lifecycle preview' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/operations/lifecycle/preview" -Token $adminToken -Body @{
    targetType = 'storyline'
    targetId = [long]$target.targetId
    targetCode = $storylineCode
    action = 'publish'
    executionMode = 'immediate'
    cascade = $true
    metadata = @{
      source = 'phase35-smoke-restore'
    }
  }
)
$restoreOperation = Ensure-Success -Context 'restore lifecycle operation' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/operations/lifecycle/operations" -Token $adminToken -Body @{
    targetType = 'storyline'
    targetId = [long]$target.targetId
    targetCode = $storylineCode
    action = 'publish'
    executionMode = 'immediate'
    reason = 'Phase 35 smoke restores seeded flagship storyline after verification.'
    previewHash = [string]$restorePreview.previewHash
    confirmedImpact = $true
    cascade = $true
    metadata = @{
      source = 'phase35-smoke-restore'
    }
  }
)
Assert-True -Condition ([string]$restoreOperation.operationStatus -eq 'applied') -Message "Restore operation should be applied, got $($restoreOperation.operationStatus)"

if ($publicChecked) {
  $visibleStoryline = Find-StorylineInPublicList -BaseUrl $PublicBaseUrl -Code $storylineCode
  Assert-True -Condition ($null -ne $visibleStoryline) -Message 'Restored published storyline should appear in public story list'
}

$tableCount = Invoke-MySqlStatement -Settings $mySqlSettings -Sql "SELECT COUNT(*) FROM content_lifecycle_operations WHERE target_type = 'storyline' AND target_code = '$storylineCode' AND deleted = 0;"
Assert-True -Condition ([int]$tableCount -ge 2) -Message 'Lifecycle operation rows were not persisted'

Write-Host 'Phase 35 lifecycle smoke passed'
