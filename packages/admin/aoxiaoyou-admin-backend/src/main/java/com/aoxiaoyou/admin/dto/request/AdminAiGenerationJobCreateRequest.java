package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminAiGenerationJobCreateRequest {

    @NotBlank
    private String capabilityCode;

    private Long policyId;

    private Long promptTemplateId;

    private Long providerId;

    private Long inventoryId;

    @NotBlank
    private String generationType;

    private String sourceScope;

    private Long sourceScopeId;

    private String promptTitle;

    private String promptText;

    private String promptVariablesJson;

    private String requestPayloadJson;
}
