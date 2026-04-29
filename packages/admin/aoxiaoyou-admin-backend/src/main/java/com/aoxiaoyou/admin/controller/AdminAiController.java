package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
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
import com.aoxiaoyou.admin.service.AdminAiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/ai")
public class AdminAiController {

    private final AdminAiService adminAiService;

    @GetMapping("/overview")
    public ApiResponse<AdminAiOverviewResponse> overview(HttpServletRequest request) {
        return ApiResponse.success(adminAiService.getOverview(
                (Long) request.getAttribute("adminUserId"),
                readRoles(request)
        ));
    }

    @GetMapping("/capabilities")
    public ApiResponse<List<AdminAiCapabilityResponse>> listCapabilities() {
        return ApiResponse.success(adminAiService.listCapabilities());
    }

    @GetMapping("/provider-templates")
    public ApiResponse<List<AdminAiProviderTemplateResponse>> listProviderTemplates() {
        return ApiResponse.success(adminAiService.listProviderTemplates());
    }

    @GetMapping("/providers")
    public ApiResponse<List<AdminAiProviderResponse>> listProviders() {
        return ApiResponse.success(adminAiService.listProviders());
    }

    @PostMapping("/providers")
    public ApiResponse<AdminAiProviderResponse> createProvider(@Valid @RequestBody AdminAiProviderUpsertRequest request) {
        return ApiResponse.success(adminAiService.createProvider(request));
    }

    @PutMapping("/providers/{id}")
    public ApiResponse<AdminAiProviderResponse> updateProvider(@PathVariable Long id,
                                                               @Valid @RequestBody AdminAiProviderUpsertRequest request) {
        return ApiResponse.success(adminAiService.updateProvider(id, request));
    }

    @DeleteMapping("/providers/{id}")
    public ApiResponse<Boolean> deleteProvider(@PathVariable Long id) {
        adminAiService.deleteProvider(id);
        return ApiResponse.success(Boolean.TRUE);
    }

    @PostMapping("/providers/{id}/test")
    public ApiResponse<AdminAiProviderTestResponse> testProvider(@PathVariable Long id,
                                                                 @RequestBody(required = false) AdminAiProviderTestRequest request,
                                                                 HttpServletRequest httpRequest) {
        return ApiResponse.success(adminAiService.testProvider(
                id,
                request == null ? new AdminAiProviderTestRequest() : request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername")
        ));
    }

    @PostMapping("/providers/{id}/sync-inventory")
    public ApiResponse<AdminAiProviderSyncJobResponse> syncProviderInventory(@PathVariable Long id,
                                                                             HttpServletRequest request) {
        return ApiResponse.success(adminAiService.syncProviderInventory(
                id,
                (Long) request.getAttribute("adminUserId"),
                (String) request.getAttribute("adminUsername")
        ));
    }

    @GetMapping("/providers/{id}/sync-jobs")
    public ApiResponse<List<AdminAiProviderSyncJobResponse>> listProviderSyncJobs(@PathVariable Long id) {
        return ApiResponse.success(adminAiService.listProviderSyncJobs(id));
    }

    @GetMapping("/inventory")
    public ApiResponse<List<AdminAiInventoryResponse>> listInventory(@RequestParam(required = false) Long providerId,
                                                                     @RequestParam(required = false) String capabilityCode,
                                                                     @RequestParam(required = false) String sourceType) {
        return ApiResponse.success(adminAiService.listInventory(providerId, capabilityCode, sourceType));
    }

    @PostMapping("/inventory")
    public ApiResponse<AdminAiInventoryResponse> createInventory(@Valid @RequestBody AdminAiInventoryUpsertRequest request) {
        return ApiResponse.success(adminAiService.createInventory(request));
    }

    @PutMapping("/inventory/{id}")
    public ApiResponse<AdminAiInventoryResponse> updateInventory(@PathVariable Long id,
                                                                 @Valid @RequestBody AdminAiInventoryUpsertRequest request) {
        return ApiResponse.success(adminAiService.updateInventory(id, request));
    }

    @DeleteMapping("/inventory/{id}")
    public ApiResponse<Boolean> deleteInventory(@PathVariable Long id) {
        adminAiService.deleteInventory(id);
        return ApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/voices")
    public ApiResponse<List<AdminAiVoiceResponse>> listVoices(@RequestParam(required = false) Long providerId,
                                                              @RequestParam(required = false) String modelCode,
                                                              @RequestParam(required = false) String languageCode,
                                                              @RequestParam(required = false) String sourceType,
                                                              HttpServletRequest request) {
        return ApiResponse.success(adminAiService.listVoices(
                providerId,
                modelCode,
                languageCode,
                sourceType,
                (Long) request.getAttribute("adminUserId"),
                readRoles(request)
        ));
    }

    @PostMapping("/providers/{id}/sync-voices")
    public ApiResponse<List<AdminAiVoiceResponse>> syncVoices(@PathVariable Long id,
                                                              @RequestBody(required = false) AdminAiVoiceSyncRequest request,
                                                              HttpServletRequest httpRequest) {
        return ApiResponse.success(adminAiService.syncVoices(
                id,
                request == null ? new AdminAiVoiceSyncRequest() : request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername"),
                readRoles(httpRequest)
        ));
    }

    @PostMapping("/voices/preview")
    public ApiResponse<AdminAiVoicePreviewResponse> previewVoice(@Valid @RequestBody AdminAiVoicePreviewRequest request,
                                                                 HttpServletRequest httpRequest) {
        return ApiResponse.success(adminAiService.previewVoice(
                request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername")
        ));
    }

    @PostMapping("/voices/clone")
    public ApiResponse<AdminAiVoiceResponse> createVoiceClone(@Valid @RequestBody AdminAiVoiceCloneRequest request,
                                                              HttpServletRequest httpRequest) {
        return ApiResponse.success(adminAiService.createVoiceClone(
                request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername")
        ));
    }

    @PostMapping("/voices/{voiceId}/refresh")
    public ApiResponse<AdminAiVoiceResponse> refreshVoice(@PathVariable Long voiceId, HttpServletRequest request) {
        return ApiResponse.success(adminAiService.refreshVoice(
                voiceId,
                (Long) request.getAttribute("adminUserId"),
                readRoles(request)
        ));
    }

    @DeleteMapping("/voices/{voiceId}")
    public ApiResponse<Boolean> deleteVoice(@PathVariable Long voiceId, HttpServletRequest request) {
        adminAiService.deleteVoice(
                voiceId,
                (Long) request.getAttribute("adminUserId"),
                readRoles(request)
        );
        return ApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/policies")
    public ApiResponse<List<AdminAiPolicyResponse>> listPolicies(@RequestParam(required = false) String capabilityCode) {
        return ApiResponse.success(adminAiService.listPolicies(capabilityCode));
    }

    @PostMapping("/policies")
    public ApiResponse<AdminAiPolicyResponse> createPolicy(@Valid @RequestBody AdminAiPolicyUpsertRequest request) {
        return ApiResponse.success(adminAiService.createPolicy(request));
    }

    @PutMapping("/policies/{id}")
    public ApiResponse<AdminAiPolicyResponse> updatePolicy(@PathVariable Long id,
                                                           @Valid @RequestBody AdminAiPolicyUpsertRequest request) {
        return ApiResponse.success(adminAiService.updatePolicy(id, request));
    }

    @DeleteMapping("/policies/{id}")
    public ApiResponse<Boolean> deletePolicy(@PathVariable Long id) {
        adminAiService.deletePolicy(id);
        return ApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/quota-rules")
    public ApiResponse<List<AdminAiQuotaRuleResponse>> listQuotaRules(@RequestParam(required = false) String capabilityCode) {
        return ApiResponse.success(adminAiService.listQuotaRules(capabilityCode));
    }

    @PostMapping("/quota-rules")
    public ApiResponse<AdminAiQuotaRuleResponse> createQuotaRule(@Valid @RequestBody AdminAiQuotaRuleUpsertRequest request) {
        return ApiResponse.success(adminAiService.createQuotaRule(request));
    }

    @PutMapping("/quota-rules/{id}")
    public ApiResponse<AdminAiQuotaRuleResponse> updateQuotaRule(@PathVariable Long id,
                                                                 @Valid @RequestBody AdminAiQuotaRuleUpsertRequest request) {
        return ApiResponse.success(adminAiService.updateQuotaRule(id, request));
    }

    @DeleteMapping("/quota-rules/{id}")
    public ApiResponse<Boolean> deleteQuotaRule(@PathVariable Long id) {
        adminAiService.deleteQuotaRule(id);
        return ApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/logs")
    public ApiResponse<PageResponse<AdminAiLogResponse>> pageLogs(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String capabilityCode,
            @RequestParam(required = false) Integer success,
            @RequestParam(required = false) Long providerId) {
        return ApiResponse.success(adminAiService.pageLogs(pageNum, pageSize, capabilityCode, success, providerId));
    }

    @GetMapping("/prompt-templates")
    public ApiResponse<List<AdminAiPromptTemplateResponse>> listPromptTemplates(
            @RequestParam(required = false) String capabilityCode,
            @RequestParam(required = false) String templateType) {
        return ApiResponse.success(adminAiService.listPromptTemplates(capabilityCode, templateType));
    }

    @PostMapping("/prompt-templates")
    public ApiResponse<AdminAiPromptTemplateResponse> createPromptTemplate(
            @Valid @RequestBody AdminAiPromptTemplateUpsertRequest request) {
        return ApiResponse.success(adminAiService.createPromptTemplate(request));
    }

    @PutMapping("/prompt-templates/{id}")
    public ApiResponse<AdminAiPromptTemplateResponse> updatePromptTemplate(
            @PathVariable Long id,
            @Valid @RequestBody AdminAiPromptTemplateUpsertRequest request) {
        return ApiResponse.success(adminAiService.updatePromptTemplate(id, request));
    }

    @DeleteMapping("/prompt-templates/{id}")
    public ApiResponse<Boolean> deletePromptTemplate(@PathVariable Long id) {
        adminAiService.deletePromptTemplate(id);
        return ApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/generation-jobs")
    public ApiResponse<PageResponse<AdminAiGenerationJobResponse>> pageGenerationJobs(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String capabilityCode,
            @RequestParam(required = false) String generationType,
            @RequestParam(required = false) String jobStatus,
            HttpServletRequest request) {
        return ApiResponse.success(adminAiService.pageGenerationJobs(
                pageNum,
                pageSize,
                capabilityCode,
                generationType,
                jobStatus,
                (Long) request.getAttribute("adminUserId"),
                readRoles(request)
        ));
    }

    @GetMapping("/generation-jobs/{jobId}")
    public ApiResponse<AdminAiGenerationJobResponse> getGenerationJob(@PathVariable Long jobId, HttpServletRequest request) {
        return ApiResponse.success(adminAiService.getGenerationJob(
                jobId,
                (Long) request.getAttribute("adminUserId"),
                readRoles(request)
        ));
    }

    @PostMapping("/generation-jobs")
    public ApiResponse<AdminAiGenerationJobResponse> createGenerationJob(
            @Valid @RequestBody AdminAiGenerationJobCreateRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(adminAiService.createGenerationJob(
                request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername"),
                readRoles(httpRequest)
        ));
    }

    @PostMapping("/generation-jobs/{jobId}/refresh")
    public ApiResponse<AdminAiGenerationJobResponse> refreshGenerationJob(@PathVariable Long jobId, HttpServletRequest request) {
        return ApiResponse.success(adminAiService.refreshGenerationJob(
                jobId,
                (Long) request.getAttribute("adminUserId"),
                readRoles(request)
        ));
    }

    @PostMapping("/generation-candidates/{candidateId}/finalize")
    public ApiResponse<AdminAiGenerationJobResponse> finalizeCandidate(
            @PathVariable Long candidateId,
            @RequestBody(required = false) AdminAiCandidateFinalizeRequest request,
            HttpServletRequest httpRequest) {
        return ApiResponse.success(adminAiService.finalizeCandidate(
                candidateId,
                request == null ? new AdminAiCandidateFinalizeRequest() : request,
                (Long) httpRequest.getAttribute("adminUserId"),
                (String) httpRequest.getAttribute("adminUsername"),
                readRoles(httpRequest)
        ));
    }

    @PostMapping("/generation-candidates/{candidateId}/restore")
    public ApiResponse<AdminAiGenerationJobResponse> restoreCandidate(@PathVariable Long candidateId, HttpServletRequest request) {
        return ApiResponse.success(adminAiService.restoreCandidate(
                candidateId,
                (Long) request.getAttribute("adminUserId"),
                readRoles(request)
        ));
    }

    @GetMapping("/platform-settings")
    public ApiResponse<AdminAiPlatformSettingsResponse> getPlatformSettings() {
        return ApiResponse.success(adminAiService.getPlatformSettings());
    }

    @PutMapping("/platform-settings")
    public ApiResponse<AdminAiPlatformSettingsResponse> updatePlatformSettings(
            @RequestBody(required = false) AdminAiPlatformSettingsUpsertRequest request) {
        return ApiResponse.success(adminAiService.updatePlatformSettings(
                request == null ? new AdminAiPlatformSettingsUpsertRequest() : request
        ));
    }

    @SuppressWarnings("unchecked")
    private List<String> readRoles(HttpServletRequest request) {
        Object roles = request.getAttribute("adminRoles");
        return roles instanceof List<?> list ? (List<String>) list : Collections.emptyList();
    }
}
