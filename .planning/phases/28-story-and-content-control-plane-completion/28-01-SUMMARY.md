---
phase: 28-story-and-content-control-plane-completion
plan: 01
subsystem: backend
tags: [spring-boot, mybatis-plus, mysql, experience-runtime, admin-auth]
requires: []
provides:
  - Versioned admin experience orchestration contract
  - Protected admin experience API surface
  - Published-only public experience runtime contract
  - Dynamic exploration event and weight vocabulary foundation
affects: [phase-29-poi-experience, phase-30-storyline-mode, phase-31-interaction-template-governance, phase-32-exploration-progress, phase-34-public-runtime]
tech-stack:
  added: []
  patterns:
    - Versioned JSON object validation before persistence
    - Shared canonical owner, override, trigger, status, and weight vocabularies
    - Public compiled runtime DTOs instead of admin-form JSON leakage
key-files:
  created:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminExperienceOrchestrationController.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminExperienceOrchestrationService.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/PublicExperienceService.java
  modified:
    - scripts/local/mysql/init/39-phase-28-experience-orchestration.sql
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminExperienceOrchestrationServiceImpl.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminExperienceRequest.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminExperienceResponse.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java
    - packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceRuntimeResponse.java
key-decisions:
  - "Experience orchestration JSON fields must be versioned objects with schemaVersion before save; reward rule ID arrays remain explicitly non-versioned."
  - "Public runtime exposes compiled published flows and steps, keeping storyline sessions separate from permanent exploration events."
  - "Exploration progress uses semantic weights such as tiny, small, medium, large, and core instead of fixed percentage increments."
patterns-established:
  - "Admin-owned experience routes remain under /api/admin/v1/experience and behind AdminAuthInterceptor."
  - "Public experience runtime compiles default flows plus story overrides into client-safe DTOs."
requirements-completed: [STORY-01, STORY-02, STORY-03, LINK-01, LINK-02]
duration: 58 min
completed: 2026-04-28
---

# Phase 28 Plan 01: Backend Experience Foundation Summary

**Versioned admin experience orchestration plus published-only public runtime DTOs for POI defaults, story overrides, and dynamic exploration events**

## Performance

- **Duration:** 58 min
- **Started:** 2026-04-28T14:32:53Z
- **Completed:** 2026-04-28T15:30:26Z
- **Tasks:** 3
- **Files modified:** 45

## Accomplishments

- Hardened the admin experience schema, DTOs, service validation, and governance vocabulary around `schemaVersion`, owner types, override modes, trigger types, statuses, and semantic exploration weights.
- Added the public experience runtime API surface and compiler for POI default flows, story chapter inherited flows, story mode config, override-applied steps, event ingestion, storyline sessions, and user exploration summaries.
- Confirmed `/api/admin/v1/experience/**` remains protected by the admin auth interceptor and rejects anonymous requests.

## Task Commits

Each task was committed atomically:

1. **Task 28-01-01: Admin experience save contract hardening** - `427bde7` (fix)
2. **Task 28-01-02: Public experience runtime contract alignment** - `a9d9435` (fix)
3. **Task 28-01-03: Experience API wiring completion** - `f882b2c` (fix)

**Plan metadata:** pending in docs commit

## Files Created/Modified

- `scripts/local/mysql/init/39-phase-28-experience-orchestration.sql` - Adds canonical experience orchestration schema, relation roles, story mode columns, seeded flow vocabulary, and utf8mb4-safe JSON payloads.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminExperienceOrchestrationController.java` - Exposes protected admin endpoints for templates, flows, bindings, overrides, exploration elements, and governance.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminExperienceOrchestrationServiceImpl.java` - Enforces canonical vocabularies, versioned JSON validation, override semantics, and governance hints.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` - Exposes public runtime endpoints for POI flows, story runtime, events, sessions, and exploration state.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - Compiles published-only default flows and story overrides into runtime DTOs and records idempotent exploration events.

## Decisions Made

The admin and public surfaces now share one vocabulary for owner types, binding roles, override modes, trigger types, status values, and exploration weight levels. This prevents later Phase 29-34 workbenches from inventing incompatible local payloads.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added missing experience API wiring files**

- **Found during:** Task 28-01-03 (admin auth and route verification)
- **Issue:** The committed service implementations imported new controller, service, entity, mapper, request, and response classes that were still untracked, which would break a clean checkout.
- **Fix:** Added the required admin and public experience controller/service/entity/mapper/DTO files in a scoped `28-01` commit.
- **Files modified:** Admin and public `Experience*`, `ExplorationElement*`, and `UserExplorationEvent*` wiring files.
- **Verification:** Both backend compile commands passed after the wiring commit.
- **Committed in:** `f882b2c`

---

**Total deviations:** 1 auto-fixed blocking issue.
**Impact on plan:** The fix was necessary for clean-checkout correctness and did not expand the Phase 28 backend contract beyond the planned files.

## Issues Encountered

- The authenticated admin smoke check could not run because `PHASE28_ADMIN_BEARER` was not set in the local environment. Anonymous access was verified as rejected; authenticated success remains a manual verification item once a valid admin bearer token is supplied.

## Verification

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` passed.
- `mvn -q -DskipTests compile -f packages/server/pom.xml` passed.
- Source checks found `schemaVersion`, `overrideMode`, `default_experience_flow`, `weightLevel`, and `story_mode_config_json` in the admin schema and service contract.
- Source checks found `published`, `compileSteps`, `storyModeConfig`, `client_event_id`, and `explorationWeightLevel` in the public runtime contract.
- Source checks confirmed `WebConfig` still applies `AdminAuthInterceptor` to `/api/admin/v1/**`, the interceptor still requires `Bearer`, and `AdminExperienceOrchestrationController` remains under `/api/admin/v1/experience`.
- Anonymous `GET http://127.0.0.1:8081/api/admin/v1/experience/templates?pageNum=1&pageSize=1` returned an auth failure as expected.

## User Setup Required

Set `PHASE28_ADMIN_BEARER` to a valid admin JWT before rerunning the authenticated admin smoke check.

## Next Phase Readiness

Wave 2 can now build the admin workbench on a committed backend contract. The only carryover is the authenticated admin smoke confirmation once a bearer token is available.

---
*Phase: 28-story-and-content-control-plane-completion*
*Completed: 2026-04-28*
