package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_exploration_events")
public class UserExplorationEvent {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("user_id")
    private Long userId;
    @TableField("element_id")
    private Long elementId;
    @TableField("element_code")
    private String elementCode;
    @TableField("event_type")
    private String eventType;
    @TableField("event_source")
    private String eventSource;
    @TableField("storyline_session_id")
    private String storylineSessionId;
    @TableField("client_event_id")
    private String clientEventId;
    @TableField("event_payload_json")
    private String eventPayloadJson;
    @TableField("occurred_at")
    private LocalDateTime occurredAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
