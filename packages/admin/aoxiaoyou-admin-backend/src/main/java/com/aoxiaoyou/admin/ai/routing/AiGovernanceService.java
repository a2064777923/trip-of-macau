package com.aoxiaoyou.admin.ai.routing;

import com.aoxiaoyou.admin.entity.AiQuotaRule;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AiGovernanceService {

    private final ConcurrentHashMap<String, AtomicInteger> inFlightCounters = new ConcurrentHashMap<>();

    public GovernanceLease acquire(String scopeKey, List<AiQuotaRule> rules, long requestCountInWindow, Integer estimatedTokens) {
        AtomicInteger counter = inFlightCounters.computeIfAbsent(scopeKey, ignored -> new AtomicInteger(0));
        int nextInFlight = counter.incrementAndGet();

        for (AiQuotaRule rule : rules) {
            if (rule == null || !"enabled".equalsIgnoreCase(rule.getStatus())) {
                continue;
            }
            if (rule.getRequestLimit() != null && requestCountInWindow >= rule.getRequestLimit()) {
                counter.decrementAndGet();
                return GovernanceLease.blocked("已觸發請求配額限制");
            }
            if (rule.getTokenLimit() != null && estimatedTokens != null && estimatedTokens > rule.getTokenLimit()) {
                counter.decrementAndGet();
                return GovernanceLease.blocked("已超出單次 Token 上限");
            }
            if (rule.getSuspiciousConcurrencyThreshold() != null && nextInFlight > rule.getSuspiciousConcurrencyThreshold()) {
                counter.decrementAndGet();
                return GovernanceLease.blocked("已觸發可疑高併發保護");
            }
        }

        return new GovernanceLease(scopeKey, counter, true, null);
    }

    public record GovernanceLease(String scopeKey, AtomicInteger counter, boolean allowed, String blockedReason) implements AutoCloseable {

        public static GovernanceLease blocked(String reason) {
            return new GovernanceLease(null, null, false, reason);
        }

        @Override
        public void close() {
            if (counter != null) {
                int value = counter.decrementAndGet();
                if (value <= 0) {
                    counter.set(0);
                }
            }
        }
    }
}
