package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserListItemResponse {

    private Long userId;
    private String openId;
    private String nickname;
    private String avatarUrl;
    private Boolean isTestAccount;
    private String accountStatus;
    private Integer level;
    private Integer totalStamps;
    private Long currentStorylineId;
    private String currentStorylineName;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
