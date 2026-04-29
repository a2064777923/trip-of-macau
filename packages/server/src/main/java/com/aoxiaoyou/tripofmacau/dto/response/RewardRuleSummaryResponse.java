package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RewardRuleSummaryResponse {
    private Long id;
    private String code;
    private String name;
    private String ruleType;
    private String summaryText;
}
