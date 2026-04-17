package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_policy_provider_bindings")
public class AiPolicyProviderBinding extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("policy_id")
    private Long policyId;

    @TableField("provider_id")
    private Long providerId;

    @TableField("inventory_id")
    private Long inventoryId;

    @TableField("binding_role")
    private String bindingRole;

    @TableField("route_mode")
    private String routeMode;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer enabled;

    @TableField("model_override")
    private String modelOverride;

    @TableField("weight_percent")
    private Integer weightPercent;

    @TableField("timeout_ms_override")
    private Integer timeoutMsOverride;

    @TableField("retry_count_override")
    private Integer retryCountOverride;

    @TableField("parameter_override_json")
    private String parameterOverrideJson;

    private String notes;
}
