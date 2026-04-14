package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminStoryChapterUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryChapterResponse;
import com.aoxiaoyou.admin.entity.StoryChapter;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.mapper.StoryChapterMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.service.AdminStoryChapterService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStoryChapterServiceImpl implements AdminStoryChapterService {

    private final StoryChapterMapper storyChapterMapper;
    private final StoryLineMapper storyLineMapper;

    @Override
    public PageResponse<AdminStoryChapterResponse> page(Long storylineId, long pageNum, long pageSize) {
        verifyStoryline(storylineId);
        Page<StoryChapter> page = storyChapterMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<StoryChapter>()
                        .eq(StoryChapter::getStorylineId, storylineId)
                        .orderByAsc(StoryChapter::getChapterOrder)
                        .orderByAsc(StoryChapter::getId));
        Page<AdminStoryChapterResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toResponse).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public List<AdminStoryChapterResponse> listByStoryline(Long storylineId) {
        verifyStoryline(storylineId);
        return storyChapterMapper.selectList(new LambdaQueryWrapper<StoryChapter>()
                        .eq(StoryChapter::getStorylineId, storylineId)
                        .orderByAsc(StoryChapter::getChapterOrder)
                        .orderByAsc(StoryChapter::getId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AdminStoryChapterResponse create(AdminStoryChapterUpsertRequest.Upsert request) {
        verifyStoryline(request.getStorylineId());
        StoryChapter chapter = new StoryChapter();
        applyRequest(chapter, request);
        storyChapterMapper.insert(chapter);
        return toResponse(requireChapter(chapter.getId()));
    }

    @Override
    public AdminStoryChapterResponse update(Long chapterId, AdminStoryChapterUpsertRequest.Upsert request) {
        verifyStoryline(request.getStorylineId());
        StoryChapter chapter = requireChapter(chapterId);
        applyRequest(chapter, request);
        storyChapterMapper.updateById(chapter);
        return toResponse(requireChapter(chapterId));
    }

    @Override
    public void delete(Long chapterId) {
        requireChapter(chapterId);
        storyChapterMapper.deleteById(chapterId);
    }

    private void applyRequest(StoryChapter chapter, AdminStoryChapterUpsertRequest.Upsert request) {
        chapter.setStorylineId(request.getStorylineId());
        chapter.setChapterOrder(request.getChapterOrder());
        chapter.setTitleZh(request.getTitleZh());
        chapter.setTitleEn(request.getTitleEn());
        chapter.setTitleZht(request.getTitleZht());
        chapter.setTitlePt(request.getTitlePt());
        chapter.setSummaryZh(request.getSummaryZh());
        chapter.setSummaryEn(request.getSummaryEn());
        chapter.setSummaryZht(request.getSummaryZht());
        chapter.setSummaryPt(request.getSummaryPt());
        chapter.setDetailZh(request.getDetailZh());
        chapter.setDetailEn(request.getDetailEn());
        chapter.setDetailZht(request.getDetailZht());
        chapter.setDetailPt(request.getDetailPt());
        chapter.setAchievementZh(request.getAchievementZh());
        chapter.setAchievementEn(request.getAchievementEn());
        chapter.setAchievementZht(request.getAchievementZht());
        chapter.setAchievementPt(request.getAchievementPt());
        chapter.setCollectibleZh(request.getCollectibleZh());
        chapter.setCollectibleEn(request.getCollectibleEn());
        chapter.setCollectibleZht(request.getCollectibleZht());
        chapter.setCollectiblePt(request.getCollectiblePt());
        chapter.setLocationNameZh(request.getLocationNameZh());
        chapter.setLocationNameEn(request.getLocationNameEn());
        chapter.setLocationNameZht(request.getLocationNameZht());
        chapter.setLocationNamePt(request.getLocationNamePt());
        chapter.setMediaAssetId(request.getMediaAssetId());
        chapter.setUnlockType(StringUtils.hasText(request.getUnlockType()) ? request.getUnlockType() : "sequence");
        chapter.setUnlockParamJson(request.getUnlockParamJson());
        chapter.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        chapter.setSortOrder(request.getSortOrder() == null ? request.getChapterOrder() : request.getSortOrder());
        chapter.setPublishedAt(parseDateTime(request.getPublishedAt()));
    }

    private StoryChapter requireChapter(Long chapterId) {
        StoryChapter chapter = storyChapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(4044, "Story chapter not found");
        }
        return chapter;
    }

    private StoryLine verifyStoryline(Long storylineId) {
        StoryLine storyLine = storyLineMapper.selectById(storylineId);
        if (storyLine == null) {
            throw new BusinessException(4042, "Storyline not found");
        }
        return storyLine;
    }

    private AdminStoryChapterResponse toResponse(StoryChapter chapter) {
        return AdminStoryChapterResponse.builder()
                .id(chapter.getId())
                .storylineId(chapter.getStorylineId())
                .chapterOrder(chapter.getChapterOrder())
                .titleZh(chapter.getTitleZh())
                .titleEn(chapter.getTitleEn())
                .titleZht(chapter.getTitleZht())
                .titlePt(chapter.getTitlePt())
                .summaryZh(chapter.getSummaryZh())
                .summaryEn(chapter.getSummaryEn())
                .summaryZht(chapter.getSummaryZht())
                .summaryPt(chapter.getSummaryPt())
                .detailZh(chapter.getDetailZh())
                .detailEn(chapter.getDetailEn())
                .detailZht(chapter.getDetailZht())
                .detailPt(chapter.getDetailPt())
                .achievementZh(chapter.getAchievementZh())
                .achievementEn(chapter.getAchievementEn())
                .achievementZht(chapter.getAchievementZht())
                .achievementPt(chapter.getAchievementPt())
                .collectibleZh(chapter.getCollectibleZh())
                .collectibleEn(chapter.getCollectibleEn())
                .collectibleZht(chapter.getCollectibleZht())
                .collectiblePt(chapter.getCollectiblePt())
                .locationNameZh(chapter.getLocationNameZh())
                .locationNameEn(chapter.getLocationNameEn())
                .locationNameZht(chapter.getLocationNameZht())
                .locationNamePt(chapter.getLocationNamePt())
                .mediaAssetId(chapter.getMediaAssetId())
                .unlockType(chapter.getUnlockType())
                .unlockParamJson(chapter.getUnlockParamJson())
                .status(chapter.getStatus())
                .sortOrder(chapter.getSortOrder())
                .publishedAt(chapter.getPublishedAt())
                .createdAt(chapter.getCreatedAt() == null ? null : chapter.getCreatedAt().toString())
                .updatedAt(chapter.getUpdatedAt() == null ? null : chapter.getUpdatedAt().toString())
                .build();
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }
}
