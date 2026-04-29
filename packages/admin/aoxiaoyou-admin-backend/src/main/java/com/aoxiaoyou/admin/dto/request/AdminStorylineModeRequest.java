package com.aoxiaoyou.admin.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

public class AdminStorylineModeRequest {

    private static final String ANCHOR_TYPE_PATTERN = "poi|indoor_building|indoor_floor|indoor_node|task|overlay|manual";
    private static final String OVERRIDE_MODE_PATTERN = "inherit|disable|replace|append";
    private static final String STATUS_PATTERN = "draft|published|archived";
    private static final String WEIGHT_LEVEL_PATTERN = "tiny|small|medium|large|core";

    @Data
    public static class StoryModeConfigUpsert {
        private Boolean hideUnrelatedContent;
        private Boolean nearbyRevealEnabled;
        private Integer nearbyRevealRadiusMeters;
        private String currentRouteHighlight;
        private String inactiveRouteStyle;
        private Boolean clearTemporaryProgressOnExit;
        private Boolean preservePermanentEvents;
        private String branchSourceType;
        private String branchInsertPosition;
        private Boolean branchSkippable;
        private Boolean branchAffectsStoryProgress;
        private List<Long> manualBranchPoiIds;
        private Boolean advancedJsonEnabled;
        private String advancedStoryModeConfigJson;
    }

    @Data
    public static class ChapterAnchorUpsert {
        @Pattern(regexp = ANCHOR_TYPE_PATTERN, message = "anchorType must use Phase 30 canonical values")
        private String anchorType;
        private Long anchorTargetId;
        private String anchorTargetCode;
        private String anchorLabelOverride;
        private Integer routeOrder;
        private String routeSegmentStyle;
    }

    @Data
    public static class ChapterOverridePolicyUpsert {
        private Boolean inheritDefaultFlow;
        private Boolean disableDefaultArrivalMedia;
        private Boolean appendStorySpecificRewards;
        private Boolean advancedJsonEnabled;
        private String advancedOverridePolicyJson;
    }

    @Data
    public static class OverrideStepUpsert {
        private String targetStepCode;
        @Pattern(regexp = OVERRIDE_MODE_PATTERN, message = "overrideMode must be inherit, disable, replace or append")
        private String overrideMode;
        private Long replacementStepId;
        private ReplacementStepDraft replacementStepDraft;
        private String effectPreset;
        private Long mediaAssetId;
        private List<Long> rewardRuleIds;
        private List<String> pickupCodes;
        private String challengeCode;
        @Pattern(regexp = WEIGHT_LEVEL_PATTERN, message = "explorationWeightLevel must use semantic weights")
        private String explorationWeightLevel;
        private Integer sortOrder;
        @Pattern(regexp = STATUS_PATTERN, message = "status must use draft, published or archived")
        private String status;
        private Boolean advancedJsonEnabled;
        private String advancedOverrideConfigJson;
    }

    @Data
    public static class ReplacementStepDraft {
        private Long stepId;
        private String stepCode;
        private String stepType;
        private String stepNameZh;
        private String stepNameZht;
        private String stepNameEn;
        private String stepNamePt;
        private String descriptionZh;
        private String descriptionZht;
        private String descriptionEn;
        private String descriptionPt;
        private String triggerType;
        private String triggerConfigJson;
        private String conditionConfigJson;
        private String effectConfigJson;
        private Long mediaAssetId;
        private List<Long> rewardRuleIds;
        private String explorationWeightLevel;
        private Boolean requiredForCompletion;
        private String inheritKey;
        @Pattern(regexp = STATUS_PATTERN, message = "status must use draft, published or archived")
        private String status;
        private Integer sortOrder;
    }
}
