package com.aoxiaoyou.tripofmacau.common.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class PublicAuthInterceptor implements HandlerInterceptor {

    private static final Set<String> PUBLIC_LOGIN_PATHS = Set.of(
            "/api/v1/user/login/wechat",
            "/api/v1/user/login/dev-bypass"
    );

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1/user")) {
            return true;
        }
        if (PUBLIC_LOGIN_PATHS.contains(path)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "Unauthorized");
            return false;
        }

        try {
            String token = authHeader.substring(7);
            request.setAttribute(PublicAuthContext.USER_ID_ATTRIBUTE, jwtUtil.getUserId(token));
            request.setAttribute(PublicAuthContext.OPEN_ID_ATTRIBUTE, jwtUtil.getOpenId(token));
            request.setAttribute(PublicAuthContext.NICKNAME_ATTRIBUTE, jwtUtil.getNickname(token));
            return true;
        } catch (Exception ex) {
            writeUnauthorized(response, "Invalid token");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
