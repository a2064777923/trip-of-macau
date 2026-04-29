package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminTravelerProgressWorkbenchResponse {
    private Long userId;
    private IdentitySection identity;
    private PreferenceSection preferences;
    private List<LinkedScopeSummary> linkedScopes;
    private DynamicProgressSection dynamicProgress;
    private List<LegacyProgressSnapshot> legacyProgressSnapshot;
    private List<StorylineSessionSummary> storylineSessions;
    private List<RewardRedemptionSummary> rewardRedemptions;
    private ExplorationContext explorationContext;

    @Data
    @Builder
    public static class IdentitySection {
        private Long userId;
        private String openId;
        private String nickname;
        private String avatarUrl;
        private Integer level;
        private Integer totalStamps;
        private Integer currentExp;
        private Integer nextLevelExp;
        private String currentLocaleCode;
        private Boolean testAccount;
        private Long currentCityId;
        private String currentCityName;
    }

    @Data
    @Builder
    public static class PreferenceSection {
        private String interfaceMode;
        private BigDecimal fontScale;
        private Boolean highContrast;
        private Boolean voiceGuideEnabled;
        private Boolean seniorMode;
        private String localeCode;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private String runtimeOverridesJson;
    }

    @Data
    @Builder
    public static class LinkedScopeSummary {
        private String scopeType;
        private Long scopeId;
        private String scopeName;
        private String relationLabel;
        private String source;
    }

    @Data
    @Builder
    public static class DynamicProgressSection {
        private AdminUserProgressSummaryResponse globalSummary;
        private List<ScopedProgressSummary> scopedSummaries;
        private String breakdownEndpoint;
        private String comparisonHint;
    }

    @Data
    @Builder
    public static class ScopedProgressSummary {
        private String scopeType;
        private Long scopeId;
        private String scopeName;
        private AdminUserProgressSummaryResponse summary;
    }

    @Data
    @Builder
    public static class LegacyProgressSnapshot {
        private String legacyScopeType;
        private Long legacyScopeId;
        private String legacyScopeName;
        private Integer legacyPercentValue;
        private Long activeStorylineId;
        private Boolean completedStoryline;
        private LocalDateTime lastSeenAt;
        private LocalDateTime updatedAt;
        private String sourceTable;
        private boolean compatibilityOnly;
        private String label;
    }

    @Data
    @Builder
    public static class StorylineSessionSummary {
        private String sessionId;
        private Long storylineId;
        private String storylineName;
        private Long currentChapterId;
        private String status;
        private LocalDateTime startedAt;
        private LocalDateTime lastEventAt;
        private LocalDateTime exitedAt;
        private Integer eventCount;
        private Boolean exitClearedTemporaryState;
        private String temporaryStepStateJson;
    }

    @Data
    @Builder
    public static class RewardRedemptionSummary {
        private Long redemptionId;
        private Long rewardId;
        private String rewardName;
        private String redemptionStatus;
        private Integer stampCostSnapshot;
        private LocalDateTime redeemedAt;
        private LocalDateTime expiresAt;
    }

    @Data
    @Builder
    public static class ExplorationContext {
        private Integer recentCheckinCount;
        private Integer recentExplorationEventCount;
        private Integer recentTriggerCount;
        private RouteTraceStatus routeTrace;
    }

    @Data
    @Builder
    public static class RouteTraceStatus {
        private String sourceStatus;
        private String message;
    }
}
