package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class AdminStoryMaterialProductionRequest {

    @Data
    public static class PreflightRequest {
        private List<String> itemKeys;
        private List<String> targetAssetKinds;
        private BigDecimal estimatedTotalCost;
        private BigDecimal dailyCostCeiling;
        private BigDecimal batchCostCeiling;
        private Boolean confirmCostOverride;
        private String verificationNote;
    }

    @Data
    public static class LocalImportRequest {
        private String relativeLocalPath;
        private String forcedCosObjectKey;
        private Long parentVersionId;
        private CropRect cropRect;
        private String subtitlePath;
        private String providerName;
        private String modelCode;
        private BigDecimal estimatedCost;
        private BigDecimal actualCost;
        private String currencyCode;
        private String promptText;
        private String scriptText;
        private String assetKind;
        private String subtitleMetadataJson;
        private String posterFallbackItemKey;
        private String verificationNote;
        private String localeCode;
    }

    @Data
    public static class AiCandidateBindRequest {
        private Long aiCandidateId;
        private Long parentVersionId;
        private CropRect cropRect;
        private String subtitlePath;
        private String providerName;
        private String modelCode;
        private BigDecimal estimatedCost;
        private BigDecimal actualCost;
        private String currencyCode;
        private String promptText;
        private String scriptText;
        private String assetKind;
        private String subtitleMetadataJson;
        private String posterFallbackItemKey;
        private String verificationNote;
    }

    @Data
    public static class PromoteRequest {
        private Long versionId;
        private String targetStatus;
        private String verificationNote;
        private Boolean superAdminConfirmation;
    }

    @Data
    public static class RollbackRequest {
        private Long rollbackVersionId;
        private String verificationNote;
        private Boolean superAdminConfirmation;
    }

    @Data
    public static class VersionHistoryQuery {
        private Long pageNum;
        private Long pageSize;
        private String promotionStatus;
    }

    @Data
    public static class CropRect {
        private Integer x;
        private Integer y;
        private Integer width;
        private Integer height;
    }
}
