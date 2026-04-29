param(
  [string]$AdminBaseUrl = 'http://127.0.0.1:8081',
  [string]$Username = 'admin',
  [string]$Password = 'admin123',
  [string]$ChatProviderCode = 'dashscope-chat',
  [string]$ImageProviderCode = 'dashscope-image',
  [string]$CapabilityCode = 'travel_qa'
)

$ErrorActionPreference = 'Stop'

function Invoke-Api {
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

function Ensure-Success {
  param(
    [Parameter(Mandatory = $true)]$Response,
    [Parameter(Mandatory = $true)][string]$Context
  )

  if ($Response.code -ne 0 -and $Response.code -ne 200) {
    throw "$Context failed: $($Response.message)"
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

$login = Ensure-Success -Context 'admin login' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/auth/login" -Body @{
    username = $Username
    password = $Password
  }
)

$token = $login.token
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($token)) -Message 'admin login token is missing'

$providers = Ensure-Success -Context 'provider list' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/providers" -Token $token
)

$chatProvider = $providers | Where-Object { $_.providerName -eq $ChatProviderCode } | Select-Object -First 1
$imageProvider = $providers | Where-Object { $_.providerName -eq $ImageProviderCode } | Select-Object -First 1

Assert-True -Condition ($null -ne $chatProvider) -Message "missing chat provider: $ChatProviderCode"
Assert-True -Condition ($null -ne $imageProvider) -Message "missing image provider: $ImageProviderCode"
Assert-True -Condition ($chatProvider.modelName -eq 'qwen3.5-flash') -Message 'unexpected default chat model'
Assert-True -Condition (($chatProvider.inventoryRecordCount -as [int]) -ge 1) -Message 'chat provider inventory count is empty'

$providerTest = Ensure-Success -Context 'chat provider test' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/ai/providers/$($chatProvider.id)/test" -Token $token -Body @{
    capabilityCode = $CapabilityCode
    prompt = 'Answer in one sentence: what makes Macau suitable for story-driven city exploration?'
  }
)

Assert-True -Condition ($providerTest.success -eq 1) -Message 'provider connectivity test did not succeed'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($providerTest.preview)) -Message 'provider test preview is empty'
Assert-True -Condition (-not $providerTest.preview.Contains('reasoning_content')) -Message 'provider test preview still contains reasoning payload'

$sync = Ensure-Success -Context 'image inventory sync' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/ai/providers/$($imageProvider.id)/sync-inventory" -Token $token
)
$syncMessage = ''
if ($null -ne $sync.message) {
  $syncMessage = [string]$sync.message
}
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($syncMessage)) -Message 'sync result message is empty'

$job = Ensure-Success -Context 'create text generation job' -Response (
  Invoke-Api -Method POST -Url "$AdminBaseUrl/api/admin/v1/ai/generation-jobs" -Token $token -Body @{
    capabilityCode = $CapabilityCode
    generationType = 'text'
    promptTitle = 'Phase 19 smoke'
    promptText = 'Answer in one sentence: what makes Macau suitable for story-driven city exploration?'
  }
)

$jobDetail = Ensure-Success -Context 'generation job detail' -Response (
  Invoke-Api -Method GET -Url "$AdminBaseUrl/api/admin/v1/ai/generation-jobs/$($job.id)" -Token $token
)

$candidate = $jobDetail.candidates | Select-Object -First 1
Assert-True -Condition ($jobDetail.jobStatus -eq 'completed') -Message 'generation job did not complete'
Assert-True -Condition ($null -ne $candidate) -Message 'generation job has no candidate output'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($candidate.previewText)) -Message 'candidate preview text is empty'
Assert-True -Condition (-not $candidate.previewText.TrimStart().StartsWith('{')) -Message 'candidate preview still looks like raw JSON'
Assert-True -Condition (-not $candidate.previewText.Contains('reasoning_content')) -Message 'candidate preview still contains reasoning payload'
Assert-True -Condition (-not [string]::IsNullOrWhiteSpace($candidate.transcriptText)) -Message 'candidate transcript text is empty'

Write-Host 'Phase 19 AI platform smoke passed.'
