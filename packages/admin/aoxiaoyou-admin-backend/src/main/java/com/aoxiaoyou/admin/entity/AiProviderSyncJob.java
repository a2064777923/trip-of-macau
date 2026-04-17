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
@TableName("ai_provider_sync_jobs")
public class AiProviderSyncJob extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("provider_id")
    private Long providerId;

    @TableField("platform_code")
    private String platformCode;

    @TableField("sync_strategy")
    private String syncStrategy;

    @TableField("job_status")
    private String jobStatus;

    private String message;

    @TableField("error_detail")
    private String errorDetail;

    @TableField("discovered_count")
    private Integer discoveredCount;

    @TableField("created_count")
    private Integer createdCount;

    @TableField("updated_count")
    private Integer updatedCount;

    @TableField("stale_count")
    private Integer staleCount;

    @TableField("raw_payload_json")
    private String rawPayloadJson;

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("finished_at")
    private LocalDateTime finishedAt;
}
