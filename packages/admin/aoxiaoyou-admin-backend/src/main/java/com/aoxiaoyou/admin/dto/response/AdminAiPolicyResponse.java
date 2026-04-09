package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AdminAiPolicyResponse {
    private Long id;
    private String policyName;
    private String scenarioCode;
    private String policyType;
    private String scenarioGroup;
    private Long providerId;
    private String providerName;
    private String modelOverride;
    private Integer multimodalEnabled;
    private Integer voiceEnabled;
    private BigDecimal temperature;
    private Integer maxTokens;
    private Integer status;
}
