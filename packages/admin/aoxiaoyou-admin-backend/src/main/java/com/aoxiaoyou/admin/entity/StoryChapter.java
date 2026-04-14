package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@TableName("story_chapters")
@Data
@EqualsAndHashCode(callSuper = true)
public class StoryChapter extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("storyline_id")
    private Long storylineId;

    @TableField("chapter_order")
    private Integer chapterOrder;

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

    @TableField("detail_zh")
    private String detailZh;

    @TableField("detail_en")
    private String detailEn;

    @TableField("detail_zht")
    private String detailZht;

    @TableField("detail_pt")
    private String detailPt;

    @TableField("achievement_zh")
    private String achievementZh;

    @TableField("achievement_en")
    private String achievementEn;

    @TableField("achievement_zht")
    private String achievementZht;

    @TableField("achievement_pt")
    private String achievementPt;

    @TableField("collectible_zh")
    private String collectibleZh;

    @TableField("collectible_en")
    private String collectibleEn;

    @TableField("collectible_zht")
    private String collectibleZht;

    @TableField("collectible_pt")
    private String collectiblePt;

    @TableField("location_name_zh")
    private String locationNameZh;

    @TableField("location_name_en")
    private String locationNameEn;

    @TableField("location_name_zht")
    private String locationNameZht;

    @TableField("location_name_pt")
    private String locationNamePt;

    @TableField("media_asset_id")
    private Long mediaAssetId;

    @TableField("unlock_type")
    private String unlockType;

    @TableField("unlock_param_json")
    private String unlockParamJson;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableField("published_at")
    private LocalDateTime publishedAt;
}
