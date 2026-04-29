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
public class AdminUserProgressOperationResultResponse {

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
    private String status;
    private Integer writtenStateRows;
    private Integer mutatedEventRows;
    private Integer deletedEventRows;
    private Map<String, Object> resultSummary;
}
