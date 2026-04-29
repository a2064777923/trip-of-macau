package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.response.AdminTravelerProgressWorkbenchResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerTimelineEntryResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressBreakdownResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressSummaryResponse;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.TestAccount;
import com.aoxiaoyou.admin.entity.TravelerProfile;
import com.aoxiaoyou.admin.mapper.AdminTravelerProgressReadMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.TestAccountMapper;
import com.aoxiaoyou.admin.mapper.TravelerProfileMapper;
import com.aoxiaoyou.admin.service.AdminTravelerProgressService;
import com.aoxiaoyou.admin.service.AdminUserProgressCalculatorService;
import com.aoxiaoyou.admin.service.support.RouteTraceSourceAdapter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminTravelerProgressServiceImpl implements AdminTravelerProgressService {

    private static final String GLOBAL_SCOPE = "global";
    private static final int WORKBENCH_SESSION_LIMIT = 5;
    private static final int WORKBENCH_REWARD_LIMIT = 5;
    private static final int MAX_PAGE_SIZE = 200;
    private static final String LEGACY_PROGRESS_LABEL = "Legacy snapshot, not used for dynamic weighted progress";

    private final TravelerProfileMapper travelerProfileMapper;
    private final TestAccountMapper testAccountMapper;
    private final CityMapper cityMapper;
    private final AdminTravelerProgressReadMapper readMapper;
    private final AdminUserProgressCalculatorService calculatorService;
    private final RouteTraceSourceAdapter routeTraceSourceAdapter;

    @Override
    public AdminTravelerProgressWorkbenchResponse getProgressWorkbench(Long userId) {
        TravelerProfile profile = requireProfile(userId);
        City currentCity = profile.getCurrentCityId() == null ? null : cityMapper.selectById(profile.getCurrentCityId());
        AdminTravelerProgressReadMapper.UserPreferenceRow preference = readMapper.selectUserPreference(userId);
        List<AdminTravelerProgressReadMapper.LinkedScopeRow> linkedScopeRows = safeList(readMapper.selectLinkedScopes(userId));
        List<AdminTravelerProgressReadMapper.LegacyProgressRow> legacyRows = safeList(readMapper.selectLegacyProgressRows(userId));
        List<AdminTravelerProgressReadMapper.StorylineSessionRow> sessionRows =
                safeList(readMapper.selectStorylineSessions(userId, WORKBENCH_SESSION_LIMIT));
        List<AdminTravelerProgressReadMapper.RewardRedemptionRow> rewardRows =
                safeList(readMapper.selectRewardRedemptions(userId, WORKBENCH_REWARD_LIMIT));
        AdminTravelerProgressReadMapper.RecentContextCountsRow contextCounts = readMapper.selectRecentContextCounts(userId, null);
        RouteTraceSourceAdapter.RouteTraceSnapshot routeTrace = routeTraceSourceAdapter.loadRouteTrace(userId, null, null);

        AdminUserProgressSummaryResponse globalSummary = calculatorService.calculateSummary(userId, GLOBAL_SCOPE, null, false);
        List<AdminTravelerProgressWorkbenchResponse.ScopedProgressSummary> scopedSummaries =
                buildScopedSummaries(userId, linkedScopeRows);

        return AdminTravelerProgressWorkbenchResponse.builder()
                .userId(userId)
                .identity(buildIdentity(profile, currentCity, isTestAccount(userId)))
                .preferences(buildPreferences(preference))
                .linkedScopes(linkedScopeRows.stream().map(this::toLinkedScope).toList())
                .dynamicProgress(AdminTravelerProgressWorkbenchResponse.DynamicProgressSection.builder()
                        .globalSummary(globalSummary)
                        .scopedSummaries(scopedSummaries)
                        .breakdownEndpoint("/api/admin/v1/users/" + userId + "/progress-breakdown")
                        .comparisonHint("Legacy compatibility snapshots are separate from dynamic weighted progress.")
                        .build())
                .legacyProgressSnapshot(legacyRows.stream().map(this::toLegacyProgressSnapshot).toList())
                .storylineSessions(sessionRows.stream().map(this::toStorylineSessionSummary).toList())
                .rewardRedemptions(rewardRows.stream().map(this::toRewardRedemptionSummary).toList())
                .explorationContext(buildExplorationContext(contextCounts, routeTrace))
                .build();
    }

    @Override
    public AdminUserProgressBreakdownResponse getProgressBreakdown(
            Long userId,
            String scopeType,
            Long scopeId,
            boolean includeInactiveElements) {
        requireProfile(userId);
        String normalizedScopeType = normalizeScopeType(scopeType);
        return calculatorService.calculateBreakdown(userId, normalizedScopeType, scopeId, includeInactiveElements);
    }

    @Override
    public PageResponse<AdminTravelerTimelineEntryResponse> getTimeline(Long userId, TimelineQuery query) {
        requireProfile(userId);
        TimelineQuery normalizedQuery = normalizeQuery(query);
        List<AdminTravelerProgressReadMapper.TimelineSourceRow> rows = loadTimelineRows(userId).stream()
                .filter(row -> matchesEventType(row, normalizedQuery.eventTypes()))
                .filter(row -> matchesStoryline(row, normalizedQuery.storylineId()))
                .filter(row -> matchesRange(row, normalizedQuery.from(), normalizedQuery.to()))
                .sorted(Comparator
                        .comparing(AdminTravelerProgressReadMapper.TimelineSourceRow::getOccurredAt,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AdminTravelerProgressReadMapper.TimelineSourceRow::getSourceRecordId,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AdminTravelerProgressReadMapper.TimelineSourceRow::getEntryId,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        int fromIndex = Math.min((int) ((normalizedQuery.pageNum() - 1) * normalizedQuery.pageSize()), rows.size());
        int toIndex = Math.min(fromIndex + (int) normalizedQuery.pageSize(), rows.size());
        List<AdminTravelerTimelineEntryResponse> pageItems = rows.subList(fromIndex, toIndex).stream()
                .map(this::toTimelineEntry)
                .toList();
        long total = rows.size();
        long totalPages = total == 0 ? 0 : (long) Math.ceil(total / (double) normalizedQuery.pageSize());

        return PageResponse.<AdminTravelerTimelineEntryResponse>builder()
                .pageNum(normalizedQuery.pageNum())
                .pageSize(normalizedQuery.pageSize())
                .total(total)
                .totalPages(totalPages)
                .list(pageItems)
                .build();
    }

    private TravelerProfile requireProfile(Long userId) {
        TravelerProfile profile = travelerProfileMapper.selectById(userId);
        if (profile == null) {
            throw new BusinessException(4040, "User not found");
        }
        return profile;
    }

    private boolean isTestAccount(Long userId) {
        return testAccountMapper.selectCount(new LambdaQueryWrapper<TestAccount>()
                .eq(TestAccount::getUserId, userId)) > 0;
    }

    private AdminTravelerProgressWorkbenchResponse.IdentitySection buildIdentity(
            TravelerProfile profile,
            City currentCity,
            boolean isTestAccount) {
        return AdminTravelerProgressWorkbenchResponse.IdentitySection.builder()
                .userId(profile.getId())
                .openId(profile.getOpenId())
                .nickname(profile.getNickname())
                .avatarUrl(profile.getAvatarUrl())
                .level(profile.getLevel())
                .totalStamps(profile.getTotalStamps())
                .currentExp(profile.getCurrentExp())
                .nextLevelExp(profile.getNextLevelExp())
                .currentLocaleCode(profile.getCurrentLocaleCode())
                .testAccount(isTestAccount)
                .currentCityId(profile.getCurrentCityId())
                .currentCityName(resolveCityName(currentCity))
                .build();
    }

    private AdminTravelerProgressWorkbenchResponse.PreferenceSection buildPreferences(
            AdminTravelerProgressReadMapper.UserPreferenceRow preference) {
        if (preference == null) {
            return AdminTravelerProgressWorkbenchResponse.PreferenceSection.builder().build();
        }
        return AdminTravelerProgressWorkbenchResponse.PreferenceSection.builder()
                .interfaceMode(preference.getInterfaceMode())
                .fontScale(preference.getFontScale())
                .highContrast(preference.getHighContrast())
                .voiceGuideEnabled(preference.getVoiceGuideEnabled())
                .seniorMode(preference.getSeniorMode())
                .localeCode(preference.getLocaleCode())
                .emergencyContactName(preference.getEmergencyContactName())
                .emergencyContactPhone(preference.getEmergencyContactPhone())
                .runtimeOverridesJson(preference.getRuntimeOverridesJson())
                .build();
    }

    private List<AdminTravelerProgressWorkbenchResponse.ScopedProgressSummary> buildScopedSummaries(
            Long userId,
            List<AdminTravelerProgressReadMapper.LinkedScopeRow> linkedScopeRows) {
        if (linkedScopeRows.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> seenKeys = new LinkedHashSet<>();
        List<AdminTravelerProgressWorkbenchResponse.ScopedProgressSummary> summaries = new ArrayList<>();
        for (AdminTravelerProgressReadMapper.LinkedScopeRow row : linkedScopeRows) {
            String scopeType = normalizeScopeType(row.getScopeType());
            String dedupeKey = scopeType + ":" + row.getScopeId();
            if (!seenKeys.add(dedupeKey)) {
                continue;
            }
            summaries.add(AdminTravelerProgressWorkbenchResponse.ScopedProgressSummary.builder()
                    .scopeType(scopeType)
                    .scopeId(row.getScopeId())
                    .scopeName(row.getScopeName())
                    .summary(calculatorService.calculateSummary(userId, scopeType, row.getScopeId(), false))
                    .build());
        }
        return summaries;
    }

    private AdminTravelerProgressWorkbenchResponse.LinkedScopeSummary toLinkedScope(
            AdminTravelerProgressReadMapper.LinkedScopeRow row) {
        return AdminTravelerProgressWorkbenchResponse.LinkedScopeSummary.builder()
                .scopeType(normalizeScopeType(row.getScopeType()))
                .scopeId(row.getScopeId())
                .scopeName(row.getScopeName())
                .relationLabel(row.getRelationLabel())
                .source(row.getSource())
                .build();
    }

    private AdminTravelerProgressWorkbenchResponse.LegacyProgressSnapshot toLegacyProgressSnapshot(
            AdminTravelerProgressReadMapper.LegacyProgressRow row) {
        String scopeType = StringUtils.hasText(row.getScopeType()) ? normalizeScopeType(row.getScopeType()) : GLOBAL_SCOPE;
        return AdminTravelerProgressWorkbenchResponse.LegacyProgressSnapshot.builder()
                .legacyScopeType(scopeType)
                .legacyScopeId(row.getScopeId())
                .legacyScopeName(StringUtils.hasText(row.getScopeName()) ? row.getScopeName() : "Global")
                .legacyPercentValue(row.getProgressPercent())
                .activeStorylineId(row.getActiveStorylineId())
                .completedStoryline(row.getCompletedStoryline())
                .lastSeenAt(row.getLastSeenAt())
                .updatedAt(row.getUpdatedAt())
                .sourceTable("traveler_progress")
                .compatibilityOnly(true)
                .label(LEGACY_PROGRESS_LABEL)
                .build();
    }

    private AdminTravelerProgressWorkbenchResponse.StorylineSessionSummary toStorylineSessionSummary(
            AdminTravelerProgressReadMapper.StorylineSessionRow row) {
        return AdminTravelerProgressWorkbenchResponse.StorylineSessionSummary.builder()
                .sessionId(row.getSessionId())
                .storylineId(row.getStorylineId())
                .storylineName(row.getStorylineName())
                .currentChapterId(row.getCurrentChapterId())
                .status(row.getStatus())
                .startedAt(row.getStartedAt())
                .lastEventAt(row.getLastEventAt())
                .exitedAt(row.getExitedAt())
                .eventCount(row.getEventCount())
                .exitClearedTemporaryState(row.getExitClearedTemporaryState())
                .temporaryStepStateJson(row.getTemporaryStepStateJson())
                .build();
    }

    private AdminTravelerProgressWorkbenchResponse.RewardRedemptionSummary toRewardRedemptionSummary(
            AdminTravelerProgressReadMapper.RewardRedemptionRow row) {
        return AdminTravelerProgressWorkbenchResponse.RewardRedemptionSummary.builder()
                .redemptionId(row.getRedemptionId())
                .rewardId(row.getRewardId())
                .rewardName(row.getRewardName())
                .redemptionStatus(row.getRedemptionStatus())
                .stampCostSnapshot(row.getStampCostSnapshot())
                .redeemedAt(row.getRedeemedAt())
                .expiresAt(row.getExpiresAt())
                .build();
    }

    private AdminTravelerProgressWorkbenchResponse.ExplorationContext buildExplorationContext(
            AdminTravelerProgressReadMapper.RecentContextCountsRow contextCounts,
            RouteTraceSourceAdapter.RouteTraceSnapshot routeTrace) {
        return AdminTravelerProgressWorkbenchResponse.ExplorationContext.builder()
                .recentCheckinCount(contextCounts == null || contextCounts.getRecentCheckinCount() == null
                        ? 0 : contextCounts.getRecentCheckinCount())
                .recentExplorationEventCount(contextCounts == null || contextCounts.getRecentExplorationEventCount() == null
                        ? 0 : contextCounts.getRecentExplorationEventCount())
                .recentTriggerCount(contextCounts == null || contextCounts.getRecentTriggerCount() == null
                        ? 0 : contextCounts.getRecentTriggerCount())
                .routeTrace(AdminTravelerProgressWorkbenchResponse.RouteTraceStatus.builder()
                        .sourceStatus(routeTrace == null || !StringUtils.hasText(routeTrace.sourceStatus())
                                ? "unavailable" : routeTrace.sourceStatus())
                        .message(routeTrace == null ? "No verified route-trace storage is available." : routeTrace.message())
                        .build())
                .build();
    }

    private List<AdminTravelerProgressReadMapper.TimelineSourceRow> loadTimelineRows(Long userId) {
        List<AdminTravelerProgressReadMapper.TimelineSourceRow> rows = new ArrayList<>();
        rows.addAll(safeList(readMapper.selectCheckinTimelineRows(userId)));
        rows.addAll(safeList(readMapper.selectTriggerTimelineRows(userId)));
        rows.addAll(safeList(readMapper.selectExplorationTimelineRows(userId)));
        rows.addAll(safeList(readMapper.selectStorySessionTimelineRows(userId)));
        rows.addAll(safeList(readMapper.selectRewardTimelineRows(userId)));
        rows.addAll(safeList(readMapper.selectRepairAuditTimelineRows(userId)));
        return rows;
    }

    private boolean matchesEventType(
            AdminTravelerProgressReadMapper.TimelineSourceRow row,
            List<String> eventTypes) {
        if (eventTypes == null || eventTypes.isEmpty()) {
            return true;
        }
        String entryType = normalizeToken(row.getEntryType());
        return eventTypes.stream().map(this::normalizeToken).anyMatch(entryType::equals);
    }

    private boolean matchesStoryline(
            AdminTravelerProgressReadMapper.TimelineSourceRow row,
            Long storylineId) {
        return storylineId == null || Objects.equals(row.getStorylineId(), storylineId);
    }

    private boolean matchesRange(
            AdminTravelerProgressReadMapper.TimelineSourceRow row,
            LocalDateTime from,
            LocalDateTime to) {
        if (row.getOccurredAt() == null) {
            return false;
        }
        if (from != null && row.getOccurredAt().isBefore(from)) {
            return false;
        }
        return to == null || !row.getOccurredAt().isAfter(to);
    }

    private AdminTravelerTimelineEntryResponse toTimelineEntry(AdminTravelerProgressReadMapper.TimelineSourceRow row) {
        return AdminTravelerTimelineEntryResponse.builder()
                .entryId(row.getEntryId())
                .entryType(row.getEntryType())
                .sourceTable(row.getSourceTable())
                .sourceRecordId(row.getSourceRecordId())
                .userId(row.getUserId())
                .storylineId(row.getStorylineId())
                .storylineName(row.getStorylineName())
                .poiId(row.getPoiId())
                .poiName(row.getPoiName())
                .title(row.getTitle())
                .summary(row.getSummary())
                .payloadPreview(StringUtils.hasText(row.getPayloadPreview())
                        ? row.getPayloadPreview()
                        : preview(row.getRawPayload()))
                .rawPayload(row.getRawPayload())
                .occurredAt(row.getOccurredAt())
                .build();
    }

    private TimelineQuery normalizeQuery(TimelineQuery query) {
        if (query == null) {
            return new TimelineQuery(1, 20, Collections.emptyList(), null, null, null);
        }
        long pageNum = Math.max(1, query.pageNum());
        long pageSize = query.pageSize() <= 0 ? 20 : Math.min(query.pageSize(), MAX_PAGE_SIZE);
        List<String> eventTypes = query.eventTypes() == null ? Collections.emptyList() : query.eventTypes().stream()
                .filter(StringUtils::hasText)
                .map(this::normalizeToken)
                .toList();
        return new TimelineQuery(pageNum, pageSize, eventTypes, query.storylineId(), query.from(), query.to());
    }

    private String resolveCityName(City city) {
        if (city == null) {
            return null;
        }
        if (StringUtils.hasText(city.getNameZht())) {
            return city.getNameZht().trim();
        }
        if (StringUtils.hasText(city.getNameZh())) {
            return city.getNameZh().trim();
        }
        if (StringUtils.hasText(city.getNameEn())) {
            return city.getNameEn().trim();
        }
        return city.getCode();
    }

    private String preview(String rawPayload) {
        if (!StringUtils.hasText(rawPayload)) {
            return null;
        }
        String normalized = rawPayload.trim();
        return normalized.length() <= 160 ? normalized : normalized.substring(0, 160);
    }

    private String normalizeScopeType(String scopeType) {
        String normalized = normalizeToken(scopeType);
        return StringUtils.hasText(normalized) ? normalized : GLOBAL_SCOPE;
    }

    private String normalizeToken(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }
}
