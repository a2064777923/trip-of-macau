package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AdminIndoorRuleValidationResponse {
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
    private BigDecimal normalizedRelativeX;
    private BigDecimal normalizedRelativeY;
    private String resolvedOverlayType;
    private Integer behaviorCount;
}
