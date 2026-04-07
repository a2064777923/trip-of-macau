package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.dto.response.StoryLineResponse;
import com.aoxiaoyou.tripofmacau.entity.StoryLine;
import com.aoxiaoyou.tripofmacau.mapper.StoryLineMapper;
import com.aoxiaoyou.tripofmacau.service.StoryLineService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoryLineServiceImpl implements StoryLineService {

    private final StoryLineMapper storyLineMapper;

    @Override
    public List<StoryLineResponse> listPublished() {
        return storyLineMapper.selectList(new LambdaQueryWrapper<StoryLine>()
                        .eq(StoryLine::getStatus, "published")
                        .orderByAsc(StoryLine::getId))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private StoryLineResponse toResponse(StoryLine storyLine) {
        return StoryLineResponse.builder()
                .id(storyLine.getId())
                .code(storyLine.getCode())
                .nameZh(storyLine.getNameZh())
                .nameEn(storyLine.getNameEn())
                .description(storyLine.getDescription())
                .coverUrl(storyLine.getCoverUrl())
                .totalChapters(storyLine.getTotalChapters())
                .status(storyLine.getStatus())
                .build();
    }
}
