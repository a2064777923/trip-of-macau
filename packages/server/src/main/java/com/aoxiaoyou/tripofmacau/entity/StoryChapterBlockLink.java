package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("story_chapter_block_links")
public class StoryChapterBlockLink extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("chapter_id")
    private Long chapterId;

    @TableField("block_id")
    private Long blockId;

    @TableField("override_title_json")
    private String overrideTitleJson;

    @TableField("override_summary_json")
    private String overrideSummaryJson;

    @TableField("override_body_json")
    private String overrideBodyJson;

    @TableField("display_condition_json")
    private String displayConditionJson;

    @TableField("override_config_json")
    private String overrideConfigJson;

    private String status;

    @TableField("sort_order")
    private Integer sortOrder;
}
