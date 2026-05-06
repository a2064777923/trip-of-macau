param(
  [string]$UatPath = '',
  [string]$VerificationPath = '',
  [string]$StatePath = ''
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$ProjectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..\..')).Path
$PhaseDir = '.planning/phases/44-management-system-ia-polish-and-release-acceptance'

if ([string]::IsNullOrWhiteSpace($UatPath)) {
  $UatPath = Join-Path $ProjectRoot "$PhaseDir/44-UAT.md"
}
if ([string]::IsNullOrWhiteSpace($VerificationPath)) {
  $VerificationPath = Join-Path $ProjectRoot "$PhaseDir/44-VERIFICATION.md"
}
if ([string]::IsNullOrWhiteSpace($StatePath)) {
  $StatePath = Join-Path $ProjectRoot '.planning/STATE.md'
}

function Read-Utf8Text {
  param([Parameter(Mandatory = $true)][string]$Path)
  if (-not (Test-Path -LiteralPath $Path)) {
    throw "missing file: $Path"
  }
  return Get-Content -LiteralPath $Path -Encoding UTF8 -Raw
}

function Assert-ContainsText {
  param(
    [Parameter(Mandatory = $true)][string]$Content,
    [Parameter(Mandatory = $true)][string]$Needle,
    [Parameter(Mandatory = $true)][string]$Label
  )
  if (-not $Content.Contains($Needle)) {
    Write-Error "missing required release doc text: $Label"
  }
}

$uat = Read-Utf8Text -Path $UatPath
$verification = Read-Utf8Text -Path $VerificationPath
$state = Read-Utf8Text -Path $StatePath

foreach ($section in @(
  '## Automated Smoke',
  '## Browser / Admin Checks',
  '## WeChat DevTools Checks',
  '## Physical Device Checks',
  '## Accepted Caveats',
  '## Future Gameplay Scope'
)) {
  Assert-ContainsText -Content $uat -Needle $section -Label "44-UAT.md::$section"
}

foreach ($text in @(
  'UAT-02',
  'UAT-03',
  'ADMIN-01',
  'ADMIN-02',
  'ADMIN-03',
  'ADMIN-04',
  'Browser / Admin Checks',
  'WeChat DevTools / Device Checks',
  'Release Outcome Rules',
  'Accepted Caveats',
  'Future Gameplay Scope'
)) {
  Assert-ContainsText -Content $verification -Needle $text -Label "44-VERIFICATION.md::$text"
}

Assert-ContainsText -Content $verification -Needle 'Final PASS 需要自動化檢查通過、瀏覽器管理端檢查通過，且 DevTools 或實機至少一項旗艦故事煙測有 PASS 證據。' -Label '44-VERIFICATION.md::final-pass-rule'
Assert-ContainsText -Content $verification -Needle '未執行的 WeChat DevTools 或實機檢查不標記為 PASS。' -Label '44-VERIFICATION.md::wechat-caveat'
Assert-ContainsText -Content $state -Needle 'milestone: v3.2' -Label 'STATE.md::milestone'

Write-Host 'phase 44 release docs checks passed'
