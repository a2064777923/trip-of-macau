package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardStatsResponse {

    private Long totalUsers;
    private Long totalStamps;
    private Long poiCount;
    private Double weeklyGrowth;
    private Long activeUsers;
    private Long storyLines;
    private Long activities;
    private Long rewards;
    private Long testAccounts;
    private List<RecentActivity> recentActivities;
    private SystemStatus systemStatus;
    private ContentSummary contentSummary;
    private IntegrationHealth integrationHealth;

    @Data
    @Builder
    public static class RecentActivity {
        private Long id;
        private String type;
        private String user;
        private String action;
        private String time;
    }

    @Data
    @Builder
    public static class SystemStatus {
        private Boolean database;
        private Boolean api;
        private Boolean cloudRun;
    }

    @Data
    @Builder
    public static class ContentSummary {
        private Long publishedCities;
        private Long publishedStoryLines;
        private Long publishedStoryChapters;
        private Long publishedPois;
        private Long publishedStamps;
        private Long publishedRewards;
        private Long publishedTips;
        private Long publishedNotifications;
        private Long publishedRuntimeSettings;
    }

    @Data
    @Builder
    public static class IntegrationHealth {
        private ComponentStatus database;
        private ComponentStatus publicApi;
        private ComponentStatus cos;
        private SeedStatus seedMigration;
    }

    @Data
    @Builder
    public static class ComponentStatus {
        private Boolean healthy;
        private String status;
        private String detail;
        private Long latencyMs;
        private String checkedAt;
    }

    @Data
    @Builder
    public static class SeedStatus {
        private String seedKey;
        private String status;
        private String executedAt;
        private String notes;
    }
}
