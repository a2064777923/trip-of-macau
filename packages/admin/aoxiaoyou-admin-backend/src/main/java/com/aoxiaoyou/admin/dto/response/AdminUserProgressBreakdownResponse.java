package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminUserProgressBreakdownResponse {
    private Long userId;
    private String scopeType;
    private Long scopeId;
    private int completedWeight;
    private int availableWeight;
    private int completedElementCount;
    private int availableElementCount;
    private int retiredCompletedWeight;
    private int retiredCompletedCount;
    private double progressPercent;
    private LocalDateTime lastRecomputeTime;
    private List<ElementBreakdown> elements;
    private List<ElementBreakdown> retiredElements;

    @Data
    @Builder
    public static class ElementBreakdown {
        private Long elementId;
        private String elementCode;
        private String elementType;
        private String title;
        private String weightLevel;
        private int weightValue;
        private boolean completed;
        private boolean includedInCurrentPercentage;
        private Long sourceEventId;
        private LocalDateTime eventOccurredAt;
    }
}
