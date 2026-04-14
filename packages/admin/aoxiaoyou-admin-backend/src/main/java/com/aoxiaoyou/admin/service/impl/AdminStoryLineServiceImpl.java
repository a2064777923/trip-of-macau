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
import com.aoxiaoyou.admin.service.AdminStoryLineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminStoryLineServiceImpl implements AdminStoryLineService {

    private final StoryLineMapper storyLineMapper;
    private final StoryChapterMapper storyChapterMapper;
    private final CityMapper cityMapper;

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
        verifyCity(request.getCityId());
        StoryLine storyLine = new StoryLine();
        applyRequest(storyLine, request);
        storyLineMapper.insert(storyLine);
        return toDetail(requireStoryline(storyLine.getId()));
    }

    @Override
    public AdminStoryLineDetailResponse update(Long storylineId, AdminStoryLineUpsertRequest.Upsert request) {
        verifyCity(request.getCityId());
        StoryLine storyLine = requireStoryline(storylineId);
        applyRequest(storyLine, request);
        storyLineMapper.updateById(storyLine);
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

    private void applyRequest(StoryLine storyLine, AdminStoryLineUpsertRequest.Upsert request) {
        storyLine.setCityId(request.getCityId());
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
        City city = storyLine.getCityId() == null ? null : cityMapper.selectById(storyLine.getCityId());
        return AdminStoryLineListItemResponse.builder()
                .storylineId(storyLine.getId())
                .cityId(storyLine.getCityId())
                .cityName(city == null ? null : city.getNameZh())
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
        City city = storyLine.getCityId() == null ? null : cityMapper.selectById(storyLine.getCityId());
        return AdminStoryLineDetailResponse.builder()
                .storylineId(storyLine.getId())
                .cityId(storyLine.getCityId())
                .cityName(city == null ? null : city.getNameZh())
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
}
