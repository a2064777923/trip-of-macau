package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationResult;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationService;
import com.aoxiaoyou.admin.dto.request.AdminBuildingUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorFloorUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorMarkerCsvConfirmRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorMarkerUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorNodeUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminSpatialAssetLinkUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminIndoorBuildingDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorFloorResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorMarkerCsvImportResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorMarkerCsvPreviewResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorMarkerResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorNodeResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleValidationResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorTilePreviewResponse;
import com.aoxiaoyou.admin.dto.response.AdminSpatialAssetLinkResponse;
import com.aoxiaoyou.admin.dto.response.BuildingResponse;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.IndoorFloor;
import com.aoxiaoyou.admin.entity.Poi;
import com.aoxiaoyou.admin.entity.SubMap;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.IndoorFloorMapper;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminContentRelationService;
import com.aoxiaoyou.admin.service.AdminIndoorService;
import com.aoxiaoyou.admin.service.AdminSpatialAssetLinkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminIndoorServiceImpl implements AdminIndoorService {

    private static final String BUILDING_ATTACHMENT_OWNER = "indoor_building";
    private static final String FLOOR_ATTACHMENT_OWNER = "indoor_floor";
    private static final String ATTACHMENT_RELATION = "attachment_asset";
    private static final String CONTENT_ASSET_TARGET = "content_asset";

    private final BuildingMapper buildingMapper;
    private final IndoorFloorMapper indoorFloorMapper;
    private final CityMapper cityMapper;
    private final SubMapMapper subMapMapper;
    private final PoiMapper poiMapper;
    private final ContentAssetMapper contentAssetMapper;
    private final AdminContentRelationService adminContentRelationService;
    private final AdminSpatialAssetLinkService adminSpatialAssetLinkService;
    private final CoordinateNormalizationService coordinateNormalizationService;
    private final IndoorTilePipelineService indoorTilePipelineService;
    private final IndoorMarkerAuthoringService indoorMarkerAuthoringService;

    @Override
    public PageResponse<BuildingResponse> pageBuildings(long pageNum, long pageSize, String keyword, Long cityId, Long subMapId, Long poiId, String status) {
        Page<Building> page = buildingMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Building>()
                        .eq(cityId != null, Building::getCityId, cityId)
                        .eq(subMapId != null, Building::getSubMapId, subMapId)
                        .eq(poiId != null, Building::getPoiId, poiId)
                        .eq(StringUtils.hasText(status), Building::getStatus, status)
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper
                                .like(Building::getBuildingCode, keyword)
                                .or().like(Building::getNameZh, keyword)
                                .or().like(Building::getNameZht, keyword)
                                .or().like(Building::getNameEn, keyword)
                                .or().like(Building::getNamePt, keyword)
                                .or().like(Building::getAddressZh, keyword))
                        .orderByAsc(Building::getSortOrder)
                        .orderByAsc(Building::getId)
        );

        List<Building> buildings = page.getRecords();
        Map<Long, City> cityMap = loadCityMap(buildings.stream().map(Building::getCityId).toList());
        Map<Long, SubMap> subMapMap = loadSubMapMap(buildings.stream().map(Building::getSubMapId).toList());
        Map<Long, Poi> poiMap = loadPoiMap(buildings.stream().map(Building::getPoiId).toList());
        Map<Long, Integer> floorCountMap = loadFloorCountMap(buildings.stream().map(Building::getId).toList());

        Page<BuildingResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(buildings.stream()
                .map(building -> toBuildingResponse(
                        building,
                        cityMap.get(building.getCityId()),
                        subMapMap.get(building.getSubMapId()),
                        poiMap.get(building.getPoiId()),
                        floorCountMap.getOrDefault(building.getId(), 0)))
                .toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminIndoorBuildingDetailResponse getBuildingDetail(Long id) {
        return toBuildingDetail(requireBuilding(id));
    }

    @Override
    public AdminIndoorBuildingDetailResponse createBuilding(AdminBuildingUpsertRequest request) {
        validateBuildingRequest(request, null);
        Building building = new Building();
        applyBuildingRequest(building, request);
        buildingMapper.insert(building);
        syncBuildingAttachments(building.getId(), request.getAttachments(), request.getAttachmentAssetIds());
        return toBuildingDetail(requireBuilding(building.getId()));
    }

    @Override
    public AdminIndoorBuildingDetailResponse updateBuilding(Long id, AdminBuildingUpsertRequest request) {
        Building existing = requireBuilding(id);
        validateBuildingRequest(request, id);
        applyBuildingRequest(existing, request);
        buildingMapper.updateById(existing);
        syncBuildingAttachments(id, request.getAttachments(), request.getAttachmentAssetIds());
        return toBuildingDetail(requireBuilding(id));
    }

    @Override
    public PageResponse<AdminIndoorFloorResponse> pageFloors(Long buildingId, long pageNum, long pageSize, String status) {
        Building building = requireBuilding(buildingId);
        Page<IndoorFloor> page = indoorFloorMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<IndoorFloor>()
                        .eq(IndoorFloor::getBuildingId, buildingId)
                        .eq(StringUtils.hasText(status), IndoorFloor::getStatus, status)
                        .orderByAsc(IndoorFloor::getSortOrder)
                        .orderByAsc(IndoorFloor::getFloorNumber)
                        .orderByAsc(IndoorFloor::getId)
        );
        Page<AdminIndoorFloorResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(floor -> toFloorResponse(floor, building)).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminIndoorFloorResponse getFloorDetail(Long floorId) {
        return indoorTilePipelineService.getFloorDetail(floorId);
    }

    @Override
    public AdminIndoorFloorResponse createFloor(Long buildingId, AdminIndoorFloorUpsertRequest request) {
        Building building = requireBuilding(buildingId);
        validateFloorRequest(buildingId, request, null);
        IndoorFloor floor = new IndoorFloor();
        floor.setBuildingId(buildingId);
        applyFloorRequest(floor, request);
        indoorFloorMapper.insert(floor);
        syncFloorAttachments(floor.getId(), request.getAttachments(), request.getAttachmentAssetIds());
        return toFloorResponse(requireFloor(floor.getId()), building);
    }

    @Override
    public AdminIndoorFloorResponse updateFloor(Long floorId, AdminIndoorFloorUpsertRequest request) {
        IndoorFloor floor = requireFloor(floorId);
        Building building = requireBuilding(floor.getBuildingId());
        validateFloorRequest(building.getId(), request, floorId);
        applyFloorRequest(floor, request);
        indoorFloorMapper.updateById(floor);
        syncFloorAttachments(floorId, request.getAttachments(), request.getAttachmentAssetIds());
        return toFloorResponse(requireFloor(floorId), building);
    }

    @Override
    public void deleteFloor(Long floorId) {
        requireFloor(floorId);
        syncFloorAttachments(floorId, Collections.emptyList(), Collections.emptyList());
        indoorMarkerAuthoringService.listFloorNodes(floorId, null)
                .forEach(node -> indoorMarkerAuthoringService.deleteNode(node.getId()));
        indoorFloorMapper.deleteById(floorId);
    }

    @Override
    public AdminIndoorTilePreviewResponse previewFloorTileZip(Long floorId, MultipartFile file, Integer tileSizePx) {
        return indoorTilePipelineService.previewFloorTileZip(floorId, file, tileSizePx);
    }

    @Override
    public AdminIndoorFloorResponse importFloorTileZip(Long floorId, MultipartFile file, Integer tileSizePx, Long adminUserId) {
        return indoorTilePipelineService.importFloorTileZip(floorId, file, tileSizePx, adminUserId);
    }

    @Override
    public AdminIndoorFloorResponse importFloorPlanImage(Long floorId, MultipartFile file, Integer tileSizePx, Long adminUserId) {
        return indoorTilePipelineService.importFloorPlanImage(floorId, file, tileSizePx, adminUserId);
    }

    @Override
    public List<AdminIndoorNodeResponse> listFloorNodes(Long floorId, String status) {
        return indoorMarkerAuthoringService.listFloorNodes(floorId, status);
    }

    @Override
    public AdminIndoorNodeResponse createNode(Long floorId, AdminIndoorNodeUpsertRequest request) {
        return indoorMarkerAuthoringService.createNode(floorId, request);
    }

    @Override
    public AdminIndoorNodeResponse updateNode(Long nodeId, AdminIndoorNodeUpsertRequest request) {
        return indoorMarkerAuthoringService.updateNode(nodeId, request);
    }

    @Override
    public void deleteNode(Long nodeId) {
        indoorMarkerAuthoringService.deleteNode(nodeId);
    }

    @Override
    public AdminIndoorRuleValidationResponse validateRuleGraph(Long floorId, Long nodeId, AdminIndoorNodeUpsertRequest request) {
        return indoorMarkerAuthoringService.validateRuleGraph(floorId, nodeId, request);
    }

    @Override
    public List<AdminIndoorMarkerResponse> listFloorMarkers(Long floorId, String status) {
        return indoorMarkerAuthoringService.listFloorMarkers(floorId, status);
    }

    @Override
    public AdminIndoorMarkerResponse createMarker(Long floorId, AdminIndoorMarkerUpsertRequest request) {
        return indoorMarkerAuthoringService.createMarker(floorId, request);
    }

    @Override
    public AdminIndoorMarkerResponse updateMarker(Long markerId, AdminIndoorMarkerUpsertRequest request) {
        return indoorMarkerAuthoringService.updateMarker(markerId, request);
    }

    @Override
    public void deleteMarker(Long markerId) {
        indoorMarkerAuthoringService.deleteMarker(markerId);
    }

    @Override
    public AdminIndoorMarkerCsvPreviewResponse previewMarkerCsv(Long floorId, MultipartFile file) {
        return indoorMarkerAuthoringService.previewMarkerCsv(floorId, file);
    }

    @Override
    public AdminIndoorMarkerCsvImportResponse confirmMarkerCsv(Long floorId, AdminIndoorMarkerCsvConfirmRequest request, Long adminUserId) {
        return indoorMarkerAuthoringService.confirmMarkerCsv(floorId, request, adminUserId);
    }

    private void validateBuildingRequest(AdminBuildingUpsertRequest request, Long currentId) {
        if (request == null) {
            throw new BusinessException(4001, "request body is required");
        }
        if (!StringUtils.hasText(request.getBuildingCode())) {
            throw new BusinessException(4001, "buildingCode is required");
        }
        if (!StringUtils.hasText(request.getNameZh())) {
            throw new BusinessException(4001, "nameZh is required");
        }
        if (request.getCityId() == null) {
            throw new BusinessException(4001, "cityId is required");
        }
        if (request.getTotalFloors() != null && request.getTotalFloors() < 1) {
            throw new BusinessException(4001, "totalFloors must be greater than 0");
        }
        if (request.getBasementFloors() != null && request.getBasementFloors() < 0) {
            throw new BusinessException(4001, "basementFloors must be at least 0");
        }

        City city = requireCity(request.getCityId());
        SubMap subMap = requireSubMap(request.getSubMapId());
        if (subMap != null && !Objects.equals(subMap.getCityId(), city.getId())) {
            throw new BusinessException(4001, "sub-map does not belong to the selected city");
        }

        String bindingMode = normalizeBindingMode(request.getBindingMode());
        Poi poi = request.getPoiId() == null ? null : requirePoi(request.getPoiId());
        if (poi != null) {
            if (!Objects.equals(poi.getCityId(), city.getId())) {
                throw new BusinessException(4001, "POI does not belong to the selected city");
            }
            if (subMap != null && !Objects.equals(poi.getSubMapId(), subMap.getId())) {
                throw new BusinessException(4001, "POI does not belong to the selected sub-map");
            }
        }
        if ("poi".equals(bindingMode)) {
            if (poi == null) {
                throw new BusinessException(4001, "poiId is required when bindingMode is poi");
            }
            if (request.getSourceLatitude() != null || request.getSourceLongitude() != null || request.getLat() != null || request.getLng() != null) {
                throw new BusinessException(4001, "POI-bound buildings must not submit standalone coordinates");
            }
        } else {
            if (poi == null && (firstCoordinate(request.getSourceLatitude(), request.getLat()) == null
                    || firstCoordinate(request.getSourceLongitude(), request.getLng()) == null)) {
                throw new BusinessException(4001, "map-bound buildings require coordinates");
            }
        }

        ensureUniqueBuildingCode(request.getBuildingCode(), currentId);
        ensureAssetExists(request.getCoverAssetId());
        ensureAttachmentAssetsExist(request.getAttachments(), request.getAttachmentAssetIds());
    }

    private void validateFloorRequest(Long buildingId, AdminIndoorFloorUpsertRequest request, Long currentFloorId) {
        if (request == null) {
            throw new BusinessException(4001, "request body is required");
        }
        if (request.getFloorNumber() == null) {
            throw new BusinessException(4001, "floorNumber is required");
        }
        if (!StringUtils.hasText(request.getFloorNameZh())) {
            throw new BusinessException(4001, "floorNameZh is required");
        }
        if (request.getAreaSqm() != null && request.getAreaSqm().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(4001, "areaSqm must be greater than or equal to 0");
        }
        if (request.getZoomMin() != null && request.getZoomMin().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(4001, "zoomMin must be greater than 0");
        }
        if (request.getZoomMax() != null && request.getZoomMax().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(4001, "zoomMax must be greater than 0");
        }
        if (request.getZoomMin() != null && request.getZoomMax() != null
                && request.getZoomMin().compareTo(request.getZoomMax()) > 0) {
            throw new BusinessException(4001, "zoomMin must be less than or equal to zoomMax");
        }
        if (request.getDefaultZoom() != null && request.getZoomMin() != null
                && request.getDefaultZoom().compareTo(request.getZoomMin()) < 0) {
            throw new BusinessException(4001, "defaultZoom must be within the zoom range");
        }
        if (request.getDefaultZoom() != null && request.getZoomMax() != null
                && request.getDefaultZoom().compareTo(request.getZoomMax()) > 0) {
            throw new BusinessException(4001, "defaultZoom must be within the zoom range");
        }
        ensureUniqueFloorNumber(buildingId, request.getFloorNumber(), currentFloorId);
        ensureAssetExists(request.getCoverAssetId());
        ensureAssetExists(request.getFloorPlanAssetId());
        ensureAttachmentAssetsExist(request.getAttachments(), request.getAttachmentAssetIds());
    }

    private void applyBuildingRequest(Building building, AdminBuildingUpsertRequest request) {
        City city = requireCity(request.getCityId());
        SubMap subMap = requireSubMap(request.getSubMapId());
        Poi poi = request.getPoiId() == null ? null : requirePoi(request.getPoiId());
        String bindingMode = normalizeBindingMode(request.getBindingMode());

        building.setBuildingCode(request.getBuildingCode().trim());
        building.setCityId(city.getId());
        building.setCityCode(city.getCode());
        building.setSubMapId(subMap == null ? (poi == null ? null : poi.getSubMapId()) : subMap.getId());
        building.setBindingMode(bindingMode);
        building.setNameZh(trimToNull(request.getNameZh()));
        building.setNameEn(trimToNull(request.getNameEn()));
        building.setNameZht(trimToNull(request.getNameZht()));
        building.setNamePt(trimToNull(request.getNamePt()));
        building.setAddressZh(trimToNull(request.getAddressZh()));
        building.setAddressEn(trimToNull(request.getAddressEn()));
        building.setAddressZht(trimToNull(request.getAddressZht()));
        building.setAddressPt(trimToNull(request.getAddressPt()));

        if (poi != null) {
            building.setPoiId(poi.getId());
            building.setSourceCoordinateSystem(StringUtils.hasText(poi.getSourceCoordinateSystem()) ? poi.getSourceCoordinateSystem() : "GCJ02");
            building.setSourceLatitude(poi.getSourceLatitude() != null ? poi.getSourceLatitude() : poi.getLatitude());
            building.setSourceLongitude(poi.getSourceLongitude() != null ? poi.getSourceLongitude() : poi.getLongitude());
            building.setLat(poi.getLatitude());
            building.setLng(poi.getLongitude());
        } else {
            BigDecimal sourceLatitude = firstCoordinate(request.getSourceLatitude(), request.getLat());
            BigDecimal sourceLongitude = firstCoordinate(request.getSourceLongitude(), request.getLng());
            CoordinateNormalizationResult normalized = coordinateNormalizationService.normalizeToGcj02(
                    request.getSourceCoordinateSystem(),
                    sourceLatitude,
                    sourceLongitude
            );
            building.setPoiId(null);
            building.setSourceCoordinateSystem(normalized.getSourceCoordinateSystem().getCode());
            building.setSourceLatitude(normalized.getSourceLatitude());
            building.setSourceLongitude(normalized.getSourceLongitude());
            building.setLat(normalized.getNormalizedLatitude());
            building.setLng(normalized.getNormalizedLongitude());
        }

        building.setTotalFloors(request.getTotalFloors() == null ? 1 : request.getTotalFloors());
        building.setBasementFloors(request.getBasementFloors() == null ? 0 : request.getBasementFloors());
        building.setCoverAssetId(request.getCoverAssetId());
        building.setCoverImageUrl(request.getCoverAssetId() == null ? trimToNull(request.getCoverImageUrl()) : resolveAssetUrl(request.getCoverAssetId()));
        building.setDescriptionZh(trimToNull(request.getDescriptionZh()));
        building.setDescriptionEn(trimToNull(request.getDescriptionEn()));
        building.setDescriptionZht(trimToNull(request.getDescriptionZht()));
        building.setDescriptionPt(trimToNull(request.getDescriptionPt()));
        building.setPopupConfigJson(trimToNull(request.getPopupConfigJson()));
        building.setDisplayConfigJson(trimToNull(request.getDisplayConfigJson()));
        building.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        building.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "unpublished");
        building.setPublishedAt(parseDateTime(request.getPublishedAt()));
    }

    private void applyFloorRequest(IndoorFloor floor, AdminIndoorFloorUpsertRequest request) {
        floor.setIndoorMapId(request.getIndoorMapId());
        floor.setFloorNumber(request.getFloorNumber());
        floor.setFloorCode(StringUtils.hasText(request.getFloorCode()) ? request.getFloorCode().trim() : defaultFloorCode(request.getFloorNumber()));
        floor.setFloorNameZh(trimToNull(request.getFloorNameZh()));
        floor.setFloorNameEn(trimToNull(request.getFloorNameEn()));
        floor.setFloorNameZht(trimToNull(request.getFloorNameZht()));
        floor.setFloorNamePt(trimToNull(request.getFloorNamePt()));
        floor.setDescriptionZh(trimToNull(request.getDescriptionZh()));
        floor.setDescriptionEn(trimToNull(request.getDescriptionEn()));
        floor.setDescriptionZht(trimToNull(request.getDescriptionZht()));
        floor.setDescriptionPt(trimToNull(request.getDescriptionPt()));
        floor.setCoverAssetId(request.getCoverAssetId());
        floor.setFloorPlanAssetId(request.getFloorPlanAssetId());
        floor.setFloorPlanUrl(request.getFloorPlanAssetId() == null ? trimToNull(request.getTilePreviewImageUrl()) : resolveAssetUrl(request.getFloorPlanAssetId()));
        floor.setTilePreviewImageUrl(trimToNull(request.getTilePreviewImageUrl()));
        floor.setAltitudeMeters(request.getAltitudeMeters());
        floor.setAreaSqm(request.getAreaSqm());
        floor.setZoomMin(defaultDecimal(request.getZoomMin(), "0.50"));
        floor.setZoomMax(defaultDecimal(request.getZoomMax(), "2.50"));
        floor.setDefaultZoom(defaultDecimal(request.getDefaultZoom(), "1.00"));
        floor.setPopupConfigJson(trimToNull(request.getPopupConfigJson()));
        floor.setDisplayConfigJson(trimToNull(request.getDisplayConfigJson()));
        floor.setSortOrder(request.getSortOrder() == null ? request.getFloorNumber() : request.getSortOrder());
        floor.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus().trim() : "unpublished");
        floor.setPublishedAt(parseDateTime(request.getPublishedAt()));
    }

    private Building requireBuilding(Long id) {
        Building building = buildingMapper.selectById(id);
        if (building == null) {
            throw new BusinessException(4040, "building not found");
        }
        return building;
    }

    private IndoorFloor requireFloor(Long floorId) {
        IndoorFloor floor = indoorFloorMapper.selectById(floorId);
        if (floor == null) {
            throw new BusinessException(4046, "floor not found");
        }
        return floor;
    }

    private City requireCity(Long cityId) {
        City city = cityMapper.selectById(cityId);
        if (city == null) {
            throw new BusinessException(4043, "city not found");
        }
        return city;
    }

    private SubMap requireSubMap(Long subMapId) {
        if (subMapId == null) {
            return null;
        }
        SubMap subMap = subMapMapper.selectById(subMapId);
        if (subMap == null) {
            throw new BusinessException(4044, "sub-map not found");
        }
        return subMap;
    }

    private Poi requirePoi(Long poiId) {
        Poi poi = poiMapper.selectById(poiId);
        if (poi == null) {
            throw new BusinessException(4041, "poi not found");
        }
        return poi;
    }

    private void ensureUniqueBuildingCode(String buildingCode, Long currentId) {
        Building existing = buildingMapper.selectOne(new LambdaQueryWrapper<Building>()
                .eq(Building::getBuildingCode, buildingCode.trim())
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), currentId)) {
            throw new BusinessException(4001, "buildingCode already exists");
        }
    }

    private void ensureUniqueFloorNumber(Long buildingId, Integer floorNumber, Long currentFloorId) {
        IndoorFloor existing = indoorFloorMapper.selectOne(new LambdaQueryWrapper<IndoorFloor>()
                .eq(IndoorFloor::getBuildingId, buildingId)
                .eq(IndoorFloor::getFloorNumber, floorNumber)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), currentFloorId)) {
            throw new BusinessException(4001, "floorNumber already exists within this building");
        }
    }

    private void ensureAssetExists(Long assetId) {
        if (assetId == null) {
            return;
        }
        if (contentAssetMapper.selectById(assetId) == null) {
            throw new BusinessException(4001, "referenced asset does not exist");
        }
    }

    private void ensureAssetIdsExist(List<Long> assetIds) {
        List<Long> normalized = normalizeIds(assetIds);
        if (normalized.isEmpty()) {
            return;
        }
        Long count = contentAssetMapper.selectCount(new LambdaQueryWrapper<ContentAsset>()
                .in(ContentAsset::getId, normalized));
        if (count == null || count.longValue() != normalized.size()) {
            throw new BusinessException(4001, "one or more attachment assets do not exist");
        }
    }

    private void ensureAttachmentAssetsExist(
            List<AdminSpatialAssetLinkUpsertRequest> attachments,
            List<Long> attachmentAssetIds
    ) {
        if (attachments != null && !attachments.isEmpty()) {
            ensureAssetIdsExist(attachments.stream()
                    .map(AdminSpatialAssetLinkUpsertRequest::getAssetId)
                    .filter(Objects::nonNull)
                    .toList());
            return;
        }
        ensureAssetIdsExist(attachmentAssetIds);
    }

    private void syncBuildingAttachments(
            Long buildingId,
            List<AdminSpatialAssetLinkUpsertRequest> attachments,
            List<Long> attachmentAssetIds
    ) {
        List<AdminSpatialAssetLinkUpsertRequest> resolvedAttachments = resolveAttachmentRequests(attachments, attachmentAssetIds);
        adminContentRelationService.syncTargetIds(
                BUILDING_ATTACHMENT_OWNER,
                buildingId,
                ATTACHMENT_RELATION,
                CONTENT_ASSET_TARGET,
                resolvedAttachments.stream()
                        .map(AdminSpatialAssetLinkUpsertRequest::getAssetId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()
        );
        adminSpatialAssetLinkService.syncLinks("indoor_building", buildingId, resolvedAttachments);
    }

    private void syncFloorAttachments(
            Long floorId,
            List<AdminSpatialAssetLinkUpsertRequest> attachments,
            List<Long> attachmentAssetIds
    ) {
        List<AdminSpatialAssetLinkUpsertRequest> resolvedAttachments = resolveAttachmentRequests(attachments, attachmentAssetIds);
        adminContentRelationService.syncTargetIds(
                FLOOR_ATTACHMENT_OWNER,
                floorId,
                ATTACHMENT_RELATION,
                CONTENT_ASSET_TARGET,
                resolvedAttachments.stream()
                        .map(AdminSpatialAssetLinkUpsertRequest::getAssetId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()
        );
        adminSpatialAssetLinkService.syncLinks("indoor_floor", floorId, resolvedAttachments);
    }

    private List<Long> loadAttachmentIds(String ownerType, Long ownerId) {
        return adminContentRelationService.listTargetIds(ownerType, ownerId, ATTACHMENT_RELATION, CONTENT_ASSET_TARGET);
    }

    private List<AdminSpatialAssetLinkResponse> loadAttachmentLinks(String entityType, Long entityId) {
        return adminSpatialAssetLinkService.listLinks(entityType, entityId);
    }

    private List<AdminSpatialAssetLinkResponse> synthesizeAttachmentLinks(List<Long> assetIds) {
        return normalizeIds(assetIds).stream()
                .map(assetId -> AdminSpatialAssetLinkResponse.builder()
                        .assetId(assetId)
                        .usageType("gallery")
                        .status("draft")
                        .build())
                .toList();
    }

    private List<AdminSpatialAssetLinkUpsertRequest> resolveAttachmentRequests(
            List<AdminSpatialAssetLinkUpsertRequest> attachments,
            List<Long> attachmentAssetIds
    ) {
        if (attachments != null && !attachments.isEmpty()) {
            return attachments;
        }
        return normalizeIds(attachmentAssetIds).stream()
                .map(assetId -> {
                    AdminSpatialAssetLinkUpsertRequest request = new AdminSpatialAssetLinkUpsertRequest();
                    request.setAssetId(assetId);
                    request.setUsageType("gallery");
                    request.setStatus("draft");
                    return request;
                })
                .toList();
    }

    private Map<Long, City> loadCityMap(Collection<Long> ids) {
        List<Long> normalized = normalizeIds(ids);
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        return cityMapper.selectBatchIds(normalized).stream()
                .collect(Collectors.toMap(City::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, SubMap> loadSubMapMap(Collection<Long> ids) {
        List<Long> normalized = normalizeIds(ids);
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        return subMapMapper.selectBatchIds(normalized).stream()
                .collect(Collectors.toMap(SubMap::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, Poi> loadPoiMap(Collection<Long> ids) {
        List<Long> normalized = normalizeIds(ids);
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        return poiMapper.selectBatchIds(normalized).stream()
                .collect(Collectors.toMap(Poi::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, Integer> loadFloorCountMap(Collection<Long> buildingIds) {
        List<Long> normalized = normalizeIds(buildingIds);
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        return indoorFloorMapper.selectList(new LambdaQueryWrapper<IndoorFloor>()
                        .in(IndoorFloor::getBuildingId, normalized))
                .stream()
                .collect(Collectors.groupingBy(IndoorFloor::getBuildingId, LinkedHashMap::new, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    private BuildingResponse toBuildingResponse(Building building, City city, SubMap subMap, Poi poi, Integer floorCount) {
        return BuildingResponse.builder()
                .id(building.getId())
                .buildingCode(building.getBuildingCode())
                .bindingMode(building.getBindingMode())
                .cityId(building.getCityId())
                .cityCode(building.getCityCode())
                .cityName(city == null ? null : city.getNameZh())
                .subMapId(building.getSubMapId())
                .subMapCode(subMap == null ? null : subMap.getCode())
                .subMapName(subMap == null ? null : subMap.getNameZh())
                .poiId(building.getPoiId())
                .poiName(poi == null ? null : poi.getNameZh())
                .nameZh(building.getNameZh())
                .nameEn(building.getNameEn())
                .nameZht(building.getNameZht())
                .namePt(building.getNamePt())
                .addressZh(building.getAddressZh())
                .addressEn(building.getAddressEn())
                .addressZht(building.getAddressZht())
                .addressPt(building.getAddressPt())
                .sourceCoordinateSystem(building.getSourceCoordinateSystem())
                .sourceLatitude(building.getSourceLatitude())
                .sourceLongitude(building.getSourceLongitude())
                .lat(building.getLat())
                .lng(building.getLng())
                .totalFloors(building.getTotalFloors())
                .basementFloors(building.getBasementFloors())
                .floorCount(floorCount)
                .coverAssetId(building.getCoverAssetId())
                .coverImageUrl(building.getCoverImageUrl())
                .status(building.getStatus())
                .sortOrder(building.getSortOrder())
                .publishedAt(building.getPublishedAt())
                .createdAt(building.getCreatedAt())
                .updatedAt(building.getUpdatedAt())
                .build();
    }

    private AdminIndoorBuildingDetailResponse toBuildingDetail(Building building) {
        City city = building.getCityId() == null ? null : cityMapper.selectById(building.getCityId());
        SubMap subMap = building.getSubMapId() == null ? null : subMapMapper.selectById(building.getSubMapId());
        Poi poi = building.getPoiId() == null ? null : poiMapper.selectById(building.getPoiId());
        List<IndoorFloor> floors = indoorFloorMapper.selectList(new LambdaQueryWrapper<IndoorFloor>()
                .eq(IndoorFloor::getBuildingId, building.getId())
                .orderByAsc(IndoorFloor::getSortOrder)
                .orderByAsc(IndoorFloor::getFloorNumber)
                .orderByAsc(IndoorFloor::getId));
        Map<Long, List<AdminIndoorMarkerResponse>> floorMarkers = floors.stream()
                .collect(Collectors.toMap(
                        IndoorFloor::getId,
                        floor -> indoorMarkerAuthoringService.listFloorMarkers(floor.getId(), null),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        List<AdminSpatialAssetLinkResponse> attachmentLinks = loadAttachmentLinks("indoor_building", building.getId());
        List<Long> legacyAttachmentIds = loadAttachmentIds(BUILDING_ATTACHMENT_OWNER, building.getId());
        List<AdminSpatialAssetLinkResponse> resolvedAttachmentLinks = attachmentLinks.isEmpty()
                ? synthesizeAttachmentLinks(legacyAttachmentIds)
                : attachmentLinks;
        return AdminIndoorBuildingDetailResponse.builder()
                .id(building.getId())
                .buildingCode(building.getBuildingCode())
                .bindingMode(building.getBindingMode())
                .cityId(building.getCityId())
                .cityCode(building.getCityCode())
                .cityName(city == null ? null : city.getNameZh())
                .subMapId(building.getSubMapId())
                .subMapCode(subMap == null ? null : subMap.getCode())
                .subMapName(subMap == null ? null : subMap.getNameZh())
                .poiId(building.getPoiId())
                .poiName(poi == null ? null : poi.getNameZh())
                .nameZh(building.getNameZh())
                .nameEn(building.getNameEn())
                .nameZht(building.getNameZht())
                .namePt(building.getNamePt())
                .addressZh(building.getAddressZh())
                .addressEn(building.getAddressEn())
                .addressZht(building.getAddressZht())
                .addressPt(building.getAddressPt())
                .sourceCoordinateSystem(building.getSourceCoordinateSystem())
                .sourceLatitude(building.getSourceLatitude())
                .sourceLongitude(building.getSourceLongitude())
                .lat(building.getLat())
                .lng(building.getLng())
                .totalFloors(building.getTotalFloors())
                .basementFloors(building.getBasementFloors())
                .coverAssetId(building.getCoverAssetId())
                .coverImageUrl(building.getCoverImageUrl())
                .descriptionZh(building.getDescriptionZh())
                .descriptionEn(building.getDescriptionEn())
                .descriptionZht(building.getDescriptionZht())
                .descriptionPt(building.getDescriptionPt())
                .popupConfigJson(building.getPopupConfigJson())
                .displayConfigJson(building.getDisplayConfigJson())
                .attachments(resolvedAttachmentLinks)
                .attachmentAssetIds((resolvedAttachmentLinks.isEmpty() ? legacyAttachmentIds : resolvedAttachmentLinks.stream()
                        .map(AdminSpatialAssetLinkResponse::getAssetId)
                        .filter(Objects::nonNull)
                        .toList()))
                .floors(floors.stream()
                        .map(floor -> toFloorResponse(
                                floor,
                                building,
                                floorMarkers.getOrDefault(floor.getId(), Collections.emptyList()),
                                false
                        ))
                        .toList())
                .sortOrder(building.getSortOrder())
                .status(building.getStatus())
                .publishedAt(building.getPublishedAt())
                .createdAt(building.getCreatedAt())
                .updatedAt(building.getUpdatedAt())
                .build();
    }

    private AdminIndoorFloorResponse toFloorResponse(IndoorFloor floor, Building building) {
        return toFloorResponse(floor, building, Collections.emptyList(), false);
    }

    private AdminIndoorFloorResponse toFloorResponse(
            IndoorFloor floor,
            Building building,
            List<AdminIndoorMarkerResponse> markers
    ) {
        return toFloorResponse(floor, building, markers, true);
    }

    private AdminIndoorFloorResponse toFloorResponse(
            IndoorFloor floor,
            Building building,
            List<AdminIndoorMarkerResponse> markers,
            boolean includeMarkers
    ) {
        List<AdminSpatialAssetLinkResponse> attachmentLinks = loadAttachmentLinks("indoor_floor", floor.getId());
        List<Long> legacyAttachmentIds = loadAttachmentIds(FLOOR_ATTACHMENT_OWNER, floor.getId());
        List<AdminSpatialAssetLinkResponse> resolvedAttachmentLinks = attachmentLinks.isEmpty()
                ? synthesizeAttachmentLinks(legacyAttachmentIds)
                : attachmentLinks;
        return AdminIndoorFloorResponse.builder()
                .id(floor.getId())
                .buildingId(building.getId())
                .indoorMapId(floor.getIndoorMapId())
                .floorCode(floor.getFloorCode())
                .floorNumber(floor.getFloorNumber())
                .floorNameZh(floor.getFloorNameZh())
                .floorNameEn(floor.getFloorNameEn())
                .floorNameZht(floor.getFloorNameZht())
                .floorNamePt(floor.getFloorNamePt())
                .descriptionZh(floor.getDescriptionZh())
                .descriptionEn(floor.getDescriptionEn())
                .descriptionZht(floor.getDescriptionZht())
                .descriptionPt(floor.getDescriptionPt())
                .coverAssetId(floor.getCoverAssetId())
                .floorPlanAssetId(floor.getFloorPlanAssetId())
                .floorPlanUrl(floor.getFloorPlanUrl())
                .tileSourceType(floor.getTileSourceType())
                .tileSourceAssetId(floor.getTileSourceAssetId())
                .tileSourceFilename(floor.getTileSourceFilename())
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
                .importedAt(floor.getImportedAt())
                .altitudeMeters(floor.getAltitudeMeters())
                .areaSqm(floor.getAreaSqm())
                .zoomMin(floor.getZoomMin())
                .zoomMax(floor.getZoomMax())
                .defaultZoom(floor.getDefaultZoom())
                .popupConfigJson(floor.getPopupConfigJson())
                .displayConfigJson(floor.getDisplayConfigJson())
                .attachments(resolvedAttachmentLinks)
                .attachmentAssetIds((resolvedAttachmentLinks.isEmpty() ? legacyAttachmentIds : resolvedAttachmentLinks.stream()
                        .map(AdminSpatialAssetLinkResponse::getAssetId)
                        .filter(Objects::nonNull)
                        .toList()))
                .markerCount(markers.size())
                .sortOrder(floor.getSortOrder())
                .status(floor.getStatus())
                .publishedAt(floor.getPublishedAt())
                .createdAt(floor.getCreatedAt())
                .updatedAt(floor.getUpdatedAt())
                .markers(includeMarkers ? markers : Collections.emptyList())
                .build();
    }

    private List<Long> normalizeIds(Collection<Long> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }
        return ids.stream().filter(Objects::nonNull).distinct().toList();
    }

    private String resolveAssetUrl(Long assetId) {
        if (assetId == null) {
            return null;
        }
        ContentAsset asset = contentAssetMapper.selectById(assetId);
        return asset == null ? null : asset.getCanonicalUrl();
    }

    private BigDecimal firstCoordinate(BigDecimal preferred, BigDecimal fallback) {
        return preferred != null ? preferred : fallback;
    }

    private String normalizeBindingMode(String bindingMode) {
        return "poi".equalsIgnoreCase(bindingMode) ? "poi" : "map";
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String defaultFloorCode(Integer floorNumber) {
        if (floorNumber == null) {
            return null;
        }
        if (floorNumber < 0) {
            return "B" + Math.abs(floorNumber);
        }
        if (floorNumber == 0) {
            return "G";
        }
        return "F" + floorNumber;
    }

    private BigDecimal defaultDecimal(BigDecimal value, String fallback) {
        return value != null ? value : new BigDecimal(fallback);
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value.trim()) : null;
    }
}
