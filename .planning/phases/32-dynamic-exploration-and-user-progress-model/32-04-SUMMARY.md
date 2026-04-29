---
phase: 32-dynamic-exploration-and-user-progress-model
plan: 32-04
subsystem: admin-ui
tags: [react, vite, mysql, smoke-test, progress-workbench]

requires:
  - phase: 32-02
    provides: durable storyline sessions for traveler workbench display
  - phase: 32-05
    provides: admin traveler progress read model and timeline endpoints
  - phase: 32-06
    provides: admin-only progress operations transport endpoints
provides:
  - Route-driven Traditional Chinese traveler progress workbench
  - Phase 32 traveler progress fixtures and local smoke harness
  - Live verification across public progress, admin workbench, repair preview/confirm, and audit visibility
affects: [user-management, admin-progress, operations-verification]

tech-stack:
  added: []
  patterns:
    - Route-driven admin detail workbench instead of drawer-only user detail
    - Preview-before-confirm progress repair and recompute UI
    - Local smoke verification with env-backed credentials and utf8mb4 fixture imports

key-files:
  created:
    - packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/UserProgressWorkbench.tsx
    - scripts/local/mysql/init/46-phase-32-traveler-progress-fixtures.sql
  modified:
    - packages/admin/aoxiaoyou-admin-ui/src/App.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/services/api.ts
    - packages/admin/aoxiaoyou-admin-ui/src/types/admin.ts
    - scripts/local/smoke-phase-32-user-progress.ps1
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/AdminTravelerProgressReadMapper.java

key-decisions:
  - "The traveler list now opens a dedicated workbench route at users/progress/:userId instead of a lightweight drawer."
  - "legacyProgressSnapshot is shown only as a compatibility card and never replaces weighted dynamic progress."
  - "The smoke script treats a non-success API envelope from the public repair probe as rejected, even when the public global error wrapper returns HTTP 200."

patterns-established:
  - "Operator workbenches should keep long JSON payloads collapsed behind detail controls."
  - "Admin repair and recompute operations must require preview hash plus confirmation text before mutation."

requirements-completed: [USER-01, USER-02, USER-03, USER-04, LINK-03]

duration: resumed
completed: 2026-04-29
---

# Phase 32-04: Traveler Progress Workbench Summary

**Traditional Chinese traveler progress workbench with seeded fixtures and live public/admin smoke coverage**

## Performance

- **Started:** resumed from prior partial execution
- **Completed:** 2026-04-29
- **Tasks:** 2
- **Files modified:** 8

## Accomplishments

- Added a route-driven `users/progress/:userId` operator workbench with the required sections: identity/preferences, dynamic progress, exploration element drill-down, storyline sessions, timeline, reward sources, repair/recompute, and audit records.
- Replaced the old user drawer entry with Traditional Chinese list copy and a clear `開啟旅客進度工作台` action.
- Seeded Phase 32 traveler progress fixtures and proved the flow with a local smoke script importing utf8mb4 SQL fixtures.
- Verified public/admin parity, retired element comparison, preview-before-confirm recompute, audit visibility, and public repair-path rejection.

## Task Commits

1. **Task 32-04-01: Seed traveler progress fixtures and smoke harness** - `0084322`
2. **Task 32-04-02: Add traveler progress workbench UI** - `a612427`

## Verification

- `mvn -q -Dtest=AdminUserTimelineServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` passed.
- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` passed.
- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` passed.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-32-user-progress.ps1` passed and printed `Phase 32 user progress smoke passed`.
- Pattern checks passed for all required workbench section titles, route wiring, API helpers, `legacyProgressSnapshot`, and removal of simplified user-list copy.

## Deviations from Plan

### Auto-fixed Issues

**1. Annotation SQL dynamic tag was sent literally to MySQL**

- **Found during:** Task 32-04-02 live smoke
- **Issue:** `AdminTravelerProgressReadMapper.selectRecentContextCounts` used MyBatis XML `<if>` fragments inside an `@Select` annotation. MyBatis did not parse them, causing `BadSqlGrammarException` during the admin progress workbench call.
- **Fix:** Replaced the XML fragments with annotation-safe nullable predicates: `AND (#{from} IS NULL OR ... >= #{from})`.
- **Files modified:** `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper/AdminTravelerProgressReadMapper.java`
- **Verification:** Admin backend compile passed, targeted service test passed, smoke reached the workbench and completed.
- **Committed in:** `a612427`

**2. Smoke expected a fixed seeded nickname after dev-bypass bootstrap**

- **Found during:** Task 32-04-02 live smoke
- **Issue:** The dev-bypass login updates the seeded traveler display name to the bootstrap nickname, so exact Chinese nickname assertion was brittle.
- **Fix:** Changed the smoke to assert a non-empty workbench identity nickname while preserving user id and locale assertions.
- **Files modified:** `scripts/local/smoke-phase-32-user-progress.ps1`
- **Verification:** Smoke passed after the assertion was updated.
- **Committed in:** `a612427`

**3. Public repair leak probe returned HTTP 200 with a non-success API envelope**

- **Found during:** Task 32-04-02 live smoke
- **Issue:** The public app wraps unmatched/error paths in a `code=5000` API envelope with HTTP 200, so HTTP-only rejection logic incorrectly treated it as success.
- **Fix:** The smoke now treats either non-2xx HTTP or a non-success API envelope as rejection and still asserts that no successful public repair data is exposed.
- **Files modified:** `scripts/local/smoke-phase-32-user-progress.ps1`
- **Verification:** Smoke passed and printed the required success line.
- **Committed in:** `a612427`

---

**Total deviations:** 3 auto-fixed
**Impact on plan:** All fixes were necessary to make the planned live verification meaningful; no public repair capability was added.

## Issues Encountered

- The smoke script still prints repeated `1` lines during MySQL import. This is noisy but not blocking; the required final success line is present.
- The public runtime currently returns a non-success API envelope rather than 404 for the guessed public repair path. The smoke records this as rejected behavior, not as an exposed admin API.

## User Setup Required

None - the smoke uses env-backed local admin credentials or bearer tokens and does not add tracked secrets.

## Next Phase Readiness

Phase 32 now has seeded progress data, public/admin parity smoke coverage, and an operator workbench that can support user progress operations and future operations-management screens.

---
*Phase: 32-dynamic-exploration-and-user-progress-model*
*Completed: 2026-04-29*
