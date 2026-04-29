package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorBuildingResponse;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorFloorResponse;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorMarkerResponse;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.aoxiaoyou.tripofmacau.entity.IndoorBuilding;
import com.aoxiaoyou.tripofmacau.entity.IndoorFloor;
import com.aoxiaoyou.tripofmacau.entity.IndoorNode;
import com.aoxiaoyou.tripofmacau.mapper.IndoorBuildingMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorFloorMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorNodeMapper;
import com.aoxiaoyou.tripofmacau.service.CatalogFoundationService;
import com.aoxiaoyou.tripofmacau.service.PublicIndoorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PublicIndoorServiceImpl implements PublicIndoorService {

    private final IndoorBuildingMapper indoorBuildingMapper;
    private final IndoorFloorMapper indoorFloorMapper;
    private final IndoorNodeMapper indoorNodeMapper;
    private final CatalogFoundationService catalogFoundationService;
    private final LocalizedContentSupport localizedContentSupport;

    @Override
    public IndoorBuildingResponse getBuilding(Long buildingId, String localeHint) {
        IndoorBuilding building = indoorBuildingMapper.selectOne(new LambdaQueryWrapper<IndoorBuilding>()
                .eq(IndoorBuilding::getId, buildingId)
                .eq(IndoorBuilding::getStatus, "published")
                .last("LIMIT 1"));
        if (building == null) {
            throw new BusinessException(4044, "Indoor building not found");
        }
        return toResponse(building, localeHint);
    }

    @Override
    public IndoorBuildingResponse getBuildingByPoi(Long poiId, String localeHint) {
        IndoorBuilding building = indoorBuildingMapper.selectOne(new LambdaQueryWrapper<IndoorBuilding>()
                .eq(IndoorBuilding::getPoiId, poiId)
                .eq(IndoorBuilding::getStatus, "published")
                .orderByAsc(IndoorBuilding::getSortOrder)
                .orderByAsc(IndoorBuilding::getId)
                .last("LIMIT 1"));
        if (building == null) {
            throw new BusinessException(4045, "Indoor building for the selected POI was not found");
        }
        return toResponse(building, localeHint);
    }

    @Override
    public IndoorFloorResponse getFloor(Long floorId, String localeHint) {
        IndoorFloor floor = requirePublishedFloor(floorId);
        Map<Long, ContentAsset> assets = loadFloorAssetMap(List.of(floor));
        return toFloorResponse(floor, assets, localeHint, true);
    }

    @Override
    public List<IndoorMarkerResponse> getFloorMarkers(Long floorId, String localeHint) {
        IndoorFloor floor = requirePublishedFloor(floorId);
        return loadFloorMarkers(List.of(floor), localeHint).getOrDefault(floor.getId(), Collections.emptyList());
    }

    private IndoorBuildingResponse toResponse(IndoorBuilding building, String localeHint) {
        List<IndoorFloor> floors = indoorFloorMapper.selectList(new LambdaQueryWrapper<IndoorFloor>()
                .eq(IndoorFloor::getBuildingId, building.getId())
                .eq(IndoorFloor::getStatus, "published")
                .orderByAsc(IndoorFloor::getSortOrder)
                .orderByAsc(IndoorFloor::getFloorNumber)
                .orderByAsc(IndoorFloor::getId));
        Map<Long, ContentAsset> assets = loadFloorAssetMap(floors, building.getCoverAssetId());

        return IndoorBuildingResponse.builder()
                .id(building.getId())
                .buildingCode(building.getBuildingCode())
                .bindingMode(building.getBindingMode())
                .cityId(building.getCityId())
                .cityCode(building.getCityCode())
                .subMapId(building.getSubMapId())
                .poiId(building.getPoiId())
                .name(localizedContentSupport.resolveText(localeHint, building.getNameZh(), building.getNameEn(), building.getNameZht(), building.getNamePt()))
                .address(localizedContentSupport.resolveText(localeHint, building.getAddressZh(), building.getAddressEn(), building.getAddressZht(), building.getAddressPt()))
                .description(localizedContentSupport.resolveText(localeHint, building.getDescriptionZh(), building.getDescriptionEn(), building.getDescriptionZht(), building.getDescriptionPt()))
                .coverImageUrl(resolveAssetUrl(assets, building.getCoverAssetId(), building.getCoverImageUrl()))
                .popupConfigJson(building.getPopupConfigJson())
                .displayConfigJson(building.getDisplayConfigJson())
                .sourceCoordinateSystem(building.getSourceCoordinateSystem())
                .sourceLatitude(building.getSourceLatitude())
                .sourceLongitude(building.getSourceLongitude())
                .latitude(building.getLat())
                .longitude(building.getLng())
                .totalFloors(building.getTotalFloors())
                .basementFloors(building.getBasementFloors())
                .floors(floors.isEmpty() ? Collections.emptyList() : floors.stream().map(floor -> toFloorResponse(floor, assets, localeHint, false)).toList())
                .build();
    }

    private IndoorFloorResponse toFloorResponse(IndoorFloor floor, Map<Long, ContentAsset> assets, String localeHint, boolean includeMarkers) {
        return IndoorFloorResponse.builder()
                .id(floor.getId())
                .floorCode(floor.getFloorCode())
                .floorNumber(floor.getFloorNumber())
                .name(localizedContentSupport.resolveText(localeHint, floor.getFloorNameZh(), floor.getFloorNameEn(), floor.getFloorNameZht(), floor.getFloorNamePt()))
                .description(localizedContentSupport.resolveText(localeHint, floor.getDescriptionZh(), floor.getDescriptionEn(), floor.getDescriptionZht(), floor.getDescriptionPt()))
                .coverImageUrl(resolveAssetUrl(assets, floor.getCoverAssetId(), null))
                .floorPlanUrl(resolveAssetUrl(assets, floor.getFloorPlanAssetId(), floor.getFloorPlanUrl()))
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
                .markers(includeMarkers ? loadFloorMarkers(List.of(floor), localeHint).getOrDefault(floor.getId(), Collections.emptyList()) : Collections.emptyList())
                .build();
    }

    private IndoorFloor requirePublishedFloor(Long floorId) {
        IndoorFloor floor = indoorFloorMapper.selectOne(new LambdaQueryWrapper<IndoorFloor>()
                .eq(IndoorFloor::getId, floorId)
                .eq(IndoorFloor::getStatus, "published")
                .last("LIMIT 1"));
        if (floor == null) {
            throw new BusinessException(4046, "Indoor floor not found");
        }
        return floor;
    }

    private Map<Long, ContentAsset> loadFloorAssetMap(List<IndoorFloor> floors) {
        return loadFloorAssetMap(floors, null);
    }

    private Map<Long, ContentAsset> loadFloorAssetMap(List<IndoorFloor> floors, Long buildingCoverAssetId) {
        return catalogFoundationService.getPublishedAssetsByIds(Stream.concat(
                        Stream.of(buildingCoverAssetId),
                        floors.stream().flatMap(floor -> Stream.of(floor.getCoverAssetId(), floor.getFloorPlanAssetId())))
                .filter(Objects::nonNull)
                .toList());
    }

    private Map<Long, List<IndoorMarkerResponse>> loadFloorMarkers(List<IndoorFloor> floors, String localeHint) {
        List<Long> floorIds = floors.stream().map(IndoorFloor::getId).filter(Objects::nonNull).toList();
        if (floorIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<IndoorNode> markers = indoorNodeMapper.selectList(new LambdaQueryWrapper<IndoorNode>()
                .in(IndoorNode::getFloorId, floorIds)
                .eq(IndoorNode::getStatus, "published")
                .orderByAsc(IndoorNode::getSortOrder)
                .orderByAsc(IndoorNode::getId));
        if (markers.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, ContentAsset> assets = catalogFoundationService.getPublishedAssetsByIds(markers.stream()
                .flatMap(marker -> Stream.of(marker.getIconAssetId(), marker.getAnimationAssetId()))
                .filter(Objects::nonNull)
                .toList());
        return markers.stream().collect(Collectors.groupingBy(
                IndoorNode::getFloorId,
                LinkedHashMap::new,
                Collectors.mapping(marker -> toMarkerResponse(marker, assets, localeHint), Collectors.toList())
        ));
    }

    private IndoorMarkerResponse toMarkerResponse(IndoorNode marker, Map<Long, ContentAsset> assets, String localeHint) {
        return IndoorMarkerResponse.builder()
                .id(marker.getId())
                .markerCode(marker.getMarkerCode())
                .nodeType(marker.getNodeType())
                .name(localizedContentSupport.resolveText(localeHint, marker.getNodeNameZh(), marker.getNodeNameEn(), marker.getNodeNameZht(), marker.getNodeNamePt()))
                .description(localizedContentSupport.resolveText(localeHint, marker.getDescriptionZh(), marker.getDescriptionEn(), marker.getDescriptionZht(), marker.getDescriptionPt()))
                .relativeX(marker.getRelativeX())
                .relativeY(marker.getRelativeY())
                .relatedPoiId(marker.getRelatedPoiId())
                .iconUrl(resolveAssetUrl(assets, marker.getIconAssetId(), marker.getIcon()))
                .animationUrl(resolveAssetUrl(assets, marker.getAnimationAssetId(), null))
                .linkedEntityType(marker.getLinkedEntityType())
                .linkedEntityId(marker.getLinkedEntityId())
                .tagsJson(marker.getTags())
                .popupConfigJson(marker.getPopupConfigJson())
                .displayConfigJson(marker.getDisplayConfigJson())
                .metadataJson(marker.getMetadataJson())
                .sortOrder(marker.getSortOrder())
                .status(marker.getStatus())
                .build();
    }

    private String resolveAssetUrl(Map<Long, ContentAsset> assets, Long assetId, String fallback) {
        String assetUrl = localizedContentSupport.resolveAssetUrl(assets, assetId);
        return assetUrl.isBlank() ? fallback : assetUrl;
    }
}
