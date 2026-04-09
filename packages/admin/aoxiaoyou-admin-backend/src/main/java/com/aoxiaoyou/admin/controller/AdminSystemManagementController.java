package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminRewardUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminMapTileResponse;
import com.aoxiaoyou.admin.dto.response.AdminOperationLogResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardResponse;
import com.aoxiaoyou.admin.dto.response.AdminSystemConfigResponse;
import com.aoxiaoyou.admin.service.AdminSystemManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台系统管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/system")
public class AdminSystemManagementController {

    private final AdminSystemManagementService adminSystemManagementService;

    @Operation(summary = "分页查询奖励配置")
    @GetMapping("/rewards")
    public ApiResponse<PageResponse<AdminRewardResponse>> pageRewards(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(adminSystemManagementService.pageRewards(pageNum, pageSize, status));
    }

    @Operation(summary = "创建奖励配置")
    @PostMapping("/rewards")
    public ApiResponse<AdminRewardResponse> createReward(@Valid @RequestBody AdminRewardUpsertRequest.Upsert request) {
        return ApiResponse.success(adminSystemManagementService.createReward(request));
    }

    @Operation(summary = "更新奖励配置")
    @PutMapping("/rewards/{rewardId}")
    public ApiResponse<AdminRewardResponse> updateReward(@PathVariable Long rewardId, @Valid @RequestBody AdminRewardUpsertRequest.Upsert request) {
        return ApiResponse.success(adminSystemManagementService.updateReward(rewardId, request));
    }

    @Operation(summary = "删除奖励配置")
    @DeleteMapping("/rewards/{rewardId}")
    public ApiResponse<Boolean> deleteReward(@PathVariable Long rewardId) {
        adminSystemManagementService.deleteReward(rewardId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @Operation(summary = "分页查询审计日志")
    @GetMapping("/audit-logs")
    public ApiResponse<PageResponse<AdminOperationLogResponse>> pageAuditLogs(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String module) {
        return ApiResponse.success(adminSystemManagementService.pageAuditLogs(pageNum, pageSize, module));
    }

    @Operation(summary = "分页查询系统配置")
    @GetMapping("/configs")
    public ApiResponse<PageResponse<AdminSystemConfigResponse>> pageConfigs(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminSystemManagementService.pageConfigs(pageNum, pageSize, keyword));
    }

    @Operation(summary = "分页查询地图瓦片配置")
    @GetMapping("/map-tiles")
    public ApiResponse<PageResponse<AdminMapTileResponse>> pageMapTiles(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(adminSystemManagementService.pageMapTiles(pageNum, pageSize));
    }
}
