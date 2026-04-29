package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.response.AdminContentAssetUsageSummaryResponse;
import com.aoxiaoyou.admin.entity.ContentAsset;
import com.aoxiaoyou.admin.entity.ContentAssetLink;
import com.aoxiaoyou.admin.entity.Collectible;
import com.aoxiaoyou.admin.entity.Poi;
import com.aoxiaoyou.admin.entity.StoryLine;
import com.aoxiaoyou.admin.mapper.AppRuntimeSettingMapper;
import com.aoxiaoyou.admin.mapper.BadgeMapper;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.CityMapper;
import com.aoxiaoyou.admin.mapper.CollectibleMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetLinkMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.NotificationMapper;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.RewardMapper;
import com.aoxiaoyou.admin.mapper.StampMapper;
import com.aoxiaoyou.admin.mapper.StoryChapterMapper;
import com.aoxiaoyou.admin.mapper.StoryContentBlockMapper;
import com.aoxiaoyou.admin.mapper.StoryLineMapper;
import com.aoxiaoyou.admin.mapper.SubMapMapper;
import com.aoxiaoyou.admin.mapper.TipArticleMapper;
import com.aoxiaoyou.admin.media.CosAssetStorageService;
import com.aoxiaoyou.admin.media.MediaIntakeService;
import com.aoxiaoyou.admin.service.impl.AdminContentManagementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminContentManagementServiceTest {

    @Mock private AppRuntimeSettingMapper runtimeSettingMapper;
    @Mock private ContentAssetMapper contentAssetMapper;
    @Mock private ContentAssetLinkMapper contentAssetLinkMapper;
    @Mock private TipArticleMapper tipArticleMapper;
    @Mock private NotificationMapper notificationMapper;
    @Mock private StampMapper stampMapper;
    @Mock private CityMapper cityMapper;
    @Mock private SubMapMapper subMapMapper;
    @Mock private PoiMapper poiMapper;
    @Mock private StoryLineMapper storyLineMapper;
    @Mock private StoryChapterMapper storyChapterMapper;
    @Mock private StoryContentBlockMapper storyContentBlockMapper;
    @Mock private RewardMapper rewardMapper;
    @Mock private CollectibleMapper collectibleMapper;
    @Mock private BadgeMapper badgeMapper;
    @Mock private BuildingMapper buildingMapper;
    @Mock private CosAssetStorageService cosAssetStorageService;
    @Mock private MediaIntakeService mediaIntakeService;

    private AdminContentManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminContentManagementServiceImpl(
                runtimeSettingMapper,
                contentAssetMapper,
                contentAssetLinkMapper,
                tipArticleMapper,
                notificationMapper,
                stampMapper,
                cityMapper,
                subMapMapper,
                poiMapper,
                storyLineMapper,
                storyChapterMapper,
                storyContentBlockMapper,
                rewardMapper,
                collectibleMapper,
                badgeMapper,
                buildingMapper,
                cosAssetStorageService,
                mediaIntakeService
        );
    }

    @Test
    void getAssetUsagesIncludesLinkDirectAndUrlReferences() {
        ContentAsset asset = asset(99L, "https://cos.example.com/assets/story-cover.png");
        when(contentAssetMapper.selectById(99L)).thenReturn(asset);

        ContentAssetLink link = new ContentAssetLink();
        link.setEntityType("poi");
        link.setEntityId(22L);
        link.setUsageType("gallery");
        link.setTitleZh("POI gallery");
        link.setStatus("published");
        when(contentAssetLinkMapper.selectList(any())).thenReturn(List.of(link));

        Poi poi = new Poi();
        poi.setId(22L);
        poi.setCode("poi_moments");
        poi.setNameZh("媽閣廟");
        poi.setStatus("published");
        when(poiMapper.selectById(22L)).thenReturn(poi);

        StoryLine storyLine = new StoryLine();
        storyLine.setId(31L);
        storyLine.setCode("fire-route");
        storyLine.setNameZh("濠江烽煙");
        storyLine.setCoverAssetId(99L);
        storyLine.setStatus("draft");
        when(storyLineMapper.selectList(any())).thenReturn(List.of(storyLine));

        Collectible collectible = new Collectible();
        collectible.setId(41L);
        collectible.setCollectibleCode("item_fire_map");
        collectible.setNameZh("戰役地圖");
        collectible.setImageUrl(asset.getCanonicalUrl());
        collectible.setStatus("1");
        when(collectibleMapper.selectList(any())).thenReturn(List.of(collectible));

        AdminContentAssetUsageSummaryResponse response = service.getAssetUsages(99L);

        assertThat(response.getAssetId()).isEqualTo(99L);
        assertThat(response.getUsageCount()).isEqualTo(3);
        assertThat(response.getUsages())
                .extracting(item -> item.getRelationType() + ":" + item.getEntityType() + ":" + item.getFieldName())
                .contains(
                        "link:poi:assetId",
                        "direct-field:storyline:coverAssetId",
                        "url-field:collectible:imageUrl"
                );
    }

    @Test
    void deleteAssetBlocksWhenAssetIsStillReferenced() {
        ContentAsset asset = asset(100L, "https://cos.example.com/assets/badge.png");
        when(contentAssetMapper.selectById(100L)).thenReturn(asset);

        ContentAssetLink link = new ContentAssetLink();
        link.setEntityType("collectible");
        link.setEntityId(77L);
        link.setUsageType("gallery");
        when(contentAssetLinkMapper.selectList(any())).thenReturn(List.of(link));

        assertThatThrownBy(() -> service.deleteAsset(100L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("still in use");

        verify(cosAssetStorageService, never()).deleteAsset(any(), any());
        verify(contentAssetMapper, never()).deleteById(100L);
    }

    private ContentAsset asset(Long id, String canonicalUrl) {
        ContentAsset asset = new ContentAsset();
        asset.setId(id);
        asset.setBucketName("tripofmacau-1301163924");
        asset.setObjectKey("miniapp/assets/image/test.png");
        asset.setCanonicalUrl(canonicalUrl);
        return asset;
    }
}
