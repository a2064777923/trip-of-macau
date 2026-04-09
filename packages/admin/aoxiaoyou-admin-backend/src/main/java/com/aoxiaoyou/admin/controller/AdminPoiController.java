package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminPoiUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminPoiDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminPoiListItemResponse;
import com.aoxiaoyou.admin.service.AdminPoiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台 POI 管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/pois")
public class AdminPoiController {

    private final AdminPoiService adminPoiService;

    @Operation(summary = "后台分页查询 POI")
    @GetMapping
    public ApiResponse<PageResponse<AdminPoiListItemResponse>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long storylineId) {
        return ApiResponse.success(adminPoiService.pagePois(pageNum, pageSize, keyword, storylineId));
    }

    @Operation(summary = "后台查看 POI 详情")
    @GetMapping("/{poiId}")
    public ApiResponse<AdminPoiDetailResponse> detail(@PathVariable Long poiId) {
        return ApiResponse.success(adminPoiService.getDetail(poiId));
    }

    @Operation(summary = "后台创建 POI")
    @PostMapping
    public ApiResponse<AdminPoiDetailResponse> create(@Valid @RequestBody AdminPoiUpsertRequest request) {
        return ApiResponse.success(adminPoiService.create(request));
    }

    @Operation(summary = "后台更新 POI")
    @PutMapping("/{poiId}")
    public ApiResponse<AdminPoiDetailResponse> update(@PathVariable Long poiId, @Valid @RequestBody AdminPoiUpsertRequest request) {
        return ApiResponse.success(adminPoiService.update(poiId, request));
    }

    @Operation(summary = "后台删除 POI")
    @DeleteMapping("/{poiId}")
    public ApiResponse<Boolean> delete(@PathVariable Long poiId) {
        adminPoiService.delete(poiId);
        return ApiResponse.success(Boolean.TRUE);
    }
}
