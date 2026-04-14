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
@TableName("user_preferences")
public class UserPreference extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("interface_mode")
    private String interfaceMode;

    @TableField("font_scale")
    private BigDecimal fontScale;

    @TableField("high_contrast")
    private Boolean highContrast;

    @TableField("voice_guide_enabled")
    private Boolean voiceGuideEnabled;

    @TableField("senior_mode")
    private Boolean seniorMode;

    @TableField("locale_code")
    private String localeCode;

    @TableField("emergency_contact_name")
    private String emergencyContactName;

    @TableField("emergency_contact_phone")
    private String emergencyContactPhone;

    @TableField("runtime_overrides_json")
    private String runtimeOverridesJson;
}
