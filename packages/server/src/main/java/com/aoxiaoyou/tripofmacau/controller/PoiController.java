package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.common.api.PageResponse;
import com.aoxiaoyou.tripofmacau.dto.response.PoiResponse;
import com.aoxiaoyou.tripofmacau.service.PoiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "POI")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pois")
public class PoiController {

    private final PoiService poiService;

    @Operation(summary = "分页获取 POI 列表")
    @GetMapping
    public ApiResponse<PageResponse<PoiResponse>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) long pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") @Min(1) @Max(100) long pageSize,
            @Parameter(description = "故事线 ID") @RequestParam(required = false) Long storyLineId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return ApiResponse.success(poiService.pagePois(pageNum, pageSize, storyLineId, keyword));
    }

    @Operation(summary = "获取 POI 详情")
    @GetMapping("/{poiId}")
    public ApiResponse<PoiResponse> detail(@PathVariable Long poiId) {
        return ApiResponse.success(poiService.getDetail(poiId));
    }
}
