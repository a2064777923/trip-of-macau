package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class RuntimeGroupResponse {

    private String group;
    private String localeCode;
    private Map<String, Object> settings;
    private List<RuntimeSettingItemResponse> items;
}
