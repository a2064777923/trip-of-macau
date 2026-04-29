---
phase: 32-dynamic-exploration-and-user-progress-model
plan: 02
subsystem: api
tags: [spring-boot, mybatis-plus, mysql, story-session, testing]
requires:
  - phase: 32-dynamic-exploration-and-user-progress-model
    provides: public exploration event idempotency and scope-expanded runtime foundation in PublicExperienceServiceImpl
provides:
  - Durable `user_storyline_sessions` schema contract for public story-mode lifecycle state
  - Persistent public start/event/exit session handling separated from immutable exploration facts
  - Expanded storyline session response fields for later admin timeline and workbench consumption
affects: [32-05, USER-03, T32-02]
tech-stack:
  added: []
  patterns: [durable runtime session rows, immutable event log plus mutable session summary]
key-files:
  created:
    - scripts/local/mysql/init/44-phase-32-story-sessions-and-timeline.sql
    - packages/server/src/test/java/com/aoxiaoyou/tripofmacau/StorylineSessionPersistenceTest.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserStorylineSession.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/UserStorylineSessionMapper.java
  modified:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java
key-decisions:
  - "Persist story-mode sessions in a dedicated MySQL table keyed by durable session_id strings so public exploration events can reference stable IDs without changing the immutable event log contract."
  - "Keep PublicExperienceServiceImpl constructor-compatible by setter-injecting only the new session mapper, because the main worktree already contains dirty constructor-based tests outside 32-02 ownership."
  - "Treat session exit as a session-row state transition that clears temporary_step_state_json and marks exitClearedTemporaryState while leaving permanent exploration facts untouched."
patterns-established:
  - "Dual-path persistence pattern: user_exploration_events stays immutable while user_storyline_sessions stores mutable story-mode lifecycle state."
  - "Session snapshot pattern: storyline event ingestion updates event_count, last_event_at, current_chapter_id, and compact temporary JSON only when a durable session row exists."
requirements-completed: [USER-03]
duration: 6 min
completed: 2026-04-29
---

# Phase 32 Plan 02: Durable Storyline Session Persistence Summary

**Durable public story-session rows in MySQL with persistent start/event/exit lifecycle fields and immutable exploration-event separation**

## Performance

- **Duration:** 6 min
- **Started:** 2026-04-29T14:47:51+08:00
- **Completed:** 2026-04-29T14:53:52+08:00
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- Added the Phase 32 SQL contract for `user_storyline_sessions` with utf8mb4-safe lifecycle fields, exit-clearing state, and admin-readable indexes.
- Added Wave 0 coverage for durable session start, session-bound event counting, and non-destructive session exit semantics.
- Updated the public runtime so storyline session start/event/exit flows persist durable session state while leaving permanent exploration events untouched.

## Task Commits

Each task was committed atomically:

1. **Task 1: Create Wave 0 durable-session tests and schema contract** - `205448d` (`test`)
2. **Task 2: Implement durable public session persistence** - `28af7aa` (`feat`)

## Files Created/Modified
- `scripts/local/mysql/init/44-phase-32-story-sessions-and-timeline.sql` - utf8mb4-safe durable session schema contract for Phase 32 story-mode lifecycle state
- `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/StorylineSessionPersistenceTest.java` - regression coverage for session creation, event counting, and non-destructive exit behavior
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/UserStorylineSession.java` - MyBatis entity for durable public story sessions
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/UserStorylineSessionMapper.java` - persistence mapper for session lifecycle reads and updates
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java` - expanded public session contract with chapter, timestamps, counts, and exit-cleared state
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - durable session persistence for start/event/exit flows alongside immutable exploration event writes

## Decisions Made

- Reused the existing public `storyline_session_id` string contract as the durable table key instead of introducing a separate numeric session identifier.
- Returned an existing active session row for repeated start calls on the same user/storyline, while keeping exited sessions as historical rows rather than mutating permanent exploration facts.
- Stored a compact temporary session snapshot JSON on each session-bound event so later admin timeline/workbench work can inspect chapter/event state without inferring it from permanent event history alone.

## Deviations from Plan

### Execution Deviations

**1. Verification fallback for the public server module**
- **Issue:** The exact plan command `mvn -q -Dtest=StorylineSessionPersistenceTest test -f packages/server/pom.xml` is still blocked by unrelated pre-existing test-compile failures in `PublicCatalogServiceImplCarryoverTest`, `CatalogFoundationServiceImplTest`, and `PublicRewardDomainServiceTest`.
- **Fallback:** Verified the plan-owned code with `mvn -q -DskipTests compile -f packages/server/pom.xml`, manual compilation of `StorylineSessionPersistenceTest.java` against the module test classpath, and `mvn -q -Dtest=StorylineSessionPersistenceTest surefire:test -f packages/server/pom.xml`.
- **Impact:** Plan-owned session code and the new regression test passed, but the exact Maven lifecycle command remains red until those unrelated dirty server tests are repaired outside `32-02` ownership.

---

**Total deviations:** 0 auto-fixed, 1 execution fallback
**Impact on plan:** No scope creep in plan-owned code. The only deviation was a verification-path workaround around unrelated test-compile failures already present in the main worktree.

## Issues Encountered

- PowerShell needed the fallback verification split into separate commands because Maven `dependency:build-classpath` arguments and stop-parsing syntax were brittle in one-line form.
- The public server module still contains unrelated constructor-mismatch test failures outside this plan, so exact `mvn test` targeting cannot be treated as a clean signal yet.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Plan 32-05 can now read `user_storyline_sessions` directly for admin timeline/workbench session visibility.
- Public runtime session responses now expose `currentChapterId`, `startedAt`, `lastEventAt`, `exitedAt`, `eventCount`, and `exitClearedTemporaryState`.
- The unrelated server test-constructor failures should still be repaired separately if later plans need the exact Maven `test` lifecycle command to pass without the scoped fallback.

## Known Stubs

None - no intentional UI or data stubs were introduced in 32-02.

## Self-Check: PASSED

- Verified summary file exists at `.planning/phases/32-dynamic-exploration-and-user-progress-model/32-02-SUMMARY.md`
- Verified task commits `205448d` and `28af7aa` exist in `git log --oneline --all`

---
*Phase: 32-dynamic-exploration-and-user-progress-model*
*Completed: 2026-04-29*
