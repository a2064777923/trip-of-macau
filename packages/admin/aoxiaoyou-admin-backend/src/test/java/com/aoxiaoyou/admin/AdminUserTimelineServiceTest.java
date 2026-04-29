package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerProgressWorkbenchResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerTimelineEntryResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressSummaryResponse;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.TravelerProfile;
import com.aoxiaoyou.admin.mapper.AdminTravelerProgressReadMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.TestAccountMapper;
import com.aoxiaoyou.admin.mapper.TravelerProfileMapper;
import com.aoxiaoyou.admin.service.AdminTravelerProgressService;
import com.aoxiaoyou.admin.service.AdminUserProgressCalculatorService;
import com.aoxiaoyou.admin.service.impl.AdminTravelerProgressServiceImpl;
import com.aoxiaoyou.admin.service.support.EmptyRouteTraceSourceAdapter;
import com.aoxiaoyou.admin.service.support.RouteTraceSourceAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserTimelineServiceTest {

    @Mock
    private TravelerProfileMapper travelerProfileMapper;

    @Mock
    private TestAccountMapper testAccountMapper;

    @Mock
    private CityMapper cityMapper;

    @Mock
    private AdminTravelerProgressReadMapper readMapper;

    @Mock
    private AdminUserProgressCalculatorService calculatorService;

    @Mock
    private RouteTraceSourceAdapter routeTraceSourceAdapter;

    private AdminTravelerProgressServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminTravelerProgressServiceImpl(
                travelerProfileMapper,
                testAccountMapper,
                cityMapper,
                readMapper,
                calculatorService,
                routeTraceSourceAdapter
        );
    }

    @Test
    void loadsWorkbenchIdentityPreferencesLinkedScopesAndSessions() {
        stubWorkbenchBase();
        when(routeTraceSourceAdapter.loadRouteTrace(88L, null, null)).thenReturn(
                new RouteTraceSourceAdapter.RouteTraceSnapshot("unavailable", "No verified route trace store", List.of())
        );

        AdminTravelerProgressWorkbenchResponse response = service.getProgressWorkbench(88L);

        assertThat(response.getIdentity().getNickname()).isEqualTo("Traveler");
        assertThat(response.getIdentity().getCurrentCityName()).isEqualTo("Macau");
        assertThat(response.getPreferences().getInterfaceMode()).isEqualTo("story");
        assertThat(response.getPreferences().getLocaleCode()).isEqualTo("zh-Hant");
        assertThat(response.getLinkedScopes()).extracting(AdminTravelerProgressWorkbenchResponse.LinkedScopeSummary::getScopeType)
                .containsExactly("city", "sub_map", "storyline");
        assertThat(response.getDynamicProgress().getGlobalSummary().getProgressPercent()).isEqualTo(61.54d);
        assertThat(response.getDynamicProgress().getScopedSummaries()).hasSize(3);
        assertThat(response.getStorylineSessions()).hasSize(1);
        assertThat(response.getRewardRedemptions()).hasSize(1);
    }

    @Test
    void mergesCheckinsTriggersExplorationEventsSessionsAndRewardRedemptions() {
        when(travelerProfileMapper.selectById(88L)).thenReturn(profile());
        when(readMapper.selectCheckinTimelineRows(88L)).thenReturn(List.of(
                timelineRow("checkin:1", "checkin", "user_checkins", 1L, 501L, "Fire Route", 101L, "A-Ma Temple",
                        "Check-in", "gps", "gps", null, LocalDateTime.of(2026, 4, 29, 12, 0))
        ));
        when(readMapper.selectTriggerTimelineRows(88L)).thenReturn(List.of(
                timelineRow("trigger:2", "trigger_log", "trigger_logs", 2L, 501L, "Fire Route", 101L, "A-Ma Temple",
                        "Trigger", "gps_accuracy=15", "gps_accuracy=15", null, LocalDateTime.of(2026, 4, 29, 11, 55))
        ));
        when(readMapper.selectExplorationTimelineRows(88L)).thenReturn(List.of(
                timelineRow("event:3", "exploration_event", "user_exploration_events", 3L, 501L, "Fire Route", 101L, "A-Ma Temple",
                        "Exploration Event", "element=ama_poi_arrival", "{\"element\":\"ama_poi_arrival\"}",
                        "{\"element\":\"ama_poi_arrival\",\"raw\":true}", LocalDateTime.of(2026, 4, 29, 11, 50))
        ));
        when(readMapper.selectStorySessionTimelineRows(88L)).thenReturn(List.of(
                timelineRow("session:4", "storyline_session", "user_storyline_sessions", 4L, 501L, "Fire Route", null, null,
                        "Story Session", "started", "started", "{\"temporary\":true}", LocalDateTime.of(2026, 4, 29, 11, 45))
        ));
        when(readMapper.selectRewardTimelineRows(88L)).thenReturn(List.of(
                timelineRow("reward:5", "reward_redemption", "reward_redemptions", 5L, null, null, null, null,
                        "Reward Redemption", "redeemed", "redeemed", "{\"rewardId\":9}", LocalDateTime.of(2026, 4, 29, 11, 40))
        ));
        when(readMapper.selectRepairAuditTimelineRows(88L)).thenReturn(List.of());

        PageResponse<AdminTravelerTimelineEntryResponse> page = service.getTimeline(
                88L,
                new AdminTravelerProgressService.TimelineQuery(1, 10, null, null, null, null)
        );

        assertThat(page.getTotal()).isEqualTo(5);
        assertThat(page.getList()).extracting(AdminTravelerTimelineEntryResponse::getEntryType)
                .containsExactly("checkin", "trigger_log", "exploration_event", "storyline_session", "reward_redemption");
        assertThat(page.getList().get(2).getPayloadPreview()).isEqualTo("{\"element\":\"ama_poi_arrival\"}");
        assertThat(page.getList().get(2).getRawPayload()).isEqualTo("{\"element\":\"ama_poi_arrival\",\"raw\":true}");
    }

    @Test
    void paginatesAndFiltersTimelineEntries() {
        when(travelerProfileMapper.selectById(88L)).thenReturn(profile());
        when(readMapper.selectCheckinTimelineRows(88L)).thenReturn(List.of(
                timelineRow("checkin:1", "checkin", "user_checkins", 1L, 700L, "Route A", 101L, "A-Ma Temple",
                        "Check-in", "gps", "gps", null, LocalDateTime.of(2026, 4, 29, 12, 0))
        ));
        when(readMapper.selectTriggerTimelineRows(88L)).thenReturn(List.of());
        when(readMapper.selectExplorationTimelineRows(88L)).thenReturn(List.of(
                timelineRow("event:2", "exploration_event", "user_exploration_events", 2L, 501L, "Fire Route", 101L, "A-Ma Temple",
                        "Exploration Event", "preview-a", "preview-a", "raw-a", LocalDateTime.of(2026, 4, 29, 11, 59)),
                timelineRow("event:3", "exploration_event", "user_exploration_events", 3L, 501L, "Fire Route", 101L, "A-Ma Temple",
                        "Exploration Event", "preview-b", "preview-b", "raw-b", LocalDateTime.of(2026, 4, 29, 11, 30))
        ));
        when(readMapper.selectStorySessionTimelineRows(88L)).thenReturn(List.of());
        when(readMapper.selectRewardTimelineRows(88L)).thenReturn(List.of(
                timelineRow("reward:4", "reward_redemption", "reward_redemptions", 4L, 501L, "Fire Route", null, null,
                        "Reward Redemption", "redeemed", "redeemed", "{\"reward\":4}", LocalDateTime.of(2026, 4, 29, 11, 45))
        ));
        when(readMapper.selectRepairAuditTimelineRows(88L)).thenReturn(List.of());

        PageResponse<AdminTravelerTimelineEntryResponse> page = service.getTimeline(
                88L,
                new AdminTravelerProgressService.TimelineQuery(
                        1,
                        1,
                        List.of("exploration_event", "reward_redemption"),
                        501L,
                        LocalDateTime.of(2026, 4, 29, 11, 40),
                        LocalDateTime.of(2026, 4, 29, 12, 0)
                )
        );

        assertThat(page.getTotal()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getList()).hasSize(1);
        assertThat(page.getList().get(0).getEntryId()).isEqualTo("event:2");
    }

    @Test
    void marksRouteTraceUnavailableWhenNoVerifiedStorageExists() {
        stubWorkbenchBase();
        service = new AdminTravelerProgressServiceImpl(
                travelerProfileMapper,
                testAccountMapper,
                cityMapper,
                readMapper,
                calculatorService,
                new EmptyRouteTraceSourceAdapter()
        );

        AdminTravelerProgressWorkbenchResponse response = service.getProgressWorkbench(88L);

        assertThat(response.getExplorationContext().getRouteTrace().getSourceStatus()).isEqualTo("unavailable");
        assertThat(response.getExplorationContext().getRouteTrace().getMessage()).contains("No verified route-trace storage");
    }

    @Test
    void legacyTravelerProgressSnapshotIsCompatibilityOnly() {
        stubWorkbenchBase();
        when(routeTraceSourceAdapter.loadRouteTrace(88L, null, null)).thenReturn(
                new RouteTraceSourceAdapter.RouteTraceSnapshot("unavailable", "No verified route trace store", List.of())
        );

        AdminTravelerProgressWorkbenchResponse response = service.getProgressWorkbench(88L);

        assertThat(response.getLegacyProgressSnapshot()).hasSize(2);
        assertThat(response.getLegacyProgressSnapshot().get(0).getSourceTable()).isEqualTo("traveler_progress");
        assertThat(response.getLegacyProgressSnapshot().get(0).isCompatibilityOnly()).isTrue();
        assertThat(response.getLegacyProgressSnapshot().get(0).getLabel())
                .isEqualTo("Legacy snapshot, not used for dynamic weighted progress");
        assertThat(response.getDynamicProgress().getGlobalSummary().getProgressPercent()).isEqualTo(61.54d);
        assertThat(response.getLegacyProgressSnapshot().get(0).getLegacyPercentValue()).isEqualTo(35);
    }

    private void stubWorkbenchBase() {
        when(travelerProfileMapper.selectById(88L)).thenReturn(profile());
        when(testAccountMapper.selectCount(any())).thenReturn(1L);
        when(cityMapper.selectById(1L)).thenReturn(city());
        when(readMapper.selectUserPreference(88L)).thenReturn(preference());
        when(readMapper.selectLinkedScopes(88L)).thenReturn(List.of(
                linkedScope("city", 1L, "Macau", "current_city", "user_profiles.current_city_id"),
                linkedScope("sub_map", 2L, "Peninsula", "recent_checkin", "user_checkins"),
                linkedScope("storyline", 501L, "Fire Route", "active_storyline", "user_progress")
        ));
        when(calculatorService.calculateSummary(88L, "global", null, false)).thenReturn(summary("global", null, 61.54d));
        when(calculatorService.calculateSummary(88L, "city", 1L, false)).thenReturn(summary("city", 1L, 80.0d));
        when(calculatorService.calculateSummary(88L, "sub_map", 2L, false)).thenReturn(summary("sub_map", 2L, 72.0d));
        when(calculatorService.calculateSummary(88L, "storyline", 501L, false)).thenReturn(summary("storyline", 501L, 50.0d));
        when(readMapper.selectLegacyProgressRows(88L)).thenReturn(List.of(
                legacyRow(null, null, null, 35, 501L, false, LocalDateTime.of(2026, 4, 29, 10, 0), LocalDateTime.of(2026, 4, 29, 10, 5)),
                legacyRow(501L, "storyline", "Fire Route", 40, 501L, false, LocalDateTime.of(2026, 4, 29, 10, 0), LocalDateTime.of(2026, 4, 29, 10, 5))
        ));
        when(readMapper.selectStorylineSessions(88L, 5)).thenReturn(List.of(
                sessionRow("sess-1", 501L, "Fire Route", 601L, "started", LocalDateTime.of(2026, 4, 29, 9, 0),
                        LocalDateTime.of(2026, 4, 29, 9, 15), null, 3, false, "{\"step\":1}")
        ));
        when(readMapper.selectRewardRedemptions(88L, 5)).thenReturn(List.of(
                redemptionRow(9L, 12L, "Explorer Pin", "redeemed", 20, LocalDateTime.of(2026, 4, 29, 8, 0), LocalDateTime.of(2026, 4, 30, 8, 0))
        ));
        when(readMapper.selectRecentContextCounts(88L, null)).thenReturn(
                new AdminTravelerProgressReadMapper.RecentContextCountsRow(2, 3, 4)
        );
    }

    private TravelerProfile profile() {
        TravelerProfile profile = new TravelerProfile();
        profile.setId(88L);
        profile.setOpenId("openid-88");
        profile.setNickname("Traveler");
        profile.setAvatarUrl("https://example.test/avatar.png");
        profile.setLevel(7);
        profile.setTotalStamps(18);
        profile.setCurrentExp(120);
        profile.setNextLevelExp(180);
        profile.setCurrentLocaleCode("zh-Hant");
        profile.setCurrentCityId(1L);
        return profile;
    }

    private City city() {
        City city = new City();
        city.setId(1L);
        city.setNameZht("Macau");
        return city;
    }

    private AdminTravelerProgressReadMapper.UserPreferenceRow preference() {
        return new AdminTravelerProgressReadMapper.UserPreferenceRow(
                88L,
                "story",
                new BigDecimal("1.1"),
                true,
                true,
                false,
                "zh-Hant",
                "Alex",
                "12345678",
                "{\"contrast\":true}"
        );
    }

    private AdminTravelerProgressReadMapper.LinkedScopeRow linkedScope(
            String scopeType,
            Long scopeId,
            String scopeName,
            String relationLabel,
            String source) {
        return new AdminTravelerProgressReadMapper.LinkedScopeRow(scopeType, scopeId, scopeName, relationLabel, source);
    }

    private AdminUserProgressSummaryResponse summary(String scopeType, Long scopeId, double progressPercent) {
        return AdminUserProgressSummaryResponse.builder()
                .userId(88L)
                .scopeType(scopeType)
                .scopeId(scopeId)
                .completedWeight(8)
                .availableWeight(13)
                .completedElementCount(1)
                .availableElementCount(2)
                .retiredCompletedWeight(0)
                .retiredCompletedCount(0)
                .progressPercent(progressPercent)
                .lastRecomputeTime(LocalDateTime.of(2026, 4, 29, 12, 30))
                .build();
    }

    private AdminTravelerProgressReadMapper.LegacyProgressRow legacyRow(
            Long scopeId,
            String scopeType,
            String scopeName,
            Integer progressPercent,
            Long activeStorylineId,
            Boolean completedStoryline,
            LocalDateTime lastSeenAt,
            LocalDateTime updatedAt) {
        return new AdminTravelerProgressReadMapper.LegacyProgressRow(
                88L,
                scopeId,
                scopeType,
                scopeName,
                progressPercent,
                activeStorylineId,
                completedStoryline,
                lastSeenAt,
                updatedAt
        );
    }

    private AdminTravelerProgressReadMapper.StorylineSessionRow sessionRow(
            String sessionId,
            Long storylineId,
            String storylineName,
            Long currentChapterId,
            String status,
            LocalDateTime startedAt,
            LocalDateTime lastEventAt,
            LocalDateTime exitedAt,
            Integer eventCount,
            Boolean exitClearedTemporaryState,
            String temporaryStepStateJson) {
        return new AdminTravelerProgressReadMapper.StorylineSessionRow(
                sessionId,
                88L,
                storylineId,
                storylineName,
                currentChapterId,
                status,
                startedAt,
                lastEventAt,
                exitedAt,
                eventCount,
                exitClearedTemporaryState,
                temporaryStepStateJson
        );
    }

    private AdminTravelerProgressReadMapper.RewardRedemptionRow redemptionRow(
            Long redemptionId,
            Long rewardId,
            String rewardName,
            String redemptionStatus,
            Integer stampCostSnapshot,
            LocalDateTime redeemedAt,
            LocalDateTime expiresAt) {
        return new AdminTravelerProgressReadMapper.RewardRedemptionRow(
                redemptionId,
                88L,
                rewardId,
                rewardName,
                redemptionStatus,
                stampCostSnapshot,
                redeemedAt,
                expiresAt
        );
    }

    private AdminTravelerProgressReadMapper.TimelineSourceRow timelineRow(
            String entryId,
            String entryType,
            String sourceTable,
            Long sourceRecordId,
            Long storylineId,
            String storylineName,
            Long poiId,
            String poiName,
            String title,
            String summary,
            String payloadPreview,
            String rawPayload,
            LocalDateTime occurredAt) {
        return new AdminTravelerProgressReadMapper.TimelineSourceRow(
                entryId,
                entryType,
                sourceTable,
                sourceRecordId,
                88L,
                storylineId,
                storylineName,
                poiId,
                poiName,
                title,
                summary,
                payloadPreview,
                rawPayload,
                occurredAt
        );
    }
}
