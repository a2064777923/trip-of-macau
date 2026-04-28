package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("experience_bindings")
public class ExperienceBinding extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("owner_type")
    private String ownerType;
    @TableField("owner_id")
    private Long ownerId;
    @TableField("owner_code")
    private String ownerCode;
    @TableField("binding_role")
    private String bindingRole;
    @TableField("flow_id")
    private Long flowId;
    private Integer priority;
    @TableField("inherit_policy")
    private String inheritPolicy;
    private String status;
    @TableField("sort_order")
    private Integer sortOrder;
}
