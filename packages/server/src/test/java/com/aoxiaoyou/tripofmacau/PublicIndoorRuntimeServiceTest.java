package com.aoxiaoyou.tripofmacau;

import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorRuntimeFloorResponse;
import com.aoxiaoyou.tripofmacau.entity.ContentAsset;
import com.aoxiaoyou.tripofmacau.entity.IndoorBuilding;
import com.aoxiaoyou.tripofmacau.entity.IndoorFloor;
import com.aoxiaoyou.tripofmacau.entity.IndoorNode;
import com.aoxiaoyou.tripofmacau.entity.IndoorNodeBehavior;
import com.aoxiaoyou.tripofmacau.mapper.IndoorBuildingMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorFloorMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorNodeBehaviorMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorNodeMapper;
import com.aoxiaoyou.tripofmacau.mapper.IndoorRuntimeLogMapper;
import com.aoxiaoyou.tripofmacau.service.CatalogFoundationService;
import com.aoxiaoyou.tripofmacau.service.impl.PublicIndoorRuntimeServiceImpl;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicIndoorRuntimeServiceTest {

    @Mock private IndoorBuildingMapper indoorBuildingMapper;
    @Mock private IndoorFloorMapper indoorFloorMapper;
    @Mock private IndoorNodeMapper indoorNodeMapper;
    @Mock private IndoorNodeBehaviorMapper indoorNodeBehaviorMapper;
    @Mock private IndoorRuntimeLogMapper indoorRuntimeLogMapper;
    @Mock private CatalogFoundationService catalogFoundationService;

    @BeforeAll
    static void initializeMybatisPlusLambdaMetadata() {
        initializeTable(com.aoxiaoyou.tripofmacau.entity.IndoorBuilding.class);
        initializeTable(com.aoxiaoyou.tripofmacau.entity.IndoorFloor.class);
        initializeTable(com.aoxiaoyou.tripofmacau.entity.IndoorNode.class);
        initializeTable(com.aoxiaoyou.tripofmacau.entity.IndoorNodeBehavior.class);
    }

    @Test
    void getFloorRuntimeProjectsDeterministicLisboaBehaviorSnapshot() {
        IndoorFloor floor = floor();
        IndoorBuilding building = building();
        List<IndoorNode> nodes = List.of(
                node(101L, "1f-phase15-night-market-overlay", "overlay", "polygon", 7001L),
                node(102L, "1f-phase15-royal-palace-dwell", "marker", null, 7002L),
                node(103L, "1f-phase15-zipcity-path", "hybrid", "polyline", 7003L)
        );
        List<IndoorNodeBehavior> behaviors = List.of(
                supportedNightMarketBehavior(101L),
                guardedRoyalPalaceBehavior(102L),
                supportedZipcityBehavior(103L)
        );

        when(indoorFloorMapper.selectOne(any())).thenReturn(floor);
        when(indoorBuildingMapper.selectOne(any())).thenReturn(building);
        when(indoorNodeMapper.selectList(any())).thenReturn(nodes);
        when(indoorNodeBehaviorMapper.selectList(any())).thenReturn(behaviors);
        when(catalogFoundationService.getPublishedAssetsByIds(any())).thenAnswer(invocation -> assetsFor(invocation.getArgument(0)));

        PublicIndoorRuntimeServiceImpl service = new PublicIndoorRuntimeServiceImpl(
                indoorBuildingMapper,
                indoorFloorMapper,
                indoorNodeMapper,
                indoorNodeBehaviorMapper,
                indoorRuntimeLogMapper,
                catalogFoundationService,
                new LocalizedContentSupport(new ObjectMapper()),
                new ObjectMapper()
        );

        IndoorRuntimeFloorResponse response = service.getFloorRuntime(11L, "zh-Hant");

        assertThat(response.getRuntimeVersion()).startsWith("phase17-11-3-3-");
        assertThat(response.getCoverImageUrl()).isEqualTo("https://cdn.tripofmacau.test/assets/3001.png");
        assertThat(response.getFloorPlanUrl()).isEqualTo("https://cdn.tripofmacau.test/assets/3002.png");
        assertThat(response.getNodes()).hasSize(3);

        Map<String, IndoorRuntimeFloorResponse.Behavior> behaviorsByCode = response.getNodes().stream()
                .flatMap(node -> node.getBehaviors().stream())
                .collect(LinkedHashMap::new, (map, behavior) -> map.put(behavior.getBehaviorCode(), behavior), Map::putAll);

        assertThat(behaviorsByCode).containsKeys(
                "night-market-schedule-overlay",
                "royal-palace-dwell-reveal",
                "zipcity-guiding-path"
        );

        IndoorRuntimeFloorResponse.Behavior nightMarket = behaviorsByCode.get("night-market-schedule-overlay");
        assertThat(nightMarket.getSupported()).isTrue();
        assertThat(nightMarket.getRequiresAuth()).isFalse();
        assertThat(nightMarket.getAppearanceRules()).extracting(IndoorRuntimeFloorResponse.RuleCondition::getCategory)
                .containsExactly("schedule_window");
        assertThat(nightMarket.getTriggerRules()).extracting(IndoorRuntimeFloorResponse.TriggerRule::getCategory)
                .containsExactly("proximity");
        assertThat(nightMarket.getEffectRules()).extracting(IndoorRuntimeFloorResponse.EffectRule::getCategory)
                .containsExactly("popup");

        IndoorRuntimeFloorResponse.Behavior royalPalace = behaviorsByCode.get("royal-palace-dwell-reveal");
        assertThat(royalPalace.getSupported()).isFalse();
        assertThat(royalPalace.getRequiresAuth()).isTrue();
        assertThat(royalPalace.getBlockedReason()).isEqualTo("unsupported_effect_category");
        assertThat(royalPalace.getTriggerRules()).extracting(IndoorRuntimeFloorResponse.TriggerRule::getCategory)
                .containsExactly("dwell");

        IndoorRuntimeFloorResponse.Behavior zipcity = behaviorsByCode.get("zipcity-guiding-path");
        assertThat(zipcity.getSupported()).isTrue();
        assertThat(zipcity.getEffectRules()).extracting(IndoorRuntimeFloorResponse.EffectRule::getCategory)
                .containsExactly("path_motion", "bubble");
        assertThat(zipcity.getPathGraph()).isNotNull();
        assertThat(zipcity.getPathGraph().getPoints()).hasSize(4);
    }

    private static void initializeTable(Class<?> entityClass) {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), entityClass);
        LambdaUtils.installCache(TableInfoHelper.getTableInfo(entityClass));
    }

    private static IndoorBuilding building() {
        IndoorBuilding building = new IndoorBuilding();
        building.setId(5L);
        building.setBuildingCode("lisboeta_macau");
        building.setStatus("published");
        return building;
    }

    private static IndoorFloor floor() {
        IndoorFloor floor = new IndoorFloor();
        floor.setId(11L);
        floor.setBuildingId(5L);
        floor.setFloorCode("1F");
        floor.setFloorNumber(1);
        floor.setFloorNameZh("Lisboa Runtime Floor");
        floor.setFloorNameZht("Lisboa Runtime Floor");
        floor.setDescriptionZh("Deterministic Phase 17 showcase floor.");
        floor.setDescriptionZht("Deterministic Phase 17 showcase floor.");
        floor.setCoverAssetId(3001L);
        floor.setFloorPlanAssetId(3002L);
        floor.setTileRootUrl("https://cdn.tripofmacau.test/tiles/lisboa/1f");
        floor.setTileManifestJson("{\"defaultLevel\":0,\"gridCols\":2,\"gridRows\":2,\"tiles\":[{\"z\":0,\"x\":0,\"y\":0,\"url\":\"https://cdn.tripofmacau.test/tiles/lisboa/1f/0_0.png\"}]}");
        floor.setTilePreviewImageUrl("https://cdn.tripofmacau.test/tiles/lisboa/1f/preview.png");
        floor.setTileSourceType("cos");
        floor.setZoomMin(new BigDecimal("0.60"));
        floor.setZoomMax(new BigDecimal("3.20"));
        floor.setDefaultZoom(new BigDecimal("1.00"));
        floor.setStatus("published");
        floor.setUpdatedAt(LocalDateTime.of(2026, 4, 17, 9, 30));
        return floor;
    }

    private static IndoorNode node(Long id, String markerCode, String presentationMode, String overlayType, Long iconAssetId) {
        IndoorNode node = new IndoorNode();
        node.setId(id);
        node.setFloorId(11L);
        node.setBuildingId(5L);
        node.setMarkerCode(markerCode);
        node.setNodeType("landmark");
        node.setPresentationMode(presentationMode);
        node.setOverlayType(overlayType);
        node.setNodeNameZh(markerCode);
        node.setNodeNameZht(markerCode);
        node.setDescriptionZh(markerCode + "-description");
        node.setDescriptionZht(markerCode + "-description");
        node.setRelativeX(new BigDecimal("0.250000"));
        node.setRelativeY(new BigDecimal("0.650000"));
        node.setIconAssetId(iconAssetId);
        node.setRuntimeSupportLevel("phase17_supported");
        node.setSortOrder(Math.toIntExact(id));
        node.setStatus("published");
        if ("1f-phase15-night-market-overlay".equals(markerCode)) {
            node.setOverlayGeometryJson("{\"geometryType\":\"polygon\",\"points\":[{\"x\":0.17,\"y\":0.58,\"order\":0},{\"x\":0.32,\"y\":0.58,\"order\":1}],\"properties\":{\"theme\":\"night\"}}");
        }
        if ("1f-phase15-zipcity-path".equals(markerCode)) {
            node.setOverlayGeometryJson("{\"geometryType\":\"polyline\",\"points\":[{\"x\":0.10,\"y\":0.63,\"order\":0},{\"x\":0.30,\"y\":0.58,\"order\":1}],\"properties\":{\"theme\":\"guide\"}}");
        }
        return node;
    }

    private static IndoorNodeBehavior supportedNightMarketBehavior(Long nodeId) {
        IndoorNodeBehavior behavior = new IndoorNodeBehavior();
        behavior.setId(1001L);
        behavior.setNodeId(nodeId);
        behavior.setBehaviorCode("night-market-schedule-overlay");
        behavior.setBehaviorNameZh("Night Market Opening Veil");
        behavior.setBehaviorNameZht("Night Market Opening Veil");
        behavior.setAppearanceRulesJson("[{\"id\":\"appearance-night\",\"category\":\"schedule_window\",\"label\":\"Night Window\",\"config\":{\"startAt\":\"19:00\",\"endAt\":\"23:30\"}}]");
        behavior.setTriggerRulesJson("[{\"id\":\"trigger-night-near\",\"category\":\"proximity\",\"label\":\"Approach Night Market\",\"config\":{\"radiusMeters\":35}}]");
        behavior.setEffectRulesJson("[{\"id\":\"effect-night-popup\",\"category\":\"popup\",\"label\":\"Show Night Story\",\"config\":{\"title\":\"Night Market\",\"body\":\"Evening runtime story.\"}}]");
        behavior.setPathGraphJson("{\"points\":[{\"x\":0.17,\"y\":0.58,\"order\":0},{\"x\":0.32,\"y\":0.68,\"order\":1}],\"durationMs\":2400,\"holdMs\":0,\"loop\":false,\"easing\":\"linear\"}");
        behavior.setRuntimeSupportLevel("phase17_supported");
        behavior.setSortOrder(1);
        behavior.setStatus("published");
        return behavior;
    }

    private static IndoorNodeBehavior guardedRoyalPalaceBehavior(Long nodeId) {
        IndoorNodeBehavior behavior = new IndoorNodeBehavior();
        behavior.setId(1002L);
        behavior.setNodeId(nodeId);
        behavior.setBehaviorCode("royal-palace-dwell-reveal");
        behavior.setBehaviorNameZh("Royal Palace Echo");
        behavior.setBehaviorNameZht("Royal Palace Echo");
        behavior.setAppearanceRulesJson("[{\"id\":\"appearance-always\",\"category\":\"always_on\",\"label\":\"Always Visible\",\"config\":{\"note\":\"wait\"}}]");
        behavior.setTriggerRulesJson("[{\"id\":\"trigger-dwell-echo\",\"category\":\"dwell\",\"label\":\"Wait For Echo\",\"config\":{\"seconds\":10}}]");
        behavior.setEffectRulesJson("[{\"id\":\"effect-echo-popup\",\"category\":\"popup\",\"label\":\"Show Echo\",\"config\":{\"title\":\"Echo\",\"body\":\"Stay long enough.\"}},{\"id\":\"effect-echo-collectible\",\"category\":\"collectible_grant\",\"label\":\"Grant Collectible\",\"config\":{\"entityId\":1,\"quantity\":1}}]");
        behavior.setPathGraphJson("{\"points\":[],\"durationMs\":1200,\"holdMs\":400,\"loop\":false,\"easing\":\"ease-in-out\"}");
        behavior.setRuntimeSupportLevel("phase17_supported");
        behavior.setSortOrder(2);
        behavior.setStatus("published");
        return behavior;
    }

    private static IndoorNodeBehavior supportedZipcityBehavior(Long nodeId) {
        IndoorNodeBehavior behavior = new IndoorNodeBehavior();
        behavior.setId(1003L);
        behavior.setNodeId(nodeId);
        behavior.setBehaviorCode("zipcity-guiding-path");
        behavior.setBehaviorNameZh("Zipcity Guiding Trail");
        behavior.setBehaviorNameZht("Zipcity Guiding Trail");
        behavior.setAppearanceRulesJson("[{\"id\":\"appearance-manual\",\"category\":\"manual\",\"label\":\"Tap To Reveal\",\"config\":{\"note\":\"waiting\"}}]");
        behavior.setTriggerRulesJson("[{\"id\":\"trigger-zipcity-tap\",\"category\":\"tap\",\"label\":\"Tap Entrance\",\"config\":{\"targetHint\":\"entry\"}},{\"id\":\"trigger-zipcity-follow\",\"category\":\"proximity\",\"label\":\"Follow Trail\",\"dependsOnTriggerId\":\"trigger-zipcity-tap\",\"config\":{\"radiusMeters\":18}}]");
        behavior.setEffectRulesJson("[{\"id\":\"effect-zipcity-path\",\"category\":\"path_motion\",\"label\":\"Play Trail\",\"config\":{\"trail\":\"zipcity-guiding\"}},{\"id\":\"effect-zipcity-bubble\",\"category\":\"bubble\",\"label\":\"Next Stop\",\"config\":{\"title\":\"Follow\",\"body\":\"The next stop is ahead.\"}}]");
        behavior.setPathGraphJson("{\"points\":[{\"x\":0.10,\"y\":0.63,\"order\":0},{\"x\":0.16,\"y\":0.61,\"order\":1},{\"x\":0.23,\"y\":0.60,\"order\":2},{\"x\":0.30,\"y\":0.58,\"order\":3}],\"durationMs\":3600,\"holdMs\":600,\"loop\":false,\"easing\":\"ease-in-out\"}");
        behavior.setRuntimeSupportLevel("phase17_supported");
        behavior.setSortOrder(3);
        behavior.setStatus("published");
        return behavior;
    }

    private static Map<Long, ContentAsset> assetsFor(Collection<Long> ids) {
        Map<Long, ContentAsset> assets = new LinkedHashMap<>();
        for (Long id : ids) {
            ContentAsset asset = new ContentAsset();
            asset.setId(id);
            asset.setCanonicalUrl("https://cdn.tripofmacau.test/assets/" + id + ".png");
            assets.put(id, asset);
        }
        return assets;
    }
}
