package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminOperationLogResponse {

    private Long id;
    private String operationType;
    private String operationTypeName;
    private String operationDesc;
    private String adminName;
    private String ipAddress;
    private LocalDateTime createTime;
}
