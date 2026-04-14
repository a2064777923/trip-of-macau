package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationResult;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationService;
import com.aoxiaoyou.admin.dto.request.AdminCityUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminCityResponse;
import com.aoxiaoyou.admin.dto.response.AdminSubMapResponse;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.SubMap;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminCityService;
import com.aoxiaoyou.admin.service.AdminSpatialAssetLinkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminCityServiceImpl implements AdminCityService {

    private final CityMapper cityMapper;
    private final SubMapMapper subMapMapper;
    private final CoordinateNormalizationService coordinateNormalizationService;
    private final AdminSpatialAssetLinkService adminSpatialAssetLinkService;

    @Override
    public PageResponse<AdminCityResponse> pageCities(long pageNum, long pageSize, String keyword, String status) {
        LambdaQueryWrapper<City> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(q -> q.like(City::getCode, keyword)
                    .or().like(City::getNameZh, keyword)
                    .or().like(City::getNameEn, keyword)
                    .or().like(City::getNameZht, keyword)
                    .or().like(City::getNamePt, keyword));
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(City::getStatus, status);
        }
        wrapper.orderByAsc(City::getSortOrder).orderByAsc(City::getId);

        Page<City> page = cityMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Page<AdminCityResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(city -> toResponse(city, true)).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminCityResponse getCityDetail(Long id) {
        return toResponse(requireCity(id), true);
    }

    @Override
    public AdminCityResponse createCity(AdminCityUpsertRequest request) {
        City city = new City();
        applyRequest(city, request == null ? null : request.getUpsert());
        if (!StringUtils.hasText(city.getStatus())) {
            city.setStatus("draft");
        }
        if ("published".equals(city.getStatus()) && city.getPublishedAt() == null) {
            city.setPublishedAt(LocalDateTime.now());
        }
        cityMapper.insert(city);
        adminSpatialAssetLinkService.syncLinks("city", city.getId(), request == null || request.getUpsert() == null
                ? Collections.emptyList()
                : request.getUpsert().getAttachments());
        return toResponse(requireCity(city.getId()), true);
    }

    @Override
    public AdminCityResponse updateCity(Long id, AdminCityUpsertRequest request) {
        City city = requireCity(id);
        applyRequest(city, request == null ? null : request.getUpsert());
        if ("published".equals(city.getStatus()) && city.getPublishedAt() == null) {
            city.setPublishedAt(LocalDateTime.now());
        }
        cityMapper.updateById(city);
        adminSpatialAssetLinkService.syncLinks("city", id, request == null || request.getUpsert() == null
                ? Collections.emptyList()
                : request.getUpsert().getAttachments());
        return toResponse(requireCity(id), true);
    }

    @Override
    public AdminCityResponse publishCity(Long id) {
        City city = requireCity(id);
        city.setStatus("published");
        city.setPublishedAt(LocalDateTime.now());
        cityMapper.updateById(city);
        return toResponse(city, true);
    }

    private City requireCity(Long id) {
        City city = cityMapper.selectById(id);
        if (city == null) {
            throw new BusinessException(4043, "City not found");
        }
        return city;
    }

    private void applyRequest(City city, AdminCityUpsertRequest.Upsert request) {
        if (request == null) {
            return;
        }
        city.setCode(request.getCode());
        city.setNameZh(request.getNameZh());
        city.setNameEn(request.getNameEn());
        city.setNameZht(request.getNameZht());
        city.setNamePt(request.getNamePt());
        city.setSubtitleZh(request.getSubtitleZh());
        city.setSubtitleEn(request.getSubtitleEn());
        city.setSubtitleZht(request.getSubtitleZht());
        city.setSubtitlePt(request.getSubtitlePt());
        city.setCountryCode(request.getCountryCode());
        city.setCustomCountryName(StringUtils.hasText(request.getCustomCountryName()) ? request.getCustomCountryName().trim() : null);
        applyCenterCoordinates(city, request);
        city.setDefaultZoom(request.getDefaultZoom() == null ? 13 : request.getDefaultZoom());
        city.setUnlockType(StringUtils.hasText(request.getUnlockType()) ? request.getUnlockType() : "default");
        city.setUnlockConditionJson(request.getUnlockConditionJson());
        city.setCoverAssetId(request.getCoverAssetId());
        city.setBannerAssetId(request.getBannerAssetId());
        city.setDescriptionZh(request.getDescriptionZh());
        city.setDescriptionEn(request.getDescriptionEn());
        city.setDescriptionZht(request.getDescriptionZht());
        city.setDescriptionPt(request.getDescriptionPt());
        city.setPopupConfigJson(request.getPopupConfigJson());
        city.setDisplayConfigJson(request.getDisplayConfigJson());
        city.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        city.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        city.setPublishedAt(parseDateTime(request.getPublishedAt()));
    }

    private void applyCenterCoordinates(City city, AdminCityUpsertRequest.Upsert request) {
        Double sourceLat = request.getSourceCenterLat() != null ? request.getSourceCenterLat() : request.getCenterLat();
        Double sourceLng = request.getSourceCenterLng() != null ? request.getSourceCenterLng() : request.getCenterLng();
        if (sourceLat == null || sourceLng == null) {
            city.setSourceCoordinateSystem(StringUtils.hasText(request.getSourceCoordinateSystem()) ? request.getSourceCoordinateSystem() : "GCJ02");
            city.setSourceCenterLat(null);
            city.setSourceCenterLng(null);
            city.setCenterLat(null);
            city.setCenterLng(null);
            return;
        }

        CoordinateNormalizationResult normalized = coordinateNormalizationService.normalizeToGcj02(
                request.getSourceCoordinateSystem(),
                BigDecimal.valueOf(sourceLat),
                BigDecimal.valueOf(sourceLng)
        );
        city.setSourceCoordinateSystem(normalized.getSourceCoordinateSystem().getCode());
        city.setSourceCenterLat(normalized.getSourceLatitude());
        city.setSourceCenterLng(normalized.getSourceLongitude());
        city.setCenterLat(normalized.getNormalizedLatitude());
        city.setCenterLng(normalized.getNormalizedLongitude());
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }

    private AdminCityResponse toResponse(City city, boolean includeChildren) {
        List<AdminSubMapResponse> subMaps = includeChildren
                ? subMapMapper.selectList(new LambdaQueryWrapper<SubMap>()
                .eq(SubMap::getCityId, city.getId())
                .orderByAsc(SubMap::getSortOrder)
                .orderByAsc(SubMap::getId))
                .stream()
                .map(subMap -> toSubMapResponse(subMap, city))
                .toList()
                : Collections.emptyList();
        return AdminCityResponse.builder()
                .id(city.getId())
                .code(city.getCode())
                .nameZh(city.getNameZh())
                .nameEn(city.getNameEn())
                .nameZht(city.getNameZht())
                .namePt(city.getNamePt())
                .subtitleZh(city.getSubtitleZh())
                .subtitleEn(city.getSubtitleEn())
                .subtitleZht(city.getSubtitleZht())
                .subtitlePt(city.getSubtitlePt())
                .countryCode(city.getCountryCode())
                .customCountryName(city.getCustomCountryName())
                .sourceCoordinateSystem(city.getSourceCoordinateSystem())
                .sourceCenterLat(city.getSourceCenterLat())
                .sourceCenterLng(city.getSourceCenterLng())
                .centerLat(city.getCenterLat())
                .centerLng(city.getCenterLng())
                .defaultZoom(city.getDefaultZoom())
                .unlockType(city.getUnlockType())
                .unlockConditionJson(city.getUnlockConditionJson())
                .coverAssetId(city.getCoverAssetId())
                .bannerAssetId(city.getBannerAssetId())
                .descriptionZh(city.getDescriptionZh())
                .descriptionEn(city.getDescriptionEn())
                .descriptionZht(city.getDescriptionZht())
                .descriptionPt(city.getDescriptionPt())
                .popupConfigJson(city.getPopupConfigJson())
                .displayConfigJson(city.getDisplayConfigJson())
                .subMaps(subMaps)
                .attachments(adminSpatialAssetLinkService.listLinks("city", city.getId()))
                .sortOrder(city.getSortOrder())
                .status(city.getStatus())
                .publishedAt(city.getPublishedAt())
                .build();
    }

    private AdminSubMapResponse toSubMapResponse(SubMap subMap, City city) {
        return AdminSubMapResponse.builder()
                .id(subMap.getId())
                .cityId(city.getId())
                .cityCode(city.getCode())
                .cityName(city.getNameZh())
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
}
