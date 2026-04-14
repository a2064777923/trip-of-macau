package com.aoxiaoyou.admin.media;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StoredAssetMetadata {
    String bucketName;
    String region;
    String objectKey;
    String canonicalUrl;
    String mimeType;
    String localeCode;
    Long fileSizeBytes;
    Integer widthPx;
    Integer heightPx;
    String checksum;
    String etag;
}
