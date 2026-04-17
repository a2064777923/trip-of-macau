package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminAiInventoryResponse {

    private Long id;
    private Long providerId;
    private String providerName;
    private String providerDisplayName;
    private String providerPlatformCode;
    private String inventoryCode;
    private String externalId;
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
    private LocalDateTime lastSeenAt;
    private LocalDateTime syncedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
