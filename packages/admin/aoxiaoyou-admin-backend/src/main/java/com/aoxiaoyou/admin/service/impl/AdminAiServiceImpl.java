package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiLogResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiPolicyResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiProviderResponse;
import com.aoxiaoyou.admin.entity.AiPolicy;
import com.aoxiaoyou.admin.entity.AiProviderConfig;
import com.aoxiaoyou.admin.entity.AiRequestLog;
import com.aoxiaoyou.admin.mapper.AiPolicyMapper;
import com.aoxiaoyou.admin.mapper.AiProviderConfigMapper;
import com.aoxiaoyou.admin.mapper.AiRequestLogMapper;
import com.aoxiaoyou.admin.service.AdminAiService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminAiServiceImpl implements AdminAiService {

    private final AiProviderConfigMapper aiProviderConfigMapper;
    private final AiPolicyMapper aiPolicyMapper;
    private final AiRequestLogMapper aiRequestLogMapper;

    @Override
    public List<AdminAiProviderResponse> listProviders() {
        return aiProviderConfigMapper.selectList(new LambdaQueryWrapper<AiProviderConfig>().orderByAsc(AiProviderConfig::getId))
                .stream()
                .map(item -> AdminAiProviderResponse.builder()
                        .id(item.getId())
                        .providerName(item.getProviderName())
                        .displayName(item.getDisplayName())
                        .apiBaseUrl(item.getApiBaseUrl())
                        .modelName(item.getModelName())
                        .capabilities(item.getCapabilities())
                        .requestTimeoutMs(item.getRequestTimeoutMs())
                        .maxRetries(item.getMaxRetries())
                        .quotaDaily(item.getQuotaDaily())
                        .costPer1kTokens(item.getCostPer1kTokens())
                        .status(item.getStatus())
                        .build())
                .toList();
    }

    @Override
    public List<AdminAiPolicyResponse> listPolicies(String scenarioGroup) {
        List<AiProviderConfig> providers = aiProviderConfigMapper.selectList(null);
        Map<Long, String> providerNameMap = providers.stream().collect(Collectors.toMap(AiProviderConfig::getId, AiProviderConfig::getDisplayName));
        return aiPolicyMapper.selectList(new LambdaQueryWrapper<AiPolicy>()
                        .eq(StringUtils.hasText(scenarioGroup), AiPolicy::getScenarioGroup, scenarioGroup)
                        .orderByAsc(AiPolicy::getScenarioGroup)
                        .orderByAsc(AiPolicy::getId))
                .stream()
                .map(item -> AdminAiPolicyResponse.builder()
                        .id(item.getId())
                        .policyName(item.getPolicyName())
                        .scenarioCode(item.getScenarioCode())
                        .policyType(item.getPolicyType())
                        .scenarioGroup(item.getScenarioGroup())
                        .providerId(item.getProviderId())
                        .providerName(providerNameMap.get(item.getProviderId()))
                        .modelOverride(item.getModelOverride())
                        .multimodalEnabled(item.getMultimodalEnabled())
                        .voiceEnabled(item.getVoiceEnabled())
                        .temperature(item.getTemperature())
                        .maxTokens(item.getMaxTokens())
                        .status(item.getStatus())
                        .build())
                .toList();
    }

    @Override
    public PageResponse<AdminAiLogResponse> pageLogs(long pageNum, long pageSize, String scenarioGroup, Integer success, Long providerId) {
        List<AiProviderConfig> providers = aiProviderConfigMapper.selectList(null);
        Map<Long, String> providerNameMap = providers.stream().collect(Collectors.toMap(AiProviderConfig::getId, AiProviderConfig::getDisplayName));
        List<AiPolicy> policies = aiPolicyMapper.selectList(null);
        Map<Long, AiPolicy> policyMap = policies.stream().collect(Collectors.toMap(AiPolicy::getId, item -> item));

        Page<AiRequestLog> page = aiRequestLogMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<AiRequestLog>()
                        .eq(providerId != null, AiRequestLog::getProviderId, providerId)
                        .eq(success != null, AiRequestLog::getSuccess, success)
                        .in(StringUtils.hasText(scenarioGroup), AiRequestLog::getPolicyId,
                                policies.stream()
                                        .filter(item -> scenarioGroup.equals(item.getScenarioGroup()))
                                        .map(AiPolicy::getId)
                                        .toList())
                        .orderByDesc(AiRequestLog::getCreatedAt)
                        .orderByDesc(AiRequestLog::getId)
        );

        Page<AdminAiLogResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(item -> {
            AiPolicy policy = policyMap.get(item.getPolicyId());
            return AdminAiLogResponse.builder()
                    .id(item.getId())
                    .providerId(item.getProviderId())
                    .providerName(providerNameMap.get(item.getProviderId()))
                    .policyId(item.getPolicyId())
                    .policyName(policy != null ? policy.getPolicyName() : null)
                    .scenarioCode(policy != null ? policy.getScenarioCode() : null)
                    .scenarioGroup(policy != null ? policy.getScenarioGroup() : null)
                    .userOpenid(item.getUserOpenid())
                    .requestType(item.getRequestType())
                    .inputDataHash(item.getInputDataHash())
                    .outputSummary(item.getOutputSummary())
                    .latencyMs(item.getLatencyMs())
                    .tokensUsed(item.getTokensUsed())
                    .costUsd(item.getCostUsd())
                    .success(item.getSuccess())
                    .errorMessage(item.getErrorMessage())
                    .createdAt(item.getCreatedAt())
                    .build();
        }).toList());
        return PageResponse.of(result);
    }
}
