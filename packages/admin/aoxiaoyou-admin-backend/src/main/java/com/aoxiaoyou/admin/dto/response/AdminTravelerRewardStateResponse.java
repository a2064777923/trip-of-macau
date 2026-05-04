package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminTravelerRewardStateResponse {
    private Long userId;
    private List<BackpackItem> backpackItems;
    private List<GameRewardItem> gameRewards;
    private List<TitleItem> titles;
    private List<RedeemableRewardItem> redeemableRewards;
    private Summary summary;

    @Data
    @Builder
    public static class BackpackItem {
        private String sourceType;
        private Long sourceId;
        private String code;
        private String name;
        private String description;
        private Long assetId;
        private Integer quantity;
        private String rarity;
        private String status;
        private Long sourceEventId;
        private Long sourceRuleId;
        private LocalDateTime earnedAt;
    }

    @Data
    @Builder
    public static class GameRewardItem {
        private Long rewardId;
        private String code;
        private String rewardType;
        private String name;
        private String rarity;
        private String status;
        private Long sourceEventId;
        private Long sourceRuleId;
        private LocalDateTime earnedAt;
    }

    @Data
    @Builder
    public static class TitleItem {
        private Long rewardId;
        private String code;
        private String name;
        private String rarity;
        private Boolean equipped;
        private String status;
        private Long sourceEventId;
        private Long sourceRuleId;
        private LocalDateTime earnedAt;
    }

    @Data
    @Builder
    public static class RedeemableRewardItem {
        private Long redemptionId;
        private Long rewardId;
        private String rewardName;
        private String redemptionStatus;
        private Integer stampCostSnapshot;
        private LocalDateTime redeemedAt;
        private LocalDateTime expiresAt;
    }

    @Data
    @Builder
    public static class Summary {
        private Integer backpackCount;
        private Integer gameRewardCount;
        private Integer titleCount;
        private Integer redeemableRewardCount;
        private LocalDateTime lastEarnedAt;
    }
}
