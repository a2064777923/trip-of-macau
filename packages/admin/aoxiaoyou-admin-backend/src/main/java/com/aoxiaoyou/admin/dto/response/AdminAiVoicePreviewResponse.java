package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAiVoicePreviewResponse {

    private Long providerId;
    private String modelCode;
    private String voiceCode;
    private String previewUrl;
    private String mimeType;
    private Long fileSizeBytes;
    private String metadataJson;
}
