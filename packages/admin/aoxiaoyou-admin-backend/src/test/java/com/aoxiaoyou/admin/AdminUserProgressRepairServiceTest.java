package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.common.exception.BusinessException;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressSummaryResponse;
import com.aoxiaoyou.admin.entity.SysOperationLog;
import com.aoxiaoyou.admin.entity.UserProgressOperationAudit;
import com.aoxiaoyou.admin.mapper.AdminUserProgressReadMapper;
import com.aoxiaoyou.admin.mapper.SysOperationLogMapper;
import com.aoxiaoyou.admin.mapper.UserExplorationEventAdminMapper;
import com.aoxiaoyou.admin.mapper.UserExplorationStateAdminMapper;
import com.aoxiaoyou.admin.mapper.UserProgressOperationAuditMapper;
import com.aoxiaoyou.admin.service.AdminUserProgressCalculatorService;
import com.aoxiaoyou.admin.service.AdminUserProgressRepairService;
import com.aoxiaoyou.admin.service.impl.AdminUserProgressRepairServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserProgressRepairServiceTest {

    @Mock
    private AdminUserProgressCalculatorService calculatorService;

    @Mock
    private AdminUserProgressReadMapper readMapper;

    @Mock
    private UserExplorationStateAdminMapper stateMapper;

    @Mock
    private UserExplorationEventAdminMapper eventMapper;

    @Mock
    private UserProgressOperationAuditMapper auditMapper;

    @Mock
    private SysOperationLogMapper sysOperationLogMapper;

    private AdminUserProgressRepairServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminUserProgressRepairServiceImpl(
                calculatorService,
                readMapper,
                stateMapper,
                eventMapper,
                auditMapper,
                sysOperationLogMapper,
                new ObjectMapper().registerModule(new JavaTimeModule())
        );
    }

    @Test
    void previewDoesNotPersistChanges() {
        stubPoiPreviewRows();

        AdminUserProgressRepairService.OperationPreview preview = service.previewRecompute(
                new AdminUserProgressRepairService.RecomputePreviewRequest(
                        operator(),
                        poiScope(),
                        "檢查發佈元素後的進度影響"
                )
        );

        assertThat(preview.actionType()).isEqualTo("RECOMPUTE_SCOPE");
        assertThat(preview.affectedUserCount()).isEqualTo(1);
        assertThat(preview.affectedScopeCount()).isEqualTo(1);
        assertThat(preview.availableElementCount()).isEqualTo(2);
        assertThat(preview.matchingEventCount()).isEqualTo(1);
        assertThat(preview.confirmationToken()).isNotBlank();
        assertThat(preview.requiredConfirmText()).isEqualTo("RECOMPUTE_SCOPE");

        verifyNoInteractions(stateMapper, auditMapper, sysOperationLogMapper);
        verify(eventMapper, never()).updateEventLink(any(), any(), any(), any());
        verify(eventMapper, never()).markDuplicate(any(), any(), any());
    }

    @Test
    void confirmRecomputeRebuildsStateAndWritesAudit() {
        stubPoiPreviewRows();
        when(calculatorService.calculateSummary(88L, "poi", 101L, false)).thenReturn(progressSummary());
        when(stateMapper.deleteScopeState(88L, "poi", 101L)).thenReturn(1);
        when(stateMapper.upsertScopeState(any())).thenReturn(1);

        AdminUserProgressRepairService.OperationPreview preview = service.previewRecompute(
                new AdminUserProgressRepairService.RecomputePreviewRequest(
                        operator(),
                        poiScope(),
                        "重新同步已發佈探索元素"
                )
        );

        AdminUserProgressRepairService.OperationResult result = service.confirmRecompute(
                new AdminUserProgressRepairService.RecomputeConfirmRequest(
                        operator(),
                        poiScope(),
                        "重新同步已發佈探索元素",
                        preview.confirmationToken(),
                        preview.requiredConfirmText()
                )
        );

        ArgumentCaptor<UserExplorationStateAdminMapper.ScopeStateUpsert> stateCaptor =
                ArgumentCaptor.forClass(UserExplorationStateAdminMapper.ScopeStateUpsert.class);
        ArgumentCaptor<UserProgressOperationAudit> auditCaptor = ArgumentCaptor.forClass(UserProgressOperationAudit.class);
        ArgumentCaptor<SysOperationLog> logCaptor = ArgumentCaptor.forClass(SysOperationLog.class);

        verify(stateMapper).deleteScopeState(88L, "poi", 101L);
        verify(stateMapper).upsertScopeState(stateCaptor.capture());
        verify(auditMapper).insert(auditCaptor.capture());
        verify(sysOperationLogMapper).insert(logCaptor.capture());

        assertThat(stateCaptor.getValue().getCompletedWeight()).isEqualTo(8);
        assertThat(stateCaptor.getValue().getAvailableWeight()).isEqualTo(13);
        assertThat(stateCaptor.getValue().getProgressPercent()).isEqualTo(61.54d);
        assertThat(result.writtenStateRows()).isEqualTo(1);
        assertThat(result.deletedEventRows()).isZero();
        assertThat(auditCaptor.getValue().getActionType()).isEqualTo("RECOMPUTE_SCOPE");
        assertThat(logCaptor.getValue().getModule()).isEqualTo("USER_PROGRESS");
        assertThat(logCaptor.getValue().getOperation()).isEqualTo("RECOMPUTE_SCOPE");
    }

    @Test
    void repairCanRelinkOrphanedEventAndMarkDuplicateWithoutDelete() {
        when(eventMapper.selectEventById(7001L)).thenReturn(orphanEvent());
        when(eventMapper.updateEventLink(eq(7001L), eq(10L), eq("ama_poi_arrival"), any())).thenReturn(1);

        AdminUserProgressRepairService.OperationPreview orphanPreview = service.previewRepair(
                new AdminUserProgressRepairService.RepairPreviewRequest(
                        operator(),
                        poiScope(),
                        "LINK_ORPHAN_EVENT",
                        7001L,
                        10L,
                        "ama_poi_arrival",
                        null,
                        "補回孤兒事件的元素綁定",
                        null,
                        null
                )
        );

        AdminUserProgressRepairService.OperationResult orphanResult = service.applyRepair(
                new AdminUserProgressRepairService.RepairApplyRequest(
                        operator(),
                        poiScope(),
                        "LINK_ORPHAN_EVENT",
                        7001L,
                        10L,
                        "ama_poi_arrival",
                        null,
                        "補回孤兒事件的元素綁定",
                        null,
                        null,
                        orphanPreview.confirmationToken(),
                        orphanPreview.requiredConfirmText()
                )
        );

        ArgumentCaptor<UserProgressOperationAudit> orphanAuditCaptor = ArgumentCaptor.forClass(UserProgressOperationAudit.class);
        verify(eventMapper).updateEventLink(eq(7001L), eq(10L), eq("ama_poi_arrival"), any());
        verify(auditMapper).insert(orphanAuditCaptor.capture());
        assertThat(orphanResult.mutatedEventRows()).isEqualTo(1);
        assertThat(orphanResult.deletedEventRows()).isZero();
        assertThat(orphanAuditCaptor.getValue().getResultSummaryJson()).contains("ama_poi_arrival");

        reset(eventMapper, auditMapper, sysOperationLogMapper);

        when(eventMapper.selectEventById(7002L)).thenReturn(duplicateCandidateEvent());
        when(eventMapper.selectEventById(7001L)).thenReturn(canonicalEvent());
        when(eventMapper.markDuplicate(eq(7002L), eq(7001L), any())).thenReturn(1);

        AdminUserProgressRepairService.OperationPreview duplicatePreview = service.previewRepair(
                new AdminUserProgressRepairService.RepairPreviewRequest(
                        operator(),
                        poiScope(),
                        "MARK_DUPLICATE_CLIENT_EVENT",
                        7002L,
                        null,
                        null,
                        7001L,
                        "標記重複的 client_event_id 事件",
                        null,
                        null
                )
        );

        AdminUserProgressRepairService.OperationResult duplicateResult = service.applyRepair(
                new AdminUserProgressRepairService.RepairApplyRequest(
                        operator(),
                        poiScope(),
                        "MARK_DUPLICATE_CLIENT_EVENT",
                        7002L,
                        null,
                        null,
                        7001L,
                        "標記重複的 client_event_id 事件",
                        null,
                        null,
                        duplicatePreview.confirmationToken(),
                        duplicatePreview.requiredConfirmText()
                )
        );

        verify(eventMapper).markDuplicate(eq(7002L), eq(7001L), any());
        assertThat(duplicateResult.mutatedEventRows()).isEqualTo(1);
        assertThat(duplicateResult.deletedEventRows()).isZero();
    }

    @Test
    void rejectsBroadUnscopedRepairRequests() {
        AdminUserProgressRepairService.ScopeTarget unscopedTarget =
                new AdminUserProgressRepairService.ScopeTarget(null, null, null, null);

        assertThatThrownBy(() -> service.previewRecompute(
                new AdminUserProgressRepairService.RecomputePreviewRequest(
                        operator(),
                        unscopedTarget,
                        "沒有範圍的全域重算"
                )
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("scoped");

        assertThatThrownBy(() -> service.previewRepair(
                new AdminUserProgressRepairService.RepairPreviewRequest(
                        operator(),
                        unscopedTarget,
                        "MARK_DUPLICATE_CLIENT_EVENT",
                        7002L,
                        null,
                        null,
                        7001L,
                        "沒有目標使用者的修復請求",
                        null,
                        null
                )
        )).isInstanceOf(BusinessException.class)
                .hasMessageContaining("scoped");
    }

    private void stubPoiPreviewRows() {
        when(readMapper.selectScopeElements("poi", 101L, true)).thenReturn(List.of(
                element(10L, "ama_poi_arrival", 8),
                element(11L, "ama_story_ch1_complete", 5)
        ));
        when(readMapper.selectUserEvents(88L)).thenReturn(List.of(
                event(9001L, 88L, 10L, "ama_poi_arrival", LocalDateTime.of(2026, 4, 29, 10, 0))
        ));
    }

    private AdminUserProgressRepairService.OperatorContext operator() {
        return new AdminUserProgressRepairService.OperatorContext(5L, "ops.lead", "127.0.0.1");
    }

    private AdminUserProgressRepairService.ScopeTarget poiScope() {
        return new AdminUserProgressRepairService.ScopeTarget(88L, "poi", 101L, null);
    }

    private AdminUserProgressSummaryResponse progressSummary() {
        return AdminUserProgressSummaryResponse.builder()
                .userId(88L)
                .scopeType("poi")
                .scopeId(101L)
                .completedWeight(8)
                .availableWeight(13)
                .completedElementCount(1)
                .availableElementCount(2)
                .retiredCompletedWeight(0)
                .retiredCompletedCount(0)
                .progressPercent(61.54d)
                .lastRecomputeTime(LocalDateTime.of(2026, 4, 29, 11, 45))
                .build();
    }

    private AdminUserProgressReadMapper.ProgressElementRow element(Long elementId, String elementCode, int weightValue) {
        return new AdminUserProgressReadMapper.ProgressElementRow(
                elementId,
                elementCode,
                "progress",
                "poi",
                101L,
                "ama_temple",
                1L,
                2L,
                101L,
                null,
                null,
                null,
                "簡體",
                "English",
                "繁體",
                "Português",
                "core",
                weightValue,
                true,
                "published"
        );
    }

    private AdminUserProgressReadMapper.ProgressEventRow event(
            Long eventId,
            Long userId,
            Long elementId,
            String elementCode,
            LocalDateTime occurredAt) {
        return new AdminUserProgressReadMapper.ProgressEventRow(
                eventId,
                userId,
                elementId,
                elementCode,
                "completed",
                occurredAt
        );
    }

    private UserExplorationEventAdminMapper.EventRecord orphanEvent() {
        return new UserExplorationEventAdminMapper.EventRecord(
                7001L,
                88L,
                null,
                "legacy-orphan",
                "completed",
                "mini_program",
                "client-orphan-1",
                "{\"source\":\"legacy\"}",
                false,
                null,
                null,
                LocalDateTime.of(2026, 4, 29, 8, 15)
        );
    }

    private UserExplorationEventAdminMapper.EventRecord duplicateCandidateEvent() {
        return new UserExplorationEventAdminMapper.EventRecord(
                7002L,
                88L,
                10L,
                "ama_poi_arrival",
                "completed",
                "mini_program",
                "dup-client-evt",
                "{\"source\":\"mini-program-duplicate\"}",
                false,
                null,
                null,
                LocalDateTime.of(2026, 4, 29, 8, 25)
        );
    }

    private UserExplorationEventAdminMapper.EventRecord canonicalEvent() {
        return new UserExplorationEventAdminMapper.EventRecord(
                7001L,
                88L,
                10L,
                "ama_poi_arrival",
                "completed",
                "mini_program",
                "dup-client-evt",
                "{\"source\":\"canonical\"}",
                false,
                null,
                null,
                LocalDateTime.of(2026, 4, 29, 8, 15)
        );
    }
}
