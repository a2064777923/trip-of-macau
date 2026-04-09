package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("admin_id")
    private Long adminId;

    @TableField("admin_username")
    private String adminUsername;

    private String module;

    private String operation;

    @TableField("request_method")
    private String requestMethod;

    @TableField("request_url")
    private String requestUrl;

    @TableField("request_params")
    private String requestParams;

    @TableField("response_data")
    private String responseData;

    private String ip;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("_openid")
    private String openid;
}

