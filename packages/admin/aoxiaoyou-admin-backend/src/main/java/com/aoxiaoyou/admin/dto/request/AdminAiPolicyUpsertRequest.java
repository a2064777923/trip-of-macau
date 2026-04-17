package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminAiPolicyUpsertRequest {

    @NotBlank
    private String capabilityCode;

    @NotBlank
    private String policyCode;

    @NotBlank
    private String policyName;

    private String policyType;

    private String executionMode;

    private String responseMode;

    private String scenePresetCode;

    private String defaultModel;

    private String systemPrompt;

    private String promptTemplate;

    private String responseSchemaJson;

    private String postProcessRulesJson;

    private String parameterConfigJson;

    private String expertOverrideJson;

    private Long manualSwitchProviderId;

    private Integer multimodalEnabled;

    private Integer voiceEnabled;

    private Integer structuredOutputEnabled;

    private BigDecimal temperature;

    private Integer maxTokens;

    private String status;

    private Integer sortOrder;

    private String notes;

    private List<ProviderBinding> providerBindings;

    @Data
    public static class ProviderBinding {
        private Long providerId;
        private Long inventoryId;
        private String bindingRole;
        private String routeMode;
        private Integer sortOrder;
        private Integer enabled;
        private String modelOverride;
        private Integer weightPercent;
        private Integer timeoutMsOverride;
        private Integer retryCountOverride;
        private String parameterOverrideJson;
        private String notes;
    }
}
