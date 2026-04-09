package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.TestAccountBatchStampGrantRequest;
import com.aoxiaoyou.admin.dto.request.TestAccountLevelAdjustRequest;
import com.aoxiaoyou.admin.dto.request.TestAccountMockLocationRequest;
import com.aoxiaoyou.admin.dto.request.TestAccountProgressResetRequest;
import com.aoxiaoyou.admin.dto.request.TestAccountStampGrantRequest;
import com.aoxiaoyou.admin.dto.response.AdminOperationLogResponse;
import com.aoxiaoyou.admin.dto.response.AdminTestAccountListItemResponse;
import com.aoxiaoyou.admin.dto.response.AdminTestStampSummaryResponse;
import com.aoxiaoyou.admin.service.AdminTestConsoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台测试控制台")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/test-console")
public class AdminTestConsoleController {

    private final AdminTestConsoleService adminTestConsoleService;

    @Operation(summary = "分页查询测试账号")
    @GetMapping("/accounts")
    public ApiResponse<PageResponse<AdminTestAccountListItemResponse>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String testGroup) {
        return ApiResponse.success(adminTestConsoleService.page(pageNum, pageSize, testGroup));
    }

    @Operation(summary = "设置测试账号模拟定位")
    @PutMapping("/accounts/{testAccountId}/mock")
    public ApiResponse<AdminTestAccountListItemResponse> toggleMockLocation(
            @PathVariable Long testAccountId,
            @Valid @RequestBody TestAccountMockLocationRequest request,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.success(adminTestConsoleService.toggleMockLocation(testAccountId, request, operatorId(httpServletRequest), operatorName(httpServletRequest), httpServletRequest.getRemoteAddr()));
    }

    @Operation(summary = "调整测试账号等级")
    @PostMapping("/accounts/{testAccountId}/level")
    public ApiResponse<AdminTestAccountListItemResponse> adjustLevel(
            @PathVariable Long testAccountId,
            @Valid @RequestBody TestAccountLevelAdjustRequest request,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.success(adminTestConsoleService.adjustLevel(testAccountId, request, operatorId(httpServletRequest), operatorName(httpServletRequest), httpServletRequest.getRemoteAddr()));
    }

    @Operation(summary = "发放测试账号印章")
    @PostMapping("/accounts/{testAccountId}/stamps/grant")
    public ApiResponse<AdminTestAccountListItemResponse> grantStamp(
            @PathVariable Long testAccountId,
            @Valid @RequestBody TestAccountStampGrantRequest request,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.success(adminTestConsoleService.grantStamp(testAccountId, request, operatorId(httpServletRequest), operatorName(httpServletRequest), httpServletRequest.getRemoteAddr()));
    }

    @Operation(summary = "批量发放测试账号印章")
    @PostMapping("/accounts/{testAccountId}/stamps/batch-grant")
    public ApiResponse<AdminTestAccountListItemResponse> batchGrantStamp(
            @PathVariable Long testAccountId,
            @Valid @RequestBody TestAccountBatchStampGrantRequest request,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.success(adminTestConsoleService.batchGrantStamp(testAccountId, request, operatorId(httpServletRequest), operatorName(httpServletRequest), httpServletRequest.getRemoteAddr()));
    }

    @Operation(summary = "清空测试账号印章")
    @DeleteMapping("/accounts/{testAccountId}/stamps")
    public ApiResponse<AdminTestAccountListItemResponse> clearStamps(
            @PathVariable Long testAccountId,
            @RequestParam(required = false) String reason,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.success(adminTestConsoleService.clearStamps(testAccountId, operatorId(httpServletRequest), operatorName(httpServletRequest), httpServletRequest.getRemoteAddr(), reason));
    }

    @Operation(summary = "查询测试账号印章概览")
    @GetMapping("/accounts/{testAccountId}/stamps/summary")
    public ApiResponse<AdminTestStampSummaryResponse> stampSummary(@PathVariable Long testAccountId) {
        return ApiResponse.success(adminTestConsoleService.stampSummary(testAccountId));
    }

    @Operation(summary = "重置测试账号进度")
    @PostMapping("/accounts/{testAccountId}/progress/reset")
    public ApiResponse<AdminTestAccountListItemResponse> resetProgress(
            @PathVariable Long testAccountId,
            @RequestBody TestAccountProgressResetRequest request,
            HttpServletRequest httpServletRequest) {
        return ApiResponse.success(adminTestConsoleService.resetProgress(testAccountId, request, operatorId(httpServletRequest), operatorName(httpServletRequest), httpServletRequest.getRemoteAddr()));
    }

    @Operation(summary = "查询测试控制台操作日志")
    @GetMapping("/accounts/{testAccountId}/logs")
    public ApiResponse<PageResponse<AdminOperationLogResponse>> logs(
            @PathVariable Long testAccountId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return ApiResponse.success(adminTestConsoleService.operationLogs(testAccountId, pageNum, pageSize));
    }

    private Long operatorId(HttpServletRequest request) {
        return (Long) request.getAttribute("adminUserId");
    }

    private String operatorName(HttpServletRequest request) {
        return (String) request.getAttribute("adminUsername");
    }
}
