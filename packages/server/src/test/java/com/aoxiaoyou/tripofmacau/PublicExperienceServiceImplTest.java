package com.aoxiaoyou.tripofmacau;

import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.response.UserExplorationResponse;
import com.aoxiaoyou.tripofmacau.entity.ExplorationElement;
import com.aoxiaoyou.tripofmacau.entity.UserExplorationEvent;
import com.aoxiaoyou.tripofmacau.mapper.ContentAssetMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceBindingMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceFlowMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceFlowStepMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceOverrideMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceTemplateMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExplorationElementMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserExplorationEventMapper;
import com.aoxiaoyou.tripofmacau.service.StoryLineService;
import com.aoxiaoyou.tripofmacau.service.impl.PublicExperienceServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicExperienceServiceImplTest {

    @Mock
    private StoryLineService storyLineService;
    @Mock
    private ExperienceTemplateMapper templateMapper;
    @Mock
    private ExperienceFlowMapper flowMapper;
    @Mock
    private ExperienceFlowStepMapper stepMapper;
    @Mock
    private ExperienceBindingMapper bindingMapper;
    @Mock
    private ExperienceOverrideMapper overrideMapper;
    @Mock
    private ExplorationElementMapper explorationElementMapper;
    @Mock
    private UserExplorationEventMapper userExplorationEventMapper;
    @Mock
    private ContentAssetMapper contentAssetMapper;
    @Mock
    private LocalizedContentSupport localizedContentSupport;

    private PublicExperienceServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PublicExperienceServiceImpl(
                storyLineService,
                templateMapper,
                flowMapper,
                stepMapper,
                bindingMapper,
                overrideMapper,
                explorationElementMapper,
                userExplorationEventMapper,
                contentAssetMapper,
                localizedContentSupport,
                new ObjectMapper()
        );
        when(localizedContentSupport.resolveText(any(), any(), any(), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));
    }

    @Test
    void supportsGlobalAndLocationScopedWeightedProgress() {
        Long poiId = 101L;
        Long indoorBuildingId = 202L;
        Long indoorFloorId = 303L;
        List<String> querySegments = new ArrayList<>();

        when(explorationElementMapper.selectList(any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<ExplorationElement> wrapper = invocation.getArgument(0);
            String sqlSegment = wrapper.getSqlSegment();
            querySegments.add(sqlSegment);
            if (sqlSegment.contains("poi_id")) {
                return List.of(element(11L, "poi-active", "poi", poiId, null, null, 8, "published", true));
            }
            if (sqlSegment.contains("indoor_building_id")) {
                return List.of(element(12L, "building-active", null, null, indoorBuildingId, null, 5, "published", true));
            }
            if (sqlSegment.contains("indoor_floor_id")) {
                return List.of(element(13L, "floor-active", null, null, null, indoorFloorId, 3, "published", true));
            }
            return List.of(element(10L, "global-active", null, null, null, null, 2, "published", true));
        });
        when(userExplorationEventMapper.selectList(any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<UserExplorationEvent> wrapper = invocation.getArgument(0);
            String sqlSegment = wrapper.getSqlSegment();
            if (sqlSegment.contains("poi-active")) {
                return List.of(event(5001L, 77L, 11L, "poi-active", LocalDateTime.of(2026, 4, 29, 9, 0)));
            }
            if (sqlSegment.contains("building-active")) {
                return List.of(event(5002L, 77L, 12L, "building-active", LocalDateTime.of(2026, 4, 29, 9, 5)));
            }
            if (sqlSegment.contains("floor-active")) {
                return List.of(event(5003L, 77L, 13L, "floor-active", LocalDateTime.of(2026, 4, 29, 9, 10)));
            }
            return List.of(event(5000L, 77L, 10L, "global-active", LocalDateTime.of(2026, 4, 29, 8, 55)));
        });

        UserExplorationResponse globalResponse = service.getUserExploration(77L, "zh-Hant", "global", null);
        UserExplorationResponse poiResponse = service.getUserExploration(77L, "zh-Hant", "poi", poiId);
        UserExplorationResponse buildingResponse = service.getUserExploration(77L, "zh-Hant", "indoor_building", indoorBuildingId);
        UserExplorationResponse floorResponse = service.getUserExploration(77L, "zh-Hant", "indoor_floor", indoorFloorId);

        assertThat(globalResponse.getAvailableWeight()).isEqualTo(2);
        assertThat(poiResponse.getAvailableWeight()).isEqualTo(8);
        assertThat(buildingResponse.getAvailableWeight()).isEqualTo(5);
        assertThat(floorResponse.getAvailableWeight()).isEqualTo(3);
        assertThat(querySegments).anyMatch(sql -> sql.contains("poi_id"));
        assertThat(querySegments).anyMatch(sql -> sql.contains("indoor_building_id"));
        assertThat(querySegments).anyMatch(sql -> sql.contains("indoor_floor_id"));
    }

    @Test
    void supportsOwnerBackedTaskCollectibleRewardAndMediaScopes() {
        List<String> querySegments = new ArrayList<>();
        List<Collection<Object>> paramValueSnapshots = new ArrayList<>();

        when(explorationElementMapper.selectList(any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<ExplorationElement> wrapper = invocation.getArgument(0);
            querySegments.add(wrapper.getSqlSegment());
            paramValueSnapshots.add(wrapper.getParamNameValuePairs().values());
            Collection<Object> values = wrapper.getParamNameValuePairs().values();
            if (values.contains("experience_flow_step") && values.contains(9001L)) {
                return List.of(element(21L, "task-step", null, null, null, null, 8, "published", true));
            }
            if (values.contains("collectible") && values.contains(9002L)) {
                return List.of(element(22L, "collectible-step", null, null, null, null, 5, "published", true));
            }
            if (values.contains("reward") && values.contains(9003L)) {
                return List.of(element(23L, "reward-step", null, null, null, null, 3, "published", true));
            }
            if (values.contains("content_asset") && values.contains(9004L)) {
                return List.of(element(24L, "media-step", null, null, null, null, 2, "published", true));
            }
            return List.of();
        });
        when(userExplorationEventMapper.selectList(any())).thenReturn(List.of(
                event(6001L, 77L, 21L, "task-step", LocalDateTime.of(2026, 4, 29, 10, 0)),
                event(6002L, 77L, 22L, "collectible-step", LocalDateTime.of(2026, 4, 29, 10, 5)),
                event(6003L, 77L, 23L, "reward-step", LocalDateTime.of(2026, 4, 29, 10, 10)),
                event(6004L, 77L, 24L, "media-step", LocalDateTime.of(2026, 4, 29, 10, 15))
        ));

        UserExplorationResponse taskResponse = service.getUserExploration(77L, "zh-Hant", "task", 9001L);
        UserExplorationResponse collectibleResponse = service.getUserExploration(77L, "zh-Hant", "collectible", 9002L);
        UserExplorationResponse rewardResponse = service.getUserExploration(77L, "zh-Hant", "reward", 9003L);
        UserExplorationResponse mediaResponse = service.getUserExploration(77L, "zh-Hant", "media", 9004L);

        assertThat(taskResponse.getAvailableWeight()).isEqualTo(8);
        assertThat(collectibleResponse.getAvailableWeight()).isEqualTo(5);
        assertThat(rewardResponse.getAvailableWeight()).isEqualTo(3);
        assertThat(mediaResponse.getAvailableWeight()).isEqualTo(2);
        assertThat(querySegments).allMatch(sql -> sql.contains("owner_type") && sql.contains("owner_id"));
        assertThat(paramValueSnapshots).anyMatch(values -> values.contains("experience_flow_step"));
        assertThat(paramValueSnapshots).anyMatch(values -> values.contains("collectible"));
        assertThat(paramValueSnapshots).anyMatch(values -> values.contains("reward"));
        assertThat(paramValueSnapshots).anyMatch(values -> values.contains("content_asset"));
    }

    @Test
    void excludesInactiveCompletedElementsFromActivePercentage() {
        when(explorationElementMapper.selectList(any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<ExplorationElement> wrapper = invocation.getArgument(0);
            Collection<Object> values = wrapper.getParamNameValuePairs().values();
            if (values.contains(31L) || values.contains("retired-complete")) {
                return List.of(element(31L, "retired-complete", null, null, null, null, 3, "archived", false));
            }
            return List.of(
                    element(30L, "active-complete", null, null, null, null, 8, "published", true),
                    element(32L, "active-open", null, null, null, null, 5, "published", true)
            );
        });
        when(userExplorationEventMapper.selectList(any())).thenReturn(List.of(
                event(7001L, 77L, 30L, "active-complete", LocalDateTime.of(2026, 4, 29, 11, 0)),
                event(7002L, 77L, 31L, "retired-complete", LocalDateTime.of(2026, 4, 28, 18, 30))
        ));

        UserExplorationResponse response = service.getUserExploration(77L, "zh-Hant", "global", null);

        assertThat(response.getProgressPercent()).isEqualTo(61.54d);
        assertThat(response.getCompletedElementCount()).isEqualTo(1);
        assertThat(response.getAvailableElementCount()).isEqualTo(2);
        assertThat(response.getElements()).hasSize(3);
        assertThat(response.getElements())
                .filteredOn(element -> "retired-complete".equals(element.getElementCode()))
                .singleElement()
                .satisfies(element -> {
                    assertThat(element.isCompleted()).isTrue();
                    assertThat(element.isIncludedInCurrentPercentage()).isFalse();
                    assertThat(element.getSourceEventId()).isEqualTo(7002L);
                    assertThat(element.getEventOccurredAt()).isEqualTo(LocalDateTime.of(2026, 4, 28, 18, 30));
                });
    }

    private ExplorationElement element(
            Long id,
            String code,
            String ownerCode,
            Long poiId,
            Long indoorBuildingId,
            Long indoorFloorId,
            int weightValue,
            String status,
            boolean includeInExploration) {
        ExplorationElement element = new ExplorationElement();
        element.setId(id);
        element.setElementCode(code);
        element.setElementType("progress");
        element.setOwnerType("poi");
        element.setOwnerId(1L);
        element.setOwnerCode(ownerCode == null ? code : ownerCode);
        element.setPoiId(poiId);
        element.setIndoorBuildingId(indoorBuildingId);
        element.setIndoorFloorId(indoorFloorId);
        element.setTitleZh(code);
        element.setWeightLevel("core");
        element.setWeightValue(weightValue);
        element.setStatus(status);
        element.setIncludeInExploration(includeInExploration);
        return element;
    }

    private UserExplorationEvent event(
            Long id,
            Long userId,
            Long elementId,
            String elementCode,
            LocalDateTime occurredAt) {
        UserExplorationEvent event = new UserExplorationEvent();
        event.setId(id);
        event.setUserId(userId);
        event.setElementId(elementId);
        event.setElementCode(elementCode);
        event.setEventType("completed");
        event.setOccurredAt(occurredAt);
        return event;
    }
}
