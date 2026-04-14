package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_checkins")
public class TravelerCheckin {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("poi_id")
    private Long poiId;

    @TableField("trigger_mode")
    private String triggerMode;

    @TableField("checked_at")
    private LocalDateTime checkedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;
}
