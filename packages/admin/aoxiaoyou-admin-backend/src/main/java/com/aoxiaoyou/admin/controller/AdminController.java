package com.aoxiaoyou.admin.controller;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.dto.request.LoginRequest;
import com.aoxiaoyou.admin.dto.request.RefreshTokenRequest;
import com.aoxiaoyou.admin.dto.response.AdminAuthResponse;
import com.aoxiaoyou.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台认证")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/auth")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "后台管理员登录")
    @PostMapping("/login")
    public ApiResponse<AdminAuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        request.setIp(getClientIp(httpRequest));
        request.setUserAgent(httpRequest.getHeader("User-Agent"));
        return ApiResponse.success(adminService.login(request));
    }

    @Operation(summary = "获取当前登录管理员")
    @GetMapping("/me")
    public ApiResponse<AdminAuthResponse> me(HttpServletRequest request) {
        Long adminUserId = (Long) request.getAttribute("adminUserId");
        return ApiResponse.success(adminService.me(adminUserId));
    }

    @Operation(summary = "刷新后台令牌")
    @PostMapping("/refresh")
    public ApiResponse<AdminAuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(adminService.refresh(request.getRefreshToken()));
    }

    @Operation(summary = "后台登出")
    @PostMapping("/logout")
    public ApiResponse<Boolean> logout() {
        return ApiResponse.success(Boolean.TRUE);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
