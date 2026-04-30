---
phase: 35
slug: operations-lifecycle-scheduling-and-dependency-workbench
status: complete
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-30
---

# Phase 35 - Validation Strategy

> Per-phase validation contract for operations lifecycle scheduling and dependency preview.

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Maven compile, Vite/TypeScript build, PowerShell smoke, MySQL import |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json`, `scripts/local/mysql/init/*.sql` |
| **Quick run command** | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` |
| **Full suite command** | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` + `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-35-lifecycle.ps1` |
| **Estimated runtime** | 2-5 minutes after services are running |

## Sampling Rate

- **After schema/entity/mapper/DTO changes:** Run admin backend compile.
- **After lifecycle service/controller changes:** Run admin backend compile and at least controller/service smoke checks.
- **After admin UI API/types/page changes:** Run admin UI build.
- **After smoke script changes:** Run the script against live local admin backend and public backend if available.
- **Before `/gsd-verify-work`:** Run backend compile, UI build, and Phase 35 smoke.
- **Max feedback latency:** 300 seconds after services are running.

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 35-01-01 | 01 | 1 | OPS-04 | T35-01 | Canonical lifecycle statuses and compatibility mappings prevent inconsistent public visibility states | compile | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | W0 | status: complete |
| 35-01-02 | 01 | 1 | OPS-02 | T35-02 | Operation and impact tables persist audit/history without tracked secrets | compile + SQL import | `scripts/local/mysql/init/50-phase-35-lifecycle-operations.sql` | W0 | status: complete |
| 35-01-03 | 01 | 1 | OPS-02 | T35-03 | DTOs require structured action/target data and versioned preview payloads | compile | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | W0 | status: complete |
| 35-02-01 | 02 | 2 | OPS-04 | T35-04 | Target registry normalizes cross-domain lifecycle labels and allowed transitions | compile | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | W0 | status: complete |
| 35-02-02 | 02 | 2 | OPS-02 | T35-05 | Preview-first dependency impact calculation prevents blind destructive changes | compile + smoke | `scripts/local/smoke-phase-35-lifecycle.ps1` | W0 | status: complete |
| 35-02-03 | 02 | 2 | OPS-02 | T35-06 | Immediate/scheduled operations create auditable records and impacts | compile + smoke | `scripts/local/smoke-phase-35-lifecycle.ps1` | W0 | status: complete |
| 35-03-01 | 03 | 3 | OPS-04 | T35-07 | Admin UI exposes visible Traditional Chinese status/action controls | build | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | W0 | status: complete |
| 35-03-02 | 03 | 3 | OPS-02 | T35-08 | UI blocks apply/schedule until preview and confirmation are present | build + manual | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | W0 | status: complete |
| 35-04-01 | 04 | 4 | OPS-02 | T35-09 | Smoke uses env-backed auth and does not hardcode credentials | smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-35-lifecycle.ps1` | W0 | status: complete |
| 35-04-02 | 04 | 4 | OPS-04 | T35-10 | Public runtime filtering remains aligned after lifecycle status changes | smoke | `scripts/local/smoke-phase-35-lifecycle.ps1` | W0 | status: complete |
| 35-04-03 | 04 | 4 | OPS-02/OPS-04 | T35-11 | Requirements and roadmap traceability are updated only after verified behavior | docs | `Select-String` doc checks | W0 | status: complete |

## Wave 0 Requirements

- Local MySQL database `aoxiaoyou` exists and can import Phase 28-35 seed/migration SQL with `--default-character-set=utf8mb4`.
- Admin backend runs at `http://127.0.0.1:8081` for smoke.
- Public backend runs at `http://127.0.0.1:8080` for public runtime filtering smoke where practical.
- Admin auth for smoke is provided by one of:
  - `PHASE35_ADMIN_BEARER_TOKEN`
  - `PHASE35_ADMIN_USERNAME` and `PHASE35_ADMIN_PASSWORD`
- No bearer token, password, WeChat secret, COS secret, or provider key is tracked.

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Operator comprehension of preview | OPS-02 | Requires judging whether dependency/impact copy is understandable | Open `/admin/#/ops/lifecycle`, preview a seeded story or chapter action, and confirm impact groups clearly explain consequences before apply/schedule. |
| Full approval chain | Future | Explicitly out of Phase 35 scope | Do not block Phase 35 on reviewer assignment, approval inbox, or notification workflows. |
| Full WeChat journey | Future | Explicitly deferred beyond v3.0 | Do not block Phase 35 on WeChat DevTools/device route gameplay UAT. |

## Validation Sign-Off Criteria

- [x] All tasks have automated verification or explicit smoke/manual coverage.
- [x] Sampling continuity: no 3 consecutive tasks without automated verify.
- [x] Wave 0 covers credentials, SQL import, and service prerequisites.
- [x] No watch-mode flags.
- [x] Feedback latency target < 300s after services are running.
- [x] `nyquist_compliant: true` remains set in frontmatter.

## Expected Final Evidence

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` exits `0`.
- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` exits `0`.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-35-lifecycle.ps1` exits `0`.
- Smoke success line: `Phase 35 lifecycle smoke passed`.
- `35-VERIFICATION.md` records any public backend smoke prerequisites or skipped checks explicitly.

## Final Evidence

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` exited `0`.
- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` exited `0`.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-35-lifecycle.ps1` exited `0`.
- Public backend filtering check was executed because `http://127.0.0.1:8080` was reachable.
