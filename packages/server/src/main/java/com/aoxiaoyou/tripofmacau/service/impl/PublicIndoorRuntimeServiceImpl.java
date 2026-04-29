package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.enums.ContentStatus;
import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.request.IndoorRuntimeInteractionRequest;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorRuntimeFloorResponse;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorRuntimeInteractionResponse;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.aoxiaoyou.tripofmacau.entity.IndoorBuilding;
import com.aoxiaoyou.tripofmacau.entity.IndoorFloor;
import com.aoxiaoyou.tripofmacau.entity.IndoorNode;
import com.aoxiaoyou.tripofmacau.entity.IndoorNodeBehavior;
import com.aoxiaoyou.tripofmacau.entity.IndoorRuntimeLog;
import com.aoxiaoyou.tripofmacau.mapper.IndoorBuildingMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorFloorMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorNodeBehaviorMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorNodeMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorRuntimeLogMapper;
import com.aoxiaoyou.tripofmacau.service.CatalogFoundationService;
import com.aoxiaoyou.tripofmacau.service.PublicIndoorRuntimeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PublicIndoorRuntimeServiceImpl implements PublicIndoorRuntimeService {

    private static final Set<String> PUBLIC_RUNTIME_STATUSES = Set.of("published", "enabled", "active");
    private static final Set<String> SUPPORTED_APPEARANCE_CATEGORIES = Set.of("always_on", "manual", "schedule_window");
    private static final Set<String> SUPPORTED_TRIGGER_CATEGORIES = Set.of("tap", "proximity", "dwell");
    private static final Set<String> SUPPORTED_EFFECT_CATEGORIES = Set.of("popup", "bubble", "media", "path_motion");
    private static final Set<String> GUARDED_EFFECT_CATEGORIES = Set.of(
            "collectible_grant",
            "badge_grant",
            "task_update",
            "account_adjustment",
            "reward_grant",
            "stamp_grant"
    );
    private static final Set<String> BLOCKED_RUNTIME_SUPPORT_LEVELS = Set.of("phase15_storage_only", "phase16_planned", "future_only");
    private static final String RUNTIME_VERSION_PREFIX = "phase17";
    private static final DateTimeFormatter SCHEDULE_TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private final IndoorBuildingMapper indoorBuildingMapper;
    private final IndoorFloorMapper indoorFloorMapper;
    private final IndoorNodeMapper indoorNodeMapper;
    private final IndoorNodeBehaviorMapper indoorNodeBehaviorMapper;
    private final IndoorRuntimeLogMapper indoorRuntimeLogMapper;
    private final CatalogFoundationService catalogFoundationService;
    private final LocalizedContentSupport localizedContentSupport;
    private final ObjectMapper objectMapper;

    @Override
    public IndoorRuntimeFloorResponse getFloorRuntime(Long floorId, String localeHint) {
        IndoorFloor floor = requirePublishedFloor(floorId);
        IndoorBuilding building = requirePublishedBuilding(floor.getBuildingId());
        List<IndoorNode> nodes = loadPublishedNodes(floorId);
        Map<Long, List<IndoorNodeBehavior>> behaviorsByNode = loadPublishedBehaviors(nodes);
        Map<Long, ContentAsset> assets = loadNodeAssetMap(nodes);
        Map<Long, ContentAsset> floorAssets = catalogFoundationService.getPublishedAssetsByIds(Stream.of(
                        floor.getCoverAssetId(),
                        floor.getFloorPlanAssetId())
                .filter(Objects::nonNull)
                .toList());

        List<IndoorRuntimeFloorResponse.Node> runtimeNodes = nodes.stream()
                .map(node -> toRuntimeNode(node, behaviorsByNode.getOrDefault(node.getId(), Collections.emptyList()), assets, localeHint))
                .toList();

        int behaviorCount = behaviorsByNode.values().stream().mapToInt(List::size).sum();

        return IndoorRuntimeFloorResponse.builder()
                .floorId(floor.getId())
                .floorCode(floor.getFloorCode())
                .floorNumber(floor.getFloorNumber())
                .buildingId(building.getId())
                .buildingCode(building.getBuildingCode())
                .name(localizedContentSupport.resolveText(localeHint, floor.getFloorNameZh(), floor.getFloorNameEn(), floor.getFloorNameZht(), floor.getFloorNamePt()))
                .description(localizedContentSupport.resolveText(localeHint, floor.getDescriptionZh(), floor.getDescriptionEn(), floor.getDescriptionZht(), floor.getDescriptionPt()))
                .coverImageUrl(resolveAssetUrl(floorAssets, floor.getCoverAssetId(), null))
                .floorPlanUrl(resolveAssetUrl(floorAssets, floor.getFloorPlanAssetId(), floor.getFloorPlanUrl()))
                .tileSourceType(floor.getTileSourceType())
                .tilePreviewImageUrl(floor.getTilePreviewImageUrl())
                .tileRootUrl(floor.getTileRootUrl())
                .tileManifestJson(floor.getTileManifestJson())
                .tileZoomDerivationJson(floor.getTileZoomDerivationJson())
                .imageWidthPx(floor.getImageWidthPx())
                .imageHeightPx(floor.getImageHeightPx())
                .tileSizePx(floor.getTileSizePx())
                .gridCols(floor.getGridCols())
                .gridRows(floor.getGridRows())
                .tileLevelCount(floor.getTileLevelCount())
                .tileEntryCount(floor.getTileEntryCount())
                .importStatus(floor.getImportStatus())
                .importNote(floor.getImportNote())
                .altitudeMeters(floor.getAltitudeMeters())
                .areaSqm(floor.getAreaSqm())
                .zoomMin(floor.getZoomMin())
                .zoomMax(floor.getZoomMax())
                .defaultZoom(floor.getDefaultZoom())
                .popupConfigJson(floor.getPopupConfigJson())
                .displayConfigJson(floor.getDisplayConfigJson())
                .runtimeVersion(buildRuntimeVersion(floor, nodes.size(), behaviorCount))
                .nodes(runtimeNodes)
                .build();
    }

    @Override
    public IndoorRuntimeInteractionResponse evaluateInteraction(
            IndoorRuntimeInteractionRequest request,
            String localeHint,
            Long userId
    ) {
        IndoorFloor floor = requirePublishedFloor(request.getFloorId());
        IndoorNode node = requirePublishedNode(request.getNodeId(), floor.getId());
        IndoorNodeBehavior behaviorEntity = requirePublishedBehavior(request.getBehaviorId(), node.getId());
        Map<Long, ContentAsset> assets = loadNodeAssetMap(List.of(node));
        IndoorRuntimeFloorResponse.Behavior behavior = toRuntimeBehavior(node, behaviorEntity, localeHint, assets);
        IndoorRuntimeFloorResponse.TriggerRule matchedTrigger = matchTrigger(behavior, request.getEventType(), request.getTriggerId());

        if (matchedTrigger == null) {
            return buildBlockedResponse(request, userId, behavior, "trigger_not_matched", false, Collections.emptyList(), null);
        }

        boolean visible = isBehaviorVisible(behavior.getAppearanceRules(), LocalDateTime.now());
        if (!visible) {
            return buildBlockedResponse(request, userId, behavior, "behavior_not_visible", false, Collections.emptyList(), matchedTrigger.getId());
        }

        if (Boolean.TRUE.equals(behavior.getRequiresAuth()) && userId == null) {
            return buildBlockedResponse(
                    request,
                    null,
                    behavior,
                    "auth_required",
                    true,
                    buildEffectCategoryList(behavior.getEffectRules()),
                    matchedTrigger.getId()
            );
        }

        if (!Boolean.TRUE.equals(behavior.getSupported())) {
            return buildBlockedResponse(
                    request,
                    userId,
                    behavior,
                    firstNonBlank(behavior.getBlockedReason(), "unsupported_behavior"),
                    Boolean.TRUE.equals(behavior.getRequiresAuth()),
                    buildEffectCategoryList(behavior.getEffectRules()),
                    matchedTrigger.getId()
            );
        }

        List<IndoorRuntimeInteractionResponse.TriggeredEffect> effects = buildTriggeredEffects(behavior);
        Long interactionLogId = insertRuntimeLog(
                request,
                userId,
                true,
                matchedTrigger.getId(),
                Boolean.TRUE.equals(behavior.getRequiresAuth()),
                null,
                buildEffectCategoryList(behavior.getEffectRules())
        );

        return IndoorRuntimeInteractionResponse.builder()
                .interactionAccepted(true)
                .visible(true)
                .matchedTriggerId(matchedTrigger.getId())
                .blockedReason(null)
                .requiresAuth(Boolean.TRUE.equals(behavior.getRequiresAuth()))
                .effects(effects)
                .interactionLogId(interactionLogId)
                .cooldownUntil(null)
                .build();
    }

    private IndoorRuntimeInteractionResponse buildBlockedResponse(
            IndoorRuntimeInteractionRequest request,
            Long userId,
            IndoorRuntimeFloorResponse.Behavior behavior,
            String blockedReason,
            boolean requiresAuth,
            List<String> effectCategories,
            String matchedTriggerId
    ) {
        Long interactionLogId = insertRuntimeLog(
                request,
                userId,
                false,
                matchedTriggerId,
                requiresAuth,
                blockedReason,
                effectCategories
        );
        return IndoorRuntimeInteractionResponse.builder()
                .interactionAccepted(false)
                .visible(isBehaviorVisible(behavior.getAppearanceRules(), LocalDateTime.now()))
                .matchedTriggerId(matchedTriggerId)
                .blockedReason(blockedReason)
                .requiresAuth(requiresAuth)
                .effects(Collections.emptyList())
                .interactionLogId(interactionLogId)
                .cooldownUntil(null)
                .build();
    }

    private Long insertRuntimeLog(
            IndoorRuntimeInteractionRequest request,
            Long userId,
            boolean interactionAccepted,
            String matchedTriggerId,
            boolean requiresAuth,
            String blockedReason,
            List<String> effectCategories
    ) {
        IndoorRuntimeLog log = new IndoorRuntimeLog();
        log.setFloorId(request.getFloorId());
        log.setNodeId(request.getNodeId());
        log.setBehaviorId(request.getBehaviorId());
        log.setTriggerId(request.getTriggerId());
        log.setEventType(normalizeCategory(request.getEventType()));
        log.setEventTimestamp(parseEventTimestamp(request.getEventTimestamp()));
        log.setRelativeX(request.getRelativeX());
        log.setRelativeY(request.getRelativeY());
        log.setDwellMs(request.getDwellMs());
        log.setUserId(userId);
        log.setClientSessionId(StringUtils.hasText(request.getClientSessionId()) ? request.getClientSessionId().trim() : null);
        log.setInteractionAccepted(interactionAccepted);
        log.setMatchedTriggerId(matchedTriggerId);
        log.setRequiresAuth(requiresAuth);
        log.setBlockedReason(blockedReason);
        log.setEffectCategoriesJson(writeJsonOrNull(effectCategories));
        indoorRuntimeLogMapper.insert(log);
        return log.getId();
    }

    private IndoorRuntimeFloorResponse.Node toRuntimeNode(
            IndoorNode node,
            List<IndoorNodeBehavior> behaviors,
            Map<Long, ContentAsset> assets,
            String localeHint
    ) {
        return IndoorRuntimeFloorResponse.Node.builder()
                .nodeId(node.getId())
                .markerCode(node.getMarkerCode())
                .nodeType(node.getNodeType())
                .presentationMode(defaultIfBlank(node.getPresentationMode(), "marker"))
                .overlayType(node.getOverlayType())
                .name(localizedContentSupport.resolveText(localeHint, node.getNodeNameZh(), node.getNodeNameEn(), node.getNodeNameZht(), node.getNodeNamePt()))
                .description(localizedContentSupport.resolveText(localeHint, node.getDescriptionZh(), node.getDescriptionEn(), node.getDescriptionZht(), node.getDescriptionPt()))
                .relativeX(node.getRelativeX())
                .relativeY(node.getRelativeY())
                .relatedPoiId(node.getRelatedPoiId())
                .iconUrl(resolveAssetUrl(assets, node.getIconAssetId(), node.getIcon()))
                .animationUrl(resolveAssetUrl(assets, node.getAnimationAssetId(), null))
                .linkedEntityType(node.getLinkedEntityType())
                .linkedEntityId(node.getLinkedEntityId())
                .popupConfigJson(node.getPopupConfigJson())
                .displayConfigJson(node.getDisplayConfigJson())
                .sortOrder(node.getSortOrder())
                .status(node.getStatus())
                .runtimeSupportLevel(node.getRuntimeSupportLevel())
                .overlayGeometry(parseOverlayGeometry(node.getOverlayGeometryJson()))
                .behaviors(behaviors.stream()
                        .map(behavior -> toRuntimeBehavior(node, behavior, localeHint, assets))
                        .toList())
                .build();
    }

    private IndoorRuntimeFloorResponse.Behavior toRuntimeBehavior(
            IndoorNode node,
            IndoorNodeBehavior behavior,
            String localeHint,
            Map<Long, ContentAsset> assets
    ) {
        List<IndoorRuntimeFloorResponse.RuleCondition> appearanceRules = parseRuleConditions(behavior.getAppearanceRulesJson());
        List<IndoorRuntimeFloorResponse.TriggerRule> triggerRules = parseTriggerRules(behavior.getTriggerRulesJson());
        List<IndoorRuntimeFloorResponse.EffectRule> effectRules = parseEffectRules(behavior.getEffectRulesJson());
        boolean runtimeLevelSupported = isRuntimeSupportLevelSupported(firstNonBlank(behavior.getRuntimeSupportLevel(), node.getRuntimeSupportLevel()));

        String blockedReason = firstNonBlank(
                runtimeLevelSupported ? null : "runtime_support_level_unsupported",
                findUnsupportedRuleConditionCategory(appearanceRules, SUPPORTED_APPEARANCE_CATEGORIES, "unsupported_appearance_category"),
                findUnsupportedTriggerCategory(triggerRules, SUPPORTED_TRIGGER_CATEGORIES, "unsupported_trigger_category"),
                findUnsupportedEffectCategory(effectRules, SUPPORTED_EFFECT_CATEGORIES, "unsupported_effect_category")
        );

        return IndoorRuntimeFloorResponse.Behavior.builder()
                .behaviorId(behavior.getId())
                .behaviorCode(behavior.getBehaviorCode())
                .name(localizedContentSupport.resolveText(localeHint, behavior.getBehaviorNameZh(), behavior.getBehaviorNameEn(), behavior.getBehaviorNameZht(), behavior.getBehaviorNamePt()))
                .status(behavior.getStatus())
                .sortOrder(behavior.getSortOrder())
                .runtimeSupportLevel(behavior.getRuntimeSupportLevel())
                .supported(blockedReason == null)
                .requiresAuth(containsGuardedEffect(effectRules))
                .blockedReason(blockedReason)
                .appearanceRules(appearanceRules)
                .triggerRules(triggerRules)
                .effectRules(effectRules)
                .pathGraph(parsePathGraph(behavior.getPathGraphJson()))
                .overlayGeometry(firstNonNull(
                        parseOverlayGeometry(behavior.getOverlayGeometryJson()),
                        parseOverlayGeometry(node.getOverlayGeometryJson())
                ))
                .build();
    }

    private IndoorRuntimeFloorResponse.TriggerRule matchTrigger(
            IndoorRuntimeFloorResponse.Behavior behavior,
            String eventType,
            String triggerId
    ) {
        String normalizedEventType = normalizeCategory(eventType);
        if (StringUtils.hasText(triggerId)) {
            return behavior.getTriggerRules().stream()
                    .filter(Objects::nonNull)
                    .filter(item -> triggerId.trim().equals(item.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return behavior.getTriggerRules().stream()
                .filter(Objects::nonNull)
                .filter(item -> normalizedEventType.equals(normalizeCategory(item.getCategory())))
                .findFirst()
                .orElse(null);
    }

    private boolean isBehaviorVisible(List<IndoorRuntimeFloorResponse.RuleCondition> appearanceRules, LocalDateTime now) {
        if (appearanceRules == null || appearanceRules.isEmpty()) {
            return true;
        }

        boolean evaluatedAppearance = false;
        for (IndoorRuntimeFloorResponse.RuleCondition rule : appearanceRules) {
            if (rule == null || !StringUtils.hasText(rule.getCategory())) {
                continue;
            }
            String category = normalizeCategory(rule.getCategory());
            if ("always_on".equals(category) || "manual".equals(category)) {
                return true;
            }
            if ("schedule_window".equals(category)) {
                evaluatedAppearance = true;
                if (isScheduleWindowActive(rule.getConfig(), now)) {
                    return true;
                }
            }
        }
        return !evaluatedAppearance;
    }

    private boolean isScheduleWindowActive(JsonNode config, LocalDateTime now) {
        if (config == null || config.isNull()) {
            return true;
        }
        if (!matchesWeekday(config, now.getDayOfWeek())) {
            return false;
        }
        LocalTime start = parseScheduleTime(config.path("startAt").asText(null), config.path("startTime").asText(null));
        LocalTime end = parseScheduleTime(config.path("endAt").asText(null), config.path("endTime").asText(null));
        if (start == null || end == null) {
            return true;
        }
        LocalTime current = now.toLocalTime();
        if (!end.isBefore(start)) {
            return !current.isBefore(start) && !current.isAfter(end);
        }
        return !current.isBefore(start) || !current.isAfter(end);
    }

    private boolean matchesWeekday(JsonNode config, DayOfWeek dayOfWeek) {
        JsonNode weekdays = firstNonNull(config.get("weekdays"), config.get("daysOfWeek"));
        if (weekdays == null || !weekdays.isArray() || weekdays.isEmpty()) {
            return true;
        }
        String current = dayOfWeek.name().toLowerCase(Locale.ROOT);
        for (JsonNode item : weekdays) {
            if (item == null || item.isNull()) {
                continue;
            }
            String value = item.asText("").trim().toLowerCase(Locale.ROOT);
            if (!value.isEmpty() && (value.equals(current) || value.equals(current.substring(0, 3)))) {
                return true;
            }
        }
        return false;
    }

    private LocalTime parseScheduleTime(String... candidates) {
        for (String candidate : candidates) {
            if (!StringUtils.hasText(candidate)) {
                continue;
            }
            try {
                return LocalTime.parse(candidate.trim(), SCHEDULE_TIME_FORMATTER);
            } catch (DateTimeParseException ignored) {
            }
            try {
                return LocalTime.parse(candidate.trim());
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private List<IndoorRuntimeInteractionResponse.TriggeredEffect> buildTriggeredEffects(IndoorRuntimeFloorResponse.Behavior behavior) {
        if (behavior.getEffectRules() == null || behavior.getEffectRules().isEmpty()) {
            return Collections.emptyList();
        }
        List<IndoorRuntimeInteractionResponse.TriggeredEffect> effects = new ArrayList<>();
        for (IndoorRuntimeFloorResponse.EffectRule effectRule : behavior.getEffectRules()) {
            if (effectRule == null || !SUPPORTED_EFFECT_CATEGORIES.contains(normalizeCategory(effectRule.getCategory()))) {
                continue;
            }
            effects.add(IndoorRuntimeInteractionResponse.TriggeredEffect.builder()
                    .effectId(effectRule.getId())
                    .category(normalizeCategory(effectRule.getCategory()))
                    .label(effectRule.getLabel())
                    .config(effectRule.getConfig())
                    .pathGraph("path_motion".equals(normalizeCategory(effectRule.getCategory())) ? behavior.getPathGraph() : null)
                    .overlayGeometry(behavior.getOverlayGeometry())
                    .build());
        }
        return effects;
    }

    private List<String> buildEffectCategoryList(List<IndoorRuntimeFloorResponse.EffectRule> effectRules) {
        if (effectRules == null || effectRules.isEmpty()) {
            return Collections.emptyList();
        }
        return effectRules.stream()
                .filter(Objects::nonNull)
                .map(IndoorRuntimeFloorResponse.EffectRule::getCategory)
                .filter(StringUtils::hasText)
                .map(this::normalizeCategory)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
    }

    private boolean containsGuardedEffect(List<IndoorRuntimeFloorResponse.EffectRule> effectRules) {
        if (effectRules == null || effectRules.isEmpty()) {
            return false;
        }
        return effectRules.stream()
                .filter(Objects::nonNull)
                .map(IndoorRuntimeFloorResponse.EffectRule::getCategory)
                .filter(StringUtils::hasText)
                .map(this::normalizeCategory)
                .anyMatch(GUARDED_EFFECT_CATEGORIES::contains);
    }

    private String findUnsupportedRuleConditionCategory(
            Collection<IndoorRuntimeFloorResponse.RuleCondition> rules,
            Set<String> supportedCategories,
            String blockedReason
    ) {
        return findUnsupportedCategory(
                rules == null ? Stream.<String>empty() : rules.stream().map(IndoorRuntimeFloorResponse.RuleCondition::getCategory),
                supportedCategories,
                blockedReason
        );
    }

    private String findUnsupportedTriggerCategory(
            Collection<IndoorRuntimeFloorResponse.TriggerRule> rules,
            Set<String> supportedCategories,
            String blockedReason
    ) {
        return findUnsupportedCategory(
                rules == null ? Stream.<String>empty() : rules.stream().map(IndoorRuntimeFloorResponse.TriggerRule::getCategory),
                supportedCategories,
                blockedReason
        );
    }

    private String findUnsupportedEffectCategory(
            Collection<IndoorRuntimeFloorResponse.EffectRule> rules,
            Set<String> supportedCategories,
            String blockedReason
    ) {
        return findUnsupportedCategory(
                rules == null ? Stream.<String>empty() : rules.stream().map(IndoorRuntimeFloorResponse.EffectRule::getCategory),
                supportedCategories,
                blockedReason
        );
    }

    private String findUnsupportedCategory(Stream<String> categories, Set<String> supportedCategories, String blockedReason) {
        return categories
                .filter(StringUtils::hasText)
                .map(this::normalizeCategory)
                .filter(category -> !supportedCategories.contains(category))
                .findFirst()
                .map(ignored -> blockedReason)
                .orElse(null);
    }

    private boolean isRuntimeSupportLevelSupported(String runtimeSupportLevel) {
        String normalized = normalizeCategory(runtimeSupportLevel);
        return !normalized.isEmpty() && !BLOCKED_RUNTIME_SUPPORT_LEVELS.contains(normalized);
    }

    private Map<Long, List<IndoorNodeBehavior>> loadPublishedBehaviors(List<IndoorNode> nodes) {
        List<Long> nodeIds = nodes.stream().map(IndoorNode::getId).filter(Objects::nonNull).toList();
        if (nodeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return indoorNodeBehaviorMapper.selectList(new LambdaQueryWrapper<IndoorNodeBehavior>()
                        .in(IndoorNodeBehavior::getNodeId, nodeIds)
                        .in(IndoorNodeBehavior::getStatus, PUBLIC_RUNTIME_STATUSES)
                        .orderByAsc(IndoorNodeBehavior::getSortOrder)
                        .orderByAsc(IndoorNodeBehavior::getId))
                .stream()
                .collect(Collectors.groupingBy(
                        IndoorNodeBehavior::getNodeId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private List<IndoorNode> loadPublishedNodes(Long floorId) {
        return indoorNodeMapper.selectList(new LambdaQueryWrapper<IndoorNode>()
                .eq(IndoorNode::getFloorId, floorId)
                .in(IndoorNode::getStatus, PUBLIC_RUNTIME_STATUSES)
                .orderByAsc(IndoorNode::getSortOrder)
                .orderByAsc(IndoorNode::getId));
    }

    private Map<Long, ContentAsset> loadNodeAssetMap(List<IndoorNode> nodes) {
        return catalogFoundationService.getPublishedAssetsByIds(nodes.stream()
                .flatMap(node -> Stream.of(node.getIconAssetId(), node.getAnimationAssetId()))
                .filter(Objects::nonNull)
                .toList());
    }

    private IndoorFloor requirePublishedFloor(Long floorId) {
        IndoorFloor floor = indoorFloorMapper.selectOne(new LambdaQueryWrapper<IndoorFloor>()
                .eq(IndoorFloor::getId, floorId)
                .eq(IndoorFloor::getStatus, ContentStatus.PUBLISHED.getCode())
                .last("LIMIT 1"));
        if (floor == null) {
            throw new BusinessException(4046, "Indoor floor not found");
        }
        return floor;
    }

    private IndoorBuilding requirePublishedBuilding(Long buildingId) {
        IndoorBuilding building = indoorBuildingMapper.selectOne(new LambdaQueryWrapper<IndoorBuilding>()
                .eq(IndoorBuilding::getId, buildingId)
                .eq(IndoorBuilding::getStatus, ContentStatus.PUBLISHED.getCode())
                .last("LIMIT 1"));
        if (building == null) {
            throw new BusinessException(4044, "Indoor building not found");
        }
        return building;
    }

    private IndoorNode requirePublishedNode(Long nodeId, Long floorId) {
        IndoorNode node = indoorNodeMapper.selectOne(new LambdaQueryWrapper<IndoorNode>()
                .eq(IndoorNode::getId, nodeId)
                .eq(IndoorNode::getFloorId, floorId)
                .in(IndoorNode::getStatus, PUBLIC_RUNTIME_STATUSES)
                .last("LIMIT 1"));
        if (node == null) {
            throw new BusinessException(4047, "Indoor runtime node not found");
        }
        return node;
    }

    private IndoorNodeBehavior requirePublishedBehavior(Long behaviorId, Long nodeId) {
        IndoorNodeBehavior behavior = indoorNodeBehaviorMapper.selectOne(new LambdaQueryWrapper<IndoorNodeBehavior>()
                .eq(IndoorNodeBehavior::getId, behaviorId)
                .eq(IndoorNodeBehavior::getNodeId, nodeId)
                .in(IndoorNodeBehavior::getStatus, PUBLIC_RUNTIME_STATUSES)
                .last("LIMIT 1"));
        if (behavior == null) {
            throw new BusinessException(4048, "Indoor runtime behavior not found");
        }
        return behavior;
    }

    private List<IndoorRuntimeFloorResponse.RuleCondition> parseRuleConditions(String json) {
        return readJson(json, new TypeReference<List<IndoorRuntimeFloorResponse.RuleCondition>>() {
        }, Collections.emptyList());
    }

    private List<IndoorRuntimeFloorResponse.TriggerRule> parseTriggerRules(String json) {
        return readJson(json, new TypeReference<List<IndoorRuntimeFloorResponse.TriggerRule>>() {
        }, Collections.emptyList());
    }

    private List<IndoorRuntimeFloorResponse.EffectRule> parseEffectRules(String json) {
        return readJson(json, new TypeReference<List<IndoorRuntimeFloorResponse.EffectRule>>() {
        }, Collections.emptyList());
    }

    private IndoorRuntimeFloorResponse.PathGraph parsePathGraph(String json) {
        return readJson(json, new TypeReference<IndoorRuntimeFloorResponse.PathGraph>() {
        }, null);
    }

    private IndoorRuntimeFloorResponse.OverlayGeometry parseOverlayGeometry(String json) {
        return readJson(json, new TypeReference<IndoorRuntimeFloorResponse.OverlayGeometry>() {
        }, null);
    }

    private <T> T readJson(String json, TypeReference<T> typeReference, T fallback) {
        if (!StringUtils.hasText(json)) {
            return fallback;
        }
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String writeJsonOrNull(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private String resolveAssetUrl(Map<Long, ContentAsset> assets, Long assetId, String fallback) {
        String assetUrl = localizedContentSupport.resolveAssetUrl(assets, assetId);
        return StringUtils.hasText(assetUrl) ? assetUrl : fallback;
    }

    private LocalDateTime parseEventTimestamp(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private String buildRuntimeVersion(IndoorFloor floor, int nodeCount, int behaviorCount) {
        String updatedMarker = floor.getUpdatedAt() == null ? "na" : floor.getUpdatedAt().toString();
        return RUNTIME_VERSION_PREFIX + "-" + floor.getId() + "-" + nodeCount + "-" + behaviorCount + "-" + updatedMarker;
    }

    private String normalizeCategory(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : "";
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private <T> T firstNonNull(T first, T second) {
        return first != null ? first : second;
    }
}
