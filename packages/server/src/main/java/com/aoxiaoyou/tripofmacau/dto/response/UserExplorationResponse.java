package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserExplorationResponse {
    private Long userId;
    private String scopeType;
    private Long scopeId;
    private int completedWeight;
    private int availableWeight;
    private double progressPercent;
    private List<ElementProgress> elements;

    @Data
    @Builder
    public static class ElementProgress {
        private Long elementId;
        private String elementCode;
        private String elementType;
        private String title;
        private String weightLevel;
        private int weightValue;
        private boolean completed;
    }
}
