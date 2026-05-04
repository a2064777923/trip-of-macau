package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.response.AdminTravelerProgressWorkbenchResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerRewardRuleTraceResponse;
import com.aoxiaoyou.admin.dto.response.AdminTravelerRewardStateResponse;
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
                .filter(row -> matchesChapter(row, normalizedQuery.chapterId()))
                .filter(row -> matchesPoi(row, normalizedQuery.poiId()))
                .filter(row -> matchesMapScope(row, normalizedQuery.mapScopeType(), normalizedQuery.mapScopeId()))
                .filter(row -> matchesStatus(row, normalizedQuery.status()))
                .filter(row -> matchesRewardType(row, normalizedQuery.rewardType()))
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

    @Override
    public AdminTravelerRewardStateResponse getRewardState(Long userId) {
        requireProfile(userId);
        List<AdminTravelerProgressReadMapper.RewardRedemptionRow> redeemableRows =
                safeList(readMapper.selectAllRewardRedemptions(userId));
        List<AdminTravelerRewardStateResponse.RedeemableRewardItem> redeemableRewards = redeemableRows.stream()
                .map(row -> AdminTravelerRewardStateResponse.RedeemableRewardItem.builder()
                        .redemptionId(row.getRedemptionId())
                        .rewardId(row.getRewardId())
                        .rewardName(row.getRewardName())
                        .redemptionStatus(row.getRedemptionStatus())
                        .stampCostSnapshot(row.getStampCostSnapshot())
                        .redeemedAt(row.getRedeemedAt())
                        .expiresAt(row.getExpiresAt())
                        .build())
                .toList();

        LocalDateTime lastEarnedAt = redeemableRows.stream()
                .map(AdminTravelerProgressReadMapper.RewardRedemptionRow::getRedeemedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return AdminTravelerRewardStateResponse.builder()
                .userId(userId)
                .backpackItems(Collections.emptyList())
                .gameRewards(Collections.emptyList())
                .titles(Collections.emptyList())
                .redeemableRewards(redeemableRewards)
                .summary(AdminTravelerRewardStateResponse.Summary.builder()
                        .backpackCount(0)
                        .gameRewardCount(0)
                        .titleCount(0)
                        .redeemableRewardCount(redeemableRewards.size())
                        .lastEarnedAt(lastEarnedAt)
                        .build())
                .build();
    }

    @Override
    public AdminTravelerRewardRuleTraceResponse getRewardRuleTrace(
            Long userId,
            Long sourceEventId,
            Long ruleId,
            Long rewardId,
            Long gameRewardId) {
        requireProfile(userId);
        AdminTravelerProgressReadMapper.TraceEventElementRow event = sourceEventId == null
                ? null : readMapper.selectTraceEventElement(userId, sourceEventId);
        List<String> missingLinks = new ArrayList<>();
        if (sourceEventId != null && event == null) {
            missingLinks.add("找不到來源事件");
        }
        if (event != null && event.getElementId() == null && !StringUtils.hasText(event.getElementCode())) {
            missingLinks.add("來源事件未連結探索元素");
        }

        List<AdminTravelerProgressReadMapper.RuleBindingTraceRow> ruleRows = safeList(readMapper.selectTraceRuleBindings(
                ruleId,
                rewardId,
                gameRewardId,
                event == null ? null : event.getElementId(),
                event == null ? "" : defaultText(event.getOwnerType()),
                event == null ? null : event.getOwnerId(),
                event == null ? null : event.getStorylineId(),
                event == null ? null : event.getChapterId(),
                event == null ? null : event.getPoiId()
        ));
        if (ruleRows.isEmpty()) {
            missingLinks.add("找不到獎勵規則綁定");
        }

        List<AdminTravelerRewardRuleTraceResponse.RuleNode> rules = ruleRows.stream()
                .map(this::toRuleNode)
                .toList();
        boolean hasDisabledRule = ruleRows.stream().anyMatch(row -> !isRuleEnabled(row.getRuleStatus()));
        boolean hasEnabledRule = ruleRows.stream().anyMatch(row -> isRuleEnabled(row.getRuleStatus()));
        boolean hasRedemption = rewardId != null && readMapper.countRewardRedemptions(userId, rewardId) > 0;

        String traceStatus;
        if (!missingLinks.isEmpty()) {
            traceStatus = AdminTravelerRewardRuleTraceResponse.MISSING_LINK;
        } else if (hasDisabledRule && !hasEnabledRule) {
            traceStatus = AdminTravelerRewardRuleTraceResponse.RULE_DISABLED;
        } else if (hasRedemption) {
            traceStatus = AdminTravelerRewardRuleTraceResponse.ELIGIBLE_GRANTED;
        } else if (conditionsContainExplicitFailure(ruleRows)) {
            traceStatus = AdminTravelerRewardRuleTraceResponse.NOT_ELIGIBLE;
        } else {
            traceStatus = AdminTravelerRewardRuleTraceResponse.DATA_UNAVAILABLE;
            missingLinks.add("grant_result_unavailable");
        }

        return AdminTravelerRewardRuleTraceResponse.builder()
                .userId(userId)
                .sourceEventId(sourceEventId)
                .traceStatus(traceStatus)
                .traceStatusLabel(traceStatusLabel(traceStatus))
                .explanation(traceExplanation(traceStatus, missingLinks))
                .event(toEventNode(event))
                .explorationElement(toElementNode(event))
                .experienceStep(null)
                .rules(rules)
                .grants(buildGrantNodes(userId, rewardId, gameRewardId, sourceEventId, ruleRows))
                .missingLinks(missingLinks)
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

    private boolean matchesChapter(
            AdminTravelerProgressReadMapper.TimelineSourceRow row,
            Long chapterId) {
        return chapterId == null || (row.getChapterId() != null && Objects.equals(row.getChapterId(), chapterId));
    }

    private boolean matchesPoi(
            AdminTravelerProgressReadMapper.TimelineSourceRow row,
            Long poiId) {
        return poiId == null || (row.getPoiId() != null && Objects.equals(row.getPoiId(), poiId));
    }

    private boolean matchesMapScope(
            AdminTravelerProgressReadMapper.TimelineSourceRow row,
            String mapScopeType,
            Long mapScopeId) {
        if (!StringUtils.hasText(mapScopeType) && mapScopeId == null) {
            return true;
        }
        if (!StringUtils.hasText(mapScopeType) || mapScopeId == null) {
            return false;
        }
        return switch (normalizeToken(mapScopeType)) {
            case "city" -> row.getCityId() != null && Objects.equals(row.getCityId(), mapScopeId);
            case "sub_map" -> row.getSubMapId() != null && Objects.equals(row.getSubMapId(), mapScopeId);
            case "poi" -> row.getPoiId() != null && Objects.equals(row.getPoiId(), mapScopeId);
            case "storyline" -> row.getStorylineId() != null && Objects.equals(row.getStorylineId(), mapScopeId);
            default -> false;
        };
    }

    private boolean matchesStatus(
            AdminTravelerProgressReadMapper.TimelineSourceRow row,
            String status) {
        if (!StringUtils.hasText(status)) {
            return true;
        }
        String rowStatus = normalizeToken(row.getStatus());
        return StringUtils.hasText(rowStatus) && Objects.equals(rowStatus, normalizeToken(status));
    }

    private boolean matchesRewardType(
            AdminTravelerProgressReadMapper.TimelineSourceRow row,
            String rewardType) {
        if (!StringUtils.hasText(rewardType)) {
            return true;
        }
        String rowRewardType = normalizeToken(row.getRewardType());
        return StringUtils.hasText(rowRewardType) && Objects.equals(rowRewardType, normalizeToken(rewardType));
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
                .chapterId(row.getChapterId())
                .chapterName(row.getChapterName())
                .poiId(row.getPoiId())
                .poiName(row.getPoiName())
                .cityId(row.getCityId())
                .subMapId(row.getSubMapId())
                .status(row.getStatus())
                .rewardType(row.getRewardType())
                .rewardId(row.getRewardId())
                .gameRewardId(row.getGameRewardId())
                .title(row.getTitle())
                .summary(row.getSummary())
                .payloadPreview(StringUtils.hasText(row.getPayloadPreview())
                        ? row.getPayloadPreview()
                        : preview(row.getRawPayload()))
                .rawPayload(row.getRawPayload())
                .occurredAt(row.getOccurredAt())
                .build();
    }

    private AdminTravelerRewardRuleTraceResponse.RuleNode toRuleNode(
            AdminTravelerProgressReadMapper.RuleBindingTraceRow row) {
        List<AdminTravelerProgressReadMapper.ConditionGroupTraceRow> groups =
                safeList(readMapper.selectConditionGroups(row.getRuleId()));
        List<AdminTravelerProgressReadMapper.ConditionTraceRow> conditions =
                safeList(readMapper.selectConditions(row.getRuleId()));
        Map<Long, List<AdminTravelerProgressReadMapper.ConditionTraceRow>> conditionsByGroup = new LinkedHashMap<>();
        for (AdminTravelerProgressReadMapper.ConditionTraceRow condition : conditions) {
            conditionsByGroup.computeIfAbsent(condition.getGroupId(), ignored -> new ArrayList<>()).add(condition);
        }

        return AdminTravelerRewardRuleTraceResponse.RuleNode.builder()
                .ruleId(row.getRuleId())
                .code(row.getRuleCode())
                .ruleType(row.getRuleType())
                .status(row.getRuleStatus())
                .name(row.getRuleName())
                .summaryText(row.getSummaryText())
                .bindingOwnerDomain(row.getOwnerDomain())
                .bindingOwnerId(row.getOwnerId())
                .bindingRole(row.getBindingRole())
                .conditionGroups(groups.stream()
                        .map(group -> AdminTravelerRewardRuleTraceResponse.ConditionGroupNode.builder()
                                .groupId(group.getGroupId())
                                .groupCode(group.getGroupCode())
                                .operatorType(group.getOperatorType())
                                .minimumMatchCount(group.getMinimumMatchCount())
                                .summaryText(group.getSummaryText())
                                .conditions(safeList(conditionsByGroup.get(group.getGroupId())).stream()
                                        .map(condition -> AdminTravelerRewardRuleTraceResponse.ConditionNode.builder()
                                                .conditionId(condition.getConditionId())
                                                .conditionType(condition.getConditionType())
                                                .metricType(condition.getMetricType())
                                                .operatorType(condition.getOperatorType())
                                                .comparatorValue(condition.getComparatorValue())
                                                .comparatorUnit(condition.getComparatorUnit())
                                                .summaryText(condition.getSummaryText())
                                                .evaluationStatus("data_unavailable")
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .build();
    }

    private AdminTravelerRewardRuleTraceResponse.EventNode toEventNode(
            AdminTravelerProgressReadMapper.TraceEventElementRow event) {
        if (event == null) {
            return null;
        }
        return AdminTravelerRewardRuleTraceResponse.EventNode.builder()
                .eventId(event.getEventId())
                .elementId(event.getElementId())
                .elementCode(event.getElementCode())
                .eventType(event.getEventType())
                .eventSource(event.getEventSource())
                .storylineSessionId(event.getStorylineSessionId())
                .payloadPreview(StringUtils.hasText(event.getPayloadPreview())
                        ? event.getPayloadPreview()
                        : preview(event.getRawPayload()))
                .occurredAt(event.getOccurredAt())
                .build();
    }

    private AdminTravelerRewardRuleTraceResponse.ElementNode toElementNode(
            AdminTravelerProgressReadMapper.TraceEventElementRow event) {
        if (event == null || event.getElementId() == null) {
            return null;
        }
        return AdminTravelerRewardRuleTraceResponse.ElementNode.builder()
                .elementId(event.getElementId())
                .elementCode(event.getElementCode())
                .elementType(event.getElementType())
                .ownerType(event.getOwnerType())
                .ownerId(event.getOwnerId())
                .ownerCode(event.getOwnerCode())
                .cityId(event.getCityId())
                .subMapId(event.getSubMapId())
                .poiId(event.getPoiId())
                .storylineId(event.getStorylineId())
                .chapterId(event.getChapterId())
                .title(event.getElementTitle())
                .status(event.getElementStatus())
                .build();
    }

    private List<AdminTravelerRewardRuleTraceResponse.GrantNode> buildGrantNodes(
            Long userId,
            Long rewardId,
            Long gameRewardId,
            Long sourceEventId,
            List<AdminTravelerProgressReadMapper.RuleBindingTraceRow> rules) {
        if (rewardId == null || readMapper.countRewardRedemptions(userId, rewardId) <= 0) {
            return Collections.emptyList();
        }
        Long sourceRuleId = rules.isEmpty() ? null : rules.get(0).getRuleId();
        return List.of(AdminTravelerRewardRuleTraceResponse.GrantNode.builder()
                .grantSource("reward_redemptions")
                .rewardId(rewardId)
                .gameRewardId(gameRewardId)
                .sourceEventId(sourceEventId)
                .sourceRuleId(sourceRuleId)
                .grantStatus("present")
                .build());
    }

    private boolean isRuleEnabled(String status) {
        String normalized = normalizeToken(status);
        return "published".equals(normalized) || "active".equals(normalized);
    }

    private boolean conditionsContainExplicitFailure(List<AdminTravelerProgressReadMapper.RuleBindingTraceRow> ruleRows) {
        for (AdminTravelerProgressReadMapper.RuleBindingTraceRow rule : ruleRows) {
            List<AdminTravelerProgressReadMapper.ConditionTraceRow> conditions =
                    safeList(readMapper.selectConditions(rule.getRuleId()));
            for (AdminTravelerProgressReadMapper.ConditionTraceRow condition : conditions) {
                if ("always_false".equals(normalizeToken(condition.getConditionType()))
                        || "disabled".equals(normalizeToken(condition.getOperatorType()))) {
                    return true;
                }
            }
        }
        return false;
    }

    private String traceStatusLabel(String traceStatus) {
        return switch (traceStatus) {
            case AdminTravelerRewardRuleTraceResponse.ELIGIBLE_GRANTED -> "已符合並已發放";
            case AdminTravelerRewardRuleTraceResponse.ELIGIBLE_ALREADY_GRANTED -> "已符合且已擁有";
            case AdminTravelerRewardRuleTraceResponse.NOT_ELIGIBLE -> "條件未符合";
            case AdminTravelerRewardRuleTraceResponse.MISSING_LINK -> "找不到必要關聯";
            case AdminTravelerRewardRuleTraceResponse.RULE_DISABLED -> "規則未啟用";
            default -> "資料不足";
        };
    }

    private String traceExplanation(String traceStatus, List<String> missingLinks) {
        return switch (traceStatus) {
            case AdminTravelerRewardRuleTraceResponse.ELIGIBLE_GRANTED -> "已找到旅客獎勵或兌換紀錄，可確認發放結果。";
            case AdminTravelerRewardRuleTraceResponse.NOT_ELIGIBLE -> "已找到可判斷的條件，且至少一組必要條件明確未符合。";
            case AdminTravelerRewardRuleTraceResponse.MISSING_LINK -> "找不到完整規則鏈路：" + String.join("、", missingLinks);
            case AdminTravelerRewardRuleTraceResponse.RULE_DISABLED -> "找到對應規則，但規則狀態不是已發佈或啟用。";
            default -> "目前資料不足以重播條件或確認發放結果：" + String.join("、", missingLinks);
        };
    }

    private TimelineQuery normalizeQuery(TimelineQuery query) {
        if (query == null) {
            return new TimelineQuery(1, 20, Collections.emptyList(), null, null, null, null, null, null, null, null, null);
        }
        long pageNum = Math.max(1, query.pageNum());
        long pageSize = query.pageSize() <= 0 ? 20 : Math.min(query.pageSize(), MAX_PAGE_SIZE);
        List<String> eventTypes = query.eventTypes() == null ? Collections.emptyList() : query.eventTypes().stream()
                .filter(StringUtils::hasText)
                .map(this::normalizeToken)
                .toList();
        return new TimelineQuery(
                pageNum,
                pageSize,
                eventTypes,
                query.storylineId(),
                query.chapterId(),
                query.poiId(),
                normalizeNullableToken(query.mapScopeType()),
                query.mapScopeId(),
                normalizeNullableToken(query.status()),
                normalizeNullableToken(query.rewardType()),
                query.from(),
                query.to());
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

    private String normalizeNullableToken(String value) {
        String normalized = normalizeToken(value);
        return StringUtils.hasText(normalized) ? normalized : null;
    }

    private String defaultText(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? Collections.emptyList() : values;
    }
}
