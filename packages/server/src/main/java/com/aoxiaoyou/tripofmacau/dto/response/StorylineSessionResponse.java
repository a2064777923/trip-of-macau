package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StorylineSessionResponse {
    private Long storylineId;
    private String sessionId;
    private Long currentChapterId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime lastEventAt;
    private LocalDateTime exitedAt;
    private Integer eventCount;
    private Boolean exitClearedTemporaryState;
}
