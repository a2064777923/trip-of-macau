package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminGameRewardUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminGameRewardResponse;
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
@RequestMapping("/api/admin/v1/game-rewards")
@RequiredArgsConstructor
public class AdminGameRewardController {

    private final AdminRewardDomainService rewardDomainService;

    @GetMapping
    public ApiResponse<PageResponse<AdminGameRewardResponse>> page(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String rewardType,
            @RequestParam(required = false) Boolean honorsOnly
    ) {
        return ApiResponse.success(rewardDomainService.pageGameRewards(pageNum, pageSize, keyword, status, rewardType, honorsOnly));
    }

    @GetMapping("/{rewardId}")
    public ApiResponse<AdminGameRewardResponse> get(@PathVariable Long rewardId) {
        return ApiResponse.success(rewardDomainService.getGameReward(rewardId));
    }

    @PostMapping
    public ApiResponse<AdminGameRewardResponse> create(@Valid @RequestBody AdminGameRewardUpsertRequest request) {
        return ApiResponse.success(rewardDomainService.createGameReward(request));
    }

    @PutMapping("/{rewardId}")
    public ApiResponse<AdminGameRewardResponse> update(
            @PathVariable Long rewardId,
            @Valid @RequestBody AdminGameRewardUpsertRequest request
    ) {
        return ApiResponse.success(rewardDomainService.updateGameReward(rewardId, request));
    }

    @DeleteMapping("/{rewardId}")
    public ApiResponse<Boolean> delete(@PathVariable Long rewardId) {
        rewardDomainService.deleteGameReward(rewardId);
        return ApiResponse.success(Boolean.TRUE);
    }
}
