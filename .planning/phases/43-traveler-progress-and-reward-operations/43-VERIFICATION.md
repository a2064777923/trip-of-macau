# Phase 43 Verification

## Requirement Evidence

| Requirement | Evidence | Status |
| --- | --- | --- |
| OPS-01 | Admin workbench smoke loaded `GET /api/admin/v1/users/{userId}/progress-workbench`, reward state, progress breakdown, audits, and rule trace for traveler `userId=320048` without direct DB inspection. | PASS |
| OPS-02 | Smoke exercised timeline filters for `eventTypes`, `storylineId`, `chapterId`, `poiId`, `mapScopeType/mapScopeId`, `status`, `rewardType`, and `from/to`; returned rows were schema-checked against active filters. | PASS |
| OPS-03 | Smoke previewed and applied `ANNOTATE_ISSUE`, verified an audit row, applied idempotent `RESEND_REWARD`, and confirmed support actions use confirmation token/hash and audit trail. | PASS |
| OPS-04 | Reward rule trace endpoint returned structured `traceStatus=missing_link` with explanatory missing links; support-granted reward became visible in both admin and public reward reads. | PASS |

## Commands

| Command | Status | Notes |
| --- | --- | --- |
| `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | PASS | Admin backend compiled successfully. |
| `mvn -q -DskipTests compile -f packages/server/pom.xml` | PASS | Public backend compiled successfully; included because Phase 43 public/admin consistency depends on public reward reads. |
| `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` | PASS | TypeScript check passed. |
| `cd packages/admin/aoxiaoyou-admin-ui; npm run build` | PASS | Production build passed; Vite reported only the existing large chunk warning. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-43-traveler-ops.ps1 -Quick` | PASS | Preview-only smoke passed against local admin `8081` and public `8080`. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-43-traveler-ops.ps1 -ApplySafeAnnotation` | PASS | Standard acceptance smoke applied migration, safe annotation, audit verification, idempotent reward resend, and public/admin reward consistency. |

## Smoke Evidence

Latest UAT report: `.planning/phases/43-traveler-progress-and-reward-operations/43-UAT.md`

Key rows:

| Check | Evidence |
| --- | --- |
| Schema migration | `Phase 43 migration apply/verify` PASS. |
| Admin support workbench | `workbench`, `progress breakdown`, `reward state`, `reward rule trace`, and `audit listing` PASS. |
| Timeline filters | All required OPS-02 filters PASS, including `rewardType` with one returned conforming row and time range with five conforming rows. |
| Safe support action | `preview operation`, `safe apply annotation`, and `audit verification` PASS. |
| Reward resend | `補發獎勵一致性實測` PASS with idempotent `already_present` result after the first successful grant. |
| Public/admin consistency | `公私端獎勵狀態一致性` PASS: admin game/title `1`, public game `1`, redeemable `0/0`. |

## Manual UAT

PENDING. Phase 43 verifies admin/public support consistency with automated local services only. WeChat DevTools or physical-device UAT is not claimed here and remains part of Phase 44 release acceptance unless it is actually performed.

## Deferred Scope

- Phase 44 management-system IA polish, browser/admin visual checks, and release acceptance reporting.
- Full WeChat DevTools/device evidence separation for `UAT-02` and `UAT-03`.
- Future advanced gameplay support actions for AR/photo, speech, puzzle, cannon-defense, and indoor visual-positioning flows.

