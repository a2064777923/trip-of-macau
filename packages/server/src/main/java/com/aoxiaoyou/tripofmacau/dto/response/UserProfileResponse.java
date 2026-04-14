package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {

    private Long id;
    private String openId;
    private String nickname;
    private String avatarUrl;
    private Integer level;
    private String title;
    private Integer totalStamps;
    private Integer currentExp;
    private Integer nextLevelExp;
    private Long currentCityId;
    private String currentCityCode;
    private String currentLocaleCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
