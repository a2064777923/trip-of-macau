package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminRewardPresentationUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminRewardPresentationResponse;
import com.aoxiaoyou.admin.service.AdminRewardDomainService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/reward-presentations")
@RequiredArgsConstructor
public class AdminRewardPresentationController {

    private final AdminRewardDomainService rewardDomainService;

    @GetMapping
    public ApiResponse<PageResponse<AdminRewardPresentationResponse>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String presentationType
    ) {
        return ApiResponse.success(rewardDomainService.pageRewardPresentations(pageNum, pageSize, keyword, status, presentationType));
    }

    @GetMapping("/{presentationId}")
    public ApiResponse<AdminRewardPresentationResponse> get(@PathVariable Long presentationId) {
        return ApiResponse.success(rewardDomainService.getRewardPresentation(presentationId));
    }

    @PostMapping
    public ApiResponse<AdminRewardPresentationResponse> create(@Valid @RequestBody AdminRewardPresentationUpsertRequest request) {
        return ApiResponse.success(rewardDomainService.createRewardPresentation(request));
    }

    @PutMapping("/{presentationId}")
    public ApiResponse<AdminRewardPresentationResponse> update(
            @PathVariable Long presentationId,
            @Valid @RequestBody AdminRewardPresentationUpsertRequest request
    ) {
        return ApiResponse.success(rewardDomainService.updateRewardPresentation(presentationId, request));
    }

    @DeleteMapping("/{presentationId}")
    public ApiResponse<Boolean> delete(@PathVariable Long presentationId) {
        rewardDomainService.deleteRewardPresentation(presentationId);
        return ApiResponse.success(Boolean.TRUE);
    }
}
