package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.request.AdminIndoorMarkerUpsertRequest;
import com.aoxiaoyou.admin.dto.request.AdminIndoorNodeBehaviorPayload;
import com.aoxiaoyou.admin.dto.request.AdminIndoorNodeUpsertRequest;
import com.aoxiaoyou.admin.dto.response.AdminIndoorMarkerResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorNodeResponse;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.entity.IndoorFloor;
import com.aoxiaoyou.admin.entity.IndoorNode;
import com.aoxiaoyou.admin.entity.IndoorNodeBehavior;
import com.aoxiaoyou.admin.entity.RewardRule;
import com.aoxiaoyou.admin.entity.RewardRuleBinding;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.ContentAssetMapper;
import com.aoxiaoyou.admin.mapper.IndoorFloorMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeBehaviorMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeImportBatchMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeMapper;
import com.aoxiaoyou.admin.mapper.PoiMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleBindingMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleMapper;
import com.aoxiaoyou.admin.service.impl.IndoorMarkerAuthoringService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndoorRuleAuthoringServiceTest {

    @Mock
    private IndoorFloorMapper indoorFloorMapper;
    @Mock
    private IndoorNodeMapper indoorNodeMapper;
    @Mock
    private IndoorNodeBehaviorMapper indoorNodeBehaviorMapper;
    @Mock
    private IndoorNodeImportBatchMapper indoorNodeImportBatchMapper;
    @Mock
    private BuildingMapper buildingMapper;
    @Mock
    private PoiMapper poiMapper;
    @Mock
    private ContentAssetMapper contentAssetMapper;
    @Mock
    private RewardRuleMapper rewardRuleMapper;
    @Mock
    private RewardRuleBindingMapper rewardRuleBindingMapper;

    private IndoorMarkerAuthoringService service;

    @BeforeEach
    void setUp() {
        service = new IndoorMarkerAuthoringService(
                indoorFloorMapper,
                indoorNodeMapper,
                indoorNodeBehaviorMapper,
                indoorNodeImportBatchMapper,
                buildingMapper,
                poiMapper,
                contentAssetMapper,
                rewardRuleMapper,
                rewardRuleBindingMapper,
                new ObjectMapper()
        );
    }

    @Test
    void createNodeRejectsInvalidTriggerPrerequisiteChains() {
        mockFloorAndBuilding();

        AdminIndoorNodeUpsertRequest request = baseNodeRequest();
        AdminIndoorNodeBehaviorPayload behavior = new AdminIndoorNodeBehaviorPayload();
        behavior.setBehaviorCode("chain-invalid");
        behavior.setBehaviorNameZh("失效鏈");

        AdminIndoorNodeBehaviorPayload.TriggerStep first = new AdminIndoorNodeBehaviorPayload.TriggerStep();
        first.setId("tap-start");
        first.setCategory("tap");
        AdminIndoorNodeBehaviorPayload.TriggerStep second = new AdminIndoorNodeBehaviorPayload.TriggerStep();
        second.setId("tap-finish");
        second.setCategory("custom");
        second.setDependsOnTriggerId("missing-step");
        behavior.setTriggerRules(List.of(first, second));

        AdminIndoorNodeBehaviorPayload.EffectDefinition popup = new AdminIndoorNodeBehaviorPayload.EffectDefinition();
        popup.setId("popup-1");
        popup.setCategory("popup");
        behavior.setEffectRules(List.of(popup));
        request.setBehaviors(List.of(behavior));

        assertThatThrownBy(() -> service.createNode(10L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("dependsOnTriggerId references a missing trigger id");
    }

    @Test
    void createNodePersistsOverlayGeometryAndStructuredBehaviors() {
        mockFloorAndBuilding();
        AtomicReference<IndoorNode> storedNode = new AtomicReference<>();
        List<IndoorNodeBehavior> storedBehaviors = new ArrayList<>();
        List<RewardRuleBinding> storedBindings = new ArrayList<>();

        when(indoorNodeMapper.insert(any())).thenAnswer(invocation -> {
            IndoorNode node = invocation.getArgument(0);
            node.setId(101L);
            storedNode.set(node);
            return 1;
        });
        when(indoorNodeMapper.selectById(101L)).thenAnswer(invocation -> storedNode.get());
        when(indoorNodeBehaviorMapper.insert(any())).thenAnswer(invocation -> {
            IndoorNodeBehavior behavior = invocation.getArgument(0);
            behavior.setId(500L + storedBehaviors.size());
            storedBehaviors.add(behavior);
            return 1;
        });
        when(indoorNodeBehaviorMapper.delete(any())).thenAnswer(invocation -> {
            storedBehaviors.clear();
            return 1;
        });
        when(indoorNodeBehaviorMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(storedBehaviors));
        when(rewardRuleBindingMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(storedBindings));
        when(rewardRuleBindingMapper.insert(any())).thenAnswer(invocation -> {
            RewardRuleBinding binding = invocation.getArgument(0);
            binding.setId(7000L + storedBindings.size());
            storedBindings.add(binding);
            return 1;
        });
        RewardRule rewardRule = new RewardRule();
        rewardRule.setId(7001L);
        rewardRule.setCode("night-activation");
        when(rewardRuleMapper.selectBatchIds(any())).thenReturn(List.of(rewardRule));

        AdminIndoorNodeUpsertRequest request = baseNodeRequest();
        request.setPresentationMode("overlay");
        request.setOverlayType("polygon");
        request.setRelativeX(null);
        request.setRelativeY(null);

        AdminIndoorNodeBehaviorPayload.OverlayGeometry geometry = new AdminIndoorNodeBehaviorPayload.OverlayGeometry();
        geometry.setGeometryType("polygon");
        geometry.setPoints(List.of(
                point("0.100000", "0.200000", 0),
                point("0.250000", "0.200000", 1),
                point("0.250000", "0.400000", 2)
        ));
        request.setOverlayGeometry(geometry);

        AdminIndoorNodeBehaviorPayload behavior = new AdminIndoorNodeBehaviorPayload();
        behavior.setBehaviorCode("overlay-schedule");
        behavior.setBehaviorNameZh("夜間禮遇");
        behavior.setAppearancePresetCode("schedule_window");
        AdminIndoorNodeBehaviorPayload.RuleCondition appearance = new AdminIndoorNodeBehaviorPayload.RuleCondition();
        appearance.setId("appearance-1");
        appearance.setCategory("schedule_window");
        behavior.setAppearanceRules(List.of(appearance));
        AdminIndoorNodeBehaviorPayload.EffectDefinition effect = new AdminIndoorNodeBehaviorPayload.EffectDefinition();
        effect.setId("effect-1");
        effect.setCategory("popup");
        behavior.setEffectRules(List.of(effect));
        behavior.setRewardRuleIds(List.of(7001L));
        request.setBehaviors(List.of(behavior));

        AdminIndoorNodeResponse response = service.createNode(10L, request);

        assertThat(response.getPresentationMode()).isEqualTo("overlay");
        assertThat(response.getOverlayType()).isEqualTo("polygon");
        assertThat(response.getOverlayGeometry()).isNotNull();
        assertThat(response.getOverlayGeometry().getPoints()).hasSize(3);
        assertThat(response.getRelativeX()).isEqualByComparingTo("0.200000");
        assertThat(response.getRelativeY()).isEqualByComparingTo("0.266667");
        assertThat(response.getBehaviors()).hasSize(1);
        assertThat(response.getBehaviors().get(0).getAppearanceRules()).hasSize(1);
        assertThat(response.getBehaviors().get(0).getAppearanceRules().get(0).getCategory()).isEqualTo("schedule_window");
        assertThat(response.getBehaviors().get(0).getRewardRuleIds()).containsExactly(7001L);
        assertThat(storedNode.get().getOverlayGeometryJson()).contains("polygon");
        assertThat(storedBehaviors).hasSize(1);
        assertThat(storedBehaviors.get(0).getAppearanceRulesJson()).contains("schedule_window");
        assertThat(storedBindings).hasSize(1);
        assertThat(storedBindings.get(0).getOwnerDomain()).isEqualTo("indoor_behavior");
        assertThat(storedBindings.get(0).getOwnerCode()).isEqualTo("overlay-schedule");
    }

    @Test
    void updateNodeClearsIndoorRewardRuleBindingsWhenBehaviorUnbindsRules() {
        mockFloorAndBuilding();
        IndoorNode existingNode = new IndoorNode();
        existingNode.setId(303L);
        existingNode.setBuildingId(20L);
        existingNode.setFloorId(10L);
        existingNode.setMarkerCode("node-alpha");
        AtomicReference<IndoorNode> storedNode = new AtomicReference<>(existingNode);
        List<IndoorNodeBehavior> storedBehaviors = new ArrayList<>();
        List<RewardRuleBinding> storedBindings = new ArrayList<>();

        IndoorNodeBehavior existingBehavior = new IndoorNodeBehavior();
        existingBehavior.setId(880L);
        existingBehavior.setNodeId(303L);
        existingBehavior.setBehaviorCode("guided-tour");
        storedBehaviors.add(existingBehavior);
        RewardRuleBinding existingBinding = new RewardRuleBinding();
        existingBinding.setId(990L);
        existingBinding.setOwnerDomain("indoor_behavior");
        existingBinding.setOwnerId(880L);
        existingBinding.setOwnerCode("guided-tour");
        existingBinding.setRuleId(7001L);
        storedBindings.add(existingBinding);

        when(indoorNodeMapper.selectById(303L)).thenAnswer(invocation -> storedNode.get());
        when(indoorNodeMapper.updateById(any())).thenAnswer(invocation -> {
            IndoorNode node = invocation.getArgument(0);
            storedNode.set(node);
            return 1;
        });
        when(indoorNodeBehaviorMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(storedBehaviors));
        when(indoorNodeBehaviorMapper.delete(any())).thenAnswer(invocation -> {
            storedBehaviors.clear();
            return 1;
        });
        when(indoorNodeBehaviorMapper.insert(any())).thenAnswer(invocation -> {
            IndoorNodeBehavior behavior = invocation.getArgument(0);
            behavior.setId(1200L + storedBehaviors.size());
            storedBehaviors.add(behavior);
            return 1;
        });
        when(rewardRuleBindingMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(storedBindings));
        when(rewardRuleBindingMapper.delete(any())).thenAnswer(invocation -> {
            storedBindings.clear();
            return 1;
        });
        AdminIndoorNodeUpsertRequest request = baseNodeRequest();
        request.setBehaviors(List.of(buildBehaviorWithoutRewardRule()));

        AdminIndoorNodeResponse response = service.updateNode(303L, request);

        assertThat(response.getBehaviors()).hasSize(1);
        assertThat(response.getBehaviors().get(0).getRewardRuleIds()).isEmpty();
        assertThat(storedBindings).isEmpty();
    }

    @Test
    void createMarkerKeepsPhase12CompatibilityWithoutBehaviorProfiles() {
        mockFloorAndBuilding();
        AtomicReference<IndoorNode> storedNode = new AtomicReference<>();

        when(indoorNodeMapper.insert(any())).thenAnswer(invocation -> {
            IndoorNode node = invocation.getArgument(0);
            node.setId(202L);
            storedNode.set(node);
            return 1;
        });
        when(indoorNodeMapper.selectById(202L)).thenAnswer(invocation -> storedNode.get());
        when(indoorNodeBehaviorMapper.selectList(any())).thenReturn(List.of());

        AdminIndoorMarkerUpsertRequest request = new AdminIndoorMarkerUpsertRequest();
        request.setMarkerCode("legacy-poi");
        request.setNodeType("poi");
        request.setNodeNameZh("舊制入口點");
        request.setRelativeX(new BigDecimal("0.333333"));
        request.setRelativeY(new BigDecimal("0.777777"));
        request.setStatus("published");

        AdminIndoorMarkerResponse response = service.createMarker(10L, request);

        assertThat(response.getMarkerCode()).isEqualTo("legacy-poi");
        assertThat(response.getNodeType()).isEqualTo("poi");
        assertThat(response.getRelativeX()).isEqualByComparingTo("0.333333");
        assertThat(response.getRelativeY()).isEqualByComparingTo("0.777777");
        assertThat(storedNode.get().getPresentationMode()).isEqualTo("marker");
        assertThat(storedNode.get().getRuntimeSupportLevel()).isEqualTo("phase15_storage_only");
        verify(indoorNodeBehaviorMapper, never()).insert(any());
    }

    private void mockFloorAndBuilding() {
        IndoorFloor floor = new IndoorFloor();
        floor.setId(10L);
        floor.setBuildingId(20L);
        Building building = new Building();
        building.setId(20L);
        building.setCityId(30L);

        when(indoorFloorMapper.selectById(10L)).thenReturn(floor);
        when(buildingMapper.selectById(20L)).thenReturn(building);
        when(indoorNodeMapper.selectOne(any())).thenReturn(null);
    }

    private AdminIndoorNodeUpsertRequest baseNodeRequest() {
        AdminIndoorNodeUpsertRequest request = new AdminIndoorNodeUpsertRequest();
        request.setMarkerCode("node-alpha");
        request.setNodeType("custom");
        request.setNodeNameZh("室內節點");
        request.setRelativeX(new BigDecimal("0.500000"));
        request.setRelativeY(new BigDecimal("0.500000"));
        request.setRuntimeSupportLevel("phase15_storage_only");
        return request;
    }

    private AdminIndoorNodeBehaviorPayload buildBehaviorWithoutRewardRule() {
        AdminIndoorNodeBehaviorPayload behavior = new AdminIndoorNodeBehaviorPayload();
        behavior.setBehaviorCode("guided-tour");
        behavior.setBehaviorNameZh("室內導覽");
        AdminIndoorNodeBehaviorPayload.EffectDefinition effect = new AdminIndoorNodeBehaviorPayload.EffectDefinition();
        effect.setId("effect-1");
        effect.setCategory("popup");
        behavior.setEffectRules(List.of(effect));
        behavior.setRewardRuleIds(List.of());
        return behavior;
    }

    private AdminIndoorNodeBehaviorPayload.CoordinatePoint point(String x, String y, int order) {
        AdminIndoorNodeBehaviorPayload.CoordinatePoint point = new AdminIndoorNodeBehaviorPayload.CoordinatePoint();
        point.setX(new BigDecimal(x));
        point.setY(new BigDecimal(y));
        point.setOrder(order);
        return point;
    }
}
