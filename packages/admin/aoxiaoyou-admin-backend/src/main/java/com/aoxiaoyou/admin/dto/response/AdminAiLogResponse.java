package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminAiLogResponse {
    private Long id;
    private Long providerId;
    private String providerName;
    private Long inventoryId;
    private String inventoryCode;
    private String modelCode;
    private Long policyId;
    private String policyName;
    private String capabilityCode;
    private Long adminOwnerId;
    private String adminOwnerName;
    private String userOpenid;
    private String requestType;
    private String inputDataHash;
    private String outputSummary;
    private Integer latencyMs;
    private Integer tokensUsed;
    private BigDecimal costUsd;
    private String costLabel;
    private String costType;
    private Integer success;
    private Integer fallbackTriggered;
    private String blockedReason;
    private String traceId;
    private String errorMessage;
    private String safeOutputSummary;
    private LocalDateTime createdAt;
}
