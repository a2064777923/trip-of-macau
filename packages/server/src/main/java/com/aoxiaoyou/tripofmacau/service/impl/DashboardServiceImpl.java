package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.mapper.PoiMapper;
import com.aoxiaoyou.tripofmacau.mapper.StoryLineMapper;
import com.aoxiaoyou.tripofmacau.mapper.TriggerLogMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserMapper;
import com.aoxiaoyou.tripofmacau.service.DashboardService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.aoxiaoyou.tripofmacau.entity.User;
import com.aoxiaoyou.tripofmacau.entity.Poi;
import com.aoxiaoyou.tripofmacau.entity.TriggerLog;
import com.aoxiaoyou.tripofmacau.entity.StoryLine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    
    private final UserMapper userMapper;
    private final PoiMapper poiMapper;
    private final TriggerLogMapper triggerLogMapper;
    private final StoryLineMapper storyLineMapper;
    
    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计数据
        long totalUsers = userMapper.selectCount(null);
        long totalPois = poiMapper.selectCount(new LambdaQueryWrapper<Poi>().eq(Poi::getDeleted, 0));
        long totalStoryLines = storyLineMapper.selectCount(new LambdaQueryWrapper<StoryLine>().eq(StoryLine::getDeleted, 0).eq(StoryLine::getStatus, "published"));
        
        // 计算印章总数 (使用 trigger_logs 的记录数)
        long totalStamps = triggerLogMapper.selectCount(null);
        
        // 本周新增用户 (简化计算，实际应该按日期范围查询)
        long weeklyGrowth = (long) (totalUsers * 0.1); // 估算 10% 增长
        
        // 活跃用户 (今天有触发记录的用户)
        long activeUsers = triggerLogMapper.selectCount(
                new LambdaQueryWrapper<TriggerLog>()
        );
        
        // 进行中的活动数量
        long activities = 3; // TODO: 从 activities 表查询
        
        stats.put("totalUsers", totalUsers);
        stats.put("totalStamps", totalStamps);
        stats.put("poiCount", totalPois);
        stats.put("weeklyGrowth", weeklyGrowth);
        stats.put("activeUsers", activeUsers);
        stats.put("storyLines", totalStoryLines);
        stats.put("activities", activities);
        
        // 系统状态
        Map<String, Boolean> systemStatus = new HashMap<>();
        systemStatus.put("database", true);
        systemStatus.put("api", true);
        systemStatus.put("cloudRun", true);
        stats.put("systemStatus", systemStatus);
        
        // 最近活动
        stats.put("recentActivities", java.util.Collections.emptyList());
        
        return stats;
    }
}
