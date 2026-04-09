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
@TableName("badges")
public class Badge extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("badge_code")
    private String badgeCode;

    @TableField("name_zh")
    private String nameZh;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("icon_url")
    private String iconUrl;

    @TableField("badge_type")
    private String badgeType;

    private String rarity;

    @TableField("is_hidden")
    private Integer isHidden;

    @TableField("is_limited_time")
    private Integer isLimitedTime;

    @TableField("limited_start")
    private LocalDateTime limitedStart;

    @TableField("limited_end")
    private LocalDateTime limitedEnd;

    @TableField("image_url")
    private String imageUrl;

    @TableField("animation_unlock")
    private String animationUnlock;

    private String status;
}
