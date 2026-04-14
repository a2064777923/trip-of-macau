package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationResult;
import com.aoxiaoyou.admin.common.spatial.CoordinateNormalizationService;
import com.aoxiaoyou.admin.dto.request.AdminPoiUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminPoiDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminPoiListItemResponse;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.Poi;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.entity.SubMap;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminPoiService;
import com.aoxiaoyou.admin.service.AdminSpatialAssetLinkService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminPoiServiceImpl implements AdminPoiService {

    private final PoiMapper poiMapper;
    private final StoryLineMapper storyLineMapper;
    private final CityMapper cityMapper;
    private final SubMapMapper subMapMapper;
    private final CoordinateNormalizationService coordinateNormalizationService;
    private final AdminSpatialAssetLinkService adminSpatialAssetLinkService;

    @Override
    public PageResponse<AdminPoiListItemResponse> pagePois(long pageNum, long pageSize, String keyword, Long cityId, Long subMapId, Long storylineId) {
        Page<Poi> page = poiMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Poi>()
                        .eq(cityId != null, Poi::getCityId, cityId)
                        .eq(subMapId != null, Poi::getSubMapId, subMapId)
                        .eq(storylineId != null, Poi::getStorylineId, storylineId)
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper
                                .like(Poi::getCode, keyword)
                                .or().like(Poi::getNameZh, keyword)
                                .or().like(Poi::getNameEn, keyword)
                                .or().like(Poi::getNameZht, keyword)
                                .or().like(Poi::getNamePt, keyword)
                                .or().like(Poi::getSubtitleZh, keyword))
                        .orderByAsc(Poi::getSortOrder)
                        .orderByAsc(Poi::getId));

        Page<AdminPoiListItemResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toListItem).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminPoiDetailResponse getDetail(Long poiId) {
        return toDetail(requirePoi(poiId));
    }

    @Override
    public AdminPoiDetailResponse create(AdminPoiUpsertRequest request) {
        verifyCity(request.getCityId());
        verifySubMap(request.getCityId(), request.getSubMapId());
        verifyStoryline(request.getStorylineId());
        Poi poi = new Poi();
        applyRequest(poi, request);
        poiMapper.insert(poi);
        adminSpatialAssetLinkService.syncLinks("poi", poi.getId(), request.getAttachments());
        return toDetail(requirePoi(poi.getId()));
    }

    @Override
    public AdminPoiDetailResponse update(Long poiId, AdminPoiUpsertRequest request) {
        verifyCity(request.getCityId());
        verifySubMap(request.getCityId(), request.getSubMapId());
        verifyStoryline(request.getStorylineId());
        Poi poi = requirePoi(poiId);
        applyRequest(poi, request);
        poiMapper.updateById(poi);
        adminSpatialAssetLinkService.syncLinks("poi", poiId, request.getAttachments());
        return toDetail(requirePoi(poiId));
    }

    @Override
    public void delete(Long poiId) {
        requirePoi(poiId);
        poiMapper.deleteById(poiId);
    }

    private Poi requirePoi(Long poiId) {
        Poi poi = poiMapper.selectById(poiId);
        if (poi == null) {
            throw new BusinessException(4041, "POI not found");
        }
        return poi;
    }

    private void verifyCity(Long cityId) {
        if (cityId == null || cityMapper.selectById(cityId) == null) {
            throw new BusinessException(4043, "City not found");
        }
    }

    private void verifySubMap(Long cityId, Long subMapId) {
        if (subMapId == null) {
            return;
        }
        SubMap subMap = subMapMapper.selectById(subMapId);
        if (subMap == null) {
            throw new BusinessException(4044, "Sub-map not found");
        }
        if (cityId != null && !cityId.equals(subMap.getCityId())) {
            throw new BusinessException(4045, "Sub-map does not belong to the selected city");
        }
    }

    private void verifyStoryline(Long storylineId) {
        if (storylineId != null && storyLineMapper.selectById(storylineId) == null) {
            throw new BusinessException(4042, "Storyline not found");
        }
    }

    private void applyRequest(Poi poi, AdminPoiUpsertRequest request) {
        poi.setCityId(request.getCityId());
        poi.setSubMapId(request.getSubMapId());
        poi.setStorylineId(request.getStorylineId());
        poi.setCode(request.getCode());
        poi.setNameZh(request.getNameZh());
        poi.setNameEn(request.getNameEn());
        poi.setNameZht(request.getNameZht());
        poi.setNamePt(request.getNamePt());
        poi.setSubtitleZh(request.getSubtitleZh());
        poi.setSubtitleEn(request.getSubtitleEn());
        poi.setSubtitleZht(request.getSubtitleZht());
        poi.setSubtitlePt(request.getSubtitlePt());
        applyCoordinates(poi, request);
        poi.setAddressZh(request.getAddressZh());
        poi.setAddressEn(request.getAddressEn());
        poi.setAddressZht(request.getAddressZht());
        poi.setAddressPt(request.getAddressPt());
        poi.setTriggerRadius(request.getTriggerRadius() == null ? 50 : request.getTriggerRadius());
        poi.setManualCheckinRadius(request.getManualCheckinRadius() == null ? 200 : request.getManualCheckinRadius());
        poi.setStaySeconds(request.getStaySeconds() == null ? 30 : request.getStaySeconds());
        poi.setCategoryCode(request.getCategoryCode());
        poi.setDifficulty(StringUtils.hasText(request.getDifficulty()) ? request.getDifficulty() : "easy");
        poi.setDistrictZh(request.getDistrictZh());
        poi.setDistrictEn(request.getDistrictEn());
        poi.setDistrictZht(request.getDistrictZht());
        poi.setDistrictPt(request.getDistrictPt());
        poi.setCoverAssetId(request.getCoverAssetId());
        poi.setMapIconAssetId(request.getMapIconAssetId());
        poi.setAudioAssetId(request.getAudioAssetId());
        poi.setDescriptionZh(request.getDescriptionZh());
        poi.setDescriptionEn(request.getDescriptionEn());
        poi.setDescriptionZht(request.getDescriptionZht());
        poi.setDescriptionPt(request.getDescriptionPt());
        poi.setIntroTitleZh(request.getIntroTitleZh());
        poi.setIntroTitleEn(request.getIntroTitleEn());
        poi.setIntroTitleZht(request.getIntroTitleZht());
        poi.setIntroTitlePt(request.getIntroTitlePt());
        poi.setIntroSummaryZh(request.getIntroSummaryZh());
        poi.setIntroSummaryEn(request.getIntroSummaryEn());
        poi.setIntroSummaryZht(request.getIntroSummaryZht());
        poi.setIntroSummaryPt(request.getIntroSummaryPt());
        poi.setPopupConfigJson(request.getPopupConfigJson());
        poi.setDisplayConfigJson(request.getDisplayConfigJson());
        poi.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        poi.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        poi.setPublishedAt(parseDateTime(request.getPublishedAt()));
    }

    private void applyCoordinates(Poi poi, AdminPoiUpsertRequest request) {
        BigDecimal sourceLat = request.getSourceLatitude() != null ? request.getSourceLatitude() : request.getLatitude();
        BigDecimal sourceLng = request.getSourceLongitude() != null ? request.getSourceLongitude() : request.getLongitude();
        if (sourceLat == null || sourceLng == null) {
            throw new BusinessException(4046, "POI coordinates are required");
        }

        CoordinateNormalizationResult normalized = coordinateNormalizationService.normalizeToGcj02(
                request.getSourceCoordinateSystem(),
                sourceLat,
                sourceLng
        );
        poi.setSourceCoordinateSystem(normalized.getSourceCoordinateSystem().getCode());
        poi.setSourceLatitude(normalized.getSourceLatitude());
        poi.setSourceLongitude(normalized.getSourceLongitude());
        poi.setLatitude(normalized.getNormalizedLatitude());
        poi.setLongitude(normalized.getNormalizedLongitude());
    }

    private AdminPoiListItemResponse toListItem(Poi poi) {
        City city = cityMapper.selectById(poi.getCityId());
        SubMap subMap = poi.getSubMapId() == null ? null : subMapMapper.selectById(poi.getSubMapId());
        StoryLine storyLine = poi.getStorylineId() == null ? null : storyLineMapper.selectById(poi.getStorylineId());
        return AdminPoiListItemResponse.builder()
                .poiId(poi.getId())
                .cityId(poi.getCityId())
                .cityName(city == null ? null : city.getNameZh())
                .subMapId(poi.getSubMapId())
                .subMapCode(subMap == null ? null : subMap.getCode())
                .subMapName(subMap == null ? null : subMap.getNameZh())
                .storylineId(poi.getStorylineId())
                .storylineName(storyLine == null ? null : storyLine.getNameZh())
                .code(poi.getCode())
                .nameZh(poi.getNameZh())
                .subtitleZh(poi.getSubtitleZh())
                .categoryCode(poi.getCategoryCode())
                .difficulty(poi.getDifficulty())
                .sourceCoordinateSystem(poi.getSourceCoordinateSystem())
                .latitude(poi.getLatitude())
                .longitude(poi.getLongitude())
                .status(poi.getStatus())
                .sortOrder(poi.getSortOrder())
                .coverAssetId(poi.getCoverAssetId())
                .mapIconAssetId(poi.getMapIconAssetId())
                .createdAt(poi.getCreatedAt())
                .build();
    }

    private AdminPoiDetailResponse toDetail(Poi poi) {
        City city = cityMapper.selectById(poi.getCityId());
        SubMap subMap = poi.getSubMapId() == null ? null : subMapMapper.selectById(poi.getSubMapId());
        StoryLine storyLine = poi.getStorylineId() == null ? null : storyLineMapper.selectById(poi.getStorylineId());
        return AdminPoiDetailResponse.builder()
                .poiId(poi.getId())
                .cityId(poi.getCityId())
                .cityName(city == null ? null : city.getNameZh())
                .subMapId(poi.getSubMapId())
                .subMapCode(subMap == null ? null : subMap.getCode())
                .subMapName(subMap == null ? null : subMap.getNameZh())
                .storylineId(poi.getStorylineId())
                .storylineName(storyLine == null ? null : storyLine.getNameZh())
                .code(poi.getCode())
                .nameZh(poi.getNameZh())
                .nameEn(poi.getNameEn())
                .nameZht(poi.getNameZht())
                .namePt(poi.getNamePt())
                .subtitleZh(poi.getSubtitleZh())
                .subtitleEn(poi.getSubtitleEn())
                .subtitleZht(poi.getSubtitleZht())
                .subtitlePt(poi.getSubtitlePt())
                .sourceCoordinateSystem(poi.getSourceCoordinateSystem())
                .sourceLatitude(poi.getSourceLatitude())
                .sourceLongitude(poi.getSourceLongitude())
                .latitude(poi.getLatitude())
                .longitude(poi.getLongitude())
                .addressZh(poi.getAddressZh())
                .addressEn(poi.getAddressEn())
                .addressZht(poi.getAddressZht())
                .addressPt(poi.getAddressPt())
                .triggerRadius(poi.getTriggerRadius())
                .manualCheckinRadius(poi.getManualCheckinRadius())
                .staySeconds(poi.getStaySeconds())
                .categoryCode(poi.getCategoryCode())
                .difficulty(poi.getDifficulty())
                .districtZh(poi.getDistrictZh())
                .districtEn(poi.getDistrictEn())
                .districtZht(poi.getDistrictZht())
                .districtPt(poi.getDistrictPt())
                .coverAssetId(poi.getCoverAssetId())
                .mapIconAssetId(poi.getMapIconAssetId())
                .audioAssetId(poi.getAudioAssetId())
                .descriptionZh(poi.getDescriptionZh())
                .descriptionEn(poi.getDescriptionEn())
                .descriptionZht(poi.getDescriptionZht())
                .descriptionPt(poi.getDescriptionPt())
                .introTitleZh(poi.getIntroTitleZh())
                .introTitleEn(poi.getIntroTitleEn())
                .introTitleZht(poi.getIntroTitleZht())
                .introTitlePt(poi.getIntroTitlePt())
                .introSummaryZh(poi.getIntroSummaryZh())
                .introSummaryEn(poi.getIntroSummaryEn())
                .introSummaryZht(poi.getIntroSummaryZht())
                .introSummaryPt(poi.getIntroSummaryPt())
                .popupConfigJson(poi.getPopupConfigJson())
                .displayConfigJson(poi.getDisplayConfigJson())
                .attachments(adminSpatialAssetLinkService.listLinks("poi", poi.getId()))
                .status(poi.getStatus())
                .sortOrder(poi.getSortOrder())
                .publishedAt(poi.getPublishedAt())
                .createdAt(poi.getCreatedAt())
                .updatedAt(poi.getUpdatedAt())
                .build();
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }
}
