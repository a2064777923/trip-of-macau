---
phase: 32-dynamic-exploration-and-user-progress-model
plan: 01
subsystem: api
tags: [spring-boot, mybatis-plus, mysql, progress-engine, testing]
requires:
  - phase: 28-story-and-content-control-plane-completion
    provides: exploration_elements, user_exploration_events, user_exploration_state foundation tables
provides:
  - Canonical scope contract for POI and indoor exploration progress
  - Public weighted progress response with inactive completion comparison data
  - Admin progress calculator service backed by exploration elements and immutable events
affects: [32-02, 32-03, 32-05, 32-06, USER-02, LINK-03]
tech-stack:
  added: []
  patterns: [published denominator registry, immutable event parity, retired completion comparison]
key-files:
  created:
    - scripts/local/mysql/init/43-phase-32-progress-engine.sql
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/AdminUserProgressReadMapper.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserProgressCalculatorServiceImpl.java
  modified:
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/UserExplorationResponse.java
    - packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicExperienceServiceImplTest.java
    - packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserProgressCalculatorTest.java
key-decisions:
  - "Keep exploration_elements as the active denominator and use user_exploration_events as the immutable completion source on both public and admin paths."
  - "Resolve task, collectible, reward, and media scopes through canonical owner_type and owner_id tokens instead of new per-domain progress tables."
  - "Surface retired completions as comparison data while excluding them from active progressPercent."
patterns-established:
  - "Progress parity pattern: public and admin calculations both index immutable completion events and then split active versus retired elements."
  - "Scope resolution pattern: spatial scopes use direct columns, owner-backed scopes use canonical owner_type tokens."
requirements-completed: [USER-02, LINK-03]
duration: 18 min
completed: 2026-04-29
---

# Phase 32 Plan 01 Summary

**Canonical weighted progress parity for public and admin services using published exploration elements, immutable completion events, and retired-element comparison data**

## Performance

- **Duration:** 18 min
- **Started:** 2026-04-29T14:19:38+08:00
- **Completed:** 2026-04-29T14:37:34+08:00
- **Tasks:** 2
- **Files modified:** 13

## Accomplishments
- Added the Phase 32 SQL contract for new `poi_id`, `indoor_building_id`, and `indoor_floor_id` scope columns plus canonical owner-backed scope tokens.
- Refactored public exploration progress to support every required scope branch, richer element-level parity fields, and retired completion comparison semantics.
- Added an admin progress calculator service, mapper, and response DTOs that compute the same active percentage as the public path while exposing retired completion counts and weights.

## Task Commits

Each task was committed atomically:

1. **Task 1: Create Wave 0 weighted-progress tests and schema contract** - `ecc00fc` (`test`)
2. **Task 2: Implement canonical public/admin weighted progress parity** - `19b1d6a` (`feat`)

## Files Created/Modified
- `scripts/local/mysql/init/43-phase-32-progress-engine.sql` - idempotent schema contract for Phase 32 progress scopes and canonical owner tokens
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity/ExplorationElement.java` - public entity mapping for direct POI and indoor scope columns
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/UserExplorationResponse.java` - public parity fields for counts, inclusion flags, and source event metadata
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - scope-aware weighted progress calculation with retired-completion comparison logic
- `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicExperienceServiceImplTest.java` - targeted regression coverage for scope expansion and inactive completion semantics
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/ExplorationElement.java` - admin entity mapping for direct POI and indoor scope columns
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminUserProgressSummaryResponse.java` - admin summary card parity contract
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminUserProgressBreakdownResponse.java` - admin drill-down contract for active and retired elements
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/AdminUserProgressReadMapper.java` - annotation-based progress read model over exploration elements, immutable events, and cache timestamps
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminUserProgressCalculatorService.java` - reusable admin progress calculator interface
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserProgressCalculatorServiceImpl.java` - canonical admin parity calculator with `includeInactiveElements` comparison behavior
- `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserProgressCalculatorTest.java` - admin/public parity regression coverage

## Decisions Made

- Kept `user_exploration_state` read-only in this plan and used it only for `lastRecomputeTime`, leaving cache rebuild ownership to later Phase 32 work.
- Implemented retired completion lookup as a second-pass element fetch only for event keys not already covered by the active denominator, which keeps the comparison path bounded and deterministic.
- Used a new admin read mapper local to this plan instead of expanding the plan boundary with extra event entities or XML mapper files.

## Deviations from Plan

### Execution Deviations

**1. Verification fallback for the public module**
- **Issue:** `mvn -q -Dtest=PublicExperienceServiceImplTest test -f packages/server/pom.xml` is blocked by unrelated pre-existing server test-compile failures in `PublicCatalogServiceImplCarryoverTest`, `CatalogFoundationServiceImplTest`, and `PublicRewardDomainServiceTest`.
- **Fallback:** Verified public behavior with `mvn -q -DskipTests compile -f packages/server/pom.xml`, a scoped manual compile of `PublicExperienceServiceImplTest.java` into `target/test-classes`, and `mvn --% -q -Dtest=PublicExperienceServiceImplTest surefire:test -f packages/server/pom.xml`.
- **Impact:** Plan-owned public code and test passed, but the exact plan command remains red until those unrelated server tests are repaired outside `32-01` ownership.

**2. `gsd-sdk` was unavailable in the shell**
- **Issue:** The executor-side `gsd-sdk query init.execute-phase` command was not on `PATH`.
- **Fallback:** Loaded plan context directly from the checked-in `.planning` files and executed the plan without STATE/ROADMAP mutation, matching the user’s explicit constraint.
- **Impact:** No code impact. Execution metadata stayed local to this summary and the task commits.

## Issues Encountered

- The server module currently has unrelated dirty test sources that fail the normal Maven `test` lifecycle before scoped verification reaches `PublicExperienceServiceImplTest`.
- MyBatis-Plus lambda wrapper internals are awkward to inspect in plain unit tests, so the final public test harness uses deterministic call sequencing rather than wrapper-SQL introspection.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- Phase 32 now has a reusable admin/public progress engine foundation for drill-down, recompute, and audit work in later plans.
- Later Phase 32 plans can build on `AdminUserProgressCalculatorService` without redefining denominator semantics or retired-completion behavior.
- The pre-existing unrelated server test-compile failures should be addressed separately if the repository needs fully clean module-wide `mvn test` runs again.

## Known Stubs

None - no intentional UI/data stubs were introduced in the plan-owned implementation.

## Self-Check: PASSED

- Verified summary file exists at `.planning/phases/32-dynamic-exploration-and-user-progress-model/32-01-SUMMARY.md`
- Verified task commits `ecc00fc` and `19b1d6a` exist in `git log --oneline --all`

---
*Phase: 32-dynamic-exploration-and-user-progress-model*
*Completed: 2026-04-29*
