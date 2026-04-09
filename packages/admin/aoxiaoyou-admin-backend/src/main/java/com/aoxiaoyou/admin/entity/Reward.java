package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("rewards")
public class Reward extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("name_zh")
    private String nameZh;

    private String description;

    @TableField("stamps_required")
    private Integer stampsRequired;

    @TableField("total_quantity")
    private Integer totalQuantity;

    @TableField("redeemed_count")
    private Integer redeemedCount;

    @TableField("start_time")
    private java.time.LocalDateTime startTime;

    @TableField("end_time")
    private java.time.LocalDateTime endTime;

    private String status;
}
