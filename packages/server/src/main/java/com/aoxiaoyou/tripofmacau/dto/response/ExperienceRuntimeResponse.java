package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

public class ExperienceRuntimeResponse {

    @Data
    @Builder
    public static class Template {
        private Long id;
        private String code;
        private String templateType;
        private String category;
        private String name;
        private String summary;
        private Map<String, Object> config;
        private String riskLevel;
    }

    @Data
    @Builder
    public static class StoryModeConfig {
        private Integer schemaVersion;
        private Boolean hideUnrelatedContent;
        private Integer nearbyRevealMeters;
        private String currentRouteStyle;
        private String inactiveRouteStyle;
        private Boolean exitResetsSessionProgress;
        private Map<String, Object> extra;
    }

    @Data
    @Builder
    public static class Step {
        private Long id;
        private Long flowId;
        private String stepCode;
        private String stepType;
        private String name;
        private String description;
        private String triggerType;
        private Map<String, Object> triggerConfig;
        private Map<String, Object> conditionConfig;
        private Map<String, Object> effectConfig;
        private Long mediaAssetId;
        private StoryMediaAssetResponse mediaAsset;
        private Object rewardRuleIds;
        private String explorationWeightLevel;
        private Integer explorationWeightValue;
        private Boolean requiredForCompletion;
        private String inheritKey;
        private Template template;
        private Integer sortOrder;
    }

    @Data
    @Builder
    public static class Flow {
        private Long id;
        private String code;
        private String flowType;
        private String mode;
        private String name;
        private String description;
        private Map<String, Object> mapPolicy;
        private Map<String, Object> advancedConfig;
        private List<Step> steps;
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
        private Long replacementStepId;
        private Map<String, Object> overrideConfig;
    }

    @Data
    @Builder
    public static class StorylineRuntime {
        private StoryLineResponse storyline;
        private StoryModeConfig storyModeConfig;
        private List<StoryChapterRuntime> chapters;
    }

    @Data
    @Builder
    public static class StoryChapterRuntime {
        private Long chapterId;
        private Integer chapterOrder;
        private String anchorType;
        private Long anchorTargetId;
        private String anchorTargetCode;
        private Map<String, Object> overridePolicy;
        private StoryModeConfig storyModeConfig;
        private StoryChapterResponse chapter;
        private Flow inheritedFlow;
        private Flow chapterFlow;
        private List<OverrideRule> overrides;
        private List<Step> compiledSteps;
    }
}
