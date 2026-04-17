package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAiProviderBindingResponse {

    private Long id;
    private Long providerId;
    private Long inventoryId;
    private String inventoryCode;
    private String inventoryDisplayName;
    private String providerName;
    private String providerDisplayName;
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
