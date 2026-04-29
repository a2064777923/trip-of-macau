package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminStoryContentBlockUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryContentBlockResponse;
import com.aoxiaoyou.admin.service.AdminStoryContentBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "後台故事內容積木管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/content/blocks")
public class AdminStoryContentBlockController {

    private final AdminStoryContentBlockService adminStoryContentBlockService;

    @Operation(summary = "分頁查詢故事內容積木")
    @GetMapping
    public ApiResponse<PageResponse<AdminStoryContentBlockResponse>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String blockType,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminStoryContentBlockService.page(pageNum, pageSize, keyword, blockType, status));
    }

    @Operation(summary = "查詢單個故事內容積木")
    @GetMapping("/{blockId}")
    public ApiResponse<AdminStoryContentBlockResponse> detail(@PathVariable Long blockId) {
        return ApiResponse.success(adminStoryContentBlockService.detail(blockId));
    }

    @Operation(summary = "建立故事內容積木")
    @PostMapping
    public ApiResponse<AdminStoryContentBlockResponse> create(@Valid @RequestBody AdminStoryContentBlockUpsertRequest request) {
        return ApiResponse.success(adminStoryContentBlockService.create(request));
    }

    @Operation(summary = "更新故事內容積木")
    @PutMapping("/{blockId}")
    public ApiResponse<AdminStoryContentBlockResponse> update(
            @PathVariable Long blockId,
            @Valid @RequestBody AdminStoryContentBlockUpsertRequest request) {
        return ApiResponse.success(adminStoryContentBlockService.update(blockId, request));
    }

    @Operation(summary = "刪除故事內容積木")
    @DeleteMapping("/{blockId}")
    public ApiResponse<Boolean> delete(@PathVariable Long blockId) {
        adminStoryContentBlockService.delete(blockId);
        return ApiResponse.success(Boolean.TRUE);
    }
}
