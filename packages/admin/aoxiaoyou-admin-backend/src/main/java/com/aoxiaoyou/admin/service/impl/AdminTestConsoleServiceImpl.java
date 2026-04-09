package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.TestAccountBatchStampGrantRequest;
import com.aoxiaoyou.admin.dto.request.TestAccountLevelAdjustRequest;
import com.aoxiaoyou.admin.dto.request.TestAccountMockLocationRequest;
import com.aoxiaoyou.admin.dto.request.TestAccountProgressResetRequest;
import com.aoxiaoyou.admin.dto.request.TestAccountStampGrantRequest;
import com.aoxiaoyou.admin.dto.response.AdminOperationLogResponse;
import com.aoxiaoyou.admin.dto.response.AdminTestAccountListItemResponse;
import com.aoxiaoyou.admin.dto.response.AdminTestStampSummaryResponse;
import com.aoxiaoyou.admin.entity.SysOperationLog;
import com.aoxiaoyou.admin.entity.TestAccount;
import com.aoxiaoyou.admin.entity.User;
import com.aoxiaoyou.admin.mapper.SysOperationLogMapper;
import com.aoxiaoyou.admin.mapper.TestAccountMapper;
import com.aoxiaoyou.admin.mapper.UserMapper;
import com.aoxiaoyou.admin.service.AdminTestConsoleService;
import com.aoxiaoyou.admin.util.LevelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminTestConsoleServiceImpl implements AdminTestConsoleService {

    private static final int MAX_STAMPS = 12;

    private final TestAccountMapper testAccountMapper;
    private final UserMapper userMapper;
    private final SysOperationLogMapper sysOperationLogMapper;

    @Override
    public PageResponse<AdminTestAccountListItemResponse> page(long pageNum, long pageSize, String testGroup) {
        Page<TestAccount> page = testAccountMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TestAccount>()
                        .eq(testGroup != null && !testGroup.isEmpty(), TestAccount::getTestGroup, testGroup)
                        .orderByDesc(TestAccount::getUpdatedAt));

        Page<AdminTestAccountListItemResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(this::toListItem).toList());
        return PageResponse.of(responsePage);
    }

    @Override
    public AdminTestAccountListItemResponse toggleMockLocation(Long testAccountId, TestAccountMockLocationRequest request, Long operatorId, String operatorName, String ip) {
        TestAccount account = requireAccount(testAccountId);
        account.setMockEnabled(Boolean.TRUE.equals(request.getEnabled()));
        if (request.getLatitude() != null) account.setMockLatitude(request.getLatitude());
        if (request.getLongitude() != null) account.setMockLongitude(request.getLongitude());
        if (request.getPoiId() != null) account.setMockPoiId(request.getPoiId());
        if (StringUtils.hasText(request.getReason())) account.setNotes(request.getReason());
        testAccountMapper.updateById(account);
        writeLog(operatorId, operatorName, "TEST_CONSOLE", account.getMockEnabled() ? "SET_MOCK_LOCATION" : "DISABLE_MOCK_LOCATION", ip, request.getReason(), "/api/admin/v1/test-console/accounts/" + testAccountId + "/mock");
        return toListItem(testAccountMapper.selectById(testAccountId));
    }

    @Override
    public AdminTestAccountListItemResponse adjustLevel(Long testAccountId, TestAccountLevelAdjustRequest request, Long operatorId, String operatorName, String ip) {
        TestAccount account = requireAccount(testAccountId);
        User user = requireUser(account.getUserId());
        int targetExp = Math.max(request.getTargetExp(), 0);
        int targetLevel = Math.max(request.getTargetLevel(), 1);
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, user.getId())
                .set(User::getLevel, targetLevel)
                .set(User::getTotalStamps, targetExp)
                .set(User::getTitle, LevelUtil.levelName(targetLevel)));
        writeLog(operatorId, operatorName, "TEST_CONSOLE", "ADJUST_LEVEL", ip, request.getReason(), "/api/admin/v1/test-console/accounts/" + testAccountId + "/level");
        return toListItem(account);
    }

    @Override
    public AdminTestAccountListItemResponse grantStamp(Long testAccountId, TestAccountStampGrantRequest request, Long operatorId, String operatorName, String ip) {
        TestAccount account = requireAccount(testAccountId);
        User user = requireUser(account.getUserId());
        updateUserStampState(user, (user.getTotalStamps() == null ? 0 : user.getTotalStamps()) + 1);
        writeLog(operatorId, operatorName, "TEST_CONSOLE", "GRANT_STAMP", ip, request.getReason(), "/api/admin/v1/test-console/accounts/" + testAccountId + "/stamps/grant");
        return toListItem(account);
    }

    @Override
    public AdminTestAccountListItemResponse batchGrantStamp(Long testAccountId, TestAccountBatchStampGrantRequest request, Long operatorId, String operatorName, String ip) {
        TestAccount account = requireAccount(testAccountId);
        User user = requireUser(account.getUserId());
        int current = user.getTotalStamps() == null ? 0 : user.getTotalStamps();
        int target = Math.min(MAX_STAMPS, current + request.getCount());
        updateUserStampState(user, target);
        String reason = StringUtils.hasText(request.getReason()) ? request.getReason() : "批量发放印章 " + request.getCount() + " 个";
        writeLog(operatorId, operatorName, "TEST_CONSOLE", "BATCH_GRANT_STAMP", ip, reason, "/api/admin/v1/test-console/accounts/" + testAccountId + "/stamps/batch-grant");
        return toListItem(account);
    }

    @Override
    public AdminTestAccountListItemResponse clearStamps(Long testAccountId, Long operatorId, String operatorName, String ip, String reason) {
        TestAccount account = requireAccount(testAccountId);
        User user = requireUser(account.getUserId());
        updateUserStampState(user, 0);
        String finalReason = StringUtils.hasText(reason) ? reason : "清空测试账号印章";
        writeLog(operatorId, operatorName, "TEST_CONSOLE", "CLEAR_STAMPS", ip, finalReason, "/api/admin/v1/test-console/accounts/" + testAccountId + "/stamps/clear");
        return toListItem(account);
    }

    @Override
    public AdminTestStampSummaryResponse stampSummary(Long testAccountId) {
        TestAccount account = requireAccount(testAccountId);
        User user = requireUser(account.getUserId());
        int stampCount = user.getTotalStamps() == null ? 0 : user.getTotalStamps();
        int level = user.getLevel() == null ? LevelUtil.normalizeLevel(stampCount) : user.getLevel();
        int nextLevelTarget = LevelUtil.nextLevelExp(level);
        return AdminTestStampSummaryResponse.builder()
                .testAccountId(account.getId())
                .userId(user.getId())
                .stampCount(stampCount)
                .currentLevel(level)
                .levelName(LevelUtil.levelName(level))
                .nextLevelTarget(nextLevelTarget)
                .remainingToNextLevel(Math.max(0, nextLevelTarget - stampCount))
                .maxStamps(MAX_STAMPS)
                .build();
    }

    @Override
    public AdminTestAccountListItemResponse resetProgress(Long testAccountId, TestAccountProgressResetRequest request, Long operatorId, String operatorName, String ip) {
        TestAccount account = requireAccount(testAccountId);
        User user = requireUser(account.getUserId());
        String resetType = request == null ? null : request.getResetType();
        if ("level".equalsIgnoreCase(resetType)) {
            int totalStamps = user.getTotalStamps() == null ? 0 : user.getTotalStamps();
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .eq(User::getId, user.getId())
                    .set(User::getLevel, 1)
                    .set(User::getTitle, LevelUtil.levelName(1))
                    .set(User::getTotalStamps, totalStamps));
        } else if ("stamps".equalsIgnoreCase(resetType) || "all".equalsIgnoreCase(resetType) || !StringUtils.hasText(resetType)) {
            updateUserStampState(user, 0);
        } else {
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .eq(User::getId, user.getId())
                    .set(User::getLevel, 1)
                    .set(User::getTotalStamps, 0)
                    .set(User::getTitle, LevelUtil.levelName(1)));
        }
        writeLog(operatorId, operatorName, "TEST_CONSOLE", "RESET_PROGRESS", ip, request == null ? null : request.getReason(), "/api/admin/v1/test-console/accounts/" + testAccountId + "/progress/reset");
        return toListItem(account);
    }

    @Override
    public PageResponse<AdminOperationLogResponse> operationLogs(Long testAccountId, long pageNum, long pageSize) {
        requireAccount(testAccountId);
        Page<SysOperationLog> page = sysOperationLogMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysOperationLog>()
                        .eq(SysOperationLog::getModule, "TEST_CONSOLE")
                        .orderByDesc(SysOperationLog::getCreatedAt));
        Page<AdminOperationLogResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        responsePage.setRecords(page.getRecords().stream().map(log -> AdminOperationLogResponse.builder()
                .id(log.getId())
                .operationType(log.getOperation())
                .operationTypeName(log.getOperation())
                .operationDesc(log.getRequestParams())
                .adminName(log.getAdminUsername())
                .ipAddress(log.getIp())
                .createTime(log.getCreatedAt())
                .build()).toList());
        return PageResponse.of(responsePage);
    }

    private void updateUserStampState(User user, int stampCount) {
        int normalizedCount = Math.max(stampCount, 0);
        int normalizedLevel = LevelUtil.normalizeLevel(normalizedCount);
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, user.getId())
                .set(User::getTotalStamps, normalizedCount)
                .set(User::getLevel, normalizedLevel)
                .set(User::getTitle, LevelUtil.levelName(normalizedLevel)));
    }

    private TestAccount requireAccount(Long id) {
        TestAccount account = testAccountMapper.selectById(id);
        if (account == null) {
            throw new BusinessException(4043, "测试账号不存在");
        }
        return account;
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(4040, "用户不存在");
        }
        return user;
    }

    private void writeLog(Long operatorId, String operatorName, String module, String operation, String ip, String params, String url) {
        SysOperationLog log = new SysOperationLog();
        log.setOpenid("");
        log.setAdminId(operatorId);
        log.setAdminUsername(operatorName);
        log.setModule(module);
        log.setOperation(operation);
        log.setRequestMethod("POST");
        log.setRequestUrl(url);
        log.setRequestParams(params);
        log.setIp(ip);
        sysOperationLogMapper.insert(log);
    }

    private AdminTestAccountListItemResponse toListItem(TestAccount account) {
        User user = userMapper.selectById(account.getUserId());
        int totalStamps = user == null || user.getTotalStamps() == null ? 0 : user.getTotalStamps();
        int level = user == null || user.getLevel() == null ? LevelUtil.normalizeLevel(totalStamps) : user.getLevel();
        return AdminTestAccountListItemResponse.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .openId(user == null ? null : user.getOpenId())
                .nickname(user == null ? "未知用户" : user.getNickname())
                .avatar(user == null ? null : user.getAvatarUrl())
                .remark(account.getNotes())
                .testGroup(account.getTestGroup())
                .mockLocation(AdminTestAccountListItemResponse.MockLocation.builder()
                        .latitude(account.getMockLatitude())
                        .longitude(account.getMockLongitude())
                        .address(account.getMockPoiId() == null ? account.getNotes() : "POI#" + account.getMockPoiId())
                        .build())
                .isMockEnabled(Boolean.TRUE.equals(account.getMockEnabled()))
                .stampCount(totalStamps)
                .level(level)
                .levelName(LevelUtil.levelName(level))
                .experience(totalStamps)
                .createTime(account.getCreatedAt())
                .lastOperationTime(account.getUpdatedAt())
                .build();
    }
}
