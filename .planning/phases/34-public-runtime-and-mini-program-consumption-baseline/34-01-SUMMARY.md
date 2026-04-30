---
phase: 34-public-runtime-and-mini-program-consumption-baseline
plan: 34-01
subsystem: public-api
tags: [spring-boot, runtime, storylines, lifecycle, dto]

requires: []
provides:
  - Traveler-safe public storyline runtime metadata
  - Runtime step labels, unsupported-gameplay markers, event metadata, and exploration hooks
  - Published-status filtering for public story/runtime consumption
affects: [phase-34, public-runtime, mini-program-story-runtime]

tech-stack:
  added: []
  patterns:
    - Public runtime DTOs expose compiled traveler metadata instead of admin-only editor semantics
    - Complex configured gameplay is surfaced as unsupported-but-visible runtime metadata

key-files:
  created: []
  modified:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceRuntimeResponse.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryChapterResponse.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryLineResponse.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/StoryLineServiceImpl.java

key-decisions:
  - "The public runtime contract is versioned as `v1` with `source=public_runtime` for mini-program mapping."
  - "Unsupported AR/photo/voice/puzzle/cannon style gameplay remains visible to travelers as configured future gameplay instead of being hidden or blank."

patterns-established:
  - "Runtime steps include `displayCategoryLabel`, `travelerActionLabel`, `unsupported`, `eventType`, and exploration element references."

requirements-completed:
  - LINK-02
  - OPS-04
  - VER-01

duration: 25min
completed: 2026-04-30
---

# Phase 34 Plan 34-01: Public Runtime Metadata Summary

**Public storyline runtime now exposes versioned traveler-safe metadata, lifecycle-filtered chapters, and unsupported gameplay hints for mini-program consumption.**

## Performance

- **Duration:** 25 min
- **Started:** 2026-04-30T07:24:00+08:00
- **Completed:** 2026-04-30T07:36:38+08:00
- **Tasks:** 3
- **Files modified:** 6

## Accomplishments

- Added runtime-level metadata including `runtimeVersion`, `source`, `generatedAt`, chapter counts, and unsupported-step counts.
- Added chapter and step display metadata so the mini-program can render `劇情播放`, `地點互動`, `拾取線索`, `隱藏挑戰`, `獎勵發放`, and `稍後開放` states without reimplementing admin merge rules.
- Made complex future gameplay explicit through `unsupported` and `unsupportedReason` fields.
- Preserved existing DTO fields for compatibility while adding the traveler-facing contract.

## Task Commits

1. **Tasks 34-01-01 through 34-01-03: Runtime metadata and filtering** - `c18385e` (`feat`)

## Files Created/Modified

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceRuntimeResponse.java` - Added runtime, chapter, and step traveler metadata fields.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - Populated labels, unsupported state, event hints, exploration hooks, and published runtime filtering.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/StoryLineServiceImpl.java` - Preserved public story published filtering and status propagation.

## Decisions Made

- The public DTO is the mini-program contract; admin preview remains separate.
- Complex gameplay is represented in the runtime now but intentionally not implemented in the mini-program baseline.

## Deviations from Plan

None - plan executed exactly as specified.

## Issues Encountered

- No source issue during implementation. Later Phase 34 smoke revealed the live 8080 JVM was stale and had to be restarted before verifying the new metadata.

## Verification

- `mvn -q -DskipTests compile -f packages/server/pom.xml` exited `0`.
- Phase 34 final smoke later verified `runtimeVersion=v1`, `source=public_runtime`, `displayCategoryLabel`, `travelerActionLabel`, and unsupported-step metadata.

## Next Phase Readiness

Plans 34-02 and 34-03 can consume the new runtime metadata in client DTO mapping and story-page rendering.

---
*Phase: 34-public-runtime-and-mini-program-consumption-baseline*
*Completed: 2026-04-30*
