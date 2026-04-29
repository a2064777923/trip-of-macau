package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.dto.response.AdminUserProgressBreakdownResponse;
import com.aoxiaoyou.admin.dto.response.AdminUserProgressSummaryResponse;
import com.aoxiaoyou.admin.mapper.AdminUserProgressReadMapper;
import com.aoxiaoyou.admin.service.impl.AdminUserProgressCalculatorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserProgressCalculatorTest {

    @Mock
    private AdminUserProgressReadMapper readMapper;

    private AdminUserProgressCalculatorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminUserProgressCalculatorServiceImpl(readMapper);
    }

    @Test
    void matchesPublicWeightSemanticsForActiveElements() {
        when(readMapper.selectScopeElements("poi", 101L, true)).thenReturn(List.of(
                element(10L, "active-complete", "published", true, 8),
                element(11L, "active-open", "published", true, 5)
        ));
        when(readMapper.selectUserEvents(77L)).thenReturn(List.of(
                event(5001L, 10L, "active-complete", LocalDateTime.of(2026, 4, 29, 11, 0))
        ));
        when(readMapper.selectLastRecomputeTime(77L, "poi", 101L))
                .thenReturn(LocalDateTime.of(2026, 4, 29, 11, 30));

        AdminUserProgressSummaryResponse summary = service.calculateSummary(77L, "poi", 101L, false);

        assertThat(summary.getCompletedWeight()).isEqualTo(8);
        assertThat(summary.getAvailableWeight()).isEqualTo(13);
        assertThat(summary.getProgressPercent()).isEqualTo(61.54d);
        assertThat(summary.getCompletedElementCount()).isEqualTo(1);
        assertThat(summary.getAvailableElementCount()).isEqualTo(2);
    }

    @Test
    void includesRetiredElementsOnlyWhenRequested() {
        when(readMapper.selectScopeElements("poi", 101L, true)).thenReturn(List.of(
                element(10L, "active-complete", "published", true, 8),
                element(11L, "active-open", "published", true, 5)
        ));
        when(readMapper.selectUserEvents(77L)).thenReturn(List.of(
                event(5001L, 10L, "active-complete", LocalDateTime.of(2026, 4, 29, 11, 0)),
                event(5002L, 12L, "retired-complete", LocalDateTime.of(2026, 4, 28, 18, 30))
        ));
        when(readMapper.selectElementsByIdsOrCodes(List.of(12L), List.of("retired-complete")))
                .thenReturn(List.of(
                        element(12L, "retired-complete", "archived", false, 3)
                ));
        when(readMapper.selectLastRecomputeTime(77L, "poi", 101L))
                .thenReturn(LocalDateTime.of(2026, 4, 29, 11, 45));

        AdminUserProgressBreakdownResponse defaultBreakdown = service.calculateBreakdown(77L, "poi", 101L, false);
        AdminUserProgressBreakdownResponse comparisonBreakdown = service.calculateBreakdown(77L, "poi", 101L, true);

        assertThat(defaultBreakdown.getRetiredElements()).isEmpty();
        assertThat(comparisonBreakdown.getRetiredElements()).hasSize(1);
        assertThat(comparisonBreakdown.getRetiredElements().get(0).isIncludedInCurrentPercentage()).isFalse();
    }

    @Test
    void surfacesRetiredCompletedWeightAndLastRecomputeTime() {
        when(readMapper.selectScopeElements("poi", 101L, true)).thenReturn(List.of(
                element(10L, "active-complete", "published", true, 8),
                element(11L, "active-open", "published", true, 5)
        ));
        when(readMapper.selectUserEvents(77L)).thenReturn(List.of(
                event(5001L, 10L, "active-complete", LocalDateTime.of(2026, 4, 29, 11, 0)),
                event(5002L, 12L, "retired-complete", LocalDateTime.of(2026, 4, 28, 18, 30))
        ));
        when(readMapper.selectElementsByIdsOrCodes(List.of(12L), List.of("retired-complete")))
                .thenReturn(List.of(
                        element(12L, "retired-complete", "archived", false, 3)
                ));
        LocalDateTime recomputeTime = LocalDateTime.of(2026, 4, 29, 11, 45);
        when(readMapper.selectLastRecomputeTime(77L, "poi", 101L)).thenReturn(recomputeTime);

        AdminUserProgressSummaryResponse summary = service.calculateSummary(77L, "poi", 101L, true);

        assertThat(summary.getRetiredCompletedWeight()).isEqualTo(3);
        assertThat(summary.getRetiredCompletedCount()).isEqualTo(1);
        assertThat(summary.getLastRecomputeTime()).isEqualTo(recomputeTime);
    }

    private AdminUserProgressReadMapper.ProgressElementRow element(
            Long elementId,
            String elementCode,
            String status,
            boolean includeInExploration,
            int weightValue) {
        return new AdminUserProgressReadMapper.ProgressElementRow(
                elementId,
                elementCode,
                "progress",
                "poi",
                101L,
                "poi-101",
                1L,
                2L,
                101L,
                null,
                null,
                null,
                "zh",
                null,
                "zht",
                null,
                "core",
                weightValue,
                includeInExploration,
                status
        );
    }

    private AdminUserProgressReadMapper.ProgressEventRow event(
            Long eventId,
            Long elementId,
            String elementCode,
            LocalDateTime occurredAt) {
        return new AdminUserProgressReadMapper.ProgressEventRow(
                eventId,
                77L,
                elementId,
                elementCode,
                "completed",
                occurredAt
        );
    }
}
