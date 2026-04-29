package com.aoxiaoyou.tripofmacau;

import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.response.RewardResponse;
import com.aoxiaoyou.tripofmacau.entity.City;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.aoxiaoyou.tripofmacau.entity.ContentRelationLink;
import com.aoxiaoyou.tripofmacau.entity.IndoorBuilding;
import com.aoxiaoyou.tripofmacau.entity.IndoorFloor;
import com.aoxiaoyou.tripofmacau.entity.Reward;
import com.aoxiaoyou.tripofmacau.entity.StoryLine;
import com.aoxiaoyou.tripofmacau.entity.SubMap;
import com.aoxiaoyou.tripofmacau.service.CatalogFoundationService;
import com.aoxiaoyou.tripofmacau.service.RuntimeSettingsService;
import com.aoxiaoyou.tripofmacau.service.impl.PublicCatalogServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicCatalogServiceImplCarryoverTest {

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
    void listRewardsIncludesCarryoverPresetBindingsIndoorRelationsAndAttachmentAssets() {
        Reward reward = new Reward();
        reward.setId(31L);
        reward.setCode("reward_lisboeta_secret_cut");
        reward.setNameZh("Reward ZH");
        reward.setNameEn("Reward EN");
        reward.setNameZht("Reward ZHT");
        reward.setNamePt("Reward PT");
        reward.setSubtitleZht("Subtitle ZHT");
        reward.setDescriptionZht("Description ZHT");
        reward.setHighlightZht("Highlight ZHT");
        reward.setStampCost(36);
        reward.setInventoryTotal(120);
        reward.setInventoryRedeemed(4);
        reward.setCoverAssetId(100L);
        reward.setPopupPresetCode("reward-modal");
        reward.setPopupConfigJson("{\"ctaLabel\":\"Open\"}");
        reward.setDisplayPresetCode("inventory-card");
        reward.setDisplayConfigJson("{\"accent\":\"ruby\"}");
        reward.setTriggerPresetCode("reward-redemption");
        reward.setTriggerConfigJson("{\"consumeStamps\":true}");
        reward.setExampleContentZh("Example ZH");
        reward.setExampleContentEn("Example EN");
        reward.setExampleContentZht("Example ZHT");
        reward.setExampleContentPt("Example PT");
        reward.setSortOrder(31);

        StoryLine storyline = new StoryLine();
        storyline.setId(8L);
        storyline.setCode("macau_fire_route");
        storyline.setNameZh("Storyline ZH");
        storyline.setNameEn("Storyline EN");
        storyline.setNameZht("Storyline ZHT");
        storyline.setNamePt("Storyline PT");

        City city = new City();
        city.setId(1L);
        city.setCode("macau");
        city.setNameZh("Macau ZH");
        city.setNameEn("Macau EN");
        city.setNameZht("Macau ZHT");
        city.setNamePt("Macau PT");

        SubMap subMap = new SubMap();
        subMap.setId(1001L);
        subMap.setCode("macau-peninsula");
        subMap.setNameZh("Peninsula ZH");
        subMap.setNameEn("Peninsula EN");
        subMap.setNameZht("Peninsula ZHT");
        subMap.setNamePt("Peninsula PT");

        IndoorBuilding building = new IndoorBuilding();
        building.setId(5L);
        building.setBuildingCode("lisboeta_demo");
        building.setNameZh("Building ZH");
        building.setNameEn("Building EN");
        building.setNameZht("Building ZHT");
        building.setNamePt("Building PT");

        IndoorFloor floor = new IndoorFloor();
        floor.setId(6L);
        floor.setFloorCode("G");
        floor.setFloorNameZh("Floor ZH");
        floor.setFloorNameEn("Floor EN");
        floor.setFloorNameZht("Floor ZHT");
        floor.setFloorNamePt("Floor PT");

        ContentAsset coverAsset = new ContentAsset();
        coverAsset.setId(100L);
        coverAsset.setCanonicalUrl("https://cdn.example.com/reward-cover.png");

        ContentAsset attachmentAsset = new ContentAsset();
        attachmentAsset.setId(101L);
        attachmentAsset.setCanonicalUrl("https://cdn.example.com/reward-preview.mp4");

        when(catalogFoundationService.listPublishedRewards()).thenReturn(List.of(reward));
        when(catalogFoundationService.listPublishedStoryLines()).thenReturn(List.of(storyline));
        when(catalogFoundationService.listPublishedCities()).thenReturn(List.of(city));
        when(catalogFoundationService.listPublishedSubMaps(null)).thenReturn(List.of(subMap));
        when(catalogFoundationService.listRelationLinks(eq("reward"), any(Collection.class), eq("storyline_binding")))
                .thenReturn(List.of(link("reward", reward.getId(), "storyline_binding", "storyline", storyline.getId(), storyline.getCode())));
        when(catalogFoundationService.listRelationLinks(eq("reward"), any(Collection.class), eq("city_binding")))
                .thenReturn(List.of(link("reward", reward.getId(), "city_binding", "city", city.getId(), city.getCode())));
        when(catalogFoundationService.listRelationLinks(eq("reward"), any(Collection.class), eq("sub_map_binding")))
                .thenReturn(List.of(link("reward", reward.getId(), "sub_map_binding", "sub_map", subMap.getId(), subMap.getCode())));
        when(catalogFoundationService.listRelationLinks(eq("reward"), any(Collection.class), eq("indoor_building_binding")))
                .thenReturn(List.of(link("reward", reward.getId(), "indoor_building_binding", "indoor_building", building.getId(), building.getBuildingCode())));
        when(catalogFoundationService.listRelationLinks(eq("reward"), any(Collection.class), eq("indoor_floor_binding")))
                .thenReturn(List.of(link("reward", reward.getId(), "indoor_floor_binding", "indoor_floor", floor.getId(), floor.getFloorCode())));
        when(catalogFoundationService.listRelationLinks(eq("reward"), any(Collection.class), eq("attachment_asset")))
                .thenReturn(List.of(link("reward", reward.getId(), "attachment_asset", "asset", attachmentAsset.getId(), "asset-101")));
        when(catalogFoundationService.getPublishedIndoorBuildingsByIds(any())).thenReturn(Map.of(building.getId(), building));
        when(catalogFoundationService.getPublishedIndoorFloorsByIds(any())).thenReturn(Map.of(floor.getId(), floor));
        when(catalogFoundationService.getPublishedAssetsByIds(any())).thenReturn(Map.of(
                coverAsset.getId(), coverAsset,
                attachmentAsset.getId(), attachmentAsset
        ));

        List<RewardResponse> responses = service.listRewards("zh-Hant");

        assertThat(responses).hasSize(1);
        RewardResponse response = responses.get(0);
        assertThat(response.getCode()).isEqualTo("reward_lisboeta_secret_cut");
        assertThat(response.getName()).isEqualTo("Reward ZHT");
        assertThat(response.getPopupPresetCode()).isEqualTo("reward-modal");
        assertThat(response.getDisplayPresetCode()).isEqualTo("inventory-card");
        assertThat(response.getTriggerPresetCode()).isEqualTo("reward-redemption");
        assertThat(response.getExampleContent()).isEqualTo("Example ZHT");
        assertThat(response.getAvailableInventory()).isEqualTo(116);
        assertThat(response.getRelatedIndoorBuildings()).singleElement().extracting("code").isEqualTo("lisboeta_demo");
        assertThat(response.getRelatedIndoorFloors()).singleElement().extracting("code").isEqualTo("G");
        assertThat(response.getAttachmentAssetUrls()).containsExactly("https://cdn.example.com/reward-preview.mp4");
        assertThat(response.getCoverImageUrl()).isEqualTo("https://cdn.example.com/reward-cover.png");
    }

    private ContentRelationLink link(String ownerType, Long ownerId, String relationType, String targetType, Long targetId, String targetCode) {
        ContentRelationLink link = new ContentRelationLink();
        link.setOwnerType(ownerType);
        link.setOwnerId(ownerId);
        link.setRelationType(relationType);
        link.setTargetType(targetType);
        link.setTargetId(targetId);
        link.setTargetCode(targetCode);
        return link;
    }
}
