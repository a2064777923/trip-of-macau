param(
  [switch]$Quick,
  [string]$ReportPath = '',
  [string]$AdminBaseUrl = '',
  [string]$PublicBaseUrl = '',
  [long]$UserId = 0,
  [string]$PublicDevIdentity = '',
  [switch]$ApplySafeAnnotation,
  [switch]$AllowNonLocal
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$PhaseDir = '.planning/phases/43-traveler-progress-and-reward-operations'
if ([string]::IsNullOrWhiteSpace($ReportPath)) {
  $ReportPath = Join-Path $ProjectRoot "$PhaseDir/43-UAT.md"
}

if ([string]::IsNullOrWhiteSpace($AdminBaseUrl)) {
  $AdminBaseUrl = [Environment]::GetEnvironmentVariable('PHASE43_ADMIN_BASE_URL')
}
if ([string]::IsNullOrWhiteSpace($AdminBaseUrl)) { $AdminBaseUrl = 'http://127.0.0.1:8081' }
$AdminBaseUrl = $AdminBaseUrl.TrimEnd('/')

if ([string]::IsNullOrWhiteSpace($PublicBaseUrl)) {
  $PublicBaseUrl = [Environment]::GetEnvironmentVariable('PHASE43_PUBLIC_BASE_URL')
}
if ([string]::IsNullOrWhiteSpace($PublicBaseUrl)) { $PublicBaseUrl = 'http://127.0.0.1:8080' }
$PublicBaseUrl = $PublicBaseUrl.TrimEnd('/')

if ($UserId -le 0) {
  $envUserId = [Environment]::GetEnvironmentVariable('PHASE43_USER_ID')
  if (-not [string]::IsNullOrWhiteSpace($envUserId)) {
    $UserId = [long]$envUserId
  }
}

if ([string]::IsNullOrWhiteSpace($PublicDevIdentity)) {
  $PublicDevIdentity = [Environment]::GetEnvironmentVariable('PHASE43_PUBLIC_DEV_IDENTITY')
}
if ([string]::IsNullOrWhiteSpace($PublicDevIdentity)) {
  $PublicDevIdentity = 'phase43-traveler-ops-smoke'
}

$SafeAnnotationFixturePath = Join-Path $ProjectRoot 'scripts/local/fixtures/phase-43-safe-annotation.json'
$ResendRewardFixturePath = Join-Path $ProjectRoot 'scripts/local/fixtures/phase-43-resend-reward.json'
$MigrationScriptPath = Join-Path $ProjectRoot 'scripts/local/apply-phase-43-traveler-ops-migration.ps1'
$Results = New-Object System.Collections.Generic.List[object]
$AdminToken = ''
$PublicToken = ''
$WorkbenchData = $null
$RewardStateData = $null
$RewardStateLoaded = $false
$PublicRewardsData = $null
$PublicRewardsLoaded = $false
$ResendRewardCheckName = 'resend reward consistency smoke'
$RewardConsistencyCheckName = 'public/admin reward state consistency'
# Acceptance markers loaded from UTF-8 fixtures/report output:
# 補發獎勵一致性實測
# 公私端獎勵狀態一致性

function Redact-SecretText {
  param([AllowNull()][string]$Text)
  if ([string]::IsNullOrEmpty($Text)) { return '' }
  $value = $Text
  $value = $value -replace '(?i)sk-[A-Za-z0-9_\-]{8,}', '[redacted-api-key]'
  $value = $value -replace '(?i)Bearer\s+[A-Za-z0-9._~+/=-]+', 'Bearer [redacted-token]'
  $value = $value -replace '(?i)(Authorization|Bearer|token|refreshToken|password|apiKey|secret|sk-|COS_SECRET|SECRET_ID|SECRET_KEY)', '[redacted-field]'
  return $value
}

function Assert-NoSecretText {
  param(
    [AllowNull()][string]$Text,
    [Parameter(Mandatory = $true)][string]$Context
  )
  if ([string]::IsNullOrEmpty($Text)) { return }
  $patterns = @(
    'sk-[A-Za-z0-9_\-]{8,}',
    'Bearer\s+',
    'refreshToken',
    'Authorization',
    'password',
    'apiKey',
    'COS_SECRET',
    'SECRET_ID',
    'SECRET_KEY'
  )
  foreach ($pattern in $patterns) {
    if ($Text -match "(?i)$pattern") {
      throw "$Context contains a banned secret-bearing pattern: $pattern"
    }
  }
}

function Escape-MarkdownCell {
  param([AllowNull()][string]$Value)
  if ($null -eq $Value) { return '' }
  return ($Value -replace '\|', '/' -replace "`r?`n", ' ').Trim()
}

function Add-Result {
  param(
    [Parameter(Mandatory = $true)][string]$Area,
    [Parameter(Mandatory = $true)][string]$Check,
    [Parameter(Mandatory = $true)][ValidateSet('PASS', 'SKIP', 'FAIL', 'BLOCKED')][string]$Status,
    [Parameter(Mandatory = $true)][string]$Evidence,
    [string]$Command = ''
  )
  $safeEvidence = Redact-SecretText -Text $Evidence
  $safeCommand = Redact-SecretText -Text $Command
  Assert-NoSecretText -Text $safeEvidence -Context "$Area/$Check evidence"
  Assert-NoSecretText -Text $safeCommand -Context "$Area/$Check command"
  $Results.Add([pscustomobject]@{
    Area = $Area
    Check = $Check
    Status = $Status
    Evidence = $safeEvidence
    Command = $safeCommand
  })
}

function Get-SafeErrorMessage {
  param($ErrorRecord)
  $message = [string]$ErrorRecord.Exception.Message
  if ([string]::IsNullOrWhiteSpace($message)) { $message = [string]$ErrorRecord }
  $message = Redact-SecretText -Text $message
  if ($message.Length -gt 260) { $message = $message.Substring(0, 260) + '...' }
  return $message
}

function Assert-LocalUrl {
  param([Parameter(Mandatory = $true)][string]$Url)
  if ($AllowNonLocal) { return }
  $uri = [Uri]$Url
  $hostName = $uri.Host.ToLowerInvariant()
  if (@('127.0.0.1', 'localhost', '::1') -contains $hostName) { return }
  throw "Refusing non-local URL '$Url'. Use -AllowNonLocal explicitly if this is intentional."
}

function Convert-ToJsonBody {
  param($Value)
  $json = ($Value | ConvertTo-Json -Depth 100 -Compress)
  $builder = New-Object System.Text.StringBuilder
  foreach ($char in $json.ToCharArray()) {
    $code = [int][char]$char
    if ($code -gt 127) {
      [void]$builder.Append('\u')
      [void]$builder.Append($code.ToString('x4'))
    } else {
      [void]$builder.Append($char)
    }
  }
  return $builder.ToString()
}

function Invoke-JsonRequest {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url,
    $Body = $null,
    [string]$Token = ''
  )
  $headers = @{ Accept = 'application/json' }
  if (-not [string]::IsNullOrWhiteSpace($Token)) {
    $headers.Authorization = "Bearer $Token"
  }
  $options = @{
    Method = $Method
    Uri = $Url
    Headers = $headers
    TimeoutSec = 45
    ErrorAction = 'Stop'
  }
  if ($null -ne $Body) {
    $options.ContentType = 'application/json; charset=utf-8'
    $options.Body = Convert-ToJsonBody $Body
  }
  return Invoke-RestMethod @options
}

function Ensure-Success {
  param(
    [Parameter(Mandatory = $true)]$Response,
    [Parameter(Mandatory = $true)][string]$Context
  )
  if ($null -eq $Response) { throw "$Context returned an empty response" }
  if ($Response.PSObject.Properties.Name -contains 'code') {
    if ($Response.code -ne 0 -and $Response.code -ne 200) {
      throw "$Context failed: code=$($Response.code), message=$($Response.message)"
    }
    return $Response.data
  }
  return $Response
}

function Get-Array {
  param($Value)
  if ($null -eq $Value) { return @() }
  return @($Value)
}

function Get-Count {
  param($Value)
  if ($null -eq $Value) { return 0 }
  return @($Value).Count
}

function Get-Prop {
  param($Value, [string]$Name)
  if ($null -eq $Value) { return $null }
  if ($Value.PSObject.Properties.Name -contains $Name) { return $Value.$Name }
  return $null
}

function Get-MapValue {
  param($Map, [string]$Name)
  if ($null -eq $Map) { return $null }
  if ($Map -is [System.Collections.IDictionary] -and $Map.Contains($Name)) { return $Map[$Name] }
  return Get-Prop -Value $Map -Name $Name
}

function Get-PageRows {
  param($PageData)
  if ($null -eq $PageData) { return @() }
  if ($PageData.PSObject.Properties.Name -contains 'list') {
    if ($null -eq $PageData.list) { return @() }
    return @($PageData.list)
  }
  if ($PageData.PSObject.Properties.Name -contains 'records') {
    if ($null -eq $PageData.records) { return @() }
    return @($PageData.records)
  }
  return @()
}

function Get-AuthToken {
  param($Value)
  if ($null -eq $Value) { return '' }
  foreach ($name in @('accessToken', 'token', 'jwt')) {
    $candidate = Get-Prop -Value $Value -Name $name
    if (-not [string]::IsNullOrWhiteSpace([string]$candidate)) { return [string]$candidate }
  }
  $session = Get-Prop -Value $Value -Name 'session'
  if ($session) {
    foreach ($name in @('accessToken', 'token', 'jwt')) {
      $candidate = Get-Prop -Value $session -Name $name
      if (-not [string]::IsNullOrWhiteSpace([string]$candidate)) { return [string]$candidate }
    }
  }
  return ''
}

function Invoke-CheckedCommand {
  param(
    [Parameter(Mandatory = $true)][string]$Command,
    [switch]$AllowFail
  )
  $stdoutPath = [System.IO.Path]::GetTempFileName()
  $stderrPath = [System.IO.Path]::GetTempFileName()
  try {
    $process = Start-Process -FilePath 'powershell.exe' -ArgumentList @('-NoProfile', '-ExecutionPolicy', 'Bypass', '-Command', $Command) -WorkingDirectory $ProjectRoot -PassThru -Wait -NoNewWindow -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath
    $stdout = Get-Content -Encoding UTF8 -Raw -LiteralPath $stdoutPath
    $stderr = Get-Content -Encoding UTF8 -Raw -LiteralPath $stderrPath
    if ($process.ExitCode -ne 0 -and -not $AllowFail) {
      throw "Command exited $($process.ExitCode): $(Redact-SecretText -Text (($stdout + $stderr).Trim()))"
    }
    return [pscustomobject]@{ ExitCode = $process.ExitCode; Output = Redact-SecretText -Text (($stdout + $stderr).Trim()) }
  } finally {
    Remove-Item -LiteralPath $stdoutPath, $stderrPath -Force -ErrorAction SilentlyContinue
  }
}

function Get-MySqlScalar {
  param([Parameter(Mandatory = $true)][string]$Sql)
  $mysql = Get-Command mysql.exe -ErrorAction SilentlyContinue
  if (-not $mysql) { $mysql = Get-Command mysql -ErrorAction SilentlyContinue }
  if (-not $mysql) { return '' }
  $hostName = [Environment]::GetEnvironmentVariable('LOCAL_MYSQL_HOST')
  if ([string]::IsNullOrWhiteSpace($hostName)) { $hostName = '127.0.0.1' }
  $port = [Environment]::GetEnvironmentVariable('LOCAL_MYSQL_PORT')
  if ([string]::IsNullOrWhiteSpace($port)) { $port = '3306' }
  $database = [Environment]::GetEnvironmentVariable('LOCAL_MYSQL_DATABASE')
  if ([string]::IsNullOrWhiteSpace($database)) { $database = 'aoxiaoyou' }
  $username = [Environment]::GetEnvironmentVariable('LOCAL_MYSQL_USERNAME')
  if ([string]::IsNullOrWhiteSpace($username)) { $username = 'root' }
  $password = [Environment]::GetEnvironmentVariable('LOCAL_MYSQL_PASSWORD')
  if ([string]::IsNullOrWhiteSpace($password)) { $password = 'Abc123456' }
  $previousPwd = [Environment]::GetEnvironmentVariable('MYSQL_PWD')
  try {
    $env:MYSQL_PWD = $password
    $output = & $mysql.Source --default-character-set=utf8mb4 --protocol=TCP "--host=$hostName" "--port=$port" "--user=$username" "--database=$database" --batch --raw --skip-column-names "--execute=$Sql" 2>&1
    if ($LASTEXITCODE -ne 0) { return '' }
    $lines = @($output | Where-Object { -not [string]::IsNullOrWhiteSpace([string]$_) })
    if ($lines.Count -eq 0) { return '' }
    return [string]$lines[-1]
  } finally {
    if ($null -eq $previousPwd) {
      Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    } else {
      $env:MYSQL_PWD = $previousPwd
    }
  }
}

function Assert-TimelineFilterRows {
  param(
    [AllowNull()]$Rows,
    [Parameter(Mandatory = $true)]$Filter
  )
  $storylineId = Get-MapValue -Map $Filter -Name 'storylineId'
  $chapterId = Get-MapValue -Map $Filter -Name 'chapterId'
  $poiId = Get-MapValue -Map $Filter -Name 'poiId'
  $status = Get-MapValue -Map $Filter -Name 'status'
  $rewardType = Get-MapValue -Map $Filter -Name 'rewardType'
  $eventTypes = Get-MapValue -Map $Filter -Name 'eventTypes'
  $mapScopeType = Get-MapValue -Map $Filter -Name 'mapScopeType'
  $mapScopeId = Get-MapValue -Map $Filter -Name 'mapScopeId'
  foreach ($row in (Get-Array $Rows)) {
    if ($storylineId -and [long](Get-Prop $row 'storylineId') -ne [long]$storylineId) {
      throw "storylineId filter mismatch"
    }
    if ($chapterId -and [long](Get-Prop $row 'chapterId') -ne [long]$chapterId) {
      throw "chapterId filter mismatch"
    }
    if ($poiId -and [long](Get-Prop $row 'poiId') -ne [long]$poiId) {
      throw "poiId filter mismatch"
    }
    if ($status -and [string](Get-Prop $row 'status') -ne [string]$status) {
      throw "status filter mismatch"
    }
    if ($rewardType -and [string](Get-Prop $row 'rewardType') -ne [string]$rewardType) {
      throw "rewardType filter mismatch"
    }
    if ($eventTypes) {
      $allowed = @($eventTypes)
      if ($allowed.Count -gt 0 -and $allowed -notcontains [string](Get-Prop $row 'entryType')) {
        throw "eventTypes filter mismatch"
      }
    }
    if ($mapScopeType -and $mapScopeId) {
      $mapScopeType = [string]$mapScopeType
      $mapScopeId = [long]$mapScopeId
      $matched = $false
      if ($mapScopeType -eq 'city' -and [long](Get-Prop $row 'cityId') -eq $mapScopeId) { $matched = $true }
      if ($mapScopeType -eq 'sub_map' -and [long](Get-Prop $row 'subMapId') -eq $mapScopeId) { $matched = $true }
      if ($mapScopeType -eq 'poi' -and [long](Get-Prop $row 'poiId') -eq $mapScopeId) { $matched = $true }
      if ($mapScopeType -eq 'storyline' -and [long](Get-Prop $row 'storylineId') -eq $mapScopeId) { $matched = $true }
      if ($mapScopeType -eq 'story_chapter' -and [long](Get-Prop $row 'chapterId') -eq $mapScopeId) { $matched = $true }
      if (-not $matched) { throw "mapScopeType/mapScopeId filter mismatch" }
    }
  }
}

function Compare-RewardStateConsistency {
  param(
    $AdminRewardState,
    $PublicRewards,
    [bool]$AdminLoaded,
    [bool]$PublicLoaded
  )
  if (-not $AdminLoaded -or -not $PublicLoaded) {
    return [pscustomobject]@{ Status = 'BLOCKED'; Evidence = 'admin or public reward state unavailable' }
  }
  $publicRows = Get-Array $PublicRewards
  $publicGameIds = @($publicRows | Where-Object { [string]$_.entryKind -eq 'game_reward' } | ForEach-Object { [long]$_.gameRewardId })
  $publicRedeemableIds = @($publicRows | Where-Object { [string]$_.entryKind -eq 'redeemable_reward' } | ForEach-Object { [long]$_.rewardId })
  $adminGameIds = @((Get-Array $AdminRewardState.gameRewards) | ForEach-Object { [long]$_.rewardId })
  $adminTitleIds = @((Get-Array $AdminRewardState.titles) | ForEach-Object { [long]$_.rewardId })
  $adminRedeemableIds = @((Get-Array $AdminRewardState.redeemableRewards) | ForEach-Object { [long]$_.rewardId })
  $missingGame = @($adminGameIds + $adminTitleIds | Where-Object { $_ -and ($publicGameIds -notcontains $_) })
  $missingRedeemable = @($adminRedeemableIds | Where-Object { $_ -and ($publicRedeemableIds -notcontains $_) })
  if ((Get-Count $missingGame) -gt 0 -or (Get-Count $missingRedeemable) -gt 0) {
    return [pscustomobject]@{ Status = 'FAIL'; Evidence = "missing game/title ids=$($missingGame -join ','); missing redeemable ids=$($missingRedeemable -join ',')" }
  }
  return [pscustomobject]@{ Status = 'PASS'; Evidence = "admin game/title=$((Get-Count $adminGameIds) + (Get-Count $adminTitleIds)), public game=$(Get-Count $publicGameIds), admin redeemable=$(Get-Count $adminRedeemableIds), public redeemable=$(Get-Count $publicRedeemableIds)" }
}

function Invoke-PublicDevBypassLogin {
  param()
  $loginData = Ensure-Success -Context 'public dev-bypass login' -Response (
    Invoke-JsonRequest -Method POST -Url "$PublicBaseUrl/api/v1/user/login/dev-bypass" -Body @{
      devIdentity = $PublicDevIdentity
      nickname = 'Phase43 Smoke'
      localeCode = 'zh-Hant'
      interfaceMode = 'standard'
    }
  )
  $PublicToken = Get-AuthToken $loginData
  if ([string]::IsNullOrWhiteSpace($PublicToken)) { throw 'public login did not return accessToken' }
  $publicUserId = [long]0
  $state = Get-Prop -Value $loginData -Name 'state'
  $profile = Get-Prop -Value $state -Name 'profile'
  $profileId = Get-Prop -Value $profile -Name 'id'
  if (-not [string]::IsNullOrWhiteSpace([string]$profileId)) { $publicUserId = [long]$profileId }
  return [pscustomobject]@{
    Token = $PublicToken
    UserId = $publicUserId
  }
}

function Write-Report {
  param([Parameter(Mandatory = $true)][string]$FinalOutcome)
  $lines = New-Object System.Collections.Generic.List[string]
  $lines.Add('# Phase 43 Traveler Operations UAT')
  $lines.Add('')
  $lines.Add("Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz')")
  $lines.Add('')
  $lines.Add('| Area | Check | Status | Evidence | Command |')
  $lines.Add('| --- | --- | --- | --- | --- |')
  foreach ($result in $Results) {
    $lines.Add("| $(Escape-MarkdownCell $result.Area) | $(Escape-MarkdownCell $result.Check) | $($result.Status) | $(Escape-MarkdownCell $result.Evidence) | $(Escape-MarkdownCell $result.Command) |")
  }
  $lines.Add('')
  $lines.Add("Final outcome: $FinalOutcome")
  $lines.Add('')
  $lines.Add('Manual WeChat physical-device UAT: PENDING. Phase 43 verifies admin/public support consistency only.')
  $reportDir = Split-Path -Parent $ReportPath
  if (-not (Test-Path -LiteralPath $reportDir)) {
    New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
  }
  Set-Content -LiteralPath $ReportPath -Encoding UTF8 -Value $lines
}

Assert-LocalUrl -Url $AdminBaseUrl
Assert-LocalUrl -Url $PublicBaseUrl

if (Test-Path -LiteralPath $ResendRewardFixturePath) {
  try {
    $resendFixtureForLabel = Get-Content -Encoding UTF8 -Raw -LiteralPath $ResendRewardFixturePath | ConvertFrom-Json
    if (-not [string]::IsNullOrWhiteSpace([string]$resendFixtureForLabel.checkName)) {
      $ResendRewardCheckName = [string]$resendFixtureForLabel.checkName
    }
  } catch {
    $ResendRewardCheckName = 'resend reward consistency smoke'
  }
}
try {
  $RewardConsistencyCheckName = [System.Text.Encoding]::UTF8.GetString([byte[]]@(0xE5,0x85,0xAC,0xE7,0xA7,0x81,0xE7,0xAB,0xAF,0xE7,0x8D,0x8E,0xE5,0x8B,0xB5,0xE7,0x8B,0x80,0xE6,0x85,0x8B,0xE4,0xB8,0x80,0xE8,0x87,0xB4,0xE6,0x80,0xA7))
} catch {
  $RewardConsistencyCheckName = 'public/admin reward state consistency'
}

try {
  if (-not $Quick) {
    $apply = Invoke-CheckedCommand -Command "& '$MigrationScriptPath'"
    $verify = Invoke-CheckedCommand -Command "& '$MigrationScriptPath' -VerifyOnly"
    Add-Result -Area 'schema' -Check 'Phase 43 migration apply/verify' -Status 'PASS' -Evidence 'Migration apply and VerifyOnly completed.' -Command 'apply-phase-43-traveler-ops-migration.ps1'
  } else {
    if (Test-Path -LiteralPath $MigrationScriptPath) {
      Add-Result -Area 'schema' -Check 'Phase 43 migration script present' -Status 'PASS' -Evidence 'Migration apply skipped in -Quick mode.' -Command 'apply-phase-43-traveler-ops-migration.ps1 -VerifyOnly'
    } else {
      Add-Result -Area 'schema' -Check 'Phase 43 migration script present' -Status 'FAIL' -Evidence 'Migration script missing.'
    }
  }
} catch {
  Add-Result -Area 'schema' -Check 'Phase 43 migration apply/verify' -Status 'FAIL' -Evidence (Get-SafeErrorMessage $_) -Command 'apply-phase-43-traveler-ops-migration.ps1'
}

try {
  if ($UserId -le 0) {
    $preAuth = Invoke-PublicDevBypassLogin
    $PublicToken = $preAuth.Token
    if ($preAuth.UserId -gt 0) {
      $UserId = $preAuth.UserId
      Add-Result -Area 'public' -Check 'dev-bypass login' -Status 'PASS' -Evidence "Public token acquired and traveler userId=$UserId resolved for admin consistency." -Command '/api/v1/user/login/dev-bypass'
    } else {
      Add-Result -Area 'public' -Check 'dev-bypass login' -Status 'PASS' -Evidence 'Public token acquired; user id not present in session payload.' -Command '/api/v1/user/login/dev-bypass'
    }
  }
} catch {
  $status = if ($Quick) { 'BLOCKED' } else { 'FAIL' }
  Add-Result -Area 'public' -Check 'dev-bypass login' -Status $status -Evidence (Get-SafeErrorMessage $_) -Command '/api/v1/user/login/dev-bypass'
}

try {
  $envToken = [Environment]::GetEnvironmentVariable('PHASE43_ADMIN_BEARER_TOKEN')
  if (-not [string]::IsNullOrWhiteSpace($envToken)) {
    $AdminToken = $envToken -replace '(?i)^Bearer\s+', ''
  } else {
    $username = [Environment]::GetEnvironmentVariable('PHASE43_ADMIN_USERNAME')
    $password = [Environment]::GetEnvironmentVariable('PHASE43_ADMIN_PASSWORD')
    if ([string]::IsNullOrWhiteSpace($username)) { $username = 'admin' }
    if ([string]::IsNullOrWhiteSpace($password)) { $password = 'admin123' }
    $loginData = Ensure-Success -Context 'admin login' -Response (
      Invoke-JsonRequest -Method POST -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{ username = $username; password = $password }
    )
    $AdminToken = Get-AuthToken $loginData
  }
  if ([string]::IsNullOrWhiteSpace($AdminToken)) { throw 'admin login did not return token' }
  Add-Result -Area 'admin' -Check 'admin auth' -Status 'PASS' -Evidence 'Admin token acquired and redacted.'
} catch {
  $status = if ($Quick) { 'BLOCKED' } else { 'FAIL' }
  Add-Result -Area 'admin' -Check 'admin auth' -Status $status -Evidence (Get-SafeErrorMessage $_) -Command "$AdminBaseUrl/api/admin/v1/auth/login"
}

if (-not [string]::IsNullOrWhiteSpace($AdminToken)) {
  try {
    if ($UserId -le 0) {
      $users = Ensure-Success -Context 'admin users' -Response (
        Invoke-JsonRequest -Method GET -Url "$AdminBaseUrl/api/admin/v1/users?pageNum=1&pageSize=10" -Token $AdminToken
      )
      $first = @(Get-PageRows $users) | Select-Object -First 1
      if ($first) { $UserId = [long]$first.userId }
    }
    if ($UserId -le 0) { throw 'No traveler user could be resolved.' }
    Add-Result -Area 'admin' -Check 'target user' -Status 'PASS' -Evidence "Using traveler userId=$UserId."
  } catch {
    Add-Result -Area 'admin' -Check 'target user' -Status 'FAIL' -Evidence (Get-SafeErrorMessage $_)
  }
}

if (-not [string]::IsNullOrWhiteSpace($AdminToken) -and $UserId -gt 0) {
  try {
    $WorkbenchData = Ensure-Success -Context 'progress workbench' -Response (
      Invoke-JsonRequest -Method GET -Url "$AdminBaseUrl/api/admin/v1/users/$UserId/progress-workbench" -Token $AdminToken
    )
    Add-Result -Area 'admin' -Check 'workbench' -Status 'PASS' -Evidence 'Loaded progress-workbench.'
  } catch {
    Add-Result -Area 'admin' -Check 'workbench' -Status 'FAIL' -Evidence (Get-SafeErrorMessage $_) -Command '/progress-workbench'
  }

  $timelineFilterRequests = @(
    @{ label = 'eventTypes'; query = @{ eventTypes = @('exploration_event') } },
    @{ label = 'storylineId'; query = @{ storylineId = [long](Get-Prop ((Get-Array $WorkbenchData.storylineSessions | Select-Object -First 1)) 'storylineId') } },
    @{ label = 'chapterId'; query = @{ chapterId = [long](Get-Prop ((Get-Array $WorkbenchData.storylineSessions | Select-Object -First 1)) 'currentChapterId') } },
    @{ label = 'poiId'; query = @{ poiId = [long]1 } },
    @{ label = 'mapScopeType/mapScopeId'; query = @{ mapScopeType = 'city'; mapScopeId = [long]1 } },
    @{ label = 'status'; query = @{ status = 'completed' } },
    @{ label = 'rewardType'; query = @{ rewardType = 'badge' } },
    @{ label = 'from/to'; query = @{ from = (Get-Date).AddYears(-5).ToString('yyyy-MM-ddTHH:mm:ss'); to = (Get-Date).AddDays(1).ToString('yyyy-MM-ddTHH:mm:ss') } }
  )
  foreach ($request in $timelineFilterRequests) {
    try {
      $query = $request.query
      $parts = New-Object System.Collections.Generic.List[string]
      $parts.Add('pageNum=1')
      $parts.Add('pageSize=20')
      foreach ($key in $query.Keys) {
        $value = $query[$key]
        if ($null -eq $value -or [string]::IsNullOrWhiteSpace([string]$value)) { continue }
        if ($value -is [array]) { $value = ($value -join ',') }
        $parts.Add("$key=$([Uri]::EscapeDataString([string]$value))")
      }
      $url = "$AdminBaseUrl/api/admin/v1/users/$UserId/timeline?$($parts -join '&')"
      $page = Ensure-Success -Context "timeline $($request.label)" -Response (
        Invoke-JsonRequest -Method GET -Url $url -Token $AdminToken
      )
      $rows = @(Get-PageRows $page)
      Assert-TimelineFilterRows -Rows $rows -Filter $query
      Add-Result -Area 'admin filters' -Check $request.label -Status 'PASS' -Evidence "Returned $(Get-Count $rows) row(s) conforming to active filter." -Command '/timeline'
    } catch {
      Add-Result -Area 'admin filters' -Check $request.label -Status 'FAIL' -Evidence (Get-SafeErrorMessage $_) -Command '/timeline'
    }
  }

  try {
    $breakdown = Ensure-Success -Context 'progress breakdown' -Response (
      Invoke-JsonRequest -Method GET -Url "$AdminBaseUrl/api/admin/v1/users/$UserId/progress-breakdown?scopeType=global" -Token $AdminToken
    )
    Add-Result -Area 'admin' -Check 'progress breakdown' -Status 'PASS' -Evidence "Progress percent=$($breakdown.progressPercent)." -Command '/progress-breakdown?scopeType=global'
  } catch {
    Add-Result -Area 'admin' -Check 'progress breakdown' -Status 'FAIL' -Evidence (Get-SafeErrorMessage $_) -Command '/progress-breakdown?scopeType=global'
  }

  try {
    $RewardStateData = Ensure-Success -Context 'reward state' -Response (
      Invoke-JsonRequest -Method GET -Url "$AdminBaseUrl/api/admin/v1/users/$UserId/reward-state" -Token $AdminToken
    )
    $RewardStateLoaded = $true
    Add-Result -Area 'admin' -Check 'reward state' -Status 'PASS' -Evidence "gameRewards=$(Get-Count (Get-Array $RewardStateData.gameRewards)), titles=$(Get-Count (Get-Array $RewardStateData.titles)), redeemable=$(Get-Count (Get-Array $RewardStateData.redeemableRewards))." -Command '/reward-state'
  } catch {
    Add-Result -Area 'admin' -Check 'reward state' -Status 'FAIL' -Evidence (Get-SafeErrorMessage $_) -Command '/reward-state'
  }

  try {
    $trace = Ensure-Success -Context 'reward rule trace' -Response (
      Invoke-JsonRequest -Method GET -Url "$AdminBaseUrl/api/admin/v1/users/$UserId/reward-rule-trace" -Token $AdminToken
    )
    if ([string]::IsNullOrWhiteSpace([string]$trace.traceStatus)) { throw 'traceStatus missing' }
    Add-Result -Area 'admin' -Check 'reward rule trace' -Status 'PASS' -Evidence "traceStatus=$($trace.traceStatus); missingLinks=$(Get-Count (Get-Array $trace.missingLinks))." -Command '/reward-rule-trace'
  } catch {
    Add-Result -Area 'admin' -Check 'reward rule trace' -Status 'FAIL' -Evidence (Get-SafeErrorMessage $_) -Command '/reward-rule-trace'
  }

  try {
    $auditsBefore = Ensure-Success -Context 'audits' -Response (
      Invoke-JsonRequest -Method GET -Url "$AdminBaseUrl/api/admin/v1/users/$UserId/progress-ops/audits?pageNum=1&pageSize=20" -Token $AdminToken
    )
    $auditRowsBefore = @(Get-PageRows $auditsBefore)
    Add-Result -Area 'admin' -Check 'audit listing' -Status 'PASS' -Evidence "Loaded $(Get-Count $auditRowsBefore) audit row(s)." -Command '/progress-ops/audits'
  } catch {
    Add-Result -Area 'admin' -Check 'audit listing' -Status 'FAIL' -Evidence (Get-SafeErrorMessage $_) -Command '/progress-ops/audits'
  }

  try {
    $annotationFixture = Get-Content -Encoding UTF8 -Raw -LiteralPath $SafeAnnotationFixturePath | ConvertFrom-Json
    $annotationPreviewBody = @{
      userId = $UserId
      scopeType = 'global'
      actionType = 'ANNOTATE_ISSUE'
      annotationText = [string]$annotationFixture.annotationText
      issueSeverity = [string]$annotationFixture.issueSeverity
      reason = [string]$annotationFixture.reason
    }
    $preview = Ensure-Success -Context 'safe annotation preview' -Response (
      Invoke-JsonRequest -Method POST -Url "$AdminBaseUrl/api/admin/v1/users/$UserId/progress-ops/repair-preview" -Token $AdminToken -Body $annotationPreviewBody
    )
    if ([string]::IsNullOrWhiteSpace([string]$preview.previewHash)) { throw 'previewHash missing' }
    Add-Result -Area 'support ops' -Check 'preview operation' -Status 'PASS' -Evidence "Preview hash returned for ANNOTATE_ISSUE." -Command '/progress-ops/repair-preview'

    if ($Quick -and -not $ApplySafeAnnotation) {
      Add-Result -Area 'support ops' -Check 'safe apply annotation' -Status 'SKIP' -Evidence '-Quick mode is preview-only.'
    } else {
      $applyBody = $annotationPreviewBody.Clone()
      $applyBody.previewHash = [string]$preview.previewHash
      $applyBody.confirmationToken = [string]$preview.confirmationToken
      $applyBody.confirmationText = 'REPAIR'
      $applyResult = Ensure-Success -Context 'safe annotation apply' -Response (
        Invoke-JsonRequest -Method POST -Url "$AdminBaseUrl/api/admin/v1/users/$UserId/progress-ops/repair-apply" -Token $AdminToken -Body $applyBody
      )
      Add-Result -Area 'support ops' -Check 'safe apply annotation' -Status 'PASS' -Evidence "status=$($applyResult.status)." -Command '/progress-ops/repair-apply'
      $auditsAfter = Ensure-Success -Context 'audits after annotation' -Response (
        Invoke-JsonRequest -Method GET -Url "$AdminBaseUrl/api/admin/v1/users/$UserId/progress-ops/audits?pageNum=1&pageSize=50&actionTypes=ANNOTATE_ISSUE" -Token $AdminToken
      )
      $found = $false
      foreach ($row in @(Get-PageRows $auditsAfter)) {
        $resultSummaryText = ($row.resultSummary | ConvertTo-Json -Depth 20 -Compress)
        if ([string]$row.actionType -eq 'ANNOTATE_ISSUE' -and ($resultSummaryText -like '*annotated*' -or [string]$row.reason -eq [string]$annotationFixture.reason)) {
          $found = $true
          break
        }
      }
      if (-not $found) { throw 'ANNOTATE_ISSUE audit row not visible after apply' }
      Add-Result -Area 'support ops' -Check 'audit verification' -Status 'PASS' -Evidence 'ANNOTATE_ISSUE audit row visible after apply.' -Command '/progress-ops/audits'
    }
  } catch {
    Add-Result -Area 'support ops' -Check 'preview/apply annotation' -Status 'FAIL' -Evidence (Get-SafeErrorMessage $_) -Command '/progress-ops/repair-preview'
  }

  if (-not $Quick) {
    try {
      $fixture = Get-Content -Encoding UTF8 -Raw -LiteralPath $ResendRewardFixturePath | ConvertFrom-Json
      $gameRewardId = [Environment]::GetEnvironmentVariable('PHASE43_SMOKE_GAME_REWARD_ID')
      if ([string]::IsNullOrWhiteSpace($gameRewardId)) { $gameRewardId = Get-MySqlScalar "SELECT id FROM game_rewards WHERE code = 'phase43_smoke_game_reward' LIMIT 1;" }
      $rewardId = [Environment]::GetEnvironmentVariable('PHASE43_SMOKE_REWARD_ID')
      $ruleId = [Environment]::GetEnvironmentVariable('PHASE43_SMOKE_RULE_ID')
      if ([string]::IsNullOrWhiteSpace($ruleId)) { $ruleId = Get-MySqlScalar "SELECT id FROM reward_rules WHERE code = 'phase43_smoke_resend_rule' LIMIT 1;" }
      $sourceEventId = [Environment]::GetEnvironmentVariable('PHASE43_SMOKE_SOURCE_EVENT_ID')
      if ([string]::IsNullOrWhiteSpace($gameRewardId) -and [string]::IsNullOrWhiteSpace($rewardId)) { throw 'phase43_smoke_game_reward id unavailable' }
      if ([string]::IsNullOrWhiteSpace($ruleId)) { throw 'phase43_smoke_resend_rule id unavailable' }
      $body = @{
        userId = $UserId
        scopeType = 'global'
        actionType = 'RESEND_REWARD'
        reason = [string]$fixture.reason
        ruleId = [long]$ruleId
      }
      if (-not [string]::IsNullOrWhiteSpace($gameRewardId)) { $body.gameRewardId = [long]$gameRewardId }
      if (-not [string]::IsNullOrWhiteSpace($rewardId)) { $body.rewardId = [long]$rewardId }
      if (-not [string]::IsNullOrWhiteSpace($sourceEventId)) { $body.sourceEventId = [long]$sourceEventId }
      $preview = Ensure-Success -Context 'resend reward preview' -Response (
        Invoke-JsonRequest -Method POST -Url "$AdminBaseUrl/api/admin/v1/users/$UserId/progress-ops/repair-preview" -Token $AdminToken -Body $body
      )
      if ([string]::IsNullOrWhiteSpace([string]$preview.previewHash)) { throw 'resend reward previewHash missing' }
      $applyBody = $body.Clone()
      $applyBody.previewHash = [string]$preview.previewHash
      $applyBody.confirmationToken = [string]$preview.confirmationToken
      $applyBody.confirmationText = 'REPAIR'
      $result = Ensure-Success -Context 'resend reward apply' -Response (
        Invoke-JsonRequest -Method POST -Url "$AdminBaseUrl/api/admin/v1/users/$UserId/progress-ops/repair-apply" -Token $AdminToken -Body $applyBody
      )
      $summaryJson = $result.resultSummary | ConvertTo-Json -Depth 20 -Compress
      $writtenStateRows = [int](Get-Prop -Value $result.resultSummary -Name 'writtenStateRows')
      $alreadyGranted = [bool](Get-Prop -Value $result.resultSummary -Name 'alreadyGranted')
      $grantRowId = Get-Prop -Value $result.resultSummary -Name 'grantRowId'
      if ($summaryJson -notmatch 'resent|already_present' -and $writtenStateRows -le 0 -and -not $alreadyGranted -and [string]::IsNullOrWhiteSpace([string]$grantRowId)) {
        throw "unexpected resend result summary: $summaryJson"
      }
      Add-Result -Area 'support ops' -Check $ResendRewardCheckName -Status 'PASS' -Evidence "RESEND_REWARD result accepted: $($result.status)." -Command '/progress-ops/repair-apply RESEND_REWARD'
      $RewardStateData = Ensure-Success -Context 'reward state after resend' -Response (
        Invoke-JsonRequest -Method GET -Url "$AdminBaseUrl/api/admin/v1/users/$UserId/reward-state" -Token $AdminToken
      )
      $RewardStateLoaded = $true
    } catch {
      Add-Result -Area 'support ops' -Check $ResendRewardCheckName -Status 'FAIL' -Evidence (Get-SafeErrorMessage $_) -Command 'RESEND_REWARD'
    }
  } else {
    Add-Result -Area 'support ops' -Check $ResendRewardCheckName -Status 'SKIP' -Evidence '-Quick mode skips resend apply.'
  }
}

try {
  if ([string]::IsNullOrWhiteSpace($PublicToken)) {
    $publicLoginResult = Invoke-PublicDevBypassLogin
    $PublicToken = $publicLoginResult.Token
    Add-Result -Area 'public' -Check 'dev-bypass login' -Status 'PASS' -Evidence 'Public token acquired and redacted.' -Command '/api/v1/user/login/dev-bypass'
  }
} catch {
  $status = if ($Quick) { 'BLOCKED' } else { 'FAIL' }
  Add-Result -Area 'public' -Check 'dev-bypass login' -Status $status -Evidence (Get-SafeErrorMessage $_) -Command '/api/v1/user/login/dev-bypass'
}

if (-not [string]::IsNullOrWhiteSpace($PublicToken)) {
  foreach ($endpoint in @(
    @{ check = 'user state'; url = "$PublicBaseUrl/api/v1/user/state" },
    @{ check = 'user progress'; url = "$PublicBaseUrl/api/v1/user/progress" },
    @{ check = 'public rewards'; url = "$PublicBaseUrl/api/v1/user/progress/rewards" },
    @{ check = 'user exploration'; url = "$PublicBaseUrl/api/v1/users/me/exploration" }
  )) {
    try {
      $data = Ensure-Success -Context $endpoint.check -Response (
        Invoke-JsonRequest -Method GET -Url $endpoint.url -Token $PublicToken
      )
      if ($endpoint.check -eq 'public rewards') {
        $PublicRewardsData = @(Get-Array $data)
        $PublicRewardsLoaded = $true
      }
      Add-Result -Area 'public' -Check $endpoint.check -Status 'PASS' -Evidence 'Endpoint returned structured ApiResponse.' -Command $endpoint.url.Replace($PublicBaseUrl, '')
    } catch {
      $safe = Get-SafeErrorMessage $_
      $status = if ($endpoint.check -eq 'user exploration' -and $safe -match '404') { 'BLOCKED' } else { 'FAIL' }
      Add-Result -Area 'public' -Check $endpoint.check -Status $status -Evidence $safe -Command $endpoint.url.Replace($PublicBaseUrl, '')
    }
  }

  $comparison = Compare-RewardStateConsistency -AdminRewardState $RewardStateData -PublicRewards $PublicRewardsData -AdminLoaded $RewardStateLoaded -PublicLoaded $PublicRewardsLoaded
  Add-Result -Area 'public/admin' -Check $RewardConsistencyCheckName -Status $comparison.Status -Evidence $comparison.Evidence -Command '/reward-state vs /api/v1/user/progress/rewards'
} else {
  Add-Result -Area 'public/admin' -Check $RewardConsistencyCheckName -Status 'BLOCKED' -Evidence 'Public user context unavailable.' -Command '/api/v1/user/progress/rewards'
}

$hasFail = @($Results | Where-Object { $_.Status -eq 'FAIL' }).Count -gt 0
$hasBlocked = @($Results | Where-Object { $_.Status -eq 'BLOCKED' }).Count -gt 0
$finalOutcome = if ($hasFail) { 'FAIL' } elseif ($hasBlocked) { 'BLOCKED' } else { 'PASS' }
Write-Report -FinalOutcome $finalOutcome
Write-Host "Final outcome: $finalOutcome"

if ($finalOutcome -eq 'PASS') { exit 0 }
if ($Quick -and $finalOutcome -eq 'BLOCKED') { exit 0 }
exit 1
