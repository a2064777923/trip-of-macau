package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.dto.response.AdminUserDetailResponse;
import com.aoxiaoyou.admin.entity.TravelerProfile;
import com.aoxiaoyou.admin.entity.TravelerProgress;
import com.aoxiaoyou.admin.entity.TriggerLog;
import com.aoxiaoyou.admin.mapper.BadgeMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.CollectibleMapper;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.RewardMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.mapper.SysOperationLogMapper;
import com.aoxiaoyou.admin.mapper.TestAccountMapper;
import com.aoxiaoyou.admin.mapper.TravelerCheckinMapper;
import com.aoxiaoyou.admin.mapper.TravelerProfileMapper;
import com.aoxiaoyou.admin.mapper.TravelerProgressMapper;
import com.aoxiaoyou.admin.mapper.TriggerLogMapper;
import com.aoxiaoyou.admin.service.impl.AdminUserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private TravelerProfileMapper travelerProfileMapper;
    @Mock
    private TravelerProgressMapper travelerProgressMapper;
    @Mock
    private TravelerCheckinMapper travelerCheckinMapper;
    @Mock
    private TestAccountMapper testAccountMapper;
    @Mock
    private CityMapper cityMapper;
    @Mock
    private SubMapMapper subMapMapper;
    @Mock
    private CollectibleMapper collectibleMapper;
    @Mock
    private BadgeMapper badgeMapper;
    @Mock
    private RewardMapper rewardMapper;
    @Mock
    private PoiMapper poiMapper;
    @Mock
    private StoryLineMapper storyLineMapper;
    @Mock
    private SysOperationLogMapper sysOperationLogMapper;
    @Mock
    private TriggerLogMapper triggerLogMapper;

    private AdminUserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminUserServiceImpl(
                travelerProfileMapper,
                travelerProgressMapper,
                travelerCheckinMapper,
                testAccountMapper,
                cityMapper,
                subMapMapper,
                collectibleMapper,
                badgeMapper,
                rewardMapper,
                poiMapper,
                storyLineMapper,
                sysOperationLogMapper,
                triggerLogMapper,
                new ObjectMapper()
        );
    }

    @Test
    void getUserDetailSkipsPoiBatchLookupWhenTriggerLogsHaveNoResolvablePoiIds() {
        Long userId = 42L;
        TravelerProfile user = new TravelerProfile();
        user.setId(userId);
        user.setOpenId("user-42");
        user.setNickname("Carryover User");
        user.setLevel(5);
        user.setCurrentExp(120);
        user.setNextLevelExp(200);
        user.setTotalStamps(2);
        user.setCurrentCityId(1L);
        user.setCreatedAt(LocalDateTime.of(2026, 4, 15, 10, 0));
        user.setUpdatedAt(LocalDateTime.of(2026, 4, 15, 12, 0));

        TravelerProgress aggregate = new TravelerProgress();
        aggregate.setUserId(userId);
        aggregate.setProgressPercent(58);
        aggregate.setCreatedAt(LocalDateTime.of(2026, 4, 15, 9, 0));
        aggregate.setUpdatedAt(LocalDateTime.of(2026, 4, 15, 11, 0));

        TriggerLog triggerLog = new TriggerLog();
        triggerLog.setId(7L);
        triggerLog.setUserId(userId);
        triggerLog.setPoiId(null);
        triggerLog.setTriggerType("auto");
        triggerLog.setDistance(new BigDecimal("11.2"));
        triggerLog.setGpsAccuracy(new BigDecimal("6.0"));
        triggerLog.setWifiUsed(Boolean.FALSE);
        triggerLog.setCreatedAt(LocalDateTime.of(2026, 4, 15, 12, 30));

        when(travelerProfileMapper.selectById(userId)).thenReturn(user);
        when(travelerProgressMapper.selectList(any())).thenReturn(List.of(aggregate));
        when(travelerProgressMapper.selectOne(any())).thenReturn(aggregate);
        when(travelerCheckinMapper.selectList(any())).thenReturn(List.of(), List.of());
        when(triggerLogMapper.selectList(any())).thenReturn(List.of(triggerLog));
        when(testAccountMapper.selectCount(any())).thenReturn(0L);
        when(cityMapper.selectCount(any())).thenReturn(3L);
        when(subMapMapper.selectCount(any())).thenReturn(4L);
        when(collectibleMapper.selectCount(any())).thenReturn(5L);
        when(badgeMapper.selectCount(any())).thenReturn(2L);
        when(rewardMapper.selectCount(any())).thenReturn(6L);

        AdminUserDetailResponse detail = service.getUserDetail(userId);

        assertThat(detail.getCityProgress().getCompletedCount()).isEqualTo(1);
        assertThat(detail.getCityProgress().getTotalCount()).isEqualTo(3);
        assertThat(detail.getCollectibleProgress().getCompletedCount()).isEqualTo(2);
        assertThat(detail.getRecentTriggerLogs()).hasSize(1);
        assertThat(detail.getRecentTriggerLogs().get(0).getPoiName()).isEqualTo("Unknown POI");
        assertThat(detail.getRecentTriggerLogs().get(0).getDistanceMeters()).isEqualTo("11.2");
        assertThat(detail.getRecentCheckIns()).isEmpty();
        verify(poiMapper, never()).selectBatchIds(any());
    }
}
