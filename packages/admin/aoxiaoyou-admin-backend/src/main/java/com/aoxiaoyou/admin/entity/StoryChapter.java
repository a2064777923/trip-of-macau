package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("story_chapters")
@Data
@EqualsAndHashCode(callSuper = true)
public class StoryChapter extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("story_line_id")
    private Long storyLineId;

    @TableField("chapter_order")
    private Integer chapterOrder;

    @TableField("title_zh")
    private String titleZh;

    @TableField("title_en")
    private String titleEn;

    @TableField("title_zht")
    private String titleZht;

    @TableField("media_type")
    private String mediaType;

    @TableField("media_url")
    private String mediaUrl;

    @TableField("script_zh")
    private String scriptZh;

    @TableField("script_en")
    private String scriptEn;

    @TableField("script_zht")
    private String scriptZht;

    @TableField("unlock_type")
    private String unlockType;

    @TableField("unlock_param")
    private String unlockParam;

    private Integer duration;

    @TableField("_openid")
    private String openid;
}
