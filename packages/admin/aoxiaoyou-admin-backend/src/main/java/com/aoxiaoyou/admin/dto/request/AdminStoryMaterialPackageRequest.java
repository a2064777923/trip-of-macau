package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class AdminStoryMaterialPackageRequest {

    @Data
    public static class PackageQuery {
        private Long pageNum;
        private Long pageSize;
        private String keyword;
        private String packageStatus;
        private Long storylineId;
    }

    @Data
    public static class PackageUpsert {
        @NotBlank(message = "code is required")
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
    }

    @Data
    public static class ItemUpsert {
        @NotBlank(message = "itemKey is required")
        private String itemKey;
        @NotBlank(message = "itemType is required")
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
    }
}
