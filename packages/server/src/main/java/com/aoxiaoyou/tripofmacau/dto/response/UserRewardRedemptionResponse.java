package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserRewardRedemptionResponse {

    private Long id;
    private String entryKind;
    private Long rewardId;
    private Long gameRewardId;
    private String gameRewardCode;
    private String rewardType;
    private String rarity;
    private String rewardName;
    private String redemptionStatus;
    private Integer stampCostSnapshot;
    private String qrCode;
    private Long sourceEventId;
    private Long sourceRuleId;
    private LocalDateTime earnedAt;
    private LocalDateTime redeemedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
