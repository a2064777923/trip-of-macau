package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserRewardRedeemResponse {

    private Long rewardId;
    private String rewardName;
    private String redemptionStatus;
    private String qrCode;
    private LocalDateTime expiresAt;
    private UserStateResponse state;
}
