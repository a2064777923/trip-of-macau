package com.aoxiaoyou.admin.media;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class ResolvedMediaUploadPolicy {
    String assetKind;
    String policyFamily;
    String requestedPolicyCode;
    String effectivePolicyCode;
    Long maxFileSizeBytes;
    Integer qualityPercent;
    Integer maxWidthPx;
    Integer maxHeightPx;
    Boolean preserveMetadata;
    Boolean uploaderAllowsLossless;
    String note;
    Map<String, Object> snapshot;
}
