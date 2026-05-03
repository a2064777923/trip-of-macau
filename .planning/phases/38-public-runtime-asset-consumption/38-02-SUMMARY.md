---
phase: 38-public-runtime-asset-consumption
plan: 02
subsystem: api
tags: [spring-boot, public-runtime, event-ingestion, story-sessions]
requires:
  - phase: 38-01
    provides: traveler-safe runtime DTO foundation
provides:
  - Allowlisted public story event contract
  - JSON object and payload size validation
  - Duplicate client event response metadata
  - Idempotent storyline session exit behavior
affects: [phase-39-mini-program-story-consumption, public-runtime-events, exploration-progress]
tech-stack:
  added: []
  patterns: [allowlisted-event-ingestion, idempotent-client-event-writes, retry-safe-session-exit]
key-files:
  created:
    - packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicExperienceEventServiceTest.java
  modified:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/ExperienceEventRequest.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceEventResponse.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java
key-decisions:
  - "Public event writes accept only Phase 39 baseline story event types plus chapter_open compatibility."
  - "payloadJson must be a bounded JSON object to avoid arbitrary large or malformed persisted client payloads."
  - "Session exit retries return duplicateExit metadata instead of throwing or deleting permanent exploration events."
patterns-established:
  - "Client retry semantics are visible in public responses through duplicate and duplicateExit flags."
  - "Reward-acquired events are recorded as baseline events only and do not grant arbitrary inventory from client payloads."
requirements-completed: [RUN-03]
duration: 35 min
completed: 2026-05-03
---

# Phase 38 Plan 02: Public Story Event Ingestion Summary

**Public story events are now allowlisted, JSON-bounded, idempotent, and session-exit retry safe.**

## Performance

- **Duration:** 35 min
- **Started:** 2026-05-03T06:25:00Z
- **Completed:** 2026-05-03T07:00:00Z
- **Tasks:** 3
- **Files modified:** 5

## Accomplishments

- Documented and enforced the public story event allowlist: story opened, chapter started/opened, content viewed, media completed, pickup interacted, task completed, reward acquired, unsupported viewed, and session exit.
- Added duplicate, acceptedAt, currentChapterId, and message fields to event responses for mini-program retry handling.
- Added duplicateExit and message fields to storyline session responses.
- Added payload validation requiring JSON objects and rejecting payloads over 8192 UTF-8 bytes.
- Made session exit idempotent so a second exit request returns success metadata and does not touch permanent exploration events.
- Added focused tests for allowlist acceptance, rejection, invalid/oversized payloads, duplicate client events, media completion chapter updates, reward events, and session exit idempotency.

## Task Commits

1. **Event ingestion hardening** - `b9daebe` (`feat`)
2. **Focused event tests** - `a17ac3f` (`test`)

## Files Created/Modified

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/ExperienceEventRequest.java` - documents supported event types.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceEventResponse.java` - adds retry/current chapter metadata.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java` - adds duplicate exit metadata.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - allowlist, payload validation, duplicate metadata, idempotent exit.
- `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicExperienceEventServiceTest.java` - focused ingestion tests.

## Decisions Made

- `chapter_open` remains accepted for Phase 34 smoke compatibility while new Phase 39 events use explicit names.
- Payloads are stored only after parsing as JSON object; arrays/scalars and invalid JSON are rejected.
- `reward_acquired` is treated as a recorded baseline event, not an authority to grant inventory directly from client JSON.

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

Plan 38-03 can now add smoke verification covering runtime asset privacy plus authenticated event idempotency/session exit behavior.

---
*Phase: 38-public-runtime-asset-consumption*
*Completed: 2026-05-03*
