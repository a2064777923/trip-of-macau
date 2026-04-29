package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.dto.response.AdminUserProgressBreakdownResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressSummaryResponse;
import com.aoxiaoyou.admin.mapper.AdminUserProgressReadMapper;
import com.aoxiaoyou.admin.service.AdminUserProgressCalculatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AdminUserProgressCalculatorServiceImpl implements AdminUserProgressCalculatorService {

    private static final String SCOPE_GLOBAL = "global";
    private static final String STATUS_PUBLISHED = "published";
    private static final String OWNER_TYPE_EXPERIENCE_FLOW_STEP = "experience_flow_step";
    private static final String OWNER_TYPE_COLLECTIBLE = "collectible";
    private static final String OWNER_TYPE_REWARD = "reward";
    private static final String OWNER_TYPE_CONTENT_ASSET = "content_asset";

    private final AdminUserProgressReadMapper readMapper;

    @Override
    public AdminUserProgressSummaryResponse calculateSummary(Long userId, String scopeType, Long scopeId, boolean includeInactiveElements) {
        CalculationResult result = calculate(userId, scopeType, scopeId, includeInactiveElements);
        return AdminUserProgressSummaryResponse.builder()
                .userId(userId)
                .scopeType(result.scopeType())
                .scopeId(scopeId)
                .completedWeight(result.completedWeight())
                .availableWeight(result.availableWeight())
                .completedElementCount(result.completedElementCount())
                .availableElementCount(result.availableElementCount())
                .retiredCompletedWeight(result.retiredCompletedWeight())
                .retiredCompletedCount(result.retiredCompletedCount())
                .progressPercent(result.progressPercent())
                .lastRecomputeTime(result.lastRecomputeTime())
                .build();
    }

    @Override
    public AdminUserProgressBreakdownResponse calculateBreakdown(Long userId, String scopeType, Long scopeId, boolean includeInactiveElements) {
        CalculationResult result = calculate(userId, scopeType, scopeId, includeInactiveElements);
        return AdminUserProgressBreakdownResponse.builder()
                .userId(userId)
                .scopeType(result.scopeType())
                .scopeId(scopeId)
                .completedWeight(result.completedWeight())
                .availableWeight(result.availableWeight())
                .completedElementCount(result.completedElementCount())
                .availableElementCount(result.availableElementCount())
                .retiredCompletedWeight(result.retiredCompletedWeight())
                .retiredCompletedCount(result.retiredCompletedCount())
                .progressPercent(result.progressPercent())
                .lastRecomputeTime(result.lastRecomputeTime())
                .elements(result.activeElements())
                .retiredElements(includeInactiveElements ? result.retiredElements() : Collections.emptyList())
                .build();
    }

    private CalculationResult calculate(Long userId, String scopeType, Long scopeId, boolean includeInactiveElements) {
        String normalizedScopeType = normalizeScopeType(scopeType);
        List<AdminUserProgressReadMapper.ProgressElementRow> activeRows = readMapper.selectScopeElements(normalizedScopeType, scopeId, true);
        List<AdminUserProgressReadMapper.ProgressEventRow> allEvents = readMapper.selectUserEvents(userId);
        Map<String, AdminUserProgressReadMapper.ProgressEventRow> completionEventsByKey = indexCompletionEvents(allEvents);

        List<AdminUserProgressBreakdownResponse.ElementBreakdown> activeElements = activeRows.stream()
                .map(row -> toBreakdownElement(row, completionEventsByKey, true))
                .toList();
        List<AdminUserProgressBreakdownResponse.ElementBreakdown> retiredElements = loadRetiredElements(
                activeRows,
                completionEventsByKey,
                normalizedScopeType,
                scopeId)
                .stream()
                .map(row -> toBreakdownElement(row, completionEventsByKey, false))
                .toList();

        int availableWeight = activeElements.stream().mapToInt(AdminUserProgressBreakdownResponse.ElementBreakdown::getWeightValue).sum();
        int completedWeight = activeElements.stream()
                .filter(AdminUserProgressBreakdownResponse.ElementBreakdown::isCompleted)
                .mapToInt(AdminUserProgressBreakdownResponse.ElementBreakdown::getWeightValue)
                .sum();
        int completedElementCount = (int) activeElements.stream()
                .filter(AdminUserProgressBreakdownResponse.ElementBreakdown::isCompleted)
                .count();
        int retiredCompletedWeight = retiredElements.stream()
                .filter(AdminUserProgressBreakdownResponse.ElementBreakdown::isCompleted)
                .mapToInt(AdminUserProgressBreakdownResponse.ElementBreakdown::getWeightValue)
                .sum();
        double progressPercent = availableWeight == 0 ? 0 : Math.round((completedWeight * 10000.0 / availableWeight)) / 100.0;
        LocalDateTime lastRecomputeTime = readMapper.selectLastRecomputeTime(userId, normalizedScopeType, scopeId);

        return new CalculationResult(
                normalizedScopeType,
                completedWeight,
                availableWeight,
                completedElementCount,
                activeElements.size(),
                retiredCompletedWeight,
                retiredElements.size(),
                progressPercent,
                lastRecomputeTime,
                activeElements,
                retiredElements,
                includeInactiveElements
        );
    }

    private List<AdminUserProgressReadMapper.ProgressElementRow> loadRetiredElements(
            List<AdminUserProgressReadMapper.ProgressElementRow> activeRows,
            Map<String, AdminUserProgressReadMapper.ProgressEventRow> completionEventsByKey,
            String scopeType,
            Long scopeId) {
        if (completionEventsByKey.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> activeKeys = activeRows.stream()
                .flatMap(this::streamElementKeys)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Long> retiredIds = new LinkedHashSet<>();
        Set<String> retiredCodes = new LinkedHashSet<>();
        new LinkedHashSet<>(completionEventsByKey.values()).forEach(event -> {
            boolean matchesActive = activeKeys.contains(eventIdentityKey(event.getElementId(), null))
                    || activeKeys.contains(eventIdentityKey(null, event.getElementCode()));
            if (!matchesActive) {
                if (event.getElementId() != null) {
                    retiredIds.add(event.getElementId());
                }
                if (StringUtils.hasText(event.getElementCode())) {
                    retiredCodes.add(event.getElementCode().trim());
                }
            }
        });
        if (retiredIds.isEmpty() && retiredCodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> orderedCompletedIds = retiredIds.stream().sorted().toList();
        List<String> orderedCompletedCodes = retiredCodes.stream().sorted().toList();
        return readMapper.selectElementsByIdsOrCodes(orderedCompletedIds, orderedCompletedCodes).stream()
                .filter(row -> !activeKeys.contains(elementIdentityKey(row)))
                .filter(row -> matchesScope(row, scopeType, scopeId))
                .filter(row -> completionEventFor(row, completionEventsByKey) != null)
                .filter(row -> !isActiveElement(row))
                .toList();
    }

    private AdminUserProgressBreakdownResponse.ElementBreakdown toBreakdownElement(
            AdminUserProgressReadMapper.ProgressElementRow row,
            Map<String, AdminUserProgressReadMapper.ProgressEventRow> completionEventsByKey,
            boolean includedInCurrentPercentage) {
        AdminUserProgressReadMapper.ProgressEventRow event = completionEventFor(row, completionEventsByKey);
        return AdminUserProgressBreakdownResponse.ElementBreakdown.builder()
                .elementId(row.getElementId())
                .elementCode(row.getElementCode())
                .elementType(row.getElementType())
                .title(resolveTitle(row))
                .weightLevel(row.getWeightLevel())
                .weightValue(resolveWeightValue(row.getWeightValue()))
                .completed(event != null)
                .includedInCurrentPercentage(includedInCurrentPercentage)
                .sourceEventId(event == null ? null : event.getEventId())
                .eventOccurredAt(event == null ? null : event.getOccurredAt())
                .build();
    }

    private Map<String, AdminUserProgressReadMapper.ProgressEventRow> indexCompletionEvents(
            List<AdminUserProgressReadMapper.ProgressEventRow> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }
        LinkedHashMap<String, AdminUserProgressReadMapper.ProgressEventRow> indexed = new LinkedHashMap<>();
        events.stream()
                .filter(Objects::nonNull)
                .sorted((left, right) -> {
                    LocalDateTime leftTime = left.getOccurredAt() == null ? LocalDateTime.MIN : left.getOccurredAt();
                    LocalDateTime rightTime = right.getOccurredAt() == null ? LocalDateTime.MIN : right.getOccurredAt();
                    int timeCompare = leftTime.compareTo(rightTime);
                    if (timeCompare != 0) {
                        return timeCompare;
                    }
                    long leftId = left.getEventId() == null ? Long.MAX_VALUE : left.getEventId();
                    long rightId = right.getEventId() == null ? Long.MAX_VALUE : right.getEventId();
                    return Long.compare(leftId, rightId);
                })
                .forEach(event -> {
                    String idKey = eventIdentityKey(event.getElementId(), null);
                    if (idKey != null) {
                        indexed.putIfAbsent(idKey, event);
                    }
                    String codeKey = eventIdentityKey(null, event.getElementCode());
                    if (codeKey != null) {
                        indexed.putIfAbsent(codeKey, event);
                    }
                });
        return indexed;
    }

    private AdminUserProgressReadMapper.ProgressEventRow completionEventFor(
            AdminUserProgressReadMapper.ProgressElementRow row,
            Map<String, AdminUserProgressReadMapper.ProgressEventRow> completionEventsByKey) {
        AdminUserProgressReadMapper.ProgressEventRow byId = completionEventsByKey.get(eventIdentityKey(row.getElementId(), null));
        if (byId != null) {
            return byId;
        }
        return completionEventsByKey.get(eventIdentityKey(null, row.getElementCode()));
    }

    private boolean matchesScope(AdminUserProgressReadMapper.ProgressElementRow row, String scopeType, Long scopeId) {
        if (scopeId == null || SCOPE_GLOBAL.equals(scopeType)) {
            return true;
        }
        return switch (scopeType) {
            case "city" -> Objects.equals(row.getCityId(), scopeId);
            case "sub_map" -> Objects.equals(row.getSubMapId(), scopeId);
            case "poi" -> Objects.equals(row.getPoiId(), scopeId);
            case "indoor_building" -> Objects.equals(row.getIndoorBuildingId(), scopeId);
            case "indoor_floor" -> Objects.equals(row.getIndoorFloorId(), scopeId);
            case "story_chapter" -> Objects.equals(row.getStoryChapterId(), scopeId);
            case "task" -> matchesOwnerScope(row, OWNER_TYPE_EXPERIENCE_FLOW_STEP, scopeId);
            case "collectible" -> matchesOwnerScope(row, OWNER_TYPE_COLLECTIBLE, scopeId);
            case "reward" -> matchesOwnerScope(row, OWNER_TYPE_REWARD, scopeId);
            case "media" -> matchesOwnerScope(row, OWNER_TYPE_CONTENT_ASSET, scopeId);
            default -> false;
        };
    }

    private boolean matchesOwnerScope(AdminUserProgressReadMapper.ProgressElementRow row, String ownerType, Long scopeId) {
        return ownerType.equals(normalizeToken(row.getOwnerType())) && Objects.equals(row.getOwnerId(), scopeId);
    }

    private boolean isActiveElement(AdminUserProgressReadMapper.ProgressElementRow row) {
        return STATUS_PUBLISHED.equalsIgnoreCase(defaultText(row.getStatus(), ""))
                && Boolean.TRUE.equals(row.getIncludeInExploration());
    }

    private int resolveWeightValue(Integer weightValue) {
        return weightValue == null ? 0 : weightValue;
    }

    private String resolveTitle(AdminUserProgressReadMapper.ProgressElementRow row) {
        if (StringUtils.hasText(row.getTitleZht())) {
            return row.getTitleZht().trim();
        }
        if (StringUtils.hasText(row.getTitleZh())) {
            return row.getTitleZh().trim();
        }
        if (StringUtils.hasText(row.getTitleEn())) {
            return row.getTitleEn().trim();
        }
        if (StringUtils.hasText(row.getTitlePt())) {
            return row.getTitlePt().trim();
        }
        return defaultText(row.getElementCode(), "");
    }

    private Stream<String> streamElementKeys(AdminUserProgressReadMapper.ProgressElementRow row) {
        return Stream.of(
                eventIdentityKey(row.getElementId(), null),
                eventIdentityKey(null, row.getElementCode()),
                elementIdentityKey(row)
        ).filter(StringUtils::hasText);
    }

    private String elementIdentityKey(AdminUserProgressReadMapper.ProgressElementRow row) {
        if (row.getElementId() != null) {
            return eventIdentityKey(row.getElementId(), null);
        }
        return eventIdentityKey(null, row.getElementCode());
    }

    private String eventIdentityKey(Long elementId, String elementCode) {
        if (elementId != null) {
            return "id:" + elementId;
        }
        if (StringUtils.hasText(elementCode)) {
            return "code:" + elementCode.trim();
        }
        return null;
    }

    private String normalizeScopeType(String scopeType) {
        String normalized = normalizeToken(scopeType);
        return StringUtils.hasText(normalized) ? normalized : SCOPE_GLOBAL;
    }

    private String normalizeToken(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase() : "";
    }

    private String defaultText(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private record CalculationResult(
            String scopeType,
            int completedWeight,
            int availableWeight,
            int completedElementCount,
            int availableElementCount,
            int retiredCompletedWeight,
            int retiredCompletedCount,
            double progressPercent,
            LocalDateTime lastRecomputeTime,
            List<AdminUserProgressBreakdownResponse.ElementBreakdown> activeElements,
            List<AdminUserProgressBreakdownResponse.ElementBreakdown> retiredElements,
            boolean includeInactiveElements) {
    }
}
