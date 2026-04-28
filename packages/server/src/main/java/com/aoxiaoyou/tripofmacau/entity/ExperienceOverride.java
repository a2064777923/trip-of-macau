package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("experience_overrides")
public class ExperienceOverride extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("owner_type")
    private String ownerType;
    @TableField("owner_id")
    private Long ownerId;
    @TableField("target_owner_type")
    private String targetOwnerType;
    @TableField("target_owner_id")
    private Long targetOwnerId;
    @TableField("target_step_code")
    private String targetStepCode;
    @TableField("override_mode")
    private String overrideMode;
    @TableField("replacement_step_id")
    private Long replacementStepId;
    @TableField("override_config_json")
    private String overrideConfigJson;
    private String status;
    @TableField("sort_order")
    private Integer sortOrder;
}
