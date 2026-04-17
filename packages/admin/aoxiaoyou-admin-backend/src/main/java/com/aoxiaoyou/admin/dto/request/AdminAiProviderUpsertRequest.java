package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminAiProviderUpsertRequest {

    @NotBlank
    private String providerName;

    private String platformCode;

    @NotBlank
    private String displayName;

    private String platformLabel;

    private String providerType;

    private String endpointStyle;

    private String syncStrategy;

    private String authScheme;

    @NotBlank
    private String apiBaseUrl;

    private String docsUrl;

    private String modelName;

    private List<String> capabilityCodes;

    private String featureFlagsJson;

    private String credentialSchemaJson;

    private String providerSettingsJson;

    private Integer requestTimeoutMs;

    private Integer maxRetries;

    private Integer quotaDaily;

    private BigDecimal costPer1kTokens;

    private Integer status;

    private String apiKey;

    private Boolean replaceApiKey;

    private String apiSecret;

    private Boolean replaceApiSecret;
}
