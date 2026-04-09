package com.aoxiaoyou.admin.service.impl;

import com.aoxiaoyou.admin.common.api.PageResponse;
import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminRewardUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminMapTileResponse;
import com.aoxiaoyou.admin.dto.response.AdminOperationLogResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardResponse;
import com.aoxiaoyou.admin.dto.response.AdminSystemConfigResponse;
import com.aoxiaoyou.admin.entity.MapTileConfig;
import com.aoxiaoyou.admin.entity.Reward;
import com.aoxiaoyou.admin.entity.SysConfig;
import com.aoxiaoyou.admin.entity.SysOperationLog;
import com.aoxiaoyou.admin.mapper.MapTileConfigMapper;
import com.aoxiaoyou.admin.mapper.RewardMapper;
import com.aoxiaoyou.admin.mapper.SysConfigMapper;
import com.aoxiaoyou.admin.mapper.SysOperationLogMapper;
import com.aoxiaoyou.admin.service.AdminSystemManagementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminSystemManagementServiceImpl implements AdminSystemManagementService {

    private final RewardMapper rewardMapper;
    private final SysOperationLogMapper sysOperationLogMapper;
    private final SysConfigMapper sysConfigMapper;
    private final MapTileConfigMapper mapTileConfigMapper;

    @Override
    public PageResponse<AdminRewardResponse> pageRewards(long pageNum, long pageSize, String status) {
        Page<Reward> page = rewardMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Reward>()
                        .eq(StringUtils.hasText(status), Reward::getStatus, status)
                        .orderByDesc(Reward::getCreatedAt));
        Page<AdminRewardResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toRewardResponse).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminRewardResponse createReward(AdminRewardUpsertRequest.Upsert request) {
        Reward reward = new Reward();
        applyRewardRequest(reward, request);
        rewardMapper.insert(reward);
        return toRewardResponse(rewardMapper.selectById(reward.getId()));
    }

    @Override
    public AdminRewardResponse updateReward(Long rewardId, AdminRewardUpsertRequest.Upsert request) {
        Reward reward = rewardMapper.selectById(rewardId);
        if (reward == null) {
            throw new BusinessException(4045, "奖励不存在");
        }
        applyRewardRequest(reward, request);
        rewardMapper.updateById(reward);
        return toRewardResponse(rewardMapper.selectById(rewardId));
    }

    @Override
    public void deleteReward(Long rewardId) {
        if (rewardMapper.selectById(rewardId) == null) {
            throw new BusinessException(4045, "奖励不存在");
        }
        rewardMapper.deleteById(rewardId);
    }

    @Override
    public PageResponse<AdminOperationLogResponse> pageAuditLogs(long pageNum, long pageSize, String module) {
        Page<SysOperationLog> page = sysOperationLogMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysOperationLog>()
                        .eq(StringUtils.hasText(module), SysOperationLog::getModule, module)
                        .orderByDesc(SysOperationLog::getCreatedAt));
        Page<AdminOperationLogResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(log -> AdminOperationLogResponse.builder()
                .id(log.getId())
                .operationType(log.getOperation())
                .operationTypeName(log.getModule() + " / " + log.getOperation())
                .operationDesc(log.getRequestParams())
                .adminName(log.getAdminUsername())
                .ipAddress(log.getIp())
                .createTime(log.getCreatedAt())
                .build()).toList());
        return PageResponse.of(result);
    }

    @Override
    public PageResponse<AdminSystemConfigResponse> pageConfigs(long pageNum, long pageSize, String keyword) {
        Page<SysConfig> page = sysConfigMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SysConfig>()
                        .and(StringUtils.hasText(keyword), q -> q.like(SysConfig::getConfigKey, keyword).or().like(SysConfig::getDescription, keyword))
                        .orderByAsc(SysConfig::getConfigKey));
        Page<AdminSystemConfigResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(item -> AdminSystemConfigResponse.builder()
                .id(item.getId())
                .configKey(item.getConfigKey())
                .configValue(item.getConfigValue())
                .configType(item.getConfigType())
                .description(item.getDescription())
                .updatedAt(item.getUpdatedAt())
                .build()).toList());
        return PageResponse.of(result);
    }

    @Override
    public PageResponse<AdminMapTileResponse> pageMapTiles(long pageNum, long pageSize) {
        Page<MapTileConfig> page = mapTileConfigMapper.selectPage(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<MapTileConfig>().orderByDesc(MapTileConfig::getUpdatedAt).orderByDesc(MapTileConfig::getId));
        Page<AdminMapTileResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(item -> AdminMapTileResponse.builder()
                .id(item.getId())
                .mapId(item.getMapId())
                .style(item.getStyle())
                .cdnBase(item.getCdnBase())
                .controlPointsUrl(item.getControlPointsUrl())
                .poisUrl(item.getPoisUrl())
                .zoomMin(item.getZoomMin())
                .zoomMax(item.getZoomMax())
                .defaultZoom(item.getDefaultZoom())
                .centerLat(item.getCenterLat())
                .centerLng(item.getCenterLng())
                .version(item.getVersion())
                .status(item.getStatus())
                .updatedAt(item.getUpdatedAt())
                .build()).toList());
        return PageResponse.of(result);
    }

    private void applyRewardRequest(Reward reward, AdminRewardUpsertRequest.Upsert request) {
        reward.setNameZh(request.getName());
        reward.setDescription(request.getDescription());
        reward.setStampsRequired(request.getStampsRequired() == null ? 1 : Math.max(request.getStampsRequired(), 0));
        reward.setTotalQuantity(request.getTotalQuantity() == null ? 0 : Math.max(request.getTotalQuantity(), 0));
        reward.setRedeemedCount(request.getRedeemedCount() == null ? 0 : Math.max(request.getRedeemedCount(), 0));
        reward.setStartTime(parseDateTime(request.getStartTime()));
        reward.setEndTime(parseDateTime(request.getEndTime()));
        reward.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "inactive");
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private AdminRewardResponse toRewardResponse(Reward item) {
        return AdminRewardResponse.builder()
                .id(item.getId())
                .name(item.getNameZh())
                .description(item.getDescription())
                .stampsRequired(item.getStampsRequired())
                .totalQuantity(item.getTotalQuantity())
                .redeemedCount(item.getRedeemedCount())
                .remainingQuantity((item.getTotalQuantity() == null ? 0 : item.getTotalQuantity()) - (item.getRedeemedCount() == null ? 0 : item.getRedeemedCount()))
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .status(item.getStatus())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
