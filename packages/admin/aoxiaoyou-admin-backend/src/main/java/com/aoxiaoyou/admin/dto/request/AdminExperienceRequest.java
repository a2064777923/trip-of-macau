package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

public class AdminExperienceRequest {

    private static final String TEMPLATE_TYPE_PATTERN = "presentation|effect|trigger_effect|gameplay|display_condition|trigger_condition|task_gameplay|reward_presentation";
    private static final String FLOW_TYPE_PATTERN = "default_poi|default_indoor_building|default_indoor_floor|default_indoor_node|default_task|default_marker|default_overlay|default_activity|story_chapter_override|manual_target";
    private static final String FLOW_MODE_PATTERN = "walk_in|story_mode|manual";
    private static final String OWNER_TYPE_PATTERN = "poi|indoor_building|indoor_floor|indoor_node|story_chapter|task|marker|overlay|activity|manual_target";
    private static final String BINDING_ROLE_PATTERN = "default_experience_flow|story_override_flow";
    private static final String INHERIT_POLICY_PATTERN = "inherit|override";
    private static final String OVERRIDE_MODE_PATTERN = "inherit|disable|replace|append";
    private static final String TRIGGER_TYPE_PATTERN = "manual|tap|tap_action|proximity|media_finished|dwell|story_mode_enter|tap_sequence|mixed|compound|content_complete|task_complete|pickup_complete";
    private static final String WEIGHT_LEVEL_PATTERN = "tiny|small|medium|large|core";
    private static final String STATUS_PATTERN = "draft|published|archived";
    private static final String RISK_LEVEL_PATTERN = "low|normal|high|critical";

    @Data
    public static class TemplateClone {
        @NotBlank(message = "code is required")
        private String code;
        @NotBlank(message = "nameZh is required")
        private String nameZh;
        private String nameZht;
        private String summaryZh;
        private String summaryZht;
        @Pattern(regexp = STATUS_PATTERN, message = "status must use the canonical publish vocabulary")
        private String status;
    }

    @Data
    public static class TemplateUpsert {
        private String code;
        @NotBlank(message = "templateType is required")
        @Pattern(regexp = TEMPLATE_TYPE_PATTERN, message = "templateType must use the canonical experience template vocabulary")
        private String templateType;
        private String category;
        @NotBlank(message = "nameZh is required")
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
        @Pattern(regexp = RISK_LEVEL_PATTERN, message = "riskLevel must use the canonical risk vocabulary")
        private String riskLevel;
        @Pattern(regexp = STATUS_PATTERN, message = "status must use the canonical publish vocabulary")
        private String status;
        private Integer sortOrder;
    }

    @Data
    public static class FlowUpsert {
        private String code;
        @Pattern(regexp = FLOW_TYPE_PATTERN, message = "flowType must use the canonical flow vocabulary")
        private String flowType;
        @Pattern(regexp = FLOW_MODE_PATTERN, message = "mode must use the canonical flow mode vocabulary")
        private String mode;
        @NotBlank(message = "nameZh is required")
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
        @Pattern(regexp = STATUS_PATTERN, message = "status must use the canonical publish vocabulary")
        private String status;
        private Integer sortOrder;
        private String publishedAt;
    }

    @Data
    public static class StepUpsert {
        private String stepCode;
        @NotBlank(message = "stepType is required")
        private String stepType;
        private Long templateId;
        @NotBlank(message = "stepNameZh is required")
        private String stepNameZh;
        private String stepNameEn;
        private String stepNameZht;
        private String stepNamePt;
        private String descriptionZh;
        private String descriptionEn;
        private String descriptionZht;
        private String descriptionPt;
        @Pattern(regexp = TRIGGER_TYPE_PATTERN, message = "triggerType must use the canonical trigger vocabulary")
        private String triggerType;
        private String triggerConfigJson;
        private String conditionConfigJson;
        private String effectConfigJson;
        private Long mediaAssetId;
        private String rewardRuleIdsJson;
        @Pattern(regexp = WEIGHT_LEVEL_PATTERN, message = "explorationWeightLevel must use the canonical semantic weight vocabulary")
        private String explorationWeightLevel;
        private Boolean requiredForCompletion;
        private String inheritKey;
        @Pattern(regexp = STATUS_PATTERN, message = "status must use the canonical publish vocabulary")
        private String status;
        private Integer sortOrder;
    }

    @Data
    public static class BindingUpsert {
        @NotBlank(message = "ownerType is required")
        @Pattern(regexp = OWNER_TYPE_PATTERN, message = "ownerType must use the canonical experience owner vocabulary")
        private String ownerType;
        private Long ownerId;
        private String ownerCode;
        @Pattern(regexp = BINDING_ROLE_PATTERN, message = "bindingRole must use the canonical binding vocabulary")
        private String bindingRole;
        @NotNull(message = "flowId is required")
        private Long flowId;
        private Integer priority;
        @Pattern(regexp = INHERIT_POLICY_PATTERN, message = "inheritPolicy must use the canonical binding inheritance vocabulary")
        private String inheritPolicy;
        @Pattern(regexp = STATUS_PATTERN, message = "status must use the canonical publish vocabulary")
        private String status;
        private Integer sortOrder;
    }

    @Data
    public static class OverrideUpsert {
        @NotBlank(message = "ownerType is required")
        @Pattern(regexp = OWNER_TYPE_PATTERN, message = "ownerType must use the canonical experience owner vocabulary")
        private String ownerType;
        @NotNull(message = "ownerId is required")
        private Long ownerId;
        @Pattern(regexp = OWNER_TYPE_PATTERN, message = "targetOwnerType must use the canonical experience owner vocabulary")
        private String targetOwnerType;
        private Long targetOwnerId;
        private String targetStepCode;
        @Pattern(regexp = OVERRIDE_MODE_PATTERN, message = "overrideMode must use the canonical override vocabulary")
        private String overrideMode;
        private Long replacementStepId;
        private String overrideConfigJson;
        @Pattern(regexp = STATUS_PATTERN, message = "status must use the canonical publish vocabulary")
        private String status;
        private Integer sortOrder;
    }

    @Data
    public static class ExplorationElementUpsert {
        private String elementCode;
        @NotBlank(message = "elementType is required")
        private String elementType;
        @NotBlank(message = "ownerType is required")
        @Pattern(regexp = OWNER_TYPE_PATTERN, message = "ownerType must use the canonical experience owner vocabulary")
        private String ownerType;
        private Long ownerId;
        private String ownerCode;
        private Long cityId;
        private Long subMapId;
        private Long storylineId;
        private Long storyChapterId;
        @NotBlank(message = "titleZh is required")
        private String titleZh;
        private String titleEn;
        private String titleZht;
        private String titlePt;
        @Pattern(regexp = WEIGHT_LEVEL_PATTERN, message = "weightLevel must use the canonical semantic weight vocabulary")
        private String weightLevel;
        private Integer weightValue;
        private Boolean includeInExploration;
        private String metadataJson;
        @Pattern(regexp = STATUS_PATTERN, message = "status must use the canonical publish vocabulary")
        private String status;
        private Integer sortOrder;
    }

    @Data
    public static class GovernanceQuery {
        private Long pageNum;
        private Long pageSize;
        private String keyword;
        private Long cityId;
        private Long subMapId;
        private Long poiId;
        private Long indoorBuildingId;
        private Long storylineId;
        private Long storyChapterId;
        private String ownerType;
        private String templateType;
        private String triggerType;
        private String effectFamily;
        private String rewardType;
        private String status;
        private Boolean storyOverrideOnly;
        private Boolean highRiskOnly;
        private Boolean conflictOnly;
    }
}
