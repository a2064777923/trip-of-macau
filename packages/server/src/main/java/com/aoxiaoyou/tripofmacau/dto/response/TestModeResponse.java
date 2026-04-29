package com.aoxiaoyou.tripofmacau.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestModeResponse {
    
    /**
     * 是否为测试账号
     */
    private Boolean isTestAccount;
    
    /**
     * 测试分组
     */
    private String testGroup;
    
    /**
     * 是否启用模拟定位
     */
    private Boolean mockEnabled;
    
    /**
     * 模拟纬度
     */
    private Double mockLatitude;
    
    /**
     * 模拟经度
     */
    private Double mockLongitude;
    
    /**
     * 模拟 POI ID
     */
    private Long mockPoiId;
}
