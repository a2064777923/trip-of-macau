package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reward_rule_bindings")
public class RewardRuleBinding extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("rule_id")
    private Long ruleId;

    @TableField("owner_domain")
    private String ownerDomain;

    @TableField("owner_id")
    private Long ownerId;

    @TableField("owner_code")
    private String ownerCode;

    @TableField("binding_role")
    private String bindingRole;

    @TableField("sort_order")
    private Integer sortOrder;
}
