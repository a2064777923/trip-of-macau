package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("content_lifecycle_operation_impacts")
public class ContentLifecycleOperationImpact extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("operation_id")
    private Long operationId;

    @TableField("impact_type")
    private String impactType;

    private String severity;

    @TableField("source_type")
    private String sourceType;

    @TableField("source_id")
    private Long sourceId;

    @TableField("source_code")
    private String sourceCode;

    @TableField("source_name")
    private String sourceName;

    @TableField("relation_type")
    private String relationType;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("target_code")
    private String targetCode;

    @TableField("target_name")
    private String targetName;

    @TableField("impact_summary")
    private String impactSummary;

    @TableField("metadata_json")
    private String metadataJson;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer deleted;
}
