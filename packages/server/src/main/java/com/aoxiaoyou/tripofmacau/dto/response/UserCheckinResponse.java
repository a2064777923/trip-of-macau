package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserCheckinResponse {

    private Boolean success;
    private Long poiId;
    private String poiName;
    private Long stampId;
    private String stampName;
    private Integer experienceGained;
    private String triggerMode;
    private Long unlockedStorylineId;
    private LocalDateTime checkedAt;
    private UserStateResponse state;
}
