package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminUserDetailResponse {

    private AdminUserListItemResponse basicInfo;
    private Progress progress;
    private ProgressSnapshot cityProgress;
    private ProgressSnapshot subMapProgress;
    private ProgressSnapshot collectibleProgress;
    private ProgressSnapshot badgeProgress;
    private ProgressSnapshot rewardProgress;
    private List<StorylineProgress> activeStorylines;
    private List<RecentCheckIn> recentCheckIns;
    private List<RecentTriggerLog> recentTriggerLogs;

    @Data
    @Builder
    public static class Progress {
        private Integer level;
        private Integer currentExp;
        private Integer nextLevelExp;
        private Integer totalStamps;
        private Integer totalBadges;
        private Integer unlockedStorylines;
        private Integer completedStorylines;
    }

    @Data
    @Builder
    public static class StorylineProgress {
        private Long storylineId;
        private String name;
        private Long currentPoiId;
        private String currentPoiName;
        private Integer completedPoiCount;
        private Integer totalPoiCount;
        private Integer progressPercent;
        private LocalDateTime startedAt;
    }

    @Data
    @Builder
    public static class RecentCheckIn {
        private Long checkInId;
        private String poiName;
        private String checkInType;
        private Boolean rewardGranted;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class ProgressSnapshot {
        private Integer completedCount;
        private Integer totalCount;
        private Integer progressPercent;
        private String summary;
    }

    @Data
    @Builder
    public static class RecentTriggerLog {
        private Long triggerLogId;
        private String poiName;
        private String triggerType;
        private String distanceMeters;
        private String gpsAccuracyMeters;
        private Boolean wifiUsed;
        private LocalDateTime createdAt;
    }
}
