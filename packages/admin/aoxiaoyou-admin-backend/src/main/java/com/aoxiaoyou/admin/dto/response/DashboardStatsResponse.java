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
}
