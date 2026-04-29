package com.aoxiaoyou.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserProgressOperationPreviewResponse {

    private Long userId;
    private String scopeType;
    private Long scopeId;
    private Long storylineId;
    private LocalDateTime from;
    private LocalDateTime to;
    private String actionType;
    private String confirmationText;
    private String previewHash;
    private String confirmationToken;
    private Integer affectedUserCount;
    private Integer affectedScopeCount;
    private Integer matchingEventCount;
    private Integer availableElementCount;
    private Integer completedElementCount;
    private Map<String, Object> previewSummary;
}
