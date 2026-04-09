package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminPoiUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminPoiDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminPoiListItemResponse;
import com.aoxiaoyou.admin.entity.Poi;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.service.AdminPoiService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPoiServiceImpl implements AdminPoiService {

    private final PoiMapper poiMapper;
    private final StoryLineMapper storyLineMapper;

    @Override
    public PageResponse<AdminPoiListItemResponse> pagePois(long pageNum, long pageSize, String keyword, Long storylineId) {
        Page<Poi> page = poiMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Poi>()
                        .eq(storylineId != null, Poi::getStoryLineId, storylineId)
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper
                                .like(Poi::getNameZh, keyword)
                                .or()
                                .like(Poi::getNameEn, keyword)
                                .or()
                                .like(Poi::getNameZht, keyword)
                                .or()
                                .like(Poi::getSubtitle, keyword))
                        .orderByDesc(Poi::getCreatedAt));

        Page<AdminPoiListItemResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toListItem).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminPoiDetailResponse getDetail(Long poiId) {
        Poi poi = poiMapper.selectById(poiId);
        if (poi == null) {
            throw new BusinessException(4041, "POI 不存在");
        }
        return toDetail(poi);
    }

    @Override
    public AdminPoiDetailResponse create(AdminPoiUpsertRequest request) {
        Poi poi = new Poi();
        applyRequest(poi, request);
        poiMapper.insert(poi);
        return toDetail(poiMapper.selectById(poi.getId()));
    }

    @Override
    public AdminPoiDetailResponse update(Long poiId, AdminPoiUpsertRequest request) {
        Poi poi = poiMapper.selectById(poiId);
        if (poi == null) {
            throw new BusinessException(4041, "POI 不存在");
        }
        applyRequest(poi, request);
        poiMapper.updateById(poi);
        return toDetail(poiMapper.selectById(poiId));
    }

    @Override
    public void delete(Long poiId) {
        if (poiMapper.selectById(poiId) == null) {
            throw new BusinessException(4041, "POI 不存在");
        }
        poiMapper.deleteById(poiId);
    }

    private void applyRequest(Poi poi, AdminPoiUpsertRequest request) {
        poi.setNameZh(request.getNameZh());
        poi.setNameEn(request.getNameEn());
        poi.setNameZht(request.getNameZht());
        poi.setSubtitle(request.getSubtitle());
        poi.setRegionCode(StringUtils.hasText(request.getRegionCode()) ? request.getRegionCode() : "macau_central");
        poi.setPoiType(StringUtils.hasText(request.getPoiType()) ? request.getPoiType() : "story_point");
        poi.setLatitude(request.getLatitude());
        poi.setLongitude(request.getLongitude());
        poi.setAddress(request.getAddress());
        poi.setCategoryId(request.getCategoryId());
        poi.setTriggerRadius(request.getTriggerRadius() == null ? 50 : request.getTriggerRadius());
        poi.setCheckInMethod(StringUtils.hasText(request.getCheckInMethod()) ? request.getCheckInMethod() : "gps_only");
        poi.setImportance(StringUtils.hasText(request.getImportance()) ? request.getImportance() : "normal");
        poi.setStoryLineId(request.getStoryLineId());
        poi.setStampType(request.getStampType());
        poi.setDescription(request.getDescription());
        poi.setCoverImageUrl(request.getCoverImageUrl());
        poi.setImageUrls(request.getImageUrls());
        poi.setAudioGuideUrl(request.getAudioGuideUrl());
        poi.setVideoUrl(request.getVideoUrl());
        poi.setArContentUrl(request.getArContentUrl());
        poi.setTags(request.getTags());
        poi.setDifficulty(StringUtils.hasText(request.getDifficulty()) ? request.getDifficulty() : "easy");
        poi.setOpenTime(StringUtils.hasText(request.getOpenTime()) ? request.getOpenTime() : "全天");
        poi.setSuggestedVisitMinutes(request.getSuggestedVisitMinutes() == null ? 30 : request.getSuggestedVisitMinutes());
        poi.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "published");
        poi.setCheckInCount(poi.getCheckInCount() == null ? 0L : poi.getCheckInCount());
        poi.setFavoriteCount(poi.getFavoriteCount() == null ? 0L : poi.getFavoriteCount());
    }

    private AdminPoiListItemResponse toListItem(Poi poi) {
        StoryLine storyLine = poi.getStoryLineId() == null ? null : storyLineMapper.selectById(poi.getStoryLineId());
        return AdminPoiListItemResponse.builder()
                .poiId(poi.getId())
                .name(poi.getNameZh())
                .subtitle(poi.getSubtitle())
                .regionCode(poi.getRegionCode())
                .regionName(resolveRegionName(poi.getRegionCode()))
                .poiType(poi.getPoiType())
                .latitude(poi.getLatitude())
                .longitude(poi.getLongitude())
                .categoryId(poi.getCategoryId())
                .categoryName(poi.getCategoryId() == null ? null : "分类#" + poi.getCategoryId())
                .importance(poi.getImportance())
                .geofenceRadius(poi.getTriggerRadius())
                .status(poi.getStatus())
                .storylineId(poi.getStoryLineId())
                .storylineName(storyLine == null ? null : storyLine.getNameZh())
                .checkInCount(poi.getCheckInCount() == null ? 0L : poi.getCheckInCount())
                .createdAt(poi.getCreatedAt())
                .build();
    }

    private AdminPoiDetailResponse toDetail(Poi poi) {
        StoryLine storyLine = poi.getStoryLineId() == null ? null : storyLineMapper.selectById(poi.getStoryLineId());
        return AdminPoiDetailResponse.builder()
                .poiId(poi.getId())
                .name(poi.getNameZh())
                .subtitle(poi.getSubtitle())
                .description(poi.getDescription())
                .regionCode(poi.getRegionCode())
                .regionName(resolveRegionName(poi.getRegionCode()))
                .latitude(poi.getLatitude())
                .longitude(poi.getLongitude())
                .gcj02Latitude(poi.getLatitude())
                .gcj02Longitude(poi.getLongitude())
                .address(poi.getAddress())
                .geofenceRadius(poi.getTriggerRadius())
                .checkInMethod(poi.getCheckInMethod())
                .coverImageUrl(poi.getCoverImageUrl())
                .imageUrls(parseJsonArray(poi.getImageUrls()))
                .audioGuideUrl(poi.getAudioGuideUrl())
                .videoUrl(poi.getVideoUrl())
                .arContentUrl(poi.getArContentUrl())
                .poiType(poi.getPoiType())
                .tags(parseJsonArray(poi.getTags()))
                .difficulty(poi.getDifficulty())
                .openTime(poi.getOpenTime())
                .suggestedVisitMinutes(poi.getSuggestedVisitMinutes())
                .status(poi.getStatus())
                .checkInCount(poi.getCheckInCount() == null ? 0L : poi.getCheckInCount())
                .favoriteCount(poi.getFavoriteCount() == null ? 0L : poi.getFavoriteCount())
                .categoryId(poi.getCategoryId())
                .categoryName(poi.getCategoryId() == null ? null : "分类#" + poi.getCategoryId())
                .storylineId(poi.getStoryLineId())
                .storylineName(storyLine == null ? null : storyLine.getNameZh())
                .stampType(poi.getStampType())
                .createdAt(poi.getCreatedAt())
                .updatedAt(poi.getUpdatedAt())
                .build();
    }

    private String resolveRegionName(String regionCode) {
        if (!StringUtils.hasText(regionCode)) {
            return "澳门半岛";
        }
        return switch (regionCode) {
            case "macau_taipa" -> "氹仔";
            case "macau_cotai" -> "路氹";
            default -> "澳门半岛";
        };
    }

    private List<String> parseJsonArray(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Collections.emptyList();
        }
        String normalized = raw.trim();
        if (normalized.startsWith("[") && normalized.endsWith("]")) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }
        if (!StringUtils.hasText(normalized)) {
            return Collections.emptyList();
        }
        return Arrays.stream(normalized.split(","))
                .map(item -> item.replace("\"", "").trim())
                .filter(StringUtils::hasText)
                .toList();
    }
}

