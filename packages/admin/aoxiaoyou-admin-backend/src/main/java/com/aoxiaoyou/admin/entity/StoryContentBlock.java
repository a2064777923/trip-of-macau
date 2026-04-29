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
@TableName("story_content_blocks")
public class StoryContentBlock extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("block_type")
    private String blockType;

    @TableField("title_zh")
    private String titleZh;

    @TableField("title_en")
    private String titleEn;

    @TableField("title_zht")
    private String titleZht;

    @TableField("title_pt")
    private String titlePt;

    @TableField("summary_zh")
    private String summaryZh;

    @TableField("summary_en")
    private String summaryEn;

    @TableField("summary_zht")
    private String summaryZht;

    @TableField("summary_pt")
    private String summaryPt;

    @TableField("body_zh")
    private String bodyZh;

    @TableField("body_en")
    private String bodyEn;

    @TableField("body_zht")
    private String bodyZht;

    @TableField("body_pt")
    private String bodyPt;

    @TableField("primary_asset_id")
    private Long primaryAssetId;

    @TableField("style_preset")
    private String stylePreset;

    @TableField("display_mode")
    private String displayMode;

    @TableField("visibility_json")
    private String visibilityJson;

    @TableField("config_json")
    private String configJson;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("published_at")
    private LocalDateTime publishedAt;
}
