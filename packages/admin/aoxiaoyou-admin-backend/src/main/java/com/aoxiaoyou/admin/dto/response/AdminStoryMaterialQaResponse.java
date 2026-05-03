package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminStoryMaterialQaResponse {

    @Data
    @Builder
    public static class QaOverview {
        private Long packageId;
        private Integer totalItems;
        private Integer blockingCount;
        private Integer warningCount;
        private Integer infoCount;
        private Map<String, Long> healthStateCounters;
        private Map<String, Long> assetKindCounters;
        private Map<String, Long> statusCounters;
        private Map<String, Long> chapterCodeCounters;
        private Map<String, Long> runtimeExposureCounters;
    }

    @Data
    @Builder
    public static class QaItem {
        private Long id;
        private Long packageId;
        private String itemKey;
        private String itemType;
        private String assetKind;
        private String targetType;
        private Long targetId;
        private String targetCode;
        private Long assetId;
        private Long currentVersionId;
        private Integer currentVersionNo;
        private Long publishedVersionId;
        private Integer publishedVersionNo;
        private AdminStoryMaterialProductionResponse.PackageItemVersionResponse currentVersion;
        private AdminStoryMaterialProductionResponse.PackageItemVersionResponse publishedVersion;
        private String previewUrl;
        private String canonicalUrl;
        private String localPath;
        private String cosObjectKey;
        private List<String> healthStates;
        private Integer findingsCount;
        private String runtimeExposure;
        private String usageTarget;
        private String chapterCode;
        private String itemStatus;
        private String providerName;
        private String modelCode;
        private List<String> usageTargets;
        private LocalDateTime lastProducedAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class QaDetail {
        private QaItem item;
        private List<AdminStoryMaterialProductionResponse.PackageItemVersionResponse> versions;
        private ContentAssetSummary currentContentAsset;
        private ContentAssetSummary publishedContentAsset;
        private List<QaFinding> findings;
        private List<String> availableActions;
    }

    @Data
    @Builder
    public static class ContentAssetSummary {
        private Long id;
        private String assetKind;
        private String canonicalUrl;
        private String objectKey;
        private String bucketName;
        private String region;
        private String mimeType;
        private String originalFilename;
        private Long fileSizeBytes;
        private Integer widthPx;
        private Integer heightPx;
        private String processingStatus;
        private String status;
    }

    @Data
    @Builder
    public static class QaFinding {
        private String severity;
        private String findingCode;
        private String messageZht;
        private String sourceType;
        private String sourceId;
        private String expectedValue;
        private String actualValue;
        private String actionHintZht;
    }

    @Data
    @Builder
    public static class QaActionResult {
        private Long itemId;
        private Long currentVersionId;
        private String currentStatus;
        private String targetStatus;
        private String messageZht;
    }

    @Data
    @Builder
    public static class ConsistencyReport {
        private Long packageId;
        private LocalDateTime checkedAt;
        private Integer blockingCount;
        private Integer warningCount;
        private Integer infoCount;
        private List<QaFinding> findings;
    }
}
