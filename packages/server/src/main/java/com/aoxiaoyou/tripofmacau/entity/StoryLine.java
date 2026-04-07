package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("story_lines")
public class StoryLine extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    @TableField("name_zh")
    private String nameZh;

    @TableField("name_en")
    private String nameEn;

    private String description;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("total_chapters")
    private Integer totalChapters;

    private String status;
}
