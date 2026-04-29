package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminAiVoiceResponse {

    private Long id;
    private Long providerId;
    private String providerName;
    private String providerDisplayName;
    private String providerPlatformCode;
    private String inventoryCode;
    private String externalId;
    private String voiceCode;
    private String parentModelCode;
    private String displayName;
    private String sourceType;
    private String availabilityStatus;
    private String cloneStatus;
    private String previewUrl;
    private String previewText;
    private List<String> languageCodes;
    private Long ownerAdminId;
    private String ownerAdminName;
    private Long sourceAssetId;
    private String featureFlagsJson;
    private String rawPayloadJson;
    private LocalDateTime syncedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastVerifiedAt;
}
