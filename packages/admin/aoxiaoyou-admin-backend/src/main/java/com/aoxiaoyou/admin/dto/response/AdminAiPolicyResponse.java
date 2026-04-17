package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminAiPolicyResponse {
    private Long id;
    private Long capabilityId;
    private String capabilityCode;
    private String capabilityNameZht;
    private String policyCode;
    private String policyName;
    private String policyType;
    private String executionMode;
    private String responseMode;
    private String scenePresetCode;
    private Long manualSwitchProviderId;
    private String manualSwitchProviderName;
    private String defaultModel;
    private Integer multimodalEnabled;
    private Integer voiceEnabled;
    private Integer structuredOutputEnabled;
    private BigDecimal temperature;
    private Integer maxTokens;
    private String responseSchemaJson;
    private String postProcessRulesJson;
    private String parameterConfigJson;
    private String expertOverrideJson;
    private String status;
    private Integer sortOrder;
    private String notes;
    private List<AdminAiProviderBindingResponse> providerBindings;
}
