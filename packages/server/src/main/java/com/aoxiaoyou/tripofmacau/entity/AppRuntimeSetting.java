package com.aoxiaoyou.tripofmacau.entity;

import com.aoxiaoyou.tripofmacau.common.enums.ContentStatus;
import com.aoxiaoyou.tripofmacau.common.enums.LocaleCode;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_runtime_settings")
public class AppRuntimeSetting extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("setting_group")
    private String settingGroup;

    @TableField("setting_key")
    private String settingKey;

    @TableField("locale_code")
    private String localeCodeValue;

    @TableField("title_zh")
    private String titleZh;

    @TableField("title_en")
    private String titleEn;

    @TableField("title_zht")
    private String titleZht;

    @TableField("title_pt")
    private String titlePt;

    @TableField("value_json")
    private String valueJson;

    @TableField("value_text")
    private String valueText;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("description_en")
    private String descriptionEn;

    @TableField("description_zht")
    private String descriptionZht;

    @TableField("description_pt")
    private String descriptionPt;

    @TableField("asset_id")
    private Long assetId;

    @TableField("status")
    private String statusCode;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("published_at")
    private LocalDateTime publishedAt;

    public LocaleCode getLocaleCode() {
        return localeCodeValue == null || localeCodeValue.isBlank() ? null : LocaleCode.fromCode(localeCodeValue);
    }

    public void setLocaleCode(LocaleCode localeCode) {
        this.localeCodeValue = localeCode == null ? null : localeCode.getCode();
    }

    public ContentStatus getStatus() {
        return statusCode == null || statusCode.isBlank() ? null : ContentStatus.fromCode(statusCode);
    }

    public void setStatus(ContentStatus status) {
        this.statusCode = status == null ? null : status.getCode();
    }
}
