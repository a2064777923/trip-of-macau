---
phase: 28
slug: story-and-content-control-plane-completion
status: ready
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-28
---

# Phase 28 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Maven compile checks for Java backends, Vite/TypeScript admin UI build, PowerShell smoke scripts |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/server/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json` |
| **Quick run command** | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` |
| **Full suite command** | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml; mvn -q -DskipTests compile -f packages/server/pom.xml; npm run build --prefix packages/admin/aoxiaoyou-admin-ui` |
| **Estimated runtime** | ~180 seconds |

---

## Sampling Rate

- **After every backend task commit:** Run the relevant Maven compile command.
- **After every admin UI task commit:** Run `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`.
- **After SQL/seed tasks:** Run the Phase 28 smoke script if created, or manually query the local MySQL and public runtime endpoints.
- **After every plan wave:** Run the full suite command.
- **Before `/gsd-verify-work`:** Full suite plus public runtime smoke checks must pass or be explicitly reported.
- **Max feedback latency:** 300 seconds.

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 28-01-01 | 01 | 1 | STORY-01, STORY-02, LINK-01 | T28-01-01, T28-01-04 | Admin JSON saves are schema-versioned and shared enum vocabulary is preserved | compile + grep | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ✅ | ⬜ pending |
| 28-01-02 | 01 | 1 | STORY-02, LINK-02 | T28-01-02, T28-01-03 | Public runtime exposes published compiled flows only and event ingestion remains idempotent | compile + grep | `mvn -q -DskipTests compile -f packages/server/pom.xml` | ✅ | ⬜ pending |
| 28-01-03 | 01 | 1 | STORY-01, STORY-04, LINK-01 | T28-01-05 | Admin experience endpoints reject anonymous access and allow authenticated admin access | live smoke + grep | `powershell -NoProfile -Command "$base='http://127.0.0.1:8081'; ..."` | ✅ | ⬜ pending |
| 28-02-01 | 02 | 2 | STORY-01, STORY-02, STORY-04, LINK-01 | T28-02-01 | Admin UI types and API helpers mirror backend experience contracts | build + grep | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | ✅ | ⬜ pending |
| 28-02-02 | 02 | 2 | STORY-01, STORY-02, STORY-04 | T28-02-02, T28-02-03, T28-02-04 | Workbench uses Traditional Chinese structured authoring with scroll/focus/shake validation and full A-Ma check-in preset | build + grep | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | ✅ | ⬜ pending |
| 28-02-03 | 02 | 2 | STORY-03, STORY-04 | T28-02-05 | Reusable content blocks, global media assets, and assembled pre-publish preview are verified | build + grep | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | ✅ | ⬜ pending |
| 28-03-01 | 03 | 3 | STORY-01, STORY-02, STORY-03, VER-02 | T28-03-01, T28-03-02 | Seed SQL remains UTF-8/utf8mb4 safe, idempotent, and secret-free | grep + SQL review | `Select-String -Path scripts/local/mysql/init/38-phase-28-story-content-and-lottie.sql,scripts/local/mysql/init/39-phase-28-experience-orchestration.sql -Pattern 'SET NAMES utf8mb4|schemaVersion|媽閣廟|lottie'` | ✅ | ⬜ pending |
| 28-03-02 | 03 | 3 | STORY-01, STORY-02, STORY-03, LINK-02, VER-02 | T28-03-02, T28-03-03, T28-03-04 | Smoke script proves admin/public runtime alignment, published filtering, and idempotent event ingestion | smoke + grep | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-28-experience.ps1` | ❌ planned output | ⬜ pending |
| 28-04-01 | 04 | 4 | STORY-01, STORY-02, STORY-03, STORY-04, LINK-01, LINK-02 | T28-04-01, T28-04-03 | Handoff maps D-01 through D-26 to delivered foundation artifacts or Phase 29-34 owners | file read + grep | `Select-String -Path .planning/phases/28-story-and-content-control-plane-completion/28-HANDOFF.md -Pattern 'D-01|D-26|Phase 29|Phase 34'` | ❌ planned output | ⬜ pending |
| 28-04-02 | 04 | 4 | STORY-01, STORY-02, STORY-03, STORY-04, LINK-01, LINK-02 | T28-04-02, T28-04-03 | Roadmap and requirements preserve the replacement direction and follow-on phase boundaries | file read + grep | `Select-String -Path .planning/ROADMAP.md,.planning/REQUIREMENTS.md -Pattern 'experience orchestration foundation|Phase 29|Phase 34|LINK-02'` | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- Existing Java and admin UI build infrastructure covers compile/build checks.
- No pre-execution Wave 0 file generation is required.
- `scripts/local/smoke-phase-28-experience.ps1` is intentionally created by task `28-03-02`, then executed by that task and final phase verification.
- `.planning/phases/28-story-and-content-control-plane-completion/28-HANDOFF.md` is intentionally created by task `28-04-01`, then consumed by `28-04-02`.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Admin workbench usability | STORY-01, STORY-02, STORY-04 | Browser interaction may not be fully automated in this phase | Log into admin, open the experience workbench, create or inspect a template/flow/binding/override/exploration element, and confirm Traditional Chinese labels plus validation panel are visible |
| Public runtime smoke | LINK-02, VER-02 | Requires local services and seeded DB to be running | Call `GET http://127.0.0.1:8080/api/v1/experience/poi/9?locale=zh-Hant` and `GET http://127.0.0.1:8080/api/v1/storylines/8/runtime?locale=zh-Hant` |

---

## Validation Sign-Off

- [x] All 10 planned tasks have automated, grep, smoke, or documented manual verification coverage in the per-task map.
- [x] Sampling continuity: no 3 consecutive tasks without automated, grep, smoke, or file-read verification.
- [x] Wave 0 requirements are complete; execution-created files are assigned to their creating tasks, not treated as pre-existing files.
- [x] No watch-mode flags.
- [x] Feedback latency target is under 300s.
- [x] `nyquist_compliant: true` set in frontmatter.

**Approval:** approved 2026-04-28
