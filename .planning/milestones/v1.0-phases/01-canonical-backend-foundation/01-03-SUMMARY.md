---
phase: 01-canonical-backend-foundation
plan: 03
subsystem: api
tags: [spring-boot, mybatis-plus, entities, tests]
requires: [phase-01-contract-and-enum-foundation]
provides:
  - public-backend WebConfig that boots without admin-only dependencies
  - canonical entity, mapper, and service scaffolding for Phase 1 domains
  - targeted Spring Boot foundation context coverage
affects: [phase-03-public-read-apis-cutover, phase-04-public-progress-and-gameplay-writes]
tech-stack:
  added: []
  patterns: [public backend remains free of admin-only beans, canonical enum conversion through helper methods]
key-files:
  created:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/City.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/AppRuntimeSetting.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/TipArticle.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/Reward.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/Stamp.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/CityMapper.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/AppRuntimeSettingMapper.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/TipArticleMapper.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/RewardMapper.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/StampMapper.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/CatalogFoundationService.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/RuntimeSettingsService.java
    - packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicFoundationContextTest.java
  modified:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/config/WebConfig.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/StatsController.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/TestAccount.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/TestAccountServiceImpl.java
    - packages/server/src/test/java/com/aoxiaoyou/tripofmacau/TripOfMacauServerApplicationTests.java
key-decisions:
  - "Keep Phase 1 focused on persistence and boot scaffolding rather than prematurely exposing incomplete public DTOs/controllers."
  - "Fix brownfield compile blockers inline when they prevent the public foundation from compiling or booting."
patterns-established:
  - "Foundation entities store persisted string codes and expose enum conversion helpers at the domain edge."
  - "Context-smoke tests should run under the same local profile used by real runtime verification."
requirements-completed: [DATA-01, DATA-03]
duration: 15min
completed: 2026-04-12
---

# Phase 1: Plan 03 Summary

**Public-backend boot cleanup, canonical MyBatis scaffolding for new domains, and a local-profile Spring context smoke test**

## Performance

- **Duration:** 15 min
- **Started:** 2026-04-12T03:31:00Z
- **Completed:** 2026-04-12T03:46:00Z
- **Tasks:** 3
- **Files modified:** 18

## Accomplishments
- Removed the public backend's dependency on the missing `AdminAuthInterceptor` by simplifying `WebConfig` to public-only CORS behavior.
- Added Phase 1 canonical entities, mappers, and service interfaces for cities, runtime settings, tips, rewards, and stamps.
- Added `PublicFoundationContextTest` and aligned the existing application test to the `local` profile so the foundation boot path is actually exercised.

## Task Commits

Atomic task commits were intentionally skipped because the repository already contained unrelated in-progress user changes and Phase 1 was executed as a single working-tree pass.

## Files Created/Modified

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/config/WebConfig.java` - Public-only Spring MVC configuration with permissive CORS and no admin interceptor dependency.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/City.java` - Canonical city entity aligned to the new schema and enum semantics.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/AppRuntimeSetting.java` - Runtime setting entity with locale and publish-state helpers.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/TipArticle.java` - Tip/article foundation entity for later public and admin work.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/Reward.java` - Reward catalog foundation entity.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/Stamp.java` - Stamp definition foundation entity.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper/*.java` - MyBatis-Plus mappers for the new canonical entities.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/CatalogFoundationService.java` - Published catalog foundation service interface.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/RuntimeSettingsService.java` - Runtime-settings service interface for grouped lookups.
- `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicFoundationContextTest.java` - Spring Boot smoke coverage for foundation beans/mappers.

## Decisions Made

- The public backend foundation stops at entities/mappers/service interfaces in Phase 1 so later API phases can wire behavior incrementally without mixing scaffolding and endpoint cutover.
- Local-profile tests are the right contract for this phase because they validate the same datasource/profile assumptions used by the actual runtime helpers and smoke harness.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Imported missing `RequestParam` in `StatsController`**
- **Found during:** Plan verification build
- **Issue:** Existing brownfield code did not compile once the Phase 1 server test path was exercised.
- **Fix:** Added the missing Spring MVC import.
- **Files modified:** `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/StatsController.java`
- **Verification:** `mvn -q -f packages/server/pom.xml test "-Dtest=TripOfMacauServerApplicationTests,PublicFoundationContextTest"`
- **Committed in:** none (dirty worktree; no atomic commit created)

**2. [Rule 3 - Blocking] Corrected `TestAccount` equality configuration**
- **Found during:** Plan verification build
- **Issue:** `@EqualsAndHashCode(callSuper = true)` was invalid because `TestAccount` does not extend `BaseEntity`.
- **Fix:** Replaced it with `@EqualsAndHashCode`.
- **Files modified:** `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/TestAccount.java`
- **Verification:** `mvn -q -f packages/server/pom.xml test "-Dtest=TripOfMacauServerApplicationTests,PublicFoundationContextTest"`
- **Committed in:** none (dirty worktree; no atomic commit created)

**3. [Rule 3 - Blocking] Replaced invalid `PageResponse` constructor usage**
- **Found during:** Plan verification build
- **Issue:** `TestAccountServiceImpl` was still constructing `PageResponse` with a signature that no longer exists.
- **Fix:** Switched to `PageResponse.of(page)`.
- **Files modified:** `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/TestAccountServiceImpl.java`
- **Verification:** `mvn -q -f packages/server/pom.xml test "-Dtest=TripOfMacauServerApplicationTests,PublicFoundationContextTest"`
- **Committed in:** none (dirty worktree; no atomic commit created)

**Total deviations:** 3 auto-fixed (3 blocking)
**Impact on plan:** All three fixes were required to get the real public-backend foundation compiling and testable; they did not expand scope beyond the Phase 1 boot path.

## Issues Encountered

- Existing brownfield compile regressions only surfaced once the new local-profile context test was added, so Plan 03 had to absorb those blockers to complete the foundation safely.

## User Setup Required

None - no external service configuration required.

## Verification

All checks passed.

- `node .codex/get-shit-done/bin/gsd-tools.cjs verify artifacts .planning/phases/01-canonical-backend-foundation/01-03-PLAN.md`
- `node .codex/get-shit-done/bin/gsd-tools.cjs verify key-links .planning/phases/01-canonical-backend-foundation/01-03-PLAN.md`
- `mvn -q -f packages/server/pom.xml test "-Dtest=TripOfMacauServerApplicationTests,PublicFoundationContextTest"` with JDK 17

## Next Phase Readiness

- Phase 3 can now add real public services/controllers on top of canonical entities and mapper interfaces.
- Later read/write work can rely on an automated context smoke test to catch boot regressions early.

---
*Phase: 01-canonical-backend-foundation*
*Completed: 2026-04-12*
