package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminStoryMaterialProductionResponse {

    @Data
    @Builder
    public static class PreflightResponse {
        private Long packageId;
        private Integer itemCount;
        private List<String> targetAssetKinds;
        private BigDecimal estimatedTotalCost;
        private BigDecimal dailyCostCeiling;
        private BigDecimal batchCostCeiling;
        private Boolean requiresSuperAdminConfirmation;
        private String verificationNote;
        private List<BatchRiskItem> risks;
    }

    @Data
    @Builder
    public static class BatchRiskItem {
        private String itemKey;
        private String assetKind;
        private BigDecimal estimatedCost;
        private String riskCode;
        private String message;
        private Boolean requiresSuperAdminConfirmation;
    }

    @Data
    @Builder
    public static class PackageItemVersionResponse {
        private Long id;
        private Long packageItemId;
        private Integer versionNo;
        private String versionStatus;
        private String promotionStatus;
        private Long contentAssetId;
        private Long aiJobId;
        private Long aiCandidateId;
        private String sourceType;
        private String providerName;
        private String modelCode;
        private Long parentVersionId;
        private String parentItemKey;
        private String localPath;
        private String cosObjectKey;
        private String canonicalUrl;
        private String assetKind;
        private String posterFallbackItemKey;
        private String promptText;
        private String scriptText;
        private String provenanceJson;
        private String cropMetadataJson;
        private String subtitleMetadataJson;
        private BigDecimal estimatedCost;
        private BigDecimal actualCost;
        private String currencyCode;
        private Long verifiedByAdminId;
        private String verifiedByAdminName;
        private LocalDateTime verifiedAt;
        private Long rollbackOfVersionId;
        private Long createdByAdminId;
        private String createdByAdminName;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class PromotionResult {
        private Long packageId;
        private Long itemId;
        private Long versionId;
        private Integer versionNo;
        private String targetStatus;
        private String itemStatus;
        private Boolean requiresSuperAdminConfirmation;
        private PackageItemVersionResponse version;
    }

    @Data
    @Builder
    public static class RollbackResult {
        private Long packageId;
        private Long itemId;
        private Long previousVersionId;
        private Long currentVersionId;
        private Integer currentVersionNo;
        private String itemStatus;
        private Boolean requiresSuperAdminConfirmation;
        private PackageItemVersionResponse version;
    }
}
