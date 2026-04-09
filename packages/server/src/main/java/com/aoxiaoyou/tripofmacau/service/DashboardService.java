package com.aoxiaoyou.tripofmacau.service;

import java.util.Map;

public interface DashboardService {
    
    /**
     * 获取仪表盘统计数据
     */
    Map<String, Object> getStats();
}
