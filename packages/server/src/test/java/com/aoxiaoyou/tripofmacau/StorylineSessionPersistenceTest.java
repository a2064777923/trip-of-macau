package com.aoxiaoyou.tripofmacau;

import com.aoxiaoyou.tripofmacau.common.util.LocalizedContentSupport;
import com.aoxiaoyou.tripofmacau.dto.request.ExperienceEventRequest;
import com.aoxiaoyou.tripofmacau.dto.response.ExperienceEventResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryChapterResponse;
import com.aoxiaoyou.tripofmacau.dto.response.StoryLineResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorylineSessionPersistenceTest {

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
        ReflectionTestUtils.setField(service, "userStorylineSessionMapper", userStorylineSessionMapper);
    }

    @Test
    void startPersistsDurableSessionRow() {
        Long userId = 77L;
        Long storylineId = 901L;
        when(storyLineService.getDetail(storylineId, "zh-Hant"))
                .thenReturn(storyline(storylineId, chapter(3001L, 1), chapter(3002L, 2)));
        when(userStorylineSessionMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> 1).when(userStorylineSessionMapper).insert(any(UserStorylineSession.class));

        StorylineSessionResponse response = service.startStorylineSession(userId, storylineId);

        ArgumentCaptor<UserStorylineSession> insertedCaptor = ArgumentCaptor.forClass(UserStorylineSession.class);
        verify(userStorylineSessionMapper).insert(insertedCaptor.capture());
        UserStorylineSession inserted = insertedCaptor.getValue();

        assertThat(inserted.getSessionId()).isNotBlank();
        assertThat(inserted.getUserId()).isEqualTo(userId);
        assertThat(inserted.getStorylineId()).isEqualTo(storylineId);
        assertThat(inserted.getCurrentChapterId()).isEqualTo(3001L);
        assertThat(inserted.getStatus()).isEqualTo("started");
        assertThat(inserted.getEventCount()).isZero();
        assertThat(inserted.getStartedAt()).isNotNull();
        assertThat(inserted.getTemporaryStepStateJson()).isEqualTo("{}");
        assertThat(inserted.getExitClearedTemporaryState()).isFalse();

        assertThat(response.getStorylineId()).isEqualTo(storylineId);
        assertThat(response.getSessionId()).isEqualTo(inserted.getSessionId());
        assertThat(response.getCurrentChapterId()).isEqualTo(3001L);
        assertThat(response.getStatus()).isEqualTo("started");
        assertThat(response.getStartedAt()).isEqualTo(inserted.getStartedAt());
        assertThat(response.getEventCount()).isZero();
        assertThat(response.getExitClearedTemporaryState()).isFalse();
    }

    @Test
    void recordStorylineEventUpdatesSessionCountsWithoutMutatingPermanentFacts() {
        Long userId = 77L;
        LocalDateTime occurredAt = LocalDateTime.of(2026, 4, 29, 15, 20, 0);
        UserStorylineSession activeSession = activeSession();
        when(userExplorationEventMapper.selectOne(any())).thenReturn(null);
        when(explorationElementMapper.selectById(501L)).thenReturn(storyChapterElement(501L, "ama_story_ch1_complete", 3002L));
        when(userStorylineSessionMapper.selectOne(any())).thenReturn(activeSession);
        doAnswer(invocation -> {
            UserExplorationEvent event = invocation.getArgument(0);
            event.setId(81001L);
            return 1;
        }).when(userExplorationEventMapper).insert(any(UserExplorationEvent.class));
        when(userStorylineSessionMapper.updateById(any(UserStorylineSession.class))).thenReturn(1);

        ExperienceEventResponse response = service.recordEvent(userId, eventRequest(
                501L,
                "session-story-901",
                "evt-session-1",
                "{\"currentChapterId\":3002,\"completedTemporarySteps\":[\"chapter-arrival\"]}",
                occurredAt));

        ArgumentCaptor<UserExplorationEvent> eventCaptor = ArgumentCaptor.forClass(UserExplorationEvent.class);
        verify(userExplorationEventMapper).insert(eventCaptor.capture());
        UserExplorationEvent insertedEvent = eventCaptor.getValue();
        assertThat(insertedEvent.getStorylineSessionId()).isEqualTo("session-story-901");
        assertThat(insertedEvent.getClientEventId()).isEqualTo("evt-session-1");
        assertThat(insertedEvent.getOccurredAt()).isEqualTo(occurredAt);

        ArgumentCaptor<UserStorylineSession> sessionCaptor = ArgumentCaptor.forClass(UserStorylineSession.class);
        verify(userStorylineSessionMapper).updateById(sessionCaptor.capture());
        UserStorylineSession updatedSession = sessionCaptor.getValue();
        assertThat(updatedSession.getSessionId()).isEqualTo("session-story-901");
        assertThat(updatedSession.getEventCount()).isEqualTo(3);
        assertThat(updatedSession.getCurrentChapterId()).isEqualTo(3002L);
        assertThat(updatedSession.getLastEventAt()).isEqualTo(occurredAt);
        assertThat(updatedSession.getTemporaryStepStateJson()).contains("completedTemporarySteps");

        assertThat(response.isAccepted()).isTrue();
        assertThat(response.getEventId()).isEqualTo(81001L);
        assertThat(response.getStorylineSessionId()).isEqualTo("session-story-901");
    }

    @Test
    void exitLeavesExplorationEventsUntouched() {
        UserStorylineSession activeSession = activeSession();
        when(userStorylineSessionMapper.selectOne(any())).thenReturn(activeSession);
        when(userStorylineSessionMapper.updateById(any(UserStorylineSession.class))).thenReturn(1);

        StorylineSessionResponse response = service.exitStorylineSession(77L, 901L, "session-story-901");

        ArgumentCaptor<UserStorylineSession> sessionCaptor = ArgumentCaptor.forClass(UserStorylineSession.class);
        verify(userStorylineSessionMapper).updateById(sessionCaptor.capture());
        UserStorylineSession exitedSession = sessionCaptor.getValue();

        assertThat(exitedSession.getStatus()).isEqualTo("exited");
        assertThat(exitedSession.getExitedAt()).isNotNull();
        assertThat(exitedSession.getEventCount()).isEqualTo(2);
        assertThat(exitedSession.getTemporaryStepStateJson()).isEqualTo("{}");
        assertThat(exitedSession.getExitClearedTemporaryState()).isTrue();

        assertThat(response.getSessionId()).isEqualTo("session-story-901");
        assertThat(response.getStorylineId()).isEqualTo(901L);
        assertThat(response.getCurrentChapterId()).isEqualTo(3001L);
        assertThat(response.getStatus()).isEqualTo("exited");
        assertThat(response.getExitedAt()).isEqualTo(exitedSession.getExitedAt());
        assertThat(response.getEventCount()).isEqualTo(2);
        assertThat(response.getExitClearedTemporaryState()).isTrue();

        verifyNoInteractions(userExplorationEventMapper);
        verifyNoInteractions(explorationElementMapper);
    }

    private StoryLineResponse storyline(Long storylineId, StoryChapterResponse... chapters) {
        return StoryLineResponse.builder()
                .id(storylineId)
                .chapters(List.of(chapters))
                .build();
    }

    private StoryChapterResponse chapter(Long chapterId, Integer chapterOrder) {
        return StoryChapterResponse.builder()
                .id(chapterId)
                .chapterOrder(chapterOrder)
                .build();
    }

    private ExplorationElement storyChapterElement(Long elementId, String elementCode, Long storyChapterId) {
        ExplorationElement element = new ExplorationElement();
        element.setId(elementId);
        element.setElementCode(elementCode);
        element.setStoryChapterId(storyChapterId);
        return element;
    }

    private ExperienceEventRequest eventRequest(
            Long elementId,
            String sessionId,
            String clientEventId,
            String payloadJson,
            LocalDateTime occurredAt) {
        ExperienceEventRequest request = new ExperienceEventRequest();
        request.setElementId(elementId);
        request.setEventType("completed");
        request.setEventSource("mini_program");
        request.setStorylineSessionId(sessionId);
        request.setClientEventId(clientEventId);
        request.setPayloadJson(payloadJson);
        request.setOccurredAt(occurredAt.toString());
        return request;
    }

    private UserStorylineSession activeSession() {
        UserStorylineSession session = new UserStorylineSession();
        session.setSessionId("session-story-901");
        session.setUserId(77L);
        session.setStorylineId(901L);
        session.setCurrentChapterId(3001L);
        session.setStatus("started");
        session.setStartedAt(LocalDateTime.of(2026, 4, 29, 14, 45, 0));
        session.setLastEventAt(LocalDateTime.of(2026, 4, 29, 15, 0, 0));
        session.setEventCount(2);
        session.setTemporaryStepStateJson("{\"completedTemporarySteps\":[\"chapter-intro\"]}");
        session.setExitClearedTemporaryState(false);
        return session;
    }
}
