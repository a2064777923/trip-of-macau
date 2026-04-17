package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.ai.config.AiCapabilityProperties;
import com.aoxiaoyou.admin.ai.config.AiSecretCryptoService;
import com.aoxiaoyou.admin.ai.provider.AiOutboundUrlGuard;
import com.aoxiaoyou.admin.ai.provider.AiProviderTemplateRegistry;
import com.aoxiaoyou.admin.ai.provider.DashScopeProviderGateway;
import com.aoxiaoyou.admin.ai.routing.AiGovernanceService;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.*;
import com.aoxiaoyou.admin.dto.response.*;
import com.aoxiaoyou.admin.entity.*;
import com.aoxiaoyou.admin.mapper.*;
import com.aoxiaoyou.admin.media.CosAssetStorageService;
import com.aoxiaoyou.admin.media.StoredAssetMetadata;
import com.aoxiaoyou.admin.media.StoredAssetPayload;
import com.aoxiaoyou.admin.service.AdminAiService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAiServiceImpl implements AdminAiService {

    private static final String STATUS_ENABLED = "enabled";
    private static final String STATUS_IDLE = "idle";
    private static final String JOB_STATUS_PENDING = "pending";
    private static final String JOB_STATUS_SUBMITTED = "submitted";
    private static final String JOB_STATUS_COMPLETED = "completed";
    private static final String JOB_STATUS_FAILED = "failed";
    private static final String CONFIG_AI_INVENTORY_FRESHNESS_HOURS = "ai.platform.inventory-freshness-hours";
    private static final String CONFIG_AI_SYNC_HISTORY_LIMIT = "ai.platform.sync-history-limit";
    private static final String CONFIG_AI_DAILY_COST_ALERT_USD = "ai.platform.daily-cost-alert-usd";
    private static final String CONFIG_AI_PROVIDER_FAILURE_RATE_WARNING = "ai.platform.provider-failure-rate-warning";
    private static final String CONFIG_AI_RECENT_WINDOW_HOURS = "ai.platform.recent-window-hours";
    private static final String CONFIG_AI_ALLOW_OPERATOR_GLOBAL_HISTORY = "ai.platform.allow-operator-global-history";

    private final ObjectMapper objectMapper;
    private final AiCapabilityProperties aiCapabilityProperties;
    private final AiSecretCryptoService aiSecretCryptoService;
    private final AiOutboundUrlGuard aiOutboundUrlGuard;
    private final AiProviderTemplateRegistry aiProviderTemplateRegistry;
    private final DashScopeProviderGateway dashScopeProviderGateway;
    private final AiGovernanceService aiGovernanceService;
    private final CosAssetStorageService cosAssetStorageService;
    private final AiCapabilityMapper aiCapabilityMapper;
    private final AiCapabilityPolicyMapper aiCapabilityPolicyMapper;
    private final AiPolicyProviderBindingMapper aiPolicyProviderBindingMapper;
    private final AiProviderConfigMapper aiProviderConfigMapper;
    private final AiProviderInventoryMapper aiProviderInventoryMapper;
    private final AiProviderSyncJobMapper aiProviderSyncJobMapper;
    private final AiQuotaRuleMapper aiQuotaRuleMapper;
    private final AiPromptTemplateMapper aiPromptTemplateMapper;
    private final AiGenerationJobMapper aiGenerationJobMapper;
    private final AiGenerationCandidateMapper aiGenerationCandidateMapper;
    private final AiRequestLogMapper aiRequestLogMapper;
    private final ContentAssetMapper contentAssetMapper;
    private final SysConfigMapper sysConfigMapper;

    @Override
    public AdminAiOverviewResponse getOverview(Long currentAdminId, List<String> roles) {
        List<AiCapability> capabilities = aiCapabilityMapper.selectList(
                new LambdaQueryWrapper<AiCapability>()
                        .orderByAsc(AiCapability::getSortOrder)
                        .orderByAsc(AiCapability::getId)
        );
        List<AiProviderConfig> providers = aiProviderConfigMapper.selectList(
                new LambdaQueryWrapper<AiProviderConfig>().orderByAsc(AiProviderConfig::getId)
        );
        int recentWindowHours = readIntegerConfig(CONFIG_AI_RECENT_WINDOW_HOURS, 24);
        int inventoryFreshnessHours = readIntegerConfig(CONFIG_AI_INVENTORY_FRESHNESS_HOURS, 24);
        LocalDateTime since = LocalDateTime.now().minusHours(recentWindowHours);
        List<AiRequestLog> recentLogs = aiRequestLogMapper.selectList(
                new LambdaQueryWrapper<AiRequestLog>()
                        .ge(AiRequestLog::getCreatedAt, since)
                        .orderByDesc(AiRequestLog::getCreatedAt)
                        .orderByDesc(AiRequestLog::getId)
        );
        List<AiProviderInventory> inventories = aiProviderInventoryMapper.selectList(null);
        Map<String, List<AiRequestLog>> logsByCapability = recentLogs.stream()
                .filter(item -> StringUtils.hasText(item.getCapabilityCode()))
                .collect(Collectors.groupingBy(AiRequestLog::getCapabilityCode));
        Map<Long, List<AiRequestLog>> logsByProvider = recentLogs.stream()
                .filter(item -> item.getProviderId() != null)
                .collect(Collectors.groupingBy(AiRequestLog::getProviderId));

        boolean superAdmin = isSuperAdmin(roles);
        List<AiGenerationJob> recentJobs = aiGenerationJobMapper.selectList(
                new LambdaQueryWrapper<AiGenerationJob>()
                        .eq(!superAdmin && currentAdminId != null, AiGenerationJob::getOwnerAdminId, currentAdminId)
                        .orderByDesc(AiGenerationJob::getUpdatedAt)
                        .orderByDesc(AiGenerationJob::getId)
                        .last("LIMIT 6")
        );
        long activeJobs = aiGenerationJobMapper.selectCount(
                new LambdaQueryWrapper<AiGenerationJob>()
                        .eq(!superAdmin && currentAdminId != null, AiGenerationJob::getOwnerAdminId, currentAdminId)
                        .in(AiGenerationJob::getJobStatus, List.of(JOB_STATUS_PENDING, JOB_STATUS_SUBMITTED))
        );

        List<AdminAiCapabilityResponse> capabilityResponses = capabilities.stream()
                .map(item -> mapCapability(item, logsByCapability.getOrDefault(item.getCapabilityCode(), Collections.emptyList())))
                .toList();
        List<AdminAiOverviewResponse.ProviderHealth> providerHealth = providers.stream()
                .map(provider -> {
                    List<AiRequestLog> providerLogs = logsByProvider.getOrDefault(provider.getId(), Collections.emptyList());
                    long failures = providerLogs.stream().filter(item -> !Objects.equals(item.getSuccess(), 1)).count();
                    long avgLatency = providerLogs.isEmpty() ? 0L : Math.round(providerLogs.stream()
                            .filter(item -> item.getLatencyMs() != null)
                            .mapToLong(AiRequestLog::getLatencyMs)
                            .average()
                            .orElse(0D));
                    return AdminAiOverviewResponse.ProviderHealth.builder()
                            .providerId(provider.getId())
                            .providerName(provider.getProviderName())
                            .displayName(provider.getDisplayName())
                            .healthStatus(provider.getHealthStatus())
                            .healthMessage(provider.getHealthMessage())
                            .endpointStyle(provider.getEndpointStyle())
                            .providerType(provider.getProviderType())
                            .status(provider.getStatus())
                            .lastInventorySyncStatus(provider.getLastInventorySyncStatus())
                            .lastInventorySyncedAt(provider.getLastInventorySyncedAt())
                            .inventoryRecordCount(provider.getInventoryRecordCount())
                            .requestCount24h((long) providerLogs.size())
                            .failureCount24h(failures)
                            .averageLatencyMs(avgLatency)
                            .build();
                })
                .toList();

        Map<Long, AiCapabilityPolicy> policyMap = aiCapabilityPolicyMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiCapabilityPolicy::getId, item -> item, (left, right) -> left));
        long staleProviders = providers.stream()
                .filter(item -> !Objects.equals(item.getStatus(), 0))
                .filter(item -> item.getLastInventorySyncedAt() == null || item.getLastInventorySyncedAt().isBefore(LocalDateTime.now().minusHours(inventoryFreshnessHours)))
                .count();
        return AdminAiOverviewResponse.builder()
                .summary(AdminAiOverviewResponse.Summary.builder()
                        .totalCapabilities(capabilities.size())
                        .enabledCapabilities((int) capabilities.stream().filter(item -> STATUS_ENABLED.equalsIgnoreCase(item.getStatus())).count())
                        .enabledProviders((int) providers.stream().filter(item -> Objects.equals(item.getStatus(), 1)).count())
                        .healthyProviders((int) providers.stream().filter(item -> "healthy".equalsIgnoreCase(item.getHealthStatus())).count())
                        .inventoryRecords(inventories.size())
                        .staleProviders((int) staleProviders)
                        .requests24h((long) recentLogs.size())
                        .failures24h(recentLogs.stream().filter(item -> !Objects.equals(item.getSuccess(), 1)).count())
                        .fallbacks24h(recentLogs.stream().filter(item -> Objects.equals(item.getFallbackTriggered(), 1)).count())
                        .estimatedCost24h(recentLogs.stream()
                                .map(AiRequestLog::getCostUsd)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .activeJobs(activeJobs)
                        .build())
                .capabilities(capabilityResponses)
                .providers(providerHealth)
                .alerts(buildOverviewAlerts(capabilities, providers, providerHealth, recentLogs))
                .recentJobs(toJobResponses(recentJobs, currentAdminId, roles))
                .recentLogs(recentLogs.stream().limit(8).map(item -> mapLog(item, providers, policyMap)).toList())
                .build();
    }

    @Override
    public List<AdminAiCapabilityResponse> listCapabilities() {
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<AiRequestLog> logs = aiRequestLogMapper.selectList(
                new LambdaQueryWrapper<AiRequestLog>().ge(AiRequestLog::getCreatedAt, since)
        );
        Map<String, List<AiRequestLog>> logsByCapability = logs.stream()
                .filter(item -> StringUtils.hasText(item.getCapabilityCode()))
                .collect(Collectors.groupingBy(AiRequestLog::getCapabilityCode));
        return aiCapabilityMapper.selectList(
                        new LambdaQueryWrapper<AiCapability>()
                                .orderByAsc(AiCapability::getSortOrder)
                                .orderByAsc(AiCapability::getId)
                ).stream()
                .map(item -> mapCapability(item, logsByCapability.getOrDefault(item.getCapabilityCode(), Collections.emptyList())))
                .toList();
    }

    @Override
    public List<AdminAiProviderTemplateResponse> listProviderTemplates() {
        return aiProviderTemplateRegistry.list().stream()
                .map(template -> AdminAiProviderTemplateResponse.builder()
                        .platformCode(template.getPlatformCode())
                        .platformLabel(template.getPlatformLabel())
                        .description(template.getDescription())
                        .providerType(template.getProviderType())
                        .endpointStyle(template.getEndpointStyle())
                        .defaultBaseUrl(template.getDefaultBaseUrl())
                        .docsUrl(template.getDocsUrl())
                        .authScheme(template.getAuthScheme())
                        .syncStrategy(template.getSyncStrategy())
                        .inventorySemantics(template.getInventorySemantics())
                        .defaultModelName(template.getDefaultModelName())
                        .supportedModalities(template.getSupportedModalities())
                        .credentialFields(template.getCredentialFields())
                        .build())
                .toList();
    }

    @Override
    public List<AdminAiProviderResponse> listProviders() {
        return aiProviderConfigMapper.selectList(
                        new LambdaQueryWrapper<AiProviderConfig>().orderByAsc(AiProviderConfig::getId)
                ).stream()
                .map(this::mapProvider)
                .toList();
    }

    @Override
    @Transactional
    public AdminAiProviderResponse createProvider(AdminAiProviderUpsertRequest request) {
        AiProviderConfig existing = aiProviderConfigMapper.selectOne(
                new LambdaQueryWrapper<AiProviderConfig>().eq(AiProviderConfig::getProviderName, request.getProviderName().trim())
        );
        if (existing != null) {
            throw new BusinessException(4090, "AI 供應商代碼已存在");
        }
        AiProviderConfig provider = new AiProviderConfig();
        fillProvider(provider, request, true);
        aiProviderConfigMapper.insert(provider);
        return mapProvider(provider);
    }

    @Override
    @Transactional
    public AdminAiProviderResponse updateProvider(Long id, AdminAiProviderUpsertRequest request) {
        AiProviderConfig provider = requireProvider(id);
        AiProviderConfig duplicate = aiProviderConfigMapper.selectOne(
                new LambdaQueryWrapper<AiProviderConfig>()
                        .eq(AiProviderConfig::getProviderName, request.getProviderName().trim())
                        .ne(AiProviderConfig::getId, id)
        );
        if (duplicate != null) {
            throw new BusinessException(4090, "AI 供應商代碼已存在");
        }
        fillProvider(provider, request, false);
        aiProviderConfigMapper.updateById(provider);
        return mapProvider(provider);
    }

    @Override
    @Transactional
    public void deleteProvider(Long id) {
        requireProvider(id);
        long policyBindings = aiPolicyProviderBindingMapper.selectCount(
                new LambdaQueryWrapper<AiPolicyProviderBinding>().eq(AiPolicyProviderBinding::getProviderId, id)
        );
        long promptRefs = aiPromptTemplateMapper.selectCount(
                new LambdaQueryWrapper<AiPromptTemplate>().eq(AiPromptTemplate::getDefaultProviderId, id)
        );
        long jobRefs = aiGenerationJobMapper.selectCount(
                new LambdaQueryWrapper<AiGenerationJob>().eq(AiGenerationJob::getProviderId, id)
        );
        if (policyBindings > 0 || promptRefs > 0 || jobRefs > 0) {
            throw new BusinessException(4091, "AI 供應商仍被策略、模板或任務引用，不能刪除");
        }
        aiProviderConfigMapper.deleteById(id);
    }

    @Override
    @Transactional
    public AdminAiProviderTestResponse testProvider(Long id, AdminAiProviderTestRequest request, Long currentAdminId, String currentAdminName) {
        AiProviderConfig provider = requireProvider(id);
        String apiKey = decryptRequiredApiKey(provider);
        String prompt = StringUtils.hasText(request.getPrompt()) ? request.getPrompt().trim() : "Trip of Macau admin provider connectivity check.";
        String traceId = UUID.randomUUID().toString().replace("-", "");
        long startedAt = System.currentTimeMillis();
        String resolvedModel = null;
        String preview = null;
        String taskId = null;
        Long latencyMs = null;

        try {
            if ("dashscope_image".equalsIgnoreCase(provider.getEndpointStyle())) {
                DashScopeProviderGateway.ImageTaskResult result = dashScopeProviderGateway.submitImageJob(
                        provider, apiKey, prompt, request.getModelOverride(), "{\"size\":\"1024*1024\"}"
                );
                resolvedModel = result.resolvedModel();
                preview = result.taskStatus();
                taskId = result.taskId();
            } else if ("dashscope_tts".equalsIgnoreCase(provider.getEndpointStyle())) {
                DashScopeProviderGateway.TtsResult result = dashScopeProviderGateway.synthesizeSpeech(
                        provider, apiKey, prompt, request.getModelOverride(), "{\"voice\":\"longyang\",\"format\":\"mp3\",\"sampleRate\":24000}"
                );
                resolvedModel = result.resolvedModel();
                preview = result.assetUrl();
                latencyMs = result.latencyMs();
            } else {
                DashScopeProviderGateway.ChatResult result = dashScopeProviderGateway.testChatProvider(
                        provider, apiKey, prompt, request.getModelOverride(), aiCapabilityProperties.getProviderTestTimeoutMs()
                );
                resolvedModel = result.resolvedModel();
                preview = result.previewText();
                latencyMs = result.latencyMs();
            }
        } catch (BusinessException ex) {
            provider.setHealthStatus("error");
            provider.setHealthMessage(trimTo(ex.getMessage(), 255));
            provider.setLastHealthCheckedAt(LocalDateTime.now());
            provider.setLastFailureAt(LocalDateTime.now());
            aiProviderConfigMapper.updateById(provider);
            insertLog(provider.getId(), null, request.getCapabilityCode(), currentAdminId, currentAdminName,
                    "provider_test", hash(prompt), null, (int) (System.currentTimeMillis() - startedAt),
                    null, BigDecimal.ZERO, 0, 0, null, ex.getMessage(), traceId);
            throw ex;
        }

        provider.setHealthStatus(StringUtils.hasText(preview) || StringUtils.hasText(taskId) ? "healthy" : "warning");
        provider.setHealthMessage(StringUtils.hasText(preview) || StringUtils.hasText(taskId) ? "最近一次連通測試成功" : "最近一次連通測試未取得有效回應");
        provider.setLastHealthCheckedAt(LocalDateTime.now());
        provider.setLastSuccessAt(LocalDateTime.now());
        aiProviderConfigMapper.updateById(provider);
        insertLog(provider.getId(), null, request.getCapabilityCode(), currentAdminId, currentAdminName,
                "provider_test", hash(prompt), preview, latencyMs == null ? (int) (System.currentTimeMillis() - startedAt) : latencyMs.intValue(),
                estimateTokens(prompt), BigDecimal.ZERO, 1, 0, null, null, traceId);

        return AdminAiProviderTestResponse.builder()
                .providerId(provider.getId())
                .providerName(provider.getDisplayName())
                .endpointStyle(provider.getEndpointStyle())
                .success(1)
                .latencyMs(latencyMs)
                .resolvedModel(resolvedModel)
                .message("連通測試成功")
                .preview(preview)
                .taskId(taskId)
                .build();
    }

    @Override
    @Transactional
    public AdminAiProviderSyncJobResponse syncProviderInventory(Long id, Long currentAdminId, String currentAdminName) {
        AiProviderConfig provider = requireProvider(id);
        AiProviderSyncJob job = new AiProviderSyncJob();
        job.setProviderId(provider.getId());
        job.setPlatformCode(defaultString(provider.getPlatformCode(), "custom"));
        job.setSyncStrategy(defaultString(provider.getSyncStrategy(), "manual"));
        job.setJobStatus(JOB_STATUS_PENDING);
        job.setMessage("等待開始同步");
        job.setStartedAt(LocalDateTime.now());
        aiProviderSyncJobMapper.insert(job);

        provider.setLastInventorySyncStatus("running");
        provider.setLastInventorySyncMessage("正在同步模型庫");
        aiProviderConfigMapper.updateById(provider);

        try {
            InventorySyncResult result = synchronizeInventory(provider);
            job.setJobStatus(JOB_STATUS_COMPLETED);
            job.setMessage(result.message());
            job.setDiscoveredCount(result.discoveredCount());
            job.setCreatedCount(result.createdCount());
            job.setUpdatedCount(result.updatedCount());
            job.setStaleCount(result.staleCount());
            job.setRawPayloadJson(result.rawPayloadJson());
            job.setFinishedAt(LocalDateTime.now());
            aiProviderSyncJobMapper.updateById(job);

            provider.setLastInventorySyncStatus("completed");
            provider.setLastInventorySyncMessage(trimTo(result.message(), 255));
            provider.setLastInventorySyncedAt(LocalDateTime.now());
            provider.setInventoryRecordCount(countProviderAvailableInventory(provider.getId()));
            aiProviderConfigMapper.updateById(provider);
            return mapSyncJob(job, provider);
        } catch (BusinessException ex) {
            job.setJobStatus(JOB_STATUS_FAILED);
            job.setMessage(trimTo(ex.getMessage(), 255));
            job.setErrorDetail(ex.getMessage());
            job.setFinishedAt(LocalDateTime.now());
            aiProviderSyncJobMapper.updateById(job);

            provider.setLastInventorySyncStatus("failed");
            provider.setLastInventorySyncMessage(trimTo(ex.getMessage(), 255));
            aiProviderConfigMapper.updateById(provider);
            throw ex;
        }
    }

    @Override
    public List<AdminAiProviderSyncJobResponse> listProviderSyncJobs(Long providerId) {
        AiProviderConfig provider = requireProvider(providerId);
        return aiProviderSyncJobMapper.selectList(
                        new LambdaQueryWrapper<AiProviderSyncJob>()
                                .eq(AiProviderSyncJob::getProviderId, providerId)
                                .orderByDesc(AiProviderSyncJob::getCreatedAt)
                                .orderByDesc(AiProviderSyncJob::getId)
                                .last("LIMIT " + readIntegerConfig(CONFIG_AI_SYNC_HISTORY_LIMIT, 12))
                ).stream()
                .map(item -> mapSyncJob(item, provider))
                .toList();
    }

    @Override
    public List<AdminAiInventoryResponse> listInventory(Long providerId, String capabilityCode, String sourceType) {
        Map<Long, AiProviderConfig> providerMap = aiProviderConfigMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiProviderConfig::getId, item -> item, (left, right) -> left));
        String normalizedCapabilityCode = StringUtils.hasText(capabilityCode) ? capabilityCode.trim() : null;
        return aiProviderInventoryMapper.selectList(
                        new LambdaQueryWrapper<AiProviderInventory>()
                                .eq(providerId != null, AiProviderInventory::getProviderId, providerId)
                                .eq(StringUtils.hasText(sourceType), AiProviderInventory::getSourceType, sourceType)
                                .orderByAsc(AiProviderInventory::getProviderId)
                                .orderByAsc(AiProviderInventory::getSortOrder)
                                .orderByAsc(AiProviderInventory::getId)
                ).stream()
                .filter(item -> !StringUtils.hasText(normalizedCapabilityCode)
                        || parseStringList(readJsonList(item.getCapabilityCodesJson())).contains(normalizedCapabilityCode))
                .map(item -> mapInventory(item, providerMap.get(item.getProviderId())))
                .toList();
    }

    @Override
    @Transactional
    public AdminAiInventoryResponse createInventory(AdminAiInventoryUpsertRequest request) {
        AiProviderConfig provider = requireProvider(request.getProviderId());
        AiProviderInventory duplicate = aiProviderInventoryMapper.selectOne(
                new LambdaQueryWrapper<AiProviderInventory>()
                        .eq(AiProviderInventory::getProviderId, provider.getId())
                        .eq(AiProviderInventory::getInventoryCode, request.getInventoryCode().trim())
        );
        if (duplicate != null) {
            throw new BusinessException(4093, "AI 模型庫代碼已存在");
        }
        AiProviderInventory inventory = new AiProviderInventory();
        fillInventory(inventory, request);
        inventory.setSyncedAt(LocalDateTime.now());
        inventory.setLastSeenAt(LocalDateTime.now());
        aiProviderInventoryMapper.insert(inventory);
        refreshProviderInventoryCount(provider.getId());
        return mapInventory(inventory, provider);
    }

    @Override
    @Transactional
    public AdminAiInventoryResponse updateInventory(Long id, AdminAiInventoryUpsertRequest request) {
        AiProviderInventory inventory = requireInventory(id);
        AiProviderConfig provider = requireProvider(request.getProviderId());
        AiProviderInventory duplicate = aiProviderInventoryMapper.selectOne(
                new LambdaQueryWrapper<AiProviderInventory>()
                        .eq(AiProviderInventory::getProviderId, provider.getId())
                        .eq(AiProviderInventory::getInventoryCode, request.getInventoryCode().trim())
                        .ne(AiProviderInventory::getId, id)
        );
        if (duplicate != null) {
            throw new BusinessException(4093, "AI 模型庫代碼已存在");
        }
        fillInventory(inventory, request);
        inventory.setSyncedAt(LocalDateTime.now());
        aiProviderInventoryMapper.updateById(inventory);
        refreshProviderInventoryCount(provider.getId());
        return mapInventory(inventory, provider);
    }

    @Override
    @Transactional
    public void deleteInventory(Long id) {
        AiProviderInventory inventory = requireInventory(id);
        long bindings = aiPolicyProviderBindingMapper.selectCount(
                new LambdaQueryWrapper<AiPolicyProviderBinding>().eq(AiPolicyProviderBinding::getInventoryId, id)
        );
        long jobs = aiGenerationJobMapper.selectCount(
                new LambdaQueryWrapper<AiGenerationJob>().eq(AiGenerationJob::getInventoryId, id)
        );
        if (bindings > 0 || jobs > 0) {
            throw new BusinessException(4094, "仍有能力路由或生成任務引用此模型庫項目，無法刪除");
        }
        aiProviderInventoryMapper.deleteById(id);
        refreshProviderInventoryCount(inventory.getProviderId());
    }

    @Override
    public List<AdminAiPolicyResponse> listPolicies(String capabilityCode) {
        Long capabilityId = StringUtils.hasText(capabilityCode) ? resolveCapability(capabilityCode.trim()).getId() : null;
        Map<Long, AiCapability> capabilityMap = aiCapabilityMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiCapability::getId, item -> item, (left, right) -> left));
        Map<Long, AiProviderConfig> providerMap = aiProviderConfigMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiProviderConfig::getId, item -> item, (left, right) -> left));
        Map<Long, AiProviderInventory> inventoryMap = aiProviderInventoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiProviderInventory::getId, item -> item, (left, right) -> left));
        Map<Long, List<AiPolicyProviderBinding>> bindings = aiPolicyProviderBindingMapper.selectList(null).stream()
                .collect(Collectors.groupingBy(AiPolicyProviderBinding::getPolicyId));
        return aiCapabilityPolicyMapper.selectList(
                        new LambdaQueryWrapper<AiCapabilityPolicy>()
                                .eq(capabilityId != null, AiCapabilityPolicy::getCapabilityId, capabilityId)
                                .orderByAsc(AiCapabilityPolicy::getSortOrder)
                                .orderByAsc(AiCapabilityPolicy::getId)
                ).stream()
                .map(item -> mapPolicy(item, capabilityMap, providerMap, inventoryMap, bindings.getOrDefault(item.getId(), Collections.emptyList())))
                .toList();
    }

    @Override
    @Transactional
    public AdminAiPolicyResponse createPolicy(AdminAiPolicyUpsertRequest request) {
        AiCapability capability = resolveCapability(request.getCapabilityCode());
        AiCapabilityPolicy existing = aiCapabilityPolicyMapper.selectOne(
                new LambdaQueryWrapper<AiCapabilityPolicy>().eq(AiCapabilityPolicy::getPolicyCode, request.getPolicyCode().trim())
        );
        if (existing != null) {
            throw new BusinessException(4092, "AI 策略代碼已存在");
        }
        AiCapabilityPolicy policy = new AiCapabilityPolicy();
        fillPolicy(policy, capability, request);
        aiCapabilityPolicyMapper.insert(policy);
        replacePolicyBindings(policy.getId(), request.getProviderBindings());
        return listPolicies(request.getCapabilityCode()).stream()
                .filter(item -> Objects.equals(item.getId(), policy.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(5001, "AI 策略建立後回讀失敗"));
    }

    @Override
    @Transactional
    public AdminAiPolicyResponse updatePolicy(Long id, AdminAiPolicyUpsertRequest request) {
        AiCapability capability = resolveCapability(request.getCapabilityCode());
        AiCapabilityPolicy policy = requirePolicy(id);
        AiCapabilityPolicy duplicate = aiCapabilityPolicyMapper.selectOne(
                new LambdaQueryWrapper<AiCapabilityPolicy>()
                        .eq(AiCapabilityPolicy::getPolicyCode, request.getPolicyCode().trim())
                        .ne(AiCapabilityPolicy::getId, id)
        );
        if (duplicate != null) {
            throw new BusinessException(4092, "AI 策略代碼已存在");
        }
        fillPolicy(policy, capability, request);
        aiCapabilityPolicyMapper.updateById(policy);
        replacePolicyBindings(policy.getId(), request.getProviderBindings());
        return listPolicies(request.getCapabilityCode()).stream()
                .filter(item -> Objects.equals(item.getId(), policy.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(5001, "AI 策略更新後回讀失敗"));
    }

    @Override
    @Transactional
    public void deletePolicy(Long id) {
        long templateRefs = aiPromptTemplateMapper.selectCount(
                new LambdaQueryWrapper<AiPromptTemplate>().eq(AiPromptTemplate::getDefaultPolicyId, id)
        );
        long jobRefs = aiGenerationJobMapper.selectCount(
                new LambdaQueryWrapper<AiGenerationJob>().eq(AiGenerationJob::getPolicyId, id)
        );
        if (templateRefs > 0 || jobRefs > 0) {
            throw new BusinessException(4093, "AI 策略仍被模板或生成任務引用，不能刪除");
        }
        aiPolicyProviderBindingMapper.delete(new LambdaQueryWrapper<AiPolicyProviderBinding>().eq(AiPolicyProviderBinding::getPolicyId, id));
        aiCapabilityPolicyMapper.deleteById(id);
    }

    @Override
    public List<AdminAiQuotaRuleResponse> listQuotaRules(String capabilityCode) {
        Long capabilityId = StringUtils.hasText(capabilityCode) ? resolveCapability(capabilityCode.trim()).getId() : null;
        Map<Long, AiCapability> capabilityMap = aiCapabilityMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiCapability::getId, item -> item, (left, right) -> left));
        return aiQuotaRuleMapper.selectList(
                        new LambdaQueryWrapper<AiQuotaRule>()
                                .eq(capabilityId != null, AiQuotaRule::getCapabilityId, capabilityId)
                                .orderByAsc(AiQuotaRule::getId)
                ).stream()
                .map(item -> mapQuotaRule(item, capabilityMap))
                .toList();
    }

    @Override
    @Transactional
    public AdminAiQuotaRuleResponse createQuotaRule(AdminAiQuotaRuleUpsertRequest request) {
        AiCapability capability = resolveCapability(request.getCapabilityCode());
        AiQuotaRule rule = new AiQuotaRule();
        fillQuotaRule(rule, capability, request);
        aiQuotaRuleMapper.insert(rule);
        return mapQuotaRule(rule, Map.of(capability.getId(), capability));
    }

    @Override
    @Transactional
    public AdminAiQuotaRuleResponse updateQuotaRule(Long id, AdminAiQuotaRuleUpsertRequest request) {
        AiQuotaRule rule = requireQuotaRule(id);
        AiCapability capability = resolveCapability(request.getCapabilityCode());
        fillQuotaRule(rule, capability, request);
        aiQuotaRuleMapper.updateById(rule);
        return mapQuotaRule(rule, Map.of(capability.getId(), capability));
    }

    @Override
    @Transactional
    public void deleteQuotaRule(Long id) {
        aiQuotaRuleMapper.deleteById(id);
    }

    @Override
    public PageResponse<AdminAiLogResponse> pageLogs(long pageNum, long pageSize, String capabilityCode, Integer success, Long providerId) {
        List<AiProviderConfig> providers = aiProviderConfigMapper.selectList(null);
        Map<Long, AiCapabilityPolicy> policyMap = aiCapabilityPolicyMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiCapabilityPolicy::getId, item -> item, (left, right) -> left));
        Page<AiRequestLog> page = aiRequestLogMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AiRequestLog>()
                        .eq(providerId != null, AiRequestLog::getProviderId, providerId)
                        .eq(success != null, AiRequestLog::getSuccess, success)
                        .eq(StringUtils.hasText(capabilityCode), AiRequestLog::getCapabilityCode, capabilityCode)
                        .orderByDesc(AiRequestLog::getCreatedAt)
                        .orderByDesc(AiRequestLog::getId)
        );
        Page<AdminAiLogResponse> mapped = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        mapped.setRecords(page.getRecords().stream().map(item -> mapLog(item, providers, policyMap)).toList());
        return PageResponse.of(mapped);
    }

    @Override
    public List<AdminAiPromptTemplateResponse> listPromptTemplates(String capabilityCode, String templateType) {
        Long capabilityId = StringUtils.hasText(capabilityCode) ? resolveCapability(capabilityCode.trim()).getId() : null;
        Map<Long, AiCapability> capabilityMap = aiCapabilityMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiCapability::getId, item -> item, (left, right) -> left));
        Map<Long, AiProviderConfig> providerMap = aiProviderConfigMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiProviderConfig::getId, item -> item, (left, right) -> left));
        Map<Long, AiCapabilityPolicy> policyMap = aiCapabilityPolicyMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiCapabilityPolicy::getId, item -> item, (left, right) -> left));
        return aiPromptTemplateMapper.selectList(
                        new LambdaQueryWrapper<AiPromptTemplate>()
                                .eq(capabilityId != null, AiPromptTemplate::getCapabilityId, capabilityId)
                                .eq(StringUtils.hasText(templateType), AiPromptTemplate::getTemplateType, templateType)
                                .orderByAsc(AiPromptTemplate::getSortOrder)
                                .orderByAsc(AiPromptTemplate::getId)
                ).stream()
                .map(item -> mapPromptTemplate(item, capabilityMap, providerMap, policyMap))
                .toList();
    }

    @Override
    @Transactional
    public AdminAiPromptTemplateResponse createPromptTemplate(AdminAiPromptTemplateUpsertRequest request) {
        AiCapability capability = resolveCapability(request.getCapabilityCode());
        AiPromptTemplate existing = aiPromptTemplateMapper.selectOne(
                new LambdaQueryWrapper<AiPromptTemplate>().eq(AiPromptTemplate::getTemplateCode, request.getTemplateCode().trim())
        );
        if (existing != null) {
            throw new BusinessException(4094, "AI 提示模板代碼已存在");
        }
        AiPromptTemplate template = new AiPromptTemplate();
        fillPromptTemplate(template, capability, request);
        aiPromptTemplateMapper.insert(template);
        return listPromptTemplates(request.getCapabilityCode(), null).stream()
                .filter(item -> Objects.equals(item.getId(), template.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(5001, "AI 模板建立後回讀失敗"));
    }

    @Override
    @Transactional
    public AdminAiPromptTemplateResponse updatePromptTemplate(Long id, AdminAiPromptTemplateUpsertRequest request) {
        AiCapability capability = resolveCapability(request.getCapabilityCode());
        AiPromptTemplate template = requirePromptTemplate(id);
        AiPromptTemplate duplicate = aiPromptTemplateMapper.selectOne(
                new LambdaQueryWrapper<AiPromptTemplate>()
                        .eq(AiPromptTemplate::getTemplateCode, request.getTemplateCode().trim())
                        .ne(AiPromptTemplate::getId, id)
        );
        if (duplicate != null) {
            throw new BusinessException(4094, "AI 提示模板代碼已存在");
        }
        fillPromptTemplate(template, capability, request);
        aiPromptTemplateMapper.updateById(template);
        return listPromptTemplates(request.getCapabilityCode(), null).stream()
                .filter(item -> Objects.equals(item.getId(), template.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(5001, "AI 模板更新後回讀失敗"));
    }

    @Override
    @Transactional
    public void deletePromptTemplate(Long id) {
        long jobRefs = aiGenerationJobMapper.selectCount(
                new LambdaQueryWrapper<AiGenerationJob>().eq(AiGenerationJob::getPromptTemplateId, id)
        );
        if (jobRefs > 0) {
            throw new BusinessException(4095, "AI 提示模板仍被生成任務引用，不能刪除");
        }
        aiPromptTemplateMapper.deleteById(id);
    }

    @Override
    public PageResponse<AdminAiGenerationJobResponse> pageGenerationJobs(long pageNum, long pageSize, String capabilityCode, String generationType, String jobStatus, Long currentAdminId, List<String> roles) {
        boolean superAdmin = isSuperAdmin(roles);
        Long capabilityId = StringUtils.hasText(capabilityCode) ? resolveCapability(capabilityCode).getId() : null;
        Page<AiGenerationJob> page = aiGenerationJobMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AiGenerationJob>()
                        .eq(capabilityId != null, AiGenerationJob::getCapabilityId, capabilityId)
                        .eq(StringUtils.hasText(generationType), AiGenerationJob::getGenerationType, generationType)
                        .eq(StringUtils.hasText(jobStatus), AiGenerationJob::getJobStatus, jobStatus)
                        .eq(!superAdmin && currentAdminId != null, AiGenerationJob::getOwnerAdminId, currentAdminId)
                        .orderByDesc(AiGenerationJob::getUpdatedAt)
                        .orderByDesc(AiGenerationJob::getId)
        );
        Page<AdminAiGenerationJobResponse> mapped = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        mapped.setRecords(toJobResponses(page.getRecords(), currentAdminId, roles));
        return PageResponse.of(mapped);
    }

    @Override
    public AdminAiGenerationJobResponse getGenerationJob(Long jobId, Long currentAdminId, List<String> roles) {
        AiGenerationJob job = requireJob(jobId);
        if (!canAccessJob(job, currentAdminId, roles)) {
            throw new BusinessException(4030, "沒有查看 AI 生成任務的權限");
        }
        return toJobResponses(List.of(job), currentAdminId, roles).stream().findFirst()
                .orElseThrow(() -> new BusinessException(4044, "AI 生成任務不存在"));
    }

    @Override
    @Transactional
    public AdminAiGenerationJobResponse createGenerationJob(AdminAiGenerationJobCreateRequest request, Long currentAdminId, String currentAdminName, List<String> roles) {
        if (currentAdminId == null) {
            throw new BusinessException(4010, "需要管理員身份才能建立 AI 生成任務");
        }
        AiCapability capability = resolveCapability(request.getCapabilityCode());
        AiPromptTemplate promptTemplate = request.getPromptTemplateId() == null ? null : requirePromptTemplate(request.getPromptTemplateId());
        AiCapabilityPolicy policy = resolvePolicy(capability, request.getPolicyId(), promptTemplate);
        ResolvedProvider resolvedProvider = resolveProvider(capability, policy, promptTemplate, request.getProviderId());
        String promptText = buildPromptText(request.getPromptText(), request.getPromptVariablesJson(), promptTemplate, policy);
        if (!StringUtils.hasText(promptText)) {
            throw new BusinessException(4055, "AI 提示詞不能為空");
        }

        List<AiQuotaRule> rules = loadQuotaRules(capability.getId(), policy == null ? null : policy.getId());
        try (AiGovernanceService.GovernanceLease lease = enforceQuotaRules(
                rules,
                capability.getCapabilityCode(),
                currentAdminId,
                estimateTokens(promptText)
        )) {
            AiGenerationJob job = new AiGenerationJob();
            job.setCapabilityId(capability.getId());
            job.setPolicyId(policy == null ? null : policy.getId());
            job.setPromptTemplateId(promptTemplate == null ? null : promptTemplate.getId());
            job.setProviderId(resolvedProvider.provider().getId());
            job.setInventoryId(resolvedProvider.inventory() == null ? null : resolvedProvider.inventory().getId());
            job.setProviderBindingId(resolvedProvider.binding() == null ? null : resolvedProvider.binding().getId());
            job.setOwnerAdminId(currentAdminId);
            job.setOwnerAdminName(StringUtils.hasText(currentAdminName) ? currentAdminName : "admin");
            job.setGenerationType(request.getGenerationType().trim());
            job.setSourceScope(request.getSourceScope());
            job.setSourceScopeId(request.getSourceScopeId());
            job.setJobStatus(JOB_STATUS_PENDING);
            job.setPromptTitle(request.getPromptTitle());
            job.setPromptText(promptText);
            job.setPromptVariablesJson(normalizeJson(request.getPromptVariablesJson()));
            job.setRequestPayloadJson(normalizeJson(request.getRequestPayloadJson()));
            aiGenerationJobMapper.insert(job);

            String traceId = UUID.randomUUID().toString().replace("-", "");
            try {
                executeGeneration(job, capability, policy, resolvedProvider, traceId);
            } catch (BusinessException ex) {
                job.setJobStatus(JOB_STATUS_FAILED);
                job.setErrorMessage(ex.getMessage());
                aiGenerationJobMapper.updateById(job);
                throw ex;
            }
            return getGenerationJob(job.getId(), currentAdminId, roles);
        }
    }

    @Override
    @Transactional
    public AdminAiGenerationJobResponse refreshGenerationJob(Long jobId, Long currentAdminId, List<String> roles) {
        AiGenerationJob job = requireJob(jobId);
        if (!canAccessJob(job, currentAdminId, roles)) {
            throw new BusinessException(4030, "沒有刷新 AI 生成任務的權限");
        }
        if (!"image".equalsIgnoreCase(job.getGenerationType()) || !StringUtils.hasText(job.getProviderRequestId())) {
            return getGenerationJob(jobId, currentAdminId, roles);
        }

        AiCapability capability = requireCapability(job.getCapabilityId());
        AiCapabilityPolicy policy = job.getPolicyId() == null ? null : requirePolicy(job.getPolicyId());
        AiProviderConfig provider = requireProvider(job.getProviderId());
        String apiKey = decryptRequiredApiKey(provider);
        DashScopeProviderGateway.ImagePollResult result = dashScopeProviderGateway.pollImageTask(provider, apiKey, job.getProviderRequestId());
        persistImagePollResult(job, result, UUID.randomUUID().toString().replace("-", ""));
        return getGenerationJob(jobId, currentAdminId, roles);
    }

    @Override
    @Transactional
    public AdminAiGenerationJobResponse finalizeCandidate(Long candidateId, AdminAiCandidateFinalizeRequest request, Long currentAdminId, String currentAdminName, List<String> roles) {
        AiGenerationCandidate candidate = requireCandidate(candidateId);
        AiGenerationJob job = requireJob(candidate.getJobId());
        if (!canAccessJob(job, currentAdminId, roles)) {
            throw new BusinessException(4030, "沒有確認 AI 候選資源的權限");
        }

        if (candidate.getFinalizedAssetId() == null) {
            ContentAsset asset = new ContentAsset();
            asset.setAssetKind(resolveAssetKind(request.getAssetKind(), candidate.getCandidateType()));
            asset.setBucketName(candidate.getStorageBucketName());
            asset.setRegion(candidate.getStorageRegion());
            asset.setObjectKey(candidate.getStorageObjectKey());
            asset.setCanonicalUrl(candidate.getStorageUrl());
            asset.setMimeType(candidate.getMimeType());
            asset.setLocaleCode(defaultString(request.getLocaleCode(), "zh-Hant"));
            asset.setOriginalFilename(resolveOriginalFilename(candidate.getStorageObjectKey()));
            asset.setFileExtension(resolveExtension(candidate.getStorageObjectKey()));
            asset.setUploadSource("api");
            asset.setUploadedByAdminId(currentAdminId);
            asset.setUploadedByAdminName(StringUtils.hasText(currentAdminName) ? currentAdminName : job.getOwnerAdminName());
            asset.setFileSizeBytes(candidate.getFileSizeBytes());
            asset.setWidthPx(candidate.getWidthPx());
            asset.setHeightPx(candidate.getHeightPx());
            asset.setProcessingPolicyCode("ai-generated");
            asset.setProcessingProfileJson(candidate.getMetadataJson());
            asset.setProcessingStatus("stored");
            asset.setProcessingNote("由 AI 生成候選結果轉為正式資源");
            asset.setStatus(defaultString(request.getStatus(), "draft"));
            asset.setPublishedAt("published".equalsIgnoreCase(asset.getStatus()) ? LocalDateTime.now() : null);
            contentAssetMapper.insert(asset);
            candidate.setFinalizedAssetId(asset.getId());
        }

        clearSelectedCandidates(job.getId());
        candidate.setIsSelected(1);
        candidate.setIsFinalized(1);
        aiGenerationCandidateMapper.updateById(candidate);
        job.setLatestCandidateId(candidate.getId());
        job.setFinalizedCandidateId(candidate.getId());
        job.setJobStatus(JOB_STATUS_COMPLETED);
        aiGenerationJobMapper.updateById(job);
        return getGenerationJob(job.getId(), currentAdminId, roles);
    }

    @Override
    @Transactional
    public AdminAiGenerationJobResponse restoreCandidate(Long candidateId, Long currentAdminId, List<String> roles) {
        AiGenerationCandidate candidate = requireCandidate(candidateId);
        AiGenerationJob job = requireJob(candidate.getJobId());
        if (!canAccessJob(job, currentAdminId, roles)) {
            throw new BusinessException(4030, "沒有切換 AI 候選版本的權限");
        }
        clearSelectedCandidates(job.getId());
        candidate.setIsSelected(1);
        aiGenerationCandidateMapper.updateById(candidate);
        job.setLatestCandidateId(candidate.getId());
        job.setResultSummary(defaultString(candidate.getPreviewText(), candidate.getStorageUrl()));
        aiGenerationJobMapper.updateById(job);
        return getGenerationJob(job.getId(), currentAdminId, roles);
    }

    @Override
    public AdminAiPlatformSettingsResponse getPlatformSettings() {
        return AdminAiPlatformSettingsResponse.builder()
                .inventoryFreshnessHours(readIntegerConfig(CONFIG_AI_INVENTORY_FRESHNESS_HOURS, 24))
                .syncHistoryLimit(readIntegerConfig(CONFIG_AI_SYNC_HISTORY_LIMIT, 12))
                .dailyCostAlertUsd(readDecimalConfig(CONFIG_AI_DAILY_COST_ALERT_USD, "80"))
                .providerFailureRateWarning(readDecimalConfig(CONFIG_AI_PROVIDER_FAILURE_RATE_WARNING, "0.20"))
                .recentWindowHours(readIntegerConfig(CONFIG_AI_RECENT_WINDOW_HOURS, 24))
                .allowOperatorGlobalHistory(readIntegerConfig(CONFIG_AI_ALLOW_OPERATOR_GLOBAL_HISTORY, 0))
                .build();
    }

    @Override
    @Transactional
    public AdminAiPlatformSettingsResponse updatePlatformSettings(AdminAiPlatformSettingsUpsertRequest request) {
        AdminAiPlatformSettingsResponse current = getPlatformSettings();
        int inventoryFreshnessHours = normalizePositiveInteger(request.getInventoryFreshnessHours(), current.getInventoryFreshnessHours());
        int syncHistoryLimit = normalizePositiveInteger(request.getSyncHistoryLimit(), current.getSyncHistoryLimit());
        int recentWindowHours = normalizePositiveInteger(request.getRecentWindowHours(), current.getRecentWindowHours());
        BigDecimal dailyCostAlertUsd = normalizePositiveDecimal(request.getDailyCostAlertUsd(), current.getDailyCostAlertUsd());
        BigDecimal providerFailureRateWarning = normalizeDecimalInRange(request.getProviderFailureRateWarning(), current.getProviderFailureRateWarning(), BigDecimal.ZERO, BigDecimal.ONE);
        int allowOperatorGlobalHistory = normalizeBooleanInt(request.getAllowOperatorGlobalHistory(), current.getAllowOperatorGlobalHistory());

        upsertConfig(CONFIG_AI_INVENTORY_FRESHNESS_HOURS, String.valueOf(inventoryFreshnessHours), "number", "AI inventory freshness warning threshold in hours");
        upsertConfig(CONFIG_AI_SYNC_HISTORY_LIMIT, String.valueOf(syncHistoryLimit), "number", "AI provider sync history retention count");
        upsertConfig(CONFIG_AI_DAILY_COST_ALERT_USD, dailyCostAlertUsd.stripTrailingZeros().toPlainString(), "number", "AI daily estimated cost alert threshold in USD");
        upsertConfig(CONFIG_AI_PROVIDER_FAILURE_RATE_WARNING, providerFailureRateWarning.stripTrailingZeros().toPlainString(), "number", "AI provider failure-rate warning threshold");
        upsertConfig(CONFIG_AI_RECENT_WINDOW_HOURS, String.valueOf(recentWindowHours), "number", "AI overview rolling recent window in hours");
        upsertConfig(CONFIG_AI_ALLOW_OPERATOR_GLOBAL_HISTORY, String.valueOf(allowOperatorGlobalHistory), "number", "Whether non-super admins can view global AI history");
        return getPlatformSettings();
    }

    private List<AdminAiOverviewResponse.Alert> buildOverviewAlerts(List<AiCapability> capabilities,
                                                                    List<AiProviderConfig> providers,
                                                                    List<AdminAiOverviewResponse.ProviderHealth> providerHealth,
                                                                    List<AiRequestLog> recentLogs) {
        List<AdminAiOverviewResponse.Alert> alerts = new ArrayList<>();
        if (providers.stream().noneMatch(item -> Objects.equals(item.getStatus(), 1))) {
            alerts.add(AdminAiOverviewResponse.Alert.builder().level("error").title("未啟用 AI 供應商")
                    .message("請至少配置一個可用的供應商與密鑰。").build());
        }
        long unhealthy = providerHealth.stream().filter(item -> !"healthy".equalsIgnoreCase(item.getHealthStatus())).count();
        if (unhealthy > 0) {
            alerts.add(AdminAiOverviewResponse.Alert.builder().level("warning").title("供應商健康狀態異常")
                    .message("目前有 " + unhealthy + " 個供應商未通過最近一次健康檢查。").build());
        }
        if (recentLogs.stream().filter(item -> !Objects.equals(item.getSuccess(), 1)).count() >= 5) {
            alerts.add(AdminAiOverviewResponse.Alert.builder().level("warning").title("近期失敗請求偏多")
                    .message("建議檢查策略、配置與上游供應商狀態。").build());
        }
        if (capabilities.stream().noneMatch(item -> STATUS_ENABLED.equalsIgnoreCase(item.getStatus()))) {
            alerts.add(AdminAiOverviewResponse.Alert.builder().level("error").title("AI 能力全部停用")
                    .message("目前沒有任何可用能力可供後台或小程序調用。").build());
        }
        return alerts;
    }

    private AdminAiCapabilityResponse mapCapability(AiCapability capability, List<AiRequestLog> logs) {
        long policyCount = aiCapabilityPolicyMapper.selectCount(
                new LambdaQueryWrapper<AiCapabilityPolicy>().eq(AiCapabilityPolicy::getCapabilityId, capability.getId())
        );
        return AdminAiCapabilityResponse.builder()
                .id(capability.getId())
                .domainCode(capability.getDomainCode())
                .capabilityCode(capability.getCapabilityCode())
                .displayNameZht(capability.getDisplayNameZht())
                .summaryZht(capability.getSummaryZht())
                .supportsPublicRuntime(capability.getSupportsPublicRuntime())
                .supportsAdminCreative(capability.getSupportsAdminCreative())
                .supportsText(capability.getSupportsText())
                .supportsImage(capability.getSupportsImage())
                .supportsAudio(capability.getSupportsAudio())
                .supportsVision(capability.getSupportsVision())
                .status(capability.getStatus())
                .sortOrder(capability.getSortOrder())
                .policyCount((int) policyCount)
                .requestCount24h((long) logs.size())
                .failedCount24h(logs.stream().filter(item -> !Objects.equals(item.getSuccess(), 1)).count())
                .fallbackCount24h(logs.stream().filter(item -> Objects.equals(item.getFallbackTriggered(), 1)).count())
                .build();
    }

    private AdminAiProviderResponse mapProvider(AiProviderConfig provider) {
        return AdminAiProviderResponse.builder()
                .id(provider.getId())
                .providerName(provider.getProviderName())
                .platformCode(provider.getPlatformCode())
                .displayName(provider.getDisplayName())
                .platformLabel(provider.getPlatformLabel())
                .providerType(provider.getProviderType())
                .endpointStyle(provider.getEndpointStyle())
                .syncStrategy(provider.getSyncStrategy())
                .authScheme(provider.getAuthScheme())
                .apiBaseUrl(provider.getApiBaseUrl())
                .docsUrl(provider.getDocsUrl())
                .modelName(provider.getModelName())
                .capabilityCodes(parseCapabilityCodes(provider.getCapabilities()))
                .featureFlagsJson(provider.getFeatureFlagsJson())
                .credentialSchemaJson(provider.getCredentialSchemaJson())
                .providerSettingsJson(provider.getProviderSettingsJson())
                .hasApiKey(StringUtils.hasText(provider.getApiKeyEncrypted()) ? 1 : 0)
                .hasApiSecret(StringUtils.hasText(provider.getApiSecretEncrypted()) ? 1 : 0)
                .apiKeyMasked(provider.getApiKeyMasked())
                .apiSecretMasked(provider.getApiSecretMasked())
                .requestTimeoutMs(provider.getRequestTimeoutMs())
                .maxRetries(provider.getMaxRetries())
                .quotaDaily(provider.getQuotaDaily())
                .costPer1kTokens(provider.getCostPer1kTokens())
                .status(provider.getStatus())
                .healthStatus(provider.getHealthStatus())
                .healthMessage(provider.getHealthMessage())
                .lastInventorySyncStatus(provider.getLastInventorySyncStatus())
                .lastInventorySyncMessage(provider.getLastInventorySyncMessage())
                .lastInventorySyncedAt(provider.getLastInventorySyncedAt())
                .inventoryRecordCount(provider.getInventoryRecordCount())
                .lastHealthCheckedAt(provider.getLastHealthCheckedAt())
                .lastSuccessAt(provider.getLastSuccessAt())
                .lastFailureAt(provider.getLastFailureAt())
                .secretUpdatedAt(provider.getSecretUpdatedAt())
                .build();
    }

    private AdminAiPolicyResponse mapPolicy(AiCapabilityPolicy policy, Map<Long, AiCapability> capabilityMap,
                                            Map<Long, AiProviderConfig> providerMap,
                                            Map<Long, AiProviderInventory> inventoryMap,
                                            List<AiPolicyProviderBinding> bindings) {
        AiCapability capability = capabilityMap.get(policy.getCapabilityId());
        return AdminAiPolicyResponse.builder()
                .id(policy.getId())
                .capabilityId(policy.getCapabilityId())
                .capabilityCode(capability == null ? null : capability.getCapabilityCode())
                .capabilityNameZht(capability == null ? null : capability.getDisplayNameZht())
                .policyCode(policy.getPolicyCode())
                .policyName(policy.getPolicyName())
                .policyType(policy.getPolicyType())
                .executionMode(policy.getExecutionMode())
                .responseMode(policy.getResponseMode())
                .scenePresetCode(policy.getScenePresetCode())
                .manualSwitchProviderId(policy.getManualSwitchProviderId())
                .manualSwitchProviderName(policy.getManualSwitchProviderId() == null ? null : providerMap.get(policy.getManualSwitchProviderId()) == null ? null : providerMap.get(policy.getManualSwitchProviderId()).getDisplayName())
                .defaultModel(policy.getDefaultModel())
                .multimodalEnabled(policy.getMultimodalEnabled())
                .voiceEnabled(policy.getVoiceEnabled())
                .structuredOutputEnabled(policy.getStructuredOutputEnabled())
                .temperature(policy.getTemperature())
                .maxTokens(policy.getMaxTokens())
                .responseSchemaJson(policy.getResponseSchemaJson())
                .postProcessRulesJson(policy.getPostProcessRulesJson())
                .parameterConfigJson(policy.getParameterConfigJson())
                .expertOverrideJson(policy.getExpertOverrideJson())
                .status(policy.getStatus())
                .sortOrder(policy.getSortOrder())
                .notes(policy.getNotes())
                .providerBindings(bindings.stream()
                        .sorted(Comparator.comparing(AiPolicyProviderBinding::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                        .map(item -> AdminAiProviderBindingResponse.builder()
                                .id(item.getId())
                                .providerId(item.getProviderId())
                                .inventoryId(item.getInventoryId())
                                .inventoryCode(item.getInventoryId() == null ? null : inventoryMap.get(item.getInventoryId()) == null ? null : inventoryMap.get(item.getInventoryId()).getInventoryCode())
                                .inventoryDisplayName(item.getInventoryId() == null ? null : inventoryMap.get(item.getInventoryId()) == null ? null : inventoryMap.get(item.getInventoryId()).getDisplayName())
                                .providerName(providerMap.get(item.getProviderId()) == null ? null : providerMap.get(item.getProviderId()).getProviderName())
                                .providerDisplayName(providerMap.get(item.getProviderId()) == null ? null : providerMap.get(item.getProviderId()).getDisplayName())
                                .bindingRole(item.getBindingRole())
                                .routeMode(item.getRouteMode())
                                .sortOrder(item.getSortOrder())
                                .enabled(item.getEnabled())
                                .modelOverride(item.getModelOverride())
                                .weightPercent(item.getWeightPercent())
                                .timeoutMsOverride(item.getTimeoutMsOverride())
                                .retryCountOverride(item.getRetryCountOverride())
                                .parameterOverrideJson(item.getParameterOverrideJson())
                                .notes(item.getNotes())
                                .build())
                        .toList())
                .build();
    }

    private AdminAiQuotaRuleResponse mapQuotaRule(AiQuotaRule rule, Map<Long, AiCapability> capabilityMap) {
        AiCapability capability = capabilityMap.get(rule.getCapabilityId());
        return AdminAiQuotaRuleResponse.builder()
                .id(rule.getId())
                .capabilityId(rule.getCapabilityId())
                .capabilityCode(capability == null ? null : capability.getCapabilityCode())
                .capabilityNameZht(capability == null ? null : capability.getDisplayNameZht())
                .policyId(rule.getPolicyId())
                .scopeType(rule.getScopeType())
                .scopeValue(rule.getScopeValue())
                .windowType(rule.getWindowType())
                .windowSize(rule.getWindowSize())
                .requestLimit(rule.getRequestLimit())
                .tokenLimit(rule.getTokenLimit())
                .suspiciousConcurrencyThreshold(rule.getSuspiciousConcurrencyThreshold())
                .actionMode(rule.getActionMode())
                .status(rule.getStatus())
                .notes(rule.getNotes())
                .build();
    }

    private AdminAiPromptTemplateResponse mapPromptTemplate(AiPromptTemplate template, Map<Long, AiCapability> capabilityMap,
                                                            Map<Long, AiProviderConfig> providerMap, Map<Long, AiCapabilityPolicy> policyMap) {
        AiCapability capability = capabilityMap.get(template.getCapabilityId());
        return AdminAiPromptTemplateResponse.builder()
                .id(template.getId())
                .capabilityId(template.getCapabilityId())
                .capabilityCode(capability == null ? null : capability.getCapabilityCode())
                .capabilityNameZht(capability == null ? null : capability.getDisplayNameZht())
                .templateCode(template.getTemplateCode())
                .templateName(template.getTemplateName())
                .templateType(template.getTemplateType())
                .assetSlotCode(template.getAssetSlotCode())
                .systemPrompt(template.getSystemPrompt())
                .promptTemplate(template.getPromptTemplate())
                .variableSchemaJson(template.getVariableSchemaJson())
                .outputConstraintsJson(template.getOutputConstraintsJson())
                .defaultProviderId(template.getDefaultProviderId())
                .defaultProviderName(template.getDefaultProviderId() == null ? null : providerMap.get(template.getDefaultProviderId()) == null ? null : providerMap.get(template.getDefaultProviderId()).getDisplayName())
                .defaultPolicyId(template.getDefaultPolicyId())
                .defaultPolicyName(template.getDefaultPolicyId() == null ? null : policyMap.get(template.getDefaultPolicyId()) == null ? null : policyMap.get(template.getDefaultPolicyId()).getPolicyName())
                .status(template.getStatus())
                .sortOrder(template.getSortOrder())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    private void fillProvider(AiProviderConfig provider, AdminAiProviderUpsertRequest request, boolean creating) {
        provider.setProviderName(request.getProviderName().trim());
        provider.setDisplayName(request.getDisplayName().trim());
        AiProviderTemplateRegistry.AiProviderTemplateDefinition template = resolveProviderTemplate(request.getPlatformCode());
        String platformCode = defaultString(request.getPlatformCode(), template == null ? "custom" : template.getPlatformCode());
        provider.setPlatformCode(platformCode);
        provider.setPlatformLabel(defaultString(request.getPlatformLabel(), template == null ? request.getDisplayName() : template.getPlatformLabel()));
        provider.setProviderType(defaultString(request.getProviderType(), template == null ? "custom" : template.getProviderType()));
        provider.setEndpointStyle(defaultString(request.getEndpointStyle(), template == null ? "openai_compatible" : template.getEndpointStyle()));
        provider.setSyncStrategy(defaultString(request.getSyncStrategy(), template == null ? "manual" : template.getSyncStrategy()));
        provider.setAuthScheme(defaultString(request.getAuthScheme(), template == null ? "bearer_key" : template.getAuthScheme()));
        provider.setApiBaseUrl(aiOutboundUrlGuard.normalizeConfiguredBaseUrl(
                platformCode,
                defaultString(request.getApiBaseUrl(), template == null ? "" : template.getDefaultBaseUrl())
        ));
        provider.setDocsUrl(defaultString(request.getDocsUrl(), template == null ? "" : template.getDocsUrl()));
        provider.setModelName(defaultString(request.getModelName(), template == null ? "" : template.getDefaultModelName()));
        provider.setCapabilities(writeJson(parseStringList(request.getCapabilityCodes())));
        provider.setFeatureFlagsJson(normalizeJson(request.getFeatureFlagsJson()));
        provider.setCredentialSchemaJson(normalizeJson(defaultString(request.getCredentialSchemaJson(),
                template == null ? null : writeJson(template.getCredentialFields()))));
        provider.setProviderSettingsJson(normalizeJson(request.getProviderSettingsJson()));
        provider.setRequestTimeoutMs(request.getRequestTimeoutMs() == null ? aiCapabilityProperties.getRequestTimeoutMs() : request.getRequestTimeoutMs());
        provider.setMaxRetries(request.getMaxRetries() == null ? 1 : request.getMaxRetries());
        provider.setQuotaDaily(request.getQuotaDaily());
        provider.setCostPer1kTokens(request.getCostPer1kTokens());
        provider.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        provider.setHealthStatus(defaultString(provider.getHealthStatus(), "unknown"));
        provider.setLastInventorySyncStatus(defaultString(provider.getLastInventorySyncStatus(), STATUS_IDLE));
        provider.setInventoryRecordCount(provider.getInventoryRecordCount() == null ? 0 : provider.getInventoryRecordCount());

        boolean replaceApiKey = creating || Boolean.TRUE.equals(request.getReplaceApiKey()) || StringUtils.hasText(request.getApiKey());
        if (replaceApiKey) {
            provider.setApiKeyEncrypted(aiSecretCryptoService.encrypt(request.getApiKey()));
            provider.setApiKeyMasked(aiSecretCryptoService.mask(request.getApiKey()));
            provider.setSecretUpdatedAt(LocalDateTime.now());
        }
        boolean replaceApiSecret = creating || Boolean.TRUE.equals(request.getReplaceApiSecret()) || StringUtils.hasText(request.getApiSecret());
        if (replaceApiSecret) {
            provider.setApiSecretEncrypted(aiSecretCryptoService.encrypt(request.getApiSecret()));
            provider.setApiSecretMasked(aiSecretCryptoService.mask(request.getApiSecret()));
            provider.setSecretUpdatedAt(LocalDateTime.now());
        }
    }

    private void fillPolicy(AiCapabilityPolicy policy, AiCapability capability, AdminAiPolicyUpsertRequest request) {
        policy.setCapabilityId(capability.getId());
        policy.setPolicyCode(request.getPolicyCode().trim());
        policy.setPolicyName(request.getPolicyName().trim());
        policy.setPolicyType(defaultString(request.getPolicyType(), "default"));
        policy.setExecutionMode(defaultString(request.getExecutionMode(), "auto"));
        policy.setResponseMode(defaultString(request.getResponseMode(), "structured"));
        policy.setScenePresetCode(request.getScenePresetCode());
        policy.setDefaultModel(request.getDefaultModel());
        policy.setSystemPrompt(request.getSystemPrompt());
        policy.setPromptTemplate(request.getPromptTemplate());
        policy.setResponseSchemaJson(normalizeJson(request.getResponseSchemaJson()));
        policy.setPostProcessRulesJson(normalizeJson(request.getPostProcessRulesJson()));
        policy.setParameterConfigJson(normalizeJson(request.getParameterConfigJson()));
        policy.setExpertOverrideJson(normalizeJson(request.getExpertOverrideJson()));
        policy.setManualSwitchProviderId(request.getManualSwitchProviderId());
        policy.setMultimodalEnabled(request.getMultimodalEnabled() == null ? 0 : request.getMultimodalEnabled());
        policy.setVoiceEnabled(request.getVoiceEnabled() == null ? 0 : request.getVoiceEnabled());
        policy.setStructuredOutputEnabled(request.getStructuredOutputEnabled() == null ? 0 : request.getStructuredOutputEnabled());
        policy.setTemperature(request.getTemperature());
        policy.setMaxTokens(request.getMaxTokens());
        policy.setStatus(defaultString(request.getStatus(), STATUS_ENABLED));
        policy.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        policy.setNotes(request.getNotes());
    }

    private void fillQuotaRule(AiQuotaRule rule, AiCapability capability, AdminAiQuotaRuleUpsertRequest request) {
        rule.setCapabilityId(capability.getId());
        rule.setPolicyId(request.getPolicyId());
        rule.setScopeType(defaultString(request.getScopeType(), "global"));
        rule.setScopeValue(request.getScopeValue());
        rule.setWindowType(defaultString(request.getWindowType(), "minute"));
        rule.setWindowSize(request.getWindowSize() == null ? 1 : request.getWindowSize());
        rule.setRequestLimit(request.getRequestLimit());
        rule.setTokenLimit(request.getTokenLimit());
        rule.setSuspiciousConcurrencyThreshold(request.getSuspiciousConcurrencyThreshold());
        rule.setActionMode(defaultString(request.getActionMode(), "throttle"));
        rule.setStatus(defaultString(request.getStatus(), STATUS_ENABLED));
        rule.setNotes(request.getNotes());
    }

    private void fillPromptTemplate(AiPromptTemplate template, AiCapability capability, AdminAiPromptTemplateUpsertRequest request) {
        template.setCapabilityId(capability.getId());
        template.setTemplateCode(request.getTemplateCode().trim());
        template.setTemplateName(request.getTemplateName().trim());
        template.setTemplateType(defaultString(request.getTemplateType(), "text"));
        template.setAssetSlotCode(request.getAssetSlotCode());
        template.setSystemPrompt(request.getSystemPrompt());
        template.setPromptTemplate(request.getPromptTemplate());
        template.setVariableSchemaJson(normalizeJson(request.getVariableSchemaJson()));
        template.setOutputConstraintsJson(normalizeJson(request.getOutputConstraintsJson()));
        template.setDefaultProviderId(request.getDefaultProviderId());
        template.setDefaultPolicyId(request.getDefaultPolicyId());
        template.setStatus(defaultString(request.getStatus(), STATUS_ENABLED));
        template.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
    }

    private AdminAiProviderSyncJobResponse mapSyncJob(AiProviderSyncJob job, AiProviderConfig provider) {
        return AdminAiProviderSyncJobResponse.builder()
                .id(job.getId())
                .providerId(job.getProviderId())
                .providerName(provider == null ? null : provider.getDisplayName())
                .platformCode(job.getPlatformCode())
                .syncStrategy(job.getSyncStrategy())
                .jobStatus(job.getJobStatus())
                .message(job.getMessage())
                .errorDetail(job.getErrorDetail())
                .discoveredCount(job.getDiscoveredCount())
                .createdCount(job.getCreatedCount())
                .updatedCount(job.getUpdatedCount())
                .staleCount(job.getStaleCount())
                .rawPayloadJson(job.getRawPayloadJson())
                .startedAt(job.getStartedAt())
                .finishedAt(job.getFinishedAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }

    private AdminAiInventoryResponse mapInventory(AiProviderInventory inventory, AiProviderConfig provider) {
        return AdminAiInventoryResponse.builder()
                .id(inventory.getId())
                .providerId(inventory.getProviderId())
                .providerName(provider == null ? null : provider.getProviderName())
                .providerDisplayName(provider == null ? null : provider.getDisplayName())
                .providerPlatformCode(provider == null ? null : provider.getPlatformCode())
                .inventoryCode(inventory.getInventoryCode())
                .externalId(inventory.getExternalId())
                .displayName(inventory.getDisplayName())
                .inventoryType(inventory.getInventoryType())
                .modalityCodes(readJsonList(inventory.getModalityCodesJson()))
                .capabilityCodes(readJsonList(inventory.getCapabilityCodesJson()))
                .syncStrategy(inventory.getSyncStrategy())
                .sourceType(inventory.getSourceType())
                .availabilityStatus(inventory.getAvailabilityStatus())
                .endpointPath(inventory.getEndpointPath())
                .contextWindowTokens(inventory.getContextWindowTokens())
                .inputPricePer1k(inventory.getInputPricePer1k())
                .outputPricePer1k(inventory.getOutputPricePer1k())
                .imagePricePerCall(inventory.getImagePricePerCall())
                .audioPricePerMinute(inventory.getAudioPricePerMinute())
                .featureFlagsJson(inventory.getFeatureFlagsJson())
                .rawPayloadJson(inventory.getRawPayloadJson())
                .isDefault(inventory.getIsDefault())
                .sortOrder(inventory.getSortOrder())
                .lastSeenAt(inventory.getLastSeenAt())
                .syncedAt(inventory.getSyncedAt())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    private void fillInventory(AiProviderInventory inventory, AdminAiInventoryUpsertRequest request) {
        inventory.setProviderId(request.getProviderId());
        inventory.setInventoryCode(request.getInventoryCode().trim());
        inventory.setExternalId(request.getExternalId().trim());
        inventory.setDisplayName(request.getDisplayName().trim());
        inventory.setInventoryType(defaultString(request.getInventoryType(), "model"));
        inventory.setModalityCodesJson(writeJson(parseStringList(request.getModalityCodes())));
        inventory.setCapabilityCodesJson(writeJson(parseStringList(request.getCapabilityCodes())));
        inventory.setSyncStrategy(defaultString(request.getSyncStrategy(), "manual"));
        inventory.setSourceType(defaultString(request.getSourceType(), "manual"));
        inventory.setAvailabilityStatus(defaultString(request.getAvailabilityStatus(), "available"));
        inventory.setEndpointPath(request.getEndpointPath());
        inventory.setContextWindowTokens(request.getContextWindowTokens());
        inventory.setInputPricePer1k(request.getInputPricePer1k());
        inventory.setOutputPricePer1k(request.getOutputPricePer1k());
        inventory.setImagePricePerCall(request.getImagePricePerCall());
        inventory.setAudioPricePerMinute(request.getAudioPricePerMinute());
        inventory.setFeatureFlagsJson(normalizeJson(request.getFeatureFlagsJson()));
        inventory.setRawPayloadJson(normalizeJson(request.getRawPayloadJson()));
        inventory.setIsDefault(request.getIsDefault() == null ? 0 : request.getIsDefault());
        inventory.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
    }

    private InventorySyncResult synchronizeInventory(AiProviderConfig provider) {
        AiProviderTemplateRegistry.AiProviderTemplateDefinition template = resolveProviderTemplate(provider.getPlatformCode());
        List<DiscoveredInventoryRecord> discovered = filterDiscoveredInventoryRecords(
                provider,
                discoverInventoryRecords(provider, template)
        );
        LocalDateTime syncedAt = LocalDateTime.now();
        List<AiProviderInventory> existing = aiProviderInventoryMapper.selectList(
                new LambdaQueryWrapper<AiProviderInventory>()
                        .eq(AiProviderInventory::getProviderId, provider.getId())
                        .orderByAsc(AiProviderInventory::getSortOrder)
                        .orderByAsc(AiProviderInventory::getId)
        );
        Map<String, AiProviderInventory> existingByCode = existing.stream()
                .collect(Collectors.toMap(AiProviderInventory::getInventoryCode, item -> item, (left, right) -> left));
        Set<String> activeCodes = new HashSet<>();
        int createdCount = 0;
        int updatedCount = 0;

        for (DiscoveredInventoryRecord item : discovered) {
            activeCodes.add(item.inventoryCode());
            AiProviderInventory inventory = existingByCode.get(item.inventoryCode());
            boolean creating = inventory == null;
            if (creating) {
                inventory = new AiProviderInventory();
                inventory.setProviderId(provider.getId());
            }
            inventory.setInventoryCode(item.inventoryCode());
            inventory.setExternalId(item.externalId());
            inventory.setDisplayName(item.displayName());
            inventory.setInventoryType(item.inventoryType());
            inventory.setModalityCodesJson(writeJson(item.modalityCodes()));
            inventory.setCapabilityCodesJson(writeJson(item.capabilityCodes()));
            inventory.setSyncStrategy(defaultString(provider.getSyncStrategy(), "manual"));
            inventory.setSourceType(item.sourceType());
            inventory.setAvailabilityStatus("available");
            inventory.setEndpointPath(item.endpointPath());
            inventory.setContextWindowTokens(item.contextWindowTokens());
            inventory.setInputPricePer1k(item.inputPricePer1k());
            inventory.setOutputPricePer1k(item.outputPricePer1k());
            inventory.setImagePricePerCall(item.imagePricePerCall());
            inventory.setAudioPricePerMinute(item.audioPricePerMinute());
            inventory.setFeatureFlagsJson(item.featureFlagsJson());
            inventory.setRawPayloadJson(item.rawPayloadJson());
            inventory.setIsDefault(item.isDefault());
            inventory.setSortOrder(item.sortOrder());
            inventory.setLastSeenAt(syncedAt);
            inventory.setSyncedAt(syncedAt);
            if (creating) {
                aiProviderInventoryMapper.insert(inventory);
                createdCount++;
            } else {
                aiProviderInventoryMapper.updateById(inventory);
                updatedCount++;
            }
        }

        int staleCount = 0;
        for (AiProviderInventory item : existing) {
            if (!activeCodes.contains(item.getInventoryCode())) {
                item.setAvailabilityStatus("stale");
                item.setIsDefault(0);
                item.setSyncedAt(syncedAt);
                aiProviderInventoryMapper.updateById(item);
                staleCount++;
            }
        }

        normalizeProviderInventoryDefaults(provider);
        refreshProviderInventoryCount(provider.getId());
        int totalRecords = countProviderAvailableInventory(provider.getId());
        String message = discovered.isEmpty()
                ? "未同步到可用模型，已保留現有模型庫。"
                : "模型庫同步完成，共整理 " + totalRecords + " 個可用項目";
        return new InventorySyncResult(
                discovered.size(),
                createdCount,
                updatedCount,
                staleCount,
                totalRecords,
                message,
                writeJson(discovered.stream().map(DiscoveredInventoryRecord::inventoryCode).toList())
        );
    }

    private List<DiscoveredInventoryRecord> discoverInventoryRecords(AiProviderConfig provider,
                                                                     AiProviderTemplateRegistry.AiProviderTemplateDefinition template) {
        String strategy = defaultString(provider.getSyncStrategy(), template == null ? "manual" : template.getSyncStrategy());
        if ("list_api".equalsIgnoreCase(strategy)) {
            return discoverViaModelList(provider);
        }
        if ("hybrid_list_or_catalog".equalsIgnoreCase(strategy)) {
            try {
                List<DiscoveredInventoryRecord> records = discoverViaModelList(provider);
                if (!records.isEmpty()) {
                    return records;
                }
            } catch (BusinessException ignored) {
                // fall through to catalog
            }
            return discoverViaTemplateCatalog(provider, template);
        }
        if ("documented_catalog".equalsIgnoreCase(strategy) || "endpoint_discovery".equalsIgnoreCase(strategy)) {
            return discoverViaTemplateCatalog(provider, template);
        }
        if ("manual".equalsIgnoreCase(strategy)) {
            return aiProviderInventoryMapper.selectList(
                            new LambdaQueryWrapper<AiProviderInventory>()
                                    .eq(AiProviderInventory::getProviderId, provider.getId())
                                    .orderByAsc(AiProviderInventory::getSortOrder)
                                    .orderByAsc(AiProviderInventory::getId)
                    ).stream()
                    .map(item -> new DiscoveredInventoryRecord(
                            item.getInventoryCode(),
                            item.getExternalId(),
                            item.getDisplayName(),
                            item.getInventoryType(),
                            readJsonList(item.getModalityCodesJson()),
                            readJsonList(item.getCapabilityCodesJson()),
                            defaultString(item.getSourceType(), "manual"),
                            item.getEndpointPath(),
                            item.getContextWindowTokens(),
                            item.getInputPricePer1k(),
                            item.getOutputPricePer1k(),
                            item.getImagePricePerCall(),
                            item.getAudioPricePerMinute(),
                            item.getFeatureFlagsJson(),
                            item.getRawPayloadJson(),
                            item.getIsDefault() == null ? 0 : item.getIsDefault(),
                            item.getSortOrder() == null ? 0 : item.getSortOrder()
                    ))
                    .toList();
        }
        return discoverViaTemplateCatalog(provider, template);
    }

    private List<DiscoveredInventoryRecord> discoverViaModelList(AiProviderConfig provider) {
        String apiKey = decryptRequiredApiKey(provider);
        return dashScopeProviderGateway.listModels(provider, apiKey, provider.getRequestTimeoutMs()).stream()
                .map(item -> new DiscoveredInventoryRecord(
                        sanitizeInventoryCode(item.id()),
                        item.id(),
                        defaultString(item.displayName(), item.id()),
                        "model",
                        inferModalities(item.id()),
                        inferCapabilities(item.id(), inferModalities(item.id())),
                        "api",
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        item.rawPayloadJson(),
                        0,
                        0
                ))
                .toList();
    }

    private List<DiscoveredInventoryRecord> discoverViaTemplateCatalog(AiProviderConfig provider,
                                                                       AiProviderTemplateRegistry.AiProviderTemplateDefinition template) {
        if (template == null || template.getInventorySeeds() == null) {
            return List.of();
        }
        return template.getInventorySeeds().stream()
                .map(item -> new DiscoveredInventoryRecord(
                        sanitizeInventoryCode(item.getInventoryCode()),
                        item.getExternalId(),
                        item.getDisplayName(),
                        item.getInventoryType(),
                        item.getModalityCodes(),
                        item.getCapabilityCodes(),
                        defaultString(item.getSourceType(), "documentation"),
                        resolveEndpointPath(provider, item),
                        item.getContextWindowTokens(),
                        item.getInputPricePer1k(),
                        item.getOutputPricePer1k(),
                        item.getImagePricePerCall(),
                        item.getAudioPricePerMinute(),
                        null,
                        writeJson(Map.of("platformCode", provider.getPlatformCode(), "templateInventoryCode", item.getInventoryCode())),
                        item.getIsDefault() == null ? 0 : item.getIsDefault(),
                        item.getSortOrder() == null ? 0 : item.getSortOrder()
                ))
                .toList();
    }

    private List<DiscoveredInventoryRecord> filterDiscoveredInventoryRecords(AiProviderConfig provider,
                                                                             List<DiscoveredInventoryRecord> discovered) {
        List<String> providerCapabilities = parseCapabilityCodes(provider.getCapabilities());
        Set<String> providerModalities = resolveProviderModalities(provider, providerCapabilities);
        if (providerCapabilities.isEmpty() && providerModalities.isEmpty()) {
            return discovered;
        }
        return discovered.stream()
                .filter(item -> providerCapabilities.isEmpty()
                        || item.capabilityCodes() == null
                        || item.capabilityCodes().isEmpty()
                        || item.capabilityCodes().stream().anyMatch(providerCapabilities::contains))
                .filter(item -> providerModalities.isEmpty()
                        || item.modalityCodes() == null
                        || item.modalityCodes().isEmpty()
                        || item.modalityCodes().stream().anyMatch(providerModalities::contains))
                .toList();
    }

    private Set<String> resolveProviderModalities(AiProviderConfig provider, List<String> providerCapabilities) {
        String endpointStyle = defaultString(provider.getEndpointStyle(), "").toLowerCase(Locale.ROOT);
        if ("dashscope_image".equals(endpointStyle)) {
            return Set.of("image");
        }
        if ("dashscope_tts".equals(endpointStyle)) {
            return Set.of("audio");
        }
        if (providerCapabilities == null || providerCapabilities.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> modalities = new LinkedHashSet<>();
        for (String capabilityCode : providerCapabilities) {
            switch (defaultString(capabilityCode, "")) {
                case "admin_image_generation" -> modalities.add("image");
                case "admin_tts_generation" -> modalities.add("audio");
                case "photo_positioning" -> {
                    modalities.add("text");
                    modalities.add("vision");
                }
                default -> modalities.add("text");
            }
        }
        return modalities;
    }

    private String resolveEndpointPath(AiProviderConfig provider, AiProviderTemplateRegistry.AiInventorySeed item) {
        if (!"endpoint".equalsIgnoreCase(item.getInventoryType())) {
            return null;
        }
        String base = defaultString(provider.getApiBaseUrl(), "");
        return StringUtils.hasText(base) ? base.replaceAll("/+$", "") + "/deployments/" + item.getExternalId() : item.getExternalId();
    }

    private AiProviderInventory resolvePreferredInventory(Long providerId, String capabilityCode, String assetSlotCode) {
        return resolvePreferredInventory(providerId, capabilityCode, assetSlotCode, null);
    }

    private AiProviderInventory resolvePreferredInventory(Long providerId,
                                                          String capabilityCode,
                                                          String assetSlotCode,
                                                          String preferredModel) {
        List<AiProviderInventory> inventories = aiProviderInventoryMapper.selectList(
                new LambdaQueryWrapper<AiProviderInventory>()
                        .eq(AiProviderInventory::getProviderId, providerId)
                        .eq(AiProviderInventory::getAvailabilityStatus, "available")
                        .orderByDesc(AiProviderInventory::getIsDefault)
                        .orderByAsc(AiProviderInventory::getSortOrder)
                        .orderByAsc(AiProviderInventory::getId)
        );
        if (inventories.isEmpty()) {
            return null;
        }
        String desiredModality = resolveAssetSlotModality(assetSlotCode);
        List<AiProviderInventory> filtered = inventories.stream()
                .filter(item -> capabilityCode == null || readJsonList(item.getCapabilityCodesJson()).isEmpty()
                        || readJsonList(item.getCapabilityCodesJson()).contains(capabilityCode))
                .filter(item -> desiredModality == null || readJsonList(item.getModalityCodesJson()).isEmpty()
                        || readJsonList(item.getModalityCodesJson()).contains(desiredModality))
                .toList();
        if (filtered.isEmpty()) {
            filtered = inventories;
        }
        String resolvedPreferredModel = StringUtils.hasText(preferredModel)
                ? preferredModel.trim()
                : resolveProviderModelName(providerId);
        AiProviderInventory preferred = findInventoryByModel(filtered, resolvedPreferredModel);
        return preferred != null ? preferred : filtered.get(0);
    }

    private String resolveInventoryCode(Long inventoryId) {
        if (inventoryId == null) {
            return null;
        }
        AiProviderInventory inventory = aiProviderInventoryMapper.selectById(inventoryId);
        return inventory == null ? null : inventory.getInventoryCode();
    }

    private int countProviderAvailableInventory(Long providerId) {
        return Math.toIntExact(aiProviderInventoryMapper.selectCount(
                new LambdaQueryWrapper<AiProviderInventory>()
                        .eq(AiProviderInventory::getProviderId, providerId)
                        .eq(AiProviderInventory::getAvailabilityStatus, "available")
        ));
    }

    private void normalizeProviderInventoryDefaults(AiProviderConfig provider) {
        if (provider == null || provider.getId() == null) {
            return;
        }
        List<AiProviderInventory> availableInventories = aiProviderInventoryMapper.selectList(
                new LambdaQueryWrapper<AiProviderInventory>()
                        .eq(AiProviderInventory::getProviderId, provider.getId())
                        .eq(AiProviderInventory::getAvailabilityStatus, "available")
                        .orderByDesc(AiProviderInventory::getIsDefault)
                        .orderByAsc(AiProviderInventory::getSortOrder)
                        .orderByAsc(AiProviderInventory::getId)
        );
        if (availableInventories.isEmpty()) {
            return;
        }
        AiProviderInventory preferred = findInventoryByModel(availableInventories, provider.getModelName());
        if (preferred == null) {
            preferred = availableInventories.stream()
                    .filter(item -> Objects.equals(item.getIsDefault(), 1))
                    .findFirst()
                    .orElse(availableInventories.get(0));
        }
        Long preferredId = preferred.getId();
        for (AiProviderInventory item : availableInventories) {
            int nextDefault = Objects.equals(item.getId(), preferredId) ? 1 : 0;
            if (!Objects.equals(item.getIsDefault(), nextDefault)) {
                item.setIsDefault(nextDefault);
                aiProviderInventoryMapper.updateById(item);
            }
        }
    }

    private String resolveProviderModelName(Long providerId) {
        AiProviderConfig provider = aiProviderConfigMapper.selectById(providerId);
        return provider == null ? null : provider.getModelName();
    }

    private AiProviderInventory findInventoryByModel(List<AiProviderInventory> inventories, String modelReference) {
        if (inventories == null || inventories.isEmpty() || !StringUtils.hasText(modelReference)) {
            return null;
        }
        return inventories.stream()
                .filter(item -> matchesInventoryModel(item, modelReference))
                .findFirst()
                .orElse(null);
    }

    private boolean matchesInventoryModel(AiProviderInventory inventory, String modelReference) {
        if (inventory == null || !StringUtils.hasText(modelReference)) {
            return false;
        }
        String normalized = modelReference.trim();
        return normalized.equalsIgnoreCase(defaultString(inventory.getInventoryCode(), ""))
                || normalized.equalsIgnoreCase(defaultString(inventory.getExternalId(), ""));
    }

    private AiProviderTemplateRegistry.AiProviderTemplateDefinition resolveProviderTemplate(String platformCode) {
        return aiProviderTemplateRegistry.find(defaultString(platformCode, "custom")).orElse(null);
    }

    private AiProviderInventory requireInventory(Long id) {
        AiProviderInventory inventory = aiProviderInventoryMapper.selectById(id);
        if (inventory == null) {
            throw new BusinessException(4044, "AI 模型庫項目不存在");
        }
        return inventory;
    }

    private void refreshProviderInventoryCount(Long providerId) {
        AiProviderConfig provider = aiProviderConfigMapper.selectById(providerId);
        if (provider == null) {
            return;
        }
        provider.setInventoryRecordCount(countProviderAvailableInventory(providerId));
        aiProviderConfigMapper.updateById(provider);
    }

    private List<AdminAiGenerationJobResponse> toJobResponses(List<AiGenerationJob> jobs, Long currentAdminId, List<String> roles) {
        if (jobs == null || jobs.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, AiCapability> capabilityMap = aiCapabilityMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiCapability::getId, item -> item, (left, right) -> left));
        Map<Long, AiCapabilityPolicy> policyMap = aiCapabilityPolicyMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiCapabilityPolicy::getId, item -> item, (left, right) -> left));
        Map<Long, AiPromptTemplate> templateMap = aiPromptTemplateMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiPromptTemplate::getId, item -> item, (left, right) -> left));
        Map<Long, AiProviderConfig> providerMap = aiProviderConfigMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiProviderConfig::getId, item -> item, (left, right) -> left));
        Map<Long, AiProviderInventory> inventoryMap = aiProviderInventoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(AiProviderInventory::getId, item -> item, (left, right) -> left));
        Map<Long, List<AiGenerationCandidate>> candidateMap = aiGenerationCandidateMapper.selectList(
                        new LambdaQueryWrapper<AiGenerationCandidate>().in(AiGenerationCandidate::getJobId, jobs.stream().map(AiGenerationJob::getId).toList())
                ).stream()
                .sorted(Comparator.comparing(AiGenerationCandidate::getCandidateIndex, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.groupingBy(AiGenerationCandidate::getJobId));

        return jobs.stream()
                .filter(job -> canAccessJob(job, currentAdminId, roles))
                .map(job -> mapJob(job, capabilityMap, policyMap, templateMap, providerMap, inventoryMap, candidateMap.getOrDefault(job.getId(), Collections.emptyList())))
                .toList();
    }

    private AdminAiGenerationJobResponse mapJob(AiGenerationJob job, Map<Long, AiCapability> capabilityMap,
                                                Map<Long, AiCapabilityPolicy> policyMap, Map<Long, AiPromptTemplate> templateMap,
                                                Map<Long, AiProviderConfig> providerMap,
                                                Map<Long, AiProviderInventory> inventoryMap,
                                                List<AiGenerationCandidate> candidates) {
        AiCapability capability = capabilityMap.get(job.getCapabilityId());
        AiCapabilityPolicy policy = policyMap.get(job.getPolicyId());
        AiPromptTemplate promptTemplate = templateMap.get(job.getPromptTemplateId());
        AiProviderConfig provider = providerMap.get(job.getProviderId());
        AiProviderInventory inventory = inventoryMap.get(job.getInventoryId());
        return AdminAiGenerationJobResponse.builder()
                .id(job.getId())
                .capabilityId(job.getCapabilityId())
                .capabilityCode(capability == null ? null : capability.getCapabilityCode())
                .capabilityNameZht(capability == null ? null : capability.getDisplayNameZht())
                .policyId(job.getPolicyId())
                .policyName(policy == null ? null : policy.getPolicyName())
                .promptTemplateId(job.getPromptTemplateId())
                .promptTemplateName(promptTemplate == null ? null : promptTemplate.getTemplateName())
                .providerId(job.getProviderId())
                .providerName(provider == null ? null : provider.getDisplayName())
                .inventoryId(job.getInventoryId())
                .inventoryCode(inventory == null ? null : inventory.getInventoryCode())
                .inventoryDisplayName(inventory == null ? null : inventory.getDisplayName())
                .ownerAdminId(job.getOwnerAdminId())
                .ownerAdminName(job.getOwnerAdminName())
                .generationType(job.getGenerationType())
                .sourceScope(job.getSourceScope())
                .sourceScopeId(job.getSourceScopeId())
                .jobStatus(job.getJobStatus())
                .promptTitle(job.getPromptTitle())
                .promptText(job.getPromptText())
                .promptVariablesJson(job.getPromptVariablesJson())
                .requestPayloadJson(job.getRequestPayloadJson())
                .providerRequestId(job.getProviderRequestId())
                .resultSummary(job.getResultSummary())
                .errorMessage(job.getErrorMessage())
                .latestCandidateId(job.getLatestCandidateId())
                .finalizedCandidateId(job.getFinalizedCandidateId())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .candidates(candidates.stream().map(this::mapCandidate).toList())
                .build();
    }

    private AdminAiGenerationCandidateResponse mapCandidate(AiGenerationCandidate candidate) {
        return AdminAiGenerationCandidateResponse.builder()
                .id(candidate.getId())
                .candidateIndex(candidate.getCandidateIndex())
                .candidateType(candidate.getCandidateType())
                .storageUrl(candidate.getStorageUrl())
                .mimeType(candidate.getMimeType())
                .fileSizeBytes(candidate.getFileSizeBytes())
                .widthPx(candidate.getWidthPx())
                .heightPx(candidate.getHeightPx())
                .durationMs(candidate.getDurationMs())
                .transcriptText(candidate.getTranscriptText())
                .previewText(candidate.getPreviewText())
                .metadataJson(candidate.getMetadataJson())
                .isSelected(candidate.getIsSelected())
                .isFinalized(candidate.getIsFinalized())
                .finalizedAssetId(candidate.getFinalizedAssetId())
                .createdAt(candidate.getCreatedAt())
                .build();
    }

    private AdminAiLogResponse mapLog(AiRequestLog log, List<AiProviderConfig> providers, Map<Long, AiCapabilityPolicy> policyMap) {
        Map<Long, AiProviderConfig> providerMap = providers.stream().collect(Collectors.toMap(AiProviderConfig::getId, item -> item, (left, right) -> left));
        AiCapabilityPolicy policy = policyMap.get(log.getPolicyId());
        return AdminAiLogResponse.builder()
                .id(log.getId())
                .providerId(log.getProviderId())
                .providerName(log.getProviderId() == null ? null : providerMap.get(log.getProviderId()) == null ? null : providerMap.get(log.getProviderId()).getDisplayName())
                .inventoryId(log.getInventoryId())
                .inventoryCode(log.getInventoryCode())
                .policyId(log.getPolicyId())
                .policyName(policy == null ? null : policy.getPolicyName())
                .capabilityCode(log.getCapabilityCode())
                .adminOwnerId(log.getAdminOwnerId())
                .adminOwnerName(log.getAdminOwnerName())
                .userOpenid(maskOpenId(log.getUserOpenid()))
                .requestType(log.getRequestType())
                .inputDataHash(log.getInputDataHash())
                .outputSummary(log.getOutputSummary())
                .latencyMs(log.getLatencyMs())
                .tokensUsed(log.getTokensUsed())
                .costUsd(log.getCostUsd())
                .success(log.getSuccess())
                .fallbackTriggered(log.getFallbackTriggered())
                .blockedReason(log.getBlockedReason())
                .traceId(log.getTraceId())
                .errorMessage(log.getErrorMessage())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private boolean canAccessJob(AiGenerationJob job, Long currentAdminId, List<String> roles) {
        return job != null && (isSuperAdmin(roles) || (currentAdminId != null && Objects.equals(job.getOwnerAdminId(), currentAdminId)));
    }

    private void replacePolicyBindings(Long policyId, List<AdminAiPolicyUpsertRequest.ProviderBinding> bindings) {
        aiPolicyProviderBindingMapper.delete(new LambdaQueryWrapper<AiPolicyProviderBinding>().eq(AiPolicyProviderBinding::getPolicyId, policyId));
        if (bindings == null || bindings.isEmpty()) {
            return;
        }
        for (AdminAiPolicyUpsertRequest.ProviderBinding item : bindings) {
            if (item.getProviderId() == null) {
                continue;
            }
            requireProvider(item.getProviderId());
            if (item.getInventoryId() != null) {
                requireInventory(item.getInventoryId());
            }
            AiPolicyProviderBinding binding = new AiPolicyProviderBinding();
            binding.setPolicyId(policyId);
            binding.setProviderId(item.getProviderId());
            binding.setInventoryId(item.getInventoryId());
            binding.setBindingRole(defaultString(item.getBindingRole(), "primary"));
            binding.setRouteMode(defaultString(item.getRouteMode(), "primary"));
            binding.setSortOrder(item.getSortOrder() == null ? 0 : item.getSortOrder());
            binding.setEnabled(item.getEnabled() == null ? 1 : item.getEnabled());
            binding.setModelOverride(item.getModelOverride());
            binding.setWeightPercent(item.getWeightPercent());
            binding.setTimeoutMsOverride(item.getTimeoutMsOverride());
            binding.setRetryCountOverride(item.getRetryCountOverride());
            binding.setParameterOverrideJson(normalizeJson(item.getParameterOverrideJson()));
            binding.setNotes(item.getNotes());
            aiPolicyProviderBindingMapper.insert(binding);
        }
    }

    private AiCapability resolveCapability(String capabilityCode) {
        AiCapability capability = aiCapabilityMapper.selectOne(
                new LambdaQueryWrapper<AiCapability>().eq(AiCapability::getCapabilityCode, capabilityCode)
        );
        if (capability == null) {
            throw new BusinessException(4044, "AI 能力不存在");
        }
        return capability;
    }

    private AiCapability requireCapability(Long capabilityId) {
        AiCapability capability = aiCapabilityMapper.selectById(capabilityId);
        if (capability == null) {
            throw new BusinessException(4044, "AI 能力不存在");
        }
        return capability;
    }

    private AiCapabilityPolicy requirePolicy(Long id) {
        AiCapabilityPolicy policy = aiCapabilityPolicyMapper.selectById(id);
        if (policy == null) {
            throw new BusinessException(4044, "AI 策略不存在");
        }
        return policy;
    }

    private AiPromptTemplate requirePromptTemplate(Long id) {
        AiPromptTemplate template = aiPromptTemplateMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(4044, "AI 提示模板不存在");
        }
        return template;
    }

    private AiQuotaRule requireQuotaRule(Long id) {
        AiQuotaRule rule = aiQuotaRuleMapper.selectById(id);
        if (rule == null) {
            throw new BusinessException(4044, "AI 配額規則不存在");
        }
        return rule;
    }

    private AiProviderConfig requireProvider(Long id) {
        AiProviderConfig provider = aiProviderConfigMapper.selectById(id);
        if (provider == null) {
            throw new BusinessException(4044, "AI 供應商不存在");
        }
        return provider;
    }

    private AiGenerationJob requireJob(Long id) {
        AiGenerationJob job = aiGenerationJobMapper.selectById(id);
        if (job == null) {
            throw new BusinessException(4044, "AI 生成任務不存在");
        }
        return job;
    }

    private AiGenerationCandidate requireCandidate(Long id) {
        AiGenerationCandidate candidate = aiGenerationCandidateMapper.selectById(id);
        if (candidate == null) {
            throw new BusinessException(4044, "AI 候選資源不存在");
        }
        return candidate;
    }

    private String decryptRequiredApiKey(AiProviderConfig provider) {
        String apiKey = aiSecretCryptoService.decrypt(provider.getApiKeyEncrypted());
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(4055, "AI 供應商尚未配置 API Key");
        }
        return apiKey;
    }

    private String buildPromptText(String directPrompt, String variablesJson, AiPromptTemplate template, AiCapabilityPolicy policy) {
        if (StringUtils.hasText(directPrompt)) {
            return directPrompt.trim();
        }
        Map<String, Object> variables = readJsonMap(variablesJson);
        String rendered = template != null ? renderTemplate(template.getPromptTemplate(), variables) : null;
        if (!StringUtils.hasText(rendered) && policy != null) {
            rendered = renderTemplate(policy.getPromptTemplate(), variables);
        }
        if (!StringUtils.hasText(rendered) && !variables.isEmpty()) {
            rendered = writeJson(variables);
        }
        String systemPrompt = template != null && StringUtils.hasText(template.getSystemPrompt())
                ? template.getSystemPrompt()
                : policy == null ? null : policy.getSystemPrompt();
        if (StringUtils.hasText(systemPrompt) && StringUtils.hasText(rendered) && !rendered.startsWith(systemPrompt)) {
            return systemPrompt + "\n\n" + rendered;
        }
        return rendered;
    }

    private AiCapabilityPolicy resolvePolicy(AiCapability capability, Long policyId, AiPromptTemplate promptTemplate) {
        if (policyId != null) {
            return requirePolicy(policyId);
        }
        if (promptTemplate != null && promptTemplate.getDefaultPolicyId() != null) {
            return requirePolicy(promptTemplate.getDefaultPolicyId());
        }
        return aiCapabilityPolicyMapper.selectOne(
                new LambdaQueryWrapper<AiCapabilityPolicy>()
                        .eq(AiCapabilityPolicy::getCapabilityId, capability.getId())
                        .eq(AiCapabilityPolicy::getStatus, STATUS_ENABLED)
                        .orderByAsc(AiCapabilityPolicy::getSortOrder)
                        .last("LIMIT 1")
        );
    }

    private ResolvedProvider resolveProvider(AiCapability capability, AiCapabilityPolicy policy, AiPromptTemplate template, Long providerId) {
        if (providerId != null) {
            AiProviderConfig provider = requireProvider(providerId);
            AiProviderInventory inventory = resolvePreferredInventory(provider.getId(), capability.getCapabilityCode(), template == null ? null : template.getAssetSlotCode());
            return new ResolvedProvider(provider, inventory, null, inventory == null ? null : inventory.getExternalId());
        }
        if (template != null && template.getDefaultProviderId() != null) {
            AiProviderConfig provider = requireProvider(template.getDefaultProviderId());
            AiProviderInventory inventory = resolvePreferredInventory(provider.getId(), capability.getCapabilityCode(), template.getAssetSlotCode());
            return new ResolvedProvider(provider, inventory, null, inventory == null ? null : inventory.getExternalId());
        }
        if (policy != null && policy.getManualSwitchProviderId() != null) {
            AiProviderConfig provider = requireProvider(policy.getManualSwitchProviderId());
            AiProviderInventory inventory = resolvePreferredInventory(provider.getId(), capability.getCapabilityCode(), null);
            return new ResolvedProvider(provider, inventory, null, inventory == null ? null : inventory.getExternalId());
        }
        if (policy != null) {
            List<AiPolicyProviderBinding> bindings = aiPolicyProviderBindingMapper.selectList(
                    new LambdaQueryWrapper<AiPolicyProviderBinding>()
                            .eq(AiPolicyProviderBinding::getPolicyId, policy.getId())
                            .eq(AiPolicyProviderBinding::getEnabled, 1)
                            .orderByAsc(AiPolicyProviderBinding::getSortOrder)
                            .orderByAsc(AiPolicyProviderBinding::getId)
            );
            if (!bindings.isEmpty()) {
                AiPolicyProviderBinding binding = bindings.get(0);
                AiProviderConfig provider = requireProvider(binding.getProviderId());
                AiProviderInventory inventory = binding.getInventoryId() == null
                        ? resolvePreferredInventory(provider.getId(), capability.getCapabilityCode(), null, binding.getModelOverride())
                        : requireInventory(binding.getInventoryId());
                return new ResolvedProvider(
                        provider,
                        inventory,
                        binding,
                        StringUtils.hasText(binding.getModelOverride()) ? binding.getModelOverride() : inventory == null ? null : inventory.getExternalId()
                );
            }
        }
        AiProviderConfig provider = aiProviderConfigMapper.selectList(
                        new LambdaQueryWrapper<AiProviderConfig>()
                                .eq(AiProviderConfig::getStatus, 1)
                                .orderByAsc(AiProviderConfig::getId)
                ).stream()
                .filter(item -> parseCapabilityCodes(item.getCapabilities()).contains(capability.getCapabilityCode()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(4044, "未找到可用的 AI 供應商"));
        AiProviderInventory inventory = resolvePreferredInventory(provider.getId(), capability.getCapabilityCode(), null);
        return new ResolvedProvider(provider, inventory, null, inventory == null ? null : inventory.getExternalId());
    }

    private List<AiQuotaRule> loadQuotaRules(Long capabilityId, Long policyId) {
        return aiQuotaRuleMapper.selectList(
                new LambdaQueryWrapper<AiQuotaRule>()
                        .eq(AiQuotaRule::getCapabilityId, capabilityId)
                        .and(wrapper -> wrapper.eq(AiQuotaRule::getPolicyId, policyId).or().isNull(AiQuotaRule::getPolicyId))
        );
    }

    private AiGovernanceService.GovernanceLease enforceQuotaRules(List<AiQuotaRule> rules,
                                                                 String capabilityCode,
                                                                 Long adminId,
                                                                 Integer estimatedTokens) {
        List<AiQuotaRule> enabledRules = rules.stream().filter(item -> STATUS_ENABLED.equalsIgnoreCase(item.getStatus())).toList();
        for (AiQuotaRule rule : enabledRules) {
            LocalDateTime since = switch (defaultString(rule.getWindowType(), "minute").toLowerCase(Locale.ROOT)) {
                case "hour" -> LocalDateTime.now().minus(rule.getWindowSize() == null ? 1 : rule.getWindowSize(), ChronoUnit.HOURS);
                case "day" -> LocalDateTime.now().minus(rule.getWindowSize() == null ? 1 : rule.getWindowSize(), ChronoUnit.DAYS);
                default -> LocalDateTime.now().minus(rule.getWindowSize() == null ? 1 : rule.getWindowSize(), ChronoUnit.MINUTES);
            };
            long requestCount = countRecentCapabilityRequests(capabilityCode, adminId, since);
            if (rule.getRequestLimit() != null && requestCount >= rule.getRequestLimit()) {
                throw new BusinessException(4290, "AI 請求已達當前配額限制");
            }
            if (rule.getTokenLimit() != null && estimatedTokens != null && estimatedTokens > rule.getTokenLimit()) {
                throw new BusinessException(4291, "AI 輸入內容超出當前 Token 限額");
            }
        }
        List<AiQuotaRule> concurrencyRules = enabledRules.stream()
                .filter(item -> item.getSuspiciousConcurrencyThreshold() != null)
                .toList();
        AiGovernanceService.GovernanceLease lease = aiGovernanceService.acquire(
                capabilityCode + ":" + defaultString(String.valueOf(adminId), "global"),
                concurrencyRules,
                0,
                estimatedTokens
        );
        if (!lease.allowed()) {
            throw new BusinessException(4292, lease.blockedReason());
        }
        return lease;
    }

    private long countRecentCapabilityRequests(String capabilityCode, Long adminId, LocalDateTime since) {
        return aiRequestLogMapper.selectCount(
                new LambdaQueryWrapper<AiRequestLog>()
                        .eq(AiRequestLog::getCapabilityCode, capabilityCode)
                        .eq(adminId != null, AiRequestLog::getAdminOwnerId, adminId)
                        .ge(AiRequestLog::getCreatedAt, since)
        );
    }

    private void executeGeneration(AiGenerationJob job, AiCapability capability, AiCapabilityPolicy policy,
                                   ResolvedProvider resolvedProvider, String traceId) {
        AiProviderConfig provider = resolvedProvider.provider();
        String apiKey = decryptRequiredApiKey(provider);
        String modelOverride = resolvedProvider.modelOverride();
        long startedAt = System.currentTimeMillis();

        if ("image".equalsIgnoreCase(job.getGenerationType())) {
            DashScopeProviderGateway.ImageTaskResult result = dashScopeProviderGateway.submitImageJob(
                    provider, apiKey, job.getPromptText(), modelOverride, job.getRequestPayloadJson()
            );
            job.setProviderRequestId(result.taskId());
            job.setJobStatus(JOB_STATUS_SUBMITTED);
            job.setResultSummary(result.taskStatus());
            aiGenerationJobMapper.updateById(job);
            insertLog(provider.getId(), policy == null ? null : policy.getId(), capability.getCapabilityCode(),
                    resolvedProvider.inventory() == null ? null : resolvedProvider.inventory().getId(),
                    resolvedProvider.inventory() == null ? null : resolvedProvider.inventory().getInventoryCode(),
                    job.getOwnerAdminId(), job.getOwnerAdminName(), "image_generation", hash(job.getPromptText()),
                    result.taskStatus(), (int) (System.currentTimeMillis() - startedAt), estimateTokens(job.getPromptText()),
                    BigDecimal.ZERO, 1, 0, null, null, traceId);
            return;
        }

        if ("tts".equalsIgnoreCase(job.getGenerationType()) || "audio".equalsIgnoreCase(job.getGenerationType())) {
            DashScopeProviderGateway.TtsResult result = dashScopeProviderGateway.synthesizeSpeech(
                    provider, apiKey, job.getPromptText(), modelOverride, job.getRequestPayloadJson()
            );
            byte[] bytes = dashScopeProviderGateway.downloadBinary(provider, result.assetUrl(), provider.getRequestTimeoutMs());
            AiGenerationCandidate candidate = persistBinaryCandidate(job, "audio", result.assetUrl(), bytes, "job-" + job.getId() + ".mp3",
                    "audio/mpeg", result.metadataJson(), trimTo(job.getPromptText(), 255), null);
            job.setLatestCandidateId(candidate.getId());
            job.setResultSummary(defaultString(candidate.getStorageUrl(), candidate.getPreviewText()));
            job.setJobStatus(JOB_STATUS_COMPLETED);
            aiGenerationJobMapper.updateById(job);
            insertLog(provider.getId(), policy == null ? null : policy.getId(), capability.getCapabilityCode(),
                    resolvedProvider.inventory() == null ? null : resolvedProvider.inventory().getId(),
                    resolvedProvider.inventory() == null ? null : resolvedProvider.inventory().getInventoryCode(),
                    job.getOwnerAdminId(), job.getOwnerAdminName(), "tts_generation", hash(job.getPromptText()),
                    candidate.getStorageUrl(), (int) result.latencyMs(), estimateTokens(job.getPromptText()),
                    BigDecimal.ZERO, 1, 0, null, null, traceId);
            return;
        }

        DashScopeProviderGateway.ChatResult result = dashScopeProviderGateway.generateText(
                provider, apiKey, policy == null ? null : policy.getSystemPrompt(), job.getPromptText(), modelOverride, provider.getRequestTimeoutMs()
        );
        AiGenerationCandidate candidate = new AiGenerationCandidate();
        candidate.setJobId(job.getId());
        candidate.setCandidateIndex(nextCandidateIndex(job.getId()));
        candidate.setCandidateType("text");
        candidate.setPreviewText(result.previewText());
        candidate.setTranscriptText(result.previewText());
        candidate.setMetadataJson(writeJson(Map.of("resolvedModel", result.resolvedModel())));
        candidate.setIsSelected(1);
        candidate.setIsFinalized(0);
        aiGenerationCandidateMapper.insert(candidate);
        job.setLatestCandidateId(candidate.getId());
        job.setResultSummary(trimTo(result.previewText(), 255));
        job.setJobStatus(JOB_STATUS_COMPLETED);
        aiGenerationJobMapper.updateById(job);
        insertLog(provider.getId(), policy == null ? null : policy.getId(), capability.getCapabilityCode(),
                resolvedProvider.inventory() == null ? null : resolvedProvider.inventory().getId(),
                resolvedProvider.inventory() == null ? null : resolvedProvider.inventory().getInventoryCode(),
                job.getOwnerAdminId(), job.getOwnerAdminName(), "text_generation", hash(job.getPromptText()),
                trimTo(result.previewText(), 255), (int) result.latencyMs(), estimateTokens(job.getPromptText()),
                BigDecimal.ZERO, 1, 0, null, null, traceId);
    }

    private void persistImagePollResult(AiGenerationJob job, DashScopeProviderGateway.ImagePollResult result, String traceId) {
        AiProviderConfig provider = requireProvider(job.getProviderId());
        if (result.urls() == null || result.urls().isEmpty()) {
            job.setJobStatus("FAILED".equalsIgnoreCase(result.taskStatus()) ? JOB_STATUS_FAILED : JOB_STATUS_SUBMITTED);
            job.setResultSummary(result.taskStatus());
            if (JOB_STATUS_FAILED.equals(job.getJobStatus())) {
                job.setErrorMessage(defaultString(result.metadataJson(), "圖片生成失敗"));
            }
            aiGenerationJobMapper.updateById(job);
            return;
        }
        clearSelectedCandidates(job.getId());
        for (String url : result.urls()) {
            byte[] bytes = dashScopeProviderGateway.downloadBinary(provider, url, provider.getRequestTimeoutMs());
            AiGenerationCandidate candidate = persistBinaryCandidate(job, "image", url, bytes, "job-" + job.getId() + ".png",
                    "image/png", result.metadataJson(), result.taskStatus(), null);
            job.setLatestCandidateId(candidate.getId());
        }
        job.setJobStatus(JOB_STATUS_COMPLETED);
        job.setResultSummary(result.taskStatus());
        aiGenerationJobMapper.updateById(job);
        insertLog(job.getProviderId(), job.getPolicyId(), requireCapability(job.getCapabilityId()).getCapabilityCode(),
                job.getInventoryId(), resolveInventoryCode(job.getInventoryId()),
                job.getOwnerAdminId(), job.getOwnerAdminName(), "image_poll", hash(job.getPromptText()),
                result.taskStatus(), null, estimateTokens(job.getPromptText()), BigDecimal.ZERO, 1, 0, null, null, traceId);
    }

    private AiGenerationCandidate persistBinaryCandidate(AiGenerationJob job, String candidateType, String providerUrl, byte[] bytes,
                                                         String filename, String contentType, String metadataJson,
                                                         String previewText, Integer durationMs) {
        StoredAssetMetadata stored = cosAssetStorageService.storeAsset(StoredAssetPayload.builder()
                .bytes(bytes)
                .originalFilename(filename)
                .contentType(contentType)
                .assetKind(resolveAssetKind(null, candidateType))
                .localeCode("zh-Hant")
                .build());
        AiGenerationCandidate candidate = new AiGenerationCandidate();
        candidate.setJobId(job.getId());
        candidate.setCandidateIndex(nextCandidateIndex(job.getId()));
        candidate.setCandidateType(candidateType);
        candidate.setStorageBucketName(stored.getBucketName());
        candidate.setStorageRegion(stored.getRegion());
        candidate.setStorageObjectKey(stored.getObjectKey());
        candidate.setStorageUrl(stored.getCanonicalUrl());
        candidate.setMimeType(stored.getMimeType());
        candidate.setFileSizeBytes(stored.getFileSizeBytes());
        candidate.setWidthPx(stored.getWidthPx());
        candidate.setHeightPx(stored.getHeightPx());
        candidate.setDurationMs(durationMs);
        candidate.setPreviewText(previewText);
        candidate.setProviderAssetUrl(providerUrl);
        candidate.setMetadataJson(metadataJson);
        candidate.setIsSelected(1);
        candidate.setIsFinalized(0);
        aiGenerationCandidateMapper.insert(candidate);
        return candidate;
    }

    private Integer nextCandidateIndex(Long jobId) {
        return aiGenerationCandidateMapper.selectList(
                        new LambdaQueryWrapper<AiGenerationCandidate>().eq(AiGenerationCandidate::getJobId, jobId)
                ).stream()
                .map(AiGenerationCandidate::getCandidateIndex)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0) + 1;
    }

    private void clearSelectedCandidates(Long jobId) {
        List<AiGenerationCandidate> candidates = aiGenerationCandidateMapper.selectList(
                new LambdaQueryWrapper<AiGenerationCandidate>().eq(AiGenerationCandidate::getJobId, jobId)
        );
        for (AiGenerationCandidate item : candidates) {
            if (!Objects.equals(item.getIsSelected(), 0)) {
                item.setIsSelected(0);
                aiGenerationCandidateMapper.updateById(item);
            }
        }
    }

    private String resolveAssetKind(String explicitAssetKind, String candidateType) {
        if (StringUtils.hasText(explicitAssetKind)) {
            return explicitAssetKind.trim();
        }
        return switch (defaultString(candidateType, "other").toLowerCase(Locale.ROOT)) {
            case "image" -> "image";
            case "audio", "tts" -> "audio";
            default -> "document";
        };
    }

    private String resolveOriginalFilename(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            return "asset.bin";
        }
        int slashIndex = objectKey.replace("\\", "/").lastIndexOf('/');
        return slashIndex >= 0 ? objectKey.substring(slashIndex + 1) : objectKey;
    }

    private String resolveExtension(String objectKey) {
        String filename = resolveOriginalFilename(objectKey);
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex >= 0 ? filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT) : "";
    }

    private record ResolvedProvider(AiProviderConfig provider,
                                    AiProviderInventory inventory,
                                    AiPolicyProviderBinding binding,
                                    String modelOverride) {
    }

    private boolean isSuperAdmin(List<String> roles) {
        return roles != null && roles.stream().filter(Objects::nonNull)
                .anyMatch(role -> "SUPER_ADMIN".equalsIgnoreCase(role) || "ROLE_SUPER_ADMIN".equalsIgnoreCase(role));
    }

    private String defaultString(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String trimTo(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private Integer estimateTokens(String text) {
        if (!StringUtils.hasText(text)) {
            return 0;
        }
        return Math.max(1, (int) Math.ceil(text.length() / 4D));
    }

    private String hash(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private String maskOpenId(String openId) {
        if (!StringUtils.hasText(openId) || openId.length() <= 8) {
            return openId;
        }
        return openId.substring(0, 4) + "****" + openId.substring(openId.length() - 4);
    }

    private String normalizeJson(String rawJson) {
        if (!StringUtils.hasText(rawJson)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(rawJson));
        } catch (Exception ex) {
            throw new BusinessException(4055, "JSON 格式不正確");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new BusinessException(5003, "AI 配置序列化失敗");
        }
    }

    private List<String> parseStringList(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream().filter(StringUtils::hasText).map(String::trim).distinct().toList();
    }

    private List<String> parseCapabilityCodes(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<>() {});
        } catch (Exception ignored) {
            return Arrays.stream(raw.split(",")).map(String::trim).filter(StringUtils::hasText).toList();
        }
    }

    private List<String> readJsonList(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        try {
            return parseStringList(objectMapper.readValue(raw, new TypeReference<>() {}));
        } catch (Exception ignored) {
            return Arrays.stream(raw.split(",")).map(String::trim).filter(StringUtils::hasText).toList();
        }
    }

    private Integer readIntegerConfig(String key, Integer fallback) {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return fallback;
        }
        try {
            return Integer.parseInt(config.getConfigValue().trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private BigDecimal readDecimalConfig(String key, String fallback) {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        if (config == null || !StringUtils.hasText(config.getConfigValue())) {
            return new BigDecimal(fallback);
        }
        try {
            return new BigDecimal(config.getConfigValue().trim());
        } catch (NumberFormatException ignored) {
            return new BigDecimal(fallback);
        }
    }

    private void upsertConfig(String key, String value, String type, String description) {
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        if (config == null) {
            config = new SysConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setConfigType(type);
            config.setDescription(description);
            sysConfigMapper.insert(config);
            return;
        }
        config.setConfigValue(value);
        config.setConfigType(type);
        config.setDescription(description);
        sysConfigMapper.updateById(config);
    }

    private Integer normalizePositiveInteger(Integer value, Integer fallback) {
        int resolved = value == null ? fallback : value;
        if (resolved <= 0) {
            throw new BusinessException(4001, "設定值必須大於 0");
        }
        return resolved;
    }

    private BigDecimal normalizePositiveDecimal(BigDecimal value, BigDecimal fallback) {
        BigDecimal resolved = value == null ? fallback : value;
        if (resolved.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(4001, "設定值必須大於 0");
        }
        return resolved;
    }

    private BigDecimal normalizeDecimalInRange(BigDecimal value, BigDecimal fallback, BigDecimal minInclusive, BigDecimal maxInclusive) {
        BigDecimal resolved = value == null ? fallback : value;
        if (resolved.compareTo(minInclusive) < 0 || resolved.compareTo(maxInclusive) > 0) {
            throw new BusinessException(4001, "設定值超出允許範圍");
        }
        return resolved;
    }

    private Integer normalizeBooleanInt(Integer value, Integer fallback) {
        int resolved = value == null ? fallback : value;
        return resolved == 1 ? 1 : 0;
    }

    private String sanitizeInventoryCode(String value) {
        if (!StringUtils.hasText(value)) {
            return "inventory-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    private String resolveAssetSlotModality(String assetSlotCode) {
        if (!StringUtils.hasText(assetSlotCode)) {
            return null;
        }
        String normalized = assetSlotCode.trim().toLowerCase(Locale.ROOT);
        if (normalized.contains("audio") || normalized.contains("voice") || normalized.contains("tts")) {
            return "audio";
        }
        if (normalized.contains("image") || normalized.contains("cover") || normalized.contains("banner") || normalized.contains("icon") || normalized.contains("overlay")) {
            return "image";
        }
        return "text";
    }

    private List<String> inferModalities(String modelCode) {
        String normalized = defaultString(modelCode, "").toLowerCase(Locale.ROOT);
        if (normalized.contains("image") || normalized.contains("wan")) {
            return List.of("image");
        }
        if (normalized.contains("tts") || normalized.contains("voice") || normalized.contains("audio")) {
            return List.of("audio");
        }
        if (normalized.contains("vision")) {
            return List.of("text", "vision");
        }
        return List.of("text");
    }

    private List<String> inferCapabilities(String modelCode, List<String> modalities) {
        if (modalities.contains("image")) {
            return List.of("admin_image_generation");
        }
        if (modalities.contains("audio")) {
            return List.of("admin_tts_generation", "npc_voice_dialogue");
        }
        String normalized = defaultString(modelCode, "").toLowerCase(Locale.ROOT);
        if (normalized.contains("planner") || normalized.contains("reason")) {
            return List.of("itinerary_planning", "travel_qa", "navigation_assist");
        }
        return List.of("admin_prompt_drafting", "itinerary_planning", "travel_qa", "navigation_assist");
    }

    private Map<String, Object> readJsonMap(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(raw, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new BusinessException(4055, "JSON 內容無法解析");
        }
    }

    private String renderTemplate(String template, Map<String, Object> variables) {
        if (!StringUtils.hasText(template)) {
            return null;
        }
        String rendered = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            rendered = rendered.replace("{{" + entry.getKey() + "}}", entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
        }
        return rendered;
    }

    private void insertLog(Long providerId, Long policyId, String capabilityCode, Long inventoryId, String inventoryCode,
                           Long adminOwnerId, String adminOwnerName, String requestType, String inputHash,
                           String outputSummary, Integer latencyMs, Integer tokensUsed, BigDecimal costUsd,
                           Integer success, Integer fallbackTriggered, String blockedReason, String errorMessage,
                           String traceId) {
        AiRequestLog log = new AiRequestLog();
        log.setProviderId(providerId);
        log.setInventoryId(inventoryId);
        log.setPolicyId(policyId);
        log.setCapabilityCode(capabilityCode);
        log.setInventoryCode(inventoryCode);
        log.setAdminOwnerId(adminOwnerId);
        log.setAdminOwnerName(adminOwnerName);
        log.setRequestType(requestType);
        log.setInputDataHash(inputHash);
        log.setOutputSummary(trimTo(outputSummary, 1024));
        log.setLatencyMs(latencyMs);
        log.setTokensUsed(tokensUsed);
        log.setCostUsd(costUsd);
        log.setSuccess(success);
        log.setFallbackTriggered(fallbackTriggered);
        log.setBlockedReason(trimTo(blockedReason, 255));
        log.setErrorMessage(errorMessage);
        log.setTraceId(traceId);
        log.setCreatedAt(LocalDateTime.now());
        aiRequestLogMapper.insert(log);
    }

    private void insertLog(Long providerId, Long policyId, String capabilityCode, Long adminOwnerId, String adminOwnerName,
                           String requestType, String inputHash, String outputSummary, Integer latencyMs, Integer tokensUsed,
                           BigDecimal costUsd, Integer success, Integer fallbackTriggered, String blockedReason,
                           String errorMessage, String traceId) {
        insertLog(providerId, policyId, capabilityCode, null, null, adminOwnerId, adminOwnerName, requestType, inputHash,
                outputSummary, latencyMs, tokensUsed, costUsd, success, fallbackTriggered, blockedReason, errorMessage, traceId);
    }

    private record DiscoveredInventoryRecord(String inventoryCode,
                                             String externalId,
                                             String displayName,
                                             String inventoryType,
                                             List<String> modalityCodes,
                                             List<String> capabilityCodes,
                                             String sourceType,
                                             String endpointPath,
                                             Integer contextWindowTokens,
                                             BigDecimal inputPricePer1k,
                                             BigDecimal outputPricePer1k,
                                             BigDecimal imagePricePerCall,
                                             BigDecimal audioPricePerMinute,
                                             String featureFlagsJson,
                                             String rawPayloadJson,
                                             Integer isDefault,
                                             Integer sortOrder) {
    }

    private record InventorySyncResult(int discoveredCount,
                                       int createdCount,
                                       int updatedCount,
                                       int staleCount,
                                       int totalRecords,
                                       String message,
                                       String rawPayloadJson) {
    }
}

