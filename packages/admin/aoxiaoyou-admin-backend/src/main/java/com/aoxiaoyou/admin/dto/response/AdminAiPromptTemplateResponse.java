package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminAiPromptTemplateResponse {

    private Long id;
    private Long capabilityId;
    private String capabilityCode;
    private String capabilityNameZht;
    private String templateCode;
    private String templateName;
    private String templateType;
    private String assetSlotCode;
    private String systemPrompt;
    private String promptTemplate;
    private String variableSchemaJson;
    private String outputConstraintsJson;
    private Long defaultProviderId;
    private String defaultProviderName;
    private Long defaultPolicyId;
    private String defaultPolicyName;
    private String status;
    private Integer sortOrder;
    private LocalDateTime updatedAt;
}
