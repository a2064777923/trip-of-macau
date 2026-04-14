package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminStoryChapterUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminStoryChapterResponse;
import com.aoxiaoyou.admin.service.AdminStoryChapterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "后台故事章节管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/storylines/{storylineId}/chapters")
public class AdminStoryChapterController {

    private final AdminStoryChapterService adminStoryChapterService;

    @Operation(summary = "分页查询故事章节")
    @GetMapping
    public ApiResponse<PageResponse<AdminStoryChapterResponse>> page(
            @PathVariable Long storylineId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "50") long pageSize) {
        return ApiResponse.success(adminStoryChapterService.page(storylineId, pageNum, pageSize));
    }

    @Operation(summary = "查询故事章节列表")
    @GetMapping("/all")
    public ApiResponse<List<AdminStoryChapterResponse>> list(@PathVariable Long storylineId) {
        return ApiResponse.success(adminStoryChapterService.listByStoryline(storylineId));
    }

    @Operation(summary = "创建故事章节")
    @PostMapping
    public ApiResponse<AdminStoryChapterResponse> create(
            @PathVariable Long storylineId,
            @Valid @RequestBody AdminStoryChapterUpsertRequest.Upsert request) {
        request.setStorylineId(storylineId);
        return ApiResponse.success(adminStoryChapterService.create(request));
    }

    @Operation(summary = "更新故事章节")
    @PutMapping("/{chapterId}")
    public ApiResponse<AdminStoryChapterResponse> update(
            @PathVariable Long storylineId,
            @PathVariable Long chapterId,
            @Valid @RequestBody AdminStoryChapterUpsertRequest.Upsert request) {
        request.setStorylineId(storylineId);
        return ApiResponse.success(adminStoryChapterService.update(chapterId, request));
    }

    @Operation(summary = "删除故事章节")
    @DeleteMapping("/{chapterId}")
    public ApiResponse<Boolean> delete(@PathVariable Long chapterId) {
        adminStoryChapterService.delete(chapterId);
        return ApiResponse.success(Boolean.TRUE);
    }
}
