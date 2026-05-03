package com.aoxiaoyou.tripofmacau;

import com.aoxiaoyou.tripofmacau.common.exception.BusinessException;
import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.request.ExperienceEventRequest;
import com.aoxiaoyou.tripofmacau.dto.response.ExperienceEventResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StorylineSessionResponse;
import com.aoxiaoyou.tripofmacau.entity.ExplorationElement;
import com.aoxiaoyou.tripofmacau.entity.UserExplorationEvent;
import com.aoxiaoyou.tripofmacau.entity.UserStorylineSession;
import com.aoxiaoyou.tripofmacau.mapper.ContentAssetMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceBindingMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceFlowMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceFlowStepMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceOverrideMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExperienceTemplateMapper;
import com.aoxiaoyou.tripofmacau.mapper.ExplorationElementMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserExplorationEventMapper;
import com.aoxiaoyou.tripofmacau.mapper.UserStorylineSessionMapper;
import com.aoxiaoyou.tripofmacau.service.PublicRuntimeAssetService;
import com.aoxiaoyou.tripofmacau.service.StoryLineService;
import com.aoxiaoyou.tripofmacau.service.impl.PublicExperienceServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicExperienceEventServiceTest {

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
    private UserStorylineSessionMapper userStorylineSessionMapper;
    @Mock
    private ContentAssetMapper contentAssetMapper;
    @Mock
    private LocalizedContentSupport localizedContentSupport;
    @Mock
    private PublicRuntimeAssetService publicRuntimeAssetService;

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
                publicRuntimeAssetService,
                new ObjectMapper()
        );
        ReflectionTestUtils.setField(service, "userStorylineSessionMapper", userStorylineSessionMapper);
        lenient().when(userExplorationEventMapper.selectOne(any())).thenReturn(null);
        lenient().doAnswer(invocation -> {
            UserExplorationEvent event = invocation.getArgument(0);
            event.setId(9000L);
            return 1;
        }).when(userExplorationEventMapper).insert(any(UserExplorationEvent.class));
    }

    @Test
    void allowedStoryEventTypesAreAccepted() {
        List<String> allowedTypes = List.of(
                "story_opened",
                "chapter_started",
                "content_viewed",
                "media_completed",
                "pickup_interacted",
                "task_completed",
                "reward_acquired",
                "unsupported_viewed",
                "story_session_exit",
                "chapter_open"
        );

        for (String eventType : allowedTypes) {
            ExperienceEventResponse response = service.recordEvent(77L, request(eventType, "evt-" + eventType, "{\"chapterId\":3001}"));

            assertThat(response.isAccepted()).isTrue();
            assertThat(response.isDuplicate()).isFalse();
            assertThat(response.getEventType()).isEqualTo(eventType);
            assertThat(response.getCurrentChapterId()).isEqualTo(3001L);
        }
    }

    @Test
    void unsupportedEventTypeIsRejected() {
        assertThatThrownBy(() -> service.recordEvent(77L, request("grant_inventory", "evt-bad", "{}")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Unsupported eventType");
    }

    @Test
    void invalidPayloadJsonIsRejected() {
        assertThatThrownBy(() -> service.recordEvent(77L, request("media_completed", "evt-invalid-json", "{broken")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("payloadJson must be valid JSON");
    }

    @Test
    void oversizedPayloadJsonIsRejected() {
        String oversized = "{\"note\":\"" + "x".repeat(8200) + "\"}";

        assertThatThrownBy(() -> service.recordEvent(77L, request("content_viewed", "evt-oversized", oversized)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("payloadJson is too large");
    }

    @Test
    void duplicateClientEventIdReturnsOriginalEvent() {
        UserExplorationEvent existing = event(8100L, "media_completed", "evt-dup", "{\"chapterId\":3002}");
        when(userExplorationEventMapper.selectOne(any())).thenReturn(existing);

        ExperienceEventResponse response = service.recordEvent(77L, request("media_completed", "evt-dup", "{\"chapterId\":3002}"));

        assertThat(response.getEventId()).isEqualTo(8100L);
        assertThat(response.isDuplicate()).isTrue();
        assertThat(response.getCurrentChapterId()).isEqualTo(3002L);
        verify(userExplorationEventMapper, never()).insert(any());
    }

    @Test
    void mediaCompletionUpdatesCurrentChapter() {
        UserStorylineSession session = activeSession();
        when(userStorylineSessionMapper.selectOne(any())).thenReturn(session);
        when(userStorylineSessionMapper.updateById(any())).thenReturn(1);

        ExperienceEventResponse response = service.recordEvent(
                77L,
                request("media_completed", "evt-media", "{\"currentChapterId\":3003,\"assetId\":901}")
                        .withSession("session-story-901"));

        ArgumentCaptor<UserStorylineSession> sessionCaptor = ArgumentCaptor.forClass(UserStorylineSession.class);
        verify(userStorylineSessionMapper).updateById(sessionCaptor.capture());
        UserStorylineSession updated = sessionCaptor.getValue();

        assertThat(updated.getCurrentChapterId()).isEqualTo(3003L);
        assertThat(updated.getEventCount()).isEqualTo(3);
        assertThat(response.getCurrentChapterId()).isEqualTo(3003L);
        assertThat(response.isDuplicate()).isFalse();
    }

    @Test
    void rewardAcquiredEventDoesNotRequireRawRewardGrant() {
        ExperienceEventResponse response = service.recordEvent(77L, request("reward_acquired", "evt-reward", "{\"rewardCode\":\"honor\"}"));

        ArgumentCaptor<UserExplorationEvent> eventCaptor = ArgumentCaptor.forClass(UserExplorationEvent.class);
        verify(userExplorationEventMapper).insert(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getEventType()).isEqualTo("reward_acquired");
        assertThat(eventCaptor.getValue().getEventPayloadJson()).contains("rewardCode");
        assertThat(response.isAccepted()).isTrue();
    }

    @Test
    void exitStorylineSessionIsIdempotent() {
        UserStorylineSession active = activeSession();
        UserStorylineSession exited = activeSession();
        exited.setStatus("exited");
        exited.setExitedAt(LocalDateTime.of(2026, 5, 3, 12, 30));
        exited.setExitClearedTemporaryState(true);
        exited.setTemporaryStepStateJson("{}");
        when(userStorylineSessionMapper.selectOne(any())).thenReturn(active, exited);
        when(userStorylineSessionMapper.updateById(any())).thenReturn(1);

        StorylineSessionResponse first = service.exitStorylineSession(77L, 901L, "session-story-901");
        StorylineSessionResponse second = service.exitStorylineSession(77L, 901L, "session-story-901");

        assertThat(first.getDuplicateExit()).isFalse();
        assertThat(second.getDuplicateExit()).isTrue();
        assertThat(second.getExitClearedTemporaryState()).isTrue();
        verifyNoInteractions(userExplorationEventMapper);
    }

    private EventRequestBuilder request(String eventType, String clientEventId, String payloadJson) {
        return new EventRequestBuilder(eventType, clientEventId, payloadJson);
    }

    private UserExplorationEvent event(Long id, String eventType, String clientEventId, String payloadJson) {
        UserExplorationEvent event = new UserExplorationEvent();
        event.setId(id);
        event.setUserId(77L);
        event.setElementId(501L);
        event.setElementCode("ama_story");
        event.setEventType(eventType);
        event.setClientEventId(clientEventId);
        event.setStorylineSessionId("session-story-901");
        event.setEventPayloadJson(payloadJson);
        event.setCreatedAt(LocalDateTime.of(2026, 5, 3, 12, 0));
        event.setOccurredAt(LocalDateTime.of(2026, 5, 3, 11, 59));
        return event;
    }

    private UserStorylineSession activeSession() {
        UserStorylineSession session = new UserStorylineSession();
        session.setSessionId("session-story-901");
        session.setUserId(77L);
        session.setStorylineId(901L);
        session.setCurrentChapterId(3001L);
        session.setStatus("started");
        session.setStartedAt(LocalDateTime.of(2026, 5, 3, 11, 0));
        session.setEventCount(2);
        session.setTemporaryStepStateJson("{\"lastEventType\":\"chapter_started\"}");
        session.setExitClearedTemporaryState(false);
        return session;
    }

    private static class EventRequestBuilder extends ExperienceEventRequest {
        EventRequestBuilder(String eventType, String clientEventId, String payloadJson) {
            setEventType(eventType);
            setClientEventId(clientEventId);
            setPayloadJson(payloadJson);
            setOccurredAt(LocalDateTime.of(2026, 5, 3, 12, 0).toString());
        }

        EventRequestBuilder withSession(String sessionId) {
            setStorylineSessionId(sessionId);
            return this;
        }
    }
}
