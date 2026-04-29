package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminRedeemablePrizeUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminRedeemablePrizeResponse;
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
@RequestMapping("/api/admin/v1/redeemable-prizes")
@RequiredArgsConstructor
public class AdminRedeemablePrizeController {

    private final AdminRewardDomainService rewardDomainService;

    @GetMapping
    public ApiResponse<PageResponse<AdminRedeemablePrizeResponse>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String prizeType,
            @RequestParam(required = false) String fulfillmentMode
    ) {
        return ApiResponse.success(rewardDomainService.pageRedeemablePrizes(pageNum, pageSize, keyword, status, prizeType, fulfillmentMode));
    }

    @GetMapping("/{prizeId}")
    public ApiResponse<AdminRedeemablePrizeResponse> get(@PathVariable Long prizeId) {
        return ApiResponse.success(rewardDomainService.getRedeemablePrize(prizeId));
    }

    @PostMapping
    public ApiResponse<AdminRedeemablePrizeResponse> create(@Valid @RequestBody AdminRedeemablePrizeUpsertRequest request) {
        return ApiResponse.success(rewardDomainService.createRedeemablePrize(request));
    }

    @PutMapping("/{prizeId}")
    public ApiResponse<AdminRedeemablePrizeResponse> update(
            @PathVariable Long prizeId,
            @Valid @RequestBody AdminRedeemablePrizeUpsertRequest request
    ) {
        return ApiResponse.success(rewardDomainService.updateRedeemablePrize(prizeId, request));
    }

    @DeleteMapping("/{prizeId}")
    public ApiResponse<Boolean> delete(@PathVariable Long prizeId) {
        rewardDomainService.deleteRedeemablePrize(prizeId);
        return ApiResponse.success(Boolean.TRUE);
    }
}
