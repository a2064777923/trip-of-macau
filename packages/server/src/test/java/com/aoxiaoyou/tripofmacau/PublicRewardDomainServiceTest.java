package com.aoxiaoyou.tripofmacau;

import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.response.GameRewardResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RedeemablePrizeResponse;
import com.aoxiaoyou.tripofmacau.dto.response.RewardPresentationResponse;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.aoxiaoyou.tripofmacau.entity.GameReward;
import com.aoxiaoyou.tripofmacau.entity.RedeemablePrize;
import com.aoxiaoyou.tripofmacau.entity.RewardPresentation;
import com.aoxiaoyou.tripofmacau.entity.RewardPresentationStep;
import com.aoxiaoyou.tripofmacau.entity.RewardRule;
import com.aoxiaoyou.tripofmacau.entity.RewardRuleBinding;
import com.aoxiaoyou.tripofmacau.service.CatalogFoundationService;
import com.aoxiaoyou.tripofmacau.service.RuntimeSettingsService;
import com.aoxiaoyou.tripofmacau.service.impl.PublicCatalogServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicRewardDomainServiceTest {

    @Mock
    private CatalogFoundationService catalogFoundationService;
    @Mock
    private RuntimeSettingsService runtimeSettingsService;

    private PublicCatalogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PublicCatalogServiceImpl(
                catalogFoundationService,
                runtimeSettingsService,
                new LocalizedContentSupport(new ObjectMapper())
        );
    }

    @Test
    void listRedeemablePrizesProjectsSplitRewardRulesAndPresentation() {
        RedeemablePrize prize = new RedeemablePrize();
        prize.setId(2001L);
        prize.setCode("prize_fire_archive_box");
        prize.setPrizeType("merchandise");
        prize.setFulfillmentMode("offline_pickup");
        prize.setNameZh("火線檔案盒");
        prize.setNameZht("火線檔案盒");
        prize.setDescriptionZht("限定故事周邊");
        prize.setHighlightZht("完成主線可兌換");
        prize.setStampCost(48);
        prize.setInventoryTotal(120);
        prize.setInventoryRedeemed(20);
        prize.setCoverAssetId(3001L);
        prize.setPresentationId(4001L);
        prize.setStockPolicyJson("{\"stockMode\":\"limited\"}");
        prize.setFulfillmentConfigJson("{\"pickupVenue\":\"旅遊服務站\"}");

        RewardRule rule = new RewardRule();
        rule.setId(5001L);
        rule.setCode("rule_fire_route_completion");
        rule.setRuleType("redemption_rule");
        rule.setNameZh("主線完成");
        rule.setNameZht("主線完成");
        rule.setSummaryText("完成指定故事線即可兌換");

        RewardRuleBinding ruleBinding = new RewardRuleBinding();
        ruleBinding.setOwnerId(prize.getId());
        ruleBinding.setOwnerDomain("redeemable_prize");
        ruleBinding.setRuleId(rule.getId());

        RewardPresentation presentation = new RewardPresentation();
        presentation.setId(4001L);
        presentation.setCode("presentation_fire_route_unlock");
        presentation.setNameZh("全屏解鎖演出");
        presentation.setNameZht("全屏解鎖演出");
        presentation.setPresentationType("fullscreen_video");
        presentation.setFirstTimeOnly(1);
        presentation.setSkippable(0);
        presentation.setMinimumDisplayMs(2800);
        presentation.setInterruptPolicy("block_until_idle");
        presentation.setQueuePolicy("enqueue");
        presentation.setCoverAssetId(3002L);
        presentation.setVoiceOverAssetId(3003L);
        presentation.setSfxAssetId(3004L);

        RewardPresentationStep step = new RewardPresentationStep();
        step.setPresentationId(presentation.getId());
        step.setStepType("fullscreen_video");
        step.setStepCode("step_1");
        step.setTitleText("濠江烽煙已解鎖");
        step.setAssetId(3005L);
        step.setDurationMs(2800);

        ContentAsset cover = new ContentAsset();
        cover.setId(3001L);
        cover.setCanonicalUrl("https://cdn.example.com/prize-cover.png");
        ContentAsset presentationCover = new ContentAsset();
        presentationCover.setId(3002L);
        presentationCover.setCanonicalUrl("https://cdn.example.com/presentation-cover.png");
        ContentAsset voiceOver = new ContentAsset();
        voiceOver.setId(3003L);
        voiceOver.setCanonicalUrl("https://cdn.example.com/presentation-voice.mp3");
        ContentAsset sfx = new ContentAsset();
        sfx.setId(3004L);
        sfx.setCanonicalUrl("https://cdn.example.com/presentation-sfx.mp3");
        ContentAsset stepAsset = new ContentAsset();
        stepAsset.setId(3005L);
        stepAsset.setCanonicalUrl("https://cdn.example.com/presentation-video.mp4");

        when(catalogFoundationService.listPublishedRedeemablePrizes()).thenReturn(List.of(prize));
        when(catalogFoundationService.listPublishedStoryLines()).thenReturn(Collections.emptyList());
        when(catalogFoundationService.listPublishedCities()).thenReturn(Collections.emptyList());
        when(catalogFoundationService.listPublishedSubMaps(null)).thenReturn(Collections.emptyList());
        when(catalogFoundationService.listRelationLinks(eq("redeemable_prize"), any(), any())).thenReturn(Collections.emptyList());
        when(catalogFoundationService.getPublishedIndoorBuildingsByIds(any())).thenReturn(Collections.emptyMap());
        when(catalogFoundationService.getPublishedIndoorFloorsByIds(any())).thenReturn(Collections.emptyMap());
        when(catalogFoundationService.getRewardRuleBindings(eq("redeemable_prize"), any()))
                .thenReturn(Map.of(prize.getId(), List.of(ruleBinding)));
        when(catalogFoundationService.getRewardRulesByIds(any())).thenReturn(Map.of(rule.getId(), rule));
        when(catalogFoundationService.getRewardPresentationsByIds(any())).thenReturn(Map.of(presentation.getId(), presentation));
        when(catalogFoundationService.listRewardPresentationSteps(any())).thenReturn(List.of(step));
        when(catalogFoundationService.getPublishedAssetsByIds(any())).thenReturn(Map.of(
                cover.getId(), cover,
                presentationCover.getId(), presentationCover,
                voiceOver.getId(), voiceOver,
                sfx.getId(), sfx,
                stepAsset.getId(), stepAsset
        ));

        List<RedeemablePrizeResponse> responses = service.listRedeemablePrizes("zh-Hant");

        assertThat(responses).hasSize(1);
        RedeemablePrizeResponse response = responses.get(0);
        assertThat(response.getCode()).isEqualTo("prize_fire_archive_box");
        assertThat(response.getRuleSummaries()).singleElement().extracting("code").isEqualTo("rule_fire_route_completion");
        assertThat(response.getPresentation()).isNotNull();
        assertThat(response.getPresentation().getPresentationType()).isEqualTo("fullscreen_video");
        assertThat(response.getPresentation().getSteps()).singleElement().extracting("assetUrl")
                .isEqualTo("https://cdn.example.com/presentation-video.mp4");
        assertThat(response.getAvailableInventory()).isEqualTo(100);
    }

    @Test
    void listGameRewardsSupportsHonorFilterAndPresentationProjection() {
        GameReward reward = new GameReward();
        reward.setId(2101L);
        reward.setCode("honor_macau_archivist");
        reward.setRewardType("title");
        reward.setRarity("legendary");
        reward.setStackable(0);
        reward.setMaxOwned(1);
        reward.setCanEquip(1);
        reward.setCanConsume(0);
        reward.setNameZh("濠江通史典藏家");
        reward.setNameZht("濠江通史典藏家");
        reward.setPresentationId(4101L);

        RewardPresentation presentation = new RewardPresentation();
        presentation.setId(4101L);
        presentation.setCode("presentation_honor_unlock");
        presentation.setNameZh("稱號亮相");
        presentation.setNameZht("稱號亮相");
        presentation.setPresentationType("fullscreen_animation");

        when(catalogFoundationService.listPublishedGameRewards(true)).thenReturn(List.of(reward));
        when(catalogFoundationService.listPublishedStoryLines()).thenReturn(Collections.emptyList());
        when(catalogFoundationService.listPublishedCities()).thenReturn(Collections.emptyList());
        when(catalogFoundationService.listPublishedSubMaps(null)).thenReturn(Collections.emptyList());
        when(catalogFoundationService.listRelationLinks(eq("game_reward"), any(), any())).thenReturn(Collections.emptyList());
        when(catalogFoundationService.getPublishedIndoorBuildingsByIds(any())).thenReturn(Collections.emptyMap());
        when(catalogFoundationService.getPublishedIndoorFloorsByIds(any())).thenReturn(Collections.emptyMap());
        when(catalogFoundationService.getRewardRuleBindings(eq("game_reward"), any())).thenReturn(Collections.emptyMap());
        when(catalogFoundationService.getRewardRulesByIds(any())).thenReturn(Collections.emptyMap());
        when(catalogFoundationService.getRewardPresentationsByIds(any())).thenReturn(Map.of(presentation.getId(), presentation));
        when(catalogFoundationService.listRewardPresentationSteps(any())).thenReturn(Collections.emptyList());
        when(catalogFoundationService.getPublishedAssetsByIds(any())).thenReturn(Collections.emptyMap());

        List<GameRewardResponse> responses = service.listGameRewards("zh-Hant", true);
        RewardPresentationResponse response = responses.get(0).getPresentation();

        assertThat(responses).singleElement().extracting("rewardType").isEqualTo("title");
        assertThat(response).isNotNull();
        assertThat(response.getPresentationType()).isEqualTo("fullscreen_animation");
    }
}
