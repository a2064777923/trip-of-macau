package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.CollectibleResponse;
import com.aoxiaoyou.admin.dto.response.BadgeResponse;
import com.aoxiaoyou.admin.service.AdminCollectibleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/collectibles")
@RequiredArgsConstructor
public class AdminCollectibleController {

    private final AdminCollectibleService collectibleService;

    // ========== 收集物 ==========
    @GetMapping("/items")
    public ApiResponse<PageResponse<CollectibleResponse>> pageCollectibles(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String rarity) {
        return ApiResponse.success(collectibleService.pageCollectibles(pageNum, pageSize, keyword, rarity));
    }

    @PostMapping("/items")
    public ApiResponse<CollectibleResponse> createCollectible(@RequestBody Object request) {
        return ApiResponse.success(collectibleService.createCollectible(request));
    }

    // ========== 徽章 ==========
    @GetMapping("/badges")
    public ApiResponse<PageResponse<BadgeResponse>> pageBadges(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.success(collectibleService.pageBadges(pageNum, pageSize));
    }

    @PostMapping("/badges")
    public ApiResponse<BadgeResponse> createBadge(@RequestBody Object request) {
        return ApiResponse.success(collectibleService.createBadge(request));
    }
}
