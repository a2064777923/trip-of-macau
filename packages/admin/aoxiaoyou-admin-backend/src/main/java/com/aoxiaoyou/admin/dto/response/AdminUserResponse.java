package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponse {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String department;
    private Integer isSuperuser;
    private String status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
}
