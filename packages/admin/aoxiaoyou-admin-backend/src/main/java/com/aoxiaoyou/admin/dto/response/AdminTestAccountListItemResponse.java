package com.aoxiaoyou.admin.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminTestAccountListItemResponse {

    private Long id;
    private Long userId;
    private String openId;
    private String nickname;
    private String avatar;
    private String remark;
    private String testGroup;
    private MockLocation mockLocation;
    private Boolean isMockEnabled;
    private Integer stampCount;
    private Integer level;
    private String levelName;
    private Integer experience;
    private LocalDateTime createTime;
    private LocalDateTime lastOperationTime;

    @Data
    @Builder
    public static class MockLocation {
        private Double latitude;
        private Double longitude;
        private String address;
    }
}
