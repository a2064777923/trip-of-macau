package com.aoxiaoyou.tripofmacau.service;

import com.aoxiaoyou.tripofmacau.dto.request.UserLoginRequest;
import com.aoxiaoyou.tripofmacau.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse login(UserLoginRequest request);

    UserProfileResponse getByOpenId(String openId);

    UserProfileResponse getById(Long userId);
}
