package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminCarryoverSettingsUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorRuntimeSettingsUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminMediaPolicySettingsUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRewardUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminTranslateRequest;
import com.aoxiaoyou.admin.dto.request.AdminTranslationSettingsUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminCarryoverSettingsResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuntimeSettingsResponse;
import com.aoxiaoyou.admin.dto.response.AdminMapTileResponse;
import com.aoxiaoyou.admin.dto.response.AdminMediaPolicySettingsResponse;
import com.aoxiaoyou.admin.dto.response.AdminOperationLogResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardResponse;
import com.aoxiaoyou.admin.dto.response.AdminSystemConfigResponse;
import com.aoxiaoyou.admin.dto.response.AdminTranslateResponse;
import com.aoxiaoyou.admin.dto.response.AdminTranslationSettingsResponse;
import com.aoxiaoyou.admin.service.AdminSystemManagementService;
import com.aoxiaoyou.admin.service.AdminTranslationService;
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
    private final AdminTranslationService adminTranslationService;

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

    @GetMapping("/carryover-settings")
    public ApiResponse<AdminCarryoverSettingsResponse> getCarryoverSettings() {
        return ApiResponse.success(adminSystemManagementService.getCarryoverSettings());
    }

    @PutMapping("/carryover-settings")
    public ApiResponse<AdminCarryoverSettingsResponse> updateCarryoverSettings(
            @RequestBody AdminCarryoverSettingsUpsertRequest request) {
        return ApiResponse.success(adminSystemManagementService.updateCarryoverSettings(request));
    }

    @GetMapping("/media-policy")
    public ApiResponse<AdminMediaPolicySettingsResponse> getMediaPolicySettings() {
        return ApiResponse.success(adminSystemManagementService.getMediaPolicySettings());
    }

    @PutMapping("/media-policy")
    public ApiResponse<AdminMediaPolicySettingsResponse> updateMediaPolicySettings(
            @Valid @RequestBody AdminMediaPolicySettingsUpsertRequest request) {
        return ApiResponse.success(adminSystemManagementService.updateMediaPolicySettings(request));
    }

    @GetMapping("/indoor-runtime")
    public ApiResponse<AdminIndoorRuntimeSettingsResponse> getIndoorRuntimeSettings() {
        return ApiResponse.success(adminSystemManagementService.getIndoorRuntimeSettings());
    }

    @PutMapping("/indoor-runtime")
    public ApiResponse<AdminIndoorRuntimeSettingsResponse> updateIndoorRuntimeSettings(
            @RequestBody AdminIndoorRuntimeSettingsUpsertRequest request) {
        return ApiResponse.success(adminSystemManagementService.updateIndoorRuntimeSettings(request));
    }

    @Operation(summary = "获取翻译设置")
    @GetMapping("/translation-settings")
    public ApiResponse<AdminTranslationSettingsResponse> getTranslationSettings() {
        return ApiResponse.success(adminTranslationService.getSettings());
    }

    @Operation(summary = "更新翻译设置")
    @PutMapping("/translation-settings")
    public ApiResponse<AdminTranslationSettingsResponse> updateTranslationSettings(@RequestBody AdminTranslationSettingsUpsertRequest request) {
        return ApiResponse.success(adminTranslationService.updateSettings(request));
    }

    @Operation(summary = "执行多语言翻译")
    @PostMapping("/translate")
    public ApiResponse<AdminTranslateResponse> translate(@RequestBody AdminTranslateRequest request) {
        return ApiResponse.success(adminTranslationService.translate(request));
    }

    @Operation(summary = "分页查询地图瓦片配置")
    @GetMapping("/map-tiles")
    public ApiResponse<PageResponse<AdminMapTileResponse>> pageMapTiles(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(adminSystemManagementService.pageMapTiles(pageNum, pageSize));
    }
}
