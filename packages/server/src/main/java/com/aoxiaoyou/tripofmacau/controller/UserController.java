package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.common.auth.PublicAuthContext;
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
import com.aoxiaoyou.tripofmacau.dto.response.TestModeResponse;
import com.aoxiaoyou.tripofmacau.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@Tag(name = "Public User")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Login a public mini-program user with WeChat")
    @PostMapping("/login/wechat")
    public ApiResponse<UserSessionResponse> loginWithWechat(@Valid @RequestBody UserWechatLoginRequest request) {
        return ApiResponse.success(userService.loginWithWechat(request));
    }

    @Operation(summary = "Login a public mini-program user through explicit local/dev bypass")
    @PostMapping("/login/dev-bypass")
    public ApiResponse<UserSessionResponse> loginWithDevBypass(@Valid @RequestBody UserDevBypassLoginRequest request) {
        return ApiResponse.success(userService.loginWithDevBypass(request));
    }

    @Operation(summary = "Get the authenticated user state snapshot")
    @GetMapping("/state")
    public ApiResponse<UserStateResponse> state(HttpServletRequest request, @RequestParam(required = false) String locale) {
        return ApiResponse.success(userService.getState(PublicAuthContext.requireUserId(request), locale));
    }

    @Operation(summary = "Get the authenticated user profile")
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> profile(HttpServletRequest request, @RequestParam(required = false) String locale) {
        return ApiResponse.success(userService.getProfile(PublicAuthContext.requireUserId(request), locale));
    }

    @Operation(summary = "Update the authenticated user's current city")
    @PutMapping("/profile/current-city")
    public ApiResponse<UserProfileResponse> updateCurrentCity(
            HttpServletRequest request,
            @Valid @RequestBody UserCurrentCityUpdateRequest body,
            @RequestParam(required = false) String locale
    ) {
        return ApiResponse.success(userService.updateCurrentCity(PublicAuthContext.requireUserId(request), body, locale));
    }

    @Operation(summary = "Get the authenticated user's progress")
    @GetMapping("/progress")
    public ApiResponse<UserProgressResponse> progress(HttpServletRequest request, @RequestParam(required = false) String locale) {
        return ApiResponse.success(userService.getProgress(PublicAuthContext.requireUserId(request), locale));
    }

    @Operation(summary = "Get the authenticated user's collected stamps")
    @GetMapping("/progress/stamps")
    public ApiResponse<List<UserStampProgressResponse>> stampProgress(HttpServletRequest request) {
        return ApiResponse.success(userService.getStampProgress(PublicAuthContext.requireUserId(request)));
    }

    @Operation(summary = "Get the authenticated user's reward redemptions")
    @GetMapping("/progress/rewards")
    public ApiResponse<List<UserRewardRedemptionResponse>> rewardRedemptions(
            HttpServletRequest request,
            @RequestParam(required = false) String locale
    ) {
        return ApiResponse.success(userService.getRewardRedemptions(PublicAuthContext.requireUserId(request), locale));
    }

    @Operation(summary = "Get the authenticated user's preferences")
    @GetMapping("/preferences")
    public ApiResponse<UserPreferencesResponse> preferences(HttpServletRequest request) {
        return ApiResponse.success(userService.getPreferences(PublicAuthContext.requireUserId(request)));
    }

    @Operation(summary = "Update the authenticated user's preferences")
    @PutMapping("/preferences")
    public ApiResponse<UserPreferencesResponse> updatePreferences(
            HttpServletRequest request,
            @Valid @RequestBody UserPreferencesUpdateRequest body
    ) {
        return ApiResponse.success(userService.updatePreferences(PublicAuthContext.requireUserId(request), body));
    }

    @Operation(summary = "Create a user check-in")
    @PostMapping("/checkins")
    public ApiResponse<UserCheckinResponse> checkin(
            HttpServletRequest request,
            @Valid @RequestBody UserCheckinRequest body,
            @RequestParam(required = false) String locale
    ) {
        return ApiResponse.success(userService.checkin(PublicAuthContext.requireUserId(request), body, locale));
    }

    @Operation(summary = "Redeem a reward")
    @PostMapping("/rewards/{rewardId}/redeem")
    public ApiResponse<UserRewardRedeemResponse> redeemReward(
            HttpServletRequest request,
            @PathVariable Long rewardId,
            @RequestParam(required = false) String locale
    ) {
        return ApiResponse.success(userService.redeemReward(PublicAuthContext.requireUserId(request), rewardId, locale));
    }

    @Operation(summary = "Get test mode status for the authenticated user")
    @GetMapping("/test-mode")
    public ApiResponse<TestModeResponse> testMode(HttpServletRequest request) {
        return ApiResponse.success(userService.getTestMode(PublicAuthContext.requireUserId(request)));
    }
}
