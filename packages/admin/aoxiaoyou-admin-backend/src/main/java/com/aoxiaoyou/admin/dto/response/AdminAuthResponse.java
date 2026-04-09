package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AdminAuthResponse {

    private String token;

    private String refreshToken;

    private Long expiresIn;

    private AdminCurrentUser user;

    @Data
    @Builder
    public static class AdminCurrentUser {
        private Long userId;
        private String username;
        private String realName;
        private String email;
        private List<String> roles;
        private List<String> permissions;
        private LocalDateTime lastLoginAt;
    }
}
