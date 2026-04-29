package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AdminRewardRuleUpsertRequest {

    @NotBlank(message = "code is required")
    private String code;

    @NotBlank(message = "nameZh is required")
    private String nameZh;

    private String nameZht;
    private String ruleType;
    private String status;
    private String summaryText;
    private String advancedConfigJson;
    private List<ConditionGroupPayload> conditionGroups;

    @Data
    public static class ConditionGroupPayload {
        private String groupCode;
        private String operatorType;
        private Integer minimumMatchCount;
        private String summaryText;
        private String advancedConfigJson;
        private Integer sortOrder;
        private List<ConditionPayload> conditions;
    }

    @Data
    public static class ConditionPayload {
        private String conditionType;
        private String metricType;
        private String operatorType;
        private String comparatorValue;
        private String comparatorUnit;
        private String summaryText;
        private String configJson;
        private Integer sortOrder;
    }
}
