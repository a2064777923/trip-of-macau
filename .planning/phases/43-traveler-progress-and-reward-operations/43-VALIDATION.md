---
phase: 43
slug: traveler-progress-and-reward-operations
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-05-04
---

# Phase 43 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Maven compile, TypeScript type-check/build, PowerShell schema apply/verify, PowerShell smoke |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/server/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json` |
| **Quick run command** | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` |
| **Full suite command** | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-43-traveler-ops.ps1` |
| **Estimated runtime** | ~180 seconds with local services running |

---

## Sampling Rate

- **After every admin backend task commit:** Run `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
- **After every public backend task commit:** Run `mvn -q -DskipTests compile -f packages/server/pom.xml`
- **After every admin UI task commit:** Run `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check`
- **After schema task:** Run `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/apply-phase-43-traveler-ops-migration.ps1`, then `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/apply-phase-43-traveler-ops-migration.ps1 -VerifyOnly`
- **After every plan wave:** Run the applicable compile/type-check command plus targeted `Select-String` acceptance checks.
- **Before `/gsd-verify-work`:** Run `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-43-traveler-ops.ps1`
- **Max feedback latency:** 180 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| T43-01-01 | 01 | 1 | OPS-01/OPS-02/OPS-04 | T43-01-A/B/C | Read-only DTOs expose state/trace without mutation | compile/select | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | yes | pending |
| T43-01-02 | 01 | 1 | OPS-01/OPS-02 | T43-01-A/D | Timeline filters are strict: rows with missing or mismatched active dimensions are excluded | compile | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | yes | pending |
| T43-01-03 | 01 | 1 | OPS-01/OPS-04 | T43-01-A/B/C | Rule trace returns explicit missing/data-unavailable statuses, not false negatives | compile | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | yes | pending |
| T43-02-01 | 02 | 2 | OPS-03 | T43-02-A/B/C/D | Support action contract requires preview token, confirmation, and audit context | compile | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | yes | pending |
| T43-02-02 | 02 | 2 | OPS-03/OPS-04 | T43-02-A/B/D | Reward resend writes idempotent live reward/grant rows and audit | compile | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | yes | pending |
| T43-02-03 | 02 | 2 | OPS-03 | T43-02-A/C/D | Annotation and duplicate void do not delete traveler events | compile | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | yes | pending |
| T43-06-01 | 06 | 3 | OPS-03/OPS-04 | T43-06-A/B/C | Active local MySQL schema is idempotently applied, verified, and seeded with deterministic local smoke reward/rule fixtures | schema apply/verify | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/apply-phase-43-traveler-ops-migration.ps1`; then `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/apply-phase-43-traveler-ops-migration.ps1 -VerifyOnly` | yes | pending |
| T43-03-01 | 03 | 3 | OPS-01/OPS-02/OPS-04 | T43-03-A/B | UI types and API wrappers match backend contracts | type-check | `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` | yes | pending |
| T43-03-02 | 03 | 3 | OPS-01 | T43-03-C | Admin IA exposes one clear support entry | type-check | `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` | yes | pending |
| T43-03-03 | 03 | 3 | OPS-01/OPS-02/OPS-04 | T43-03-B/C | Workbench filters are labeled and raw JSON stays in detail controls | type-check | `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` | yes | pending |
| T43-03-04 | 03 | 3 | OPS-03 | T43-03-A/D | Support actions are preview-first, confirmed, disabled while loading, and refresh audit state | type-check | `cd packages/admin/aoxiaoyou-admin-ui; npm run type-check` | yes | pending |
| T43-05-01 | 05 | 3 | OPS-01/OPS-02/OPS-04 | T43-05-A/B | Admin reward state, timeline, and trace include live game reward/title grant rows | compile | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | yes | pending |
| T43-05-02 | 05 | 3 | OPS-01/OPS-04 | T43-05-A/B/C | Public reward reads expose game grants without audit/operator internals | compile | `mvn -q -DskipTests compile -f packages/server/pom.xml` | yes | pending |
| T43-04-01 | 04 | 4 | OPS-01/OPS-02/OPS-03/OPS-04 | T43-04-A/B/C/D | Smoke redacts credentials, loads Chinese payloads from UTF-8 fixtures, auto-discovers seeded resend fixtures, asserts filtered timeline rows, checks public/admin consistency, and requires `Final outcome: PASS` | smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-43-traveler-ops.ps1` | yes | pending |
| T43-04-02 | 04 | 4 | OPS-01/OPS-02/OPS-03/OPS-04 | T43-04-A/C | Verification docs map every requirement to truthful evidence and manual UAT caveat | select | `Select-String` evidence checks in 43-04 plan | yes | pending |

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Browser layout comfort and filter usability | OPS-01/OPS-02 | Visual density and support workflow feel require browser inspection | Open admin UI, navigate to `用戶與進度管理`, search a traveler, inspect tabs, filters, drawers, and confirm no cramped or unlabeled controls. |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 180s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** approved 2026-05-04
