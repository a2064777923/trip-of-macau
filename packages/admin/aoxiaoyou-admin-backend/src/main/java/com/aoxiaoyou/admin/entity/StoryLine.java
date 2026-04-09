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

    @TableField("banner_url")
    private String bannerUrl;

    private String category;

    private String difficulty;

    @TableField("estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    private String tags;

    @TableField("total_chapters")
    private Integer totalChapters;

    private String status;

    @TableField("publish_at")
    private LocalDateTime publishAt;

    @TableField("start_at")
    private LocalDateTime startAt;

    @TableField("end_at")
    private LocalDateTime endAt;

    @TableField("participation_count")
    private Integer participationCount;

    @TableField("completion_count")
    private Integer completionCount;

    @TableField("average_completion_time")
    private Integer averageCompletionTime;
}

