package com.aoxiaoyou.tripofmacau;

import com.aoxiaoyou.tripofmacau.mapper.ActivityMapper;
import com.aoxiaoyou.tripofmacau.mapper.BadgeMapper;
import com.aoxiaoyou.tripofmacau.mapper.CityMapper;
import com.aoxiaoyou.tripofmacau.mapper.ContentAssetMapper;
import com.aoxiaoyou.tripofmacau.mapper.ContentRelationLinkMapper;
import com.aoxiaoyou.tripofmacau.mapper.CollectibleMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorBuildingMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorFloorMapper;
import com.aoxiaoyou.tripofmacau.mapper.NotificationMapper;
import com.aoxiaoyou.tripofmacau.mapper.PoiMapper;
import com.aoxiaoyou.tripofmacau.mapper.RedeemablePrizeMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardPresentationMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardPresentationStepMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardRuleBindingMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardRuleMapper;
import com.aoxiaoyou.tripofmacau.mapper.RewardMapper;
import com.aoxiaoyou.tripofmacau.mapper.StampMapper;
import com.aoxiaoyou.tripofmacau.mapper.StoryChapterMapper;
import com.aoxiaoyou.tripofmacau.mapper.StoryLineMapper;
import com.aoxiaoyou.tripofmacau.mapper.SubMapMapper;
import com.aoxiaoyou.tripofmacau.mapper.TipArticleMapper;
import com.aoxiaoyou.tripofmacau.mapper.GameRewardMapper;
import com.aoxiaoyou.tripofmacau.service.impl.CatalogFoundationServiceImpl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogFoundationServiceImplTest {

    @Mock private CityMapper cityMapper;
    @Mock private SubMapMapper subMapMapper;
    @Mock private PoiMapper poiMapper;
    @Mock private ActivityMapper activityMapper;
    @Mock private CollectibleMapper collectibleMapper;
    @Mock private BadgeMapper badgeMapper;
    @Mock private IndoorBuildingMapper indoorBuildingMapper;
    @Mock private IndoorFloorMapper indoorFloorMapper;
    @Mock private StoryLineMapper storyLineMapper;
    @Mock private StoryChapterMapper storyChapterMapper;
    @Mock private ContentRelationLinkMapper contentRelationLinkMapper;
    @Mock private TipArticleMapper tipArticleMapper;
    @Mock private RewardMapper rewardMapper;
    @Mock private RedeemablePrizeMapper redeemablePrizeMapper;
    @Mock private GameRewardMapper gameRewardMapper;
    @Mock private RewardRuleMapper rewardRuleMapper;
    @Mock private RewardRuleBindingMapper rewardRuleBindingMapper;
    @Mock private RewardPresentationMapper rewardPresentationMapper;
    @Mock private RewardPresentationStepMapper rewardPresentationStepMapper;
    @Mock private StampMapper stampMapper;
    @Mock private NotificationMapper notificationMapper;
    @Mock private ContentAssetMapper contentAssetMapper;

    @BeforeAll
    static void initializeMybatisPlusLambdaMetadata() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), com.aoxiaoyou.tripofmacau.entity.Poi.class);
        LambdaUtils.installCache(TableInfoHelper.getTableInfo(com.aoxiaoyou.tripofmacau.entity.Poi.class));
    }

    @Test
    void listPublishedPoisBuildsPublishedAndSubMapFilters() {
        when(poiMapper.selectList(any())).thenReturn(List.of());

        CatalogFoundationServiceImpl service = new CatalogFoundationServiceImpl(
                cityMapper,
                subMapMapper,
                poiMapper,
                activityMapper,
                collectibleMapper,
                badgeMapper,
                indoorBuildingMapper,
                indoorFloorMapper,
                storyLineMapper,
                storyChapterMapper,
                contentRelationLinkMapper,
                tipArticleMapper,
                rewardMapper,
                redeemablePrizeMapper,
                gameRewardMapper,
                rewardRuleMapper,
                rewardRuleBindingMapper,
                rewardPresentationMapper,
                rewardPresentationStepMapper,
                stampMapper,
                notificationMapper,
                contentAssetMapper
        );

        service.listPublishedPois(1L, 2L, 3L, "harbor");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaQueryWrapper<com.aoxiaoyou.tripofmacau.entity.Poi>> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(poiMapper).selectList(captor.capture());
        assertThat(captor.getValue().getSqlSegment()).contains("sub_map_id").contains("status").contains("storyline_id");
    }
}
