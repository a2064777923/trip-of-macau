package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public class AdminPoiExperienceRequest {

    private static final String FLOW_STATUS_PATTERN = "draft|published|archived";
    private static final String STEP_TYPE_PATTERN = "intro_modal|route_guidance|proximity_media|checkin_task|pickup|hidden_challenge|reward_grant|custom";
    private static final String TRIGGER_TYPE_PATTERN = "manual|tap|tap_action|proximity|media_finished|dwell|story_mode_enter|tap_sequence|mixed|compound|content_complete|task_complete|pickup_complete";
    private static final String WEIGHT_LEVEL_PATTERN = "tiny|small|medium|large|core";
    private static final String TEMPLATE_TYPE_PATTERN = "presentation|effect|trigger_effect|gameplay|display_condition|trigger_condition|task_gameplay|reward_presentation";
    private static final String RISK_LEVEL_PATTERN = "low|normal|high|critical";

    @Data
    public static class FlowUpsert {
        private String code;
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
        @Pattern(regexp = FLOW_STATUS_PATTERN, message = "status must use the canonical publish vocabulary")
        private String status;
        private Integer sortOrder;
        private String publishedAt;
    }

    @Data
    public static class StepStructuredUpsert {
        private String stepCode;
        @Pattern(regexp = STEP_TYPE_PATTERN, message = "stepType must use the POI experience vocabulary")
        private String stepType;
        private Long templateId;
        private String stepNameZh;
        private String stepNameZht;
        private String stepNameEn;
        private String stepNamePt;
        private String descriptionZh;
        private String descriptionZht;
        private String descriptionEn;
        private String descriptionPt;
        @Pattern(regexp = TRIGGER_TYPE_PATTERN, message = "triggerType must use the canonical trigger vocabulary")
        private String triggerType;
        private Long mediaAssetId;
        @Pattern(regexp = WEIGHT_LEVEL_PATTERN, message = "explorationWeightLevel must use the canonical semantic weight vocabulary")
        private String explorationWeightLevel;
        private Boolean requiredForCompletion;
        @Pattern(regexp = FLOW_STATUS_PATTERN, message = "status must use the canonical publish vocabulary")
        private String status;
        private Integer sortOrder;

        private String triggerPreset;
        private BigDecimal triggerRadiusMeters;
        private Integer dwellSeconds;
        private String tapActionCode;
        private String afterStepCode;

        private String conditionPreset;
        private Boolean oncePerUser;
        private String timeWindowStart;
        private String timeWindowEnd;
        private List<String> requiredItemCodes;
        private List<String> requiredBadgeCodes;

        private String effectPreset;
        private String modalTitle;
        private String modalBody;
        private String primaryActionLabel;
        private List<String> routeCardTypes;
        private List<String> taskCodes;
        private List<String> pickupCodes;
        private List<Long> rewardRuleIds;
        private String rewardSummary;
        private Long fullScreenMediaAssetId;
        private Long audioAssetId;

        private Boolean advancedJsonEnabled;
        private String advancedTriggerConfigJson;
        private String advancedConditionConfigJson;
        private String advancedEffectConfigJson;
    }

    @Data
    public static class SaveTemplateRequest {
        private String code;
        @Pattern(regexp = TEMPLATE_TYPE_PATTERN, message = "templateType must use the canonical experience template vocabulary")
        private String templateType;
        private String category;
        private String nameZh;
        private String nameZht;
        private String nameEn;
        private String namePt;
        private String summaryZh;
        private String summaryZht;
        private String summaryEn;
        private String summaryPt;
        @Pattern(regexp = RISK_LEVEL_PATTERN, message = "riskLevel must use the canonical risk vocabulary")
        private String riskLevel;
        @Pattern(regexp = FLOW_STATUS_PATTERN, message = "status must use the canonical publish vocabulary")
        private String status;
        private Integer sortOrder;
    }
}
