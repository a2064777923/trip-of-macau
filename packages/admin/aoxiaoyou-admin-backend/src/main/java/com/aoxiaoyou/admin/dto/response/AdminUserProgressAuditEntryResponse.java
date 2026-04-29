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
public class AdminUserProgressAuditEntryResponse {

    private Long id;
    private Long userId;
    private String scopeType;
    private Long scopeId;
    private Long storylineId;
    private String actionType;
    private Long operatorId;
    private String operatorName;
    private String reason;
    private String requestIp;
    private Map<String, Object> previewSummary;
    private Map<String, Object> resultSummary;
    private LocalDateTime timestamp;
}
