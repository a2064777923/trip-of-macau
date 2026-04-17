package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminAiGenerationJobResponse {

    private Long id;
    private Long capabilityId;
    private String capabilityCode;
    private String capabilityNameZht;
    private Long policyId;
    private String policyName;
    private Long promptTemplateId;
    private String promptTemplateName;
    private Long providerId;
    private String providerName;
    private Long inventoryId;
    private String inventoryCode;
    private String inventoryDisplayName;
    private Long ownerAdminId;
    private String ownerAdminName;
    private String generationType;
    private String sourceScope;
    private Long sourceScopeId;
    private String jobStatus;
    private String promptTitle;
    private String promptText;
    private String promptVariablesJson;
    private String requestPayloadJson;
    private String providerRequestId;
    private String resultSummary;
    private String errorMessage;
    private Long latestCandidateId;
    private Long finalizedCandidateId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AdminAiGenerationCandidateResponse> candidates;
}
