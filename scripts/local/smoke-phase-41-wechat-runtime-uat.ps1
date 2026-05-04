param(
  [switch]$Quick,
  [switch]$IncludeBuild,
  [switch]$OpenDevTools,
  [switch]$RequireDevTools,
  [string]$ReportPath = ''
)

$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$PhaseDir = '.planning/phases/41-wechat-runtime-uat-harness-and-story-entry-hardening'
if ([string]::IsNullOrWhiteSpace($ReportPath)) {
  $ReportPath = Join-Path $ProjectRoot "$PhaseDir/41-UAT.md"
}

$BaseUrl = [Environment]::GetEnvironmentVariable('PHASE41_PUBLIC_BASE_URL')
if ([string]::IsNullOrWhiteSpace($BaseUrl)) {
  $BaseUrl = 'http://127.0.0.1:8080/api/v1'
}
$BaseUrl = $BaseUrl.TrimEnd('/')

$HealthUrl = [Environment]::GetEnvironmentVariable('PHASE41_HEALTH_URL')
if ([string]::IsNullOrWhiteSpace($HealthUrl)) {
  $HealthUrl = 'http://127.0.0.1:8080/actuator/health'
}

$Locale = [Environment]::GetEnvironmentVariable('PHASE41_LOCALE')
if ([string]::IsNullOrWhiteSpace($Locale)) {
  $Locale = 'zh-Hant'
}
$EscapedLocale = [Uri]::EscapeDataString($Locale)

$StorylineCode = [Environment]::GetEnvironmentVariable('PHASE41_STORYLINE_CODE')
if ([string]::IsNullOrWhiteSpace($StorylineCode)) {
  $StorylineCode = 'east_west_war_and_coexistence'
}

$DevToolsCli = [Environment]::GetEnvironmentVariable('WECHAT_DEVTOOLS_CLI')
if ([string]::IsNullOrWhiteSpace($DevToolsCli)) {
  $devToolsDirName = [string]::Concat(
    [char]0x5FAE,
    [char]0x4FE1,
    'web',
    [char]0x5F00,
    [char]0x53D1,
    [char]0x8005,
    [char]0x5DE5,
    [char]0x5177
  )
  $DevToolsCli = Join-Path (Join-Path 'D:/Software' $devToolsDirName) 'cli.bat'
}

$FlagshipNameNeedle = [string]::Concat(
  [char]0x6771,
  [char]0x897F,
  [char]0x65B9,
  [char]0x6587,
  [char]0x660E
)

$Results = New-Object System.Collections.Generic.List[object]

function ConvertTo-CompactJson {
  param($Value)
  if ($null -eq $Value) { return '' }
  return ($Value | ConvertTo-Json -Depth 100 -Compress)
}

function Redact-SecretText {
  param([AllowNull()][string]$Text)
  if ([string]::IsNullOrEmpty($Text)) { return '' }
  $value = $Text
  $value = $value -replace '(?i)sk-[A-Za-z0-9_\-]{8,}', '[redacted-api-key]'
  $value = $value -replace '(?i)Bearer\s+[A-Za-z0-9._~+/=-]+', 'Bearer [redacted-token]'
  $value = $value -replace '(?i)(COS_SECRET|SECRET_ID|SECRET_KEY|providerApiKey|apiKey|authorization|promptText|scriptText|localPath)', '[redacted-field]'
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
    'COS_SECRET',
    'SECRET_ID',
    'SECRET_KEY',
    'providerApiKey',
    'apiKey',
    'authorization',
    'promptText',
    'scriptText',
    'localPath'
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
  if ([string]::IsNullOrWhiteSpace($message)) {
    $message = [string]$ErrorRecord
  }
  $message = Redact-SecretText -Text $message
  if ($message.Length -gt 220) {
    $message = $message.Substring(0, 220) + '...'
  }
  return $message
}

function Invoke-JsonRequest {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url
  )
  return Invoke-RestMethod -Method $Method -Uri $Url -Headers @{ Accept = 'application/json' } -TimeoutSec 45 -ErrorAction Stop
}

function Ensure-Success {
  param(
    [Parameter(Mandatory = $true)]$Response,
    [Parameter(Mandatory = $true)][string]$Context
  )
  if ($null -eq $Response) {
    throw "$Context returned an empty response"
  }
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

function Find-FlagshipStoryline {
  param($Storylines)
  $items = Get-Array $Storylines
  $byCode = @($items | Where-Object { [string]$_.code -eq $StorylineCode } | Select-Object -First 1)
  if ($byCode.Count -gt 0) {
    return $byCode[0]
  }
  $byName = @($items | Where-Object { ([string]$_.name).Contains($FlagshipNameNeedle) } | Select-Object -First 1)
  if ($byName.Count -gt 0) {
    return $byName[0]
  }
  return $null
}

function Collect-RuntimeMediaAssets {
  param($Runtime)
  $assets = @()
  if ($Runtime.storyline) {
    foreach ($asset in (Get-Array $Runtime.storyline.attachmentAssets)) {
      if ($asset) { $assets += $asset }
    }
  }
  foreach ($chapterRuntime in (Get-Array $Runtime.chapters)) {
    if ($chapterRuntime.chapter.primaryMediaAsset) {
      $assets += $chapterRuntime.chapter.primaryMediaAsset
    }
    foreach ($asset in (Get-Array $chapterRuntime.chapter.attachmentAssets)) {
      if ($asset) { $assets += $asset }
    }
    foreach ($block in (Get-Array $chapterRuntime.chapter.contentBlocks)) {
      if ($block.primaryAsset) {
        $assets += $block.primaryAsset
      }
      foreach ($asset in (Get-Array $block.attachmentAssets)) {
        if ($asset) { $assets += $asset }
      }
    }
    foreach ($step in (Get-Array $chapterRuntime.compiledSteps)) {
      if ($step.mediaAsset) {
        $assets += $step.mediaAsset
      }
    }
  }
  return $assets
}

function Count-RuntimeContentBlocks {
  param($Runtime)
  $count = 0
  foreach ($chapterRuntime in (Get-Array $Runtime.chapters)) {
    $count += (Get-Array $chapterRuntime.chapter.contentBlocks).Count
  }
  return $count
}

function Count-RuntimeSteps {
  param($Runtime)
  $count = 0
  foreach ($chapterRuntime in (Get-Array $Runtime.chapters)) {
    $count += (Get-Array $chapterRuntime.compiledSteps).Count
  }
  return $count
}

function Get-MediaSummary {
  param($Assets)
  $assetsArray = Get-Array $Assets
  if ($assetsArray.Count -eq 0) {
    return 'assets=0'
  }

  $kindRows = @($assetsArray | Group-Object { if ([string]::IsNullOrWhiteSpace([string]$_.assetKind)) { 'missing' } else { [string]$_.assetKind } } | Sort-Object Name)
  $availabilityRows = @($assetsArray | Group-Object { if ([string]::IsNullOrWhiteSpace([string]$_.availability)) { 'missing' } else { [string]$_.availability } } | Sort-Object Name)
  $kindSummary = ($kindRows | ForEach-Object { "$($_.Name)=$($_.Count)" }) -join ', '
  $availabilitySummary = ($availabilityRows | ForEach-Object { "$($_.Name)=$($_.Count)" }) -join ', '
  return "assets=$($assetsArray.Count); kinds=[$kindSummary]; availability=[$availabilitySummary]"
}

function Test-RequiredBuildArtifacts {
  $dist = Join-Path $ProjectRoot 'packages/client/dist'
  $required = @(
    'app.js',
    'app.json',
    'pages/story/index.js',
    'project.config.json'
  )
  $missing = @()
  foreach ($relative in $required) {
    if (-not (Test-Path -LiteralPath (Join-Path $dist $relative))) {
      $missing += $relative
    }
  }
  return $missing
}

function Test-PublicBackend {
  try {
    $health = Invoke-JsonRequest -Method GET -Url $HealthUrl
    $status = if ($health.PSObject.Properties.Name -contains 'status') { [string]$health.status } else { 'available' }
    if ([string]::IsNullOrWhiteSpace($status)) { $status = 'available' }
    Add-Result -Area 'public backend' -Check 'health' -Status 'PASS' -Evidence "health=$status" -Command "Invoke-RestMethod $HealthUrl"
    return $true
  } catch {
    Add-Result -Area 'public backend' -Check 'health' -Status 'BLOCKED' -Evidence "health unavailable: $(Get-SafeErrorMessage $_)" -Command "Invoke-RestMethod $HealthUrl"
    return $false
  }
}

function Test-StoryRuntime {
  try {
    $storylines = Ensure-Success -Context 'public storylines list' -Response (
      Invoke-JsonRequest -Method GET -Url "$BaseUrl/story-lines?locale=$EscapedLocale"
    )
    $storyline = Find-FlagshipStoryline -Storylines $storylines
    if ($null -eq $storyline -or [string]::IsNullOrWhiteSpace([string]$storyline.id)) {
      throw "flagship storyline not found by code=$StorylineCode"
    }

    $storyId = [string]$storyline.id
    $runtime = Ensure-Success -Context 'public storyline runtime' -Response (
      Invoke-JsonRequest -Method GET -Url "$BaseUrl/storylines/$storyId/runtime?locale=$EscapedLocale"
    )
    $chapters = Get-Array $runtime.chapters
    $stepCount = Count-RuntimeSteps -Runtime $runtime
    $blockCount = Count-RuntimeContentBlocks -Runtime $runtime

    if ($chapters.Count -lt 5) {
      throw "expected at least 5 chapters; got $($chapters.Count)"
    }
    if ($stepCount -lt 1) {
      throw 'expected at least one compiled step'
    }
    if ($blockCount -lt 1) {
      throw 'expected at least one content block'
    }

    Add-Result -Area 'story runtime' -Check 'flagship runtime' -Status 'PASS' -Evidence "id=$storyId; code=$StorylineCode; chapters=$($chapters.Count); compiledSteps=$stepCount; contentBlocks=$blockCount" -Command "GET /storylines/$storyId/runtime?locale=$Locale"

    $mediaAssets = @(Collect-RuntimeMediaAssets -Runtime $runtime)
    Add-Result -Area 'media assets' -Check 'runtime media metadata' -Status 'PASS' -Evidence (Get-MediaSummary -Assets $mediaAssets) -Command 'collect runtime story, block, and step assets'
    return $true
  } catch {
    Add-Result -Area 'story runtime' -Check 'flagship runtime' -Status 'BLOCKED' -Evidence "runtime unavailable: $(Get-SafeErrorMessage $_)" -Command "GET /story-lines and /storylines/{id}/runtime"
    Add-Result -Area 'media assets' -Check 'runtime media metadata' -Status 'BLOCKED' -Evidence 'runtime media unavailable because flagship runtime did not load.' -Command 'collect runtime story, block, and step assets'
    return $false
  }
}

function Test-ClientBuild {
  if (-not $IncludeBuild) {
    Add-Result -Area 'client build' -Check 'mini-program build artifacts' -Status 'SKIP' -Evidence 'run with -IncludeBuild to execute npm run build:weapp.' -Command 'npm run build:weapp'
    return $true
  }

  $clientDir = Join-Path $ProjectRoot 'packages/client'
  Push-Location $clientDir
  try {
    & cmd.exe /c 'npm run build:weapp'
    $exitCode = $LASTEXITCODE
  } finally {
    Pop-Location
  }
  if ($exitCode -ne 0) {
    Add-Result -Area 'client build' -Check 'mini-program build artifacts' -Status 'FAIL' -Evidence "build exited $exitCode." -Command 'npm run build:weapp'
    return $false
  }

  $missing = @(Test-RequiredBuildArtifacts)
  if ($missing.Count -gt 0) {
    Add-Result -Area 'client build' -Check 'mini-program build artifacts' -Status 'FAIL' -Evidence ('missing: ' + ($missing -join ', ')) -Command 'npm run build:weapp'
    return $false
  }

  Add-Result -Area 'client build' -Check 'mini-program build artifacts' -Status 'PASS' -Evidence 'dist/app.js, dist/app.json, dist/pages/story/index.js, and dist/project.config.json exist.' -Command 'npm run build:weapp'
  return $true
}

function Test-WeChatDevTools {
  $dist = Join-Path $ProjectRoot 'packages/client/dist'
  $devToolsCommandLabel = 'wechat-devtools-cli open --project <client-dist> --lang zh'
  if (-not $OpenDevTools) {
    Add-Result -Area 'wechat devtools' -Check 'open dist project' -Status 'SKIP' -Evidence 'run with -OpenDevTools to launch WeChat DevTools.' -Command $devToolsCommandLabel
    return $true
  }

  if (-not (Test-Path -LiteralPath $DevToolsCli)) {
    Add-Result -Area 'wechat devtools' -Check 'open dist project' -Status 'BLOCKED' -Evidence 'WeChat DevTools CLI not found; set WECHAT_DEVTOOLS_CLI.' -Command $devToolsCommandLabel
    return (-not $RequireDevTools)
  }
  if (-not (Test-Path -LiteralPath $dist)) {
    Add-Result -Area 'wechat devtools' -Check 'open dist project' -Status 'BLOCKED' -Evidence 'client dist folder is missing; run with -IncludeBuild first.' -Command $devToolsCommandLabel
    return (-not $RequireDevTools)
  }

  $startInfo = New-Object System.Diagnostics.ProcessStartInfo
  $startInfo.FileName = $DevToolsCli
  $startInfo.Arguments = "open --project `"$dist`" --lang zh"
  $startInfo.WorkingDirectory = $ProjectRoot
  $startInfo.RedirectStandardOutput = $true
  $startInfo.RedirectStandardError = $true
  $startInfo.UseShellExecute = $false
  $startInfo.StandardOutputEncoding = [System.Text.Encoding]::UTF8
  $startInfo.StandardErrorEncoding = [System.Text.Encoding]::UTF8

  $process = New-Object System.Diagnostics.Process
  $process.StartInfo = $startInfo
  [void]$process.Start()
  $stdout = $process.StandardOutput.ReadToEnd()
  $stderr = $process.StandardError.ReadToEnd()
  $process.WaitForExit()

  if ($process.ExitCode -eq 0) {
    Add-Result -Area 'wechat devtools' -Check 'open dist project' -Status 'PASS' -Evidence 'WeChat DevTools CLI exited 0.' -Command $devToolsCommandLabel
    return $true
  }

  $combined = Redact-SecretText -Text "$stdout`n$stderr"
  $lastLine = (($combined -split "`r?`n") | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -Last 1)
  if ([string]::IsNullOrWhiteSpace($lastLine)) {
    $lastLine = "exit code $($process.ExitCode)"
  }
  if ($lastLine.Length -gt 180) {
    $lastLine = $lastLine.Substring(0, 180) + '...'
  }
  Add-Result -Area 'wechat devtools' -Check 'open dist project' -Status 'BLOCKED' -Evidence "CLI exited $($process.ExitCode): $lastLine" -Command $devToolsCommandLabel
  return (-not $RequireDevTools)
}

function Write-Report {
  $reportFullPath = $ReportPath
  if (-not [System.IO.Path]::IsPathRooted($reportFullPath)) {
    $reportFullPath = Join-Path $ProjectRoot $reportFullPath
  }
  $dir = Split-Path -Parent $reportFullPath
  if (-not (Test-Path -LiteralPath $dir)) {
    New-Item -ItemType Directory -Path $dir | Out-Null
  }

  $mode = @(
    "Quick=$([bool]$Quick)",
    "IncludeBuild=$([bool]$IncludeBuild)",
    "OpenDevTools=$([bool]$OpenDevTools)",
    "RequireDevTools=$([bool]$RequireDevTools)"
  ) -join '; '

  $lines = New-Object System.Collections.Generic.List[string]
  $lines.Add('# Phase 41 WeChat Runtime UAT Report')
  $lines.Add('')
  $lines.Add("Generated: $(Get-Date -Format s)")
  $lines.Add("Mode: $mode")
  $lines.Add("Locale: $Locale")
  $lines.Add("Storyline code: $StorylineCode")
  $lines.Add('')
  $lines.Add('| Area | Check | Status | Evidence | Command |')
  $lines.Add('| --- | --- | --- | --- | --- |')
  foreach ($result in $Results) {
    $lines.Add("| $(Escape-MarkdownCell $result.Area) | $(Escape-MarkdownCell $result.Check) | $(Escape-MarkdownCell $result.Status) | $(Escape-MarkdownCell $result.Evidence) | $(Escape-MarkdownCell $result.Command) |")
  }
  $lines.Add('')
  $failed = @($Results | Where-Object { $_.Status -eq 'FAIL' })
  $blocked = @($Results | Where-Object { $_.Status -eq 'BLOCKED' })
  if ($failed.Count -gt 0) {
    $lines.Add('Final outcome: FAIL')
  } elseif ($blocked.Count -gt 0) {
    $lines.Add('Final outcome: BLOCKED')
  } else {
    $lines.Add('Final outcome: PASS_WITH_SKIPS_ALLOWED')
  }

  $content = ($lines -join "`n") + "`n"
  Assert-NoSecretText -Text $content -Context 'Phase 41 UAT report'
  Set-Content -LiteralPath $reportFullPath -Value $content -Encoding UTF8
}

$backendOk = Test-PublicBackend
$runtimeOk = Test-StoryRuntime
$buildOk = Test-ClientBuild
$devToolsOk = Test-WeChatDevTools

Write-Report

if (-not $backendOk -or -not $runtimeOk -or -not $buildOk -or -not $devToolsOk) {
  Write-Host 'Phase 41 WeChat runtime UAT completed with blocking results. See 41-UAT.md for safe details.'
  exit 1
}

Write-Host 'Phase 41 WeChat runtime UAT completed.'
