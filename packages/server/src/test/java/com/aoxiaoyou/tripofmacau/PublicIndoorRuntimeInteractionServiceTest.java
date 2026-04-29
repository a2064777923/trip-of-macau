package com.aoxiaoyou.tripofmacau;

import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.request.IndoorRuntimeInteractionRequest;
import com.aoxiaoyou.tripofmacau.dto.response.IndoorRuntimeInteractionResponse;
import com.aoxiaoyou.tripofmacau.entity.IndoorBuilding;
import com.aoxiaoyou.tripofmacau.entity.IndoorFloor;
import com.aoxiaoyou.tripofmacau.entity.IndoorNode;
import com.aoxiaoyou.tripofmacau.entity.IndoorNodeBehavior;
import com.aoxiaoyou.tripofmacau.entity.IndoorRuntimeLog;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicIndoorRuntimeInteractionServiceTest {

    @Mock private IndoorBuildingMapper indoorBuildingMapper;
    @Mock private IndoorFloorMapper indoorFloorMapper;
    @Mock private IndoorNodeMapper indoorNodeMapper;
    @Mock private IndoorNodeBehaviorMapper indoorNodeBehaviorMapper;
    @Mock private IndoorRuntimeLogMapper indoorRuntimeLogMapper;
    @Mock private CatalogFoundationService catalogFoundationService;

    @BeforeAll
    static void initializeMybatisPlusLambdaMetadata() {
        initializeTable(com.aoxiaoyou.tripofmacau.entity.IndoorFloor.class);
        initializeTable(com.aoxiaoyou.tripofmacau.entity.IndoorNode.class);
        initializeTable(com.aoxiaoyou.tripofmacau.entity.IndoorNodeBehavior.class);
    }

    @Test
    void evaluateInteractionReturnsAuthRequiredForAnonymousGuardedRuntime() {
        when(indoorFloorMapper.selectOne(any())).thenReturn(floor());
        when(indoorNodeMapper.selectOne(any())).thenReturn(royalNode());
        when(indoorNodeBehaviorMapper.selectOne(any())).thenReturn(royalBehavior());
        when(catalogFoundationService.getPublishedAssetsByIds(any())).thenReturn(Collections.emptyMap());
        doAnswer(invocation -> {
            IndoorRuntimeLog log = invocation.getArgument(0);
            log.setId(91001L);
            return 1;
        }).when(indoorRuntimeLogMapper).insert(any(IndoorRuntimeLog.class));

        PublicIndoorRuntimeServiceImpl service = service();

        IndoorRuntimeInteractionRequest request = new IndoorRuntimeInteractionRequest();
        request.setFloorId(11L);
        request.setNodeId(102L);
        request.setBehaviorId(1002L);
        request.setEventType("dwell");
        request.setDwellMs(10000L);
        request.setClientSessionId("phase17-anonymous");

        IndoorRuntimeInteractionResponse response = service.evaluateInteraction(request, "zh-Hant", null);

        assertThat(response.getInteractionAccepted()).isFalse();
        assertThat(response.getRequiresAuth()).isTrue();
        assertThat(response.getBlockedReason()).isEqualTo("auth_required");
        assertThat(response.getMatchedTriggerId()).isEqualTo("trigger-dwell-echo");
        assertThat(response.getInteractionLogId()).isEqualTo(91001L);

        ArgumentCaptor<IndoorRuntimeLog> captor = ArgumentCaptor.forClass(IndoorRuntimeLog.class);
        verify(indoorRuntimeLogMapper).insert(captor.capture());
        assertThat(captor.getValue().getInteractionAccepted()).isFalse();
        assertThat(captor.getValue().getRequiresAuth()).isTrue();
        assertThat(captor.getValue().getBlockedReason()).isEqualTo("auth_required");
        assertThat(captor.getValue().getEventType()).isEqualTo("dwell");
        assertThat(captor.getValue().getClientSessionId()).isEqualTo("phase17-anonymous");
    }

    @Test
    void evaluateInteractionReturnsEffectsForSupportedZipcityBehavior() {
        when(indoorFloorMapper.selectOne(any())).thenReturn(floor());
        when(indoorNodeMapper.selectOne(any())).thenReturn(zipcityNode());
        when(indoorNodeBehaviorMapper.selectOne(any())).thenReturn(zipcityBehavior());
        when(catalogFoundationService.getPublishedAssetsByIds(any())).thenReturn(Collections.emptyMap());
        doAnswer(invocation -> {
            IndoorRuntimeLog log = invocation.getArgument(0);
            log.setId(92002L);
            return 1;
        }).when(indoorRuntimeLogMapper).insert(any(IndoorRuntimeLog.class));

        PublicIndoorRuntimeServiceImpl service = service();

        IndoorRuntimeInteractionRequest request = new IndoorRuntimeInteractionRequest();
        request.setFloorId(11L);
        request.setNodeId(103L);
        request.setBehaviorId(1003L);
        request.setEventType("tap");
        request.setRelativeX(new BigDecimal("0.104000"));
        request.setRelativeY(new BigDecimal("0.633000"));
        request.setEventTimestamp("2026-04-17T09:35:00");
        request.setClientSessionId("phase17-zipcity");

        IndoorRuntimeInteractionResponse response = service.evaluateInteraction(request, "zh-Hant", 77L);

        assertThat(response.getInteractionAccepted()).isTrue();
        assertThat(response.getRequiresAuth()).isFalse();
        assertThat(response.getBlockedReason()).isNull();
        assertThat(response.getMatchedTriggerId()).isEqualTo("trigger-zipcity-tap");
        assertThat(response.getInteractionLogId()).isEqualTo(92002L);
        assertThat(response.getEffects()).extracting(IndoorRuntimeInteractionResponse.TriggeredEffect::getCategory)
                .containsExactly("path_motion", "bubble");
    }

    @Test
    void evaluateInteractionRejectsUnknownBehavior() {
        when(indoorFloorMapper.selectOne(any())).thenReturn(floor());
        when(indoorNodeMapper.selectOne(any())).thenReturn(zipcityNode());
        when(indoorNodeBehaviorMapper.selectOne(any())).thenReturn(null);

        PublicIndoorRuntimeServiceImpl service = service();

        IndoorRuntimeInteractionRequest request = new IndoorRuntimeInteractionRequest();
        request.setFloorId(11L);
        request.setNodeId(103L);
        request.setBehaviorId(9999L);
        request.setEventType("tap");

        assertThatThrownBy(() -> service.evaluateInteraction(request, "zh-Hant", 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Indoor runtime behavior not found");
    }

    private PublicIndoorRuntimeServiceImpl service() {
        return new PublicIndoorRuntimeServiceImpl(
                indoorBuildingMapper,
                indoorFloorMapper,
                indoorNodeMapper,
                indoorNodeBehaviorMapper,
                indoorRuntimeLogMapper,
                catalogFoundationService,
                new LocalizedContentSupport(new ObjectMapper()),
                new ObjectMapper()
        );
    }

    private static void initializeTable(Class<?> entityClass) {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), entityClass);
        LambdaUtils.installCache(TableInfoHelper.getTableInfo(entityClass));
    }

    private static IndoorFloor floor() {
        IndoorFloor floor = new IndoorFloor();
        floor.setId(11L);
        floor.setBuildingId(5L);
        floor.setFloorCode("1F");
        floor.setFloorNumber(1);
        floor.setFloorNameZh("Lisboa Runtime Floor");
        floor.setFloorNameZht("Lisboa Runtime Floor");
        floor.setStatus("published");
        return floor;
    }

    private static IndoorNode royalNode() {
        IndoorNode node = new IndoorNode();
        node.setId(102L);
        node.setFloorId(11L);
        node.setBuildingId(5L);
        node.setMarkerCode("1f-phase15-royal-palace-dwell");
        node.setNodeType("landmark");
        node.setPresentationMode("marker");
        node.setRuntimeSupportLevel("phase17_supported");
        node.setStatus("published");
        return node;
    }

    private static IndoorNode zipcityNode() {
        IndoorNode node = new IndoorNode();
        node.setId(103L);
        node.setFloorId(11L);
        node.setBuildingId(5L);
        node.setMarkerCode("1f-phase15-zipcity-path");
        node.setNodeType("landmark");
        node.setPresentationMode("hybrid");
        node.setRuntimeSupportLevel("phase17_supported");
        node.setStatus("published");
        node.setOverlayGeometryJson("{\"geometryType\":\"polyline\",\"points\":[{\"x\":0.10,\"y\":0.63,\"order\":0},{\"x\":0.30,\"y\":0.58,\"order\":1}],\"properties\":{\"theme\":\"guide\"}}");
        return node;
    }

    private static IndoorNodeBehavior royalBehavior() {
        IndoorNodeBehavior behavior = new IndoorNodeBehavior();
        behavior.setId(1002L);
        behavior.setNodeId(102L);
        behavior.setBehaviorCode("royal-palace-dwell-reveal");
        behavior.setAppearanceRulesJson("[{\"id\":\"appearance-always\",\"category\":\"always_on\",\"label\":\"Always Visible\",\"config\":{\"note\":\"wait\"}}]");
        behavior.setTriggerRulesJson("[{\"id\":\"trigger-dwell-echo\",\"category\":\"dwell\",\"label\":\"Wait For Echo\",\"config\":{\"seconds\":10}}]");
        behavior.setEffectRulesJson("[{\"id\":\"effect-echo-popup\",\"category\":\"popup\",\"label\":\"Show Echo\",\"config\":{\"title\":\"Echo\",\"body\":\"Stay long enough.\"}},{\"id\":\"effect-echo-collectible\",\"category\":\"collectible_grant\",\"label\":\"Grant Collectible\",\"config\":{\"entityId\":1,\"quantity\":1}}]");
        behavior.setPathGraphJson("{\"points\":[],\"durationMs\":1200,\"holdMs\":400,\"loop\":false,\"easing\":\"ease-in-out\"}");
        behavior.setRuntimeSupportLevel("phase17_supported");
        behavior.setStatus("published");
        return behavior;
    }

    private static IndoorNodeBehavior zipcityBehavior() {
        IndoorNodeBehavior behavior = new IndoorNodeBehavior();
        behavior.setId(1003L);
        behavior.setNodeId(103L);
        behavior.setBehaviorCode("zipcity-guiding-path");
        behavior.setAppearanceRulesJson("[{\"id\":\"appearance-manual\",\"category\":\"manual\",\"label\":\"Tap To Reveal\",\"config\":{\"note\":\"waiting\"}}]");
        behavior.setTriggerRulesJson("[{\"id\":\"trigger-zipcity-tap\",\"category\":\"tap\",\"label\":\"Tap Entrance\",\"config\":{\"targetHint\":\"entry\"}},{\"id\":\"trigger-zipcity-follow\",\"category\":\"proximity\",\"label\":\"Follow Trail\",\"dependsOnTriggerId\":\"trigger-zipcity-tap\",\"config\":{\"radiusMeters\":18}}]");
        behavior.setEffectRulesJson("[{\"id\":\"effect-zipcity-path\",\"category\":\"path_motion\",\"label\":\"Play Trail\",\"config\":{\"trail\":\"zipcity-guiding\"}},{\"id\":\"effect-zipcity-bubble\",\"category\":\"bubble\",\"label\":\"Next Stop\",\"config\":{\"title\":\"Follow\",\"body\":\"The next stop is ahead.\"}}]");
        behavior.setPathGraphJson("{\"points\":[{\"x\":0.10,\"y\":0.63,\"order\":0},{\"x\":0.16,\"y\":0.61,\"order\":1},{\"x\":0.23,\"y\":0.60,\"order\":2},{\"x\":0.30,\"y\":0.58,\"order\":3}],\"durationMs\":3600,\"holdMs\":600,\"loop\":false,\"easing\":\"ease-in-out\"}");
        behavior.setRuntimeSupportLevel("phase17_supported");
        behavior.setStatus("published");
        return behavior;
    }
}
