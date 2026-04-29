package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AdminRewardPresentationUpsertRequest {

    @NotBlank(message = "code is required")
    private String code;

    @NotBlank(message = "nameZh is required")
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
    private List<StepPayload> steps;

    @Data
    public static class StepPayload {
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
