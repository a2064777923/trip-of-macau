package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
@TableName("sys_login_log")
public class SysLoginLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long adminId;
    
    private String username;
    
    private String ip;
    
    private String userAgent;
    
    /**
     * success: 成功
     * failed: 失败
     */
    private String loginStatus;
    
    private String failReason;

    @TableField("_openid")
    private String openid;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}

