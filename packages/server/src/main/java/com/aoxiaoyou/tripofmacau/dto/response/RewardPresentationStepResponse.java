package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RewardPresentationStepResponse {
    private String stepType;
    private String stepCode;
    private String titleText;
    private String assetUrl;
    private Integer durationMs;
    private Integer skippableOverride;
    private String triggerSfxUrl;
    private String voiceOverUrl;
    private String overlayConfigJson;
    private Integer sortOrder;
}
