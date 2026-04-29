---
phase: 29
slug: poi-default-experience-workbench
status: draft
nyquist_compliant: true
wave_0_complete: true
created: 2026-04-29
---

# Phase 29 - Validation Strategy

> Per-phase validation contract for POI default experience authoring.

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Maven compile, Vite build, UTF-8 PowerShell smoke |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json`, `scripts/local/smoke-phase-29-poi-experience.ps1` |
| **Quick run command** | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` |
| **Full suite command** | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml; npm run build --prefix packages/admin/aoxiaoyou-admin-ui; powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-29-poi-experience.ps1` |
| **Estimated runtime** | ~180 seconds with local services already running |

## Sampling Rate

- **After every backend task:** Run `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`.
- **After every admin UI task:** Run `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`.
- **After smoke-script task:** Run `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-29-poi-experience.ps1`.
- **Before `/gsd-verify-work`:** Run the full suite command.
- **Max feedback latency:** 180 seconds after local stack is up.

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 29-01-01 | 01 | 1 | STORY-01, LINK-01 | T29-01-01, T29-01-02 | POI facade remains protected admin-only and writes canonical Phase 28 tables | compile/source | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | yes | pending |
| 29-01-02 | 01 | 1 | STORY-01, LINK-01 | T29-01-03 | UI types and API calls cannot post local-only POI experience shapes | build/source | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | yes | pending |
| 29-02-01 | 02 | 2 | STORY-01 | T29-02-01, T29-02-02 | Structured cards avoid raw JSON-first operator errors | build/source | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | yes | pending |
| 29-02-02 | 02 | 2 | STORY-01, LINK-01 | T29-02-03 | POI route stays inside protected admin shell | build/source | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | yes | pending |
| 29-03-01 | 03 | 3 | STORY-01, LINK-01 | T29-03-01, T29-03-02 | Live smoke proves admin-authored POI flow compiles to public runtime without admin-only leakage | smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-29-poi-experience.ps1` | created by plan | pending |

## Wave 0 Requirements

Existing infrastructure covers all phase requirements:

- Admin backend Maven compile exists.
- Admin UI Vite build exists.
- Phase 28 smoke pattern exists and can be copied safely for Phase 29 with explicit UTF-8 handling.

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Operator UX comfort for timeline/card authoring | STORY-01 | Source/build checks cannot fully prove ease of use | Open `/space/pois/{poiId}/experience`, select A-Ma Temple, add/edit each canonical step, verify the primary path uses cards and selectors before the collapsed JSON area |

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or existing infrastructure.
- [x] Sampling continuity: no 3 consecutive tasks without automated verify.
- [x] Wave 0 covers all missing references.
- [x] No watch-mode flags.
- [x] Feedback latency target is below 180 seconds when services are running.

**Approval:** pending execution
