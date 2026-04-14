package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_checkins")
public class UserCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("poi_id")
    private Long poiId;

    @TableField("trigger_mode")
    private String triggerMode;

    @TableField("distance_meters")
    private BigDecimal distanceMeters;

    @TableField("gps_accuracy")
    private BigDecimal gpsAccuracy;

    private BigDecimal latitude;

    private BigDecimal longitude;

    @TableField("checked_at")
    private LocalDateTime checkedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
