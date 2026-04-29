param(
  [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
  [string]$Username = 'admin',
  [string]$Password = 'admin123',
  [switch]$EmitJson,
  [switch]$PassThru
)

$ErrorActionPreference = 'Stop'
$statusEvents = New-Object 'System.Collections.Generic.List[object]'
$requiredLabels = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)

function Invoke-ApiEnvelope {
  param(
    [Parameter(Mandatory = $true)][string]$Method,
    [Parameter(Mandatory = $true)][string]$Url,
    [string]$Token,
    $Body
  )

  $headers = @{}
  if ($Token) {
    $headers.Authorization = "Bearer $Token"
  }

  $params = @{
    Method = $Method
    Uri = $Url
    Headers = $headers
    ContentType = 'application/json; charset=utf-8'
  }
  if ($null -ne $Body) {
    $params.Body = ($Body | ConvertTo-Json -Depth 20)
  }

  return Invoke-RestMethod @params
}

function Ensure-ApiSuccess {
  param(
    [Parameter(Mandatory = $true)]$Envelope,
    [Parameter(Mandatory = $true)][string]$Context
  )

  if ($Envelope.code -ne 0 -and $Envelope.code -ne 200) {
    throw "$Context failed: $($Envelope.message)"
  }
  return $Envelope.data
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

function Emit-Status {
  param(
    [Parameter(Mandatory = $true)][string]$Label,
    [Parameter(Mandatory = $true)][string]$Name,
    [Parameter(Mandatory = $true)][string]$Detail
  )

  $event = [pscustomobject]@{
    label = $Label
    name = $Name
    detail = $Detail
  }
  [void]$requiredLabels.Add($Label)
  [void]$statusEvents.Add($event)
  if (-not $EmitJson) {
    Write-Host "[$Label] $Name - $Detail"
  }
}

function Import-Statuses {
  param([Parameter(Mandatory = $true)]$Statuses)

  foreach ($status in @($Statuses)) {
    [void]$requiredLabels.Add($status.label)
    [void]$statusEvents.Add([pscustomobject]@{
      label = $status.label
      name = $status.name
      detail = $status.detail
    })
  }
}

$phase22Script = Join-Path $PSScriptRoot 'smoke-phase-22-ai-platform-verification.ps1'
$phase22 = & $phase22Script -AdminBaseUrl $AdminBaseUrl -Username $Username -Password $Password -Scope all -PassThru
Import-Statuses -Statuses $phase22.statuses

$login = Ensure-ApiSuccess -Context 'admin login' -Envelope (
  Invoke-ApiEnvelope -Method POST -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
    username = $Username
    password = $Password
  }
)
$token = $login.token
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($token)) -Message 'admin login token is missing'

$providers = Ensure-ApiSuccess -Context 'provider list' -Envelope (
  Invoke-ApiEnvelope -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/providers" -Token $token
)
$ttsProvider = @($providers | Where-Object { $_.providerName -eq 'dashscope-tts' }) | Select-Object -First 1
Assert-True -Condition ($null -ne $ttsProvider) -Message 'missing dashscope-tts provider row'

$syncedVoices = Ensure-ApiSuccess -Context 'voice sync' -Envelope (
  Invoke-ApiEnvelope -Method POST -Url "$AdminBaseUrl/api/admin/v1/ai/providers/$($ttsProvider.id)/sync-voices" -Token $token -Body @{
    modelCode = $ttsProvider.modelName
  }
)
$syncedVoiceList = @($syncedVoices)
Emit-Status -Label 'LIVE_VERIFIED' -Name 'voice-workbench:sync' -Detail "voice sync returned $($syncedVoiceList.Count) row(s) for dashscope-tts."

$voices = Ensure-ApiSuccess -Context 'voice catalog' -Envelope (
  Invoke-ApiEnvelope -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/voices?providerId=$($ttsProvider.id)&sourceType=system_catalog" -Token $token
)
$voiceList = @($voices)
Assert-True -Condition ($voiceList.Count -gt 0) -Message 'voice catalog returned no rows for dashscope-tts'
Emit-Status -Label 'LIVE_VERIFIED' -Name 'voice-workbench:catalog' -Detail "system_catalog returned $($voiceList.Count) voice row(s) for dashscope-tts."

$selectedVoice = $voiceList | Where-Object { $_.availabilityStatus -eq 'available' } | Select-Object -First 1
if ($null -eq $selectedVoice) {
  $selectedVoice = $voiceList | Select-Object -First 1
}
Assert-True -Condition ($null -ne $selectedVoice) -Message 'unable to select a voice from the catalog'

$languageCode = @('yue', 'zh', 'en', 'pt') | Where-Object { @($selectedVoice.languageCodes) -contains $_ } | Select-Object -First 1
if ([string]::IsNullOrWhiteSpace($languageCode)) {
  $languageCode = @($selectedVoice.languageCodes) | Select-Object -First 1
}
if ([string]::IsNullOrWhiteSpace($languageCode)) {
  $languageCode = 'zh'
}

$preview = Ensure-ApiSuccess -Context 'voice preview' -Envelope (
  Invoke-ApiEnvelope -Method POST -Url "$AdminBaseUrl/api/admin/v1/ai/voices/preview" -Token $token -Body @{
    providerId = $selectedVoice.providerId
    modelCode = $selectedVoice.parentModelCode
    voiceCode = $selectedVoice.voiceCode
    languageCode = $languageCode
    scriptText = 'Welcome to the Macau story walk. This is a live voice workbench preview.'
  }
)
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($preview.previewUrl)) -Message 'voice preview did not return previewUrl'
Emit-Status -Label 'LIVE_VERIFIED' -Name 'voice-workbench:preview' -Detail "voice preview succeeded for $($selectedVoice.voiceCode) ($languageCode)."

$ttsJobId = $null
if ($phase22.witnessJobs -and $phase22.witnessJobs.admin_tts_generation) {
  $ttsJobId = $phase22.witnessJobs.admin_tts_generation.jobId
}

$jobsPage = Ensure-ApiSuccess -Context 'generation jobs page' -Envelope (
  Invoke-ApiEnvelope -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/generation-jobs?pageNum=1&pageSize=10&generationType=audio" -Token $token
)
$jobList = @($jobsPage.list)
Assert-True -Condition ($jobList.Count -gt 0) -Message 'audio generation jobs page returned no rows'
Emit-Status -Label 'LIVE_VERIFIED' -Name 'voice-workbench:job-list' -Detail "generation jobs page returned $($jobList.Count) audio row(s)."

if (-not $ttsJobId) {
  $ttsJobId = ($jobList | Select-Object -First 1).id
}
Assert-True -Condition ($null -ne $ttsJobId) -Message 'missing tts job id for generation job detail lookup'

$jobDetail = Ensure-ApiSuccess -Context 'generation job detail' -Envelope (
  Invoke-ApiEnvelope -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/generation-jobs/$ttsJobId" -Token $token
)
Assert-True -Condition ($jobDetail.id -eq $ttsJobId) -Message 'generation job detail returned wrong job id'
Assert-True -Condition ($jobDetail.jobStatus -in @('completed', 'failed')) -Message 'generation job detail did not resolve to a terminal state'
Emit-Status -Label 'LIVE_VERIFIED' -Name 'voice-workbench:job-detail' -Detail "generation job #$ttsJobId resolved via detail endpoint with status $($jobDetail.jobStatus)."

$logsPage = Ensure-ApiSuccess -Context 'ai logs page' -Envelope (
  Invoke-ApiEnvelope -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/logs?pageNum=1&pageSize=10&capabilityCode=admin_tts_generation" -Token $token
)
$logList = @($logsPage.list)
Assert-True -Condition ($logList.Count -gt 0) -Message 'ai logs page returned no admin_tts_generation rows'
Emit-Status -Label 'LIVE_VERIFIED' -Name 'voice-workbench:logs' -Detail "ai logs page returned $($logList.Count) admin_tts_generation row(s)."

foreach ($requiredLabel in @('LIVE_VERIFIED', 'TEMPLATE_ONLY', 'CREDENTIAL_MISSING')) {
  Assert-True -Condition ($requiredLabels.Contains($requiredLabel)) -Message "required output label missing: $requiredLabel"
}

$result = [pscustomobject]@{
  adminBaseUrl = $AdminBaseUrl
  emittedAt = [DateTimeOffset]::Now.ToString('o')
  phase22 = $phase22
  voiceCatalogCount = $voiceList.Count
  selectedVoice = [pscustomobject]@{
    providerId = $selectedVoice.providerId
    modelCode = $selectedVoice.parentModelCode
    voiceCode = $selectedVoice.voiceCode
    languageCode = $languageCode
    sourceType = $selectedVoice.sourceType
  }
  voicePreview = [pscustomobject]@{
    previewUrl = $preview.previewUrl
    mimeType = $preview.mimeType
    fileSizeBytes = $preview.fileSizeBytes
  }
  generationJobListCount = $jobList.Count
  generationJobDetail = [pscustomobject]@{
    jobId = $jobDetail.id
    jobStatus = $jobDetail.jobStatus
    latestCandidateId = $jobDetail.latestCandidateId
    finalizedCandidateId = $jobDetail.finalizedCandidateId
    candidateCount = @($jobDetail.candidates).Count
  }
  logCount = $logList.Count
  statuses = $statusEvents.ToArray()
}

if ($EmitJson) {
  $result | ConvertTo-Json -Depth 20
} elseif ($PassThru) {
  Write-Host 'Phase 26 AI platform closure smoke passed.'
  $result
} else {
  Write-Host 'Phase 26 AI platform closure smoke passed.'
}
