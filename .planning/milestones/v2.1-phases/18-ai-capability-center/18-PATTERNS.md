# Phase 18: AI Capability Center - Pattern Map

**Mapped:** 2026-04-17
**Scope source:** `18-CONTEXT.md`, `18-AI-SPEC.md`
**Files classified:** 14 inferred targets
**Analogs found:** 13 / 14
**Project context:** No `CLAUDE.md`; no project-local `.claude/skills` or `.agents/skills` directories.

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/AiCapabilityCenter.tsx` | component/page | request-response | `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/AiCapabilityCenter.tsx`, `packages/admin/aoxiaoyou-admin-ui/src/pages/SystemManagement/index.tsx`, `packages/admin/aoxiaoyou-admin-ui/src/pages/Dashboard/index.tsx`, `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorRuleCenter.tsx` | hybrid-baseline |
| `packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiProviderFormModal.tsx` | component | CRUD | `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/RewardManagement.tsx` | role-match |
| `packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiPolicyFormModal.tsx` | component | CRUD | `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/RewardManagement.tsx` | role-match |
| `packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiGenerationHistoryDrawer.tsx` | component | request-response | `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetDetailDrawer.tsx` | role-match |
| `packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiCapabilityWorkbenchDrawer.tsx` | component | request-response / file-I/O | `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleWorkbench.tsx`, `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPickerField.tsx` | partial |
| `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` | service | request-response | `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts`, `packages/admin/aoxiaoyou-admin-ui/src/utils/request.ts` | exact |
| `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` | model | transform | `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` | exact |
| `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java` | controller | request-response | `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminContentManagementController.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminSystemManagementController.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/DashboardController.java` | hybrid-baseline |
| `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminAiService.java` and `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java` | service | CRUD / aggregate / request-response | `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminContentManagementServiceImpl.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/DashboardServiceImpl.java` | hybrid |
| `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminAi*Request.java` | request DTO | transform | request DTO pattern used by `AdminContentManagementController.java` and `AdminSystemManagementController.java` | role-match |
| `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminAiOverviewResponse.java` and sibling response DTOs | response DTO | aggregate / transform | `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/DashboardStatsResponse.java`, existing `AdminAi*Response.java` | role-match |
| `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiProviderConfig.java`, `AiPolicy.java`, `AiRequestLog.java` | model | CRUD / append-only log | same files | exact |
| `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiGenerationJob.java`, `AiAssetCandidate.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/AiAssetCandidateService.java` | model/service | batch / file-I/O / event-driven | `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosAssetStorageService.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiRequestLog.java` | partial |
| `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/provider/DashScopeCapabilityProvider.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/config/AiSecretCryptoService.java` | service/config | outbound request-response / transform | `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/auth/WechatAuthService.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosProperties.java`, `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/CosConfig.java` | partial / no direct crypto analog |
| `scripts/local/mysql/init/01-init.sql` | config | batch | `scripts/local/mysql/init/01-init.sql` | exact |

## Pattern Assignments

### `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/AiCapabilityCenter.tsx`

**Primary baseline:** `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/AiCapabilityCenter.tsx`

**Fetch + filter + paged table pattern** (`AiCapabilityCenter.tsx` lines 83-114, 221-343):
```tsx
const [logFilters, setLogFilters] = useState<{ scenarioGroup?: string; success?: number; providerId?: number }>({});
const [logPagination, setLogPagination] = useState({ current: 1, pageSize: 8 });

const providers = useRequest(() => getAiProviders());
const policies = useRequest(() => getAiPolicies());
const logs = useRequest(
  () => getAiLogs({
    pageNum: logPagination.current,
    pageSize: logPagination.pageSize,
    ...logFilters,
  }),
  { refreshDeps: [logFilters, logPagination.current, logPagination.pageSize] },
);
```

**Use this for:** read-side overview, provider/policy grids, log filters, and the existing page shell.

**Secondary analog:** `packages/admin/aoxiaoyou-admin-ui/src/pages/SystemManagement/index.tsx`

**Settings form + save + refresh pattern** (`SystemManagement/index.tsx` lines 127-145, 260-274, 446-460, 509-520):
```tsx
const carryoverRequest = useRequest(getAdminCarryoverSettings);
const translationRequest = useRequest(getAdminTranslationSettings);
const mediaPolicyRequest = useRequest(getAdminMediaPolicySettings);
const indoorRuntimeRequest = useRequest(getAdminIndoorRuntimeSettings);

<Form
  form={carryoverForm}
  layout="vertical"
  onFinish={async (values) => {
    const response = await updateAdminCarryoverSettings(values);
    if (!response.success || !response.data) {
      message.error(response.message || '更新總控預設失敗');
      return;
    }
    message.success('總控預設已更新');
    carryoverRequest.refresh();
    translationRequest.refresh();
    mediaPolicyRequest.refresh();
    indoorRuntimeRequest.refresh();
  }}
>
```

**Use this for:** provider defaults, quota/governance forms, fallback switches, and any capability-level config forms.

**Secondary analog:** `packages/admin/aoxiaoyou-admin-ui/src/pages/Dashboard/index.tsx`

**Overview + health composition pattern** (`Dashboard/index.tsx` lines 121-145, 149-182, 183-189):
```tsx
<Row gutter={[16, 16]}>
  <Col xs={24} sm={12} xl={6}>
    <Card loading={loading}>
      <Statistic title="Total travelers" value={stats.totalUsers} prefix={<TeamOutlined />} />
    </Card>
  </Col>
</Row>

<Card title="Integration health" loading={loading}>
  <Descriptions column={1} size="small">
```

**Use this for:** AI capability KPIs, provider health, fallback activation rate, quota pressure, and cost snapshots.

**Secondary analog:** `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace/IndoorRuleCenter.tsx`

**Governance center interaction pattern** (`IndoorRuleCenter.tsx` lines 118-133, 181-215, 247-250, 384-395):
```tsx
const response = await getIndoorRuleOverview(nextFilters);
if (!response.success || !response.data) {
  throw new Error(response.message || "載入互動規則治理中心失敗");
}

setPanelOpen(true);
const detailResponse = await getIndoorRuleBehaviorDetail(behaviorId);
```

**Use this for:** provider/policy detail drawers, log drilldown, capability health detail, and central governance pages.

**Important adaptation**

- Keep the page entrypoint and high-level shell, but stop hardcoding the capability registry in `scenarioMeta` (`AiCapabilityCenter.tsx` lines 43-74). Phase 18 decisions split capabilities into operator-facing and mini-program-facing domains, and translation is explicitly out of scope for this center.
- If the page grows beyond one read-only screen, split it into `components/ai/*` instead of adding more inline tables/forms to the page file.

---

### `packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiProviderFormModal.tsx` and `AiPolicyFormModal.tsx`

**Analog:** `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/RewardManagement.tsx`

**CRUD modal pattern** (`RewardManagement.tsx` lines 89-98, 178-198, 205-236):
```tsx
const [form] = Form.useForm<RewardFormValues>();
const [modalOpen, setModalOpen] = useState(false);
const [editingItem, setEditingItem] = useState<AdminRewardItem | null>(null);

const submitForm = async () => {
  const values = await form.validateFields();
  const payload = buildPayload(values);
  const response = editingItem
    ? await updateAdminReward(editingItem.id, payload)
    : await createAdminReward(payload);
  if (!response.success) {
    message.error(response.message || '儲存獎勵失敗');
    return;
  }
  message.success(editingItem ? '獎勵已更新' : '獎勵已建立');
  setModalOpen(false);
  actionRef.current?.reload();
};
```

**Use this for:** create/edit provider profiles, capability policies, allowlists, fallback chains, and capability-specific quotas.

**Do not copy blindly**

- RewardManagement is a single huge file. Use its modal/open/save lifecycle, but split AI provider/policy forms into dedicated components.
- Secrets must never be treated like normal editable text fields that round-trip back from server. Use one-way set/replace fields with masked preview only.

---

### `packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiCapabilityWorkbenchDrawer.tsx`

**Analog:** `packages/admin/aoxiaoyou-admin-ui/src/components/indoor/IndoorRuleWorkbench.tsx`

**Full-screen apply/validate drawer pattern** (`IndoorRuleWorkbench.tsx` lines 41-50, 200-236, 514-535):
```tsx
interface Props {
  open: boolean;
  onClose: () => void;
  onApply: (values: Partial<...>) => void;
}

const IndoorRuleWorkbench: React.FC<Props> = ({ open, onClose, onApply }) => {
  const [form] = Form.useForm<...>();
  const [validating, setValidating] = React.useState(false);

  React.useEffect(() => {
    if (!open) {
      return;
    }
    form.setFieldsValue(nextValues);
  }, [form, initialValues, open]);
```

**Use this for:** AI authoring workbench, prompt-template preview, test execution console, image/audio candidate review, and apply/finalize flows.

---

### `packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiGenerationHistoryDrawer.tsx`

**Analog:** `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetDetailDrawer.tsx`

**Detail drawer + metadata + usage/history pattern** (`MediaAssetDetailDrawer.tsx` lines 8-24, 39-71, 73-119):
```tsx
interface MediaAssetDetailDrawerProps {
  open: boolean;
  asset?: AdminContentAssetItem | null;
  usageSummary?: AdminContentAssetUsageSummary | null;
  usageLoading?: boolean;
  onClose: () => void;
}

<Drawer title="資源詳情" open={open} width={640} onClose={onClose} destroyOnClose>
  <Descriptions column={1} bordered size="small">
    <Descriptions.Item label="資源 ID">{asset.id}</Descriptions.Item>
    <Descriptions.Item label="上傳管理員">{asset.uploadedByAdminName || '-'}</Descriptions.Item>
    <Descriptions.Item label="處理策略">{asset.processingPolicyCode || '-'}</Descriptions.Item>
  </Descriptions>

  <List
    size="small"
    dataSource={usageSummary.usages || []}
    renderItem={(usage) => (
      <List.Item>
```

**Use this for:** candidate generation history, per-request detail, finalized asset binding detail, and operator-visible audit/history drawers.

---

### `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` and `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts`

**Request wrapper pattern:** `packages/admin/aoxiaoyou-admin-ui/src/utils/request.ts` lines 23-39, 57-117
```ts
const instance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' },
});

instance.interceptors.request.use((config) => {
  const token = getAdminToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

const request = {
  get<T = unknown>(url: string, config?: AxiosRequestConfig) {
    return unwrapResponse<T>(instance.get(url, config));
  },
  post<T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig) {
```

**Typed endpoint pattern:** `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` lines 1009-1025
```ts
export const getAiProviders = () => {
  return request.get<AiProviderItem[]>('/api/admin/v1/ai/providers');
};

export const getAiPolicies = (params?: { scenarioGroup?: string }) => {
  return request.get<AiPolicyItem[]>('/api/admin/v1/ai/policies', { params });
};

export const getAiLogs = (params?: { ... }) => {
  return request.get<PaginationResponse<AiLogItem>>('/api/admin/v1/ai/logs', { params });
};
```

**Multipart upload pattern to reuse for AI-generated candidate finalize/import flows:** `packages/admin/aoxiaoyou-admin-ui/src/services/api.ts` lines 392-440
```ts
const formData = new FormData();
formData.append('file', payload.file);
formData.append('assetKind', payload.assetKind);
return request.post<AdminContentAssetItem>('/api/admin/v1/content/assets/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' },
});
```

**Type module pattern:** `packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts` lines 21-113
```ts
export interface AdminTranslationSettings { ... }
export interface AdminMediaPolicySettings { ... }
export interface PaginationResponse<T> {
  pageNum: number;
  pageSize: number;
  total: number;
  totalPages: number;
  list: T[];
}
```

**Planner guidance**

- New AI interfaces should move into `src/types/admin.ts`. The current AI interfaces embedded in `src/services/api.ts` (`api.ts` lines 959-1025) are acceptable as a temporary baseline, but they do not match the dominant repo convention for larger domains.
- Reuse the existing `request` wrapper; do not add a second AI-specific HTTP client.

---

### `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java`

**Baseline controller envelope:** `AdminAiController.java` lines 17-41
```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/ai")
public class AdminAiController {

    private final AdminAiService adminAiService;

    @GetMapping("/providers")
    public ApiResponse<List<AdminAiProviderResponse>> listProviders() {
        return ApiResponse.success(adminAiService.listProviders());
    }
```

**Write-side controller shape to copy:** `AdminContentManagementController.java` lines 58-118
```java
@GetMapping("/assets")
public ApiResponse<PageResponse<AdminContentAssetResponse>> pageAssets(...) { ... }

@PostMapping(value = "/assets/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ApiResponse<AdminContentAssetResponse> uploadAsset(
        @Valid @ModelAttribute AdminContentAssetUploadRequest request,
        HttpServletRequest httpRequest) {
    return ApiResponse.success(adminContentManagementService.uploadAsset(
            request,
            (Long) httpRequest.getAttribute("adminUserId"),
            (String) httpRequest.getAttribute("adminUsername")
    ));
}

@PutMapping("/assets/{id}")
public ApiResponse<AdminContentAssetResponse> updateAsset(...) { ... }
```

**System settings GET/PUT pattern to copy:** `AdminSystemManagementController.java` lines 83-131
```java
@GetMapping("/media-policy")
public ApiResponse<AdminMediaPolicySettingsResponse> getMediaPolicySettings() {
    return ApiResponse.success(adminSystemManagementService.getMediaPolicySettings());
}

@PutMapping("/media-policy")
public ApiResponse<AdminMediaPolicySettingsResponse> updateMediaPolicySettings(
        @Valid @RequestBody AdminMediaPolicySettingsUpsertRequest request) {
    return ApiResponse.success(adminSystemManagementService.updateMediaPolicySettings(request));
}
```

**Apply to Phase 18**

- Add CRUD and overview endpoints under `/api/admin/v1/ai`.
- For any endpoint that creates generation jobs, candidate assets, or admin-owned history, read `adminUserId` and `adminUsername` from the request just like media upload does.
- Return DTOs only. Never return entity classes with encrypted fields.

---

### `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java`

**Read-side mapping pattern:** `AdminAiServiceImpl.java` lines 32-124
```java
return aiProviderConfigMapper.selectList(new LambdaQueryWrapper<AiProviderConfig>().orderByAsc(AiProviderConfig::getId))
        .stream()
        .map(item -> AdminAiProviderResponse.builder()
                .id(item.getId())
                .providerName(item.getProviderName())
                .displayName(item.getDisplayName())
                .apiBaseUrl(item.getApiBaseUrl())
                .modelName(item.getModelName())
                .quotaDaily(item.getQuotaDaily())
                .build())
        .toList();
```

**Paged service pattern:** `AdminContentManagementServiceImpl.java` lines 127-157
```java
Page<ContentAsset> page = contentAssetMapper.selectPage(new Page<>(pageNum, pageSize),
        new LambdaQueryWrapper<ContentAsset>()
                .eq(StringUtils.hasText(assetKind), ContentAsset::getAssetKind, assetKind)
                .orderByDesc(ContentAsset::getId));
Page<AdminContentAssetResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
result.setRecords(page.getRecords().stream().map(this::toContentAssetResponse).toList());
return PageResponse.of(result);
```

**Aggregate overview pattern:** `DashboardServiceImpl.java` lines 64-129
```java
DashboardStatsResponse.ComponentStatus databaseStatus = checkDatabase();
DashboardStatsResponse.ComponentStatus publicApiStatus = checkPublicApi();
DashboardStatsResponse.ComponentStatus cosStatus = checkCos();

return DashboardStatsResponse.builder()
        .totalUsers(totalUsers)
        .contentSummary(contentSummary)
        .integrationHealth(DashboardStatsResponse.IntegrationHealth.builder()
                .database(databaseStatus)
                .publicApi(publicApiStatus)
                .cos(cosStatus)
                .build())
        .build();
```

**Use this for**

- Provider list/detail DTO mapping
- Policy list/detail DTO mapping
- Paged request logs and generation history
- Central AI overview response with capability metrics + provider health + fallback/quota alerts

**Do not copy blindly**

- Current `AdminAiServiceImpl` is read-only. It is not enough for Phase 18 write-side secret rotation, fallback routing, quota governance, or ownership-isolated history.
- Extend the service with `requireX` and `applyXRequest` style helpers like `AdminContentManagementServiceImpl` (`AdminContentManagementServiceImpl.java` lines 697-776) instead of scattering field assignment across controller methods.

---

### `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiProviderConfig.java`, `AiPolicy.java`, `AiRequestLog.java`

**Current entity baselines**

`AiProviderConfig.java` lines 14-52:
```java
@TableName("ai_provider_configs")
public class AiProviderConfig extends BaseEntity {
    @TableField("api_key_encrypted")
    private String apiKeyEncrypted;
    @TableField("api_secret_encrypted")
    private String apiSecretEncrypted;
    @TableField("request_timeout_ms")
    private Integer requestTimeoutMs;
    @TableField("quota_daily")
    private Integer quotaDaily;
}
```

`AiPolicy.java` lines 14-64:
```java
@TableName("ai_navigation_policies")
public class AiPolicy extends BaseEntity {
    @TableField("scenario_group")
    private String scenarioGroup;
    @TableField("provider_id")
    private Long providerId;
    @TableField("prompt_template")
    private String promptTemplate;
    @TableField("fallback_policy_id")
    private Long fallbackPolicyId;
}
```

`AiRequestLog.java` lines 13-52:
```java
@TableName("ai_request_logs")
public class AiRequestLog {
    @TableField("provider_id")
    private Long providerId;
    @TableField("policy_id")
    private Long policyId;
    @TableField("user_openid")
    private String userOpenid;
    @TableField("input_data_hash")
    private String inputDataHash;
    @TableField("output_summary")
    private String outputSummary;
}
```

**DTO secret-safe pattern:** `AdminAiProviderResponse.java` lines 10-21
```java
public class AdminAiProviderResponse {
    private Long id;
    private String providerName;
    private String displayName;
    private String apiBaseUrl;
    private String modelName;
    private String capabilities;
    private Integer quotaDaily;
}
```

**Apply to Phase 18**

- Keep mutable config tables on `BaseEntity` (`BaseEntity.java` lines 10-16) so `created_at` / `updated_at` keep filling automatically.
- Keep append-only logs/history tables explicit if they do not actually have `updated_at` / `deleted`.
- Preserve the secret-safe response boundary: encrypted columns stay in entities only, never in response DTOs.

**Critical schema cautions**

- `AiPolicy` currently maps to `ai_navigation_policies` (`AiPolicy.java` line 14). Do not spread navigation-specific naming into new endpoint names, DTOs, or UI copy. Either widen this table intentionally with a migration plan or introduce a clearly broader table and mapper.
- `AiRequestLog` has `user_openid` but no admin owner columns (`AiRequestLog.java` lines 19-35). Phase 18 requires "admins can see all history, normal operators only their own generation history", so add explicit admin ownership fields on generation/candidate history instead of overloading `userOpenid`.

---

### `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/AiAssetCandidateService.java`

**Analog:** `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/media/MediaIntakeService.java`

**Persisted asset intake pattern** (`MediaIntakeService.java` lines 100-137):
```java
SysAdmin admin = requireAdmin(adminUserId);
ResolvedMediaUploadPolicy policy = mediaUploadPolicyService.resolvePolicy(assetKind, file, admin);
ProcessedMediaPayload processedPayload = processPayload(file, policy);
StoredAssetMetadata stored = cosAssetStorageService.storeAsset(...);

ContentAsset asset = new ContentAsset();
asset.setAssetKind(policy.getAssetKind());
asset.setBucketName(stored.getBucketName());
asset.setObjectKey(stored.getObjectKey());
asset.setCanonicalUrl(stored.getCanonicalUrl());
asset.setUploadedByAdminId(admin.getId());
asset.setUploadedByAdminName(...);
asset.setProcessingPolicyCode(policy.getEffectivePolicyCode());
contentAssetMapper.insert(asset);
```

**COS storage pattern** (`CosAssetStorageService.java` lines 70-107, 128-160):
```java
String objectKey = StringUtils.hasText(forcedObjectKey)
        ? sanitizeObjectKey(forcedObjectKey)
        : buildObjectKey(payload.getOriginalFilename(), payload.getAssetKind(), normalizedLocale, mimeType);
String checksum = sha256(bytes);

PutObjectRequest putObjectRequest = new PutObjectRequest(
        cosProperties.getBucketName(),
        objectKey,
        new ByteArrayInputStream(bytes),
        objectMetadata
);
PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
```

**Delete/usage guard pattern for finalized assets:** `AdminContentManagementServiceImpl.java` lines 205-223
```java
List<AdminContentAssetUsageItemResponse> usages = listAssetUsages(item);
if (!usages.isEmpty()) {
    throw new BusinessException(4055, "Content asset is still in use and cannot be deleted");
}
cosAssetStorageService.deleteAsset(item.getBucketName(), item.getObjectKey());
contentAssetMapper.deleteById(id);
```

**Planner guidance**

- AI-generated image/audio candidates can have their own job/history tables, but finalized assets should still land in the existing `content_assets` pipeline and COS object-key scheme.
- Reuse `MediaIntakeService` field conventions for owner, processing note, policy code, checksum, etag, and published status.
- Do not build a second "final asset library" outside `content_assets`.

---

### `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/provider/DashScopeCapabilityProvider.java`

**Analog:** `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/auth/WechatAuthService.java`

**Outbound HTTP + timeout + typed response + BusinessException pattern** (`WechatAuthService.java` lines 19-30, 34-62):
```java
SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
requestFactory.setConnectTimeout(properties.getAuthTimeoutMs());
requestFactory.setReadTimeout(properties.getAuthTimeoutMs());
this.restClient = RestClient.builder()
        .requestFactory(requestFactory)
        .build();

try {
    response = restClient.get()
            .uri(url)
            .retrieve()
            .body(WechatCode2SessionResponse.class);
} catch (RestClientException ex) {
    throw new BusinessException(5003, "Failed to reach WeChat auth service");
}
```

**Use this for**

- provider invocation adapters
- provider health probes
- capability test endpoints
- fallback failover probes

**Related config pattern:** `CosProperties.java` lines 10-35 and `CosConfig.java` lines 17-32
```java
@ConfigurationProperties(prefix = "app.cos")
public class CosProperties {
    private boolean enabled = false;
    private String secretId;
    private String secretKey;
}
```

**Interpretation**

- The repo already has strong patterns for env-backed runtime secrets and typed outbound clients.
- The repo does **not** have an existing at-rest secret crypto/masking service for admin-managed provider keys. Plan a new `AiSecretCryptoService` deliberately; do not pretend `CosProperties` solves this requirement.

---

## Shared Patterns

### Response Envelope

**Sources:** `ApiResponse.java` lines 14-47, `PageResponse.java` lines 13-38

- All admin AI endpoints should return `ApiResponse<T>`.
- Any paged list should return `ApiResponse<PageResponse<T>>`.

### Error Handling

**Sources:** `BusinessException.java` lines 6-17, `GlobalExceptionHandler.java` lines 19-54

- Service-layer validation and domain failures should throw `BusinessException`.
- Controllers should stay thin and let `GlobalExceptionHandler` convert exceptions into the standard envelope.

### Admin Identity / Ownership

**Source:** `AdminAuthInterceptor.java` lines 34-39
```java
Long userId = jwtUtil.getUserId(token);
request.setAttribute("adminUserId", userId);
request.setAttribute("adminUsername", jwtUtil.getUsername(token));
request.setAttribute("adminRoles", jwtUtil.getRoles(token));
```

- Reuse these request attributes for generation history ownership, provider-change audit, and candidate/finalize actions.

### Frontend Request Guard

**Source:** `packages/admin/aoxiaoyou-admin-ui/src/utils/request.ts` lines 95-113

- All new AI POST/PUT/PATCH calls should continue using the existing suspicious-text guard and auth interceptor.
- Do not bypass `request.ts` with raw `fetch` or a second Axios instance.

### Overview Aggregation

**Source:** `DashboardServiceImpl.java` lines 64-129 and `DashboardStatsResponse.java` lines 10-84

- Model AI overview as one aggregate response with summary cards plus nested health/status sections, not many uncoordinated endpoints.

### Asset Pipeline Reuse

**Sources:** `MediaIntakeService.java` lines 100-137, `CosAssetStorageService.java` lines 70-107, `AdminContentManagementServiceImpl.java` lines 205-223

- Final AI image/audio assets should use the existing COS object-key generation, metadata capture, and `content_assets` persistence.
- History/candidate tables may be new, but final publish must converge on the existing media system.

### Seed / UTF-8 Discipline

**Source:** `scripts/local/mysql/init/01-init.sql` lines 406-567

- Reuse the existing AI table/seed section structure.
- Keep seed values as placeholders only; never commit real provider secrets or per-user temporary keys.

## Anti-Patterns To Avoid

- Do not leave capability definitions hardcoded in `AiCapabilityCenter.tsx` (`lines 43-74`) once Phase 18 introduces operator-facing and mini-program-facing capability domains.
- Do not extend the `ai_navigation_policies` name into new DTOs, endpoints, or UI copy (`AiPolicy.java` line 14). It is already too narrow for the Phase 18 scope.
- Do not use `AiRequestLog.userOpenid` as a proxy for admin ownership (`AiRequestLog.java` lines 19-35). Add explicit admin owner fields where ownership isolation matters.
- Do not keep a growing AI type surface inline in `src/services/api.ts` (`api.ts` lines 959-1025). Move stable interfaces into `src/types/admin.ts`.
- Do not return `apiKeyEncrypted`, `apiSecretEncrypted`, raw prompt templates, or raw provider payloads to the frontend. Existing response DTOs already establish the secret-safe boundary.
- Do not bypass `MediaIntakeService` / `CosAssetStorageService` for AI-generated final media.
- Do not make long-running image generation or TTS finalize synchronously on controller threads. The AI spec calls for job/polling style for heavier media paths.
- Do not place real provider keys in `01-init.sql`, repo config, frontend constants, or planning docs. The existing SQL uses placeholders only (`01-init.sql` lines 533-567).

## No Analog Found

| File / Area | Role | Data Flow | Reason |
|---|---|---|---|
| `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/config/AiSecretCryptoService.java` | service/config | transform | The repo has env-backed runtime secret config (`CosProperties`, `CosConfig`) but no existing at-rest crypto/masking service for admin-managed secrets. |
| `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/routing/AiQuotaThrottleService.java` | service | event-driven / request-response | No existing quota, suspicious-concurrency, or provider-routing throttle service was found beyond static `quotaDaily` fields. Use existing controller/service/DTO patterns, but design this subsystem explicitly. |

## Metadata

**Analog search scope:** `packages/admin/aoxiaoyou-admin-ui/src`, `packages/admin/aoxiaoyou-admin-backend/src/main/java`, `packages/server/src/main/java`, `scripts/local/mysql/init`

**Files scanned:** 30+

**Most important reusable anchors**

- UI overview/governance shell: `AiCapabilityCenter.tsx`, `SystemManagement/index.tsx`, `Dashboard/index.tsx`, `IndoorRuleCenter.tsx`
- UI CRUD/detail primitives: `RewardManagement.tsx`, `IndoorRuleWorkbench.tsx`, `MediaAssetDetailDrawer.tsx`, `MediaAssetPickerField.tsx`
- Backend controller/service envelope: `AdminAiController.java`, `AdminContentManagementController.java`, `AdminSystemManagementController.java`, `AdminAiServiceImpl.java`
- Asset pipeline: `MediaIntakeService.java`, `CosAssetStorageService.java`, `AdminContentManagementServiceImpl.java`
- Outbound integration: `WechatAuthService.java`
