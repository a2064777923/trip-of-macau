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
    private String localeCode;
    private Long fileSizeBytes;
    private Integer widthPx;
    private Integer heightPx;
    private String checksum;
    private String etag;
    private String status;
}
