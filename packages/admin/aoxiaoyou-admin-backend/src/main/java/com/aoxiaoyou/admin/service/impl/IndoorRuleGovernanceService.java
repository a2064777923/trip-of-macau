package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminIndoorNodeBehaviorPayload;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleConflictResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleGovernanceDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleGovernanceItemResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardLinkedEntityResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardRuleLinkResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleStatusUpdateResponse;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.entity.GameReward;
import com.aoxiaoyou.admin.entity.IndoorFloor;
import com.aoxiaoyou.admin.entity.IndoorNode;
import com.aoxiaoyou.admin.entity.IndoorNodeBehavior;
import com.aoxiaoyou.admin.entity.RedeemablePrize;
import com.aoxiaoyou.admin.entity.RewardRule;
import com.aoxiaoyou.admin.entity.RewardRuleBinding;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.GameRewardMapper;
import com.aoxiaoyou.admin.mapper.IndoorFloorMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeBehaviorMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeMapper;
import com.aoxiaoyou.admin.mapper.RedeemablePrizeMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleBindingMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndoorRuleGovernanceService {

    public static final String CONFLICT_MISSING_PREREQUISITE = "MISSING_PREREQUISITE";
    public static final String CONFLICT_SCHEDULE_OVERLAP = "SCHEDULE_OVERLAP";
    public static final String CONFLICT_ENTITY_COLLISION = "ENTITY_COLLISION";
    public static final String CONFLICT_STATUS_MISMATCH = "STATUS_MISMATCH";
    public static final String STORAGE_ONLY_RUNTIME = "phase15_storage_only";
    private static final String OWNER_TYPE_INDOOR_BEHAVIOR = "indoor_behavior";
    private static final String OWNER_TYPE_REDEEMABLE_PRIZE = "redeemable_prize";
    private static final String OWNER_TYPE_GAME_REWARD = "game_reward";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final Set<String> ENABLED_STATUSES = Set.of("enabled", "published", "active");
    private static final Set<String> MUTABLE_STATUSES = Set.of("draft", "enabled", "disabled", "published");

    private final IndoorNodeBehaviorMapper behaviorMapper;
    private final IndoorNodeMapper nodeMapper;
    private final IndoorFloorMapper floorMapper;
    private final BuildingMapper buildingMapper;
    private final RewardRuleBindingMapper rewardRuleBindingMapper;
    private final RewardRuleMapper rewardRuleMapper;
    private final RedeemablePrizeMapper redeemablePrizeMapper;
    private final GameRewardMapper gameRewardMapper;
    private final ObjectMapper objectMapper;

    public List<AdminIndoorRuleGovernanceItemResponse> listOverview(
            String keyword, Long cityId, Long buildingId, Long floorId, Long relatedPoiId,
            String linkedEntityType, Long linkedEntityId, String status, String runtimeSupportLevel,
            Boolean conflictOnly, Boolean enabledOnly
    ) {
        GovernanceContext context = buildContext(new GovernanceFilters(
                keyword, cityId, buildingId, floorId, relatedPoiId,
                linkedEntityType, linkedEntityId, status, runtimeSupportLevel,
                truthy(conflictOnly), truthy(enabledOnly)
        ));
        return context.records().stream()
                .map(record -> toOverviewItem(
                        record,
                        context.conflictsByBehaviorId().getOrDefault(record.behavior().getId(), Collections.emptyList()),
                        context.rewardRuleBindingsByBehaviorId().getOrDefault(record.behavior().getId(), Collections.emptyList())
                ))
                .toList();
    }

    public List<AdminIndoorRuleConflictResponse> listConflicts(
            String keyword, Long cityId, Long buildingId, Long floorId, Long relatedPoiId,
            String linkedEntityType, Long linkedEntityId, String status, String runtimeSupportLevel,
            Boolean conflictOnly, Boolean enabledOnly
    ) {
        GovernanceContext context = buildContext(new GovernanceFilters(
                keyword, cityId, buildingId, floorId, relatedPoiId,
                linkedEntityType, linkedEntityId, status, runtimeSupportLevel,
                truthy(conflictOnly), truthy(enabledOnly)
        ));
        return context.records().stream()
                .flatMap(record -> context.conflictsByBehaviorId()
                        .getOrDefault(record.behavior().getId(), Collections.emptyList())
                        .stream())
                .distinct()
                .toList();
    }

    public AdminIndoorRuleGovernanceDetailResponse getBehaviorDetail(Long behaviorId) {
        GovernanceRecord record = loadRecord(behaviorId);
        GovernanceContext context = buildContext(new GovernanceFilters(
                null,
                record.building() == null ? null : record.building().getCityId(),
                record.node().getBuildingId(),
                record.node().getFloorId(),
                null,
                null,
                null,
                null,
                null,
                false,
                false
        ));
        GovernanceRecord scopedRecord = context.records().stream()
                .filter(item -> Objects.equals(item.behavior().getId(), behaviorId))
                .findFirst()
                .orElse(record);
        List<AdminIndoorRuleConflictResponse> conflicts = context.conflictsByBehaviorId()
                .getOrDefault(behaviorId, Collections.emptyList());
        List<RewardRuleBinding> rewardRuleBindings = context.rewardRuleBindingsByBehaviorId()
                .getOrDefault(behaviorId, Collections.emptyList());
        List<Long> rewardRuleIds = rewardRuleBindings.stream()
                .map(RewardRuleBinding::getRuleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        return AdminIndoorRuleGovernanceDetailResponse.builder()
                .nodeId(scopedRecord.node().getId())
                .behaviorId(scopedRecord.behavior().getId())
                .behaviorCode(scopedRecord.behavior().getBehaviorCode())
                .behaviorNameZh(scopedRecord.behavior().getBehaviorNameZh())
                .behaviorNameZht(scopedRecord.behavior().getBehaviorNameZht())
                .behaviorNameEn(scopedRecord.behavior().getBehaviorNameEn())
                .behaviorNamePt(scopedRecord.behavior().getBehaviorNamePt())
                .markerCode(scopedRecord.node().getMarkerCode())
                .presentationMode(scopedRecord.node().getPresentationMode())
                .overlayType(scopedRecord.node().getOverlayType())
                .buildingId(scopedRecord.node().getBuildingId())
                .buildingNameZht(buildingName(scopedRecord.building()))
                .floorId(scopedRecord.node().getFloorId())
                .floorCode(scopedRecord.floor() == null ? null : scopedRecord.floor().getFloorCode())
                .linkedEntityType(scopedRecord.node().getLinkedEntityType())
                .linkedEntityId(scopedRecord.node().getLinkedEntityId())
                .runtimeSupportLevel(scopedRecord.runtimeSupportLevel())
                .status(scopedRecord.behavior().getStatus())
                .appearanceRuleCount(scopedRecord.appearanceRules().size())
                .triggerRuleCount(scopedRecord.triggerRules().size())
                .effectRuleCount(scopedRecord.effectRules().size())
                .hasPathGraph(hasPathGraph(scopedRecord.pathGraph()))
                .conflictCount(conflicts.size())
                .appearanceRules(scopedRecord.appearanceRules())
                .triggerRules(scopedRecord.triggerRules())
                .effectRules(scopedRecord.effectRules())
                .pathGraph(scopedRecord.pathGraph())
                .linkedRewardRuleIds(rewardRuleIds)
                .linkedRewardRules(toRewardRuleLinks(rewardRuleIds))
                .linkedRewards(loadLinkedRewardOwners(rewardRuleIds))
                .conflicts(conflicts)
                .parentNode(AdminIndoorRuleGovernanceDetailResponse.ParentNodeSummary.builder()
                        .nodeId(scopedRecord.node().getId())
                        .markerCode(scopedRecord.node().getMarkerCode())
                        .nodeNameZht(firstNonBlank(
                                scopedRecord.node().getNodeNameZht(),
                                scopedRecord.node().getNodeNameZh(),
                                scopedRecord.node().getMarkerCode()
                        ))
                        .nodeStatus(scopedRecord.node().getStatus())
                        .presentationMode(scopedRecord.node().getPresentationMode())
                        .overlayType(scopedRecord.node().getOverlayType())
                        .buildingId(scopedRecord.node().getBuildingId())
                        .buildingNameZht(buildingName(scopedRecord.building()))
                        .floorId(scopedRecord.node().getFloorId())
                        .floorCode(scopedRecord.floor() == null ? null : scopedRecord.floor().getFloorCode())
                        .relatedPoiId(scopedRecord.node().getRelatedPoiId())
                        .build())
                .build();
    }

    public AdminIndoorRuleStatusUpdateResponse updateBehaviorStatus(Long behaviorId, String requestedStatus) {
        IndoorNodeBehavior behavior = requireBehavior(behaviorId);
        IndoorNode node = requireNode(behavior.getNodeId());

        String normalizedStatus = normalizeStatus(requestedStatus);
        if (!StringUtils.hasText(normalizedStatus) || !MUTABLE_STATUSES.contains(normalizedStatus)) {
            throw new BusinessException(4001, "Indoor behavior status is invalid");
        }

        String runtimeSupportLevel = resolveRuntimeSupportLevel(behavior, node);
        List<String> warnings = buildStatusWarnings(node, runtimeSupportLevel, normalizedStatus);
        if (warnings.isEmpty()) {
            behavior.setStatus(normalizedStatus);
            behaviorMapper.updateById(behavior);
        }

        return AdminIndoorRuleStatusUpdateResponse.builder()
                .behaviorId(behaviorId)
                .status(warnings.isEmpty() ? normalizedStatus : behavior.getStatus())
                .parentNodeStatus(node.getStatus())
                .warnings(warnings)
                .build();
    }

    private GovernanceContext buildContext(GovernanceFilters filters) {
        List<IndoorNodeBehavior> behaviors = behaviorMapper.selectList(new LambdaQueryWrapper<IndoorNodeBehavior>()
                .orderByAsc(IndoorNodeBehavior::getSortOrder)
                .orderByAsc(IndoorNodeBehavior::getId));
        if (behaviors.isEmpty()) {
            return new GovernanceContext(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap());
        }

        Map<Long, IndoorNode> nodeMap = loadNodes(behaviors.stream().map(IndoorNodeBehavior::getNodeId).toList());
        Map<Long, IndoorFloor> floorMap = loadFloors(nodeMap.values().stream().map(IndoorNode::getFloorId).toList());
        Map<Long, Building> buildingMap = loadBuildings(nodeMap.values().stream().map(IndoorNode::getBuildingId).toList());

        List<GovernanceRecord> records = behaviors.stream()
                .map(behavior -> toRecord(behavior, nodeMap.get(behavior.getNodeId()), floorMap, buildingMap))
                .filter(Objects::nonNull)
                .filter(record -> matchesFilters(record, filters))
                .toList();

        Map<Long, List<AdminIndoorRuleConflictResponse>> conflictsByBehaviorId = computeConflicts(records);
        Map<Long, List<RewardRuleBinding>> rewardRuleBindingsByBehaviorId = loadRewardRuleBindings(
                records.stream().map(record -> record.behavior().getId()).toList()
        );
        if (filters.conflictOnly()) {
            records = records.stream()
                    .filter(record -> !conflictsByBehaviorId
                            .getOrDefault(record.behavior().getId(), Collections.emptyList())
                            .isEmpty())
                    .toList();
        }
        return new GovernanceContext(records, conflictsByBehaviorId, rewardRuleBindingsByBehaviorId);
    }

    private GovernanceRecord loadRecord(Long behaviorId) {
        IndoorNodeBehavior behavior = requireBehavior(behaviorId);
        IndoorNode node = requireNode(behavior.getNodeId());
        IndoorFloor floor = requireFloor(node.getFloorId());
        Building building = requireBuilding(node.getBuildingId());
        return toRecord(behavior, node, Map.of(floor.getId(), floor), Map.of(building.getId(), building));
    }

    private GovernanceRecord toRecord(
            IndoorNodeBehavior behavior,
            IndoorNode node,
            Map<Long, IndoorFloor> floorMap,
            Map<Long, Building> buildingMap
    ) {
        if (behavior == null || node == null) {
            return null;
        }
        return new GovernanceRecord(
                behavior,
                node,
                floorMap.get(node.getFloorId()),
                buildingMap.get(node.getBuildingId()),
                readJsonList(behavior.getAppearanceRulesJson(), new TypeReference<List<AdminIndoorNodeBehaviorPayload.RuleCondition>>() {}),
                readJsonList(behavior.getTriggerRulesJson(), new TypeReference<List<AdminIndoorNodeBehaviorPayload.TriggerStep>>() {}),
                readJsonList(behavior.getEffectRulesJson(), new TypeReference<List<AdminIndoorNodeBehaviorPayload.EffectDefinition>>() {}),
                readJsonValue(behavior.getPathGraphJson(), AdminIndoorNodeBehaviorPayload.PathGraph.class),
                resolveRuntimeSupportLevel(behavior, node)
        );
    }

    private boolean matchesFilters(GovernanceRecord record, GovernanceFilters filters) {
        if (filters.cityId() != null
                && (record.building() == null || !Objects.equals(record.building().getCityId(), filters.cityId()))) {
            return false;
        }
        if (filters.buildingId() != null && !Objects.equals(record.node().getBuildingId(), filters.buildingId())) {
            return false;
        }
        if (filters.floorId() != null && !Objects.equals(record.node().getFloorId(), filters.floorId())) {
            return false;
        }
        if (filters.relatedPoiId() != null && !Objects.equals(record.node().getRelatedPoiId(), filters.relatedPoiId())) {
            return false;
        }
        if (StringUtils.hasText(filters.linkedEntityType())
                && !filters.linkedEntityType().trim().equalsIgnoreCase(record.node().getLinkedEntityType())) {
            return false;
        }
        if (filters.linkedEntityId() != null && !Objects.equals(record.node().getLinkedEntityId(), filters.linkedEntityId())) {
            return false;
        }
        if (StringUtils.hasText(filters.status())
                && !filters.status().trim().equalsIgnoreCase(record.behavior().getStatus())) {
            return false;
        }
        if (StringUtils.hasText(filters.runtimeSupportLevel())
                && !filters.runtimeSupportLevel().trim().equalsIgnoreCase(record.runtimeSupportLevel())) {
            return false;
        }
        if (filters.enabledOnly() && !isEnabled(record.behavior().getStatus())) {
            return false;
        }
        if (!StringUtils.hasText(filters.keyword())) {
            return true;
        }

        String keyword = filters.keyword().trim().toLowerCase(Locale.ROOT);
        return contains(record.behavior().getBehaviorCode(), keyword)
                || contains(record.behavior().getBehaviorNameZh(), keyword)
                || contains(record.behavior().getBehaviorNameZht(), keyword)
                || contains(record.behavior().getBehaviorNameEn(), keyword)
                || contains(record.behavior().getBehaviorNamePt(), keyword)
                || contains(record.node().getMarkerCode(), keyword)
                || contains(record.floor() == null ? null : record.floor().getFloorCode(), keyword)
                || contains(buildingName(record.building()), keyword);
    }

    private Map<Long, List<AdminIndoorRuleConflictResponse>> computeConflicts(List<GovernanceRecord> records) {
        Map<Long, List<AdminIndoorRuleConflictResponse>> result = new LinkedHashMap<>();
        records.forEach(record -> result.put(record.behavior().getId(), new ArrayList<>()));

        for (GovernanceRecord record : records) {
            result.get(record.behavior().getId()).addAll(detectMissingPrerequisite(record));
            detectStatusMismatch(record).ifPresent(conflict -> result.get(record.behavior().getId()).add(conflict));
        }

        List<GovernanceRecord> enabledRecords = records.stream()
                .filter(record -> isEnabled(record.behavior().getStatus()))
                .toList();
        for (int i = 0; i < enabledRecords.size(); i++) {
            for (int j = i + 1; j < enabledRecords.size(); j++) {
                GovernanceRecord left = enabledRecords.get(i);
                GovernanceRecord right = enabledRecords.get(j);
                if (!Objects.equals(left.node().getFloorId(), right.node().getFloorId())) {
                    continue;
                }
                if (hasScheduleOverlap(left, right)) {
                    addPairConflict(
                            result,
                            left,
                            right,
                            CONFLICT_SCHEDULE_OVERLAP,
                            "warning",
                            "\u540c\u4e00\u6a13\u5c64\u5167\u6709\u4e92\u52d5\u898f\u5247\u7684\u6642\u9593\u689d\u4ef6\u91cd\u758a\uff0c\u9700\u8981\u518d\u78ba\u8a8d\u6642\u6bb5\u5b89\u6392\u3002"
                    );
                }
                if (hasEntityCollision(left, right)) {
                    addPairConflict(
                            result,
                            left,
                            right,
                            CONFLICT_ENTITY_COLLISION,
                            "warning",
                            "\u540c\u4e00\u6a13\u5c64\u5167\u6709\u591a\u689d\u898f\u5247\u7d81\u5b9a\u76f8\u540c\u5be6\u9ad4\u8207\u89f8\u767c\u985e\u578b\uff0c\u53ef\u80fd\u9020\u6210\u91cd\u8907\u89f8\u767c\u3002"
                    );
                }
            }
        }

        return result.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> dedupe(entry.getValue()),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    private List<AdminIndoorRuleConflictResponse> detectMissingPrerequisite(GovernanceRecord record) {
        if (record.triggerRules().isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, AdminIndoorNodeBehaviorPayload.TriggerStep> triggerMap = new LinkedHashMap<>();
        for (AdminIndoorNodeBehaviorPayload.TriggerStep step : record.triggerRules()) {
            if (step == null || !StringUtils.hasText(step.getId())) {
                continue;
            }
            triggerMap.put(step.getId().trim(), step);
        }

        List<AdminIndoorRuleConflictResponse> conflicts = new ArrayList<>();
        for (AdminIndoorNodeBehaviorPayload.TriggerStep step : record.triggerRules()) {
            if (step == null || !StringUtils.hasText(step.getDependsOnTriggerId())) {
                continue;
            }
            String dependencyId = step.getDependsOnTriggerId().trim();
            if (!triggerMap.containsKey(dependencyId)) {
                conflicts.add(buildConflict(
                        record,
                        null,
                        CONFLICT_MISSING_PREREQUISITE,
                        "error",
                        "\u89f8\u767c\u689d\u4ef6\u4f9d\u8cf4\u4e86\u4e0d\u5b58\u5728\u7684\u524d\u7f6e\u89f8\u767c\u6b65\u9a5f\uff1a" + dependencyId
                ));
                continue;
            }
            if (hasTriggerCycle(step, triggerMap)) {
                conflicts.add(buildConflict(
                        record,
                        null,
                        CONFLICT_MISSING_PREREQUISITE,
                        "error",
                        "\u89f8\u767c\u689d\u4ef6\u4e4b\u9593\u5b58\u5728\u5faa\u74b0\u6216\u4e0d\u53ef\u9054\u7684\u524d\u7f6e\u95dc\u4fc2\uff0c\u8acb\u91cd\u65b0\u68b3\u7406\u89f8\u767c\u93c8\u3002"
                ));
            }
        }
        return dedupe(conflicts);
    }

    private Optional<AdminIndoorRuleConflictResponse> detectStatusMismatch(GovernanceRecord record) {
        if (!isEnabled(record.behavior().getStatus())) {
            return Optional.empty();
        }
        List<String> warnings = buildStatusWarnings(record.node(), record.runtimeSupportLevel(), "enabled");
        if (warnings.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(buildConflict(
                record,
                null,
                CONFLICT_STATUS_MISMATCH,
                "error",
                String.join(" ", warnings)
        ));
    }

    private boolean hasScheduleOverlap(GovernanceRecord left, GovernanceRecord right) {
        List<ScheduleWindow> leftWindows = extractScheduleWindows(left.appearanceRules());
        List<ScheduleWindow> rightWindows = extractScheduleWindows(right.appearanceRules());
        if (leftWindows.isEmpty() || rightWindows.isEmpty()) {
            return false;
        }
        for (ScheduleWindow leftWindow : leftWindows) {
            for (ScheduleWindow rightWindow : rightWindows) {
                if (leftWindow.overlaps(rightWindow)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasEntityCollision(GovernanceRecord left, GovernanceRecord right) {
        if (!StringUtils.hasText(left.node().getLinkedEntityType())
                || !StringUtils.hasText(right.node().getLinkedEntityType())) {
            return false;
        }
        if (left.node().getLinkedEntityId() == null || right.node().getLinkedEntityId() == null) {
            return false;
        }
        if (!left.node().getLinkedEntityType().trim().equalsIgnoreCase(right.node().getLinkedEntityType())) {
            return false;
        }
        if (!Objects.equals(left.node().getLinkedEntityId(), right.node().getLinkedEntityId())) {
            return false;
        }

        Set<String> leftCategories = left.triggerRules().stream()
                .filter(Objects::nonNull)
                .map(AdminIndoorNodeBehaviorPayload.TriggerStep::getCategory)
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> rightCategories = right.triggerRules().stream()
                .filter(Objects::nonNull)
                .map(AdminIndoorNodeBehaviorPayload.TriggerStep::getCategory)
                .filter(StringUtils::hasText)
                .map(value -> value.trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (leftCategories.isEmpty() || rightCategories.isEmpty()) {
            return false;
        }
        for (String category : leftCategories) {
            if (rightCategories.contains(category)) {
                return true;
            }
        }
        return false;
    }

    private void addPairConflict(
            Map<Long, List<AdminIndoorRuleConflictResponse>> result,
            GovernanceRecord left,
            GovernanceRecord right,
            String code,
            String severity,
            String message
    ) {
        result.get(left.behavior().getId()).add(buildConflict(left, right, code, severity, message));
        result.get(right.behavior().getId()).add(buildConflict(right, left, code, severity, message));
    }

    private List<AdminIndoorRuleConflictResponse> dedupe(List<AdminIndoorRuleConflictResponse> conflicts) {
        if (conflicts == null || conflicts.isEmpty()) {
            return Collections.emptyList();
        }
        Map<String, AdminIndoorRuleConflictResponse> deduped = new LinkedHashMap<>();
        for (AdminIndoorRuleConflictResponse conflict : conflicts) {
            String key = String.join("|",
                    String.valueOf(conflict.getBehaviorId()),
                    String.valueOf(conflict.getRelatedBehaviorId()),
                    String.valueOf(conflict.getConflictCode()),
                    String.valueOf(conflict.getSeverity()),
                    String.valueOf(conflict.getMessage()));
            deduped.putIfAbsent(key, conflict);
        }
        return new ArrayList<>(deduped.values());
    }

    private AdminIndoorRuleGovernanceItemResponse toOverviewItem(
            GovernanceRecord record,
            List<AdminIndoorRuleConflictResponse> conflicts,
            List<RewardRuleBinding> rewardRuleBindings
    ) {
        return AdminIndoorRuleGovernanceItemResponse.builder()
                .nodeId(record.node().getId())
                .behaviorId(record.behavior().getId())
                .behaviorCode(record.behavior().getBehaviorCode())
                .behaviorNameZh(record.behavior().getBehaviorNameZh())
                .behaviorNameZht(record.behavior().getBehaviorNameZht())
                .behaviorNameEn(record.behavior().getBehaviorNameEn())
                .behaviorNamePt(record.behavior().getBehaviorNamePt())
                .markerCode(record.node().getMarkerCode())
                .presentationMode(record.node().getPresentationMode())
                .overlayType(record.node().getOverlayType())
                .buildingId(record.node().getBuildingId())
                .buildingNameZht(buildingName(record.building()))
                .floorId(record.node().getFloorId())
                .floorCode(record.floor() == null ? null : record.floor().getFloorCode())
                .linkedEntityType(record.node().getLinkedEntityType())
                .linkedEntityId(record.node().getLinkedEntityId())
                .runtimeSupportLevel(record.runtimeSupportLevel())
                .status(record.behavior().getStatus())
                .appearanceRuleCount(record.appearanceRules().size())
                .triggerRuleCount(record.triggerRules().size())
                .effectRuleCount(record.effectRules().size())
                .hasPathGraph(hasPathGraph(record.pathGraph()))
                .conflictCount(conflicts == null ? 0 : conflicts.size())
                .linkedRewardRuleCount(rewardRuleBindings == null ? 0 : rewardRuleBindings.size())
                .build();
    }

    private Map<Long, List<RewardRuleBinding>> loadRewardRuleBindings(Collection<Long> behaviorIds) {
        List<Long> normalizedIds = normalizeIds(behaviorIds);
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return rewardRuleBindingMapper.selectList(new LambdaQueryWrapper<RewardRuleBinding>()
                        .eq(RewardRuleBinding::getOwnerDomain, OWNER_TYPE_INDOOR_BEHAVIOR)
                        .in(RewardRuleBinding::getOwnerId, normalizedIds)
                        .orderByAsc(RewardRuleBinding::getOwnerId)
                        .orderByAsc(RewardRuleBinding::getSortOrder)
                        .orderByAsc(RewardRuleBinding::getId))
                .stream()
                .collect(Collectors.groupingBy(RewardRuleBinding::getOwnerId, LinkedHashMap::new, Collectors.toList()));
    }

    private List<AdminRewardRuleLinkResponse> toRewardRuleLinks(Collection<Long> ruleIds) {
        List<Long> normalizedRuleIds = normalizeIds(ruleIds);
        if (normalizedRuleIds.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, RewardRule> rulesById = rewardRuleMapper.selectBatchIds(normalizedRuleIds).stream()
                .collect(Collectors.toMap(RewardRule::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
        return normalizedRuleIds.stream()
                .map(rulesById::get)
                .filter(Objects::nonNull)
                .map(rule -> AdminRewardRuleLinkResponse.builder()
                        .id(rule.getId())
                        .code(rule.getCode())
                        .nameZh(rule.getNameZh())
                        .nameZht(rule.getNameZht())
                        .summaryText(rule.getSummaryText())
                        .status(rule.getStatus())
                        .build())
                .toList();
    }

    private List<AdminRewardLinkedEntityResponse> loadLinkedRewardOwners(Collection<Long> ruleIds) {
        List<Long> normalizedRuleIds = normalizeIds(ruleIds);
        if (normalizedRuleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<RewardRuleBinding> bindings = rewardRuleBindingMapper.selectList(new LambdaQueryWrapper<RewardRuleBinding>()
                .in(RewardRuleBinding::getRuleId, normalizedRuleIds)
                .in(RewardRuleBinding::getOwnerDomain, List.of(OWNER_TYPE_REDEEMABLE_PRIZE, OWNER_TYPE_GAME_REWARD))
                .orderByAsc(RewardRuleBinding::getRuleId)
                .orderByAsc(RewardRuleBinding::getSortOrder)
                .orderByAsc(RewardRuleBinding::getId));
        if (bindings.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, RedeemablePrize> prizesById = redeemablePrizeMapper.selectBatchIds(bindings.stream()
                        .filter(binding -> OWNER_TYPE_REDEEMABLE_PRIZE.equals(binding.getOwnerDomain()))
                        .map(RewardRuleBinding::getOwnerId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(RedeemablePrize::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
        Map<Long, GameReward> gameRewardsById = gameRewardMapper.selectBatchIds(bindings.stream()
                        .filter(binding -> OWNER_TYPE_GAME_REWARD.equals(binding.getOwnerDomain()))
                        .map(RewardRuleBinding::getOwnerId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(GameReward::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        return bindings.stream()
                .map(binding -> {
                    if (OWNER_TYPE_REDEEMABLE_PRIZE.equals(binding.getOwnerDomain())) {
                        RedeemablePrize prize = prizesById.get(binding.getOwnerId());
                        return AdminRewardLinkedEntityResponse.builder()
                                .ownerDomain(binding.getOwnerDomain())
                                .ownerId(binding.getOwnerId())
                                .ownerCode(prize == null ? binding.getOwnerCode() : prize.getCode())
                                .ownerName(prize == null ? binding.getOwnerCode() : firstNonBlank(prize.getNameZht(), prize.getNameZh(), prize.getCode()))
                                .bindingRole(binding.getBindingRole())
                                .build();
                    }
                    GameReward reward = gameRewardsById.get(binding.getOwnerId());
                    return AdminRewardLinkedEntityResponse.builder()
                            .ownerDomain(binding.getOwnerDomain())
                            .ownerId(binding.getOwnerId())
                            .ownerCode(reward == null ? binding.getOwnerCode() : reward.getCode())
                            .ownerName(reward == null ? binding.getOwnerCode() : firstNonBlank(reward.getNameZht(), reward.getNameZh(), reward.getCode()))
                            .bindingRole(binding.getBindingRole())
                            .build();
                })
                .toList();
    }

    private List<String> buildStatusWarnings(IndoorNode node, String runtimeSupportLevel, String requestedStatus) {
        if (!isEnabled(requestedStatus)) {
            return Collections.emptyList();
        }
        List<String> warnings = new ArrayList<>();
        String nodeStatus = normalizeStatus(node == null ? null : node.getStatus());
        if (!"published".equals(nodeStatus)) {
            warnings.add("\u7236\u7bc0\u9ede\u672a\u767c\u4f48\u6216\u5df2\u4e0b\u7dda\uff0c\u4e0d\u80fd\u555f\u7528\u4e92\u52d5\u898f\u5247\u3002");
        }
        if (STORAGE_ONLY_RUNTIME.equalsIgnoreCase(runtimeSupportLevel)) {
            warnings.add("\u76ee\u524d\u50c5\u652f\u63f4 Phase 15 \u5b58\u6a94\u6a21\u5f0f\uff0c\u7121\u6cd5\u76f4\u63a5\u555f\u7528\u57f7\u884c\u3002");
        }
        return warnings;
    }

    private String resolveRuntimeSupportLevel(IndoorNodeBehavior behavior, IndoorNode node) {
        return firstNonBlank(
                behavior == null ? null : behavior.getRuntimeSupportLevel(),
                node == null ? null : node.getRuntimeSupportLevel(),
                STORAGE_ONLY_RUNTIME
        );
    }

    private boolean isEnabled(String status) {
        return ENABLED_STATUSES.contains(normalizeStatus(status));
    }

    private String normalizeStatus(String status) {
        return StringUtils.hasText(status) ? status.trim().toLowerCase(Locale.ROOT) : null;
    }

    private boolean contains(String value, String keyword) {
        return StringUtils.hasText(value) && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private String buildingName(Building building) {
        if (building == null) {
            return null;
        }
        return firstNonBlank(
                building.getNameZht(),
                building.getNameZh(),
                building.getNameEn(),
                building.getBuildingCode()
        );
    }

    private boolean hasPathGraph(AdminIndoorNodeBehaviorPayload.PathGraph pathGraph) {
        return pathGraph != null && pathGraph.getPoints() != null && pathGraph.getPoints().size() >= 2;
    }

    private boolean truthy(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private <T> List<T> readJsonList(String value, TypeReference<List<T>> typeReference) {
        if (!StringUtils.hasText(value)) {
            return Collections.emptyList();
        }
        try {
            List<T> list = objectMapper.readValue(value, typeReference);
            return list == null ? Collections.emptyList() : list;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private <T> T readJsonValue(String value, Class<T> clazz) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(value, clazz);
        } catch (Exception ignored) {
            return null;
        }
    }

    private IndoorNodeBehavior requireBehavior(Long behaviorId) {
        IndoorNodeBehavior behavior = behaviorMapper.selectById(behaviorId);
        if (behavior == null) {
            throw new BusinessException(4041, "Indoor behavior not found");
        }
        return behavior;
    }

    private IndoorNode requireNode(Long nodeId) {
        IndoorNode node = nodeMapper.selectById(nodeId);
        if (node == null) {
            throw new BusinessException(4041, "Indoor node not found");
        }
        return node;
    }

    private IndoorFloor requireFloor(Long floorId) {
        IndoorFloor floor = floorMapper.selectById(floorId);
        if (floor == null) {
            throw new BusinessException(4041, "Indoor floor not found");
        }
        return floor;
    }

    private Building requireBuilding(Long buildingId) {
        Building building = buildingMapper.selectById(buildingId);
        if (building == null) {
            throw new BusinessException(4041, "Indoor building not found");
        }
        return building;
    }

    private Map<Long, IndoorNode> loadNodes(Collection<Long> nodeIds) {
        List<Long> normalizedIds = normalizeIds(nodeIds);
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return nodeMapper.selectBatchIds(normalizedIds).stream()
                .collect(Collectors.toMap(IndoorNode::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, IndoorFloor> loadFloors(Collection<Long> floorIds) {
        List<Long> normalizedIds = normalizeIds(floorIds);
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return floorMapper.selectBatchIds(normalizedIds).stream()
                .collect(Collectors.toMap(IndoorFloor::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, Building> loadBuildings(Collection<Long> buildingIds) {
        List<Long> normalizedIds = normalizeIds(buildingIds);
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return buildingMapper.selectBatchIds(normalizedIds).stream()
                .collect(Collectors.toMap(Building::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private List<Long> normalizeIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private AdminIndoorRuleConflictResponse buildConflict(
            GovernanceRecord source,
            GovernanceRecord related,
            String code,
            String severity,
            String message
    ) {
        return AdminIndoorRuleConflictResponse.builder()
                .behaviorId(source.behavior().getId())
                .nodeId(source.node().getId())
                .buildingId(source.node().getBuildingId())
                .floorId(source.node().getFloorId())
                .relatedBehaviorId(related == null ? null : related.behavior().getId())
                .relatedNodeId(related == null ? null : related.node().getId())
                .conflictCode(code)
                .severity(severity)
                .message(message)
                .build();
    }

    private List<ScheduleWindow> extractScheduleWindows(List<AdminIndoorNodeBehaviorPayload.RuleCondition> appearanceRules) {
        if (appearanceRules == null || appearanceRules.isEmpty()) {
            return Collections.emptyList();
        }
        List<ScheduleWindow> windows = new ArrayList<>();
        for (AdminIndoorNodeBehaviorPayload.RuleCondition condition : appearanceRules) {
            if (condition == null || !StringUtils.hasText(condition.getCategory()) || condition.getConfig() == null) {
                continue;
            }
            String category = condition.getCategory().trim().toLowerCase(Locale.ROOT);
            if (!"schedule_window".equals(category) && !"recurring_calendar".equals(category)) {
                continue;
            }

            List<JsonNode> entries = new ArrayList<>();
            JsonNode config = condition.getConfig();
            if (config.isArray()) {
                config.forEach(entries::add);
            } else if (config.has("windows") && config.get("windows").isArray()) {
                config.get("windows").forEach(entries::add);
            } else if (config.has("timeRanges") && config.get("timeRanges").isArray()) {
                config.get("timeRanges").forEach(entries::add);
            } else if (config.has("schedules") && config.get("schedules").isArray()) {
                config.get("schedules").forEach(entries::add);
            } else {
                entries.add(config);
            }

            for (JsonNode entry : entries) {
                Set<Integer> weekdays = parseWeekdays(entry);
                if (weekdays.isEmpty()) {
                    weekdays = Set.of(1, 2, 3, 4, 5, 6, 7);
                }
                Integer startMinutes = firstNonNull(
                        parseMinutes(text(entry, "startTime")),
                        parseMinutes(text(entry, "start")),
                        parseMinutes(text(entry, "from")),
                        parseMinutes(text(entry, "beginTime"))
                );
                Integer endMinutes = firstNonNull(
                        parseMinutes(text(entry, "endTime")),
                        parseMinutes(text(entry, "end")),
                        parseMinutes(text(entry, "to")),
                        parseMinutes(text(entry, "finishTime"))
                );

                int start = startMinutes == null ? 0 : Math.max(0, Math.min(startMinutes, 1440));
                int end = endMinutes == null ? 1440 : Math.max(0, Math.min(endMinutes, 1440));
                if (start == end) {
                    windows.add(new ScheduleWindow(weekdays, 0, 1440));
                    continue;
                }
                if (end < start) {
                    windows.add(new ScheduleWindow(weekdays, start, 1440));
                    windows.add(new ScheduleWindow(weekdays, 0, end));
                    continue;
                }
                windows.add(new ScheduleWindow(weekdays, start, end));
            }
        }
        return windows;
    }

    private Integer parseMinutes(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        try {
            if (normalized.matches("^\\d{1,2}:\\d{2}$")) {
                LocalTime localTime = LocalTime.parse(normalized, TIME_FORMATTER);
                return localTime.getHour() * 60 + localTime.getMinute();
            }
            if (normalized.matches("^\\d{3,4}$")) {
                String padded = normalized.length() == 3 ? "0" + normalized : normalized;
                int hour = Integer.parseInt(padded.substring(0, 2));
                int minute = Integer.parseInt(padded.substring(2, 4));
                return hour * 60 + minute;
            }
            if (normalized.matches("^\\d+$")) {
                return Integer.parseInt(normalized);
            }
        } catch (DateTimeParseException | NumberFormatException ignored) {
            return null;
        }
        return null;
    }

    private String text(JsonNode node, String fieldName) {
        if (node == null || fieldName == null || !node.has(fieldName)) {
            return null;
        }
        JsonNode child = node.get(fieldName);
        if (child == null || child.isNull()) {
            return null;
        }
        if (child.isTextual() || child.isNumber() || child.isBoolean()) {
            return child.asText();
        }
        return null;
    }

    private Integer weekdayToNumber(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "1", "mon", "monday", "\u9031\u4e00", "\u661f\u671f\u4e00", "\u79ae\u62dc\u4e00" -> 1;
            case "2", "tue", "tues", "tuesday", "\u9031\u4e8c", "\u661f\u671f\u4e8c", "\u79ae\u62dc\u4e8c" -> 2;
            case "3", "wed", "wednesday", "\u9031\u4e09", "\u661f\u671f\u4e09", "\u79ae\u62dc\u4e09" -> 3;
            case "4", "thu", "thur", "thursday", "\u9031\u56db", "\u661f\u671f\u56db", "\u79ae\u62dc\u56db" -> 4;
            case "5", "fri", "friday", "\u9031\u4e94", "\u661f\u671f\u4e94", "\u79ae\u62dc\u4e94" -> 5;
            case "6", "sat", "saturday", "\u9031\u516d", "\u661f\u671f\u516d", "\u79ae\u62dc\u516d" -> 6;
            case "0", "7", "sun", "sunday", "\u9031\u65e5", "\u9031\u5929", "\u661f\u671f\u65e5", "\u661f\u671f\u5929", "\u79ae\u62dc\u65e5", "\u79ae\u62dc\u5929" -> 7;
            default -> null;
        };
    }

    private boolean hasTriggerCycle(
            AdminIndoorNodeBehaviorPayload.TriggerStep source,
            Map<String, AdminIndoorNodeBehaviorPayload.TriggerStep> triggerMap
    ) {
        String sourceId = StringUtils.hasText(source.getId()) ? source.getId().trim() : null;
        String dependencyId = StringUtils.hasText(source.getDependsOnTriggerId()) ? source.getDependsOnTriggerId().trim() : null;
        if (!StringUtils.hasText(sourceId) || !StringUtils.hasText(dependencyId)) {
            return false;
        }
        if (sourceId.equals(dependencyId)) {
            return true;
        }
        Set<String> visited = new HashSet<>();
        visited.add(sourceId);
        while (StringUtils.hasText(dependencyId)) {
            if (!visited.add(dependencyId)) {
                return true;
            }
            AdminIndoorNodeBehaviorPayload.TriggerStep dependency = triggerMap.get(dependencyId);
            if (dependency == null || !StringUtils.hasText(dependency.getDependsOnTriggerId())) {
                return false;
            }
            dependencyId = dependency.getDependsOnTriggerId().trim();
        }
        return false;
    }

    private Set<Integer> parseWeekdays(JsonNode node) {
        Set<Integer> weekdays = new LinkedHashSet<>();
        if (node == null) {
            return weekdays;
        }
        List<String> values = new ArrayList<>();
        copyWeekdayValues(node.get("weekdays"), values);
        copyWeekdayValues(node.get("daysOfWeek"), values);
        copyWeekdayValues(node.get("dayOfWeek"), values);
        copyWeekdayValues(node.get("weekday"), values);
        copyWeekdayValues(node.get("repeatDays"), values);
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }
            for (String token : value.split("[,|/\\s]+")) {
                Integer weekday = weekdayToNumber(token);
                if (weekday != null) {
                    weekdays.add(weekday);
                }
            }
        }
        return weekdays;
    }

    private void copyWeekdayValues(JsonNode node, List<String> target) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isArray()) {
            node.forEach(child -> target.add(child.asText()));
            return;
        }
        if (node.isTextual() || node.isNumber()) {
            target.add(node.asText());
        }
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        if (values == null) {
            return null;
        }
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private record GovernanceFilters(
            String keyword,
            Long cityId,
            Long buildingId,
            Long floorId,
            Long relatedPoiId,
            String linkedEntityType,
            Long linkedEntityId,
            String status,
            String runtimeSupportLevel,
            boolean conflictOnly,
            boolean enabledOnly
    ) {}

    private record GovernanceRecord(
            IndoorNodeBehavior behavior,
            IndoorNode node,
            IndoorFloor floor,
            Building building,
            List<AdminIndoorNodeBehaviorPayload.RuleCondition> appearanceRules,
            List<AdminIndoorNodeBehaviorPayload.TriggerStep> triggerRules,
            List<AdminIndoorNodeBehaviorPayload.EffectDefinition> effectRules,
            AdminIndoorNodeBehaviorPayload.PathGraph pathGraph,
            String runtimeSupportLevel
    ) {}

    private record GovernanceContext(
            List<GovernanceRecord> records,
            Map<Long, List<AdminIndoorRuleConflictResponse>> conflictsByBehaviorId,
            Map<Long, List<RewardRuleBinding>> rewardRuleBindingsByBehaviorId
    ) {}

    private record ScheduleWindow(Set<Integer> weekdays, int startMinutes, int endMinutes) {
        boolean overlaps(ScheduleWindow other) {
            Set<Integer> intersection = new HashSet<>(weekdays);
            intersection.retainAll(other.weekdays);
            if (intersection.isEmpty()) {
                return false;
            }
            return startMinutes < other.endMinutes && other.startMinutes < endMinutes;
        }
    }
}
