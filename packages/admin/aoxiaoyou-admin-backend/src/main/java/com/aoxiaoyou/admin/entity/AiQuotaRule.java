package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_quota_rules")
public class AiQuotaRule extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("capability_id")
    private Long capabilityId;

    @TableField("policy_id")
    private Long policyId;

    @TableField("scope_type")
    private String scopeType;

    @TableField("scope_value")
    private String scopeValue;

    @TableField("window_type")
    private String windowType;

    @TableField("window_size")
    private Integer windowSize;

    @TableField("request_limit")
    private Integer requestLimit;

    @TableField("token_limit")
    private Integer tokenLimit;

    @TableField("suspicious_concurrency_threshold")
    private Integer suspiciousConcurrencyThreshold;

    @TableField("action_mode")
    private String actionMode;

    private String status;

    private String notes;
}
