package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminAiPromptTemplateUpsertRequest {

    @NotBlank
    private String capabilityCode;

    @NotBlank
    private String templateCode;

    @NotBlank
    private String templateName;

    private String templateType;

    private String assetSlotCode;

    private String systemPrompt;

    @NotBlank
    private String promptTemplate;

    private String variableSchemaJson;

    private String outputConstraintsJson;

    private Long defaultProviderId;

    private Long defaultPolicyId;

    private String status;

    private Integer sortOrder;
}
