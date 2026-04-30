param(
  [string]$PublicBaseUrl = '',
  [string]$Locale = 'zh-Hant',
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

function Assert-Any {
  param(
    [Parameter(Mandatory = $true)]$Items,
    [Parameter(Mandatory = $true)][scriptblock]$Predicate,
    [Parameter(Mandatory = $true)][string]$Context
  )

  $matches = @($Items | Where-Object $Predicate)
  Assert-True -Condition ($matches.Count -ge 1) -Message $Context
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
    User = ''
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
  $envCandidate = Get-EnvValue -Name 'PHASE34_MYSQL_EXE'
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

  $resolvedHost = if (-not [string]::IsNullOrWhiteSpace($MySqlHostValue)) { $MySqlHostValue.Trim() } elseif (Get-EnvValue -Name 'PHASE34_MYSQL_HOST') { Get-EnvValue -Name 'PHASE34_MYSQL_HOST' } elseif (Get-EnvValue -Name 'DB_HOST') { Get-EnvValue -Name 'DB_HOST' } else { $defaults.Host }
  $resolvedPort = if ($MySqlPortValue -gt 0) { $MySqlPortValue } elseif (Get-EnvValue -Name 'PHASE34_MYSQL_PORT') { [int](Get-EnvValue -Name 'PHASE34_MYSQL_PORT') } elseif (Get-EnvValue -Name 'DB_PORT') { [int](Get-EnvValue -Name 'DB_PORT') } else { $defaults.Port }
  $resolvedDatabase = if (-not [string]::IsNullOrWhiteSpace($MySqlDatabaseValue)) { $MySqlDatabaseValue.Trim() } elseif (Get-EnvValue -Name 'PHASE34_MYSQL_DATABASE') { Get-EnvValue -Name 'PHASE34_MYSQL_DATABASE' } elseif (Get-EnvValue -Name 'DB_NAME') { Get-EnvValue -Name 'DB_NAME' } else { $defaults.Database }
  $resolvedUser = if (-not [string]::IsNullOrWhiteSpace($MySqlUserValue)) { $MySqlUserValue.Trim() } elseif (Get-EnvValue -Name 'PHASE34_MYSQL_USER') { Get-EnvValue -Name 'PHASE34_MYSQL_USER' } elseif (Get-EnvValue -Name 'DB_USERNAME') { Get-EnvValue -Name 'DB_USERNAME' } else { $defaults.User }
  $resolvedPassword = if (-not [string]::IsNullOrWhiteSpace($MySqlPasswordValue)) { $MySqlPasswordValue } elseif (Get-EnvValue -Name 'PHASE34_MYSQL_PASSWORD') { Get-EnvValue -Name 'PHASE34_MYSQL_PASSWORD' } elseif (Get-EnvValue -Name 'MYSQL_PWD') { Get-EnvValue -Name 'MYSQL_PWD' } elseif (Get-EnvValue -Name 'DB_PASSWORD') { Get-EnvValue -Name 'DB_PASSWORD' } else { $defaults.Password }

  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($resolvedUser)) -Message 'MySQL user is required. Set PHASE34_MYSQL_USER or DB_USERNAME.'
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($resolvedPassword)) -Message 'MySQL password is required. Set PHASE34_MYSQL_PASSWORD, MYSQL_PWD, or DB_PASSWORD.'

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
      --host=$($Settings.Host) `
      --port=$($Settings.Port) `
      --user=$($Settings.User) `
      --database=$($Settings.Database) `
      --execute=$Sql 2>&1
    if ($LASTEXITCODE -ne 0) {
      throw "mysql statement failed`n$($output -join "`n")"
    }
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

  Invoke-MySqlStatement -Settings $Settings -Sql $sql
}

function Get-TravelerToken {
  param(
    [Parameter(Mandatory = $true)][string]$BaseUrl,
    [Parameter(Mandatory = $true)][string]$LocaleCode
  )

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE34_TRAVELER_BEARER_TOKEN')
  if ($bearer) {
    return $bearer
  }

  $devIdentity = Get-EnvValue -Name 'PHASE34_TRAVELER_DEV_IDENTITY'
  $devBypassEnabled = Get-EnvValue -Name 'WECHAT_DEV_BYPASS_ENABLED'

  if (-not $devIdentity -or -not [string]::Equals($devBypassEnabled, 'true', [System.StringComparison]::OrdinalIgnoreCase)) {
    return $null
  }

  $login = Ensure-Success -Context 'public dev-bypass login' -Response (
    Invoke-Api -Method POST -Url "$BaseUrl/api/v1/user/login/dev-bypass" -Body @{
      devIdentity = $devIdentity
      nickname = 'Phase 34 Smoke Traveler'
      localeCode = $LocaleCode
      interfaceMode = 'standard'
    }
  )
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$login.accessToken)) -Message 'Dev-bypass login did not return accessToken'
  return [string]$login.accessToken
}

function Get-PropertyNames {
  param($Value)
  if ($null -eq $Value -or $null -eq $Value.PSObject) {
    return @()
  }
  return @($Value.PSObject.Properties.Name)
}

$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$PublicBaseUrl = Resolve-Default -Value $PublicBaseUrl -EnvName 'PHASE34_PUBLIC_BASE_URL' -Fallback 'http://127.0.0.1:8080'
$script:ResolvedMySqlExe = Resolve-MySqlExe -Candidate $MySqlExe
$mySqlSettings = Resolve-MySqlSettings -ProjectRoot $ProjectRoot -MySqlHostValue $MySqlHost -MySqlPortValue $MySqlPort -MySqlDatabaseValue $MySqlDatabase -MySqlUserValue $MySqlUser -MySqlPasswordValue $MySqlPassword

$seedFiles = @(
  'scripts/local/mysql/init/38-phase-28-story-content-and-lottie.sql',
  'scripts/local/mysql/init/39-phase-28-experience-orchestration.sql',
  'scripts/local/mysql/init/40-phase-29-poi-default-experience.sql',
  'scripts/local/mysql/init/41-phase-30-storyline-mode-overrides.sql',
  'scripts/local/mysql/init/43-phase-32-progress-engine.sql',
  'scripts/local/mysql/init/44-phase-32-story-sessions-and-timeline.sql',
  'scripts/local/mysql/init/45-phase-32-progress-repair-and-audit.sql',
  'scripts/local/mysql/init/47-phase-33-story-material-package-model.sql',
  'scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql',
  'scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql'
)

foreach ($relativePath in $seedFiles) {
  if ($relativePath -eq 'scripts/local/mysql/init/40-phase-29-poi-default-experience.sql') {
    Reset-Phase29PoiDefaultFlowSeed -Settings $mySqlSettings
  }
  Invoke-MySqlFile -Settings $mySqlSettings -Path (Join-Path $ProjectRoot $relativePath)
}

$storylineCode = 'east_west_war_and_coexistence'
$escapedLocale = [Uri]::EscapeDataString($Locale)

$storylines = Ensure-Success -Context 'public storylines list' -Response (
  Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/story-lines?locale=$escapedLocale"
)
$storyline = @($storylines | Where-Object { [string]$_.code -eq $storylineCode }) | Select-Object -First 1
Assert-True -Condition ($null -ne $storyline) -Message "Storyline $storylineCode was not found through public story list"

$runtime = Ensure-Success -Context 'public storyline runtime' -Response (
  Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/storylines/$($storyline.id)/runtime?locale=$escapedLocale"
)

Assert-Equal -Actual $runtime.runtimeVersion -Expected 'v1' -Context 'runtimeVersion'
Assert-Equal -Actual $runtime.source -Expected 'public_runtime' -Context 'runtime source'
Assert-True -Condition ([int]$runtime.publishedChapterCount -ge 5) -Message "publishedChapterCount should be at least 5, got $($runtime.publishedChapterCount)"
Assert-True -Condition (@($runtime.chapters).Count -ge 5) -Message "runtime chapters should be at least 5, got $(@($runtime.chapters).Count)"
Assert-True -Condition ($null -ne $runtime.storyModeConfig) -Message 'storyModeConfig is missing from runtime'

$chapters = @($runtime.chapters)
Assert-Any -Items $chapters -Context 'Expected at least one chapter with compiledSteps' -Predicate {
  @($_.compiledSteps).Count -ge 1
}

$steps = @($chapters | ForEach-Object { @($_.compiledSteps) })
Assert-Any -Items $steps -Context 'Expected at least one step with displayCategoryLabel' -Predicate {
  -not [string]::IsNullOrWhiteSpace([string]$_.displayCategoryLabel)
}
Assert-Any -Items $steps -Context 'Expected at least one step with travelerActionLabel' -Predicate {
  -not [string]::IsNullOrWhiteSpace([string]$_.travelerActionLabel)
}
Assert-Any -Items $steps -Context 'Expected at least one step exposing unsupported as a boolean' -Predicate {
  (Get-PropertyNames -Value $_) -contains 'unsupported' -and $null -ne $_.unsupported
}
Assert-Any -Items $chapters -Context 'Expected at least one chapter with contentBlocks' -Predicate {
  @($_.chapter.contentBlocks).Count -ge 1
}

$contentBlocks = @($chapters | ForEach-Object { @($_.chapter.contentBlocks) })
Assert-Any -Items $contentBlocks -Context 'Expected at least one lottie content block' -Predicate {
  [string]$_.blockType -eq 'lottie'
}
Assert-Any -Items $contentBlocks -Context 'Expected at least one audio content block' -Predicate {
  [string]$_.blockType -eq 'audio'
}
Assert-Any -Items $contentBlocks -Context 'Expected at least one image or gallery content block' -Predicate {
  [string]$_.blockType -eq 'image' -or [string]$_.blockType -eq 'gallery'
}

$travelerToken = Get-TravelerToken -BaseUrl $PublicBaseUrl -LocaleCode $Locale
if (-not $travelerToken) {
  Write-Host 'Skipping authenticated Phase 34 checks because PHASE34_TRAVELER_BEARER_TOKEN is not set'
  Write-Host 'Phase 34 public runtime smoke passed'
  exit 0
}

$session = Ensure-Success -Context 'storyline session start' -Response (
  Invoke-Api -Method POST -Url "$PublicBaseUrl/api/v1/storylines/$($storyline.id)/sessions/start" -Token $travelerToken
)
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$session.sessionId)) -Message 'Session start did not return sessionId'

$firstChapter = @($chapters | Sort-Object { [int]($_.chapterOrder) }) | Select-Object -First 1
$firstStep = @($firstChapter.compiledSteps) | Select-Object -First 1
$clientEventId = "phase34:$($storyline.id):$($firstChapter.chapterId):$($firstStep.id):chapter_open"
$eventBody = @{
  elementId = $firstStep.elementId
  elementCode = if (-not [string]::IsNullOrWhiteSpace([string]$firstStep.elementCode)) { [string]$firstStep.elementCode } else { "storyline_$($storyline.id)_phase34" }
  eventType = 'chapter_open'
  eventSource = 'phase34_smoke'
  clientEventId = $clientEventId
  payloadJson = (@{
    storylineId = [int]$storyline.id
    chapterId = [int]$firstChapter.chapterId
    stepId = if ($firstStep.id) { [int]$firstStep.id } else { $null }
  } | ConvertTo-Json -Depth 10 -Compress)
  occurredAt = (Get-Date).ToUniversalTime().ToString('s')
}

$event = Ensure-Success -Context 'storyline session event' -Response (
  Invoke-Api -Method POST -Url "$PublicBaseUrl/api/v1/storylines/$($storyline.id)/sessions/$($session.sessionId)/events" -Token $travelerToken -Body $eventBody
)
$duplicate = Ensure-Success -Context 'duplicate storyline session event' -Response (
  Invoke-Api -Method POST -Url "$PublicBaseUrl/api/v1/storylines/$($storyline.id)/sessions/$($session.sessionId)/events" -Token $travelerToken -Body $eventBody
)
Assert-True -Condition (
  ($null -ne $event.eventId -and $event.eventId -eq $duplicate.eventId) -or
  ($event.clientEventId -eq $duplicate.clientEventId)
) -Message 'Duplicate event did not return an idempotent event response'

$exploration = Ensure-Success -Context 'user storyline exploration' -Response (
  Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/users/me/exploration?locale=$escapedLocale&scopeType=storyline&scopeId=$($storyline.id)" -Token $travelerToken
)
Assert-Equal -Actual $exploration.scopeType -Expected 'storyline' -Context 'exploration scopeType'
Assert-True -Condition ([int]$exploration.availableElementCount -ge 1) -Message 'Exploration response should expose available elements'

$exit = Ensure-Success -Context 'storyline session exit' -Response (
  Invoke-Api -Method POST -Url "$PublicBaseUrl/api/v1/storylines/$($storyline.id)/sessions/$($session.sessionId)/exit" -Token $travelerToken
)
Assert-Equal -Actual $exit.sessionId -Expected $session.sessionId -Context 'session exit id'

Write-Host 'Phase 34 public runtime smoke passed'
