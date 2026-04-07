package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.api.PageResponse;
import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.dto.response.PoiResponse;
import com.aoxiaoyou.tripofmacau.entity.Poi;
import com.aoxiaoyou.tripofmacau.mapper.PoiMapper;
import com.aoxiaoyou.tripofmacau.service.PoiService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PoiServiceImpl implements PoiService {

    private final PoiMapper poiMapper;

    @Override
    public PageResponse<PoiResponse> pagePois(long pageNum, long pageSize, Long storyLineId, String keyword) {
        Page<Poi> page = poiMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Poi>()
                        .eq(storyLineId != null, Poi::getStoryLineId, storyLineId)
                        .and(StringUtils.hasText(keyword), wrapper -> wrapper
                                .like(Poi::getNameZh, keyword)
                                .or()
                                .like(Poi::getNameEn, keyword)
                                .or()
                                .like(Poi::getNameZht, keyword))
                        .orderByAsc(Poi::getId));
        Page<PoiResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toResponse).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public PoiResponse getDetail(Long poiId) {
        Poi poi = poiMapper.selectById(poiId);
        if (poi == null) {
            throw new BusinessException(4041, "POI 不存在");
        }
        return toResponse(poi);
    }

    private PoiResponse toResponse(Poi poi) {
        return PoiResponse.builder()
                .id(poi.getId())
                .nameZh(poi.getNameZh())
                .nameEn(poi.getNameEn())
                .nameZht(poi.getNameZht())
                .latitude(poi.getLatitude())
                .longitude(poi.getLongitude())
                .address(poi.getAddress())
                .categoryId(poi.getCategoryId())
                .triggerRadius(poi.getTriggerRadius())
                .importance(poi.getImportance())
                .storyLineId(poi.getStoryLineId())
                .stampType(poi.getStampType())
                .description(poi.getDescription())
                .build();
    }
}
