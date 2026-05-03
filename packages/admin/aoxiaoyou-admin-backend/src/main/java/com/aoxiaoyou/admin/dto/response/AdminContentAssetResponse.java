package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminContentAssetResponse {
    private Long id;
    private String assetKind;
    private String bucketName;
    private String region;
    private String objectKey;
    private String canonicalUrl;
    private String mimeType;
    private String animationSubtype;
    private Long posterAssetId;
    private Long fallbackAssetId;
    private Boolean defaultLoop;
    private Boolean defaultAutoplay;
    private String localeCode;
    private String originalFilename;
    private String fileExtension;
    private String uploadSource;
    private String clientRelativePath;
    private Long uploadedByAdminId;
    private String uploadedByAdminName;
    private Long fileSizeBytes;
    private Integer widthPx;
    private Integer heightPx;
    private String checksum;
    private String etag;
    private String processingPolicyCode;
    private String processingProfileJson;
    private String processingStatus;
    private String processingNote;
    private String status;
    private Long materialPackageId;
    private String materialPackageCode;
    private String materialPackageTitleZht;
    private Long materialItemId;
    private String materialItemKey;
    private String materialItemStatus;
    private Long materialVersionId;
    private String materialPromotionStatus;
    private String usageTarget;
    private String chapterCode;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
