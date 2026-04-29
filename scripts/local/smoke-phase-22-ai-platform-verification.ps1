param(
  [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
  [string]$Username = 'admin',
  [string]$Password = 'admin123',
  [ValidateSet('all', 'workspace', 'creative')][string]$Scope = 'all',
  [string]$BackendLogPath = 'D:\Archive\trip-of-macau\packages\admin\aoxiaoyou-admin-backend\admin-backend-8081.out.log',
  [switch]$EmitJson,
  [switch]$PassThru
)

$ErrorActionPreference = 'Stop'
$requiredLabels = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
$statusEvents = New-Object 'System.Collections.Generic.List[object]'

$result = [ordered]@{
  adminBaseUrl = $AdminBaseUrl
  scope = $Scope
  emittedAt = [DateTimeOffset]::Now.ToString('o')
  templateCount = 0
  providerCount = 0
  overviewSummaryPresent = $false
  witnessDefaults = [ordered]@{
    travel_qa = [ordered]@{
      providerCode = 'dashscope-chat'
      modelCode = 'qwen3.5-flash'
    }
    admin_image_generation = [ordered]@{
      providerCode = 'dashscope-image'
      modelCode = 'wan2.6-image'
    }
    admin_tts_generation = [ordered]@{
      providerCode = 'dashscope-tts'
      modelCode = 'cosyvoice-v3-flash'
    }
  }
  witnessProviders = [ordered]@{}
  witnessJobs = [ordered]@{
    travel_qa = $null
    admin_image_generation = $null
    admin_tts_generation = $null
  }
  finalizedCandidateId = $null
  finalizedAssetId = $null
  statuses = @()
}

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
    $params.Body = ($Body | ConvertTo-Json -Depth 30)
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

function Read-BackendLogTail {
  param([int]$Lines = 240)

  if (-not (Test-Path $BackendLogPath)) {
    return ''
  }
  return ((Get-Content $BackendLogPath -Tail $Lines -ErrorAction SilentlyContinue) -join "`n")
}

function Resolve-BackendFailureLabel {
  param(
    [string]$LogTail,
    [string]$PromptTitle
  )

  if (-not $LogTail -or -not $PromptTitle) {
    return $null
  }

  $hasPrompt = $LogTail -match [regex]::Escape($PromptTitle)
  if (-not $hasPrompt) {
    return $null
  }
  if ($LogTail -match 'Model\.AccessDenied') {
    return 'ACCESS_DENIED'
  }
  if ($LogTail -match 'InvalidParameter' -or $LogTail -match 'Model not exist') {
    return 'BROKEN_DEFAULT'
  }
  if ($LogTail -match 'AI provider call failed') {
    return 'BACKEND_FAILED'
  }
  return $null
}

function Find-Provider {
  param(
    [Parameter(Mandatory = $true)]$Providers,
    [Parameter(Mandatory = $true)][string]$ProviderName
  )

  $provider = $Providers | Where-Object { $_.providerName -eq $ProviderName } | Select-Object -First 1
  Assert-True -Condition ($null -ne $provider) -Message "missing provider: $ProviderName"
  return $provider
}

function Classify-TemplateStates {
  param(
    [Parameter(Mandatory = $true)]$Templates,
    [Parameter(Mandatory = $true)]$Providers
  )

  foreach ($template in $Templates) {
    $platformProviders = @($Providers | Where-Object { $_.platformCode -eq $template.platformCode })
    if ($platformProviders.Count -eq 0) {
      Emit-Status -Label 'TEMPLATE_ONLY' -Name "template:$($template.platformCode)" -Detail 'Template exists but no provider row is configured locally.'
      continue
    }

    $credentialedProviders = @(
      $platformProviders | Where-Object { $_.hasApiKey -eq 1 -or $_.hasApiSecret -eq 1 }
    )
    if ($credentialedProviders.Count -eq 0) {
      Emit-Status -Label 'CREDENTIAL_MISSING' -Name "platform:$($template.platformCode)" -Detail 'Provider row exists locally but no usable credential is configured.'
    }
  }
}

function Set-WitnessProvider {
  param(
    [Parameter(Mandatory = $true)][string]$CapabilityCode,
    [Parameter(Mandatory = $true)]$Provider
  )

  $result.witnessProviders[$CapabilityCode] = [ordered]@{
    providerId = $Provider.id
    providerName = $Provider.providerName
    displayName = $Provider.displayName
    modelName = $Provider.modelName
    platformCode = $Provider.platformCode
    inventoryRecordCount = $Provider.inventoryRecordCount
  }
}

function Set-WitnessJob {
  param(
    [Parameter(Mandatory = $true)][string]$CapabilityCode,
    $Job
  )

  if ($null -eq $Job) {
    return
  }
  $result.witnessJobs[$CapabilityCode] = [ordered]@{
    jobId = $Job.id
    generationType = $Job.generationType
    jobStatus = $Job.jobStatus
    latestCandidateId = $Job.latestCandidateId
    finalizedCandidateId = $Job.finalizedCandidateId
    providerName = $Job.providerName
    inventoryCode = $Job.inventoryCode
  }
}

function Invoke-WitnessGeneration {
  param(
    [Parameter(Mandatory = $true)][string]$Token,
    [Parameter(Mandatory = $true)][string]$CapabilityCode,
    [Parameter(Mandatory = $true)][string]$GenerationType,
    [Parameter(Mandatory = $true)][string]$PromptTitle,
    [Parameter(Mandatory = $true)][string]$PromptText,
    [string]$ExpectedFailureLabel,
    [string]$ExpectedFailureName
  )

  $createEnvelope = Invoke-ApiEnvelope -Method POST -Url "$AdminBaseUrl/api/admin/v1/ai/generation-jobs" -Token $Token -Body @{
    capabilityCode = $CapabilityCode
    generationType = $GenerationType
    promptTitle = $PromptTitle
    promptText = $PromptText
  }

  if ($createEnvelope.code -ne 0 -and $createEnvelope.code -ne 200) {
    $failureLabel = Resolve-BackendFailureLabel -LogTail (Read-BackendLogTail) -PromptTitle $PromptTitle
    if ($failureLabel -and $ExpectedFailureLabel -and $failureLabel -eq $ExpectedFailureLabel) {
      Emit-Status -Label $failureLabel -Name $ExpectedFailureName -Detail $createEnvelope.message
      return $null
    }
    throw "generation job create failed for ${CapabilityCode}: $($createEnvelope.message)"
  }

  $job = $createEnvelope.data
  for ($attempt = 0; $attempt -lt 3; $attempt += 1) {
    if ($job.jobStatus -in @('completed', 'failed')) {
      break
    }
    Start-Sleep -Seconds 4
    $job = Ensure-ApiSuccess -Context "refresh generation job $($job.id)" -Envelope (
      Invoke-ApiEnvelope -Method POST -Url "$AdminBaseUrl/api/admin/v1/ai/generation-jobs/$($job.id)/refresh" -Token $Token -Body $null
    )
  }

  if ($job.jobStatus -eq 'failed') {
    $errorDetail = ''
    if ($job.errorMessage) {
      $errorDetail = [string]$job.errorMessage
    } elseif ($job.resultSummary) {
      $errorDetail = [string]$job.resultSummary
    }

    if ($ExpectedFailureLabel -and $errorDetail -match 'Model\.AccessDenied' -and $ExpectedFailureLabel -eq 'ACCESS_DENIED') {
      Emit-Status -Label 'ACCESS_DENIED' -Name $ExpectedFailureName -Detail $errorDetail
      return $job
    }
    if ($errorDetail -match 'InvalidParameter|Model not exist') {
      throw "broken witness default for ${CapabilityCode}: $errorDetail"
    }
    throw "generation job failed for ${CapabilityCode}: $errorDetail"
  }

  return $job
}

$login = Ensure-ApiSuccess -Context 'admin login' -Envelope (
  Invoke-ApiEnvelope -Method POST -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
    username = $Username
    password = $Password
  }
)

$token = $login.token
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($token)) -Message 'admin login token is missing'

$templates = Ensure-ApiSuccess -Context 'provider templates' -Envelope (
  Invoke-ApiEnvelope -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/provider-templates" -Token $token
)
$providers = Ensure-ApiSuccess -Context 'provider list' -Envelope (
  Invoke-ApiEnvelope -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/providers" -Token $token
)
$overview = Ensure-ApiSuccess -Context 'ai overview' -Envelope (
  Invoke-ApiEnvelope -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/overview" -Token $token
)

$result.templateCount = @($templates).Count
$result.providerCount = @($providers).Count
$result.overviewSummaryPresent = ($null -ne $overview.summary)

Assert-True -Condition (@($templates).Count -ge 6) -Message 'provider templates inventory is incomplete'
Assert-True -Condition (@($providers).Count -ge 3) -Message 'provider list is incomplete'
Assert-True -Condition ($null -ne $overview.summary) -Message 'ai overview summary is missing'

Classify-TemplateStates -Templates $templates -Providers $providers

$expectedWitness = @{
  'dashscope-chat'  = 'qwen3.5-flash'
  'dashscope-image' = 'wan2.6-image'
  'dashscope-tts'   = 'cosyvoice-v3-flash'
}

foreach ($providerName in $expectedWitness.Keys) {
  $provider = Find-Provider -Providers $providers -ProviderName $providerName
  Assert-True -Condition ($provider.modelName -eq $expectedWitness[$providerName]) -Message "unexpected default model for ${providerName}: $($provider.modelName)"
}

$chatProvider = Find-Provider -Providers $providers -ProviderName 'dashscope-chat'
$imageProvider = Find-Provider -Providers $providers -ProviderName 'dashscope-image'
$ttsProvider = Find-Provider -Providers $providers -ProviderName 'dashscope-tts'

Set-WitnessProvider -CapabilityCode 'travel_qa' -Provider $chatProvider
Set-WitnessProvider -CapabilityCode 'admin_image_generation' -Provider $imageProvider
Set-WitnessProvider -CapabilityCode 'admin_tts_generation' -Provider $ttsProvider

if ($Scope -in @('all', 'workspace')) {
  $chatTest = Ensure-ApiSuccess -Context 'chat provider test' -Envelope (
    Invoke-ApiEnvelope -Method POST -Url "$AdminBaseUrl/api/admin/v1/ai/providers/$($chatProvider.id)/test" -Token $token -Body @{
      capabilityCode = 'travel_qa'
      prompt = 'Reply in one Traditional Chinese sentence: Macau suits story-driven city exploration because layered history and mixed-cultural streets are densely connected.'
    }
  )
  Assert-True -Condition ($chatTest.success -eq 1) -Message 'chat witness provider test did not succeed'
  Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($chatTest.preview)) -Message 'chat witness provider preview is empty'
  Emit-Status -Label 'LIVE_VERIFIED' -Name 'witness:travel_qa' -Detail "$($chatProvider.providerName) / $($chatProvider.modelName) provider test passed."

  foreach ($provider in @($imageProvider, $ttsProvider)) {
    $sync = Ensure-ApiSuccess -Context "sync inventory for $($provider.providerName)" -Envelope (
      Invoke-ApiEnvelope -Method POST -Url "$AdminBaseUrl/api/admin/v1/ai/providers/$($provider.id)/sync-inventory" -Token $token
    )
    Assert-True -Condition (-not [string]::IsNullOrWhiteSpace([string]$sync.message)) -Message "sync inventory message missing for $($provider.providerName)"
  }
}

if ($Scope -in @('all', 'creative')) {
  $stamp = Get-Date -Format 'yyyyMMddHHmmss'
  $textJob = Invoke-WitnessGeneration -Token $token -CapabilityCode 'travel_qa' -GenerationType 'text' -PromptTitle "Phase22-text-$stamp" -PromptText 'Reply with exactly one Traditional Chinese sentence: Macau suits story-driven city exploration because layered history and mixed-cultural streets are densely connected.'
  Assert-True -Condition ($null -ne $textJob) -Message 'text witness job did not return a job'
  Assert-True -Condition ($textJob.jobStatus -eq 'completed') -Message 'text witness job did not complete'
  Set-WitnessJob -CapabilityCode 'travel_qa' -Job $textJob

  $textCandidate = $textJob.candidates | Select-Object -First 1
  Assert-True -Condition ($null -ne $textCandidate) -Message 'text witness job has no candidate'

  $finalized = Ensure-ApiSuccess -Context 'finalize text candidate' -Envelope (
    Invoke-ApiEnvelope -Method POST -Url "$AdminBaseUrl/api/admin/v1/ai/generation-candidates/$($textCandidate.id)/finalize" -Token $token -Body @{
      assetKind = 'document'
      localeCode = 'zh-Hant'
      status = 'published'
    }
  )
  $finalizedCandidate = $finalized.candidates | Where-Object { $_.id -eq $textCandidate.id } | Select-Object -First 1
  Assert-True -Condition ($finalized.finalizedCandidateId -eq $textCandidate.id) -Message 'finalized candidate id mismatch'
  Assert-True -Condition ($null -ne $finalizedCandidate -and $finalizedCandidate.finalizedAssetId) -Message 'finalized content asset id missing'
  $result.finalizedCandidateId = $finalized.finalizedCandidateId
  $result.finalizedAssetId = $finalizedCandidate.finalizedAssetId
  if ($result.witnessJobs.travel_qa) {
    $result.witnessJobs.travel_qa.finalizedCandidateId = $finalized.finalizedCandidateId
    $result.witnessJobs.travel_qa.finalizedAssetId = $finalizedCandidate.finalizedAssetId
  }
  Emit-Status -Label 'LIVE_VERIFIED' -Name 'creative:finalize' -Detail "travel_qa candidate finalized into canonical content asset #$($finalizedCandidate.finalizedAssetId)."

  if ($Scope -eq 'all') {
    $imageJob = Invoke-WitnessGeneration -Token $token -CapabilityCode 'admin_image_generation' -GenerationType 'image' -PromptTitle "Phase22-image-$stamp" -PromptText 'Generate a cinematic tourism poster for Macau city exploration with warm twilight lighting.' -ExpectedFailureLabel 'ACCESS_DENIED' -ExpectedFailureName 'witness:admin_image_generation'
    Set-WitnessJob -CapabilityCode 'admin_image_generation' -Job $imageJob
    if ($null -ne $imageJob -and $imageJob.jobStatus -eq 'completed') {
      Emit-Status -Label 'LIVE_VERIFIED' -Name 'witness:admin_image_generation' -Detail 'image witness job completed successfully.'
    }

    $ttsJob = Invoke-WitnessGeneration -Token $token -CapabilityCode 'admin_tts_generation' -GenerationType 'audio' -PromptTitle "Phase22-tts-$stamp" -PromptText 'Welcome to the Macau story walk. Your first stop is ready.' -ExpectedFailureLabel 'ACCESS_DENIED' -ExpectedFailureName 'witness:admin_tts_generation'
    Set-WitnessJob -CapabilityCode 'admin_tts_generation' -Job $ttsJob
    if ($null -ne $ttsJob -and $ttsJob.jobStatus -eq 'completed') {
      Emit-Status -Label 'LIVE_VERIFIED' -Name 'witness:admin_tts_generation' -Detail 'tts witness job completed successfully.'
    }
  }
}

foreach ($requiredLabel in @('LIVE_VERIFIED', 'TEMPLATE_ONLY', 'CREDENTIAL_MISSING')) {
  Assert-True -Condition ($requiredLabels.Contains($requiredLabel)) -Message "required output label missing: $requiredLabel"
}

$result['statuses'] = $statusEvents.ToArray()
$output = [pscustomobject]$result

if ($EmitJson) {
  $output | ConvertTo-Json -Depth 20
} elseif ($PassThru) {
  Write-Host 'Phase 22 AI platform verification smoke passed.'
  $output
} else {
  Write-Host 'Phase 22 AI platform verification smoke passed.'
}
