package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorylineSessionResponse {
    private Long storylineId;
    private String sessionId;
    private String status;
}
