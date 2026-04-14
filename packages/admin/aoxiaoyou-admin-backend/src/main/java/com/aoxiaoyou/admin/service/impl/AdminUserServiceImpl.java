package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.dto.request.AdminTestFlagRequest;
import com.aoxiaoyou.admin.dto.response.AdminUserDetailResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserListItemResponse;
import com.aoxiaoyou.admin.entity.Poi;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.entity.SysOperationLog;
import com.aoxiaoyou.admin.entity.TestAccount;
import com.aoxiaoyou.admin.entity.TravelerCheckin;
import com.aoxiaoyou.admin.entity.TravelerProfile;
import com.aoxiaoyou.admin.entity.TravelerProgress;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SysOperationLogMapper;
import com.aoxiaoyou.admin.mapper.TestAccountMapper;
import com.aoxiaoyou.admin.mapper.TravelerCheckinMapper;
import com.aoxiaoyou.admin.mapper.TravelerProfileMapper;
import com.aoxiaoyou.admin.mapper.TravelerProgressMapper;
import com.aoxiaoyou.admin.service.AdminUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final TravelerProfileMapper travelerProfileMapper;
    private final TravelerProgressMapper travelerProgressMapper;
    private final TravelerCheckinMapper travelerCheckinMapper;
    private final TestAccountMapper testAccountMapper;
    private final PoiMapper poiMapper;
    private final StoryLineMapper storyLineMapper;
    private final SysOperationLogMapper sysOperationLogMapper;

    @Override
    public PageResponse<AdminUserListItemResponse> pageUsers(long pageNum, long pageSize, String keyword, Boolean isTestAccount) {
        Set<Long> testUserIds = loadTestUserIds();
        LambdaQueryWrapper<TravelerProfile> wrapper = new LambdaQueryWrapper<TravelerProfile>()
                .and(StringUtils.hasText(keyword), q -> q.like(TravelerProfile::getNickname, keyword).or().like(TravelerProfile::getOpenId, keyword))
                .orderByDesc(TravelerProfile::getCreatedAt);

        if (Boolean.TRUE.equals(isTestAccount)) {
            if (testUserIds.isEmpty()) {
                return PageResponse.<AdminUserListItemResponse>builder()
                        .pageNum(pageNum)
                        .pageSize(pageSize)
                        .total(0)
                        .totalPages(0)
                        .list(Collections.emptyList())
                        .build();
            }
            wrapper.in(TravelerProfile::getId, testUserIds);
        } else if (Boolean.FALSE.equals(isTestAccount) && !testUserIds.isEmpty()) {
            wrapper.notIn(TravelerProfile::getId, testUserIds);
        }

        Page<TravelerProfile> page = travelerProfileMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AdminUserListItemResponse> list = page.getRecords().stream()
                .map(user -> toListItem(user, testUserIds.contains(user.getId())))
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
        TravelerProfile user = travelerProfileMapper.selectById(userId);
        if (user == null) {
            throw new com.aoxiaoyou.admin.common.exception.BusinessException(4040, "User not found");
        }

        List<TravelerProgress> progressRows = travelerProgressMapper.selectList(new LambdaQueryWrapper<TravelerProgress>()
                .eq(TravelerProgress::getUserId, userId)
                .orderByDesc(TravelerProgress::getUpdatedAt)
                .orderByDesc(TravelerProgress::getId));
        TravelerProgress aggregate = progressRows.stream()
                .filter(row -> row.getStorylineId() == null)
                .findFirst()
                .orElse(null);

        Set<Long> unlockedStorylineIds = new LinkedHashSet<>();
        if (aggregate != null && aggregate.getActiveStorylineId() != null) {
            unlockedStorylineIds.add(aggregate.getActiveStorylineId());
        }
        progressRows.stream()
                .map(TravelerProgress::getStorylineId)
                .filter(id -> id != null)
                .forEach(unlockedStorylineIds::add);

        long completedStorylines = progressRows.stream()
                .filter(row -> row.getStorylineId() != null && Boolean.TRUE.equals(row.getCompletedStoryline()))
                .count();

        AdminUserListItemResponse basicInfo = toListItem(user, isTestUser(user.getId()));
        AdminUserDetailResponse.Progress progress = AdminUserDetailResponse.Progress.builder()
                .level(user.getLevel())
                .currentExp(user.getCurrentExp())
                .nextLevelExp(user.getNextLevelExp())
                .totalStamps(user.getTotalStamps())
                .totalBadges(0)
                .unlockedStorylines(unlockedStorylineIds.size())
                .completedStorylines((int) completedStorylines)
                .build();

        List<TravelerCheckin> recentCheckinRows = travelerCheckinMapper.selectList(new LambdaQueryWrapper<TravelerCheckin>()
                .eq(TravelerCheckin::getUserId, userId)
                .orderByDesc(TravelerCheckin::getCheckedAt)
                .orderByDesc(TravelerCheckin::getId)
                .last("limit 10"));
        Map<Long, Poi> poisById = recentCheckinRows.isEmpty()
                ? Collections.emptyMap()
                : poiMapper.selectBatchIds(recentCheckinRows.stream().map(TravelerCheckin::getPoiId).distinct().toList()).stream()
                .map(Poi.class::cast)
                .collect(Collectors.toMap(Poi::getId, poi -> poi, (left, right) -> left, LinkedHashMap::new));

        List<AdminUserDetailResponse.RecentCheckIn> recentCheckIns = recentCheckinRows.stream()
                .map(log -> AdminUserDetailResponse.RecentCheckIn.builder()
                        .checkInId(log.getId())
                        .poiName(resolvePoiName(poisById.get(log.getPoiId()), log.getPoiId()))
                        .checkInType(log.getTriggerMode())
                        .rewardGranted(Boolean.TRUE)
                        .createdAt(log.getCheckedAt())
                        .build())
                .toList();

        List<AdminUserDetailResponse.StorylineProgress> activeStorylines = aggregate == null || aggregate.getActiveStorylineId() == null
                ? Collections.emptyList()
                : Collections.singletonList(buildActiveStoryline(aggregate));

        return AdminUserDetailResponse.builder()
                .basicInfo(basicInfo)
                .progress(progress)
                .activeStorylines(activeStorylines)
                .recentCheckIns(recentCheckIns)
                .build();
    }

    @Override
    public AdminUserListItemResponse updateTestFlag(Long userId, AdminTestFlagRequest request, Long operatorId, String operatorName, String ip) {
        TravelerProfile user = travelerProfileMapper.selectById(userId);
        if (user == null) {
            throw new com.aoxiaoyou.admin.common.exception.BusinessException(4040, "User not found");
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

        return toListItem(travelerProfileMapper.selectById(userId), target);
    }

    private AdminUserListItemResponse toListItem(TravelerProfile user, boolean isTestAccount) {
        TravelerProgress aggregate = travelerProgressMapper.selectOne(new LambdaQueryWrapper<TravelerProgress>()
                .eq(TravelerProgress::getUserId, user.getId())
                .isNull(TravelerProgress::getStorylineId)
                .orderByDesc(TravelerProgress::getUpdatedAt)
                .orderByDesc(TravelerProgress::getId)
                .last("limit 1"));
        Long currentStorylineId = aggregate == null ? null : aggregate.getActiveStorylineId();
        StoryLine currentStoryline = currentStorylineId == null ? null : storyLineMapper.selectById(currentStorylineId);
        return AdminUserListItemResponse.builder()
                .userId(user.getId())
                .openId(user.getOpenId())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .isTestAccount(isTestAccount)
                .accountStatus("active")
                .level(user.getLevel())
                .totalStamps(user.getTotalStamps())
                .currentStorylineId(currentStorylineId)
                .currentStorylineName(currentStoryline == null ? null : currentStoryline.getNameZh())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getUpdatedAt())
                .build();
    }

    private AdminUserDetailResponse.StorylineProgress buildActiveStoryline(TravelerProgress aggregate) {
        StoryLine storyline = storyLineMapper.selectById(aggregate.getActiveStorylineId());
        return AdminUserDetailResponse.StorylineProgress.builder()
                .storylineId(aggregate.getActiveStorylineId())
                .name(storyline == null ? null : storyline.getNameZh())
                .currentPoiId(null)
                .currentPoiName(null)
                .completedPoiCount(0)
                .totalPoiCount(0)
                .progressPercent(aggregate.getProgressPercent())
                .startedAt(aggregate.getCreatedAt())
                .build();
    }

    private String resolvePoiName(Poi poi, Long poiId) {
        if (poi == null) {
            return poiId == null ? "Unknown POI" : "POI#" + poiId;
        }
        return StringUtils.hasText(poi.getNameZh()) ? poi.getNameZh() : poi.getNameEn();
    }

    private Set<Long> loadTestUserIds() {
        return testAccountMapper.selectList(new LambdaQueryWrapper<TestAccount>().select(TestAccount::getUserId))
                .stream()
                .map(TestAccount::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
    }

    private boolean isTestUser(Long userId) {
        return testAccountMapper.selectCount(new LambdaQueryWrapper<TestAccount>().eq(TestAccount::getUserId, userId)) > 0;
    }
}
