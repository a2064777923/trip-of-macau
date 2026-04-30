package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminLifecycleOperationRequest;
import com.aoxiaoyou.admin.dto.response.AdminLifecycleOperationResponse;
import com.aoxiaoyou.admin.dto.response.AdminLifecycleTargetResponse;
import com.aoxiaoyou.admin.service.AdminLifecycleOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "生命週期與發布排程")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/operations/lifecycle")
public class AdminLifecycleOperationController {

    private final AdminLifecycleOperationService lifecycleOperationService;

    @Operation(summary = "查詢生命週期主體類型")
    @GetMapping("/target-types")
    public ApiResponse<List<AdminLifecycleTargetResponse.TargetType>> listTargetTypes() {
        return ApiResponse.success(lifecycleOperationService.listTargetTypes());
    }

    @Operation(summary = "查詢生命週期狀態選項")
    @GetMapping("/statuses")
    public ApiResponse<List<AdminLifecycleTargetResponse.StatusOption>> listStatuses() {
        return ApiResponse.success(lifecycleOperationService.listStatuses());
    }

    @Operation(summary = "查詢可操作內容主體")
    @GetMapping("/targets")
    public ApiResponse<PageResponse<AdminLifecycleTargetResponse.TargetSummary>> pageTargets(AdminLifecycleOperationRequest.TargetQuery query) {
        return ApiResponse.success(lifecycleOperationService.pageTargets(query));
    }

    @Operation(summary = "預覽生命週期操作影響")
    @PostMapping("/preview")
    public ApiResponse<AdminLifecycleOperationResponse.Preview> preview(@Valid @RequestBody AdminLifecycleOperationRequest.Preview request) {
        return ApiResponse.success(lifecycleOperationService.preview(request));
    }

    @Operation(summary = "建立生命週期操作")
    @PostMapping("/operations")
    public ApiResponse<AdminLifecycleOperationResponse.Summary> createOperation(
            @Valid @RequestBody AdminLifecycleOperationRequest.CreateOperation request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(lifecycleOperationService.createOperation(request, adminUserId(servletRequest), adminUsername(servletRequest)));
    }

    @Operation(summary = "立即套用生命週期操作")
    @PostMapping("/operations/{operationId}/apply")
    public ApiResponse<AdminLifecycleOperationResponse.Summary> applyOperation(
            @PathVariable Long operationId,
            @RequestBody(required = false) AdminLifecycleOperationRequest.ApplyOperation request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(lifecycleOperationService.applyOperation(
                operationId,
                request == null ? new AdminLifecycleOperationRequest.ApplyOperation() : request,
                adminUserId(servletRequest),
                adminUsername(servletRequest)));
    }

    @Operation(summary = "取消生命週期排程")
    @PostMapping("/operations/{operationId}/cancel")
    public ApiResponse<AdminLifecycleOperationResponse.Summary> cancelOperation(
            @PathVariable Long operationId,
            @RequestBody(required = false) AdminLifecycleOperationRequest.CancelOperation request,
            HttpServletRequest servletRequest) {
        return ApiResponse.success(lifecycleOperationService.cancelOperation(
                operationId,
                request == null ? new AdminLifecycleOperationRequest.CancelOperation() : request,
                adminUserId(servletRequest),
                adminUsername(servletRequest)));
    }

    @Operation(summary = "執行到期生命週期排程")
    @PostMapping("/operations/run-due")
    public ApiResponse<List<AdminLifecycleOperationResponse.Summary>> runDueOperations(HttpServletRequest servletRequest) {
        return ApiResponse.success(lifecycleOperationService.runDueOperations(adminUserId(servletRequest), adminUsername(servletRequest)));
    }

    @Operation(summary = "查詢生命週期操作歷史")
    @GetMapping("/operations")
    public ApiResponse<PageResponse<AdminLifecycleOperationResponse.Summary>> pageOperations(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String operationStatus,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(lifecycleOperationService.pageOperations(pageNum, pageSize, targetType, action, operationStatus, keyword));
    }

    @Operation(summary = "查看生命週期操作詳情")
    @GetMapping("/operations/{operationId}")
    public ApiResponse<AdminLifecycleOperationResponse.Detail> getOperation(@PathVariable Long operationId) {
        return ApiResponse.success(lifecycleOperationService.getOperation(operationId));
    }

    private Long adminUserId(HttpServletRequest request) {
        Object value = request.getAttribute("adminUserId");
        return value instanceof Long userId ? userId : null;
    }

    private String adminUsername(HttpServletRequest request) {
        Object value = request.getAttribute("adminUsername");
        return value == null ? null : String.valueOf(value);
    }
}
