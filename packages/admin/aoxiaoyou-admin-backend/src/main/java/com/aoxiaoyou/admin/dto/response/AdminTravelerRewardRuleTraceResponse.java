package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminTravelerRewardRuleTraceResponse {
    public static final String ELIGIBLE_GRANTED = "eligible_granted";
    public static final String ELIGIBLE_ALREADY_GRANTED = "eligible_already_granted";
    public static final String NOT_ELIGIBLE = "not_eligible";
    public static final String MISSING_LINK = "missing_link";
    public static final String RULE_DISABLED = "rule_disabled";
    public static final String DATA_UNAVAILABLE = "data_unavailable";

    private Long userId;
    private Long sourceEventId;
    private String traceStatus;
    private String traceStatusLabel;
    private String explanation;
    private EventNode event;
    private ElementNode explorationElement;
    private ExperienceNode experienceStep;
    private List<RuleNode> rules;
    private List<GrantNode> grants;
    private List<String> missingLinks;

    @Data
    @Builder
    public static class EventNode {
        private Long eventId;
        private Long elementId;
        private String elementCode;
        private String eventType;
        private String eventSource;
        private String storylineSessionId;
        private String payloadPreview;
        private LocalDateTime occurredAt;
    }

    @Data
    @Builder
    public static class ElementNode {
        private Long elementId;
        private String elementCode;
        private String elementType;
        private String ownerType;
        private Long ownerId;
        private String ownerCode;
        private Long cityId;
        private Long subMapId;
        private Long poiId;
        private Long storylineId;
        private Long chapterId;
        private String title;
        private String status;
    }

    @Data
    @Builder
    public static class ExperienceNode {
        private Long flowStepId;
        private Long flowId;
        private String stepCode;
        private String stepType;
        private String stepName;
        private String status;
    }

    @Data
    @Builder
    public static class RuleNode {
        private Long ruleId;
        private String code;
        private String ruleType;
        private String status;
        private String name;
        private String summaryText;
        private String bindingOwnerDomain;
        private Long bindingOwnerId;
        private String bindingRole;
        private List<ConditionGroupNode> conditionGroups;
    }

    @Data
    @Builder
    public static class ConditionGroupNode {
        private Long groupId;
        private String groupCode;
        private String operatorType;
        private Integer minimumMatchCount;
        private String summaryText;
        private List<ConditionNode> conditions;
    }

    @Data
    @Builder
    public static class ConditionNode {
        private Long conditionId;
        private String conditionType;
        private String metricType;
        private String operatorType;
        private String comparatorValue;
        private String comparatorUnit;
        private String summaryText;
        private String evaluationStatus;
    }

    @Data
    @Builder
    public static class GrantNode {
        private String grantSource;
        private Long grantRowId;
        private Long rewardId;
        private Long gameRewardId;
        private Long sourceEventId;
        private Long sourceRuleId;
        private String grantStatus;
        private LocalDateTime grantedAt;
    }
}
