package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminCollectibleUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRewardUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminRewardResponse;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.Collectible;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.IndoorFloor;
import com.aoxiaoyou.admin.entity.Reward;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.entity.SubMap;
import com.aoxiaoyou.admin.mapper.BadgeMapper;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.CollectibleMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.IndoorFloorMapper;
import com.aoxiaoyou.admin.mapper.RewardMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminContentRelationService;
import com.aoxiaoyou.admin.service.impl.AdminCollectibleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminCollectibleServiceImplTest {

    @Mock
    private CollectibleMapper collectibleMapper;
    @Mock
    private BadgeMapper badgeMapper;
    @Mock
    private RewardMapper rewardMapper;
    @Mock
    private StoryLineMapper storyLineMapper;
    @Mock
    private CityMapper cityMapper;
    @Mock
    private SubMapMapper subMapMapper;
    @Mock
    private BuildingMapper buildingMapper;
    @Mock
    private IndoorFloorMapper indoorFloorMapper;
    @Mock
    private ContentAssetMapper contentAssetMapper;
    @Mock
    private AdminContentRelationService adminContentRelationService;

    private AdminCollectibleServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminCollectibleServiceImpl(
                collectibleMapper,
                badgeMapper,
                rewardMapper,
                storyLineMapper,
                cityMapper,
                subMapMapper,
                buildingMapper,
                indoorFloorMapper,
                contentAssetMapper,
                adminContentRelationService
        );
    }

    @Test
    void updateRewardPreservesCarryoverBindingsAndPresetFields() {
        Long rewardId = 31L;
        Reward reward = new Reward();
        reward.setId(rewardId);
        reward.setCreatedAt(LocalDateTime.of(2026, 4, 15, 12, 0));

        when(rewardMapper.selectById(rewardId)).thenReturn(reward, reward);
        when(storyLineMapper.selectById(8L)).thenReturn(new StoryLine());
        when(cityMapper.selectById(1L)).thenReturn(new City());
        when(subMapMapper.selectById(2L)).thenReturn(new SubMap());
        when(buildingMapper.selectById(5L)).thenReturn(new Building());
        IndoorFloor floor = new IndoorFloor();
        floor.setId(11L);
        floor.setBuildingId(5L);
        when(indoorFloorMapper.selectById(11L)).thenReturn(floor);
        when(contentAssetMapper.selectById(300038L)).thenReturn(new ContentAsset());

        when(adminContentRelationService.listTargetIds("reward", rewardId, "storyline_binding", "storyline")).thenReturn(List.of(8L));
        when(adminContentRelationService.listTargetIds("reward", rewardId, "city_binding", "city")).thenReturn(List.of(1L));
        when(adminContentRelationService.listTargetIds("reward", rewardId, "sub_map_binding", "sub_map")).thenReturn(List.of(2L));
        when(adminContentRelationService.listTargetIds("reward", rewardId, "indoor_building_binding", "indoor_building")).thenReturn(List.of(5L));
        when(adminContentRelationService.listTargetIds("reward", rewardId, "indoor_floor_binding", "indoor_floor")).thenReturn(List.of(11L));
        when(adminContentRelationService.listTargetIds("reward", rewardId, "attachment_asset", "asset")).thenReturn(List.of(300038L));

        AdminRewardUpsertRequest.Upsert request = new AdminRewardUpsertRequest.Upsert();
        request.setCode("reward_lisboeta_secret_cut");
        request.setNameZh("葡京人秘藏片段");
        request.setNameZht("葡京人秘藏片段");
        request.setSubtitleZht("夜遊收官彩蛋");
        request.setDescriptionZht("完成路線後可兌換的限定剪輯。");
        request.setHighlightZht("限時兌換");
        request.setStampCost(36);
        request.setInventoryTotal(120);
        request.setInventoryRedeemed(4);
        request.setCoverAssetId(300038L);
        request.setPopupPresetCode("reward-modal");
        request.setPopupConfigJson("{\"ctaLabel\":\"立即查看\"}");
        request.setDisplayPresetCode("inventory-card");
        request.setDisplayConfigJson("{\"accent\":\"ruby\"}");
        request.setTriggerPresetCode("reward-redemption");
        request.setTriggerConfigJson("{\"consumeStamps\":true}");
        request.setExampleContentZht("完成夜間路線後即可兌換剪輯與彩蛋內容。");
        request.setStatus("published");
        request.setSortOrder(31);
        request.setPublishStartAt("2026-04-15T10:00:00");
        request.setPublishEndAt("2026-05-15T10:00:00");
        request.setStorylineBindings(List.of(8L));
        request.setCityBindings(List.of(1L));
        request.setSubMapBindings(List.of(2L));
        request.setIndoorBuildingBindings(List.of(5L));
        request.setIndoorFloorBindings(List.of(11L));
        request.setAttachmentAssetIds(List.of(300038L));

        AdminRewardResponse response = service.updateReward(rewardId, request);

        assertThat(response.getCode()).isEqualTo("reward_lisboeta_secret_cut");
        assertThat(response.getNameZh()).isEqualTo("葡京人秘藏片段");
        assertThat(response.getNameZht()).isEqualTo("葡京人秘藏片段");
        assertThat(response.getPopupPresetCode()).isEqualTo("reward-modal");
        assertThat(response.getDisplayPresetCode()).isEqualTo("inventory-card");
        assertThat(response.getTriggerPresetCode()).isEqualTo("reward-redemption");
        assertThat(response.getCoverAssetId()).isEqualTo(300038L);
        assertThat(response.getStorylineBindings()).containsExactly(8L);
        assertThat(response.getCityBindings()).containsExactly(1L);
        assertThat(response.getSubMapBindings()).containsExactly(2L);
        assertThat(response.getIndoorBuildingBindings()).containsExactly(5L);
        assertThat(response.getIndoorFloorBindings()).containsExactly(11L);
        assertThat(response.getAttachmentAssetIds()).containsExactly(300038L);
        assertThat(response.getInventoryRemaining()).isEqualTo(116);
        assertThat(response.getPublishStartAt()).isEqualTo(LocalDateTime.parse("2026-04-15T10:00:00"));
        assertThat(response.getPublishEndAt()).isEqualTo(LocalDateTime.parse("2026-05-15T10:00:00"));

        verify(rewardMapper).updateById(reward);
        verify(adminContentRelationService).syncTargetIds("reward", rewardId, "storyline_binding", "storyline", List.of(8L));
        verify(adminContentRelationService).syncTargetIds("reward", rewardId, "city_binding", "city", List.of(1L));
        verify(adminContentRelationService).syncTargetIds("reward", rewardId, "sub_map_binding", "sub_map", List.of(2L));
        verify(adminContentRelationService).syncTargetIds("reward", rewardId, "indoor_building_binding", "indoor_building", List.of(5L));
        verify(adminContentRelationService).syncTargetIds("reward", rewardId, "indoor_floor_binding", "indoor_floor", List.of(11L));
        verify(adminContentRelationService).syncTargetIds("reward", rewardId, "attachment_asset", "asset", List.of(300038L));
    }

    @Test
    void updateCollectibleRejectsFloorsOutsideSelectedBuildings() {
        Long collectibleId = 12L;
        Collectible collectible = new Collectible();
        collectible.setId(collectibleId);
        when(collectibleMapper.selectById(collectibleId)).thenReturn(collectible);
        when(buildingMapper.selectById(5L)).thenReturn(new Building());

        IndoorFloor floor = new IndoorFloor();
        floor.setId(11L);
        floor.setBuildingId(99L);
        when(indoorFloorMapper.selectById(11L)).thenReturn(floor);

        AdminCollectibleUpsertRequest request = new AdminCollectibleUpsertRequest();
        request.setCollectibleCode("collectible_lisboeta_night_pass");
        request.setNameZh("葡京人夜行通票");
        request.setIndoorBuildingBindings(List.of(5L));
        request.setIndoorFloorBindings(List.of(11L));

        assertThatThrownBy(() -> service.updateCollectible(collectibleId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("indoor_floor bindings must belong to the selected indoor_building bindings");

        verify(collectibleMapper, never()).updateById(any());
    }
}
