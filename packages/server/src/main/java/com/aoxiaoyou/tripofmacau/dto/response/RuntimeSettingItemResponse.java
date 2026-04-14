package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RuntimeSettingItemResponse {

    private Long id;
    private String settingKey;
    private String title;
    private String description;
    private Object value;
    private String assetUrl;
    private Integer sortOrder;
}
