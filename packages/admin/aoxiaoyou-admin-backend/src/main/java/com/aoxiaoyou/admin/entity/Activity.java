package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("activities")
public class Activity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String title;

    private String description;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("start_time")
    private java.time.LocalDateTime startTime;

    @TableField("end_time")
    private java.time.LocalDateTime endTime;

    private String status;

    @TableField("participation_count")
    private Integer participationCount;
}
