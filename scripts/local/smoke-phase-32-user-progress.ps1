param(
  [string]$AdminBaseUrl = '',
  [string]$PublicBaseUrl = '',
  [string]$Locale = 'zh-Hant',
  [string]$MySqlHost = '',
  [int]$MySqlPort = 0,
  [string]$MySqlDatabase = '',
  [string]$MySqlUser = '',
  [string]$MySqlPassword = '',
  [long]$SeededUserId = 320041,
  [long]$SeededPoiId = 9
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

function Assert-Equal {
  param(
    [Parameter(Mandatory = $true)]$Actual,
    [Parameter(Mandatory = $true)]$Expected,
    [Parameter(Mandatory = $true)][string]$Context
  )

  if ($Actual -ne $Expected) {
    throw "$Context expected '$Expected' but got '$Actual'"
  }
}

function Assert-ApproxNumber {
  param(
    [Parameter(Mandatory = $true)][double]$Actual,
    [Parameter(Mandatory = $true)][double]$Expected,
    [Parameter(Mandatory = $true)][string]$Context,
    [double]$Tolerance = 0.01
  )

  if ([Math]::Abs($Actual - $Expected) -gt $Tolerance) {
    throw "$Context expected approximately $Expected but got $Actual"
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

  $serverLocalPath = Join-Path $ProjectRoot 'packages/server/src/main/resources/application-local.yml'
  if (Test-Path -LiteralPath $serverLocalPath) {
    $content = Get-Content -LiteralPath $serverLocalPath -Raw -Encoding UTF8
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

  $dockerComposePath = Join-Path $ProjectRoot 'docker-compose.local.yml'
  if (Test-Path -LiteralPath $dockerComposePath) {
    $compose = Get-Content -LiteralPath $dockerComposePath -Raw -Encoding UTF8
    $composePassword = [regex]::Match($compose, 'MYSQL_ROOT_PASSWORD:\s*(?<value>[^\r\n]+)')
    if ($composePassword.Success -and [string]::IsNullOrWhiteSpace($defaults.Password)) {
      $defaults.Password = $composePassword.Groups['value'].Value.Trim()
    }
  }

  return [pscustomobject]$defaults
}

function Resolve-MySqlSettings {
  param(
    [Parameter(Mandatory = $true)][string]$ProjectRoot,
    [string]$Host,
    [int]$Port,
    [string]$Database,
    [string]$User,
    [string]$Password
  )

  $defaults = Get-LocalMySqlDefaults -ProjectRoot $ProjectRoot

  $resolvedHost = if (-not [string]::IsNullOrWhiteSpace($Host)) { $Host.Trim() } elseif (Get-EnvValue -Name 'PHASE32_MYSQL_HOST') { Get-EnvValue -Name 'PHASE32_MYSQL_HOST' } elseif (Get-EnvValue -Name 'DB_HOST') { Get-EnvValue -Name 'DB_HOST' } else { $defaults.Host }
  $resolvedPort = if ($Port -gt 0) { $Port } elseif (Get-EnvValue -Name 'PHASE32_MYSQL_PORT') { [int](Get-EnvValue -Name 'PHASE32_MYSQL_PORT') } elseif (Get-EnvValue -Name 'DB_PORT') { [int](Get-EnvValue -Name 'DB_PORT') } else { $defaults.Port }
  $resolvedDatabase = if (-not [string]::IsNullOrWhiteSpace($Database)) { $Database.Trim() } elseif (Get-EnvValue -Name 'PHASE32_MYSQL_DATABASE') { Get-EnvValue -Name 'PHASE32_MYSQL_DATABASE' } elseif (Get-EnvValue -Name 'DB_NAME') { Get-EnvValue -Name 'DB_NAME' } else { $defaults.Database }
  $resolvedUser = if (-not [string]::IsNullOrWhiteSpace($User)) { $User.Trim() } elseif (Get-EnvValue -Name 'PHASE32_MYSQL_USER') { Get-EnvValue -Name 'PHASE32_MYSQL_USER' } elseif (Get-EnvValue -Name 'DB_USERNAME') { Get-EnvValue -Name 'DB_USERNAME' } else { $defaults.User }
  $resolvedPassword = if (-not [string]::IsNullOrWhiteSpace($Password)) { $Password } elseif (Get-EnvValue -Name 'PHASE32_MYSQL_PASSWORD') { Get-EnvValue -Name 'PHASE32_MYSQL_PASSWORD' } elseif (Get-EnvValue -Name 'DB_PASSWORD') { Get-EnvValue -Name 'DB_PASSWORD' } else { $defaults.Password }

  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($resolvedPassword)) -Message 'MySQL password is required. Set PHASE32_MYSQL_PASSWORD or DB_PASSWORD.'

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
    & mysql --default-character-set=utf8mb4 -h $Settings.Host -P $Settings.Port -u $Settings.User $Settings.Database --execute="source $normalizedPath"
    if ($LASTEXITCODE -ne 0) {
      throw "mysql import failed for $Path"
    }
  } finally {
    $env:MYSQL_PWD = $previousPassword
  }
}

function Get-AdminToken {
  param([Parameter(Mandatory = $true)][string]$BaseUrl)

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE32_ADMIN_BEARER_TOKEN')
  if ($bearer) {
    return $bearer
  }

  $username = Get-EnvValue -Name 'PHASE32_ADMIN_USERNAME'
  $password = Get-EnvValue -Name 'PHASE32_ADMIN_PASSWORD'
  if (-not $username -or -not $password) {
    throw 'Admin auth is required. Set PHASE32_ADMIN_BEARER_TOKEN or both PHASE32_ADMIN_USERNAME and PHASE32_ADMIN_PASSWORD.'
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

function Get-TravelerToken {
  param(
    [Parameter(Mandatory = $true)][string]$BaseUrl,
    [Parameter(Mandatory = $true)][string]$LocaleCode
  )

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE32_TRAVELER_BEARER_TOKEN')
  if ($bearer) {
    return $bearer
  }

  $devIdentity = Get-EnvValue -Name 'PHASE32_TRAVELER_DEV_IDENTITY'
  $devBypassEnabled = Get-EnvValue -Name 'WECHAT_DEV_BYPASS_ENABLED'

  if (-not $devIdentity) {
    throw 'Traveler auth is required. Set PHASE32_TRAVELER_BEARER_TOKEN, or set PHASE32_TRAVELER_DEV_IDENTITY together with WECHAT_DEV_BYPASS_ENABLED=true on a local/dev public runtime.'
  }

  if (-not [string]::Equals($devBypassEnabled, 'true', [System.StringComparison]::OrdinalIgnoreCase)) {
    throw 'PHASE32_TRAVELER_BEARER_TOKEN is required unless WECHAT_DEV_BYPASS_ENABLED=true and PHASE32_TRAVELER_DEV_IDENTITY is provided.'
  }

  try {
    $login = Ensure-Success -Context 'public dev-bypass login' -Response (
      Invoke-Api -Method POST -Url "$BaseUrl/api/v1/user/login/dev-bypass" -Body @{
        devIdentity = $devIdentity
        nickname = 'Phase 32 Smoke Traveler'
        localeCode = $LocaleCode
        interfaceMode = 'story'
      }
    )
    Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$login.accessToken)) -Message 'Dev-bypass login did not return accessToken'
    return [string]$login.accessToken
  } catch {
    throw "Dev-bypass bootstrap is unavailable. Ensure the public runtime is running in local/dev with WECHAT_DEV_BYPASS_ENABLED=true, or provide PHASE32_TRAVELER_BEARER_TOKEN. Detail: $($_.Exception.Message)"
  }
}

$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$AdminBaseUrl = Resolve-Default -Value $AdminBaseUrl -EnvName 'PHASE32_ADMIN_BASE_URL' -Fallback 'http://127.0.0.1:8081'
$PublicBaseUrl = Resolve-Default -Value $PublicBaseUrl -EnvName 'PHASE32_PUBLIC_BASE_URL' -Fallback 'http://127.0.0.1:8080'
$mySqlSettings = Resolve-MySqlSettings -ProjectRoot $ProjectRoot -Host $MySqlHost -Port $MySqlPort -Database $MySqlDatabase -User $MySqlUser -Password $MySqlPassword

$sqlFiles = @(
  'scripts/local/mysql/init/43-phase-32-progress-engine.sql',
  'scripts/local/mysql/init/44-phase-32-story-sessions-and-timeline.sql',
  'scripts/local/mysql/init/45-phase-32-progress-repair-and-audit.sql',
  'scripts/local/mysql/init/46-phase-32-traveler-progress-fixtures.sql'
)

foreach ($relativePath in $sqlFiles) {
  Invoke-MySqlFile -Settings $mySqlSettings -Path (Join-Path $ProjectRoot $relativePath)
}

$adminToken = Get-AdminToken -BaseUrl $AdminBaseUrl
$travelerToken = Get-TravelerToken -BaseUrl $PublicBaseUrl -LocaleCode $Locale

$expectedPoiCompletedWeight = 72
$expectedPoiAvailableWeight = 72
$expectedGlobalCompletedWeight = 72
$expectedGlobalAvailableWeight = 93
$expectedGlobalProgress = 77.42
$expectedRetiredCompletedWeight = 3

$publicExploration = Ensure-Success -Context 'public user exploration' -Response (
  Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/users/me/exploration?locale=$([Uri]::EscapeDataString($Locale))&scopeType=poi&scopeId=$SeededPoiId" -Token $travelerToken
)

Assert-Equal -Actual $publicExploration.scopeType -Expected 'poi' -Context 'public exploration scopeType'
Assert-Equal -Actual ([int]$publicExploration.scopeId) -Expected ([int]$SeededPoiId) -Context 'public exploration scopeId'
Assert-Equal -Actual ([int]$publicExploration.completedWeight) -Expected $expectedPoiCompletedWeight -Context 'public exploration completedWeight'
Assert-Equal -Actual ([int]$publicExploration.availableWeight) -Expected $expectedPoiAvailableWeight -Context 'public exploration availableWeight'
Assert-Equal -Actual ([int]$publicExploration.completedElementCount) -Expected 12 -Context 'public exploration completedElementCount'
Assert-Equal -Actual ([int]$publicExploration.availableElementCount) -Expected 12 -Context 'public exploration availableElementCount'
Assert-ApproxNumber -Actual ([double]$publicExploration.progressPercent) -Expected 100.0 -Context 'public exploration progressPercent'
Assert-Any -Items @($publicExploration.elements) -Predicate {
  $_.elementCode -eq 'phase32_retired_guardian_note' -and $true -eq $_.completed -and $false -eq $_.includedInCurrentPercentage
} -Context 'public exploration should expose the retired completed comparison element without counting it in the active percentage'

$workbench = Ensure-Success -Context 'admin progress workbench' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/users/$SeededUserId/progress-workbench" -Token $adminToken
)

Assert-Equal -Actual ([int]$workbench.userId) -Expected ([int]$SeededUserId) -Context 'workbench userId'
Assert-Equal -Actual $workbench.identity.nickname -Expected '第 32 階段旅客' -Context 'workbench identity nickname'
Assert-Equal -Actual $workbench.preferences.localeCode -Expected 'zh-Hant' -Context 'workbench locale'
Assert-ApproxNumber -Actual ([double]$workbench.dynamicProgress.globalSummary.progressPercent) -Expected $expectedGlobalProgress -Context 'workbench dynamic global progress'
Assert-True -Condition (@($workbench.legacyProgressSnapshot).Count -ge 1) -Message 'workbench should return legacyProgressSnapshot entries'
Assert-Equal -Actual $workbench.legacyProgressSnapshot[0].sourceTable -Expected 'traveler_progress' -Context 'legacy progress source table'
Assert-True -Condition ([bool]$workbench.legacyProgressSnapshot[0].compatibilityOnly) -Message 'legacy progress snapshot must be compatibilityOnly=true'
Assert-True -Condition (@($workbench.storylineSessions).Count -ge 2) -Message 'workbench should expose both active and exited storyline sessions'
Assert-True -Condition (@($workbench.rewardRedemptions).Count -ge 2) -Message 'workbench should expose seeded reward redemption history'

$breakdown = Ensure-Success -Context 'admin progress breakdown' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/users/$SeededUserId/progress-breakdown?scopeType=poi&scopeId=$SeededPoiId&includeInactiveElements=false" -Token $adminToken
)

Assert-Equal -Actual $breakdown.scopeType -Expected 'poi' -Context 'breakdown scopeType'
Assert-Equal -Actual ([int]$breakdown.scopeId) -Expected ([int]$SeededPoiId) -Context 'breakdown scopeId'
Assert-Equal -Actual ([int]$breakdown.completedWeight) -Expected $expectedPoiCompletedWeight -Context 'breakdown completedWeight'
Assert-Equal -Actual ([int]$breakdown.availableWeight) -Expected $expectedPoiAvailableWeight -Context 'breakdown availableWeight'
Assert-ApproxNumber -Actual ([double]$breakdown.progressPercent) -Expected 100.0 -Context 'breakdown progressPercent'
Assert-Equal -Actual ([int]$breakdown.retiredCompletedWeight) -Expected $expectedRetiredCompletedWeight -Context 'breakdown retiredCompletedWeight'
Assert-Equal -Actual ([int]$breakdown.retiredCompletedCount) -Expected 1 -Context 'breakdown retiredCompletedCount'
Assert-Any -Items @($breakdown.retiredElements) -Predicate {
  $_.elementCode -eq 'phase32_retired_guardian_note'
} -Context 'breakdown retiredElements should include the archived compatibility completion'

$timelineData = Ensure-Success -Context 'admin traveler timeline' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/users/$SeededUserId/timeline?pageNum=1&pageSize=50" -Token $adminToken
)
$timelineEntries = Get-PageItems -Data $timelineData -Context 'admin traveler timeline'
Assert-True -Condition ($timelineEntries.Count -ge 6) -Message 'timeline should expose seeded check-ins, triggers, events, sessions, rewards, and audits'
$timelineTypes = @($timelineEntries | ForEach-Object { [string]$_.entryType })
Assert-Contains -Items $timelineTypes -Expected 'checkin' -Context 'timeline entry types'
Assert-Contains -Items $timelineTypes -Expected 'exploration_event' -Context 'timeline entry types'
Assert-Contains -Items $timelineTypes -Expected 'storyline_session' -Context 'timeline entry types'
Assert-Contains -Items $timelineTypes -Expected 'reward_redemption' -Context 'timeline entry types'

$recomputeReason = 'Phase 32 smoke recompute confirm'
$preview = Ensure-Success -Context 'progress recompute preview' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/users/$SeededUserId/progress-ops/recompute-preview" -Token $adminToken -Body @{
    userId = $SeededUserId
    scopeType = 'poi'
    scopeId = $SeededPoiId
    reason = $recomputeReason
  }
)

Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$preview.previewHash)) -Message 'recompute preview must return previewHash'
Assert-Equal -Actual $preview.confirmationText -Expected 'RECOMPUTE' -Context 'recompute preview confirmationText'

$confirm = Ensure-Success -Context 'progress recompute confirm' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/users/$SeededUserId/progress-ops/recompute-confirm" -Token $adminToken -Body @{
    userId = $SeededUserId
    scopeType = 'poi'
    scopeId = $SeededPoiId
    reason = $recomputeReason
    previewHash = [string]$preview.previewHash
    confirmationText = 'RECOMPUTE'
  }
)

Assert-Equal -Actual $confirm.confirmationText -Expected 'RECOMPUTE' -Context 'recompute confirm confirmationText'
Assert-True -Condition ([string]$confirm.status -like 'confirmed*' -or [string]$confirm.actionType -eq 'RECOMPUTE_SCOPE') -Message 'recompute confirm should complete successfully'

$auditsData = Ensure-Success -Context 'progress audit list' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/users/$SeededUserId/progress-ops/audits?pageNum=1&pageSize=20&actionTypes=RECOMPUTE&scopeType=poi" -Token $adminToken
)
$audits = Get-PageItems -Data $auditsData -Context 'progress audit list'
Assert-Any -Items $audits -Predicate {
  $_.reason -eq $recomputeReason
} -Context 'audit list should expose the new recompute audit row'

$publicLeakProbe = Invoke-ApiDetailed -Method GET -Url "$PublicBaseUrl/api/v1/users/me/progress-ops/audits" -Token $travelerToken
Assert-True -Condition (-not $publicLeakProbe.Success) -Message 'public progress-ops probe unexpectedly succeeded'
Assert-True -Condition (@(401, 403, 404).Contains([int]$publicLeakProbe.StatusCode)) -Message "public progress-ops probe should fail with 401/403/404, got $($publicLeakProbe.StatusCode)"

Write-Host 'Phase 32 user progress smoke passed'
