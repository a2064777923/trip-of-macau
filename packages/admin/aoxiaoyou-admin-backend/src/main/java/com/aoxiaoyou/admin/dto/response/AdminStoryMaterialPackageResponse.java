package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class AdminStoryMaterialPackageResponse {

    @Data
    @Builder
    public static class PackageCounters {
        private Integer materialCount;
        private Integer assetCount;
        private Integer storyObjectCount;
    }

    @Data
    @Builder
    public static class PackageSummary {
        private Long id;
        private String code;
        private Long storylineId;
        private String titleZh;
        private String titleZht;
        private String titleEn;
        private String titlePt;
        private String summaryZh;
        private String summaryZht;
        private String packageStatus;
        private PackageCounters counters;
        private String localRoot;
        private String cosPrefix;
        private String manifestPath;
        private Long createdByAdminId;
        private String createdByAdminName;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class PackageDetail {
        private Long id;
        private String code;
        private Long storylineId;
        private String titleZh;
        private String titleZht;
        private String titleEn;
        private String titlePt;
        private String summaryZh;
        private String summaryZht;
        private String historicalBasisZh;
        private String historicalBasisZht;
        private String literaryDramatizationZh;
        private String literaryDramatizationZht;
        private String localRoot;
        private String cosPrefix;
        private String manifestPath;
        private String manifestJson;
        private String packageStatus;
        private PackageCounters counters;
        private Long createdByAdminId;
        private String createdByAdminName;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<PackageItem> items;
    }

    @Data
    @Builder
    public static class PackageItem {
        private Long id;
        private Long packageId;
        private String itemKey;
        private String itemType;
        private String assetKind;
        private String targetType;
        private Long targetId;
        private String targetCode;
        private Long assetId;
        private String localPath;
        private String cosObjectKey;
        private String canonicalUrl;
        private String usageTarget;
        private String chapterCode;
        private String provenanceType;
        private String promptText;
        private String scriptText;
        private String historicalBasisZh;
        private String historicalBasisZht;
        private String literaryDramatizationZh;
        private String literaryDramatizationZht;
        private String fallbackItemKey;
        private String status;
        private Integer sortOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
