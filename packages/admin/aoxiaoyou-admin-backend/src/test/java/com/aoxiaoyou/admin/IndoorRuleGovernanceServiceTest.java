package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.dto.request.AdminIndoorNodeBehaviorPayload;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleConflictResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleGovernanceItemResponse;
import com.aoxiaoyou.admin.dto.response.AdminIndoorRuleStatusUpdateResponse;
import com.aoxiaoyou.admin.entity.Building;
import com.aoxiaoyou.admin.entity.GameReward;
import com.aoxiaoyou.admin.entity.IndoorFloor;
import com.aoxiaoyou.admin.entity.IndoorNode;
import com.aoxiaoyou.admin.entity.IndoorNodeBehavior;
import com.aoxiaoyou.admin.entity.RedeemablePrize;
import com.aoxiaoyou.admin.entity.RewardRule;
import com.aoxiaoyou.admin.entity.RewardRuleBinding;
import com.aoxiaoyou.admin.mapper.BuildingMapper;
import com.aoxiaoyou.admin.mapper.GameRewardMapper;
import com.aoxiaoyou.admin.mapper.IndoorFloorMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeBehaviorMapper;
import com.aoxiaoyou.admin.mapper.IndoorNodeMapper;
import com.aoxiaoyou.admin.mapper.RedeemablePrizeMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleBindingMapper;
import com.aoxiaoyou.admin.mapper.RewardRuleMapper;
import com.aoxiaoyou.admin.service.impl.IndoorRuleGovernanceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndoorRuleGovernanceServiceTest {

    @Mock
    private IndoorNodeBehaviorMapper behaviorMapper;
    @Mock
    private IndoorNodeMapper nodeMapper;
    @Mock
    private IndoorFloorMapper floorMapper;
    @Mock
    private BuildingMapper buildingMapper;
    @Mock
    private RewardRuleBindingMapper rewardRuleBindingMapper;
    @Mock
    private RewardRuleMapper rewardRuleMapper;
    @Mock
    private RedeemablePrizeMapper redeemablePrizeMapper;
    @Mock
    private GameRewardMapper gameRewardMapper;

    private ObjectMapper objectMapper;
    private IndoorRuleGovernanceService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        service = new IndoorRuleGovernanceService(
                behaviorMapper,
                nodeMapper,
                floorMapper,
                buildingMapper,
                rewardRuleBindingMapper,
                rewardRuleMapper,
                redeemablePrizeMapper,
                gameRewardMapper,
                objectMapper
        );
    }

    @Test
    void listOverviewIncludesRuntimeSupportAndConflictCount() throws Exception {
        Fixture fixture = buildFixture();
        mockFixtureReads(fixture);

        List<AdminIndoorRuleGovernanceItemResponse> result = service.listOverview(
                null, null, null, null, null,
                null, null, null, null,
                null, null
        );

        assertThat(result).hasSize(2);
        AdminIndoorRuleGovernanceItemResponse first = result.stream()
                .filter(item -> item.getBehaviorId().equals(fixture.behaviorAlpha().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(first.getBehaviorCode()).isEqualTo("alpha");
        assertThat(first.getRuntimeSupportLevel()).isEqualTo("phase15_storage_only");
        assertThat(first.getConflictCount()).isGreaterThanOrEqualTo(3);
        assertThat(first.getLinkedRewardRuleCount()).isEqualTo(1);
    }

    @Test
    void listConflictsEmitsAllFourDeterministicCodes() throws Exception {
        Fixture fixture = buildFixture();
        mockFixtureReads(fixture);

        List<AdminIndoorRuleConflictResponse> conflicts = service.listConflicts(
                null, null, null, null, null,
                null, null, null, null,
                null, null
        );

        assertThat(conflicts).extracting(AdminIndoorRuleConflictResponse::getConflictCode)
                .contains(
                        IndoorRuleGovernanceService.CONFLICT_MISSING_PREREQUISITE,
                        IndoorRuleGovernanceService.CONFLICT_SCHEDULE_OVERLAP,
                        IndoorRuleGovernanceService.CONFLICT_ENTITY_COLLISION,
                        IndoorRuleGovernanceService.CONFLICT_STATUS_MISMATCH
                );
    }

    @Test
    void updateBehaviorStatusReturnsWarningsWhenEnableIsBlocked() throws Exception {
        Fixture fixture = buildFixture();
        when(behaviorMapper.selectById(fixture.behaviorAlpha().getId())).thenReturn(fixture.behaviorAlpha());
        when(nodeMapper.selectById(fixture.nodeAlpha().getId())).thenReturn(fixture.nodeAlpha());

        AdminIndoorRuleStatusUpdateResponse response = service.updateBehaviorStatus(
                fixture.behaviorAlpha().getId(),
                "enabled"
        );

        assertThat(response.getBehaviorId()).isEqualTo(fixture.behaviorAlpha().getId());
        assertThat(response.getStatus()).isEqualTo("enabled");
        assertThat(response.getParentNodeStatus()).isEqualTo("draft");
        assertThat(response.getWarnings()).isNotEmpty();
        verify(behaviorMapper, never()).updateById(any());
    }

    @Test
    void getBehaviorDetailIncludesSharedRewardOwnersFromRewardDomain() throws Exception {
        Fixture fixture = buildFixture();
        when(behaviorMapper.selectList(any())).thenReturn(List.of(fixture.behaviorAlpha()));
        when(nodeMapper.selectBatchIds(any())).thenReturn(List.of(fixture.nodeAlpha()));
        when(floorMapper.selectBatchIds(any())).thenReturn(List.of(fixture.floor()));
        when(buildingMapper.selectBatchIds(any())).thenReturn(List.of(fixture.building()));
        when(behaviorMapper.selectById(fixture.behaviorAlpha().getId())).thenReturn(fixture.behaviorAlpha());
        when(nodeMapper.selectById(fixture.nodeAlpha().getId())).thenReturn(fixture.nodeAlpha());
        when(floorMapper.selectById(fixture.floor().getId())).thenReturn(fixture.floor());
        when(buildingMapper.selectById(fixture.building().getId())).thenReturn(fixture.building());

        RewardRuleBinding indoorBinding = new RewardRuleBinding();
        indoorBinding.setOwnerDomain("indoor_behavior");
        indoorBinding.setOwnerId(fixture.behaviorAlpha().getId());
        indoorBinding.setRuleId(9001L);
        indoorBinding.setSortOrder(0);

        RewardRuleBinding prizeBinding = new RewardRuleBinding();
        prizeBinding.setOwnerDomain("redeemable_prize");
        prizeBinding.setOwnerId(8001L);
        prizeBinding.setOwnerCode("prize_fixture");
        prizeBinding.setRuleId(9001L);

        RewardRuleBinding rewardBinding = new RewardRuleBinding();
        rewardBinding.setOwnerDomain("game_reward");
        rewardBinding.setOwnerId(8002L);
        rewardBinding.setOwnerCode("reward_fixture");
        rewardBinding.setRuleId(9001L);

        when(rewardRuleBindingMapper.selectList(any())).thenReturn(
                List.of(indoorBinding),
                List.of(prizeBinding, rewardBinding)
        );

        RewardRule rule = new RewardRule();
        rule.setId(9001L);
        rule.setCode("rule_fixture");
        rule.setNameZht("共享規則");
        when(rewardRuleMapper.selectBatchIds(any())).thenReturn(List.of(rule));

        RedeemablePrize prize = new RedeemablePrize();
        prize.setId(8001L);
        prize.setCode("prize_fixture");
        prize.setNameZht("兌換獎勵");
        when(redeemablePrizeMapper.selectBatchIds(any())).thenReturn(List.of(prize));

        GameReward reward = new GameReward();
        reward.setId(8002L);
        reward.setCode("reward_fixture");
        reward.setNameZht("遊戲內獎勵");
        when(gameRewardMapper.selectBatchIds(any())).thenReturn(List.of(reward));

        var detail = service.getBehaviorDetail(fixture.behaviorAlpha().getId());

        assertThat(detail.getLinkedRewardRuleIds()).containsExactly(9001L);
        assertThat(detail.getLinkedRewardRules()).extracting("code").containsExactly("rule_fixture");
        assertThat(detail.getLinkedRewards()).extracting("ownerDomain")
                .containsExactly("redeemable_prize", "game_reward");
    }

    private void mockFixtureReads(Fixture fixture) {
        when(behaviorMapper.selectList(any())).thenReturn(List.of(fixture.behaviorAlpha(), fixture.behaviorBeta()));
        when(nodeMapper.selectBatchIds(any())).thenReturn(List.of(fixture.nodeAlpha(), fixture.nodeBeta()));
        when(floorMapper.selectBatchIds(any())).thenReturn(List.of(fixture.floor()));
        when(buildingMapper.selectBatchIds(any())).thenReturn(List.of(fixture.building()));
        RewardRuleBinding binding = new RewardRuleBinding();
        binding.setOwnerDomain("indoor_behavior");
        binding.setOwnerId(fixture.behaviorAlpha().getId());
        binding.setRuleId(9001L);
        when(rewardRuleBindingMapper.selectList(any())).thenReturn(List.of(binding));
        RewardRule rule = new RewardRule();
        rule.setId(9001L);
        rule.setCode("rule_fixture");
        rule.setNameZht("規則樣板");
        lenient().when(rewardRuleMapper.selectBatchIds(any())).thenReturn(List.of(rule));
        RedeemablePrize prize = new RedeemablePrize();
        prize.setId(8001L);
        prize.setCode("prize_fixture");
        prize.setNameZht("實體獎勵");
        GameReward reward = new GameReward();
        reward.setId(8002L);
        reward.setCode("reward_fixture");
        reward.setNameZht("稱號獎勵");
        lenient().when(redeemablePrizeMapper.selectBatchIds(any())).thenReturn(List.of(prize));
        lenient().when(gameRewardMapper.selectBatchIds(any())).thenReturn(List.of(reward));
    }

    private Fixture buildFixture() throws JsonProcessingException {
        Building building = new Building();
        building.setId(30L);
        building.setCityId(1L);
        building.setNameZht("路氹金光大道");

        IndoorFloor floor = new IndoorFloor();
        floor.setId(20L);
        floor.setBuildingId(30L);
        floor.setFloorCode("B1");

        IndoorNode nodeAlpha = new IndoorNode();
        nodeAlpha.setId(10L);
        nodeAlpha.setBuildingId(30L);
        nodeAlpha.setFloorId(20L);
        nodeAlpha.setMarkerCode("marker-alpha");
        nodeAlpha.setPresentationMode("marker");
        nodeAlpha.setOverlayType("point");
        nodeAlpha.setLinkedEntityType("task");
        nodeAlpha.setLinkedEntityId(500L);
        nodeAlpha.setRuntimeSupportLevel("phase15_storage_only");
        nodeAlpha.setStatus("draft");

        IndoorNode nodeBeta = new IndoorNode();
        nodeBeta.setId(11L);
        nodeBeta.setBuildingId(30L);
        nodeBeta.setFloorId(20L);
        nodeBeta.setMarkerCode("marker-beta");
        nodeBeta.setPresentationMode("marker");
        nodeBeta.setOverlayType("point");
        nodeBeta.setLinkedEntityType("task");
        nodeBeta.setLinkedEntityId(500L);
        nodeBeta.setRuntimeSupportLevel("phase16_supported");
        nodeBeta.setStatus("published");

        IndoorNodeBehavior behaviorAlpha = new IndoorNodeBehavior();
        behaviorAlpha.setId(100L);
        behaviorAlpha.setNodeId(10L);
        behaviorAlpha.setBehaviorCode("alpha");
        behaviorAlpha.setBehaviorNameZht("夜色初現");
        behaviorAlpha.setAppearanceRulesJson(writeJson(List.of(scheduleRule("09:00", "10:00"))));
        behaviorAlpha.setTriggerRulesJson(writeJson(List.of(
                triggerStep("tap-start", "tap", null),
                triggerStep("tap-finish", "tap", "missing-step")
        )));
        behaviorAlpha.setEffectRulesJson(writeJson(List.of(effect("popup"))));
        behaviorAlpha.setRuntimeSupportLevel("phase15_storage_only");
        behaviorAlpha.setStatus("enabled");
        behaviorAlpha.setSortOrder(1);

        IndoorNodeBehavior behaviorBeta = new IndoorNodeBehavior();
        behaviorBeta.setId(101L);
        behaviorBeta.setNodeId(11L);
        behaviorBeta.setBehaviorCode("beta");
        behaviorBeta.setBehaviorNameZht("光影同行");
        behaviorBeta.setAppearanceRulesJson(writeJson(List.of(scheduleRule("09:30", "10:30"))));
        behaviorBeta.setTriggerRulesJson(writeJson(List.of(triggerStep("tap-2", "tap", null))));
        behaviorBeta.setEffectRulesJson(writeJson(List.of(effect("bubble"))));
        behaviorBeta.setRuntimeSupportLevel("phase16_supported");
        behaviorBeta.setStatus("enabled");
        behaviorBeta.setSortOrder(2);

        return new Fixture(building, floor, nodeAlpha, nodeBeta, behaviorAlpha, behaviorBeta);
    }

    private AdminIndoorNodeBehaviorPayload.RuleCondition scheduleRule(String start, String end) {
        AdminIndoorNodeBehaviorPayload.RuleCondition rule = new AdminIndoorNodeBehaviorPayload.RuleCondition();
        rule.setId("schedule-" + start);
        rule.setCategory("schedule_window");
        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.put("startTime", start);
        config.put("endTime", end);
        config.putArray("weekdays").addAll(Set.of("1", "2", "3", "4", "5").stream()
                .map(JsonNodeFactory.instance::textNode)
                .toList());
        rule.setConfig(config);
        return rule;
    }

    private AdminIndoorNodeBehaviorPayload.TriggerStep triggerStep(String id, String category, String dependsOn) {
        AdminIndoorNodeBehaviorPayload.TriggerStep trigger = new AdminIndoorNodeBehaviorPayload.TriggerStep();
        trigger.setId(id);
        trigger.setCategory(category);
        trigger.setDependsOnTriggerId(dependsOn);
        return trigger;
    }

    private AdminIndoorNodeBehaviorPayload.EffectDefinition effect(String category) {
        AdminIndoorNodeBehaviorPayload.EffectDefinition effect = new AdminIndoorNodeBehaviorPayload.EffectDefinition();
        effect.setId("effect-" + category);
        effect.setCategory(category);
        return effect;
    }

    private String writeJson(Object value) throws JsonProcessingException {
        return objectMapper.writeValueAsString(value);
    }

    private record Fixture(
            Building building,
            IndoorFloor floor,
            IndoorNode nodeAlpha,
            IndoorNode nodeBeta,
            IndoorNodeBehavior behaviorAlpha,
            IndoorNodeBehavior behaviorBeta
    ) {}
}
