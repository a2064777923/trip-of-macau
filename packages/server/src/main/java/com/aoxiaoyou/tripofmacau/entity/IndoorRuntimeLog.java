package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("indoor_runtime_logs")
public class IndoorRuntimeLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("floor_id")
    private Long floorId;

    @TableField("node_id")
    private Long nodeId;

    @TableField("behavior_id")
    private Long behaviorId;

    @TableField("trigger_id")
    private String triggerId;

    @TableField("event_type")
    private String eventType;

    @TableField("event_timestamp")
    private LocalDateTime eventTimestamp;

    @TableField("relative_x")
    private BigDecimal relativeX;

    @TableField("relative_y")
    private BigDecimal relativeY;

    @TableField("dwell_ms")
    private Long dwellMs;

    @TableField("user_id")
    private Long userId;

    @TableField("client_session_id")
    private String clientSessionId;

    @TableField("interaction_accepted")
    private Boolean interactionAccepted;

    @TableField("matched_trigger_id")
    private String matchedTriggerId;

    @TableField("requires_auth")
    private Boolean requiresAuth;

    @TableField("blocked_reason")
    private String blockedReason;

    @TableField("effect_categories_json")
    private String effectCategoriesJson;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
