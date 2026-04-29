package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminBadgeUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminCollectibleUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRewardUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminRewardResponse;
import com.aoxiaoyou.admin.dto.response.BadgeResponse;
import com.aoxiaoyou.admin.dto.response.CollectibleResponse;
import com.aoxiaoyou.admin.service.AdminCollectibleService;
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
@RequestMapping("/api/admin/v1/collectibles")
@RequiredArgsConstructor
public class AdminCollectibleController {

    private final AdminCollectibleService collectibleService;

    @GetMapping("/items")
    public ApiResponse<PageResponse<CollectibleResponse>> pageCollectibles(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String rarity) {
        return ApiResponse.success(collectibleService.pageCollectibles(pageNum, pageSize, keyword, rarity));
    }

    @GetMapping("/items/{collectibleId}")
    public ApiResponse<CollectibleResponse> getCollectible(@PathVariable Long collectibleId) {
        return ApiResponse.success(collectibleService.getCollectible(collectibleId));
    }

    @PostMapping("/items")
    public ApiResponse<CollectibleResponse> createCollectible(@Valid @RequestBody AdminCollectibleUpsertRequest request) {
        return ApiResponse.success(collectibleService.createCollectible(request));
    }

    @PutMapping("/items/{collectibleId}")
    public ApiResponse<CollectibleResponse> updateCollectible(
            @PathVariable Long collectibleId,
            @Valid @RequestBody AdminCollectibleUpsertRequest request) {
        return ApiResponse.success(collectibleService.updateCollectible(collectibleId, request));
    }

    @DeleteMapping("/items/{collectibleId}")
    public ApiResponse<Boolean> deleteCollectible(@PathVariable Long collectibleId) {
        collectibleService.deleteCollectible(collectibleId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/badges")
    public ApiResponse<PageResponse<BadgeResponse>> pageBadges(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.success(collectibleService.pageBadges(pageNum, pageSize));
    }

    @GetMapping("/badges/{badgeId}")
    public ApiResponse<BadgeResponse> getBadge(@PathVariable Long badgeId) {
        return ApiResponse.success(collectibleService.getBadge(badgeId));
    }

    @PostMapping("/badges")
    public ApiResponse<BadgeResponse> createBadge(@Valid @RequestBody AdminBadgeUpsertRequest request) {
        return ApiResponse.success(collectibleService.createBadge(request));
    }

    @PutMapping("/badges/{badgeId}")
    public ApiResponse<BadgeResponse> updateBadge(
            @PathVariable Long badgeId,
            @Valid @RequestBody AdminBadgeUpsertRequest request) {
        return ApiResponse.success(collectibleService.updateBadge(badgeId, request));
    }

    @DeleteMapping("/badges/{badgeId}")
    public ApiResponse<Boolean> deleteBadge(@PathVariable Long badgeId) {
        collectibleService.deleteBadge(badgeId);
        return ApiResponse.success(Boolean.TRUE);
    }

    @GetMapping("/rewards")
    public ApiResponse<PageResponse<AdminRewardResponse>> pageRewards(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(collectibleService.pageRewards(pageNum, pageSize, status));
    }

    @PostMapping("/rewards")
    public ApiResponse<AdminRewardResponse> createReward(@Valid @RequestBody AdminRewardUpsertRequest.Upsert request) {
        return ApiResponse.success(collectibleService.createReward(request));
    }

    @PutMapping("/rewards/{rewardId}")
    public ApiResponse<AdminRewardResponse> updateReward(
            @PathVariable Long rewardId,
            @Valid @RequestBody AdminRewardUpsertRequest.Upsert request) {
        return ApiResponse.success(collectibleService.updateReward(rewardId, request));
    }

    @DeleteMapping("/rewards/{rewardId}")
    public ApiResponse<Boolean> deleteReward(@PathVariable Long rewardId) {
        collectibleService.deleteReward(rewardId);
        return ApiResponse.success(Boolean.TRUE);
    }
}
