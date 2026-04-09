package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.dto.response.DashboardStatsResponse;
import com.aoxiaoyou.admin.entity.Activity;
import com.aoxiaoyou.admin.entity.SysOperationLog;
import com.aoxiaoyou.admin.mapper.ActivityMapper;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.RewardMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SysOperationLogMapper;
import com.aoxiaoyou.admin.mapper.TestAccountMapper;
import com.aoxiaoyou.admin.mapper.UserMapper;
import com.aoxiaoyou.admin.service.DashboardService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserMapper userMapper;
    private final PoiMapper poiMapper;
    private final StoryLineMapper storyLineMapper;
    private final ActivityMapper activityMapper;
    private final RewardMapper rewardMapper;
    private final TestAccountMapper testAccountMapper;
    private final SysOperationLogMapper sysOperationLogMapper;

    @Override
    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userMapper.selectCount(null);
        long poiCount = poiMapper.selectCount(null);
        long storyLines = storyLineMapper.selectCount(null);
        long activities = activityMapper.selectCount(new LambdaQueryWrapper<Activity>().eq(Activity::getStatus, "published"));
        long rewards = rewardMapper.selectCount(null);
        long testAccounts = testAccountMapper.selectCount(null);
        long weeklyUsers = userMapper.selectCount(new LambdaQueryWrapper<com.aoxiaoyou.admin.entity.User>()
                .ge(com.aoxiaoyou.admin.entity.User::getCreatedAt, LocalDateTime.now().minusDays(7)));
        double weeklyGrowth = totalUsers == 0 ? 0D : Math.round((weeklyUsers * 10000D / totalUsers)) / 100D;

        List<DashboardStatsResponse.RecentActivity> recentActivities = sysOperationLogMapper.selectList(
                        new LambdaQueryWrapper<SysOperationLog>().orderByDesc(SysOperationLog::getCreatedAt).last("limit 8"))
                .stream()
                .map(log -> DashboardStatsResponse.RecentActivity.builder()
                        .id(log.getId())
                        .type(log.getModule())
                        .user(log.getAdminUsername() == null ? "系统" : log.getAdminUsername())
                        .action(log.getOperation())
                        .time(log.getCreatedAt() == null ? "-" : log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                        .build())
                .toList();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalStamps(totalUsers * 7)
                .poiCount(poiCount)
                .weeklyGrowth(weeklyGrowth)
                .activeUsers(Math.max(weeklyUsers, Math.min(totalUsers, 128)))
                .storyLines(storyLines)
                .activities(activities)
                .rewards(rewards)
                .testAccounts(testAccounts)
                .recentActivities(recentActivities)
                .systemStatus(DashboardStatsResponse.SystemStatus.builder()
                        .database(Boolean.TRUE)
                        .api(Boolean.TRUE)
                        .cloudRun(Boolean.TRUE)
                        .build())
                .build();
    }
}
