package com.aoxiaoyou.admin.media;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProcessedMediaPayload {
    byte[] bytes;
    String originalFilename;
    String contentType;
    Integer widthPx;
    Integer heightPx;
    String processingStatus;
    String processingNote;
}
