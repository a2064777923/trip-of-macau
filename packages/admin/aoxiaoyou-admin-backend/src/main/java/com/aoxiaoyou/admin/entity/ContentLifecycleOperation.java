package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_lifecycle_operations")
public class ContentLifecycleOperation extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("operation_code")
    private String operationCode;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("target_code")
    private String targetCode;

    @TableField("target_name")
    private String targetName;

    private String action;

    @TableField("from_status")
    private String fromStatus;

    @TableField("to_status")
    private String toStatus;

    @TableField("operation_status")
    private String operationStatus;

    @TableField("scheduled_at")
    private LocalDateTime scheduledAt;

    @TableField("applied_at")
    private LocalDateTime appliedAt;

    @TableField("cancelled_at")
    private LocalDateTime cancelledAt;

    @TableField("failed_at")
    private LocalDateTime failedAt;

    @TableField("requested_by")
    private Long requestedBy;

    @TableField("requested_by_name")
    private String requestedByName;

    private String reason;

    @TableField("preview_hash")
    private String previewHash;

    @TableField("preview_json")
    private String previewJson;

    @TableField("request_json")
    private String requestJson;

    @TableField("result_json")
    private String resultJson;

    @TableField("error_message")
    private String errorMessage;

    private Integer deleted;
}
