package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminActivityUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminActivityResponse;
import com.aoxiaoyou.admin.service.AdminOperationsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台运营管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/operations")
public class AdminOperationsController {

    private final AdminOperationsService adminOperationsService;

    @Operation(summary = "分页查询活动 / 任务")
    @GetMapping("/activities")
    public ApiResponse<PageResponse<AdminActivityResponse>> pageActivities(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String activityType) {
        return ApiResponse.success(adminOperationsService.pageActivities(pageNum, pageSize, keyword, status, activityType));
    }

    @Operation(summary = "查看活动 / 任务详情")
    @GetMapping("/activities/{activityId}")
    public ApiResponse<AdminActivityResponse> getActivity(@PathVariable Long activityId) {
        return ApiResponse.success(adminOperationsService.getActivity(activityId));
    }

    @Operation(summary = "创建活动 / 任务")
    @PostMapping("/activities")
    public ApiResponse<AdminActivityResponse> createActivity(@Valid @RequestBody AdminActivityUpsertRequest.Upsert request) {
        return ApiResponse.success(adminOperationsService.createActivity(request));
    }

    @Operation(summary = "更新活动 / 任务")
    @PutMapping("/activities/{activityId}")
    public ApiResponse<AdminActivityResponse> updateActivity(
            @PathVariable Long activityId,
            @Valid @RequestBody AdminActivityUpsertRequest.Upsert request) {
        return ApiResponse.success(adminOperationsService.updateActivity(activityId, request));
    }

    @Operation(summary = "删除活动 / 任务")
    @DeleteMapping("/activities/{activityId}")
    public ApiResponse<Boolean> deleteActivity(@PathVariable Long activityId) {
        adminOperationsService.deleteActivity(activityId);
        return ApiResponse.success(Boolean.TRUE);
    }
}
