package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminRewardGovernanceResponse {
    private GovernanceSummary summary;
    private List<AdminRewardRuleResponse> rules;
    private List<AdminRewardPresentationResponse> presentations;

    @Data
    @Builder
    public static class GovernanceSummary {
        private Integer redeemablePrizeCount;
        private Integer gameRewardCount;
        private Integer honorCount;
        private Integer ruleCount;
        private Integer presentationCount;
        private Integer linkedIndoorBehaviorCount;
    }
}
