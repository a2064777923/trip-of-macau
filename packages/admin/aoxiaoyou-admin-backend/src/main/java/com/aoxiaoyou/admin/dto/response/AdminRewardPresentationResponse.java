package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminRewardPresentationResponse {
    private Long id;
    private String code;
    private String nameZh;
    private String nameZht;
    private String presentationType;
    private Integer firstTimeOnly;
    private Integer skippable;
    private Integer minimumDisplayMs;
    private String interruptPolicy;
    private String queuePolicy;
    private Integer priorityWeight;
    private Long coverAssetId;
    private Long voiceOverAssetId;
    private Long sfxAssetId;
    private String summaryText;
    private String configJson;
    private String status;
    private List<StepItem> steps;
    private List<AdminRewardLinkedEntityResponse> linkedOwners;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class StepItem {
        private Long id;
        private String stepType;
        private String stepCode;
        private String titleText;
        private Long assetId;
        private Integer durationMs;
        private Integer skippableOverride;
        private Long triggerSfxAssetId;
        private Long voiceOverAssetId;
        private String overlayConfigJson;
        private Integer sortOrder;
    }
}
