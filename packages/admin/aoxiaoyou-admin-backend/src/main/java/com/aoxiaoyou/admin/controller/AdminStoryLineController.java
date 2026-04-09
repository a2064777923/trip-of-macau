package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminStoryLineUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryLineDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminStoryLineListItemResponse;
import com.aoxiaoyou.admin.service.AdminStoryLineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台故事线管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/storylines")
public class AdminStoryLineController {

    private final AdminStoryLineService adminStoryLineService;

    @Operation(summary = "后台分页查询故事线")
    @GetMapping
    public ApiResponse<PageResponse<AdminStoryLineListItemResponse>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminStoryLineService.page(pageNum, pageSize, keyword, status));
    }

    @Operation(summary = "后台查看故事线详情")
    @GetMapping("/{storylineId}")
    public ApiResponse<AdminStoryLineDetailResponse> detail(@PathVariable Long storylineId) {
        return ApiResponse.success(adminStoryLineService.detail(storylineId));
    }

    @Operation(summary = "后台创建故事线")
    @PostMapping
    public ApiResponse<AdminStoryLineDetailResponse> create(@Valid @RequestBody AdminStoryLineUpsertRequest.Upsert request) {
        return ApiResponse.success(adminStoryLineService.create(request));
    }

    @Operation(summary = "后台更新故事线")
    @PutMapping("/{storylineId}")
    public ApiResponse<AdminStoryLineDetailResponse> update(@PathVariable Long storylineId, @Valid @RequestBody AdminStoryLineUpsertRequest.Upsert request) {
        return ApiResponse.success(adminStoryLineService.update(storylineId, request));
    }

    @Operation(summary = "后台删除故事线")
    @DeleteMapping("/{storylineId}")
    public ApiResponse<Boolean> delete(@PathVariable Long storylineId) {
        adminStoryLineService.delete(storylineId);
        return ApiResponse.success(Boolean.TRUE);
    }
}
