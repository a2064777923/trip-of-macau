package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.config.IntegrationProperties;
import com.aoxiaoyou.admin.dto.response.DashboardStatsResponse;
import com.aoxiaoyou.admin.entity.Activity;
import com.aoxiaoyou.admin.entity.SysOperationLog;
import com.aoxiaoyou.admin.mapper.ActivityMapper;
import com.aoxiaoyou.admin.mapper.AppRuntimeSettingMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.NotificationMapper;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.RewardMapper;
import com.aoxiaoyou.admin.mapper.StampMapper;
import com.aoxiaoyou.admin.mapper.StoryChapterMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SysOperationLogMapper;
import com.aoxiaoyou.admin.mapper.TestAccountMapper;
import com.aoxiaoyou.admin.mapper.TipArticleMapper;
import com.aoxiaoyou.admin.media.CosProperties;
import com.aoxiaoyou.admin.service.DashboardService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PoiMapper poiMapper;
    private final StoryLineMapper storyLineMapper;
    private final StoryChapterMapper storyChapterMapper;
    private final CityMapper cityMapper;
    private final ActivityMapper activityMapper;
    private final RewardMapper rewardMapper;
    private final StampMapper stampMapper;
    private final TipArticleMapper tipArticleMapper;
    private final NotificationMapper notificationMapper;
    private final AppRuntimeSettingMapper appRuntimeSettingMapper;
    private final TestAccountMapper testAccountMapper;
    private final SysOperationLogMapper sysOperationLogMapper;
    private final JdbcTemplate jdbcTemplate;
    private final IntegrationProperties integrationProperties;
    private final CosProperties cosProperties;
    private final ObjectMapper objectMapper;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse.ComponentStatus databaseStatus = checkDatabase();
        DashboardStatsResponse.ComponentStatus publicApiStatus = checkPublicApi();
        DashboardStatsResponse.ComponentStatus cosStatus = checkCos();
        DashboardStatsResponse.SeedStatus seedStatus = checkSeedMigration();

        long totalUsers = safeLongQuery("SELECT COUNT(*) FROM user_profiles WHERE deleted = 0");
        long totalStamps = safeLongQuery("SELECT COALESCE(SUM(total_stamps), 0) FROM user_profiles");
        long publishedPois = safeLongQuery("SELECT COUNT(*) FROM pois WHERE status = 'published' AND deleted = 0");
        long publishedStoryLines = safeLongQuery("SELECT COUNT(*) FROM storylines WHERE status = 'published' AND deleted = 0");
        long activities = safeCount(() -> activityMapper.selectCount(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, "published")));
        long rewards = safeLongQuery("SELECT COUNT(*) FROM rewards WHERE status = 'published' AND deleted = 0");
        long testAccounts = safeCount(() -> testAccountMapper.selectCount(null));
        long weeklyUsers = safeLongQuery("SELECT COUNT(*) FROM user_profiles WHERE deleted = 0 AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)");
        double weeklyGrowth = totalUsers == 0 ? 0D : Math.round((weeklyUsers * 10000D / totalUsers)) / 100D;

        DashboardStatsResponse.ContentSummary contentSummary = DashboardStatsResponse.ContentSummary.builder()
                .publishedCities(safeLongQuery("SELECT COUNT(*) FROM cities WHERE status = 'published' AND deleted = 0"))
                .publishedStoryLines(publishedStoryLines)
                .publishedStoryChapters(safeLongQuery("SELECT COUNT(*) FROM story_chapters WHERE status = 'published' AND deleted = 0"))
                .publishedPois(publishedPois)
                .publishedStamps(safeLongQuery("SELECT COUNT(*) FROM stamps WHERE status = 'published' AND deleted = 0"))
                .publishedRewards(rewards)
                .publishedTips(safeLongQuery("SELECT COUNT(*) FROM tip_articles WHERE status = 'published' AND deleted = 0"))
                .publishedNotifications(safeLongQuery("SELECT COUNT(*) FROM notifications WHERE status = 'published' AND deleted = 0"))
                .publishedRuntimeSettings(safeLongQuery("SELECT COUNT(*) FROM app_runtime_settings WHERE status = 'published' AND deleted = 0"))
                .build();

        List<DashboardStatsResponse.RecentActivity> recentActivities = sysOperationLogMapper.selectList(
                        new LambdaQueryWrapper<SysOperationLog>().orderByDesc(SysOperationLog::getCreatedAt).last("limit 8"))
                .stream()
                .map(log -> DashboardStatsResponse.RecentActivity.builder()
                        .id(log.getId())
                        .type(log.getModule())
                        .user(log.getAdminUsername() == null ? "system" : log.getAdminUsername())
                        .action(log.getOperation())
                        .time(log.getCreatedAt() == null ? "-" : log.getCreatedAt().format(DISPLAY_TIME))
                        .build())
                .toList();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalStamps(totalStamps)
                .poiCount(publishedPois)
                .weeklyGrowth(weeklyGrowth)
                .activeUsers(Math.max(weeklyUsers, Math.min(totalUsers, 128)))
                .storyLines(publishedStoryLines)
                .activities(activities)
                .rewards(rewards)
                .testAccounts(testAccounts)
                .recentActivities(recentActivities)
                .systemStatus(DashboardStatsResponse.SystemStatus.builder()
                        .database(databaseStatus.getHealthy())
                        .api(publicApiStatus.getHealthy())
                        .cloudRun(cosStatus.getHealthy())
                        .build())
                .contentSummary(contentSummary)
                .integrationHealth(DashboardStatsResponse.IntegrationHealth.builder()
                        .database(databaseStatus)
                        .publicApi(publicApiStatus)
                        .cos(cosStatus)
                        .seedMigration(seedStatus)
                        .build())
                .build();
    }

    private long safeCount(Supplier<Long> supplier) {
        try {
            Long value = supplier.get();
            return value == null ? 0L : value;
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private long safeLongQuery(String sql) {
        try {
            Number value = jdbcTemplate.queryForObject(sql, Number.class);
            return value == null ? 0L : value.longValue();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private DashboardStatsResponse.ComponentStatus checkDatabase() {
        try {
            Number probe = jdbcTemplate.queryForObject("SELECT 1", Number.class);
            boolean healthy = probe != null && probe.intValue() == 1;
            return componentStatus(healthy, healthy ? "UP" : "DOWN", healthy ? "MySQL 查詢探針通過。" : "MySQL 查詢探針失敗。", null);
        } catch (Exception ex) {
            return componentStatus(false, "DOWN", "MySQL 探針失敗：" + ex.getMessage(), null);
        }
    }

    private DashboardStatsResponse.ComponentStatus checkPublicApi() {
        String url = integrationProperties.resolvePublicHealthUrl();
        long startedAt = System.currentTimeMillis();
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(integrationProperties.getTimeoutMs()))
                    .build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofMillis(integrationProperties.getTimeoutMs()))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - startedAt;

            JsonNode root = objectMapper.readTree(response.body());
            int apiCode = root.path("code").asInt(5000);
            JsonNode data = root.path("data");
            String status = data.path("status").asText(response.statusCode() >= 200 && response.statusCode() < 300 ? "UP" : "DOWN");
            boolean healthy = response.statusCode() >= 200
                    && response.statusCode() < 300
                    && (apiCode == 0 || apiCode == 200)
                    && "UP".equalsIgnoreCase(status);

            String detail = "公開 API 健康檢查已回應：" + url + "（apiCode=" + apiCode + "）";
            if (data.has("discoverCuratedCardsConfigured")) {
                detail += "，發現頁精選卡片配置：" + data.path("discoverCuratedCardsConfigured").asBoolean(false);
            }
            return componentStatus(healthy, healthy ? "UP" : "DOWN", detail, latency);
        } catch (Exception ex) {
            return componentStatus(false, "DOWN", "公開 API 探針失敗：" + ex.getMessage(), System.currentTimeMillis() - startedAt);
        }
    }

    private DashboardStatsResponse.ComponentStatus checkCos() {
        boolean enabled = cosProperties.isEnabled();
        boolean configured = enabled
                && StringUtils.hasText(cosProperties.getBucketName())
                && StringUtils.hasText(cosProperties.getRegion())
                && StringUtils.hasText(cosProperties.resolvePublicBaseUrl());
        String detail;
        String status;
        if (configured) {
            status = "UP";
            detail = "COS bucket " + cosProperties.getBucketName() + " 已配置，區域：" + cosProperties.getRegion() + "。";
        } else if (enabled) {
            status = "WARN";
            detail = "COS 已啟用，但當前運行配置不完整。";
        } else {
            status = "DOWN";
            detail = "當前後台運行環境未啟用 COS 上傳。";
        }
        return componentStatus(configured, status, detail, null);
    }

    private DashboardStatsResponse.SeedStatus checkSeedMigration() {
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT seed_key, status, executed_at, notes FROM seed_runs WHERE seed_key = ?",
                    integrationProperties.getPhase6SeedKey());
            Object executedAt = row.get("executed_at");
            return DashboardStatsResponse.SeedStatus.builder()
                    .seedKey(String.valueOf(row.get("seed_key")))
                    .status(String.valueOf(row.get("status")))
                    .executedAt(executedAt == null ? null : String.valueOf(executedAt))
                    .notes(row.get("notes") == null ? null : String.valueOf(row.get("notes")))
                    .build();
        } catch (EmptyResultDataAccessException ignored) {
            return DashboardStatsResponse.SeedStatus.builder()
                    .seedKey(integrationProperties.getPhase6SeedKey())
                    .status("missing")
                    .executedAt(null)
                    .notes("未找到 Phase 6 種子資料遷移的 seed_runs 記錄。")
                    .build();
        } catch (Exception ex) {
            return DashboardStatsResponse.SeedStatus.builder()
                    .seedKey(integrationProperties.getPhase6SeedKey())
                    .status("error")
                    .executedAt(null)
                    .notes("讀取 seed_runs 失敗：" + ex.getMessage())
                    .build();
        }
    }

    private DashboardStatsResponse.ComponentStatus componentStatus(boolean healthy, String status, String detail, Long latencyMs) {
        return DashboardStatsResponse.ComponentStatus.builder()
                .healthy(healthy)
                .status(status)
                .detail(detail)
                .latencyMs(latencyMs)
                .checkedAt(LocalDateTime.now().format(DISPLAY_TIME))
                .build();
    }
}
