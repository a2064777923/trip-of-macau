package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryLineResponse;
import com.aoxiaoyou.tripofmacau.service.StoryLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "故事线")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/story-lines")
public class StoryLineController {

    private final StoryLineService storyLineService;

    @Operation(summary = "获取已发布故事线列表")
    @GetMapping
    public ApiResponse<List<StoryLineResponse>> list() {
        return ApiResponse.success(storyLineService.listPublished());
    }
}
