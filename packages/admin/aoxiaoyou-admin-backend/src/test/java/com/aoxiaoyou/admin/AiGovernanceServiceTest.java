package com.aoxiaoyou.admin;

import com.aoxiaoyou.admin.ai.routing.AiGovernanceService;
import com.aoxiaoyou.admin.entity.AiQuotaRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AiGovernanceServiceTest {

    private final AiGovernanceService service = new AiGovernanceService();

    @Test
    void acquireBlocksWhenRequestLimitReached() {
        AiQuotaRule rule = new AiQuotaRule();
        rule.setStatus("enabled");
        rule.setRequestLimit(3);

        try (AiGovernanceService.GovernanceLease lease = service.acquire("phase18:requests", List.of(rule), 3, 10)) {
            assertThat(lease.allowed()).isFalse();
            assertThat(lease.blockedReason()).isNotBlank();
        }
    }

    @Test
    void acquireBlocksWhenTokenLimitExceeded() {
        AiQuotaRule rule = new AiQuotaRule();
        rule.setStatus("enabled");
        rule.setTokenLimit(100);

        try (AiGovernanceService.GovernanceLease lease = service.acquire("phase18:tokens", List.of(rule), 0, 120)) {
            assertThat(lease.allowed()).isFalse();
            assertThat(lease.blockedReason()).isNotBlank();
        }
    }

    @Test
    void acquireBlocksOnSuspiciousConcurrencyAndRecoversAfterClose() {
        AiQuotaRule rule = new AiQuotaRule();
        rule.setStatus("enabled");
        rule.setSuspiciousConcurrencyThreshold(1);

        try (AiGovernanceService.GovernanceLease first = service.acquire("phase18:concurrency", List.of(rule), 0, 10)) {
            assertThat(first.allowed()).isTrue();

            try (AiGovernanceService.GovernanceLease second = service.acquire("phase18:concurrency", List.of(rule), 0, 10)) {
                assertThat(second.allowed()).isFalse();
            }
        }

        try (AiGovernanceService.GovernanceLease third = service.acquire("phase18:concurrency", List.of(rule), 0, 10)) {
            assertThat(third.allowed()).isTrue();
        }
    }
}
