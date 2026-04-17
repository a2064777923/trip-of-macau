package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAiQuotaRuleResponse {

    private Long id;
    private Long capabilityId;
    private String capabilityCode;
    private String capabilityNameZht;
    private Long policyId;
    private String scopeType;
    private String scopeValue;
    private String windowType;
    private Integer windowSize;
    private Integer requestLimit;
    private Integer tokenLimit;
    private Integer suspiciousConcurrencyThreshold;
    private String actionMode;
    private String status;
    private String notes;
}
