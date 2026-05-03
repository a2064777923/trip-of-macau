---
phase: 40-acceptance-cost-visibility-and-release-readiness
plan: 40-02
subsystem: admin-ai-observability
tags: [admin-ui, ai, observability, cost, safe-summary]

requires:
  - phase: 40-01
    provides: release-readiness smoke wrapper
provides:
  - safe admin AI generation job summaries and cost labels
  - AI request log filters and safe output summaries
  - Traditional Chinese monitoring and cost UI
  - smoke coverage for AI observability API shape
affects: [phase-40, acc-03, ai-capability-center]

tech-stack:
  added: []
  patterns:
    - safe summary fields for default admin observability views
    - advanced diagnostics behind collapsed UI
    - smoke checks that assert API shape without writing payloads

key-files:
  created: []
  modified:
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminAiGenerationJobResponse.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/response/AdminAiLogResponse.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminAiService.java
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java
    - packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/ObservabilityPage.tsx
    - packages/admin/aoxiaoyou-admin-ui/src/services/api.ts
    - scripts/local/smoke-phase-40-release-readiness.ps1
    - .planning/phases/40-acceptance-cost-visibility-and-release-readiness/40-SMOKE-REPORT.md

requirements-completed: [ACC-03]

duration: 75 min
completed: 2026-05-03
---

# Phase 40 Plan 02 Summary

**Admin AI monitoring now shows generation jobs, request logs, safe summaries, asset linkage, and labeled cost data without default raw diagnostics.**

## Accomplishments

- Extended admin AI job/log DTOs with `costLabel`, `costType`, safe summary fields, model code, candidate count, and latest asset metadata.
- Added request log filters for request type, owner, and model/inventory code while preserving role-aware generation job and request-log access.
- Rebuilt `/ai/observability` as `監控與成本` with summary cards, labeled filters, `生成作業` and `請求日誌` tables, ellipsized long values, and a `查看詳情` drawer.
- Moved raw diagnostic JSON into a collapsed `進階診斷` section and kept default table/detail content on safe summary fields.
- Extended the Phase 40 smoke wrapper to call AI overview, generation jobs, and logs, then assert safe field shape without writing payloads or tokens into the report.

## Verification

| Command | Result |
| --- | --- |
| `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | Passed |
| `cd packages/admin/aoxiaoyou-admin-ui; npm run build` | Passed |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-40-release-readiness.ps1 -Quick` | Passed |
| Phase 40 smoke report sensitive-token pattern scan | Passed; no credential, provider secret, raw prompt, script, or local path literals were found |

## Evidence Notes

- The current admin backend was restarted on `8081` from the updated `target/classes` before the final smoke run, because the previous long-running local process was still serving the old DTO shape.
- The final smoke report records `admin ai / observability API / PASS` with `safe fields checked`.
- Code review tightened AI request-log and overview history visibility so non-super admins default to their own rows unless the platform setting explicitly allows global operator history.
- Admin UI build still reports only the existing large chunk warning from Vite; no TypeScript or build failure was introduced.

## Deviations

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Restarted stale local admin backend for API shape verification**

- **Found during:** Task `40-02-03` smoke coverage.
- **Issue:** The first quick smoke hit an old `8081` Java process started before the DTO changes, so generation job rows lacked `costType`.
- **Fix:** Stopped the stale process and started the admin backend from the current code with the `local` profile.
- **Files modified:** None.
- **Verification:** Re-ran quick smoke successfully with AI observability API safe field checks.

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** Verification now tests the current code instead of a stale local process. No scope expansion.

### Code Review Fixes

**1. [P1] Scoped AI request-log history by role and platform setting**

- **Found during:** Phase 40 code review gate.
- **Issue:** AI request logs and overview history could expose global request rows to non-super admins.
- **Fix:** Passed current admin context into log pagination and reused the platform `allow-operator-global-history` setting so non-super admins default to their own logs.
- **Files modified:** `AdminAiController.java`, `AdminAiService.java`, `AdminAiServiceImpl.java`.
- **Verification:** Recompiled admin backend, restarted `8081`, and re-ran Phase 40 quick smoke successfully.

## Issues Encountered

- Maven startup with a direct `-Dspring-boot.run.profiles=local` command was misparsed by PowerShell during background launch; using the local environment/profile already loaded by the app resolved startup.

## User Setup Required

None for the code change. Local live smoke continues to require existing admin service credentials or `PHASE40_ADMIN_BEARER_TOKEN` when defaults are not valid.

## Next Phase Readiness

Proceed to `40-03` to create the WeChat/manual UAT checklist, final acceptance report, verification closure, and requirements/state updates.

---
*Phase: 40-acceptance-cost-visibility-and-release-readiness*
*Completed: 2026-05-03*
