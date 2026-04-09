package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.AdminActivityResponse;
import com.aoxiaoyou.admin.entity.Activity;
import com.aoxiaoyou.admin.mapper.ActivityMapper;
import com.aoxiaoyou.admin.service.AdminOperationsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminOperationsServiceImpl implements AdminOperationsService {

    private final ActivityMapper activityMapper;

    @Override
    public PageResponse<AdminActivityResponse> pageActivities(long pageNum, long pageSize, String keyword, String status) {
        Page<Activity> page = activityMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Activity>()
                        .like(StringUtils.hasText(keyword), Activity::getTitle, keyword)
                        .eq(StringUtils.hasText(status), Activity::getStatus, status)
                        .orderByDesc(Activity::getCreatedAt));

        Page<AdminActivityResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(item -> AdminActivityResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .title(item.getTitle())
                .description(item.getDescription())
                .coverUrl(item.getCoverUrl())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .status(item.getStatus())
                .participationCount(item.getParticipationCount())
                .createdAt(item.getCreatedAt())
                .build()).toList());
        return PageResponse.of(result);
    }
}
