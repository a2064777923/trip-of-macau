package com.aoxiaoyou.tripofmacau.service.impl;

import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.dto.request.TriggerLogCreateRequest;
import com.aoxiaoyou.tripofmacau.dto.response.TriggerLogResponse;
import com.aoxiaoyou.tripofmacau.entity.Poi;
import com.aoxiaoyou.tripofmacau.entity.TriggerLog;
import com.aoxiaoyou.tripofmacau.entity.User;
import com.aoxiaoyou.tripofmacau.mapper.PoiMapper;
import com.aoxiaoyou.tripofmacau.mapper.TriggerLogMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserMapper;
import com.aoxiaoyou.tripofmacau.service.TriggerLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TriggerLogServiceImpl implements TriggerLogService {

    private final TriggerLogMapper triggerLogMapper;
    private final UserMapper userMapper;
    private final PoiMapper poiMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TriggerLogResponse create(TriggerLogCreateRequest request) {
        User user = userMapper.selectById(request.getUserId());
        if (user == null) {
            throw new BusinessException(4040, "用户不存在");
        }
        Poi poi = poiMapper.selectById(request.getPoiId());
        if (poi == null) {
            throw new BusinessException(4041, "POI 不存在");
        }

        TriggerLog triggerLog = new TriggerLog();
        triggerLog.setUserId(request.getUserId());
        triggerLog.setPoiId(request.getPoiId());
        triggerLog.setTriggerType(request.getTriggerType());
        triggerLog.setDistance(request.getDistance());
        triggerLog.setGpsAccuracy(request.getGpsAccuracy());
        triggerLog.setWifiUsed(Boolean.TRUE.equals(request.getWifiUsed()));
        triggerLogMapper.insert(triggerLog);

        TriggerLog saved = triggerLogMapper.selectById(triggerLog.getId());
        return TriggerLogResponse.builder()
                .id(saved.getId())
                .userId(saved.getUserId())
                .poiId(saved.getPoiId())
                .triggerType(saved.getTriggerType())
                .distance(saved.getDistance())
                .gpsAccuracy(saved.getGpsAccuracy())
                .wifiUsed(saved.getWifiUsed())
                .createdAt(saved.getCreatedAt())
                .build();
    }
}
