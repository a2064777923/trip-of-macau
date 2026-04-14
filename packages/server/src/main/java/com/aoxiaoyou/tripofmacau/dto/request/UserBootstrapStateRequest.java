package com.aoxiaoyou.tripofmacau.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Optional bootstrap snapshot from the existing mini-program local state")
public class UserBootstrapStateRequest {

    private Integer level;

    private String title;

    private Integer totalStamps;

    private Integer currentExp;

    private Integer nextLevelExp;

    private String currentCityCode;

    private Long activeStoryId;

    private List<Long> collectedStampIds;

    private List<Long> completedStoryIds;

    private List<Long> completedChapterIds;

    private List<Long> redeemedRewardIds;

    private List<UserBootstrapCheckinRequest> checkinHistory;

    private UserPreferencesUpdateRequest preferences;
}
