package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminRedeemablePrizeUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminRewardPresentationUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminRedeemablePrizeResponse;
import com.aoxiaoyou.admin.dto.response.AdminRewardPresentationResponse;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.entity.City;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.IndoorFloor;
import com.aoxiaoyou.admin.entity.RewardPresentation;
import com.aoxiaoyou.admin.entity.RewardPresentationStep;
import com.aoxiaoyou.admin.entity.RewardRule;
import com.aoxiaoyou.admin.entity.RewardRuleBinding;
import com.aoxiaoyou.admin.entity.RedeemablePrize;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.entity.SubMap;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.GameRewardMapper;
import com.aoxiaoyou.admin.mapper.IndoorFloorMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeBehaviorMapper;
import com.aoxiaoyou.admin.mapper.RedeemablePrizeMapper;
import com.aoxiaoyou.admin.mapper.RewardConditionGroupMapper;
import com.aoxiaoyou.admin.mapper.RewardConditionMapper;
import com.aoxiaoyou.admin.mapper.RewardPresentationMapper;
import com.aoxiaoyou.admin.mapper.RewardPresentationStepMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleBindingMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.service.AdminContentRelationService;
import com.aoxiaoyou.admin.service.impl.AdminRewardDomainServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRewardDomainServiceImplTest {

    @Mock private RedeemablePrizeMapper redeemablePrizeMapper;
    @Mock private GameRewardMapper gameRewardMapper;
    @Mock private RewardRuleMapper rewardRuleMapper;
    @Mock private RewardConditionGroupMapper rewardConditionGroupMapper;
    @Mock private RewardConditionMapper rewardConditionMapper;
    @Mock private RewardRuleBindingMapper rewardRuleBindingMapper;
    @Mock private RewardPresentationMapper rewardPresentationMapper;
    @Mock private RewardPresentationStepMapper rewardPresentationStepMapper;
    @Mock private StoryLineMapper storyLineMapper;
    @Mock private CityMapper cityMapper;
    @Mock private SubMapMapper subMapMapper;
    @Mock private BuildingMapper buildingMapper;
    @Mock private IndoorFloorMapper indoorFloorMapper;
    @Mock private ContentAssetMapper contentAssetMapper;
    @Mock private IndoorNodeBehaviorMapper indoorNodeBehaviorMapper;
    @Mock private AdminContentRelationService adminContentRelationService;

    private AdminRewardDomainServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminRewardDomainServiceImpl(
                redeemablePrizeMapper,
                gameRewardMapper,
                rewardRuleMapper,
                rewardConditionGroupMapper,
                rewardConditionMapper,
                rewardRuleBindingMapper,
                rewardPresentationMapper,
                rewardPresentationStepMapper,
                storyLineMapper,
                cityMapper,
                subMapMapper,
                buildingMapper,
                indoorFloorMapper,
                contentAssetMapper,
                indoorNodeBehaviorMapper,
                adminContentRelationService
        );
    }

    @Test
    void createRedeemablePrizePersistsBindingsAndSharedRuleLinks() {
        RewardPresentation presentation = new RewardPresentation();
        presentation.setId(88L);
        presentation.setCode("presentation_reward");

        RewardRule ruleOne = new RewardRule();
        ruleOne.setId(501L);
        ruleOne.setCode("rule_redemption_primary");
        ruleOne.setNameZh("主要兌換條件");
        ruleOne.setNameZht("主要兌換條件");
        ruleOne.setSummaryText("需達成主要條件");
        ruleOne.setStatus("published");

        RewardRule ruleTwo = new RewardRule();
        ruleTwo.setId(502L);
        ruleTwo.setCode("rule_redemption_secondary");
        ruleTwo.setNameZh("次要兌換條件");
        ruleTwo.setNameZht("次要兌換條件");
        ruleTwo.setSummaryText("需達成次要條件");
        ruleTwo.setStatus("published");

        when(rewardPresentationMapper.selectById(88L)).thenReturn(presentation);
        when(rewardRuleMapper.selectBatchIds(List.of(501L, 502L))).thenReturn(List.of(ruleOne, ruleTwo));
        when(storyLineMapper.selectById(8L)).thenReturn(new StoryLine());
        when(cityMapper.selectById(1L)).thenReturn(new City());
        when(subMapMapper.selectById(2L)).thenReturn(new SubMap());
        when(buildingMapper.selectById(5L)).thenReturn(new Building());
        IndoorFloor floor = new IndoorFloor();
        floor.setId(11L);
        floor.setBuildingId(5L);
        when(indoorFloorMapper.selectById(11L)).thenReturn(floor);
        when(contentAssetMapper.selectById(300009L)).thenReturn(new ContentAsset());
        when(contentAssetMapper.selectById(300010L)).thenReturn(new ContentAsset());

        ArgumentCaptor<RedeemablePrize> prizeCaptor = ArgumentCaptor.forClass(RedeemablePrize.class);
        doAnswer(invocation -> {
            RedeemablePrize prize = invocation.getArgument(0);
            prize.setId(101L);
            prize.setCreatedAt(LocalDateTime.of(2026, 4, 18, 14, 30));
            prize.setUpdatedAt(LocalDateTime.of(2026, 4, 18, 14, 30));
            return 1;
        }).when(redeemablePrizeMapper).insert(prizeCaptor.capture());

        RewardRuleBinding bindingOne = new RewardRuleBinding();
        bindingOne.setRuleId(501L);
        bindingOne.setOwnerDomain("redeemable_prize");
        bindingOne.setOwnerId(101L);
        RewardRuleBinding bindingTwo = new RewardRuleBinding();
        bindingTwo.setRuleId(502L);
        bindingTwo.setOwnerDomain("redeemable_prize");
        bindingTwo.setOwnerId(101L);
        when(rewardRuleBindingMapper.selectList(any()))
                .thenReturn(List.of())
                .thenReturn(List.of(bindingOne, bindingTwo));

        RedeemablePrize stored = new RedeemablePrize();
        stored.setId(101L);
        stored.setCode("prize_lisboeta_offline_postcard");
        stored.setPrizeType("postcard");
        stored.setFulfillmentMode("offline_pickup");
        stored.setNameZh("葡京人夜巡明信片");
        stored.setNameZht("葡京人夜巡明信片");
        stored.setCoverAssetId(300009L);
        stored.setStampCost(42);
        stored.setInventoryTotal(60);
        stored.setInventoryRedeemed(5);
        stored.setPresentationId(88L);
        stored.setStatus("published");
        stored.setSortOrder(10);
        stored.setPublishStartAt(LocalDateTime.parse("2026-04-18T12:00:00"));
        stored.setPublishEndAt(LocalDateTime.parse("2026-12-31T23:59:59"));
        stored.setCreatedAt(LocalDateTime.of(2026, 4, 18, 14, 30));
        when(redeemablePrizeMapper.selectById(101L)).thenReturn(stored);

        when(adminContentRelationService.listTargetIds("redeemable_prize", 101L, "storyline_binding", "storyline"))
                .thenReturn(List.of(8L));
        when(adminContentRelationService.listTargetIds("redeemable_prize", 101L, "city_binding", "city"))
                .thenReturn(List.of(1L));
        when(adminContentRelationService.listTargetIds("redeemable_prize", 101L, "sub_map_binding", "sub_map"))
                .thenReturn(List.of(2L));
        when(adminContentRelationService.listTargetIds("redeemable_prize", 101L, "indoor_building_binding", "indoor_building"))
                .thenReturn(List.of(5L));
        when(adminContentRelationService.listTargetIds("redeemable_prize", 101L, "indoor_floor_binding", "indoor_floor"))
                .thenReturn(List.of(11L));
        when(adminContentRelationService.listTargetIds("redeemable_prize", 101L, "attachment_asset", "asset"))
                .thenReturn(List.of(300010L));

        AdminRedeemablePrizeUpsertRequest request = new AdminRedeemablePrizeUpsertRequest();
        request.setCode("prize_lisboeta_offline_postcard");
        request.setNameZh("葡京人夜巡明信片");
        request.setNameZht("葡京人夜巡明信片");
        request.setPrizeType("postcard");
        request.setFulfillmentMode("offline_pickup");
        request.setCoverAssetId(300009L);
        request.setStampCost(42);
        request.setInventoryTotal(60);
        request.setInventoryRedeemed(5);
        request.setPresentationId(88L);
        request.setRuleIds(List.of(501L, 502L));
        request.setStorylineBindings(List.of(8L));
        request.setCityBindings(List.of(1L));
        request.setSubMapBindings(List.of(2L));
        request.setIndoorBuildingBindings(List.of(5L));
        request.setIndoorFloorBindings(List.of(11L));
        request.setAttachmentAssetIds(List.of(300010L));
        request.setStatus("published");
        request.setSortOrder(10);
        request.setPublishStartAt("2026-04-18T12:00:00");
        request.setPublishEndAt("2026-12-31T23:59:59");

        AdminRedeemablePrizeResponse response = service.createRedeemablePrize(request);

        assertThat(prizeCaptor.getValue().getPresentationId()).isEqualTo(88L);
        assertThat(prizeCaptor.getValue().getFulfillmentMode()).isEqualTo("offline_pickup");
        assertThat(response.getCode()).isEqualTo("prize_lisboeta_offline_postcard");
        assertThat(response.getPresentation()).isNotNull();
        assertThat(response.getPresentation().getCode()).isEqualTo("presentation_reward");
        assertThat(response.getRuleIds()).containsExactly(501L, 502L);
        assertThat(response.getLinkedRules()).hasSize(2);
        assertThat(response.getStorylineBindings()).containsExactly(8L);
        assertThat(response.getIndoorFloorBindings()).containsExactly(11L);
        assertThat(response.getAttachmentAssetIds()).containsExactly(300010L);
        assertThat(response.getInventoryRemaining()).isEqualTo(55);

        verify(adminContentRelationService).syncTargetIds("redeemable_prize", 101L, "storyline_binding", "storyline", List.of(8L));
        verify(adminContentRelationService).syncTargetIds("redeemable_prize", 101L, "city_binding", "city", List.of(1L));
        verify(adminContentRelationService).syncTargetIds("redeemable_prize", 101L, "sub_map_binding", "sub_map", List.of(2L));
        verify(adminContentRelationService).syncTargetIds("redeemable_prize", 101L, "indoor_building_binding", "indoor_building", List.of(5L));
        verify(adminContentRelationService).syncTargetIds("redeemable_prize", 101L, "indoor_floor_binding", "indoor_floor", List.of(11L));
        verify(adminContentRelationService).syncTargetIds("redeemable_prize", 101L, "attachment_asset", "asset", List.of(300010L));
        verify(rewardRuleBindingMapper, times(2)).insert(any(RewardRuleBinding.class));
    }

    @Test
    void createRewardPresentationPersistsStructuredSteps() {
        ArgumentCaptor<RewardPresentation> presentationCaptor = ArgumentCaptor.forClass(RewardPresentation.class);
        doAnswer(invocation -> {
            RewardPresentation presentation = invocation.getArgument(0);
            presentation.setId(201L);
            presentation.setCreatedAt(LocalDateTime.of(2026, 4, 18, 15, 0));
            return 1;
        }).when(rewardPresentationMapper).insert(presentationCaptor.capture());

        RewardPresentation stored = new RewardPresentation();
        stored.setId(201L);
        stored.setCode("presentation_lisboeta_fullscreen_finale");
        stored.setNameZh("葡京人夜巡終章演出");
        stored.setNameZht("葡京人夜巡終章演出");
        stored.setPresentationType("fullscreen_video");
        stored.setFirstTimeOnly(1);
        stored.setSkippable(1);
        stored.setMinimumDisplayMs(4200);
        stored.setInterruptPolicy("queue_after_current");
        stored.setQueuePolicy("enqueue");
        stored.setPriorityWeight(95);
        stored.setCoverAssetId(300009L);
        stored.setVoiceOverAssetId(300010L);
        stored.setSfxAssetId(300010L);
        stored.setSummaryText("終章演出");
        stored.setConfigJson("{\"blockMapInput\":true}");
        stored.setStatus("published");
        stored.setCreatedAt(LocalDateTime.of(2026, 4, 18, 15, 0));
        when(rewardPresentationMapper.selectById(201L)).thenReturn(stored);
        when(rewardPresentationStepMapper.selectList(any())).thenReturn(List.of(
                buildStep(201L, "intro_video", 0),
                buildStep(201L, "reward_recap", 1)
        ));
        when(contentAssetMapper.selectById(300009L)).thenReturn(new ContentAsset());
        when(contentAssetMapper.selectById(300010L)).thenReturn(new ContentAsset());
        when(redeemablePrizeMapper.selectList(any())).thenReturn(List.of());
        when(gameRewardMapper.selectList(any())).thenReturn(List.of());

        AdminRewardPresentationUpsertRequest request = new AdminRewardPresentationUpsertRequest();
        request.setCode("presentation_lisboeta_fullscreen_finale");
        request.setNameZh("葡京人夜巡終章演出");
        request.setNameZht("葡京人夜巡終章演出");
        request.setPresentationType("fullscreen_video");
        request.setFirstTimeOnly(1);
        request.setSkippable(1);
        request.setMinimumDisplayMs(4200);
        request.setInterruptPolicy("queue_after_current");
        request.setQueuePolicy("enqueue");
        request.setPriorityWeight(95);
        request.setCoverAssetId(300009L);
        request.setVoiceOverAssetId(300010L);
        request.setSfxAssetId(300010L);
        request.setSummaryText("終章演出");
        request.setConfigJson("{\"blockMapInput\":true}");
        request.setStatus("published");
        request.setSteps(List.of(
                buildStepPayload("intro_video", 0),
                buildStepPayload("reward_recap", 1)
        ));

        AdminRewardPresentationResponse response = service.createRewardPresentation(request);

        assertThat(presentationCaptor.getValue().getPresentationType()).isEqualTo("fullscreen_video");
        assertThat(response.getPresentationType()).isEqualTo("fullscreen_video");
        assertThat(response.getSteps()).hasSize(2);
        assertThat(response.getSteps().get(0).getStepCode()).isEqualTo("intro_video");
        verify(rewardPresentationStepMapper, times(2)).insert(any(RewardPresentationStep.class));
    }

    @Test
    void deleteRewardRuleRejectsWhenIndoorBehaviorStillLinked() {
        RewardRule rule = new RewardRule();
        rule.setId(301L);
        rule.setCode("rule_indoor_shared");
        when(rewardRuleMapper.selectById(301L)).thenReturn(rule);
        when(rewardRuleBindingMapper.selectCount(any())).thenReturn(1L);

        RewardRuleBinding indoorBinding = new RewardRuleBinding();
        indoorBinding.setRuleId(301L);
        indoorBinding.setOwnerDomain("indoor_behavior");
        indoorBinding.setOwnerId(66L);
        when(rewardRuleBindingMapper.selectList(any())).thenReturn(List.of(indoorBinding));

        assertThatThrownBy(() -> service.deleteRewardRule(301L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("室內互動行為")
                .hasMessageContaining("不能直接刪除");

        verify(rewardRuleMapper, never()).deleteById(eq(301L));
    }

    private RewardPresentationStep buildStep(Long presentationId, String stepCode, int sortOrder) {
        RewardPresentationStep step = new RewardPresentationStep();
        step.setPresentationId(presentationId);
        step.setStepType("fullscreen_video");
        step.setStepCode(stepCode);
        step.setTitleText(stepCode);
        step.setSortOrder(sortOrder);
        return step;
    }

    private AdminRewardPresentationUpsertRequest.StepPayload buildStepPayload(String stepCode, int sortOrder) {
        AdminRewardPresentationUpsertRequest.StepPayload step = new AdminRewardPresentationUpsertRequest.StepPayload();
        step.setStepType("fullscreen_video");
        step.setStepCode(stepCode);
        step.setTitleText(stepCode);
        step.setDurationMs(1200 + (sortOrder * 200));
        step.setSortOrder(sortOrder);
        return step;
    }
}
