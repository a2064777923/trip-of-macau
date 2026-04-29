package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminAiCandidateFinalizeRequest;
import com.aoxiaoyou.admin.dto.request.AdminAiGenerationJobCreateRequest;
import com.aoxiaoyou.admin.dto.request.AdminAiInventoryUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminAiPolicyUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminAiPlatformSettingsUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminAiPromptTemplateUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminAiProviderTestRequest;
import com.aoxiaoyou.admin.dto.request.AdminAiProviderUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminAiQuotaRuleUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminAiVoiceCloneRequest;
import com.aoxiaoyou.admin.dto.request.AdminAiVoicePreviewRequest;
import com.aoxiaoyou.admin.dto.request.AdminAiVoiceSyncRequest;
import com.aoxiaoyou.admin.dto.response.AdminAiCapabilityResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiGenerationJobResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiInventoryResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiLogResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiOverviewResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiPolicyResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiPlatformSettingsResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiPromptTemplateResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiProviderResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiProviderSyncJobResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiProviderTemplateResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiProviderTestResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiQuotaRuleResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiVoicePreviewResponse;
import com.aoxiaoyou.admin.dto.response.AdminAiVoiceResponse;

import java.util.List;

public interface AdminAiService {

    AdminAiOverviewResponse getOverview(Long currentAdminId, List<String> roles);

    List<AdminAiCapabilityResponse> listCapabilities();

    List<AdminAiProviderTemplateResponse> listProviderTemplates();

    List<AdminAiProviderResponse> listProviders();

    AdminAiProviderResponse createProvider(AdminAiProviderUpsertRequest request);

    AdminAiProviderResponse updateProvider(Long id, AdminAiProviderUpsertRequest request);

    void deleteProvider(Long id);

    AdminAiProviderTestResponse testProvider(Long id, AdminAiProviderTestRequest request, Long currentAdminId, String currentAdminName);

    AdminAiProviderSyncJobResponse syncProviderInventory(Long id, Long currentAdminId, String currentAdminName);

    List<AdminAiProviderSyncJobResponse> listProviderSyncJobs(Long providerId);

    List<AdminAiInventoryResponse> listInventory(Long providerId, String capabilityCode, String sourceType);

    AdminAiInventoryResponse createInventory(AdminAiInventoryUpsertRequest request);

    AdminAiInventoryResponse updateInventory(Long id, AdminAiInventoryUpsertRequest request);

    void deleteInventory(Long id);

    List<AdminAiVoiceResponse> listVoices(Long providerId,
                                          String modelCode,
                                          String languageCode,
                                          String sourceType,
                                          Long currentAdminId,
                                          List<String> roles);

    List<AdminAiVoiceResponse> syncVoices(Long providerId,
                                          AdminAiVoiceSyncRequest request,
                                          Long currentAdminId,
                                          String currentAdminName,
                                          List<String> roles);

    AdminAiVoicePreviewResponse previewVoice(AdminAiVoicePreviewRequest request,
                                             Long currentAdminId,
                                             String currentAdminName);

    AdminAiVoiceResponse createVoiceClone(AdminAiVoiceCloneRequest request,
                                          Long currentAdminId,
                                          String currentAdminName);

    AdminAiVoiceResponse refreshVoice(Long voiceId, Long currentAdminId, List<String> roles);

    void deleteVoice(Long voiceId, Long currentAdminId, List<String> roles);

    List<AdminAiPolicyResponse> listPolicies(String capabilityCode);

    AdminAiPolicyResponse createPolicy(AdminAiPolicyUpsertRequest request);

    AdminAiPolicyResponse updatePolicy(Long id, AdminAiPolicyUpsertRequest request);

    void deletePolicy(Long id);

    List<AdminAiQuotaRuleResponse> listQuotaRules(String capabilityCode);

    AdminAiQuotaRuleResponse createQuotaRule(AdminAiQuotaRuleUpsertRequest request);

    AdminAiQuotaRuleResponse updateQuotaRule(Long id, AdminAiQuotaRuleUpsertRequest request);

    void deleteQuotaRule(Long id);

    PageResponse<AdminAiLogResponse> pageLogs(long pageNum, long pageSize, String capabilityCode, Integer success, Long providerId);

    List<AdminAiPromptTemplateResponse> listPromptTemplates(String capabilityCode, String templateType);

    AdminAiPromptTemplateResponse createPromptTemplate(AdminAiPromptTemplateUpsertRequest request);

    AdminAiPromptTemplateResponse updatePromptTemplate(Long id, AdminAiPromptTemplateUpsertRequest request);

    void deletePromptTemplate(Long id);

    PageResponse<AdminAiGenerationJobResponse> pageGenerationJobs(
            long pageNum,
            long pageSize,
            String capabilityCode,
            String generationType,
            String jobStatus,
            Long currentAdminId,
            List<String> roles
    );

    AdminAiGenerationJobResponse getGenerationJob(Long jobId, Long currentAdminId, List<String> roles);

    AdminAiGenerationJobResponse createGenerationJob(
            AdminAiGenerationJobCreateRequest request,
            Long currentAdminId,
            String currentAdminName,
            List<String> roles
    );

    AdminAiGenerationJobResponse refreshGenerationJob(Long jobId, Long currentAdminId, List<String> roles);

    AdminAiGenerationJobResponse finalizeCandidate(
            Long candidateId,
            AdminAiCandidateFinalizeRequest request,
            Long currentAdminId,
            String currentAdminName,
            List<String> roles
    );

    AdminAiGenerationJobResponse restoreCandidate(Long candidateId, Long currentAdminId, List<String> roles);

    AdminAiPlatformSettingsResponse getPlatformSettings();

    AdminAiPlatformSettingsResponse updatePlatformSettings(AdminAiPlatformSettingsUpsertRequest request);
}
