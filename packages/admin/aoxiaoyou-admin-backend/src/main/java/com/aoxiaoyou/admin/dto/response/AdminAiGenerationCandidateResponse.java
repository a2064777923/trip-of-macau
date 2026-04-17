package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminAiGenerationCandidateResponse {

    private Long id;
    private Integer candidateIndex;
    private String candidateType;
    private String storageUrl;
    private String mimeType;
    private Long fileSizeBytes;
    private Integer widthPx;
    private Integer heightPx;
    private Integer durationMs;
    private String transcriptText;
    private String previewText;
    private String metadataJson;
    private Integer isSelected;
    private Integer isFinalized;
    private Long finalizedAssetId;
    private LocalDateTime createdAt;
}
