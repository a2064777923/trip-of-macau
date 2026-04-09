package com.aoxiaoyou.admin.dto.request;

import lombok.Data;

@Data
public class TestAccountMockLocationRequest {

    private Boolean enabled;

    private Double latitude;

    private Double longitude;

    private Long poiId;

    private String address;

    private String reason;
}
