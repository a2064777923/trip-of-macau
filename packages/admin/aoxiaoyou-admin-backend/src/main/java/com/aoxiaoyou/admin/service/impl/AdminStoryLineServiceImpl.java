package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminStoryLineUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryLineDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminStoryLineListItemResponse;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.StoryChapter;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.StoryChapterMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminContentRelationService;
import com.aoxiaoyou.admin.service.AdminStoryLineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdminStoryLineServiceImpl implements AdminStoryLineService {

    private final StoryLineMapper storyLineMapper;
    private final StoryChapterMapper storyChapterMapper;
    private final CityMapper cityMapper;
    private final SubMapMapper subMapMapper;
    private final AdminContentRelationService adminContentRelationService;

    @Override
    public PageResponse<AdminStoryLineListItemResponse> page(long pageNum, long pageSize, String keyword, String status) {
        Page<StoryLine> page = storyLineMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<StoryLine>()
                        .and(StringUtils.hasText(keyword), q -> q
                                .like(StoryLine::getCode, keyword)
                                .or().like(StoryLine::getNameZh, keyword)
                                .or().like(StoryLine::getNameEn, keyword)
                                .or().like(StoryLine::getNameZht, keyword)
                                .or().like(StoryLine::getNamePt, keyword))
                        .eq(StringUtils.hasText(status), StoryLine::getStatus, status)
                        .orderByAsc(StoryLine::getSortOrder)
                        .orderByAsc(StoryLine::getId));

        Page<AdminStoryLineListItemResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toListItem).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminStoryLineDetailResponse detail(Long storylineId) {
        return toDetail(requireStoryline(storylineId));
    }

    @Override
    public AdminStoryLineDetailResponse create(AdminStoryLineUpsertRequest.Upsert request) {
        StorylineRelationPayload relationPayload = resolveStorylineRelations(request);
        StoryLine storyLine = new StoryLine();
        applyRequest(storyLine, request, relationPayload);
        storyLineMapper.insert(storyLine);
        syncRelations(storyLine.getId(), relationPayload);
        return toDetail(requireStoryline(storyLine.getId()));
    }

    @Override
    public AdminStoryLineDetailResponse update(Long storylineId, AdminStoryLineUpsertRequest.Upsert request) {
        StorylineRelationPayload relationPayload = resolveStorylineRelations(request);
        StoryLine storyLine = requireStoryline(storylineId);
        applyRequest(storyLine, request, relationPayload);
        storyLineMapper.updateById(storyLine);
        syncRelations(storylineId, relationPayload);
        return toDetail(requireStoryline(storylineId));
    }

    @Override
    public void delete(Long storylineId) {
        requireStoryline(storylineId);
        storyLineMapper.deleteById(storylineId);
    }

    private StoryLine requireStoryline(Long storylineId) {
        StoryLine storyLine = storyLineMapper.selectById(storylineId);
        if (storyLine == null) {
            throw new BusinessException(4042, "Storyline not found");
        }
        return storyLine;
    }

    private void verifyCity(Long cityId) {
        if (cityId != null && cityMapper.selectById(cityId) == null) {
            throw new BusinessException(4043, "City not found");
        }
    }

    private void applyRequest(StoryLine storyLine, AdminStoryLineUpsertRequest.Upsert request, StorylineRelationPayload relationPayload) {
        storyLine.setCityId(relationPayload.primaryCityId());
        storyLine.setCode(request.getCode());
        storyLine.setNameZh(request.getNameZh());
        storyLine.setNameEn(request.getNameEn());
        storyLine.setNameZht(request.getNameZht());
        storyLine.setNamePt(request.getNamePt());
        storyLine.setDescriptionZh(request.getDescriptionZh());
        storyLine.setDescriptionEn(request.getDescriptionEn());
        storyLine.setDescriptionZht(request.getDescriptionZht());
        storyLine.setDescriptionPt(request.getDescriptionPt());
        storyLine.setEstimatedMinutes(request.getEstimatedMinutes() == null ? 0 : request.getEstimatedMinutes());
        storyLine.setDifficulty(StringUtils.hasText(request.getDifficulty()) ? request.getDifficulty() : "easy");
        storyLine.setCoverAssetId(request.getCoverAssetId());
        storyLine.setBannerAssetId(request.getBannerAssetId());
        storyLine.setRewardBadgeZh(request.getRewardBadgeZh());
        storyLine.setRewardBadgeEn(request.getRewardBadgeEn());
        storyLine.setRewardBadgeZht(request.getRewardBadgeZht());
        storyLine.setRewardBadgePt(request.getRewardBadgePt());
        storyLine.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        storyLine.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        storyLine.setPublishedAt(parseDateTime(request.getPublishedAt()));
    }

    private AdminStoryLineListItemResponse toListItem(StoryLine storyLine) {
        List<Long> cityBindingIds = getCityBindingIds(storyLine);
        List<Long> subMapBindingIds = adminContentRelationService.listTargetIds("storyline", storyLine.getId(), "sub_map_binding", "sub_map");
        return AdminStoryLineListItemResponse.builder()
                .storylineId(storyLine.getId())
                .cityId(cityBindingIds.isEmpty() ? storyLine.getCityId() : cityBindingIds.get(0))
                .cityName(joinCityNames(cityBindingIds))
                .cityBindings(cityBindingIds)
                .subMapBindings(subMapBindingIds)
                .code(storyLine.getCode())
                .nameZh(storyLine.getNameZh())
                .difficulty(storyLine.getDifficulty())
                .status(storyLine.getStatus())
                .estimatedMinutes(storyLine.getEstimatedMinutes())
                .totalChapters(countChapters(storyLine.getId()))
                .coverAssetId(storyLine.getCoverAssetId())
                .sortOrder(storyLine.getSortOrder())
                .createdAt(storyLine.getCreatedAt())
                .build();
    }

    private AdminStoryLineDetailResponse toDetail(StoryLine storyLine) {
        List<Long> cityBindingIds = getCityBindingIds(storyLine);
        List<Long> subMapBindingIds = adminContentRelationService.listTargetIds("storyline", storyLine.getId(), "sub_map_binding", "sub_map");
        List<Long> attachmentAssetIds = adminContentRelationService.listTargetIds("storyline", storyLine.getId(), "attachment_asset", "asset");
        return AdminStoryLineDetailResponse.builder()
                .storylineId(storyLine.getId())
                .cityId(cityBindingIds.isEmpty() ? storyLine.getCityId() : cityBindingIds.get(0))
                .cityName(joinCityNames(cityBindingIds))
                .cityBindings(cityBindingIds)
                .subMapBindings(subMapBindingIds)
                .attachmentAssetIds(attachmentAssetIds)
                .code(storyLine.getCode())
                .nameZh(storyLine.getNameZh())
                .nameEn(storyLine.getNameEn())
                .nameZht(storyLine.getNameZht())
                .namePt(storyLine.getNamePt())
                .descriptionZh(storyLine.getDescriptionZh())
                .descriptionEn(storyLine.getDescriptionEn())
                .descriptionZht(storyLine.getDescriptionZht())
                .descriptionPt(storyLine.getDescriptionPt())
                .estimatedMinutes(storyLine.getEstimatedMinutes())
                .difficulty(storyLine.getDifficulty())
                .coverAssetId(storyLine.getCoverAssetId())
                .bannerAssetId(storyLine.getBannerAssetId())
                .rewardBadgeZh(storyLine.getRewardBadgeZh())
                .rewardBadgeEn(storyLine.getRewardBadgeEn())
                .rewardBadgeZht(storyLine.getRewardBadgeZht())
                .rewardBadgePt(storyLine.getRewardBadgePt())
                .status(storyLine.getStatus())
                .totalChapters(countChapters(storyLine.getId()))
                .sortOrder(storyLine.getSortOrder())
                .publishedAt(storyLine.getPublishedAt())
                .createdAt(storyLine.getCreatedAt())
                .updatedAt(storyLine.getUpdatedAt())
                .build();
    }

    private Integer countChapters(Long storylineId) {
        return Math.toIntExact(storyChapterMapper.selectCount(new LambdaQueryWrapper<StoryChapter>()
                .eq(StoryChapter::getStorylineId, storylineId)));
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }

    private StorylineRelationPayload resolveStorylineRelations(AdminStoryLineUpsertRequest.Upsert request) {
        List<Long> cityIds = normalizeIds(request.getCityBindings());
        if (cityIds.isEmpty() && request.getCityId() != null) {
            cityIds = List.of(request.getCityId());
        }
        cityIds.forEach(this::verifyCity);

        List<Long> subMapIds = normalizeIds(request.getSubMapBindings());
        if (!subMapIds.isEmpty()) {
            Map<Long, Long> subMapCityIds = subMapIds.stream()
                    .collect(LinkedHashMap::new, (map, id) -> {
                        var subMap = subMapMapper.selectById(id);
                        if (subMap == null) {
                            throw new BusinessException(4048, "Sub-map not found");
                        }
                        map.put(id, subMap.getCityId());
                    }, Map::putAll);
            if (!cityIds.isEmpty()) {
                boolean allCompatible = subMapCityIds.values().stream().allMatch(cityIds::contains);
                if (!allCompatible) {
                    throw new BusinessException(4049, "Sub-map bindings must belong to bound cities");
                }
            } else {
                cityIds = subMapCityIds.values().stream().filter(Objects::nonNull).distinct().toList();
            }
        }

        return new StorylineRelationPayload(
                cityIds.isEmpty() ? null : cityIds.get(0),
                cityIds,
                subMapIds,
                normalizeIds(request.getAttachmentAssetIds()));
    }

    private void syncRelations(Long storylineId, StorylineRelationPayload payload) {
        adminContentRelationService.syncTargetIds("storyline", storylineId, "city_binding", "city", payload.cityBindings());
        adminContentRelationService.syncTargetIds("storyline", storylineId, "sub_map_binding", "sub_map", payload.subMapBindings());
        adminContentRelationService.syncTargetIds("storyline", storylineId, "attachment_asset", "asset", payload.attachmentAssetIds());
    }

    private List<Long> getCityBindingIds(StoryLine storyLine) {
        if (storyLine.getId() == null) {
            return storyLine.getCityId() == null ? Collections.emptyList() : List.of(storyLine.getCityId());
        }
        List<Long> cityBindingIds = adminContentRelationService.listTargetIds("storyline", storyLine.getId(), "city_binding", "city");
        if (cityBindingIds.isEmpty() && storyLine.getCityId() != null) {
            return List.of(storyLine.getCityId());
        }
        return cityBindingIds;
    }

    private List<Long> normalizeIds(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        return values.stream().filter(Objects::nonNull).distinct().toList();
    }

    private String joinCityNames(List<Long> cityIds) {
        if (cityIds == null || cityIds.isEmpty()) {
            return null;
        }
        List<String> cityNames = new ArrayList<>();
        for (Long cityId : cityIds) {
            City city = cityMapper.selectById(cityId);
            if (city != null && StringUtils.hasText(city.getNameZh())) {
                cityNames.add(city.getNameZh());
            }
        }
        return cityNames.isEmpty() ? null : String.join(" / ", cityNames);
    }

    private record StorylineRelationPayload(
            Long primaryCityId,
            List<Long> cityBindings,
            List<Long> subMapBindings,
            List<Long> attachmentAssetIds
    ) {
    }
}
