---
phase: 31
slug: interaction-task-template-library-and-governance-center
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-29
---

# Phase 31 - Validation Strategy

## Test Infrastructure

| Property | Value |
|----------|-------|
| Framework | Maven compile, Vite build, PowerShell smoke scripts |
| Config file | `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json` |
| Quick run command | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` |
| Full suite command | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` plus `scripts/local/smoke-phase-31-template-governance.ps1` |
| Estimated runtime | 60-180 seconds |

## Sampling Rate

- After backend plan changes: run admin backend compile.
- After admin UI plan changes: run admin UI build.
- Before verification: import Phase 31 seed and run Phase 31 smoke plus Phase 30 regression smoke.
- Max feedback latency: 180 seconds for full phase smoke.

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 31-01-01 | 31-01 | 1 | OPS-01, LINK-01 | T31-01 | Template JSON stays schema-versioned and canonical | compile/source | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | yes | pending |
| 31-01-02 | 31-01 | 1 | OPS-01, LINK-01 | T31-01 | Template usage does not leak unrelated deleted records | smoke/source | `Select-String` on controller/service for `/usage` and `deleted = 0` query paths | yes | pending |
| 31-02-01 | 31-02 | 2 | OPS-03, LINK-01 | T31-02 | Governance reads only admin-authenticated endpoints | compile/source | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | yes | pending |
| 31-02-02 | 31-02 | 2 | OPS-03, LINK-01 | T31-02 | Conflict output is deterministic and paginated | smoke/source | `scripts/local/smoke-phase-31-template-governance.ps1` | yes | pending |
| 31-03-01 | 31-03 | 3 | OPS-01, OPS-03 | T31-03 | UI avoids raw JSON-first authoring | build/source | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | yes | pending |
| 31-03-02 | 31-03 | 3 | OPS-03 | T31-03 | Governance filters remain labelled and usable | browser/smoke | Playwright screenshot or smoke source checks | yes | pending |
| 31-04-01 | 31-04 | 4 | VER-01, VER-02, LINK-01 | T31-04 | Seed data uses UTF-8/utf8mb4 | smoke | `scripts/local/smoke-phase-31-template-governance.ps1` | yes | pending |
| 31-04-02 | 31-04 | 4 | OPS-01, OPS-03 | T31-04 | Regression does not break Phase 30 runtime | smoke | `scripts/local/smoke-phase-30-storyline-mode.ps1` | yes | pending |

## Wave 0 Requirements

Existing infrastructure covers the phase:

- Admin backend Maven compile already available.
- Admin UI build already available.
- MySQL import path already used by Phases 29-30.
- Smoke script pattern already exists in `scripts/local/smoke-phase-30-storyline-mode.ps1`.

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Admin visual usability for template library/governance | OPS-01, OPS-03 | Build cannot judge layout quality | Open `/admin/#/content/experience/templates` and `/admin/#/content/experience/governance`, verify filters have labels, rows/cards are readable, and detail panels are not blank. |

## Validation Sign-Off

- [x] All tasks have automated verification or source checks.
- [x] Sampling continuity: no three consecutive tasks without automated verification.
- [x] Existing infrastructure covers Wave 0.
- [x] No watch-mode commands.
- [x] Feedback latency target is under 180 seconds.
