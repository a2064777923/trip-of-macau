---
phase: 28
slug: story-and-content-control-plane-completion
status: draft
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
| 28-01-01 | 01 | 1 | STORY-01, STORY-02, LINK-01 | T28-01, T28-02 | Admin JSON and runtime rows are schema-versioned and status-filtered | compile + grep | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ✅ | ⬜ pending |
| 28-01-02 | 01 | 1 | STORY-02, LINK-02 | T28-03, T28-04 | Public runtime does not expose draft flows and event ingestion is idempotent | compile + smoke | `mvn -q -DskipTests compile -f packages/server/pom.xml` | ✅ | ⬜ pending |
| 28-02-01 | 02 | 2 | STORY-01, STORY-03, STORY-04 | T28-05 | Admin workbench exposes structured Traditional Chinese UI and avoids JSON-only authoring | build | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | ✅ | ⬜ pending |
| 28-03-01 | 03 | 3 | STORY-01, STORY-02, VER-02 | T28-06 | Seed/smoke path is UTF-8 safe and contains no secrets | smoke + grep | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-28-experience.ps1` | ❌ W0 | ⬜ pending |
| 28-04-01 | 04 | 4 | STORY-01, STORY-02, STORY-03, STORY-04 | — | Planning artifacts preserve Phase 29-34 handoff and requirement coverage | file read | `Get-Content .planning/phases/28-story-and-content-control-plane-completion/28-HANDOFF.md` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- Existing Java and admin UI build infrastructure covers compile/build checks.
- Create `scripts/local/smoke-phase-28-experience.ps1` during execution for local API smoke checks.
- Create `.planning/phases/28-story-and-content-control-plane-completion/28-HANDOFF.md` during execution to record Phase 29-34 boundary.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Admin workbench usability | STORY-01, STORY-02, STORY-04 | Browser interaction may not be fully automated in this phase | Log into admin, open the experience workbench, create or inspect a template/flow/binding/override/exploration element, and confirm Traditional Chinese labels plus validation panel are visible |
| Public runtime smoke | LINK-02, VER-02 | Requires local services and seeded DB to be running | Call `GET http://127.0.0.1:8080/api/v1/experience/poi/9?locale=zh-Hant` and `GET http://127.0.0.1:8080/api/v1/storylines/8/runtime?locale=zh-Hant` |

---

## Validation Sign-Off

- [x] All planned tasks have automated or smoke verification coverage.
- [x] Sampling continuity: no 3 consecutive tasks without automated verify.
- [x] Wave 0 requirements are identified.
- [x] No watch-mode flags.
- [x] Feedback latency target is under 300s.
- [x] `nyquist_compliant: true` set in frontmatter.

**Approval:** approved 2026-04-28
