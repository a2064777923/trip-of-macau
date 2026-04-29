package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_progress_operation_audits")
public class UserProgressOperationAudit {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("target_user_id")
    private Long targetUserId;

    @TableField("scope_type")
    private String scopeType;

    @TableField("scope_id")
    private Long scopeId;

    @TableField("storyline_id")
    private Long storylineId;

    @TableField("action_type")
    private String actionType;

    @TableField("preview_token_hash")
    private String previewTokenHash;

    @TableField("preview_summary_json")
    private String previewSummaryJson;

    @TableField("result_summary_json")
    private String resultSummaryJson;

    private String reason;

    @TableField("request_ip")
    private String requestIp;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
