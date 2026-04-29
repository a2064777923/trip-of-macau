package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.content.ContentLifecycleStatusSupport;
import com.aoxiaoyou.admin.common.enums.ContentStatus;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationResult;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationService;
import com.aoxiaoyou.admin.dto.request.AdminSubMapUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminSubMapResponse;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.SubMap;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminSpatialAssetLinkService;
import com.aoxiaoyou.admin.service.AdminSubMapService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminSubMapServiceImpl implements AdminSubMapService {

    private final SubMapMapper subMapMapper;
    private final CityMapper cityMapper;
    private final CoordinateNormalizationService coordinateNormalizationService;
    private final AdminSpatialAssetLinkService adminSpatialAssetLinkService;

    @Override
    public PageResponse<AdminSubMapResponse> pageSubMaps(long pageNum, long pageSize, Long cityId, String keyword, String status) {
        Page<SubMap> page = subMapMapper.selectPage(new Page<>(pageNum, pageSize), new LambdaQueryWrapper<SubMap>()
                .eq(cityId != null, SubMap::getCityId, cityId)
                .eq(StringUtils.hasText(status), SubMap::getStatus, status)
                .and(StringUtils.hasText(keyword), wrapper -> wrapper
                        .like(SubMap::getCode, keyword)
                        .or().like(SubMap::getNameZh, keyword)
                        .or().like(SubMap::getNameEn, keyword)
                        .or().like(SubMap::getNameZht, keyword)
                        .or().like(SubMap::getNamePt, keyword))
                .orderByAsc(SubMap::getSortOrder)
                .orderByAsc(SubMap::getId));
        Page<AdminSubMapResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toResponse).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminSubMapResponse getSubMapDetail(Long id) {
        return toResponse(requireSubMap(id));
    }

    @Override
    public AdminSubMapResponse createSubMap(AdminSubMapUpsertRequest request) {
        requireCity(request.getCityId());
        SubMap subMap = new SubMap();
        applyRequest(subMap, request);
        subMap.setStatus(ContentStatus.DRAFT.getCode());
        subMap.setPublishedAt(null);
        subMapMapper.insert(subMap);
        adminSpatialAssetLinkService.syncLinks("sub_map", subMap.getId(), request.getAttachments());
        return toResponse(requireSubMap(subMap.getId()));
    }

    @Override
    public AdminSubMapResponse updateSubMap(Long id, AdminSubMapUpsertRequest request) {
        requireCity(request.getCityId());
        SubMap subMap = requireSubMap(id);
        applyRequest(subMap, request);
        subMapMapper.updateById(subMap);
        adminSpatialAssetLinkService.syncLinks("sub_map", id, request.getAttachments());
        return toResponse(requireSubMap(id));
    }

    @Override
    public AdminSubMapResponse publishSubMap(Long id) {
        return updateSubMapStatus(id, ContentStatus.PUBLISHED.getCode());
    }

    @Override
    public AdminSubMapResponse updateSubMapStatus(Long id, String status) {
        SubMap subMap = requireSubMap(id);
        applyLifecycleStatus(subMap, ContentLifecycleStatusSupport.parseManuallyOperableStatus(status));
        subMapMapper.updateById(subMap);
        return toResponse(requireSubMap(id));
    }

    private City requireCity(Long cityId) {
        City city = cityMapper.selectById(cityId);
        if (city == null) {
            throw new BusinessException(4043, "City not found");
        }
        return city;
    }

    private SubMap requireSubMap(Long id) {
        SubMap subMap = subMapMapper.selectById(id);
        if (subMap == null) {
            throw new BusinessException(4044, "Sub-map not found");
        }
        return subMap;
    }

    private void applyRequest(SubMap subMap, AdminSubMapUpsertRequest request) {
        subMap.setCityId(request.getCityId());
        subMap.setCode(request.getCode());
        subMap.setNameZh(request.getNameZh());
        subMap.setNameEn(request.getNameEn());
        subMap.setNameZht(request.getNameZht());
        subMap.setNamePt(request.getNamePt());
        subMap.setSubtitleZh(request.getSubtitleZh());
        subMap.setSubtitleEn(request.getSubtitleEn());
        subMap.setSubtitleZht(request.getSubtitleZht());
        subMap.setSubtitlePt(request.getSubtitlePt());
        subMap.setDescriptionZh(request.getDescriptionZh());
        subMap.setDescriptionEn(request.getDescriptionEn());
        subMap.setDescriptionZht(request.getDescriptionZht());
        subMap.setDescriptionPt(request.getDescriptionPt());
        subMap.setCoverAssetId(request.getCoverAssetId());
        applyCoordinates(subMap, request);
        subMap.setBoundsJson(request.getBoundsJson());
        subMap.setPopupConfigJson(request.getPopupConfigJson());
        subMap.setDisplayConfigJson(request.getDisplayConfigJson());
        subMap.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
    }

    private void applyCoordinates(SubMap subMap, AdminSubMapUpsertRequest request) {
        Double sourceLat = request.getSourceCenterLat() != null ? request.getSourceCenterLat() : request.getCenterLat();
        Double sourceLng = request.getSourceCenterLng() != null ? request.getSourceCenterLng() : request.getCenterLng();
        if (sourceLat == null || sourceLng == null) {
            subMap.setSourceCoordinateSystem(StringUtils.hasText(request.getSourceCoordinateSystem()) ? request.getSourceCoordinateSystem() : "GCJ02");
            subMap.setSourceCenterLat(null);
            subMap.setSourceCenterLng(null);
            subMap.setCenterLat(null);
            subMap.setCenterLng(null);
            return;
        }

        CoordinateNormalizationResult normalized = coordinateNormalizationService.normalizeToGcj02(
                request.getSourceCoordinateSystem(),
                BigDecimal.valueOf(sourceLat),
                BigDecimal.valueOf(sourceLng)
        );
        subMap.setSourceCoordinateSystem(normalized.getSourceCoordinateSystem().getCode());
        subMap.setSourceCenterLat(normalized.getSourceLatitude());
        subMap.setSourceCenterLng(normalized.getSourceLongitude());
        subMap.setCenterLat(normalized.getNormalizedLatitude());
        subMap.setCenterLng(normalized.getNormalizedLongitude());
    }

    private AdminSubMapResponse toResponse(SubMap subMap) {
        City city = cityMapper.selectById(subMap.getCityId());
        return AdminSubMapResponse.builder()
                .id(subMap.getId())
                .cityId(subMap.getCityId())
                .cityCode(city == null ? null : city.getCode())
                .cityName(city == null ? null : city.getNameZh())
                .code(subMap.getCode())
                .nameZh(subMap.getNameZh())
                .nameEn(subMap.getNameEn())
                .nameZht(subMap.getNameZht())
                .namePt(subMap.getNamePt())
                .subtitleZh(subMap.getSubtitleZh())
                .subtitleEn(subMap.getSubtitleEn())
                .subtitleZht(subMap.getSubtitleZht())
                .subtitlePt(subMap.getSubtitlePt())
                .descriptionZh(subMap.getDescriptionZh())
                .descriptionEn(subMap.getDescriptionEn())
                .descriptionZht(subMap.getDescriptionZht())
                .descriptionPt(subMap.getDescriptionPt())
                .coverAssetId(subMap.getCoverAssetId())
                .sourceCoordinateSystem(subMap.getSourceCoordinateSystem())
                .sourceCenterLat(subMap.getSourceCenterLat())
                .sourceCenterLng(subMap.getSourceCenterLng())
                .centerLat(subMap.getCenterLat())
                .centerLng(subMap.getCenterLng())
                .boundsJson(subMap.getBoundsJson())
                .popupConfigJson(subMap.getPopupConfigJson())
                .displayConfigJson(subMap.getDisplayConfigJson())
                .sortOrder(subMap.getSortOrder())
                .status(subMap.getStatus())
                .publishedAt(subMap.getPublishedAt())
                .attachments(adminSpatialAssetLinkService.listLinks("sub_map", subMap.getId()))
                .build();
    }

    private void applyLifecycleStatus(SubMap subMap, ContentStatus status) {
        subMap.setStatus(status.getCode());
        subMap.setPublishedAt(ContentLifecycleStatusSupport.resolvePublishedAt(status, subMap.getPublishedAt()));
    }
}
