package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserProgressResponse {

    private Long activeStoryId;
    private List<Long> collectedStampIds;
    private List<Long> completedStoryIds;
    private List<Long> completedChapterIds;
    private List<String> unlockedCityCodes;
    private List<Long> redeemedRewardIds;
    private List<UserCheckinHistoryItemResponse> checkinHistory;
}
