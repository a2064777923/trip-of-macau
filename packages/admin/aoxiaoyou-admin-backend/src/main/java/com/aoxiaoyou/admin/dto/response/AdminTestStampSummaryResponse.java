package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminTestStampSummaryResponse {

    private Long testAccountId;
    private Long userId;
    private Integer stampCount;
    private Integer currentLevel;
    private String levelName;
    private Integer nextLevelTarget;
    private Integer remainingToNextLevel;
    private Integer maxStamps;
}
