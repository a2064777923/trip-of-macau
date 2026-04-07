package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pois")
public class Poi extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_en")
    private String nameEn;

    @TableField("name_zht")
    private String nameZht;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String address;

    @TableField("category_id")
    private Long categoryId;

    @TableField("trigger_radius")
    private Integer triggerRadius;

    private String importance;

    @TableField("story_line_id")
    private Long storyLineId;

    @TableField("stamp_type")
    private String stampType;

    private String description;
}
