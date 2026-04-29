package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminRewardRuleResponse {
    private Long id;
    private String code;
    private String ruleType;
    private String status;
    private String nameZh;
    private String nameZht;
    private String summaryText;
    private String advancedConfigJson;
    private List<ConditionGroupItem> conditionGroups;
    private List<AdminRewardLinkedEntityResponse> linkedOwners;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class ConditionGroupItem {
        private Long id;
        private String groupCode;
        private String operatorType;
        private Integer minimumMatchCount;
        private String summaryText;
        private String advancedConfigJson;
        private Integer sortOrder;
        private List<ConditionItem> conditions;
    }

    @Data
    @Builder
    public static class ConditionItem {
        private Long id;
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
