package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminUserProgressRecomputeConfirmRequest;
import com.aoxiaoyou.admin.dto.request.AdminUserProgressRecomputePreviewRequest;
import com.aoxiaoyou.admin.dto.request.AdminUserProgressRepairRequest;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressAuditEntryResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressOperationPreviewResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressOperationResultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;

@Tag(name = "Traveler Progress Operations")
@RestController
@RequestMapping("/api/admin/v1/users")
public class AdminTravelerProgressOpsController {

    @Operation(summary = "Preview traveler progress recompute")
    @PostMapping("/{userId}/progress-ops/recompute-preview")
    public ApiResponse<AdminUserProgressOperationPreviewResponse> previewRecompute(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserProgressRecomputePreviewRequest request) {
        return ApiResponse.success(AdminUserProgressOperationPreviewResponse.builder()
                .userId(userId)
                .scopeType(request.getScopeType())
                .scopeId(request.getScopeId())
                .storylineId(request.getStorylineId())
                .from(request.getFrom())
                .to(request.getTo())
                .actionType("RECOMPUTE")
                .confirmationText("RECOMPUTE")
                .build());
    }

    @Operation(summary = "Confirm traveler progress recompute")
    @PostMapping("/{userId}/progress-ops/recompute-confirm")
    public ApiResponse<AdminUserProgressOperationResultResponse> confirmRecompute(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserProgressRecomputeConfirmRequest request) {
        return ApiResponse.success(AdminUserProgressOperationResultResponse.builder()
                .userId(userId)
                .scopeType(request.getScopeType())
                .scopeId(request.getScopeId())
                .storylineId(request.getStorylineId())
                .from(request.getFrom())
                .to(request.getTo())
                .actionType("RECOMPUTE")
                .confirmationText(request.getConfirmationText())
                .previewHash(request.getPreviewHash())
                .confirmationToken(request.getConfirmationToken())
                .status("pending")
                .build());
    }

    @Operation(summary = "Preview traveler progress repair")
    @PostMapping("/{userId}/progress-ops/repair-preview")
    public ApiResponse<AdminUserProgressOperationPreviewResponse> previewRepair(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserProgressRepairRequest request) {
        return ApiResponse.success(AdminUserProgressOperationPreviewResponse.builder()
                .userId(userId)
                .scopeType(request.getScopeType())
                .scopeId(request.getScopeId())
                .storylineId(request.getStorylineId())
                .from(request.getFrom())
                .to(request.getTo())
                .actionType(request.getActionType())
                .confirmationText("REPAIR")
                .build());
    }

    @Operation(summary = "Apply traveler progress repair")
    @PostMapping("/{userId}/progress-ops/repair-apply")
    public ApiResponse<AdminUserProgressOperationResultResponse> applyRepair(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserProgressRepairRequest request) {
        return ApiResponse.success(AdminUserProgressOperationResultResponse.builder()
                .userId(userId)
                .scopeType(request.getScopeType())
                .scopeId(request.getScopeId())
                .storylineId(request.getStorylineId())
                .from(request.getFrom())
                .to(request.getTo())
                .actionType(request.getActionType())
                .confirmationText(request.getConfirmationText())
                .previewHash(request.getPreviewHash())
                .confirmationToken(request.getConfirmationToken())
                .status("pending")
                .build());
    }

    @Operation(summary = "List traveler progress operation audits")
    @GetMapping("/{userId}/progress-ops/audits")
    public ApiResponse<PageResponse<AdminUserProgressAuditEntryResponse>> listAudits(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) java.util.List<String> actionTypes,
            @RequestParam(required = false) String scopeType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ApiResponse.success(PageResponse.<AdminUserProgressAuditEntryResponse>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(0)
                .totalPages(0)
                .list(Collections.emptyList())
                .build());
    }
}
