package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminAiQuotaRuleUpsertRequest {

    @NotBlank
    private String capabilityCode;

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
