---
phase: 17-indoor-runtime-evaluation-and-mini-program-alignment
plan: 01
subsystem: api
tags: [spring-boot, mybatis-plus, mysql, indoor-runtime, public-api]
requires:
  - phase: 15-indoor-interaction-rule-authoring
    provides: canonical indoor node and behavior authoring model
  - phase: 16-indoor-rule-workbench-and-governance
    provides: overlay geometry and governed runtime support levels
provides:
  - public indoor runtime snapshot endpoint
  - authoritative indoor interaction endpoint
  - dedicated indoor runtime audit log table
  - Lisboa Phase 17 runtime fixture promotion
affects: [packages/client indoor runtime, smoke verification, wechat indoor experience]
tech-stack:
  added: [none]
  patterns: [additive runtime projection, deterministic blocked-reason contract, dedicated runtime audit logging]
key-files:
  created:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/IndoorRuntimeInteractionRequest.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/IndoorRuntimeFloorResponse.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/IndoorRuntimeInteractionResponse.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/IndoorRuntimeLog.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/IndoorRuntimeLogMapper.java
    - scripts/local/mysql/init/29-phase-17-public-indoor-runtime.sql
    - scripts/local/mysql/init/30-phase-17-runtime-fixture-promotion.sql
  modified:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/IndoorController.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicIndoorRuntimeServiceImpl.java
    - packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicIndoorRuntimeServiceTest.java
    - packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicIndoorRuntimeInteractionServiceTest.java
key-decisions:
  - "Kept the admin-authored indoor model as the only write contract and projected it additively into public runtime DTOs."
  - "Blocked reasons are explicit lowercase codes so the mini-program can degrade safely without guessing."
  - "Guarded or stateful indoor interactions write into `indoor_runtime_logs` instead of overloading unrelated trigger logs."
patterns-established:
  - "Public indoor runtime reads published floor, node, and behavior rows and normalizes support into `supported`, `requiresAuth`, and `blockedReason`."
  - "Runtime execution only supports the Phase 17 subset while still surfacing unsupported authored categories for visibility."
requirements-completed: [RULE-03]
duration: multi-session
completed: 2026-04-17
---

# Phase 17 Plan 01 Summary

**Additive public indoor runtime APIs now project authored floor behaviors, evaluate supported interactions, and persist authoritative runtime audit logs.**

## Accomplishments

- Added `GET /api/v1/indoor/floors/{floorId}/runtime` without breaking the existing static indoor reads.
- Added `POST /api/v1/indoor/runtime/interactions` with deterministic behavior matching, auth gating, and blocked-reason responses.
- Added `indoor_runtime_logs` plus fixture-promotion SQL so Lisboa showcase behaviors can run against live local data.
- Added focused backend tests covering runtime projection and interaction evaluation.

## Files Created Or Modified

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/IndoorController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/PublicIndoorRuntimeService.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicIndoorRuntimeServiceImpl.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/IndoorNodeBehavior.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/IndoorRuntimeLog.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/IndoorNodeBehaviorMapper.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/IndoorRuntimeLogMapper.java`
- `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicIndoorRuntimeServiceTest.java`
- `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicIndoorRuntimeInteractionServiceTest.java`
- `scripts/local/mysql/init/29-phase-17-public-indoor-runtime.sql`
- `scripts/local/mysql/init/30-phase-17-runtime-fixture-promotion.sql`

## Verification

- `mvn -q "-Dtest=PublicIndoorRuntimeServiceTest,PublicIndoorRuntimeInteractionServiceTest" test` in `packages/server`
- Live local schema gap fixed by applying:
  - `scripts/local/mysql/init/28-phase-16-behavior-overlay-geometry.sql`
  - `scripts/local/mysql/init/29-phase-17-public-indoor-runtime.sql`
  - `scripts/local/mysql/init/30-phase-17-runtime-fixture-promotion.sql`
- `GET http://127.0.0.1:8080/api/v1/indoor/floors/12/runtime?locale=zh-Hant`

## Notes

- Local MySQL was initially behind the authored schema and caused a live `500` on `overlay_geometry_json`; Phase 17 execution closed that migration gap.
- The Lisboa `1F` showcase now exposes:
  - `night-market-schedule-overlay`
  - `royal-palace-dwell-reveal`
  - `zipcity-guiding-path`
