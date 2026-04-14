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
                        .orderByAsc(Reward::getSortOrder)
                        .orderByAsc(Reward::getId));
        Page<AdminRewardResponse> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toRewardResponse).toList());
        return PageResponse.of(result);
    }

    @Override
    public AdminRewardResponse createReward(AdminRewardUpsertRequest.Upsert request) {
        Reward reward = new Reward();
        applyRewardRequest(reward, request);
        rewardMapper.insert(reward);
        return toRewardResponse(requireReward(reward.getId()));
    }

    @Override
    public AdminRewardResponse updateReward(Long rewardId, AdminRewardUpsertRequest.Upsert request) {
        Reward reward = requireReward(rewardId);
        applyRewardRequest(reward, request);
        rewardMapper.updateById(reward);
        return toRewardResponse(requireReward(rewardId));
    }

    @Override
    public void deleteReward(Long rewardId) {
        requireReward(rewardId);
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

    private Reward requireReward(Long rewardId) {
        Reward reward = rewardMapper.selectById(rewardId);
        if (reward == null) {
            throw new BusinessException(4045, "Reward not found");
        }
        return reward;
    }

    private void applyRewardRequest(Reward reward, AdminRewardUpsertRequest.Upsert request) {
        reward.setCode(request.getCode());
        reward.setNameZh(request.getNameZh());
        reward.setNameEn(request.getNameEn());
        reward.setNameZht(request.getNameZht());
        reward.setNamePt(request.getNamePt());
        reward.setSubtitleZh(request.getSubtitleZh());
        reward.setSubtitleEn(request.getSubtitleEn());
        reward.setSubtitleZht(request.getSubtitleZht());
        reward.setSubtitlePt(request.getSubtitlePt());
        reward.setDescriptionZh(request.getDescriptionZh());
        reward.setDescriptionEn(request.getDescriptionEn());
        reward.setDescriptionZht(request.getDescriptionZht());
        reward.setDescriptionPt(request.getDescriptionPt());
        reward.setHighlightZh(request.getHighlightZh());
        reward.setHighlightEn(request.getHighlightEn());
        reward.setHighlightZht(request.getHighlightZht());
        reward.setHighlightPt(request.getHighlightPt());
        reward.setStampCost(request.getStampCost() == null ? 0 : Math.max(request.getStampCost(), 0));
        reward.setInventoryTotal(request.getInventoryTotal() == null ? 0 : Math.max(request.getInventoryTotal(), 0));
        reward.setInventoryRedeemed(request.getInventoryRedeemed() == null ? 0 : Math.max(request.getInventoryRedeemed(), 0));
        reward.setCoverAssetId(request.getCoverAssetId());
        reward.setStatus(StringUtils.hasText(request.getStatus()) ? request.getStatus() : "draft");
        reward.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        reward.setPublishStartAt(parseDateTime(request.getPublishStartAt()));
        reward.setPublishEndAt(parseDateTime(request.getPublishEndAt()));
    }

    private LocalDateTime parseDateTime(String value) {
        return StringUtils.hasText(value) ? LocalDateTime.parse(value) : null;
    }

    private AdminRewardResponse toRewardResponse(Reward item) {
        int total = item.getInventoryTotal() == null ? 0 : item.getInventoryTotal();
        int redeemed = item.getInventoryRedeemed() == null ? 0 : item.getInventoryRedeemed();
        return AdminRewardResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .nameZh(item.getNameZh())
                .nameEn(item.getNameEn())
                .nameZht(item.getNameZht())
                .namePt(item.getNamePt())
                .subtitleZh(item.getSubtitleZh())
                .subtitleEn(item.getSubtitleEn())
                .subtitleZht(item.getSubtitleZht())
                .subtitlePt(item.getSubtitlePt())
                .descriptionZh(item.getDescriptionZh())
                .descriptionEn(item.getDescriptionEn())
                .descriptionZht(item.getDescriptionZht())
                .descriptionPt(item.getDescriptionPt())
                .highlightZh(item.getHighlightZh())
                .highlightEn(item.getHighlightEn())
                .highlightZht(item.getHighlightZht())
                .highlightPt(item.getHighlightPt())
                .stampCost(item.getStampCost())
                .inventoryTotal(total)
                .inventoryRedeemed(redeemed)
                .inventoryRemaining(Math.max(total - redeemed, 0))
                .coverAssetId(item.getCoverAssetId())
                .status(item.getStatus())
                .sortOrder(item.getSortOrder())
                .publishStartAt(item.getPublishStartAt())
                .publishEndAt(item.getPublishEndAt())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
