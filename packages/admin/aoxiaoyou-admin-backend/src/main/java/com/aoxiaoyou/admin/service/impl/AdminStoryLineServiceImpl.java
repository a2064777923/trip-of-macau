package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminStoryLineUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryLineDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminStoryLineListItemResponse;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.service.AdminStoryLineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminStoryLineServiceImpl implements AdminStoryLineService {

    private final StoryLineMapper storyLineMapper;

    @Override
    public PageResponse<AdminStoryLineListItemResponse> page(long pageNum, long pageSize, String keyword, String status) {
        Page<StoryLine> page = storyLineMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<StoryLine>()
                        .like(StringUtils.hasText(keyword), StoryLine::getNameZh, keyword)
                        .eq(StringUtils.hasText(status), StoryLine::getStatus, status)
                        .orderByDesc(StoryLine::getCreatedAt));

        Page<AdminStoryLineListItemResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toListItem).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminStoryLineDetailResponse detail(Long storylineId) {
        StoryLine storyLine = storyLineMapper.selectById(storylineId);
        if (storyLine == null) {
            throw new BusinessException(4042, "故事线不存在");
        }
        return toDetail(storyLine);
    }

    @Override
    public AdminStoryLineDetailResponse create(AdminStoryLineUpsertRequest.Upsert request) {
        StoryLine storyLine = new StoryLine();
        applyRequest(storyLine, request);
        storyLineMapper.insert(storyLine);
        return toDetail(storyLineMapper.selectById(storyLine.getId()));
    }

    @Override
    public AdminStoryLineDetailResponse update(Long storylineId, AdminStoryLineUpsertRequest.Upsert request) {
        StoryLine storyLine = storyLineMapper.selectById(storylineId);
        if (storyLine == null) {
            throw new BusinessException(4042, "故事线不存在");
        }
        applyRequest(storyLine, request);
        storyLineMapper.updateById(storyLine);
        return toDetail(storyLineMapper.selectById(storylineId));
    }

    @Override
    public void delete(Long storylineId) {
        if (storyLineMapper.selectById(storylineId) == null) {
            throw new BusinessException(4042, "故事线不存在");
        }
        storyLineMapper.deleteById(storylineId);
    }

    private void applyRequest(StoryLine storyLine, AdminStoryLineUpsertRequest.Upsert request) {
        storyLine.setCode(request.getCode());
        storyLine.setNameZh(request.getNameZh());
        storyLine.setNameEn(request.getNameEn());
        storyLine.setDescription(request.getDescription());
        storyLine.setCoverUrl(request.getCoverUrl());
        storyLine.setBannerUrl(StringUtils.hasText(request.getBannerUrl()) ? request.getBannerUrl() : request.getCoverUrl());
        storyLine.setTotalChapters(request.getTotalChapters() == null ? 0 : request.getTotalChapters());
        storyLine.setCategory(StringUtils.hasText(request.getCategory()) ? request.getCategory() : "historical");
        storyLine.setDifficulty(StringUtils.hasText(request.getDifficulty()) ? request.getDifficulty() : "easy");
        storyLine.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes() == null ? 60 : request.getEstimatedDurationMinutes());
        storyLine.setTags(request.getTags());
        storyLine.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        storyLine.setPublishAt(parseDateTime(request.getPublishAt()));
        storyLine.setStartAt(parseDateTime(request.getStartAt()));
        storyLine.setEndAt(parseDateTime(request.getEndAt()));
        storyLine.setParticipationCount(storyLine.getParticipationCount() == null ? 0 : storyLine.getParticipationCount());
        storyLine.setCompletionCount(storyLine.getCompletionCount() == null ? 0 : storyLine.getCompletionCount());
        storyLine.setAverageCompletionTime(storyLine.getAverageCompletionTime() == null ? 0 : storyLine.getAverageCompletionTime());
    }

    private AdminStoryLineListItemResponse toListItem(StoryLine storyLine) {
        return AdminStoryLineListItemResponse.builder()
                .storylineId(storyLine.getId())
                .code(storyLine.getCode())
                .name(storyLine.getNameZh())
                .description(storyLine.getDescription())
                .coverImageUrl(storyLine.getCoverUrl())
                .category(storyLine.getCategory())
                .difficulty(storyLine.getDifficulty())
                .status(storyLine.getStatus())
                .poiCount(storyLine.getTotalChapters())
                .participationCount(storyLine.getParticipationCount())
                .completionCount(storyLine.getCompletionCount())
                .createdAt(storyLine.getCreatedAt())
                .build();
    }

    private AdminStoryLineDetailResponse toDetail(StoryLine storyLine) {
        return AdminStoryLineDetailResponse.builder()
                .storylineId(storyLine.getId())
                .code(storyLine.getCode())
                .name(storyLine.getNameZh())
                .description(storyLine.getDescription())
                .coverImageUrl(storyLine.getCoverUrl())
                .bannerImageUrl(storyLine.getBannerUrl())
                .category(storyLine.getCategory())
                .difficulty(storyLine.getDifficulty())
                .estimatedDurationMinutes(storyLine.getEstimatedDurationMinutes())
                .tags(parseTags(storyLine.getTags()))
                .status(storyLine.getStatus())
                .totalChapters(storyLine.getTotalChapters())
                .participationCount(storyLine.getParticipationCount())
                .completionCount(storyLine.getCompletionCount())
                .averageCompletionTime(storyLine.getAverageCompletionTime())
                .publishAt(storyLine.getPublishAt())
                .startAt(storyLine.getStartAt())
                .endAt(storyLine.getEndAt())
                .createdAt(storyLine.getCreatedAt())
                .updatedAt(storyLine.getUpdatedAt())
                .build();
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private List<String> parseTags(String raw) {
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

