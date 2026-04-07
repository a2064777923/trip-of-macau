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
@TableName("users")
public class User extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("open_id")
    private String openId;

    private String nickname;

    @TableField("avatar_url")
    private String avatarUrl;

    @TableField("language_preference")
    private String languagePreference;

    private Integer level;

    private String title;

    @TableField("total_stamps")
    private Integer totalStamps;

    @TableField("interface_mode")
    private String interfaceMode;

    @TableField("font_scale")
    private BigDecimal fontScale;

    @TableField("high_contrast")
    private Boolean highContrast;

    @TableField("voice_guide_enabled")
    private Boolean voiceGuideEnabled;

    @TableField("simplified_mode")
    private Boolean simplifiedMode;
}
