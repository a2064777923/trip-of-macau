package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AdminStorylineModeResponse {

    @Data
    @Builder
    public static class Snapshot {
        private AdminStoryLineDetailResponse storyline;
        private List<AdminStoryChapterResponse> chapters;
        private RouteStrategy routeStrategy;
        private List<ChapterRuntime> chapterRuntimes;
        private List<String> availableAnchorTypes;
        private List<String> availableOverrideModes;
        private List<ValidationFinding> validationFindings;
        private String publicRuntimePath;
    }

    @Data
    @Builder
    public static class RuntimePreview {
        private Long storylineId;
        private String publicRuntimePath;
        private RouteStrategy storyModeConfig;
        private List<ChapterRuntime> chapters;
        private List<ValidationFinding> validationFindings;
    }

    @Data
    @Builder
    public static class RouteStrategy {
        private Integer schemaVersion;
        private Boolean hideUnrelatedContent;
        private Boolean nearbyRevealEnabled;
        private Integer nearbyRevealRadiusMeters;
        private Integer nearbyRevealMeters;
        private String currentRouteHighlight;
        private String currentRouteStyle;
        private String inactiveRouteStyle;
        private Boolean clearTemporaryProgressOnExit;
        private Boolean exitResetsSessionProgress;
        private Boolean preservePermanentEvents;
        private String branchSourceType;
        private String branchInsertPosition;
        private Boolean branchSkippable;
        private Boolean branchAffectsStoryProgress;
        private List<Long> manualBranchPoiIds;
        private Map<String, Object> extra;
    }

    @Data
    @Builder
    public static class ChapterRuntime {
        private AdminStoryChapterResponse chapter;
        private Anchor anchor;
        private FlowSummary inheritedFlow;
        private FlowSummary chapterFlow;
        private List<OverrideRule> overrides;
        private List<StepSummary> compiledStepPreview;
        private List<ValidationFinding> validationFindings;
    }

    @Data
    @Builder
    public static class Anchor {
        private String anchorType;
        private Long anchorTargetId;
        private String anchorTargetCode;
        private String anchorLabel;
        private Integer routeOrder;
        private String routeSegmentStyle;
    }

    @Data
    @Builder
    public static class FlowSummary {
        private Long id;
        private String code;
        private String flowType;
        private String mode;
        private String nameZh;
        private String nameZht;
        private String descriptionZh;
        private String descriptionZht;
        private String status;
        private Integer sortOrder;
        private LocalDateTime publishedAt;
        private List<StepSummary> steps;
    }

    @Data
    @Builder
    public static class StepSummary {
        private Long id;
        private Long flowId;
        private String stepCode;
        private String stepType;
        private String stepNameZh;
        private String stepNameZht;
        private String triggerType;
        private Long mediaAssetId;
        private String rewardRuleIdsJson;
        private String explorationWeightLevel;
        private Boolean requiredForCompletion;
        private String inheritKey;
        private String status;
        private Integer sortOrder;
        private String overrideMode;
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
        private StepSummary replacementStep;
        private String overrideConfigJson;
        private String status;
        private Integer sortOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    public static class ValidationFinding {
        private String severity;
        private String findingType;
        private String title;
        private String description;
        private Long chapterId;
        private Long stepId;
        private String stepCode;
    }
}
