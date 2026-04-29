package com.aoxiaoyou.tripofmacau.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_storyline_sessions")
public class UserStorylineSession {

    @TableId(value = "session_id", type = IdType.INPUT)
    private String sessionId;

    @TableField("user_id")
    private Long userId;

    @TableField("storyline_id")
    private Long storylineId;

    @TableField("current_chapter_id")
    private Long currentChapterId;

    @TableField("status")
    private String status;

    @TableField("started_at")
    private LocalDateTime startedAt;

    @TableField("last_event_at")
    private LocalDateTime lastEventAt;

    @TableField("exited_at")
    private LocalDateTime exitedAt;

    @TableField("event_count")
    private Integer eventCount;

    @TableField("temporary_step_state_json")
    private String temporaryStepStateJson;

    @TableField("exit_cleared_temporary_state")
    private Boolean exitClearedTemporaryState;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
