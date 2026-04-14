package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reward_redemptions")
public class RewardRedemption extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("reward_id")
    private Long rewardId;

    @TableField("redemption_status")
    private String redemptionStatus;

    @TableField("stamp_cost_snapshot")
    private Integer stampCostSnapshot;

    @TableField("qr_code")
    private String qrCode;

    @TableField("redeemed_at")
    private LocalDateTime redeemedAt;

    @TableField("expires_at")
    private LocalDateTime expiresAt;
}
