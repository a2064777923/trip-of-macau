package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AdminAiInventoryUpsertRequest {

    @NotNull
    private Long providerId;

    @NotBlank
    private String inventoryCode;

    @NotBlank
    private String externalId;

    @NotBlank
    private String displayName;

    private String inventoryType;

    private List<String> modalityCodes;

    private List<String> capabilityCodes;

    private String syncStrategy;

    private String sourceType;

    private String availabilityStatus;

    private String endpointPath;

    private Integer contextWindowTokens;

    private BigDecimal inputPricePer1k;

    private BigDecimal outputPricePer1k;

    private BigDecimal imagePricePerCall;

    private BigDecimal audioPricePerMinute;

    private String featureFlagsJson;

    private String rawPayloadJson;

    private Integer isDefault;

    private Integer sortOrder;
}
