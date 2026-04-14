package com.aoxiaoyou.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_progress")
public class TravelerProgress {

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

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
