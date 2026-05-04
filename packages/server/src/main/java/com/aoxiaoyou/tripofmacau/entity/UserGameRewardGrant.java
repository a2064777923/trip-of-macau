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
@TableName("user_game_reward_grants")
public class UserGameRewardGrant extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("game_reward_id")
    private Long gameRewardId;

    @TableField("rule_id")
    private Long ruleId;

    @TableField("source_event_id")
    private Long sourceEventId;

    @TableField("source_session_id")
    private Long sourceSessionId;

    @TableField("grant_status")
    private String grantStatus;

    @TableField("grant_reason")
    private String grantReason;

    @TableField("granted_by")
    private Long grantedBy;

    @TableField("granted_at")
    private LocalDateTime grantedAt;

    @TableField("idempotency_key")
    private String idempotencyKey;
}
