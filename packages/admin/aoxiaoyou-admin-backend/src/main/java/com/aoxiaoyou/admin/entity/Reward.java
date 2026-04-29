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
@TableName("rewards")
public class Reward extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_en")
    private String nameEn;

    @TableField("name_zht")
    private String nameZht;

    @TableField("name_pt")
    private String namePt;

    @TableField("subtitle_zh")
    private String subtitleZh;

    @TableField("subtitle_en")
    private String subtitleEn;

    @TableField("subtitle_zht")
    private String subtitleZht;

    @TableField("subtitle_pt")
    private String subtitlePt;

    @TableField("description_zh")
    private String descriptionZh;

    @TableField("description_en")
    private String descriptionEn;

    @TableField("description_zht")
    private String descriptionZht;

    @TableField("description_pt")
    private String descriptionPt;

    @TableField("highlight_zh")
    private String highlightZh;

    @TableField("highlight_en")
    private String highlightEn;

    @TableField("highlight_zht")
    private String highlightZht;

    @TableField("highlight_pt")
    private String highlightPt;

    @TableField("stamp_cost")
    private Integer stampCost;

    @TableField("inventory_total")
    private Integer inventoryTotal;

    @TableField("inventory_redeemed")
    private Integer inventoryRedeemed;

    @TableField("cover_asset_id")
    private Long coverAssetId;

    @TableField("popup_preset_code")
    private String popupPresetCode;

    @TableField("popup_config_json")
    private String popupConfigJson;

    @TableField("display_preset_code")
    private String displayPresetCode;

    @TableField("display_config_json")
    private String displayConfigJson;

    @TableField("trigger_preset_code")
    private String triggerPresetCode;

    @TableField("trigger_config_json")
    private String triggerConfigJson;

    @TableField("example_content_zh")
    private String exampleContentZh;

    @TableField("example_content_en")
    private String exampleContentEn;

    @TableField("example_content_zht")
    private String exampleContentZht;

    @TableField("example_content_pt")
    private String exampleContentPt;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("publish_start_at")
    private LocalDateTime publishStartAt;

    @TableField("publish_end_at")
    private LocalDateTime publishEndAt;
}
