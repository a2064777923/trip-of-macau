package com.aoxiaoyou.admin.common.config;

import com.aoxiaoyou.admin.common.api.ApiResponse;
import com.aoxiaoyou.admin.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/v1/auth/login") || path.startsWith("/api/admin/v1/auth/refresh") || path.startsWith("/api/public/") || path.startsWith("/api/v1/health") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs")) {
            return true;
        }

        if (!path.startsWith("/api/admin/v1/")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或登录已过期");
            return false;
        }

        try {
            String token = authHeader.substring(7);
            Long userId = jwtUtil.getUserId(token);
            request.setAttribute("adminUserId", userId);
            request.setAttribute("adminUsername", jwtUtil.getUsername(token));
            request.setAttribute("adminRoles", jwtUtil.getRoles(token));
            return true;
        } catch (Exception ex) {
            writeUnauthorized(response, "登录凭证无效");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
