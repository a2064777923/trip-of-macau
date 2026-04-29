package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RewardPresentationResponse {
    private Long id;
    private String code;
    private String name;
    private String presentationType;
    private Integer firstTimeOnly;
    private Integer skippable;
    private Integer minimumDisplayMs;
    private String interruptPolicy;
    private String queuePolicy;
    private Integer priorityWeight;
    private String coverImageUrl;
    private String voiceOverUrl;
    private String sfxUrl;
    private String summaryText;
    private String configJson;
    private List<RewardPresentationStepResponse> steps;
}
