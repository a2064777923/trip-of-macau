package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("permissions")
public class Permission extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("perm_code")
    private String permCode;

    @TableField("perm_name")
    private String permName;

    @TableField("module")
    private String module;

    @TableField("perm_type")
    private String permType;

    @TableField("parent_id")
    private Long parentId;

    private String path;

    private String method;

    private String description;

    @TableField("sort_order")
    private Integer sortOrder;

    private String status;
}
