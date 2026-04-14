package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryLineResponse;
import com.aoxiaoyou.tripofmacau.service.StoryLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Public Storylines")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/story-lines")
public class StoryLineController {

    private final StoryLineService storyLineService;

    @Operation(summary = "List published storylines")
    @GetMapping
    public ApiResponse<List<StoryLineResponse>> list(
            @Parameter(description = "Locale hint such as zh-Hans / zh-Hant / en")
            @RequestParam(required = false) String locale
    ) {
        return ApiResponse.success(storyLineService.listPublished(locale));
    }

    @Operation(summary = "Get storyline detail")
    @GetMapping("/{storyLineId}")
    public ApiResponse<StoryLineResponse> detail(
            @PathVariable Long storyLineId,
            @RequestParam(required = false) String locale
    ) {
        return ApiResponse.success(storyLineService.getDetail(storyLineId, locale));
    }
}
