package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminTestFlagRequest;
import com.aoxiaoyou.admin.dto.response.AdminUserDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserListItemResponse;
import com.aoxiaoyou.admin.entity.SysOperationLog;
import com.aoxiaoyou.admin.entity.TestAccount;
import com.aoxiaoyou.admin.entity.TriggerLog;
import com.aoxiaoyou.admin.entity.User;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SysOperationLogMapper;
import com.aoxiaoyou.admin.mapper.TestAccountMapper;
import com.aoxiaoyou.admin.mapper.TriggerLogMapper;
import com.aoxiaoyou.admin.mapper.UserMapper;
import com.aoxiaoyou.admin.service.AdminUserService;
import com.aoxiaoyou.admin.util.LevelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserMapper userMapper;
    private final TestAccountMapper testAccountMapper;
    private final TriggerLogMapper triggerLogMapper;
    private final StoryLineMapper storyLineMapper;
    private final SysOperationLogMapper sysOperationLogMapper;

    @Override
    public PageResponse<AdminUserListItemResponse> pageUsers(long pageNum, long pageSize, String keyword, Boolean isTestAccount) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .and(StringUtils.hasText(keyword), q -> q.like(User::getNickname, keyword).or().like(User::getOpenId, keyword))
                .orderByDesc(User::getCreatedAt);

        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AdminUserListItemResponse> list = page.getRecords().stream()
                .filter(user -> {
                    boolean test = isTestUser(user.getId());
                    return isTestAccount == null || test == isTestAccount;
                })
                .map(this::toListItem)
                .toList();

        return PageResponse.<AdminUserListItemResponse>builder()
                .pageNum(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .totalPages(page.getPages())
                .list(list)
                .build();
    }

    @Override
    public AdminUserDetailResponse getUserDetail(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new com.aoxiaoyou.admin.common.exception.BusinessException(4040, "用户不存在");
        }

        long totalCheckIns = triggerLogMapper.selectCount(new LambdaQueryWrapper<TriggerLog>().eq(TriggerLog::getUserId, userId));
        AdminUserListItemResponse basicInfo = toListItem(user);
        AdminUserDetailResponse.Progress progress = AdminUserDetailResponse.Progress.builder()
                .level(user.getLevel())
                .currentExp(user.getTotalStamps())
                .nextLevelExp(LevelUtil.nextLevelExp(user.getLevel()))
                .totalStamps(user.getTotalStamps())
                .totalBadges(0)
                .unlockedStorylines(storyLineMapper.selectCount(null).intValue())
                .completedStorylines(0)
                .build();

        List<AdminUserDetailResponse.RecentCheckIn> recentCheckIns = triggerLogMapper.selectList(
                        new LambdaQueryWrapper<TriggerLog>()
                                .eq(TriggerLog::getUserId, userId)
                                .orderByDesc(TriggerLog::getCreatedAt)
                                .last("limit 10"))
                .stream()
                .map(log -> AdminUserDetailResponse.RecentCheckIn.builder()
                        .checkInId(log.getId())
                        .poiName("POI#" + log.getPoiId())
                        .checkInType(log.getTriggerType())
                        .rewardGranted(Boolean.TRUE)
                        .createdAt(log.getCreatedAt())
                        .build())
                .toList();

        return AdminUserDetailResponse.builder()
                .basicInfo(basicInfo)
                .progress(progress)
                .activeStorylines(Collections.emptyList())
                .recentCheckIns(recentCheckIns)
                .build();
    }

    @Override
    public AdminUserListItemResponse updateTestFlag(Long userId, AdminTestFlagRequest request, Long operatorId, String operatorName, String ip) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new com.aoxiaoyou.admin.common.exception.BusinessException(4040, "用户不存在");
        }

        TestAccount existing = testAccountMapper.selectOne(new LambdaQueryWrapper<TestAccount>().eq(TestAccount::getUserId, userId).last("limit 1"));
        boolean target = Boolean.TRUE.equals(request.getIsTestAccount());
        if (target && existing == null) {
            TestAccount account = new TestAccount();
            account.setOpenid("");
            account.setUserId(userId);
            account.setTestGroup("default");
            account.setNotes(request.getReason());
            account.setMockEnabled(false);
            testAccountMapper.insert(account);
        }
        if (!target && existing != null) {
            testAccountMapper.deleteById(existing.getId());
        }

        SysOperationLog log = new SysOperationLog();
        log.setOpenid("");
        log.setAdminId(operatorId);
        log.setAdminUsername(operatorName);
        log.setModule("USER");
        log.setOperation(target ? "MARK_TEST_ACCOUNT" : "UNMARK_TEST_ACCOUNT");
        log.setRequestMethod("POST");
        log.setRequestUrl("/api/admin/v1/users/" + userId + "/test-flag");
        log.setRequestParams(request.getReason());
        log.setIp(ip);
        sysOperationLogMapper.insert(log);

        return toListItem(userMapper.selectById(userId));
    }

    private AdminUserListItemResponse toListItem(User user) {
        boolean isTestAccount = isTestUser(user.getId());
        return AdminUserListItemResponse.builder()
                .userId(user.getId())
                .openId(user.getOpenId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .isTestAccount(isTestAccount)
                .accountStatus("active")
                .level(user.getLevel())
                .totalStamps(user.getTotalStamps())
                .currentStorylineId(null)
                .currentStorylineName(null)
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getUpdatedAt())
                .build();
    }

    private boolean isTestUser(Long userId) {
        return testAccountMapper.selectCount(new LambdaQueryWrapper<TestAccount>().eq(TestAccount::getUserId, userId)) > 0;
    }
}
