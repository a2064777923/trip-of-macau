package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_progress")
public class UserProgress extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("storyline_id")
    private Long storylineId;

    @TableField("active_storyline_id")
    private Long activeStorylineId;

    @TableField("completed_storyline")
    private Boolean completedStoryline;

    @TableField("completed_chapter_ids_json")
    private String completedChapterIdsJson;

    @TableField("collected_stamp_ids_json")
    private String collectedStampIdsJson;

    @TableField("progress_percent")
    private Integer progressPercent;

    @TableField("last_seen_at")
    private LocalDateTime lastSeenAt;

    @TableField("completed_at")
    private LocalDateTime completedAt;
}
