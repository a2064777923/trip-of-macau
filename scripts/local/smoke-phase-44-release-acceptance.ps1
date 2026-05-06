param(
  [switch]$Quick,
  [switch]$StaticOnly,
  [switch]$IncludeBuilds,
  [switch]$IncludeExistingSmokes,
  [switch]$IncludeBrowserChecklist,
  [switch]$IncludeWeChatChecklist,
  [string]$ReportPath = '',
  [string]$AdminBaseUrl = '',
  [string]$PublicBaseUrl = '',
  [switch]$AllowNonLocal
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$PhaseDir = '.planning/phases/44-management-system-ia-polish-and-release-acceptance'
$FixturePath = Join-Path $ProjectRoot 'scripts/local/fixtures/phase-44-admin-ia-text-checks.json'
$AdminTextCheckPath = Join-Path $ProjectRoot 'scripts/local/check-phase-44-admin-text.ps1'

if ([string]::IsNullOrWhiteSpace($ReportPath)) {
  $ReportPath = Join-Path $ProjectRoot "$PhaseDir/44-UAT.md"
}
if ([string]::IsNullOrWhiteSpace($AdminBaseUrl)) {
  $AdminBaseUrl = [Environment]::GetEnvironmentVariable('PHASE44_ADMIN_BASE_URL')
}
if ([string]::IsNullOrWhiteSpace($AdminBaseUrl)) {
  $AdminBaseUrl = 'http://127.0.0.1:8081'
}
if ([string]::IsNullOrWhiteSpace($PublicBaseUrl)) {
  $PublicBaseUrl = [Environment]::GetEnvironmentVariable('PHASE44_PUBLIC_BASE_URL')
}
if ([string]::IsNullOrWhiteSpace($PublicBaseUrl)) {
  $PublicBaseUrl = 'http://127.0.0.1:8080'
}
$AdminBaseUrl = $AdminBaseUrl.TrimEnd('/')
$PublicBaseUrl = $PublicBaseUrl.TrimEnd('/')

$AutomatedResults = New-Object System.Collections.Generic.List[object]
$BrowserRows = New-Object System.Collections.Generic.List[object]
$DevToolsRows = New-Object System.Collections.Generic.List[object]
$DeviceRows = New-Object System.Collections.Generic.List[object]

function Redact-SecretText {
  param([AllowNull()][string]$Text)
  if ([string]::IsNullOrEmpty($Text)) { return '' }
  $value = $Text
  $value = $value -replace '(?i)sk-[A-Za-z0-9_\-]{8,}', '[redacted-api-key]'
  $value = $value -replace '(?i)Bearer\s+[A-Za-z0-9._~+/=-]+', 'Bearer [redacted-token]'
  $value = $value -replace '(?i)(Authorization|refreshToken|password|apiKey|COS_SECRET|SECRET_ID|SECRET_KEY)', '[redacted-field]'
  $value = $value -replace '(?i)(^|[^A-Za-z])token([^A-Za-z]|$)', '$1[redacted-field]$2'
  $value = $value -replace '(?i)(^|[^A-Za-z])secret([^A-Za-z]|$)', '$1[redacted-field]$2'
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
    'Authorization',
    'refreshToken',
    'password',
    'apiKey',
    'COS_SECRET',
    'SECRET_ID',
    'SECRET_KEY'
  )
  foreach ($pattern in $patterns) {
    if ($Text -match "(?i)$pattern") {
      throw "$Context contains banned secret pattern: $pattern"
    }
  }
}

function Assert-LocalUrl {
  param([Parameter(Mandatory = $true)][string]$Url)
  if ($AllowNonLocal) { return }
  $uri = [Uri]$Url
  $hostName = $uri.Host.ToLowerInvariant()
  if (@('127.0.0.1', 'localhost', '::1') -contains $hostName) { return }
  throw "Refusing non-local URL '$Url'. Pass -AllowNonLocal only for intentional non-local release checks."
}

function Escape-MarkdownCell {
  param([AllowNull()][string]$Value)
  if ($null -eq $Value) { return '' }
  return ((Redact-SecretText -Text $Value) -replace '\|', '/' -replace "`r?`n", ' ').Trim()
}

function Get-SafeErrorMessage {
  param($ErrorRecord)
  $message = [string]$ErrorRecord.Exception.Message
  if ([string]::IsNullOrWhiteSpace($message)) { $message = [string]$ErrorRecord }
  $message = Redact-SecretText -Text $message
  if ($message.Length -gt 240) { $message = $message.Substring(0, 240) + '...' }
  return $message
}

function Add-AutomatedResult {
  param(
    [Parameter(Mandatory = $true)][string]$Area,
    [Parameter(Mandatory = $true)][string]$Check,
    [Parameter(Mandatory = $true)][ValidateSet('PASS', 'FAIL', 'BLOCKED', 'SKIP')][string]$Status,
    [AllowEmptyString()][string]$Evidence,
    [string]$Command = ''
  )
  $safeEvidence = Redact-SecretText -Text $Evidence
  $safeCommand = Redact-SecretText -Text $Command
  Assert-NoSecretText -Text $safeEvidence -Context "$Area/$Check evidence"
  Assert-NoSecretText -Text $safeCommand -Context "$Area/$Check command"
  $AutomatedResults.Add([pscustomobject]@{
    Area = $Area
    Check = $Check
    Status = $Status
    Evidence = $safeEvidence
    Command = $safeCommand
  })
}

function Add-ManualRow {
  param(
    [Parameter(Mandatory = $true)]$Rows,
    [Parameter(Mandatory = $true)][string]$Check,
    [Parameter(Mandatory = $true)][ValidateSet('PASS', 'FAIL', 'BLOCKED', 'PENDING')][string]$Status,
    [AllowEmptyString()][string]$Evidence,
    [string]$Reason = ''
  )
  $safeEvidence = Redact-SecretText -Text $Evidence
  $safeReason = Redact-SecretText -Text $Reason
  Assert-NoSecretText -Text $safeEvidence -Context "$Check manual evidence"
  Assert-NoSecretText -Text $safeReason -Context "$Check manual reason"
  $Rows.Add([pscustomobject]@{
    Check = $Check
    Status = $Status
    Evidence = $safeEvidence
    Reason = $safeReason
  })
}

function Invoke-CheckedCommand {
  param(
    [Parameter(Mandatory = $true)][string]$Area,
    [Parameter(Mandatory = $true)][string]$Check,
    [Parameter(Mandatory = $true)][string]$Command,
    [string]$WorkingDirectory = $ProjectRoot
  )

  $stdoutPath = [System.IO.Path]::GetTempFileName()
  $stderrPath = [System.IO.Path]::GetTempFileName()
  try {
    $process = Start-Process -FilePath 'powershell.exe' -ArgumentList @(
      '-NoProfile',
      '-ExecutionPolicy',
      'Bypass',
      '-Command',
      $Command
    ) -WorkingDirectory $WorkingDirectory -PassThru -Wait -NoNewWindow -RedirectStandardOutput $stdoutPath -RedirectStandardError $stderrPath

    $stdout = Get-Content -LiteralPath $stdoutPath -Encoding UTF8 -Raw
    $stderr = Get-Content -LiteralPath $stderrPath -Encoding UTF8 -Raw
    $combined = Redact-SecretText -Text (($stdout + "`n" + $stderr).Trim())
    if ($process.ExitCode -eq 0) {
      Add-AutomatedResult -Area $Area -Check $Check -Status 'PASS' -Evidence 'Command exited 0.' -Command $Command
      return
    }

    $line = (($combined -split "`r?`n") | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -Last 1)
    if ([string]::IsNullOrWhiteSpace($line)) { $line = "exit code $($process.ExitCode)" }
    if ($line.Length -gt 220) { $line = $line.Substring(0, 220) + '...' }
    Add-AutomatedResult -Area $Area -Check $Check -Status 'FAIL' -Evidence "Command exited $($process.ExitCode): $line" -Command $Command
  } finally {
    Remove-Item -LiteralPath $stdoutPath, $stderrPath -Force -ErrorAction SilentlyContinue
  }
}

function Get-ManualStatusSet {
  param(
    [Parameter(Mandatory = $true)][string]$Prefix,
    [Parameter(Mandatory = $true)][string]$DefaultStatus
  )
  $status = [Environment]::GetEnvironmentVariable("${Prefix}_STATUS")
  $evidence = [Environment]::GetEnvironmentVariable("${Prefix}_EVIDENCE")
  $reason = [Environment]::GetEnvironmentVariable("${Prefix}_REASON")
  if ([string]::IsNullOrWhiteSpace($status)) { $status = $DefaultStatus }
  $status = $status.Trim().ToUpperInvariant()
  if (@('PASS', 'FAIL', 'BLOCKED', 'PENDING') -notcontains $status) {
    $status = 'FAIL'
    $reason = '手動狀態值不合法。'
  }
  if (($status -eq 'PASS') -and ([string]::IsNullOrWhiteSpace($evidence))) {
    $status = 'FAIL'
    $reason = 'PASS 缺少證據。'
  }
  if ((($status -eq 'FAIL') -or ($status -eq 'BLOCKED')) -and ([string]::IsNullOrWhiteSpace($reason))) {
    $reason = '未提供原因'
  }
  if ([string]::IsNullOrWhiteSpace($evidence)) { $evidence = '' }
  if ([string]::IsNullOrWhiteSpace($reason)) { $reason = '' }
  return [pscustomobject]@{
    Status = $status
    Evidence = $evidence
    Reason = $reason
  }
}

function Add-BrowserChecklist {
  $manual = Get-ManualStatusSet -Prefix 'PHASE44_BROWSER' -DefaultStatus 'PENDING'
  foreach ($check in @(
    '側欄 IA 巡檢',
    '故事素材包預覽',
    '媒體資源詳情',
    '體驗流程工作台',
    '互動模板庫',
    '規則治理中心',
    '旅客進度與獎勵支援'
  )) {
    Add-ManualRow -Rows $BrowserRows -Check $check -Status $manual.Status -Evidence $manual.Evidence -Reason $manual.Reason
  }
}

function Add-WeChatChecklist {
  $devTools = Get-ManualStatusSet -Prefix 'PHASE44_DEVTOOLS' -DefaultStatus 'PENDING'
  foreach ($check in @(
    'DevTools 旗艦故事進入',
    'DevTools 章節與媒體播放',
    'DevTools 事件上報'
  )) {
    Add-ManualRow -Rows $DevToolsRows -Check $check -Status $devTools.Status -Evidence $devTools.Evidence -Reason $devTools.Reason
  }

  $device = Get-ManualStatusSet -Prefix 'PHASE44_DEVICE' -DefaultStatus 'PENDING'
  foreach ($check in @(
    '實機故事進入',
    '實機定位 / 靠近事件'
  )) {
    Add-ManualRow -Rows $DeviceRows -Check $check -Status $device.Status -Evidence $device.Evidence -Reason $device.Reason
  }
}

function Add-DefaultManualPlaceholders {
  if ($BrowserRows.Count -eq 0) {
    Add-ManualRow -Rows $BrowserRows -Check '瀏覽器管理端檢查' -Status 'PENDING' -Evidence '' -Reason '未要求或未提供瀏覽器證據。'
  }
  if ($DevToolsRows.Count -eq 0) {
    Add-ManualRow -Rows $DevToolsRows -Check 'DevTools 旗艦故事煙測' -Status 'PENDING' -Evidence '' -Reason '未要求或未提供 DevTools 證據。'
  }
  if ($DeviceRows.Count -eq 0) {
    Add-ManualRow -Rows $DeviceRows -Check '實機旗艦故事煙測' -Status 'PENDING' -Evidence '' -Reason '未要求或未提供實機證據。'
  }
}

function Write-ManualTable {
  param(
    [Parameter(Mandatory = $true)]$Lines,
    [Parameter(Mandatory = $true)]$Rows
  )
  $Lines.Add('| Check | Status | Evidence | Reason |')
  $Lines.Add('| --- | --- | --- | --- |')
  foreach ($row in $Rows) {
    $Lines.Add("| $(Escape-MarkdownCell $row.Check) | $(Escape-MarkdownCell $row.Status) | $(Escape-MarkdownCell $row.Evidence) | $(Escape-MarkdownCell $row.Reason) |")
  }
}

function Get-FinalOutcome {
  $automatedFails = @($AutomatedResults | Where-Object { $_.Status -eq 'FAIL' })
  $automatedBlocked = @($AutomatedResults | Where-Object { $_.Status -eq 'BLOCKED' })
  if ($automatedFails.Count -gt 0) { return 'FAIL' }
  if ($automatedBlocked.Count -gt 0) { return 'BLOCKED' }

  $browserRowsToCheck = @(if ($IncludeBrowserChecklist) { $BrowserRows } else { @() })
  if ($browserRowsToCheck.Count -gt 0) {
    $browserNotPass = @($browserRowsToCheck | Where-Object { $_.Status -ne 'PASS' })
    if ($browserNotPass.Count -gt 0) { return 'BLOCKED' }
  } else {
    return 'BLOCKED'
  }

  $devToolsPass = @($DevToolsRows | Where-Object { $_.Check -eq 'DevTools 旗艦故事進入' -and $_.Status -eq 'PASS' -and -not [string]::IsNullOrWhiteSpace([string]$_.Evidence) }).Count -gt 0
  $devicePass = @($DeviceRows | Where-Object { $_.Check -eq '實機故事進入' -and $_.Status -eq 'PASS' -and -not [string]::IsNullOrWhiteSpace([string]$_.Evidence) }).Count -gt 0
  if (-not ($devToolsPass -or $devicePass)) { return 'BLOCKED' }

  $manualRows = @($BrowserRows) + @($DevToolsRows) + @($DeviceRows)
  $manualFails = @($manualRows | Where-Object { $_.Status -eq 'FAIL' })
  if ($manualFails.Count -gt 0) { return 'FAIL' }
  return 'PASS'
}

function Write-Report {
  $reportFullPath = $ReportPath
  if (-not [System.IO.Path]::IsPathRooted($reportFullPath)) {
    $reportFullPath = Join-Path $ProjectRoot $reportFullPath
  }
  $reportDir = Split-Path -Parent $reportFullPath
  if (-not (Test-Path -LiteralPath $reportDir)) {
    New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
  }

  $fixture = Get-Content -LiteralPath $FixturePath -Encoding UTF8 | ConvertFrom-Json
  $finalOutcome = Get-FinalOutcome

  $lines = New-Object System.Collections.Generic.List[string]
  $lines.Add('# Phase 44 Release Acceptance UAT')
  $lines.Add('')
  $lines.Add("Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz')")
  $lines.Add("Mode: Quick=$([bool]$Quick); StaticOnly=$([bool]$StaticOnly); IncludeBuilds=$([bool]$IncludeBuilds); IncludeExistingSmokes=$([bool]$IncludeExistingSmokes); IncludeBrowserChecklist=$([bool]$IncludeBrowserChecklist); IncludeWeChatChecklist=$([bool]$IncludeWeChatChecklist)")
  $lines.Add("Admin URL: $(Escape-MarkdownCell $AdminBaseUrl)")
  $lines.Add("Public URL: $(Escape-MarkdownCell $PublicBaseUrl)")
  $lines.Add("Fixture groups: layout=$(@($fixture.requiredLayoutText).Count); mission=$(@($fixture.requiredPageMissionText).Count); usability=$(@($fixture.requiredUsabilityText).Count); media=$(@($fixture.requiredMediaText).Count)")
  $lines.Add('')
  $lines.Add('## Automated Smoke')
  $lines.Add('')
  $lines.Add('| Area | Check | Status | Evidence | Command |')
  $lines.Add('| --- | --- | --- | --- | --- |')
  foreach ($result in $AutomatedResults) {
    $lines.Add("| $(Escape-MarkdownCell $result.Area) | $(Escape-MarkdownCell $result.Check) | $(Escape-MarkdownCell $result.Status) | $(Escape-MarkdownCell $result.Evidence) | $(Escape-MarkdownCell $result.Command) |")
  }
  $lines.Add('')
  $lines.Add('## Browser / Admin Checks')
  $lines.Add('')
  Write-ManualTable -Lines $lines -Rows $BrowserRows
  $lines.Add('')
  $lines.Add('## WeChat DevTools Checks')
  $lines.Add('')
  Write-ManualTable -Lines $lines -Rows $DevToolsRows
  $lines.Add('')
  $lines.Add('## Physical Device Checks')
  $lines.Add('')
  Write-ManualTable -Lines $lines -Rows $DeviceRows
  $lines.Add('')
  $lines.Add('## Accepted Caveats')
  $lines.Add('')
  $lines.Add('- 未執行的 WeChat DevTools 或實機檢查不標記為 PASS。')
  $lines.Add('- 進階 AR、語音、拼圖、防守玩法與生產級室內定位屬後續 gameplay scope。')
  $lines.Add('')
  $lines.Add('## Future Gameplay Scope')
  $lines.Add('')
  $lines.Add('- AR、語音、拼圖、防守玩法與生產級室內定位留待後續 gameplay milestone。')
  $lines.Add('- 小程序完整裝置旅程、真實定位漂移處理與高階玩法仍需在後續 milestone 以 DevTools 或實機證據驗收。')
  $lines.Add('')
  $lines.Add("Final outcome: $finalOutcome")

  $content = ($lines -join "`n") + "`n"
  Assert-NoSecretText -Text $content -Context 'Phase 44 UAT report'
  Set-Content -LiteralPath $reportFullPath -Encoding UTF8 -Value $content
  return $finalOutcome
}

Assert-LocalUrl -Url $AdminBaseUrl
Assert-LocalUrl -Url $PublicBaseUrl

try {
  $null = Get-Content -LiteralPath $FixturePath -Encoding UTF8 | ConvertFrom-Json
  Add-AutomatedResult -Area 'static' -Check 'UTF-8 fixture load' -Status 'PASS' -Evidence 'phase-44-admin-ia-text-checks.json loaded with ConvertFrom-Json.' -Command 'Get-Content -Encoding UTF8'
} catch {
  Add-AutomatedResult -Area 'static' -Check 'UTF-8 fixture load' -Status 'FAIL' -Evidence (Get-SafeErrorMessage $_) -Command 'Get-Content -Encoding UTF8'
}

Invoke-CheckedCommand -Area 'static' -Check 'admin IA and copy fixture' -Command "& '$AdminTextCheckPath'"

if (-not $StaticOnly) {
  Invoke-CheckedCommand -Area 'admin ui' -Check 'type-check' -Command 'npm run type-check' -WorkingDirectory (Join-Path $ProjectRoot 'packages/admin/aoxiaoyou-admin-ui')
}

if ($IncludeBuilds -and -not $StaticOnly) {
  Invoke-CheckedCommand -Area 'admin ui' -Check 'build' -Command 'npm run build' -WorkingDirectory (Join-Path $ProjectRoot 'packages/admin/aoxiaoyou-admin-ui')
  Invoke-CheckedCommand -Area 'admin backend' -Check 'compile' -Command 'mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml'
  Invoke-CheckedCommand -Area 'public backend' -Check 'compile' -Command 'mvn -q -DskipTests compile -f packages/server/pom.xml'
}

if ($IncludeExistingSmokes -and -not $StaticOnly) {
  $existingSmokeReportDir = Join-Path ([System.IO.Path]::GetTempPath()) 'trip-of-macau-phase44-existing-smokes'
  if (-not (Test-Path -LiteralPath $existingSmokeReportDir)) {
    New-Item -ItemType Directory -Path $existingSmokeReportDir -Force | Out-Null
  }
  foreach ($smoke in @(
    @{ Label = 'scripts/local/smoke-phase-41-wechat-runtime-uat.ps1 -Quick'; Script = 'scripts/local/smoke-phase-41-wechat-runtime-uat.ps1'; Report = '41-UAT-quick.md' },
    @{ Label = 'scripts/local/smoke-phase-42-gameplay-event-engine.ps1 -Quick'; Script = 'scripts/local/smoke-phase-42-gameplay-event-engine.ps1'; Report = '42-UAT-quick.md' },
    @{ Label = 'scripts/local/smoke-phase-43-traveler-ops.ps1 -Quick'; Script = 'scripts/local/smoke-phase-43-traveler-ops.ps1'; Report = '43-UAT-quick.md' }
  )) {
    $smokeReportPath = Join-Path $existingSmokeReportDir ([string]$smoke.Report)
    $smokeCommand = "& ./$($smoke.Script) -Quick -ReportPath '$smokeReportPath'"
    Invoke-CheckedCommand -Area 'existing smoke' -Check ([string]$smoke.Label) -Command $smokeCommand
  }
}

if ($IncludeBrowserChecklist) {
  Add-BrowserChecklist
}
if ($IncludeWeChatChecklist) {
  Add-WeChatChecklist
}
Add-DefaultManualPlaceholders

$outcome = Write-Report
Write-Host "Final outcome: $outcome"

if ($outcome -eq 'FAIL') { exit 1 }
if ($outcome -eq 'BLOCKED' -and -not $Quick -and -not $StaticOnly) { exit 1 }
exit 0
