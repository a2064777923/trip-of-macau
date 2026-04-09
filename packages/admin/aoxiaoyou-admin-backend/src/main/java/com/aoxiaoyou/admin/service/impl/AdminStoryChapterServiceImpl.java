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
                        .eq(StoryChapter::getStoryLineId, storylineId)
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
                        .eq(StoryChapter::getStoryLineId, storylineId)
                        .orderByAsc(StoryChapter::getChapterOrder)
                        .orderByAsc(StoryChapter::getId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AdminStoryChapterResponse create(AdminStoryChapterUpsertRequest.Upsert request) {
        verifyStoryline(request.getStoryLineId());
        StoryChapter chapter = new StoryChapter();
        applyRequest(chapter, request);
        chapter.setOpenid("");
        storyChapterMapper.insert(chapter);
        recalculateChapterCount(request.getStoryLineId());
        return toResponse(storyChapterMapper.selectById(chapter.getId()));
    }

    @Override
    public AdminStoryChapterResponse update(Long chapterId, AdminStoryChapterUpsertRequest.Upsert request) {
        StoryChapter chapter = requireChapter(chapterId);
        verifyStoryline(request.getStoryLineId());
        Long oldStorylineId = chapter.getStoryLineId();
        applyRequest(chapter, request);
        storyChapterMapper.updateById(chapter);
        recalculateChapterCount(oldStorylineId);
        recalculateChapterCount(request.getStoryLineId());
        return toResponse(storyChapterMapper.selectById(chapterId));
    }

    @Override
    public void delete(Long chapterId) {
        StoryChapter chapter = requireChapter(chapterId);
        Long storylineId = chapter.getStoryLineId();
        storyChapterMapper.deleteById(chapterId);
        recalculateChapterCount(storylineId);
    }

    private void applyRequest(StoryChapter chapter, AdminStoryChapterUpsertRequest.Upsert request) {
        chapter.setStoryLineId(request.getStoryLineId());
        chapter.setChapterOrder(request.getChapterOrder());
        chapter.setTitleZh(request.getTitleZh());
        chapter.setTitleEn(request.getTitleEn());
        chapter.setTitleZht(request.getTitleZht());
        chapter.setMediaType(StringUtils.hasText(request.getMediaType()) ? request.getMediaType() : "image");
        chapter.setMediaUrl(request.getMediaUrl());
        chapter.setScriptZh(request.getScriptZh());
        chapter.setScriptEn(request.getScriptEn());
        chapter.setScriptZht(request.getScriptZht());
        chapter.setUnlockType(StringUtils.hasText(request.getUnlockType()) ? request.getUnlockType() : "sequential");
        chapter.setUnlockParam(request.getUnlockParam());
        chapter.setDuration(request.getDuration() == null ? 180 : request.getDuration());
    }

    private StoryChapter requireChapter(Long chapterId) {
        StoryChapter chapter = storyChapterMapper.selectById(chapterId);
        if (chapter == null) {
            throw new BusinessException(4044, "章节不存在");
        }
        return chapter;
    }

    private StoryLine verifyStoryline(Long storylineId) {
        StoryLine storyLine = storyLineMapper.selectById(storylineId);
        if (storyLine == null) {
            throw new BusinessException(4042, "故事线不存在");
        }
        return storyLine;
    }

    private void recalculateChapterCount(Long storylineId) {
        if (storylineId == null) {
            return;
        }
        long count = storyChapterMapper.selectCount(new LambdaQueryWrapper<StoryChapter>()
                .eq(StoryChapter::getStoryLineId, storylineId));
        StoryLine storyLine = storyLineMapper.selectById(storylineId);
        if (storyLine != null) {
            storyLine.setTotalChapters((int) count);
            storyLineMapper.updateById(storyLine);
        }
    }

    private AdminStoryChapterResponse toResponse(StoryChapter chapter) {
        return AdminStoryChapterResponse.builder()
                .id(chapter.getId())
                .storyLineId(chapter.getStoryLineId())
                .chapterOrder(chapter.getChapterOrder())
                .titleZh(chapter.getTitleZh())
                .titleEn(chapter.getTitleEn())
                .titleZht(chapter.getTitleZht())
                .mediaType(chapter.getMediaType())
                .mediaUrl(chapter.getMediaUrl())
                .scriptZh(chapter.getScriptZh())
                .scriptEn(chapter.getScriptEn())
                .scriptZht(chapter.getScriptZht())
                .unlockType(chapter.getUnlockType())
                .unlockParam(chapter.getUnlockParam())
                .duration(chapter.getDuration())
                .createdAt(chapter.getCreatedAt() == null ? null : chapter.getCreatedAt().toString())
                .updatedAt(chapter.getUpdatedAt() == null ? null : chapter.getUpdatedAt().toString())
                .build();
    }
}
