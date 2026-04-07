package com.aoxiaoyou.tripofmacau.controller;

import com.aoxiaoyou.tripofmacau.common.api.ApiResponse;
import com.aoxiaoyou.tripofmacau.dto.request.UserLoginRequest;
import com.aoxiaoyou.tripofmacau.dto.response.UserProfileResponse;
import com.aoxiaoyou.tripofmacau.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "用户")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "按 openId 登录或首次注册")
    @PostMapping("/login")
    public ApiResponse<UserProfileResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @Operation(summary = "按 openId 获取用户")
    @GetMapping("/by-open-id")
    public ApiResponse<UserProfileResponse> getByOpenId(
            @Parameter(description = "微信 openId") @RequestParam String openId) {
        return ApiResponse.success(userService.getByOpenId(openId));
    }

    @Operation(summary = "按用户 ID 获取详情")
    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getById(@PathVariable Long userId) {
        return ApiResponse.success(userService.getById(userId));
    }
}
