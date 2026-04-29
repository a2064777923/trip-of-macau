package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserProgressSummaryResponse {
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
}
