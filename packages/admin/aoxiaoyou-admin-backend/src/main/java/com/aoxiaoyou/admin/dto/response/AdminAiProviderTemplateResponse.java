package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class AdminAiProviderTemplateResponse {

    private String platformCode;
    private String platformLabel;
    private String description;
    private String providerType;
    private String endpointStyle;
    private String defaultBaseUrl;
    private String docsUrl;
    private String authScheme;
    private String syncStrategy;
    private String inventorySemantics;
    private String defaultModelName;
    private List<String> supportedModalities;
    private List<Map<String, Object>> credentialFields;
}
