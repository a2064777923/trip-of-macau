package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminContentAssetUpsertRequest {

    @NotBlank(message = "assetKind is required")
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
    private String publishedAt;
}
