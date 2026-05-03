---
phase: 40
slug: acceptance-cost-visibility-and-release-readiness
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-05-03
---

# Phase 40 - Validation Strategy

> Per-phase validation contract for release-readiness closure.

## Test Infrastructure

| Property | Value |
| --- | --- |
| Framework | PowerShell smoke scripts, Maven compile/tests, npm/Vite build, Taro WeChat build |
| Config file | Existing package `pom.xml` and `package.json` files |
| Quick run command | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-40-release-readiness.ps1 -Quick` |
| Full suite command | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-40-release-readiness.ps1` |
| Estimated runtime | 5-20 minutes depending on backend/service availability |

## Sampling Rate

- After every task commit: run the relevant quick command or build for the touched area.
- After every plan wave: run the Phase 40 smoke wrapper where possible.
- Before `/gsd-verify-work`: full suite must be recorded in `40-VERIFICATION.md`.
- Max feedback latency: 20 minutes for full local smoke, 5 minutes for docs-only checks.

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 40-01-01 | 01 | 1 | ACC-01 | T40-01/T40-02 | Smoke output does not print secrets and records PASS/SKIP/FAIL states. | smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-40-release-readiness.ps1 -Quick` | yes after task | pending |
| 40-01-02 | 01 | 1 | ACC-01 | T40-03 | Client alias runs the wrapper without changing existing build scripts. | build/smoke | `cd packages/client; npm run smoke:phase40:release-readiness` | yes after task | pending |
| 40-02-01 | 02 | 2 | ACC-03 | T40-04/T40-05 | Admin UI tables show summaries, not default secret-bearing payloads. | frontend build | `cd packages/admin/aoxiaoyou-admin-ui; npm run build` | yes | pending |
| 40-02-02 | 02 | 2 | ACC-03 | T40-04/T40-05 | Admin backend exposes role-aware history and safe summary/detail fields. | backend compile | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | yes | pending |
| 40-03-01 | 03 | 3 | ACC-02 | T40-06 | UAT checklist separates manual device evidence from automated smoke. | docs | `Select-String -Path .planning/phases/40-acceptance-cost-visibility-and-release-readiness/40-UAT.md -Pattern '微信開發者工具','結果','證據'` | yes after task | pending |
| 40-03-02 | 03 | 3 | ACC-04 | T40-07 | Acceptance report lists implemented/manual/skipped/deferred evidence and no secrets. | docs | `Select-String -Path .planning/phases/40-acceptance-cost-visibility-and-release-readiness/40-ACCEPTANCE.md -Pattern 'Automated Evidence','Manual Evidence','Deferred'` | yes after task | pending |

## Wave 0 Requirements

Existing infrastructure covers all Phase 40 requirements:

- Phase 36-39 smoke scripts already exist.
- Admin UI build command already exists.
- Admin backend compile command already exists.
- Client WeChat build command already exists.

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
| --- | --- | --- | --- |
| WeChat DevTools/device story journey | ACC-02 | Visual route/media/playback behavior must be confirmed in WeChat tooling or device. | Follow `40-UAT.md` and record pass/fail/skipped evidence per checklist row. |
| Milestone closure acceptance | ACC-04 | User must accept any manual UAT caveats before milestone archive. | Review `40-ACCEPTANCE.md` and confirm no blocking items remain. |

## Validation Sign-Off

- [x] All tasks have automated verify or explicit manual-only rationale.
- [x] Sampling continuity: no 3 consecutive tasks without automated verify.
- [x] Wave 0 covers all missing infrastructure.
- [x] No watch-mode flags.
- [x] Feedback latency target is documented.
- [x] `nyquist_compliant: true` set in frontmatter.

**Approval:** approved 2026-05-03
