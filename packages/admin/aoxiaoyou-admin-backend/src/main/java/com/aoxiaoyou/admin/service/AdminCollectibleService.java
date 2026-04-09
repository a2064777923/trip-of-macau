package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.response.CollectibleResponse;
import com.aoxiaoyou.admin.dto.response.BadgeResponse;

public interface AdminCollectibleService {
    PageResponse<CollectibleResponse> pageCollectibles(long pageNum, long pageSize, String keyword, String rarity);
    CollectibleResponse createCollectible(Object request);
    PageResponse<BadgeResponse> pageBadges(long pageNum, long pageSize);
    BadgeResponse createBadge(Object request);
}
