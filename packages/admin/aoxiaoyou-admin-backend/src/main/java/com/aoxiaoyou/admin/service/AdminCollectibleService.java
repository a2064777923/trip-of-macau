package com.aoxiaoyou.admin.service;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminBadgeUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminCollectibleUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRewardUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminRewardResponse;
import com.aoxiaoyou.admin.dto.response.CollectibleResponse;
import com.aoxiaoyou.admin.dto.response.BadgeResponse;

public interface AdminCollectibleService {
    PageResponse<CollectibleResponse> pageCollectibles(long pageNum, long pageSize, String keyword, String rarity);
    CollectibleResponse getCollectible(Long collectibleId);
    CollectibleResponse createCollectible(AdminCollectibleUpsertRequest request);
    CollectibleResponse updateCollectible(Long collectibleId, AdminCollectibleUpsertRequest request);
    void deleteCollectible(Long collectibleId);
    PageResponse<BadgeResponse> pageBadges(long pageNum, long pageSize);
    BadgeResponse getBadge(Long badgeId);
    BadgeResponse createBadge(AdminBadgeUpsertRequest request);
    BadgeResponse updateBadge(Long badgeId, AdminBadgeUpsertRequest request);
    void deleteBadge(Long badgeId);
    PageResponse<AdminRewardResponse> pageRewards(long pageNum, long pageSize, String status);
    AdminRewardResponse createReward(AdminRewardUpsertRequest.Upsert request);
    AdminRewardResponse updateReward(Long rewardId, AdminRewardUpsertRequest.Upsert request);
    void deleteReward(Long rewardId);
}
