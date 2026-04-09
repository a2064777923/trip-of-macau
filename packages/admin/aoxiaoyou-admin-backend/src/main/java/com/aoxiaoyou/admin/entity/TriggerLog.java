package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("trigger_logs")
public class TriggerLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("poi_id")
    private Long poiId;

    @TableField("trigger_type")
    private String triggerType;

    private BigDecimal distance;

    @TableField("gps_accuracy")
    private BigDecimal gpsAccuracy;

    @TableField("wifi_used")
    private Boolean wifiUsed;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
