package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminIndoorMarkerCsvConfirmRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorMarkerUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorNodeBehaviorPayload;
import com.aoxiaoyou.admin.dto.request.AdminIndoorNodeUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminIndoorMarkerCsvImportResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorMarkerCsvPreviewResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorMarkerResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorNodeResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleValidationResponse;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.IndoorFloor;
import com.aoxiaoyou.admin.entity.IndoorNode;
import com.aoxiaoyou.admin.entity.IndoorNodeBehavior;
import com.aoxiaoyou.admin.entity.IndoorNodeImportBatch;
import com.aoxiaoyou.admin.entity.Poi;
import com.aoxiaoyou.admin.entity.RewardRule;
import com.aoxiaoyou.admin.entity.RewardRuleBinding;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.IndoorFloorMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeBehaviorMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeImportBatchMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeMapper;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleBindingMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class IndoorMarkerAuthoringService {

    private static final String OWNER_TYPE_INDOOR_BEHAVIOR = "indoor_behavior";
    private static final String REWARD_BINDING_ROLE_ATTACHED = "attached";

    private static final Set<String> ALLOWED_NODE_TYPES = Set.of(
            "poi", "shop", "service", "landmark", "elevator", "stairs", "restroom", "entrance", "exit", "custom"
    );

    private static final Set<String> ALLOWED_PRESENTATION_MODES = Set.of("marker", "overlay", "hybrid");
    private static final Set<String> ALLOWED_OVERLAY_TYPES = Set.of("point", "polyline", "polygon");
    private static final Set<String> ALLOWED_INHERIT_MODES = Set.of("override", "append", "linked_entity_default", "linked_entity_only", "manual");
    private static final Set<String> ALLOWED_RUNTIME_SUPPORT_LEVELS = Set.of(
            "phase15_storage_only", "phase16_planned", "phase16_supported", "future_only", "preview"
    );
    private static final Set<String> ALLOWED_APPEARANCE_CATEGORIES = Set.of(
            "schedule_window", "recurring_calendar", "user_progress", "scene_dwell", "proximity", "always_on", "manual"
    );
    private static final Set<String> ALLOWED_TRIGGER_CATEGORIES = Set.of(
            "tap", "proximity", "dwell", "drag", "voice_placeholder", "custom"
    );
    private static final Set<String> ALLOWED_EFFECT_CATEGORIES = Set.of(
            "popup", "bubble", "media", "path_motion", "reward_grant", "collectible_grant",
            "badge_grant", "task_update", "account_adjustment"
    );

    private final IndoorFloorMapper indoorFloorMapper;
    private final IndoorNodeMapper indoorNodeMapper;
    private final IndoorNodeBehaviorMapper indoorNodeBehaviorMapper;
    private final IndoorNodeImportBatchMapper indoorNodeImportBatchMapper;
    private final BuildingMapper buildingMapper;
    private final PoiMapper poiMapper;
    private final ContentAssetMapper contentAssetMapper;
    private final RewardRuleMapper rewardRuleMapper;
    private final RewardRuleBindingMapper rewardRuleBindingMapper;
    private final ObjectMapper objectMapper;

    public List<AdminIndoorNodeResponse> listFloorNodes(Long floorId, String status) {
        requireFloor(floorId);
        List<IndoorNode> nodes = indoorNodeMapper.selectList(new LambdaQueryWrapper<IndoorNode>()
                .eq(IndoorNode::getFloorId, floorId)
                .eq(StringUtils.hasText(status), IndoorNode::getStatus, status)
                .orderByAsc(IndoorNode::getSortOrder)
                .orderByAsc(IndoorNode::getId));
        return toNodeResponses(nodes);
    }

    public AdminIndoorNodeResponse createNode(Long floorId, AdminIndoorNodeUpsertRequest request) {
        IndoorFloor floor = requireFloor(floorId);
        NodeValidationContext validationContext = validateNodeRequest(floor, request, null, true);
        IndoorNode node = new IndoorNode();
        node.setBuildingId(floor.getBuildingId());
        node.setFloorId(floorId);
        applyNodeRequest(node, request, validationContext);
        indoorNodeMapper.insert(node);
        syncBehaviors(node.getId(), request.getBehaviors());
        return getNodeResponse(node.getId());
    }

    public AdminIndoorNodeResponse updateNode(Long nodeId, AdminIndoorNodeUpsertRequest request) {
        IndoorNode node = requireNode(nodeId);
        IndoorFloor floor = requireFloor(node.getFloorId());
        NodeValidationContext validationContext = validateNodeRequest(floor, request, nodeId, true);
        applyNodeRequest(node, request, validationContext);
        indoorNodeMapper.updateById(node);
        syncBehaviors(nodeId, request.getBehaviors());
        return getNodeResponse(nodeId);
    }

    public void deleteNode(Long nodeId) {
        requireNode(nodeId);
        clearBehaviorRewardRuleBindings(loadBehaviorIdsByNode(nodeId));
        indoorNodeBehaviorMapper.delete(new LambdaQueryWrapper<IndoorNodeBehavior>()
                .eq(IndoorNodeBehavior::getNodeId, nodeId));
        indoorNodeMapper.deleteById(nodeId);
    }

    public AdminIndoorRuleValidationResponse validateRuleGraph(Long floorId, Long nodeId, AdminIndoorNodeUpsertRequest request) {
        IndoorFloor floor = null;
        if (floorId != null) {
            floor = requireFloor(floorId);
        } else if (nodeId != null) {
            floor = requireFloor(requireNode(nodeId).getFloorId());
        }
        NodeValidationContext validationContext = validateNodeRequest(floor, request, nodeId, floor != null);
        return AdminIndoorRuleValidationResponse.builder()
                .valid(true)
                .errors(Collections.emptyList())
                .warnings(validationContext.warnings())
                .normalizedRelativeX(validationContext.relativeX())
                .normalizedRelativeY(validationContext.relativeY())
                .resolvedOverlayType(validationContext.overlayType())
                .behaviorCount(request == null || request.getBehaviors() == null ? 0 : request.getBehaviors().size())
                .build();
    }

    public List<AdminIndoorMarkerResponse> listFloorMarkers(Long floorId, String status) {
        return listFloorNodes(floorId, status).stream()
                .filter(item -> !"overlay".equals(item.getPresentationMode()))
                .map(this::toMarkerResponse)
                .toList();
    }

    public AdminIndoorMarkerResponse createMarker(Long floorId, AdminIndoorMarkerUpsertRequest request) {
        return toMarkerResponse(createNode(floorId, toNodeRequest(request)));
    }

    public AdminIndoorMarkerResponse updateMarker(Long markerId, AdminIndoorMarkerUpsertRequest request) {
        return toMarkerResponse(updateNode(markerId, toNodeRequest(request)));
    }

    public void deleteMarker(Long markerId) {
        deleteNode(markerId);
    }

    public AdminIndoorMarkerCsvPreviewResponse previewMarkerCsv(Long floorId, MultipartFile file) {
        IndoorFloor floor = requireFloor(floorId);
        List<AdminIndoorMarkerCsvPreviewResponse.Row> rows = parseMarkerCsv(floor, file);
        return buildPreviewResponse(floorId, fileName(file), rows);
    }

    public AdminIndoorMarkerCsvImportResponse confirmMarkerCsv(Long floorId, AdminIndoorMarkerCsvConfirmRequest request, Long adminUserId) {
        IndoorFloor floor = requireFloor(floorId);
        List<AdminIndoorMarkerCsvPreviewResponse.Row> rows = rebuildRowsFromRequest(floor, request);
        if (rows.stream().anyMatch(row -> !row.isValid())) {
            throw new BusinessException(4001, "Marker CSV confirm contains invalid rows");
        }

        IndoorNodeImportBatch batch = new IndoorNodeImportBatch();
        batch.setFloorId(floorId);
        batch.setSourceFilename(StringUtils.hasText(request.getSourceFilename()) ? request.getSourceFilename().trim() : "manual-confirm");
        batch.setTotalRows(rows.size());
        batch.setValidRows(rows.size());
        batch.setInvalidRows(0);
        batch.setPreviewPayloadJson(writeJson(Map.of("floorId", floorId, "rows", rows)));
        batch.setCreatedByAdminId(adminUserId);
        indoorNodeImportBatchMapper.insert(batch);

        List<AdminIndoorMarkerResponse> created = new ArrayList<>();
        for (AdminIndoorMarkerCsvPreviewResponse.Row row : rows) {
            IndoorNode marker = new IndoorNode();
            marker.setBuildingId(floor.getBuildingId());
            marker.setFloorId(floorId);
            marker.setImportBatchId(batch.getId());
            applyPreviewRow(marker, row);
            indoorNodeMapper.insert(marker);
            syncBehaviors(marker.getId(), buildBehaviorPayloadsFromCsvRow(row));
            created.add(toMarkerResponse(getNodeResponse(marker.getId())));
        }

        return AdminIndoorMarkerCsvImportResponse.builder()
                .batchId(batch.getId())
                .floorId(floorId)
                .totalRows(rows.size())
                .importedRows(created.size())
                .skippedRows(0)
                .markers(created)
                .build();
    }

    private List<AdminIndoorMarkerCsvPreviewResponse.Row> parseMarkerCsv(IndoorFloor floor, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(4055, "Marker CSV file is required");
        }
        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new BusinessException(5057, "Failed to read marker CSV");
        }
        List<String> lines = content.lines().filter(StringUtils::hasText).toList();
        if (lines.isEmpty()) {
            throw new BusinessException(4001, "Marker CSV is empty");
        }
        List<String> headers = parseCsvLine(lines.get(0));
        if (headers.isEmpty()) {
            throw new BusinessException(4001, "Marker CSV header is invalid");
        }

        Set<String> previewCodes = new LinkedHashSet<>();
        List<AdminIndoorMarkerCsvPreviewResponse.Row> rows = new ArrayList<>();
        for (int index = 1; index < lines.size(); index++) {
            List<String> cells = parseCsvLine(lines.get(index));
            Map<String, String> cellMap = new LinkedHashMap<>();
            for (int headerIndex = 0; headerIndex < headers.size(); headerIndex++) {
                cellMap.put(headers.get(headerIndex), headerIndex < cells.size() ? cells.get(headerIndex) : "");
            }
            rows.add(buildPreviewRow(floor, index + 1, cellMap, previewCodes));
        }
        return rows;
    }

    private List<AdminIndoorMarkerCsvPreviewResponse.Row> rebuildRowsFromRequest(IndoorFloor floor, AdminIndoorMarkerCsvConfirmRequest request) {
        if (request == null || request.getRows() == null || request.getRows().isEmpty()) {
            throw new BusinessException(4001, "Marker CSV confirm request is empty");
        }
        Set<String> previewCodes = new LinkedHashSet<>();
        List<AdminIndoorMarkerCsvPreviewResponse.Row> rows = new ArrayList<>();
        int fallbackRowNumber = 1;
        for (AdminIndoorMarkerCsvConfirmRequest.Row row : request.getRows()) {
            Map<String, String> cellMap = new LinkedHashMap<>();
            cellMap.put("markerCode", row.getMarkerCode());
            cellMap.put("nodeType", row.getNodeType());
            cellMap.put("nameZh", row.getNodeNameZh());
            cellMap.put("nameEn", row.getNodeNameEn());
            cellMap.put("nameZht", row.getNodeNameZht());
            cellMap.put("namePt", row.getNodeNamePt());
            cellMap.put("descriptionZh", row.getDescriptionZh());
            cellMap.put("descriptionEn", row.getDescriptionEn());
            cellMap.put("descriptionZht", row.getDescriptionZht());
            cellMap.put("descriptionPt", row.getDescriptionPt());
            cellMap.put("relativeX", toPlainString(row.getRelativeX()));
            cellMap.put("relativeY", toPlainString(row.getRelativeY()));
            cellMap.put("relatedPoiId", row.getRelatedPoiId() == null ? null : String.valueOf(row.getRelatedPoiId()));
            cellMap.put("iconAssetId", row.getIconAssetId() == null ? null : String.valueOf(row.getIconAssetId()));
            cellMap.put("animationAssetId", row.getAnimationAssetId() == null ? null : String.valueOf(row.getAnimationAssetId()));
            cellMap.put("linkedEntityType", row.getLinkedEntityType());
            cellMap.put("linkedEntityId", row.getLinkedEntityId() == null ? null : String.valueOf(row.getLinkedEntityId()));
            cellMap.put("tagsJson", row.getTagsJson());
            cellMap.put("popupConfigJson", row.getPopupConfigJson());
            cellMap.put("displayConfigJson", row.getDisplayConfigJson());
            cellMap.put("metadataJson", row.getMetadataJson());
            cellMap.put("sortOrder", row.getSortOrder() == null ? null : String.valueOf(row.getSortOrder()));
            cellMap.put("status", row.getStatus());
            cellMap.put("presentationMode", row.getPresentationMode());
            cellMap.put("appearancePresetCode", row.getAppearancePresetCode());
            cellMap.put("triggerTemplateCode", row.getTriggerTemplateCode());
            cellMap.put("effectTemplateCode", row.getEffectTemplateCode());
            cellMap.put("inheritMode", row.getInheritMode());
            rows.add(buildPreviewRow(floor, row.getRowNumber() == null ? fallbackRowNumber : row.getRowNumber(), cellMap, previewCodes));
            fallbackRowNumber++;
        }
        return rows;
    }

    private AdminIndoorMarkerCsvPreviewResponse.Row buildPreviewRow(
            IndoorFloor floor,
            int rowNumber,
            Map<String, String> cellMap,
            Set<String> previewCodes) {
        List<String> errors = new ArrayList<>();
        String markerCode = textOrNull(cellMap.get("markerCode"));
        String nodeNameZh = textOrNull(cellMap.get("nameZh"));
        String nodeType = textOrDefault(cellMap.get("nodeType"), "custom").toLowerCase(Locale.ROOT);
        BigDecimal relativeX = parseBigDecimal(cellMap.get("relativeX"), "relativeX", errors);
        BigDecimal relativeY = parseBigDecimal(cellMap.get("relativeY"), "relativeY", errors);
        Long relatedPoiId = parseLong(cellMap.get("relatedPoiId"), "relatedPoiId", errors);
        Long iconAssetId = parseLong(cellMap.get("iconAssetId"), "iconAssetId", errors);
        Long animationAssetId = parseLong(cellMap.get("animationAssetId"), "animationAssetId", errors);
        Long linkedEntityId = parseLong(cellMap.get("linkedEntityId"), "linkedEntityId", errors);
        Integer sortOrder = parseInteger(cellMap.get("sortOrder"), "sortOrder", errors);
        String status = textOrDefault(cellMap.get("status"), "draft");
        String presentationMode = textOrDefault(cellMap.get("presentationMode"), "marker").toLowerCase(Locale.ROOT);
        String appearancePresetCode = textOrNull(cellMap.get("appearancePresetCode"));
        String triggerTemplateCode = textOrNull(cellMap.get("triggerTemplateCode"));
        String effectTemplateCode = textOrNull(cellMap.get("effectTemplateCode"));
        String inheritMode = textOrDefault(cellMap.get("inheritMode"), "override");
        Building building = requireBuilding(floor.getBuildingId());

        if (!StringUtils.hasText(markerCode)) {
            markerCode = String.format(Locale.ROOT, "floor-%d-row-%d", floor.getId(), rowNumber);
        }
        if (!previewCodes.add(markerCode)) {
            errors.add("markerCode is duplicated within the CSV");
        }
        if (!StringUtils.hasText(nodeNameZh)) {
            errors.add("nameZh is required");
        }
        if (!ALLOWED_NODE_TYPES.contains(nodeType)) {
            errors.add("nodeType is not supported");
        }
        if (!ALLOWED_PRESENTATION_MODES.contains(presentationMode)) {
            errors.add("presentationMode is not supported");
        }
        if (StringUtils.hasText(inheritMode) && !ALLOWED_INHERIT_MODES.contains(inheritMode)) {
            errors.add("inheritMode is not supported");
        }
        validateRelative(relativeX, "relativeX", errors);
        validateRelative(relativeY, "relativeY", errors);
        if (relatedPoiId != null) {
            Poi poi = poiMapper.selectById(relatedPoiId);
            if (poi == null) {
                errors.add("relatedPoiId does not exist");
            } else if (!Objects.equals(poi.getCityId(), building.getCityId())) {
                errors.add("relatedPoiId must belong to the same city as the current building");
            }
        }
        if (iconAssetId != null && contentAssetMapper.selectById(iconAssetId) == null) {
            errors.add("iconAssetId does not exist");
        }
        if (animationAssetId != null && contentAssetMapper.selectById(animationAssetId) == null) {
            errors.add("animationAssetId does not exist");
        }
        IndoorNode existing = indoorNodeMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<IndoorNode>()
                .eq(IndoorNode::getFloorId, floor.getId())
                .eq(IndoorNode::getMarkerCode, markerCode)
                .last("LIMIT 1"));
        if (existing != null) {
            errors.add("markerCode already exists in this floor");
        }

        String tagsJson = textOrNull(cellMap.get("tagsJson"));
        String popupConfigJson = textOrNull(cellMap.get("popupConfigJson"));
        String displayConfigJson = textOrNull(cellMap.get("displayConfigJson"));
        String metadataJson = textOrNull(cellMap.get("metadataJson"));
        requireJson(tagsJson, "tagsJson", errors);
        requireJson(popupConfigJson, "popupConfigJson", errors);
        requireJson(displayConfigJson, "displayConfigJson", errors);
        requireJson(metadataJson, "metadataJson", errors);

        return AdminIndoorMarkerCsvPreviewResponse.Row.builder()
                .rowNumber(rowNumber)
                .markerCode(markerCode)
                .nodeType(nodeType)
                .nodeNameZh(nodeNameZh)
                .nodeNameEn(textOrNull(cellMap.get("nameEn")))
                .nodeNameZht(textOrNull(cellMap.get("nameZht")))
                .nodeNamePt(textOrNull(cellMap.get("namePt")))
                .descriptionZh(textOrNull(cellMap.get("descriptionZh")))
                .descriptionEn(textOrNull(cellMap.get("descriptionEn")))
                .descriptionZht(textOrNull(cellMap.get("descriptionZht")))
                .descriptionPt(textOrNull(cellMap.get("descriptionPt")))
                .relativeX(relativeX)
                .relativeY(relativeY)
                .relatedPoiId(relatedPoiId)
                .iconAssetId(iconAssetId)
                .animationAssetId(animationAssetId)
                .linkedEntityType(textOrNull(cellMap.get("linkedEntityType")))
                .linkedEntityId(linkedEntityId)
                .tagsJson(tagsJson)
                .popupConfigJson(popupConfigJson)
                .displayConfigJson(displayConfigJson)
                .metadataJson(metadataJson)
                .sortOrder(sortOrder == null ? rowNumber : sortOrder)
                .status(status)
                .presentationMode(presentationMode)
                .appearancePresetCode(appearancePresetCode)
                .triggerTemplateCode(triggerTemplateCode)
                .effectTemplateCode(effectTemplateCode)
                .inheritMode(inheritMode)
                .valid(errors.isEmpty())
                .errors(errors)
                .build();
    }

    private NodeValidationContext validateNodeRequest(
            IndoorFloor floor,
            AdminIndoorNodeUpsertRequest request,
            Long currentNodeId,
            boolean validateFloorBindings
    ) {
        if (request == null) {
            throw new BusinessException(4001, "request body is required");
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Building building = validateFloorBindings && floor != null ? requireBuilding(floor.getBuildingId()) : null;

        if (!StringUtils.hasText(request.getNodeNameZh())) {
            errors.add("nodeNameZh is required");
        }

        String nodeType = textOrDefault(request.getNodeType(), "custom").toLowerCase(Locale.ROOT);
        if (!ALLOWED_NODE_TYPES.contains(nodeType)) {
            errors.add("nodeType is not supported");
        }

        String resolvedPresentationMode = resolvePresentationMode(request);
        if (!ALLOWED_PRESENTATION_MODES.contains(resolvedPresentationMode)) {
            errors.add("presentationMode is not supported");
        }

        if (validateFloorBindings && building != null && request.getRelatedPoiId() != null) {
            Poi poi = requirePoi(request.getRelatedPoiId());
            if (!Objects.equals(poi.getCityId(), building.getCityId())) {
                errors.add("relatedPoiId must belong to the same city as the current building");
            }
        }

        ensureAssetExists(request.getIconAssetId());
        ensureAssetExists(request.getAnimationAssetId());

        String normalizedTagsJson = normalizeTagsJson(request.getTags(), request.getTagsJson(), errors);
        requireJson(request.getPopupConfigJson(), "popupConfigJson", errors);
        requireJson(request.getDisplayConfigJson(), "displayConfigJson", errors);
        requireJson(request.getMetadataJson(), "metadataJson", errors);

        if (validateFloorBindings && floor != null && StringUtils.hasText(request.getMarkerCode())) {
            IndoorNode existing = indoorNodeMapper.selectOne(new LambdaQueryWrapper<IndoorNode>()
                    .eq(IndoorNode::getFloorId, floor.getId())
                    .eq(IndoorNode::getMarkerCode, request.getMarkerCode().trim())
                    .last("LIMIT 1"));
            if (existing != null && !Objects.equals(existing.getId(), currentNodeId)) {
                errors.add("markerCode already exists within the floor");
            }
        }

        ResolvedOverlay resolvedOverlay = resolveOverlay(request, resolvedPresentationMode, errors);
        BigDecimal normalizedRelativeX = request.getRelativeX();
        BigDecimal normalizedRelativeY = request.getRelativeY();
        if (normalizedRelativeX == null && resolvedOverlay.anchorX() != null) {
            normalizedRelativeX = resolvedOverlay.anchorX();
        }
        if (normalizedRelativeY == null && resolvedOverlay.anchorY() != null) {
            normalizedRelativeY = resolvedOverlay.anchorY();
        }
        validateRelative(normalizedRelativeX, "relativeX", errors);
        validateRelative(normalizedRelativeY, "relativeY", errors);

        String runtimeSupportLevel = normalizeRuntimeSupportLevel(request.getRuntimeSupportLevel(), errors, "runtimeSupportLevel");
        validateBehaviorPayloads(request.getBehaviors(), errors, warnings);
        if (!Objects.equals(runtimeSupportLevel, "phase16_supported") && request.getBehaviors() != null && !request.getBehaviors().isEmpty()) {
            warnings.add("Structured indoor rules are stored in Phase 15, but live runtime execution still belongs to Phase 16.");
        }

        if (!errors.isEmpty()) {
            throw new BusinessException(4001, String.join("; ", errors));
        }

        return new NodeValidationContext(
                normalizeRelative(normalizedRelativeX),
                normalizeRelative(normalizedRelativeY),
                resolvedPresentationMode,
                resolvedOverlay.overlayType(),
                resolvedOverlay.overlayGeometryJson(),
                normalizedTagsJson,
                runtimeSupportLevel,
                warnings
        );
    }

    private void applyNodeRequest(IndoorNode node, AdminIndoorNodeUpsertRequest request, NodeValidationContext validationContext) {
        node.setMarkerCode(StringUtils.hasText(request.getMarkerCode())
                ? request.getMarkerCode().trim()
                : String.format(Locale.ROOT, "floor-%d-node-%d", node.getFloorId(), request.getSortOrder() == null ? 0 : request.getSortOrder()));
        node.setNodeType(textOrDefault(request.getNodeType(), "custom").toLowerCase(Locale.ROOT));
        node.setPresentationMode(validationContext.presentationMode());
        node.setOverlayType(validationContext.overlayType());
        node.setNodeNameZh(textOrNull(request.getNodeNameZh()));
        node.setNodeNameEn(textOrNull(request.getNodeNameEn()));
        node.setNodeNameZht(textOrNull(request.getNodeNameZht()));
        node.setNodeNamePt(textOrNull(request.getNodeNamePt()));
        node.setDescriptionZh(textOrNull(request.getDescriptionZh()));
        node.setDescriptionEn(textOrNull(request.getDescriptionEn()));
        node.setDescriptionZht(textOrNull(request.getDescriptionZht()));
        node.setDescriptionPt(textOrNull(request.getDescriptionPt()));
        node.setRelativeX(validationContext.relativeX());
        node.setRelativeY(validationContext.relativeY());
        node.setPositionX(validationContext.relativeX());
        node.setPositionY(validationContext.relativeY());
        node.setRelatedPoiId(request.getRelatedPoiId());
        node.setIconAssetId(request.getIconAssetId());
        node.setAnimationAssetId(request.getAnimationAssetId());
        node.setLinkedEntityType(textOrNull(request.getLinkedEntityType()));
        node.setLinkedEntityId(request.getLinkedEntityId());
        node.setTags(validationContext.tagsJson());
        node.setPopupConfigJson(textOrNull(request.getPopupConfigJson()));
        node.setDisplayConfigJson(textOrNull(request.getDisplayConfigJson()));
        node.setOverlayGeometryJson(validationContext.overlayGeometryJson());
        node.setInheritLinkedEntityRules(Boolean.TRUE.equals(request.getInheritLinkedEntityRules()));
        node.setRuntimeSupportLevel(validationContext.runtimeSupportLevel());
        node.setMetadataJson(textOrNull(request.getMetadataJson()));
        node.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        node.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "draft");
        node.setIcon(resolveLegacyIconValue(node.getIcon(), request.getIconAssetId()));
    }

    private void applyPreviewRow(IndoorNode marker, AdminIndoorMarkerCsvPreviewResponse.Row row) {
        marker.setMarkerCode(row.getMarkerCode());
        marker.setNodeType(row.getNodeType());
        String presentationMode = StringUtils.hasText(row.getPresentationMode()) ? row.getPresentationMode().trim() : "marker";
        marker.setPresentationMode(presentationMode);
        marker.setOverlayType("marker".equals(presentationMode) ? null : "point");
        marker.setNodeNameZh(row.getNodeNameZh());
        marker.setNodeNameEn(row.getNodeNameEn());
        marker.setNodeNameZht(row.getNodeNameZht());
        marker.setNodeNamePt(row.getNodeNamePt());
        marker.setDescriptionZh(row.getDescriptionZh());
        marker.setDescriptionEn(row.getDescriptionEn());
        marker.setDescriptionZht(row.getDescriptionZht());
        marker.setDescriptionPt(row.getDescriptionPt());
        marker.setRelativeX(normalizeRelative(row.getRelativeX()));
        marker.setRelativeY(normalizeRelative(row.getRelativeY()));
        marker.setPositionX(marker.getRelativeX());
        marker.setPositionY(marker.getRelativeY());
        marker.setRelatedPoiId(row.getRelatedPoiId());
        marker.setIconAssetId(row.getIconAssetId());
        marker.setAnimationAssetId(row.getAnimationAssetId());
        marker.setLinkedEntityType(row.getLinkedEntityType());
        marker.setLinkedEntityId(row.getLinkedEntityId());
        marker.setTags(row.getTagsJson());
        marker.setPopupConfigJson(row.getPopupConfigJson());
        marker.setDisplayConfigJson(row.getDisplayConfigJson());
        marker.setOverlayGeometryJson("marker".equals(presentationMode)
                ? null
                : writeJson(buildPointOverlayGeometry(row.getRelativeX(), row.getRelativeY())));
        marker.setInheritLinkedEntityRules(Boolean.FALSE);
        marker.setRuntimeSupportLevel("phase15_storage_only");
        marker.setMetadataJson(row.getMetadataJson());
        marker.setSortOrder(row.getSortOrder() == null ? 0 : row.getSortOrder());
        marker.setStatus(StringUtils.hasText(row.getStatus()) ? row.getStatus().trim() : "draft");
        marker.setIcon(resolveLegacyIconValue(marker.getIcon(), row.getIconAssetId()));
    }

    private List<AdminIndoorNodeBehaviorPayload> buildBehaviorPayloadsFromCsvRow(AdminIndoorMarkerCsvPreviewResponse.Row row) {
        boolean hasBehaviorTemplate = StringUtils.hasText(row.getAppearancePresetCode())
                || StringUtils.hasText(row.getTriggerTemplateCode())
                || StringUtils.hasText(row.getEffectTemplateCode())
                || StringUtils.hasText(row.getInheritMode());
        if (!hasBehaviorTemplate) {
            return Collections.emptyList();
        }
        AdminIndoorNodeBehaviorPayload payload = new AdminIndoorNodeBehaviorPayload();
        payload.setBehaviorCode(String.format(Locale.ROOT, "%s-csv-behavior", row.getMarkerCode()));
        payload.setBehaviorNameZh(row.getNodeNameZh());
        payload.setBehaviorNameEn(row.getNodeNameEn());
        payload.setBehaviorNameZht(row.getNodeNameZht());
        payload.setBehaviorNamePt(row.getNodeNamePt());
        payload.setAppearancePresetCode(textOrNull(row.getAppearancePresetCode()));
        payload.setTriggerTemplateCode(textOrNull(row.getTriggerTemplateCode()));
        payload.setEffectTemplateCode(textOrNull(row.getEffectTemplateCode()));
        payload.setAppearanceRules(new ArrayList<>());
        payload.setTriggerRules(new ArrayList<>());
        payload.setEffectRules(new ArrayList<>());
        payload.setPathGraph(null);
        payload.setInheritMode(StringUtils.hasText(row.getInheritMode()) ? row.getInheritMode().trim() : "override");
        payload.setRuntimeSupportLevel("phase15_storage_only");
        payload.setSortOrder(0);
        payload.setStatus(StringUtils.hasText(row.getStatus()) ? row.getStatus().trim() : "draft");
        return List.of(payload);
    }

    private List<AdminIndoorNodeResponse> toNodeResponses(List<IndoorNode> nodes) {
        Map<Long, ContentAsset> assetMap = loadAssetMap(nodes.stream()
                .flatMap(item -> Stream.of(item.getIconAssetId(), item.getAnimationAssetId()))
                .filter(Objects::nonNull)
                .toList());
        Map<Long, List<IndoorNodeBehavior>> behaviorMap = loadBehaviorMap(nodes.stream()
                .map(IndoorNode::getId)
                .filter(Objects::nonNull)
                .toList());
        Map<Long, List<RewardRuleBinding>> rewardRuleBindingsByBehaviorId = loadBehaviorRewardRuleBindings(
                behaviorMap.values().stream()
                        .flatMap(List::stream)
                        .map(IndoorNodeBehavior::getId)
                        .filter(Objects::nonNull)
                        .toList()
        );
        return nodes.stream().map(item -> toNodeResponse(item, assetMap, behaviorMap, rewardRuleBindingsByBehaviorId)).toList();
    }

    private AdminIndoorMarkerCsvPreviewResponse buildPreviewResponse(Long floorId, String sourceFilename, List<AdminIndoorMarkerCsvPreviewResponse.Row> rows) {
        int validRows = (int) rows.stream().filter(AdminIndoorMarkerCsvPreviewResponse.Row::isValid).count();
        return AdminIndoorMarkerCsvPreviewResponse.builder()
                .floorId(floorId)
                .sourceFilename(sourceFilename)
                .totalRows(rows.size())
                .validRows(validRows)
                .invalidRows(rows.size() - validRows)
                .rows(rows)
                .build();
    }

    private List<String> parseCsvLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (character == '"') {
                if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    quoted = !quoted;
                }
            } else if (character == ',' && !quoted) {
                cells.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(character);
            }
        }
        cells.add(current.toString().trim());
        return cells;
    }

    private IndoorFloor requireFloor(Long floorId) {
        IndoorFloor floor = indoorFloorMapper.selectById(floorId);
        if (floor == null) {
            throw new BusinessException(4046, "floor not found");
        }
        return floor;
    }

    private Building requireBuilding(Long buildingId) {
        Building building = buildingMapper.selectById(buildingId);
        if (building == null) {
            throw new BusinessException(4045, "building not found");
        }
        return building;
    }

    private IndoorNode requireNode(Long nodeId) {
        IndoorNode node = indoorNodeMapper.selectById(nodeId);
        if (node == null) {
            throw new BusinessException(4047, "marker not found");
        }
        return node;
    }

    private Poi requirePoi(Long poiId) {
        Poi poi = poiMapper.selectById(poiId);
        if (poi == null) {
            throw new BusinessException(4041, "poi not found");
        }
        return poi;
    }

    private void ensureAssetExists(Long assetId) {
        if (assetId != null && contentAssetMapper.selectById(assetId) == null) {
            throw new BusinessException(4001, "referenced asset does not exist");
        }
    }

    private Map<Long, ContentAsset> loadAssetMap(Collection<Long> ids) {
        List<Long> normalized = normalizeIds(ids);
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        return contentAssetMapper.selectBatchIds(normalized).stream()
                .collect(Collectors.toMap(ContentAsset::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private List<Long> normalizeIds(Collection<Long> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private Map<Long, List<IndoorNodeBehavior>> loadBehaviorMap(Collection<Long> nodeIds) {
        List<Long> normalized = normalizeIds(nodeIds);
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        return indoorNodeBehaviorMapper.selectList(new LambdaQueryWrapper<IndoorNodeBehavior>()
                        .in(IndoorNodeBehavior::getNodeId, normalized)
                        .orderByAsc(IndoorNodeBehavior::getSortOrder)
                        .orderByAsc(IndoorNodeBehavior::getId))
                .stream()
                .collect(Collectors.groupingBy(IndoorNodeBehavior::getNodeId, LinkedHashMap::new, Collectors.toList()));
    }

    private String resolveAssetUrl(Long assetId) {
        if (assetId == null) {
            return null;
        }
        ContentAsset asset = contentAssetMapper.selectById(assetId);
        return asset == null ? null : asset.getCanonicalUrl();
    }

    private String resolveLegacyIconValue(String currentIcon, Long assetId) {
        if (assetId != null) {
            return null;
        }
        if (!StringUtils.hasText(currentIcon)) {
            return null;
        }
        String trimmed = currentIcon.trim();
        return trimmed.length() > 128 ? null : trimmed;
    }

    private String resolveAssetUrl(Map<Long, ContentAsset> assetMap, Long assetId, String fallback) {
        if (assetId == null) {
            return fallback;
        }
        ContentAsset asset = assetMap.get(assetId);
        return asset == null || !StringUtils.hasText(asset.getCanonicalUrl()) ? fallback : asset.getCanonicalUrl();
    }

    private AdminIndoorNodeResponse getNodeResponse(Long nodeId) {
        IndoorNode node = requireNode(nodeId);
        Map<Long, ContentAsset> assetMap = loadAssetMap(Stream.of(node.getIconAssetId(), node.getAnimationAssetId())
                .filter(Objects::nonNull)
                .toList());
        Map<Long, List<IndoorNodeBehavior>> behaviorMap = loadBehaviorMap(List.of(nodeId));
        Map<Long, List<RewardRuleBinding>> rewardRuleBindingsByBehaviorId = loadBehaviorRewardRuleBindings(
                behaviorMap.values().stream()
                        .flatMap(List::stream)
                        .map(IndoorNodeBehavior::getId)
                        .filter(Objects::nonNull)
                        .toList()
        );
        return toNodeResponse(node, assetMap, behaviorMap, rewardRuleBindingsByBehaviorId);
    }

    private AdminIndoorNodeResponse toNodeResponse(
            IndoorNode node,
            Map<Long, ContentAsset> assetMap,
            Map<Long, List<IndoorNodeBehavior>> behaviorMap,
            Map<Long, List<RewardRuleBinding>> rewardRuleBindingsByBehaviorId
    ) {
        return AdminIndoorNodeResponse.builder()
                .id(node.getId())
                .buildingId(node.getBuildingId())
                .floorId(node.getFloorId())
                .markerCode(node.getMarkerCode())
                .nodeType(node.getNodeType())
                .presentationMode(node.getPresentationMode())
                .overlayType(node.getOverlayType())
                .nodeNameZh(node.getNodeNameZh())
                .nodeNameEn(node.getNodeNameEn())
                .nodeNameZht(node.getNodeNameZht())
                .nodeNamePt(node.getNodeNamePt())
                .descriptionZh(node.getDescriptionZh())
                .descriptionEn(node.getDescriptionEn())
                .descriptionZht(node.getDescriptionZht())
                .descriptionPt(node.getDescriptionPt())
                .relativeX(node.getRelativeX())
                .relativeY(node.getRelativeY())
                .relatedPoiId(node.getRelatedPoiId())
                .iconAssetId(node.getIconAssetId())
                .iconUrl(resolveAssetUrl(assetMap, node.getIconAssetId(), node.getIcon()))
                .animationAssetId(node.getAnimationAssetId())
                .animationUrl(resolveAssetUrl(assetMap, node.getAnimationAssetId(), null))
                .linkedEntityType(node.getLinkedEntityType())
                .linkedEntityId(node.getLinkedEntityId())
                .tags(parseTags(node.getTags()))
                .tagsJson(node.getTags())
                .popupConfigJson(node.getPopupConfigJson())
                .displayConfigJson(node.getDisplayConfigJson())
                .overlayGeometry(readJson(node.getOverlayGeometryJson(), AdminIndoorNodeBehaviorPayload.OverlayGeometry.class))
                .overlayGeometryJson(node.getOverlayGeometryJson())
                .inheritLinkedEntityRules(Boolean.TRUE.equals(node.getInheritLinkedEntityRules()))
                .runtimeSupportLevel(node.getRuntimeSupportLevel())
                .metadataJson(node.getMetadataJson())
                .importBatchId(node.getImportBatchId())
                .sortOrder(node.getSortOrder())
                .status(node.getStatus())
                .behaviors(behaviorMap.getOrDefault(node.getId(), Collections.emptyList()).stream()
                        .map(behavior -> toBehaviorPayload(
                                behavior,
                                rewardRuleBindingsByBehaviorId.getOrDefault(behavior.getId(), Collections.emptyList())
                        ))
                        .toList())
                .createdAt(node.getCreatedAt())
                .updatedAt(node.getUpdatedAt())
                .build();
    }

    private AdminIndoorNodeBehaviorPayload toBehaviorPayload(
            IndoorNodeBehavior behavior,
            List<RewardRuleBinding> rewardRuleBindings
    ) {
        AdminIndoorNodeBehaviorPayload payload = new AdminIndoorNodeBehaviorPayload();
        payload.setBehaviorCode(behavior.getBehaviorCode());
        payload.setBehaviorNameZh(behavior.getBehaviorNameZh());
        payload.setBehaviorNameEn(behavior.getBehaviorNameEn());
        payload.setBehaviorNameZht(behavior.getBehaviorNameZht());
        payload.setBehaviorNamePt(behavior.getBehaviorNamePt());
        payload.setAppearancePresetCode(behavior.getAppearancePresetCode());
        payload.setTriggerTemplateCode(behavior.getTriggerTemplateCode());
        payload.setEffectTemplateCode(behavior.getEffectTemplateCode());
        payload.setAppearanceRules(readJson(behavior.getAppearanceRulesJson(), new TypeReference<List<AdminIndoorNodeBehaviorPayload.RuleCondition>>() {
        }, new ArrayList<>()));
        payload.setTriggerRules(readJson(behavior.getTriggerRulesJson(), new TypeReference<List<AdminIndoorNodeBehaviorPayload.TriggerStep>>() {
        }, new ArrayList<>()));
        payload.setEffectRules(readJson(behavior.getEffectRulesJson(), new TypeReference<List<AdminIndoorNodeBehaviorPayload.EffectDefinition>>() {
        }, new ArrayList<>()));
        payload.setRewardRuleIds(rewardRuleBindings == null
                ? new ArrayList<>()
                : rewardRuleBindings.stream()
                .map(RewardRuleBinding::getRuleId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new)));
        payload.setPathGraph(readJson(behavior.getPathGraphJson(), AdminIndoorNodeBehaviorPayload.PathGraph.class));
        payload.setOverlayGeometry(readJson(behavior.getOverlayGeometryJson(), AdminIndoorNodeBehaviorPayload.OverlayGeometry.class));
        payload.setInheritMode(behavior.getInheritMode());
        payload.setRuntimeSupportLevel(behavior.getRuntimeSupportLevel());
        payload.setSortOrder(behavior.getSortOrder());
        payload.setStatus(behavior.getStatus());
        return payload;
    }

    private AdminIndoorMarkerResponse toMarkerResponse(AdminIndoorNodeResponse node) {
        return AdminIndoorMarkerResponse.builder()
                .id(node.getId())
                .buildingId(node.getBuildingId())
                .floorId(node.getFloorId())
                .markerCode(node.getMarkerCode())
                .nodeType(node.getNodeType())
                .nodeNameZh(node.getNodeNameZh())
                .nodeNameEn(node.getNodeNameEn())
                .nodeNameZht(node.getNodeNameZht())
                .nodeNamePt(node.getNodeNamePt())
                .descriptionZh(node.getDescriptionZh())
                .descriptionEn(node.getDescriptionEn())
                .descriptionZht(node.getDescriptionZht())
                .descriptionPt(node.getDescriptionPt())
                .relativeX(node.getRelativeX())
                .relativeY(node.getRelativeY())
                .relatedPoiId(node.getRelatedPoiId())
                .iconAssetId(node.getIconAssetId())
                .iconUrl(node.getIconUrl())
                .animationAssetId(node.getAnimationAssetId())
                .animationUrl(node.getAnimationUrl())
                .linkedEntityType(node.getLinkedEntityType())
                .linkedEntityId(node.getLinkedEntityId())
                .tagsJson(node.getTagsJson())
                .popupConfigJson(node.getPopupConfigJson())
                .displayConfigJson(node.getDisplayConfigJson())
                .metadataJson(node.getMetadataJson())
                .importBatchId(node.getImportBatchId())
                .sortOrder(node.getSortOrder())
                .status(node.getStatus())
                .createdAt(node.getCreatedAt())
                .updatedAt(node.getUpdatedAt())
                .build();
    }

    private AdminIndoorNodeUpsertRequest toNodeRequest(AdminIndoorMarkerUpsertRequest request) {
        AdminIndoorNodeUpsertRequest nodeRequest = new AdminIndoorNodeUpsertRequest();
        if (request == null) {
            return nodeRequest;
        }
        nodeRequest.setMarkerCode(request.getMarkerCode());
        nodeRequest.setNodeType(request.getNodeType());
        nodeRequest.setPresentationMode("marker");
        nodeRequest.setNodeNameZh(request.getNodeNameZh());
        nodeRequest.setNodeNameEn(request.getNodeNameEn());
        nodeRequest.setNodeNameZht(request.getNodeNameZht());
        nodeRequest.setNodeNamePt(request.getNodeNamePt());
        nodeRequest.setDescriptionZh(request.getDescriptionZh());
        nodeRequest.setDescriptionEn(request.getDescriptionEn());
        nodeRequest.setDescriptionZht(request.getDescriptionZht());
        nodeRequest.setDescriptionPt(request.getDescriptionPt());
        nodeRequest.setRelativeX(request.getRelativeX());
        nodeRequest.setRelativeY(request.getRelativeY());
        nodeRequest.setRelatedPoiId(request.getRelatedPoiId());
        nodeRequest.setIconAssetId(request.getIconAssetId());
        nodeRequest.setAnimationAssetId(request.getAnimationAssetId());
        nodeRequest.setLinkedEntityType(request.getLinkedEntityType());
        nodeRequest.setLinkedEntityId(request.getLinkedEntityId());
        nodeRequest.setTagsJson(request.getTagsJson());
        nodeRequest.setPopupConfigJson(request.getPopupConfigJson());
        nodeRequest.setDisplayConfigJson(request.getDisplayConfigJson());
        nodeRequest.setMetadataJson(request.getMetadataJson());
        nodeRequest.setInheritLinkedEntityRules(Boolean.FALSE);
        nodeRequest.setRuntimeSupportLevel("phase15_storage_only");
        nodeRequest.setSortOrder(request.getSortOrder());
        nodeRequest.setStatus(request.getStatus());
        return nodeRequest;
    }

    private String resolvePresentationMode(AdminIndoorNodeUpsertRequest request) {
        if (request == null) {
            return "marker";
        }
        if (StringUtils.hasText(request.getPresentationMode())) {
            return request.getPresentationMode().trim().toLowerCase(Locale.ROOT);
        }
        if (StringUtils.hasText(request.getOverlayType()) || request.getOverlayGeometry() != null) {
            return "overlay";
        }
        return "marker";
    }

    private void validateBehaviorPayloads(
            List<AdminIndoorNodeBehaviorPayload> behaviors,
            List<String> errors,
            List<String> warnings
    ) {
        if (behaviors == null || behaviors.isEmpty()) {
            return;
        }
        Map<Long, RewardRule> rewardRuleMap = loadRewardRuleMap(behaviors.stream()
                .filter(Objects::nonNull)
                .map(AdminIndoorNodeBehaviorPayload::getRewardRuleIds)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .toList());
        Set<String> behaviorCodes = new LinkedHashSet<>();
        for (int index = 0; index < behaviors.size(); index++) {
            AdminIndoorNodeBehaviorPayload behavior = behaviors.get(index);
            if (behavior == null) {
                errors.add("behaviors[" + index + "] must not be null");
                continue;
            }
            String prefix = "behaviors[" + index + "]";
            String behaviorCode = textOrNull(behavior.getBehaviorCode());
            if (StringUtils.hasText(behaviorCode) && !behaviorCodes.add(behaviorCode)) {
                errors.add(prefix + ".behaviorCode is duplicated");
            }
            if (StringUtils.hasText(behavior.getInheritMode()) && !ALLOWED_INHERIT_MODES.contains(behavior.getInheritMode().trim())) {
                errors.add(prefix + ".inheritMode is not supported");
            }
            normalizeRuntimeSupportLevel(behavior.getRuntimeSupportLevel(), errors, prefix + ".runtimeSupportLevel");
            validateRuleConditions(behavior.getAppearanceRules(), ALLOWED_APPEARANCE_CATEGORIES, prefix + ".appearanceRules", errors);
            validateTriggerRules(behavior.getTriggerRules(), prefix + ".triggerRules", errors);
            validateEffectRules(behavior.getEffectRules(), prefix + ".effectRules", errors);
            validatePathGraph(behavior.getPathGraph(), prefix + ".pathGraph", errors);
            validateBehaviorOverlayGeometry(behavior.getOverlayGeometry(), prefix + ".overlayGeometry", errors);
            List<Long> normalizedRewardRuleIds = normalizeIds(behavior.getRewardRuleIds());
            if (behavior.getRewardRuleIds() != null
                    && normalizedRewardRuleIds.size() != behavior.getRewardRuleIds().stream().filter(Objects::nonNull).count()) {
                errors.add(prefix + ".rewardRuleIds contains duplicated or invalid ids");
            }
            for (Long rewardRuleId : normalizedRewardRuleIds) {
                if (!rewardRuleMap.containsKey(rewardRuleId)) {
                    errors.add(prefix + ".rewardRuleIds contains unknown reward rule id: " + rewardRuleId);
                }
            }
            boolean hasPathMotion = behavior.getEffectRules() != null && behavior.getEffectRules().stream()
                    .filter(Objects::nonNull)
                    .map(AdminIndoorNodeBehaviorPayload.EffectDefinition::getCategory)
                    .filter(StringUtils::hasText)
                    .map(value -> value.trim().toLowerCase(Locale.ROOT))
                    .anyMatch("path_motion"::equals);
            if (hasPathMotion && behavior.getPathGraph() == null) {
                errors.add(prefix + ".pathGraph is required when an effect uses path_motion");
            }
            boolean hasVoicePlaceholder = behavior.getTriggerRules() != null && behavior.getTriggerRules().stream()
                    .filter(Objects::nonNull)
                    .map(AdminIndoorNodeBehaviorPayload.TriggerStep::getCategory)
                    .filter(StringUtils::hasText)
                    .map(value -> value.trim().toLowerCase(Locale.ROOT))
                    .anyMatch("voice_placeholder"::equals);
            if (hasVoicePlaceholder) {
                warnings.add(prefix + " includes voice_placeholder and will remain stored-only until runtime support lands.");
            }
        }
    }

    private void validateRuleConditions(
            List<AdminIndoorNodeBehaviorPayload.RuleCondition> rules,
            Set<String> allowedCategories,
            String prefix,
            List<String> errors
    ) {
        if (rules == null) {
            return;
        }
        for (int index = 0; index < rules.size(); index++) {
            AdminIndoorNodeBehaviorPayload.RuleCondition rule = rules.get(index);
            if (rule == null) {
                errors.add(prefix + "[" + index + "] must not be null");
                continue;
            }
            String category = textOrNull(rule.getCategory());
            if (!StringUtils.hasText(category) || !allowedCategories.contains(category.toLowerCase(Locale.ROOT))) {
                errors.add(prefix + "[" + index + "].category is not supported");
            }
        }
    }

    private void validateTriggerRules(
            List<AdminIndoorNodeBehaviorPayload.TriggerStep> triggerRules,
            String prefix,
            List<String> errors
    ) {
        if (triggerRules == null) {
            return;
        }
        Set<String> triggerIds = new LinkedHashSet<>();
        for (int index = 0; index < triggerRules.size(); index++) {
            AdminIndoorNodeBehaviorPayload.TriggerStep rule = triggerRules.get(index);
            if (rule == null) {
                errors.add(prefix + "[" + index + "] must not be null");
                continue;
            }
            String category = textOrNull(rule.getCategory());
            if (!StringUtils.hasText(category) || !ALLOWED_TRIGGER_CATEGORIES.contains(category.toLowerCase(Locale.ROOT))) {
                errors.add(prefix + "[" + index + "].category is not supported");
            }
            String triggerId = textOrNull(rule.getId());
            if (StringUtils.hasText(triggerId) && !triggerIds.add(triggerId)) {
                errors.add(prefix + "[" + index + "].id is duplicated");
            }
        }
        for (int index = 0; index < triggerRules.size(); index++) {
            AdminIndoorNodeBehaviorPayload.TriggerStep rule = triggerRules.get(index);
            if (rule == null) {
                continue;
            }
            String dependsOnTriggerId = textOrNull(rule.getDependsOnTriggerId());
            if (StringUtils.hasText(dependsOnTriggerId) && !triggerIds.contains(dependsOnTriggerId)) {
                errors.add(prefix + "[" + index + "].dependsOnTriggerId references a missing trigger id");
            }
        }
    }

    private void validateEffectRules(
            List<AdminIndoorNodeBehaviorPayload.EffectDefinition> effectRules,
            String prefix,
            List<String> errors
    ) {
        if (effectRules == null) {
            return;
        }
        for (int index = 0; index < effectRules.size(); index++) {
            AdminIndoorNodeBehaviorPayload.EffectDefinition rule = effectRules.get(index);
            if (rule == null) {
                errors.add(prefix + "[" + index + "] must not be null");
                continue;
            }
            String category = textOrNull(rule.getCategory());
            if (!StringUtils.hasText(category) || !ALLOWED_EFFECT_CATEGORIES.contains(category.toLowerCase(Locale.ROOT))) {
                errors.add(prefix + "[" + index + "].category is not supported");
            }
        }
    }

    private void validatePathGraph(
            AdminIndoorNodeBehaviorPayload.PathGraph pathGraph,
            String prefix,
            List<String> errors
    ) {
        if (pathGraph == null) {
            return;
        }
        List<AdminIndoorNodeBehaviorPayload.CoordinatePoint> points = pathGraph.getPoints() == null
                ? Collections.emptyList()
                : pathGraph.getPoints();
        if (points.size() < 2) {
            errors.add(prefix + ".points must contain at least two points");
        }
        for (int index = 0; index < points.size(); index++) {
            AdminIndoorNodeBehaviorPayload.CoordinatePoint point = points.get(index);
            if (point == null) {
                errors.add(prefix + ".points[" + index + "] must not be null");
                continue;
            }
            validateRelative(point.getX(), prefix + ".points[" + index + "].x", errors);
            validateRelative(point.getY(), prefix + ".points[" + index + "].y", errors);
        }
        if (pathGraph.getDurationMs() != null && pathGraph.getDurationMs() <= 0) {
            errors.add(prefix + ".durationMs must be greater than 0");
        }
        if (pathGraph.getHoldMs() != null && pathGraph.getHoldMs() < 0) {
            errors.add(prefix + ".holdMs must be greater than or equal to 0");
        }
    }

    private void validateBehaviorOverlayGeometry(
            AdminIndoorNodeBehaviorPayload.OverlayGeometry overlayGeometry,
            String prefix,
            List<String> errors
    ) {
        if (overlayGeometry == null) {
            return;
        }
        String geometryType = textOrNull(overlayGeometry.getGeometryType());
        if (!StringUtils.hasText(geometryType) || !ALLOWED_OVERLAY_TYPES.contains(geometryType.toLowerCase(Locale.ROOT))) {
            errors.add(prefix + ".geometryType is not supported");
            return;
        }
        List<AdminIndoorNodeBehaviorPayload.CoordinatePoint> points = overlayGeometry.getPoints() == null
                ? Collections.emptyList()
                : overlayGeometry.getPoints();
        if ("point".equalsIgnoreCase(geometryType) && points.size() < 1) {
            errors.add(prefix + ".points must contain one point for point overlays");
        } else if ("polyline".equalsIgnoreCase(geometryType) && points.size() < 2) {
            errors.add(prefix + ".points must contain at least two points for polyline overlays");
        } else if ("polygon".equalsIgnoreCase(geometryType) && points.size() < 3) {
            errors.add(prefix + ".points must contain at least three points for polygon overlays");
        }
        for (int index = 0; index < points.size(); index++) {
            AdminIndoorNodeBehaviorPayload.CoordinatePoint point = points.get(index);
            if (point == null) {
                errors.add(prefix + ".points[" + index + "] must not be null");
                continue;
            }
            validateRelative(point.getX(), prefix + ".points[" + index + "].x", errors);
            validateRelative(point.getY(), prefix + ".points[" + index + "].y", errors);
        }
    }

    private ResolvedOverlay resolveOverlay(
            AdminIndoorNodeUpsertRequest request,
            String resolvedPresentationMode,
            List<String> errors
    ) {
        String resolvedOverlayType = textOrNull(request.getOverlayType());
        AdminIndoorNodeBehaviorPayload.OverlayGeometry overlayGeometry = request.getOverlayGeometry();
        if (!StringUtils.hasText(resolvedOverlayType) && overlayGeometry != null && StringUtils.hasText(overlayGeometry.getGeometryType())) {
            resolvedOverlayType = overlayGeometry.getGeometryType().trim().toLowerCase(Locale.ROOT);
        }

        boolean overlayRequested = !"marker".equals(resolvedPresentationMode);
        if ("marker".equals(resolvedPresentationMode) && (StringUtils.hasText(resolvedOverlayType) || overlayGeometry != null)) {
            errors.add("overlayType and overlayGeometry are not allowed when presentationMode is marker");
            return new ResolvedOverlay(null, null, null, null);
        }

        if (overlayRequested && !StringUtils.hasText(resolvedOverlayType) && overlayGeometry == null
                && request.getRelativeX() != null && request.getRelativeY() != null) {
            resolvedOverlayType = "point";
            overlayGeometry = buildPointOverlayGeometry(request.getRelativeX(), request.getRelativeY());
        }

        if (StringUtils.hasText(resolvedOverlayType) && !ALLOWED_OVERLAY_TYPES.contains(resolvedOverlayType)) {
            errors.add("overlayType is not supported");
            return new ResolvedOverlay(null, null, null, null);
        }

        if (overlayRequested && !StringUtils.hasText(resolvedOverlayType)) {
            errors.add("overlayType is required when presentationMode is overlay or hybrid");
            return new ResolvedOverlay(null, null, null, null);
        }

        if (!overlayRequested) {
            return new ResolvedOverlay(null, null, null, null);
        }

        if (overlayGeometry == null) {
            overlayGeometry = new AdminIndoorNodeBehaviorPayload.OverlayGeometry();
            overlayGeometry.setGeometryType(resolvedOverlayType);
            if ("point".equals(resolvedOverlayType) && request.getRelativeX() != null && request.getRelativeY() != null) {
                overlayGeometry.setPoints(List.of(toPoint(request.getRelativeX(), request.getRelativeY(), 0)));
            }
        }

        List<AdminIndoorNodeBehaviorPayload.CoordinatePoint> points = overlayGeometry.getPoints() == null
                ? new ArrayList<>()
                : overlayGeometry.getPoints().stream()
                .filter(Objects::nonNull)
                .map(point -> toPoint(point.getX(), point.getY(), point.getOrder()))
                .toList();
        if ("point".equals(resolvedOverlayType) && points.size() < 1) {
            errors.add("overlayGeometry.points must contain one point for point overlays");
        } else if ("polyline".equals(resolvedOverlayType) && points.size() < 2) {
            errors.add("overlayGeometry.points must contain at least two points for polyline overlays");
        } else if ("polygon".equals(resolvedOverlayType) && points.size() < 3) {
            errors.add("overlayGeometry.points must contain at least three points for polygon overlays");
        }
        for (int index = 0; index < points.size(); index++) {
            AdminIndoorNodeBehaviorPayload.CoordinatePoint point = points.get(index);
            validateRelative(point.getX(), "overlayGeometry.points[" + index + "].x", errors);
            validateRelative(point.getY(), "overlayGeometry.points[" + index + "].y", errors);
        }
        AdminIndoorNodeBehaviorPayload.OverlayGeometry normalizedGeometry = new AdminIndoorNodeBehaviorPayload.OverlayGeometry();
        normalizedGeometry.setGeometryType(resolvedOverlayType);
        normalizedGeometry.setPoints(points);
        normalizedGeometry.setProperties(overlayGeometry.getProperties());
        BigDecimal anchorX = null;
        BigDecimal anchorY = null;
        if (!points.isEmpty()) {
            if ("point".equals(resolvedOverlayType)) {
                anchorX = points.get(0).getX();
                anchorY = points.get(0).getY();
            } else {
                BigDecimal sumX = BigDecimal.ZERO;
                BigDecimal sumY = BigDecimal.ZERO;
                for (AdminIndoorNodeBehaviorPayload.CoordinatePoint point : points) {
                    sumX = sumX.add(point.getX());
                    sumY = sumY.add(point.getY());
                }
                BigDecimal size = BigDecimal.valueOf(points.size());
                anchorX = normalizeRelative(sumX.divide(size, 6, RoundingMode.HALF_UP));
                anchorY = normalizeRelative(sumY.divide(size, 6, RoundingMode.HALF_UP));
            }
        }
        return new ResolvedOverlay(
                resolvedOverlayType,
                writeJson(normalizedGeometry),
                anchorX,
                anchorY
        );
    }

    private String normalizeRuntimeSupportLevel(String runtimeSupportLevel, List<String> errors, String fieldName) {
        String resolved = StringUtils.hasText(runtimeSupportLevel) ? runtimeSupportLevel.trim() : "phase15_storage_only";
        if (!ALLOWED_RUNTIME_SUPPORT_LEVELS.contains(resolved)) {
            errors.add(fieldName + " is not supported");
        }
        return resolved;
    }

    private String normalizeTagsJson(List<String> tags, String tagsJson, List<String> errors) {
        if (tags != null && !tags.isEmpty()) {
            return writeJson(tags.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .toList());
        }
        requireJson(tagsJson, "tagsJson", errors);
        return textOrNull(tagsJson);
    }

    private List<String> parseTags(String tagsJson) {
        if (!StringUtils.hasText(tagsJson)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException ex) {
            return Collections.emptyList();
        }
    }

    private AdminIndoorNodeBehaviorPayload.OverlayGeometry buildPointOverlayGeometry(BigDecimal x, BigDecimal y) {
        AdminIndoorNodeBehaviorPayload.OverlayGeometry geometry = new AdminIndoorNodeBehaviorPayload.OverlayGeometry();
        geometry.setGeometryType("point");
        geometry.setPoints(List.of(toPoint(x, y, 0)));
        return geometry;
    }

    private AdminIndoorNodeBehaviorPayload.CoordinatePoint toPoint(BigDecimal x, BigDecimal y, Integer order) {
        AdminIndoorNodeBehaviorPayload.CoordinatePoint point = new AdminIndoorNodeBehaviorPayload.CoordinatePoint();
        point.setX(normalizeRelative(x));
        point.setY(normalizeRelative(y));
        point.setOrder(order);
        return point;
    }

    private void syncBehaviors(Long nodeId, List<AdminIndoorNodeBehaviorPayload> behaviors) {
        clearBehaviorRewardRuleBindings(loadBehaviorIdsByNode(nodeId));
        indoorNodeBehaviorMapper.delete(new LambdaQueryWrapper<IndoorNodeBehavior>()
                .eq(IndoorNodeBehavior::getNodeId, nodeId));
        if (behaviors == null || behaviors.isEmpty()) {
            return;
        }
        int fallbackIndex = 0;
        for (AdminIndoorNodeBehaviorPayload payload : behaviors) {
            if (payload == null) {
                fallbackIndex++;
                continue;
            }
            IndoorNodeBehavior behavior = new IndoorNodeBehavior();
            behavior.setNodeId(nodeId);
            behavior.setBehaviorCode(StringUtils.hasText(payload.getBehaviorCode())
                    ? payload.getBehaviorCode().trim()
                    : String.format(Locale.ROOT, "node-%d-behavior-%d", nodeId, fallbackIndex));
            behavior.setBehaviorNameZh(textOrNull(payload.getBehaviorNameZh()));
            behavior.setBehaviorNameEn(textOrNull(payload.getBehaviorNameEn()));
            behavior.setBehaviorNameZht(textOrNull(payload.getBehaviorNameZht()));
            behavior.setBehaviorNamePt(textOrNull(payload.getBehaviorNamePt()));
            behavior.setAppearancePresetCode(textOrNull(payload.getAppearancePresetCode()));
            behavior.setTriggerTemplateCode(textOrNull(payload.getTriggerTemplateCode()));
            behavior.setEffectTemplateCode(textOrNull(payload.getEffectTemplateCode()));
            behavior.setAppearanceRulesJson(writeJsonOrNull(payload.getAppearanceRules()));
            behavior.setTriggerRulesJson(writeJsonOrNull(payload.getTriggerRules()));
            behavior.setEffectRulesJson(writeJsonOrNull(payload.getEffectRules()));
            behavior.setPathGraphJson(writeJsonOrNull(payload.getPathGraph()));
            behavior.setOverlayGeometryJson(writeJsonOrNull(payload.getOverlayGeometry()));
            behavior.setInheritMode(StringUtils.hasText(payload.getInheritMode()) ? payload.getInheritMode().trim() : "override");
            behavior.setRuntimeSupportLevel(StringUtils.hasText(payload.getRuntimeSupportLevel())
                    ? payload.getRuntimeSupportLevel().trim()
                    : "phase15_storage_only");
            behavior.setSortOrder(payload.getSortOrder() == null ? fallbackIndex : payload.getSortOrder());
            behavior.setStatus(StringUtils.hasText(payload.getStatus()) ? payload.getStatus().trim() : "draft");
            indoorNodeBehaviorMapper.insert(behavior);
            if (behavior.getId() == null) {
                throw new BusinessException(5003, "Failed to persist indoor behavior before syncing reward bindings");
            }
            syncBehaviorRewardRuleBindings(behavior.getId(), behavior.getBehaviorCode(), payload.getRewardRuleIds());
            fallbackIndex++;
        }
    }

    private List<Long> loadBehaviorIdsByNode(Long nodeId) {
        if (nodeId == null) {
            return Collections.emptyList();
        }
        return indoorNodeBehaviorMapper.selectList(new LambdaQueryWrapper<IndoorNodeBehavior>()
                        .eq(IndoorNodeBehavior::getNodeId, nodeId))
                .stream()
                .map(IndoorNodeBehavior::getId)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<Long, RewardRule> loadRewardRuleMap(Collection<Long> ids) {
        List<Long> normalizedIds = normalizeIds(ids);
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return rewardRuleMapper.selectBatchIds(normalizedIds).stream()
                .collect(Collectors.toMap(RewardRule::getId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, List<RewardRuleBinding>> loadBehaviorRewardRuleBindings(Collection<Long> behaviorIds) {
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

    private void clearBehaviorRewardRuleBindings(Collection<Long> behaviorIds) {
        List<Long> normalizedIds = normalizeIds(behaviorIds);
        if (normalizedIds.isEmpty()) {
            return;
        }
        rewardRuleBindingMapper.delete(new LambdaQueryWrapper<RewardRuleBinding>()
                .eq(RewardRuleBinding::getOwnerDomain, OWNER_TYPE_INDOOR_BEHAVIOR)
                .in(RewardRuleBinding::getOwnerId, normalizedIds));
    }

    private void syncBehaviorRewardRuleBindings(Long behaviorId, String behaviorCode, List<Long> rewardRuleIds) {
        List<Long> normalizedRuleIds = normalizeIds(rewardRuleIds);
        List<RewardRuleBinding> existing = rewardRuleBindingMapper.selectList(new LambdaQueryWrapper<RewardRuleBinding>()
                .eq(RewardRuleBinding::getOwnerDomain, OWNER_TYPE_INDOOR_BEHAVIOR)
                .eq(RewardRuleBinding::getOwnerId, behaviorId));
        Map<Long, RewardRuleBinding> existingByRuleId = existing.stream()
                .collect(Collectors.toMap(RewardRuleBinding::getRuleId, Function.identity(), (left, right) -> left, LinkedHashMap::new));
        Set<Long> retainedRuleIds = new LinkedHashSet<>(normalizedRuleIds);
        for (RewardRuleBinding binding : existing) {
            if (binding.getRuleId() != null && !retainedRuleIds.contains(binding.getRuleId())) {
                rewardRuleBindingMapper.deleteById(binding.getId());
            }
        }
        for (int index = 0; index < normalizedRuleIds.size(); index++) {
            Long rewardRuleId = normalizedRuleIds.get(index);
            RewardRuleBinding binding = existingByRuleId.get(rewardRuleId);
            if (binding == null) {
                binding = new RewardRuleBinding();
                binding.setRuleId(rewardRuleId);
                binding.setOwnerDomain(OWNER_TYPE_INDOOR_BEHAVIOR);
                binding.setOwnerId(behaviorId);
                binding.setOwnerCode(behaviorCode);
                binding.setBindingRole(REWARD_BINDING_ROLE_ATTACHED);
                binding.setSortOrder(index);
                rewardRuleBindingMapper.insert(binding);
                continue;
            }
            binding.setOwnerCode(behaviorCode);
            binding.setBindingRole(REWARD_BINDING_ROLE_ATTACHED);
            binding.setSortOrder(index);
            rewardRuleBindingMapper.updateById(binding);
        }
    }

    private BigDecimal normalizeRelative(BigDecimal value) {
        return value == null ? null : value.setScale(6, RoundingMode.HALF_UP);
    }

    private void validateRelative(BigDecimal value, String fieldName, List<String> errors) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(BigDecimal.ONE) > 0) {
            errors.add(fieldName + " must be between 0 and 1");
        }
    }

    private String textOrNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String textOrDefault(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private BigDecimal parseBigDecimal(String value, String fieldName, List<String> errors) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            errors.add(fieldName + " is not a valid decimal");
            return null;
        }
    }

    private Long parseLong(String value, String fieldName, List<String> errors) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            errors.add(fieldName + " is not a valid integer");
            return null;
        }
    }

    private Integer parseInteger(String value, String fieldName, List<String> errors) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            errors.add(fieldName + " is not a valid integer");
            return null;
        }
    }

    private String toPlainString(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().toPlainString();
    }

    private String fileName(MultipartFile file) {
        return StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename().trim() : "markers.csv";
    }

    private void requireJson(String value, String fieldName, List<String> errors) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        try {
            objectMapper.readTree(value);
        } catch (JsonProcessingException ex) {
            errors.add(fieldName + " must be valid JSON");
        }
    }

    private <T> T readJson(String value, Class<T> type) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(value, type);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }

    private <T> T readJson(String value, TypeReference<T> typeReference, T fallback) {
        if (!StringUtils.hasText(value)) {
            return fallback;
        }
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (JsonProcessingException ex) {
            return fallback;
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new BusinessException(5003, "Failed to serialize indoor authoring payload");
        }
    }

    private String writeJsonOrNull(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Collection<?> collection && collection.isEmpty()) {
            return null;
        }
        return writeJson(value);
    }

    private record NodeValidationContext(
            BigDecimal relativeX,
            BigDecimal relativeY,
            String presentationMode,
            String overlayType,
            String overlayGeometryJson,
            String tagsJson,
            String runtimeSupportLevel,
            List<String> warnings
    ) {
    }

    private record ResolvedOverlay(
            String overlayType,
            String overlayGeometryJson,
            BigDecimal anchorX,
            BigDecimal anchorY
    ) {
    }
}
