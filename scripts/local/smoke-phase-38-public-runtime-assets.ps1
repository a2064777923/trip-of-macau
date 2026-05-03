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

function ConvertTo-CompactJson {
  param($Value)
  if ($null -eq $Value) { return '' }
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
      $request.Headers.Authorization = New-Object System.Net.Http.Headers.AuthenticationHeaderValue('Bearer', (Normalize-BearerToken -Token $Token))
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
      try { $jsonResponse = $raw | ConvertFrom-Json } catch { $jsonResponse = $null }
    }
    return [pscustomobject]@{
      StatusCode = [int]$response.StatusCode
      Success = $response.IsSuccessStatusCode
      Raw = $raw
      Json = $jsonResponse
    }
  } finally {
    if ($request) { $request.Dispose() }
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
  param([Parameter(Mandatory = $true)]$Response, [Parameter(Mandatory = $true)][string]$Context)
  if ($null -eq $Response) { throw "$Context returned an empty response" }
  if ($Response.code -ne 0 -and $Response.code -ne 200) {
    throw "$Context failed: code=$($Response.code), message=$($Response.message)"
  }
  return $Response.data
}

function Assert-Any {
  param([Parameter(Mandatory = $true)]$Items, [Parameter(Mandatory = $true)][scriptblock]$Predicate, [Parameter(Mandatory = $true)][string]$Context)
  $matches = @($Items | Where-Object $Predicate)
  Assert-True -Condition ($matches.Count -ge 1) -Message $Context
}

function Parse-DbUrl {
  param([string]$DbUrl)
  if ([string]::IsNullOrWhiteSpace($DbUrl)) { return $null }
  $match = [regex]::Match($DbUrl, 'jdbc:mysql://(?<host>[^:/?]+):(?<port>\d+)/(?<db>[^?]+)')
  if (-not $match.Success) { return $null }
  return [pscustomobject]@{ Host = $match.Groups['host'].Value; Port = [int]$match.Groups['port'].Value; Database = $match.Groups['db'].Value }
}

function Get-LocalMySqlDefaults {
  param([Parameter(Mandatory = $true)][string]$ProjectRoot)
  $defaults = [ordered]@{ Host = '127.0.0.1'; Port = 3306; Database = 'aoxiaoyou'; User = 'root'; Password = 'Abc123456' }
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
    if ($userMatch.Success) { $defaults.User = $userMatch.Groups['value'].Value.Trim() }
    $passwordMatch = [regex]::Match($content, 'password:\s*\$\{DB_PASSWORD:(?<value>[^}]+)\}')
    if ($passwordMatch.Success) { $defaults.Password = $passwordMatch.Groups['value'].Value.Trim() }
  }
  return [pscustomobject]$defaults
}

function Resolve-MySqlExe {
  param([string]$Candidate)
  if (-not [string]::IsNullOrWhiteSpace($Candidate) -and (Test-Path -LiteralPath $Candidate)) { return $Candidate }
  $envCandidate = Get-EnvValue -Name 'PHASE38_MYSQL_EXE'
  if ($envCandidate -and (Test-Path -LiteralPath $envCandidate)) { return $envCandidate }
  $localCandidate = 'D:\Software\mysql-8.0.41-winx64\bin\mysql.exe'
  if (Test-Path -LiteralPath $localCandidate) { return $localCandidate }
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
  $resolvedHost = if (-not [string]::IsNullOrWhiteSpace($MySqlHostValue)) { $MySqlHostValue.Trim() } elseif (Get-EnvValue -Name 'PHASE38_MYSQL_HOST') { Get-EnvValue -Name 'PHASE38_MYSQL_HOST' } elseif (Get-EnvValue -Name 'DB_HOST') { Get-EnvValue -Name 'DB_HOST' } else { $defaults.Host }
  $resolvedPort = if ($MySqlPortValue -gt 0) { $MySqlPortValue } elseif (Get-EnvValue -Name 'PHASE38_MYSQL_PORT') { [int](Get-EnvValue -Name 'PHASE38_MYSQL_PORT') } elseif (Get-EnvValue -Name 'DB_PORT') { [int](Get-EnvValue -Name 'DB_PORT') } else { $defaults.Port }
  $resolvedDatabase = if (-not [string]::IsNullOrWhiteSpace($MySqlDatabaseValue)) { $MySqlDatabaseValue.Trim() } elseif (Get-EnvValue -Name 'PHASE38_MYSQL_DATABASE') { Get-EnvValue -Name 'PHASE38_MYSQL_DATABASE' } elseif (Get-EnvValue -Name 'DB_NAME') { Get-EnvValue -Name 'DB_NAME' } else { $defaults.Database }
  $resolvedUser = if (-not [string]::IsNullOrWhiteSpace($MySqlUserValue)) { $MySqlUserValue.Trim() } elseif (Get-EnvValue -Name 'PHASE38_MYSQL_USER') { Get-EnvValue -Name 'PHASE38_MYSQL_USER' } elseif (Get-EnvValue -Name 'DB_USERNAME') { Get-EnvValue -Name 'DB_USERNAME' } else { $defaults.User }
  $resolvedPassword = if (-not [string]::IsNullOrWhiteSpace($MySqlPasswordValue)) { $MySqlPasswordValue } elseif (Get-EnvValue -Name 'PHASE38_MYSQL_PASSWORD') { Get-EnvValue -Name 'PHASE38_MYSQL_PASSWORD' } elseif (Get-EnvValue -Name 'MYSQL_PWD') { Get-EnvValue -Name 'MYSQL_PWD' } elseif (Get-EnvValue -Name 'DB_PASSWORD') { Get-EnvValue -Name 'DB_PASSWORD' } else { $defaults.Password }
  return [pscustomobject]@{ Host = $resolvedHost; Port = $resolvedPort; Database = $resolvedDatabase; User = $resolvedUser; Password = $resolvedPassword }
}

function Invoke-MySqlFile {
  param([Parameter(Mandatory = $true)][pscustomobject]$Settings, [Parameter(Mandatory = $true)][string]$Path)
  if (-not (Test-Path -LiteralPath $Path)) { return }
  $previousPassword = $env:MYSQL_PWD
  try {
    $env:MYSQL_PWD = $Settings.Password
    $normalizedPath = (Resolve-Path -LiteralPath $Path).Path.Replace('\', '/')
    $output = & $script:ResolvedMySqlExe --default-character-set=utf8mb4 --host=$($Settings.Host) --port=$($Settings.Port) --user=$($Settings.User) --database=$($Settings.Database) --execute="SOURCE $normalizedPath" 2>&1
    if ($LASTEXITCODE -ne 0) { throw "mysql import failed for $Path`n$($output -join "`n")" }
  } finally {
    $env:MYSQL_PWD = $previousPassword
  }
}

function Collect-MediaAssets {
  param($Runtime)
  $assets = @()
  foreach ($chapter in @($Runtime.chapters)) {
    if ($chapter.chapter.primaryMediaAsset) { $assets += $chapter.chapter.primaryMediaAsset }
    foreach ($block in @($chapter.chapter.contentBlocks)) {
      if ($block.primaryAsset) { $assets += $block.primaryAsset }
      foreach ($asset in @($block.attachmentAssets)) {
        if ($asset) { $assets += $asset }
      }
    }
    foreach ($step in @($chapter.compiledSteps)) {
      if ($step.mediaAsset) { $assets += $step.mediaAsset }
    }
  }
  return $assets
}

function Get-TravelerToken {
  param(
    [Parameter(Mandatory = $true)][string]$BaseUrl,
    [Parameter(Mandatory = $true)][string]$LocaleCode
  )

  $bearer = Normalize-BearerToken -Token (Get-EnvValue -Name 'PHASE38_TRAVELER_BEARER_TOKEN')
  if ($bearer) {
    return $bearer
  }

  $devIdentity = Get-EnvValue -Name 'PHASE38_TRAVELER_DEV_IDENTITY'
  if (-not $devIdentity) {
    return $null
  }

  $login = Ensure-Success -Context 'public dev-bypass login' -Response (
    Invoke-Api -Method POST -Url "$BaseUrl/api/v1/user/login/dev-bypass" -Body @{
      devIdentity = $devIdentity
      nickname = 'Phase 38 Smoke Traveler'
      localeCode = $LocaleCode
      interfaceMode = 'standard'
    }
  )
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$login.accessToken)) -Message 'Dev-bypass login did not return accessToken'
  return [string]$login.accessToken
}

$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$PublicBaseUrl = Resolve-Default -Value $PublicBaseUrl -EnvName 'PHASE38_PUBLIC_BASE_URL' -Fallback 'http://127.0.0.1:8080'
$script:ResolvedMySqlExe = Resolve-MySqlExe -Candidate $MySqlExe
$mySqlSettings = Resolve-MySqlSettings -ProjectRoot $ProjectRoot -MySqlHostValue $MySqlHost -MySqlPortValue $MySqlPort -MySqlDatabaseValue $MySqlDatabase -MySqlUserValue $MySqlUser -MySqlPasswordValue $MySqlPassword

$seedFiles = @(
  'scripts/local/mysql/init/47-phase-33-story-material-package-model.sql',
  'scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql',
  'scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql',
  'scripts/local/mysql/init/51-phase-36-material-production-versioning.sql'
)
foreach ($relativePath in $seedFiles) {
  Invoke-MySqlFile -Settings $mySqlSettings -Path (Join-Path $ProjectRoot $relativePath)
}

$storylineCode = 'east_west_war_and_coexistence'
$escapedLocale = [Uri]::EscapeDataString($Locale)
$storylines = Ensure-Success -Context 'public storylines list' -Response (Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/story-lines?locale=$escapedLocale")
$storyline = @($storylines | Where-Object { [string]$_.code -eq $storylineCode } | Select-Object -First 1)[0]
Assert-True -Condition ($null -ne $storyline) -Message "Storyline $storylineCode was not found"

$runtime = Ensure-Success -Context 'public storyline runtime' -Response (Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/storylines/$($storyline.id)/runtime?locale=$escapedLocale")
Assert-True -Condition ([string]$runtime.runtimeVersion -eq 'v1') -Message 'runtimeVersion should be v1'
Assert-True -Condition (@($runtime.chapters).Count -ge 5) -Message 'runtime should include at least five chapters'
Assert-Any -Items @($runtime.chapters) -Context 'Expected at least one chapter with compiledSteps' -Predicate { @($_.compiledSteps).Count -ge 1 }
Assert-Any -Items @($runtime.chapters) -Context 'Expected at least one content block asset' -Predicate {
  @($_.chapter.contentBlocks | Where-Object { $_.primaryAsset -or @($_.attachmentAssets).Count -gt 0 }).Count -ge 1
}

$mediaAssets = @(Collect-MediaAssets -Runtime $runtime)
Assert-Any -Items $mediaAssets -Context 'Expected at least one media asset with availability' -Predicate { $_.PSObject.Properties.Name -contains 'availability' }
Assert-Any -Items $mediaAssets -Context 'Expected at least one image media asset' -Predicate { [string]$_.runtimeKind -eq 'image' -or [string]$_.assetKind -eq 'image' }
Assert-Any -Items $mediaAssets -Context 'Expected at least one audio media asset' -Predicate { [string]$_.runtimeKind -eq 'audio' -or [string]$_.assetKind -eq 'audio' }
Assert-Any -Items $mediaAssets -Context 'Expected at least one video media asset' -Predicate { [string]$_.runtimeKind -eq 'video' -or [string]$_.assetKind -eq 'video' }

$lottieBlocks = @($runtime.chapters | ForEach-Object { @($_.chapter.contentBlocks) } | Where-Object { [string]$_.blockType -eq 'lottie' })
if ($lottieBlocks.Count -gt 0) {
  Assert-Any -Items $mediaAssets -Context 'Expected lottie-capable metadata when lottie blocks exist' -Predicate {
    [string]$_.runtimeKind -eq 'lottie' -or [string]$_.assetKind -eq 'lottie' -or $_.animationSubtype
  }
}

$rawRuntime = $runtime | ConvertTo-Json -Depth 100 -Compress
$banned = @('promptText', 'scriptText', 'localPath', 'providerApiKey', 'apiKey', 'secret', 'estimatedCost', 'actualCost', 'qaNote', 'cosObjectKey')
foreach ($token in $banned) {
  Assert-True -Condition (-not $rawRuntime.Contains($token)) -Message "Runtime leaked banned field name: $token"
}

$travelerToken = Get-TravelerToken -BaseUrl $PublicBaseUrl -LocaleCode $Locale
if (-not $travelerToken) {
  Write-Host 'Skipping authenticated Phase 38 event checks because PHASE38_TRAVELER_BEARER_TOKEN is not set'
  Write-Host 'Set PHASE38_TRAVELER_DEV_IDENTITY with local/dev dev-bypass enabled to run the same checks without printing a bearer token'
  Write-Host 'Phase 38 public runtime asset smoke passed'
  exit 0
}

$session = Ensure-Success -Context 'storyline session start' -Response (Invoke-Api -Method POST -Url "$PublicBaseUrl/api/v1/storylines/$($storyline.id)/sessions/start" -Token $travelerToken)
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$session.sessionId)) -Message 'Session start did not return sessionId'

$firstChapter = @($runtime.chapters | Sort-Object { [int]($_.chapterOrder) } | Select-Object -First 1)[0]
$clientEventId = "phase38:$($storyline.id):media-completed"
$eventBody = @{
  eventType = 'media_completed'
  eventSource = 'phase38_smoke'
  clientEventId = $clientEventId
  payloadJson = (@{ storylineId = [int]$storyline.id; chapterId = [int]$firstChapter.chapterId; mediaProgress = 1 } | ConvertTo-Json -Depth 10 -Compress)
  occurredAt = (Get-Date).ToUniversalTime().ToString('s')
}
$event = Ensure-Success -Context 'media_completed event' -Response (Invoke-Api -Method POST -Url "$PublicBaseUrl/api/v1/storylines/$($storyline.id)/sessions/$($session.sessionId)/events" -Token $travelerToken -Body $eventBody)
$duplicate = Ensure-Success -Context 'duplicate media_completed event' -Response (Invoke-Api -Method POST -Url "$PublicBaseUrl/api/v1/storylines/$($storyline.id)/sessions/$($session.sessionId)/events" -Token $travelerToken -Body $eventBody)
Assert-True -Condition (($event.eventId -eq $duplicate.eventId) -or ($true -eq $duplicate.duplicate)) -Message 'Duplicate event did not return idempotent metadata'

foreach ($eventType in @('pickup_interacted', 'task_completed', 'reward_acquired')) {
  $body = @{
    eventType = $eventType
    eventSource = 'phase38_smoke'
    clientEventId = "phase38:$($storyline.id):$eventType"
    payloadJson = (@{ storylineId = [int]$storyline.id; chapterId = [int]$firstChapter.chapterId; code = $eventType } | ConvertTo-Json -Depth 10 -Compress)
    occurredAt = (Get-Date).ToUniversalTime().ToString('s')
  }
  $accepted = Ensure-Success -Context "$eventType event" -Response (Invoke-Api -Method POST -Url "$PublicBaseUrl/api/v1/storylines/$($storyline.id)/sessions/$($session.sessionId)/events" -Token $travelerToken -Body $body)
  Assert-True -Condition ($true -eq $accepted.accepted) -Message "$eventType was not accepted"
}

$exploration = Ensure-Success -Context 'user storyline exploration' -Response (Invoke-Api -Method GET -Url "$PublicBaseUrl/api/v1/users/me/exploration?locale=$escapedLocale&scopeType=storyline&scopeId=$($storyline.id)" -Token $travelerToken)
Assert-True -Condition ([string]$exploration.scopeType -eq 'storyline') -Message 'Exploration scopeType should be storyline'

$exit = Ensure-Success -Context 'storyline session exit' -Response (Invoke-Api -Method POST -Url "$PublicBaseUrl/api/v1/storylines/$($storyline.id)/sessions/$($session.sessionId)/exit" -Token $travelerToken)
$exitAgain = Ensure-Success -Context 'duplicate storyline session exit' -Response (Invoke-Api -Method POST -Url "$PublicBaseUrl/api/v1/storylines/$($storyline.id)/sessions/$($session.sessionId)/exit" -Token $travelerToken)
Assert-True -Condition ([string]$exit.sessionId -eq [string]$session.sessionId) -Message 'Session exit returned wrong sessionId'
Assert-True -Condition ($true -eq $exitAgain.duplicateExit -or $true -eq $exitAgain.exitClearedTemporaryState) -Message 'Duplicate session exit did not return retry-safe metadata'

Write-Host 'Phase 38 public runtime asset smoke passed'
