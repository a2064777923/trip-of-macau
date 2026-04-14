package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserStateResponse {

    private UserProfileResponse profile;
    private UserPreferencesResponse preferences;
    private UserProgressResponse progress;
    private List<UserRewardRedemptionResponse> rewardRedemptions;
}
