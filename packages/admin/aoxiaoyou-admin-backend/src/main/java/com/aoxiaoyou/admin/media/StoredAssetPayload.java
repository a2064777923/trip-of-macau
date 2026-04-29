package com.aoxiaoyou.admin.media;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StoredAssetPayload {
    byte[] bytes;
    String originalFilename;
    String contentType;
    String assetKind;
    String localeCode;
}
