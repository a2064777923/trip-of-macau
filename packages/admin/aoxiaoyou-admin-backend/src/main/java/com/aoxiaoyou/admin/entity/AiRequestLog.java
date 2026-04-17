package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_request_logs")
public class AiRequestLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("provider_id")
    private Long providerId;

    @TableField("inventory_id")
    private Long inventoryId;

    @TableField("policy_id")
    private Long policyId;

    @TableField("capability_code")
    private String capabilityCode;

    @TableField("inventory_code")
    private String inventoryCode;

    @TableField("user_openid")
    private String userOpenid;

    @TableField("admin_owner_id")
    private Long adminOwnerId;

    @TableField("admin_owner_name")
    private String adminOwnerName;

    @TableField("request_type")
    private String requestType;

    @TableField("input_data_hash")
    private String inputDataHash;

    @TableField("output_summary")
    private String outputSummary;

    @TableField("latency_ms")
    private Integer latencyMs;

    @TableField("tokens_used")
    private Integer tokensUsed;

    @TableField("cost_usd")
    private BigDecimal costUsd;

    private Integer success;

    @TableField("fallback_triggered")
    private Integer fallbackTriggered;

    @TableField("blocked_reason")
    private String blockedReason;

    @TableField("trace_id")
    private String traceId;

    @TableField("error_message")
    private String errorMessage;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
