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
@TableName("admin_users")
public class AdminUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("username")
    private String username;

    @TableField("password_hash")
    private String passwordHash;

    @TableField("display_name")
    private String displayName;

    private String email;

    private String phone;

    @TableField("avatar_url")
    private String avatarUrl;

    private String department;

    @TableField("is_superuser")
    private Integer isSuperuser;

    private String status;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    @TableField("last_login_ip")
    private String lastLoginIp;

    @TableField("password_changed_at")
    private LocalDateTime passwordChangedAt;
}
