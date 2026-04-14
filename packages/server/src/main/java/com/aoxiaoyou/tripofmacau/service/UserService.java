package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.dto.request.UserCheckinRequest;
import com.aoxiaoyou.tripofmacau.dto.request.UserCurrentCityUpdateRequest;
import com.aoxiaoyou.tripofmacau.dto.request.UserDevBypassLoginRequest;
import com.aoxiaoyou.tripofmacau.dto.request.UserPreferencesUpdateRequest;
import com.aoxiaoyou.tripofmacau.dto.request.UserWechatLoginRequest;
import com.aoxiaoyou.tripofmacau.dto.response.UserCheckinResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserPreferencesResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserProfileResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserProgressResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserRewardRedeemResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserRewardRedemptionResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserSessionResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserStampProgressResponse;
import com.aoxiaoyou.tripofmacau.dto.response.UserStateResponse;

import java.util.List;

public interface UserService {

    UserSessionResponse loginWithWechat(UserWechatLoginRequest request);

    UserSessionResponse loginWithDevBypass(UserDevBypassLoginRequest request);

    UserStateResponse getState(Long userId, String localeHint);

    UserProfileResponse getProfile(Long userId, String localeHint);

    UserProfileResponse updateCurrentCity(Long userId, UserCurrentCityUpdateRequest request, String localeHint);

    UserProgressResponse getProgress(Long userId, String localeHint);

    UserPreferencesResponse getPreferences(Long userId);

    UserPreferencesResponse updatePreferences(Long userId, UserPreferencesUpdateRequest request);

    List<UserStampProgressResponse> getStampProgress(Long userId);

    List<UserRewardRedemptionResponse> getRewardRedemptions(Long userId, String localeHint);

    UserCheckinResponse checkin(Long userId, UserCheckinRequest request, String localeHint);

    UserRewardRedeemResponse redeemReward(Long userId, Long rewardId, String localeHint);
}
