package com.aoxiaoyou.admin.service.support;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface RouteTraceSourceAdapter {

    RouteTraceSnapshot loadRouteTrace(Long userId, LocalDateTime from, LocalDateTime to);

    record RouteTraceSnapshot(String sourceStatus, String message, List<RouteTracePoint> tracePoints) {
    }

    record RouteTracePoint(BigDecimal latitude, BigDecimal longitude, LocalDateTime occurredAt, String sourceType) {
    }
}
