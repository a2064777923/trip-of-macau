package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class AdminExperienceResponse {

    @Data
    @Builder
    public static class Template {
        private Long id;
        private String code;
        private String templateType;
        private String templateTypeLabelZh;
        private String templateTypeGuidance;
        private String category;
        private String nameZh;
        private String nameEn;
        private String nameZht;
        private String namePt;
        private String summaryZh;
        private String summaryEn;
        private String summaryZht;
        private String summaryPt;
        private String configJson;
        private String schemaJson;
        private String riskLevel;
        private String status;
        private Integer sortOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long usageCount;
    }

    @Data
    @Builder
    public static class TemplatePreset {
        private String presetCode;
        private String templateType;
        private String category;
        private String nameZh;
        private String nameZht;
        private String summaryZh;
        private String summaryZht;
        private String riskLevel;
        private String configJson;
        private String schemaJson;
        private List<String> recommendedTriggerTypes;
        private List<String> recommendedEffectFamilies;
    }

    @Data
    @Builder
    public static class TemplateUsage {
        private Long templateId;
        private String templateCode;
        private String templateNameZh;
        private Long usageCount;
        private List<TemplateUsageRef> flowStepRefs;
    }

    @Data
    @Builder
    public static class TemplateUsageRef {
        private Long flowId;
        private String flowCode;
        private String flowNameZh;
        private String flowType;
        private Long stepId;
        private String stepCode;
        private String stepNameZh;
        private String stepType;
        private String triggerType;
        private String status;
    }

    @Data
    @Builder
    public static class Flow {
        private Long id;
        private String code;
        private String flowType;
        private String flowTypeLabelZh;
        private String mode;
        private String modeLabelZh;
        private String nameZh;
        private String nameEn;
        private String nameZht;
        private String namePt;
        private String descriptionZh;
        private String descriptionEn;
        private String descriptionZht;
        private String descriptionPt;
        private String mapPolicyJson;
        private String advancedConfigJson;
        private String status;
        private Integer sortOrder;
        private LocalDateTime publishedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<Step> steps;
        private List<Binding> bindings;
        private List<OverrideRule> overrides;
    }

    @Data
    @Builder
    public static class Step {
        private Long id;
        private Long flowId;
        private String stepCode;
        private String stepType;
        private Long templateId;
        private Template template;
        private String stepNameZh;
        private String stepNameEn;
        private String stepNameZht;
        private String stepNamePt;
        private String descriptionZh;
        private String descriptionEn;
        private String descriptionZht;
        private String descriptionPt;
        private String triggerType;
        private String triggerTypeLabelZh;
        private String triggerConfigJson;
        private String conditionConfigJson;
        private String effectConfigJson;
        private Long mediaAssetId;
        private String rewardRuleIdsJson;
        private String explorationWeightLevel;
        private String explorationWeightLabelZh;
        private Integer explorationWeightValue;
        private Boolean requiredForCompletion;
        private String inheritKey;
        private String status;
        private Integer sortOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class Binding {
        private Long id;
        private String ownerType;
        private Long ownerId;
        private String ownerCode;
        private String bindingRole;
        private String bindingRoleLabelZh;
        private Long flowId;
        private String flowName;
        private Integer priority;
        private String inheritPolicy;
        private String status;
        private Integer sortOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class OverrideRule {
        private Long id;
        private String ownerType;
        private Long ownerId;
        private String targetOwnerType;
        private Long targetOwnerId;
        private String targetStepCode;
        private String overrideMode;
        private String overrideModeLabelZh;
        private Long replacementStepId;
        private String overrideConfigJson;
        private Boolean requiresTargetStepCode;
        private Boolean requiresReplacementStep;
        private String semanticsHint;
        private String status;
        private Integer sortOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class ExplorationElement {
        private Long id;
        private String elementCode;
        private String elementType;
        private String ownerType;
        private Long ownerId;
        private String ownerCode;
        private Long cityId;
        private Long subMapId;
        private Long storylineId;
        private Long storyChapterId;
        private String titleZh;
        private String titleEn;
        private String titleZht;
        private String titlePt;
        private String weightLevel;
        private String weightLabelZh;
        private Integer weightValue;
        private Boolean includeInExploration;
        private String metadataJson;
        private String weightGuidance;
        private String status;
        private Integer sortOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class GovernanceOverview {
        private long templateCount;
        private long flowCount;
        private long bindingCount;
        private long overrideCount;
        private long explorationElementCount;
        private long highRiskTemplateCount;
        private ContractVocabulary contractVocabulary;
        private List<OperatorHint> operatorHints;
        private List<GovernanceFinding> findings;
    }

    @Data
    @Builder
    public static class GovernanceItem {
        private String itemKey;
        private String sourceDomain;
        private String ownerType;
        private Long ownerId;
        private String ownerCode;
        private String ownerName;
        private Long cityId;
        private Long subMapId;
        private Long poiId;
        private Long indoorBuildingId;
        private Long storylineId;
        private Long storyChapterId;
        private Long templateId;
        private String templateCode;
        private String templateNameZh;
        private String templateType;
        private Long flowId;
        private String flowCode;
        private Long stepId;
        private String stepCode;
        private String triggerType;
        private String effectFamily;
        private String rewardType;
        private String status;
        private String riskLevel;
        private Boolean storyOverride;
        private Long conflictCount;
    }

    @Data
    @Builder
    public static class GovernanceDetail {
        private GovernanceItem item;
        private List<GovernanceUsageRef> usageRefs;
        private List<GovernanceFinding> conflicts;
        private String rawSummary;
    }

    @Data
    @Builder
    public static class GovernanceUsageRef {
        private String sourceDomain;
        private String relationType;
        private String ownerType;
        private Long ownerId;
        private String ownerName;
        private Long flowId;
        private Long stepId;
        private Long rewardRuleId;
        private Long indoorNodeId;
        private String description;
    }

    @Data
    @Builder
    public static class ContractVocabulary {
        private List<VocabularyOption> templateTypes;
        private List<VocabularyOption> flowTypes;
        private List<VocabularyOption> flowModes;
        private List<VocabularyOption> ownerTypes;
        private List<VocabularyOption> bindingRoles;
        private List<VocabularyOption> inheritPolicies;
        private List<VocabularyOption> overrideModes;
        private List<VocabularyOption> triggerTypes;
        private List<VocabularyOption> weightLevels;
        private List<VocabularyOption> statuses;
    }

    @Data
    @Builder
    public static class VocabularyOption {
        private String code;
        private String labelZh;
        private String guidance;
        private Integer numericValue;
    }

    @Data
    @Builder
    public static class OperatorHint {
        private String fieldName;
        private String title;
        private String description;
    }

    @Data
    @Builder
    public static class GovernanceFinding {
        private String severity;
        private String findingType;
        private String title;
        private String description;
        private String sourceDomain;
        private String ownerType;
        private Long ownerId;
        private Long flowId;
        private Long stepId;
        private Long templateId;
        private Long rewardRuleId;
        private String itemKey;
    }
}
