---
phase: 33
slug: complete-flagship-story-content-material-package
status: draft
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-29
---

# Phase 33 - Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Maven compile/tests, Vite build, PowerShell smoke, MySQL import |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json`, `scripts/local/mysql/init/*.sql` |
| **Quick run command** | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` |
| **Full suite command** | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` + `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-33-flagship-package.ps1` |
| **Estimated runtime** | ~90-180 seconds after services are running |

## Sampling Rate

- **After every task commit:** Run the nearest compile/build command for touched code.
- **After every SQL seed:** Import the SQL with `mysql --default-character-set=utf8mb4`.
- **After every plan wave:** Run admin backend compile. Run admin UI build after UI changes.
- **Before `/gsd-verify-work`:** Run the Phase 33 smoke script against live admin backend `8081`.
- **Max feedback latency:** 180 seconds.

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 33-01-01 | 01 | 1 | STORY-03 | T33-01 | Admin-only package API does not expose secrets and validates ids/status | compile | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | W0 | pending |
| 33-01-02 | 01 | 1 | VER-02 | T33-02 | Manifest item provenance is persisted without storing provider secrets | SQL import | `mysql --default-character-set=utf8mb4 aoxiaoyou < scripts/local/mysql/init/47-phase-33-story-material-package-model.sql` | W0 | pending |
| 33-02-01 | 02 | 2 | STORY-03 | T33-03 | Asset records use COS keys and fallback ids without hardcoded credentials | SQL import | `mysql --default-character-set=utf8mb4 aoxiaoyou < scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql` | W0 | pending |
| 33-03-01 | 03 | 3 | VER-02 | T33-04 | Flagship seed is idempotent and UTF-8 safe | SQL import | `mysql --default-character-set=utf8mb4 aoxiaoyou < scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql` | W0 | pending |
| 33-04-01 | 04 | 4 | STORY-03 | T33-05 | Admin package page requires authenticated admin API and shows only package metadata | UI build | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | W0 | pending |
| 33-04-02 | 04 | 4 | VER-02 | T33-06 | Smoke uses ignored local auth/env tokens, not tracked secrets | smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-33-flagship-package.ps1` | W0 | pending |

## Wave 0 Requirements

- Existing Maven and Vite infrastructure covers compile/build validation.
- Phase 33 must create `scripts/local/smoke-phase-33-flagship-package.ps1` before the final smoke can run.
- Phase 33 must create `scripts/local/mysql/init/47-phase-33-story-material-package-model.sql`, `48-phase-33-flagship-material-assets.sql`, and `49-phase-33-east-west-flagship-story.sql`.

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Package page usefulness | STORY-03 | Requires operator judgment on navigation and readability | Open `/admin/#/content/material-packages`, inspect package detail, and confirm links route to media, blocks, story mode, and experience pages. |
| Historical basis vs literary dramatization clarity | VER-02 | Requires content review | Inspect package detail and `docs/content-packages/east-west-war-and-coexistence/historical-checklist.md`; confirm both fields are separately visible. |

## Validation Sign-Off

- [x] All tasks have automated verification or explicit smoke/manual coverage.
- [x] Sampling continuity: no 3 consecutive tasks without automated verify.
- [x] Wave 0 covers all missing references.
- [x] No watch-mode flags.
- [x] Feedback latency target < 180s after services are running.
- [x] `nyquist_compliant: true` set in frontmatter.

**Approval:** pending execution
