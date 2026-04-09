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
    private List<StorylineProgress> activeStorylines;
    private List<RecentCheckIn> recentCheckIns;

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
}
