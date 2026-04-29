package com.aoxiaoyou.admin.service.support;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmptyRouteTraceSourceAdapter implements RouteTraceSourceAdapter {

    @Override
    public RouteTraceSnapshot loadRouteTrace(Long userId, LocalDateTime from, LocalDateTime to) {
        return new RouteTraceSnapshot(
                "unavailable",
                "No verified route-trace storage is available in the current Phase 32 paths.",
                List.of()
        );
    }
}
