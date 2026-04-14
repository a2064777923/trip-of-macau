package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserRewardRedemptionResponse {

    private Long id;
    private Long rewardId;
    private String rewardName;
    private String redemptionStatus;
    private Integer stampCostSnapshot;
    private String qrCode;
    private LocalDateTime redeemedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
