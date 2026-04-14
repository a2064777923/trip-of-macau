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
    private String localeCode;
    private Long fileSizeBytes;
    private Integer widthPx;
    private Integer heightPx;
    private String checksum;
    private String etag;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
