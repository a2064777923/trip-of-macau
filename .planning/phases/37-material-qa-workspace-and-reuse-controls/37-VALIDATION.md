---
phase: 37
slug: material-qa-workspace-and-reuse-controls
status: planned
nyquist_compliant: true
wave_0_complete: true
created: 2026-05-03
---

# Phase 37 - Validation Strategy

> Per-phase validation contract for material QA, replacement, consistency checking, and media reuse controls.

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit/Spring Boot, Vite build, PowerShell smoke, browser UAT |
| **Backend quick command** | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` |
| **Backend focused test** | `mvn -q -Dtest=AdminStoryMaterialQaServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` |
| **Admin UI build** | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` |
| **Smoke command** | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-37-material-qa.ps1` |
| **Estimated runtime** | 180-420 seconds depending on local MySQL/admin services and COS check mode |

## Sampling Rate

- After backend API/service tasks: run backend compile and focused QA tests.
- After admin UI tasks: run `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`.
- After smoke script changes: run `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-37-material-qa.ps1 -ValidateOnly` if implemented, then full smoke.
- Before `/gsd-verify-work`: run backend compile, UI build, smoke, and browser UAT notes.

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------------|-----------|-------------------|-------------|--------|
| 37-01-01 | 01 | 1 | QA-01/QA-03 | QA DTOs do not expose provider secrets and classify asset health deterministically | compile | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | planned | pending |
| 37-01-02 | 01 | 1 | QA-02/QA-03 | reject/approve/replace preserve version lineage and require notes/confirmation | unit | `mvn -q -Dtest=AdminStoryMaterialQaServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | planned | pending |
| 37-02-01 | 02 | 2 | QA-01/QA-02 | Admin QA workspace shows previews, failed states, actions, and no overflowing links | build | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` | planned | pending |
| 37-03-01 | 03 | 3 | QA-03/QA-04 | smoke proves consistency checks and media picker reuse without leaking secrets | smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-37-material-qa.ps1` | planned | pending |

## Wave 0 Requirements

- [x] Phase 36 production/versioning foundation exists.
- [x] Phase 36 live smoke produced image, board-slice, audio, video, COS, version history, and rollback evidence.
- [x] Phase 37 context is written and identifies backend, UI, reuse, and consistency requirements.
- [x] Research identifies validation commands and acceptance slices.

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Preview quality and visual layout | QA-01 | Browser rendering and media previews require visual inspection | Open material package QA workspace, inspect image/audio/video/Lottie/failed states, and capture notes/screenshots. |
| Operator workflow clarity | QA-02 | Reject/replace/approve language and drawer actions are usability-sensitive | Walk through a reject -> replace -> approve -> rollback flow on a non-critical test item. |
| Reuse discoverability | QA-04 | Search result quality is best verified in UI | Open a downstream media picker and search by package code and material item key. |

## Validation Sign-Off

- [ ] All tasks have automated verification or manual browser coverage.
- [ ] Backend tests cover lineage-preserving reject/replace/rollback behavior.
- [ ] Admin build passes after QA workspace changes.
- [ ] Smoke covers package QA overview, consistency check, action flow, and media picker search.
- [ ] No secrets, bearer tokens, provider keys, or COS keys are committed.

**Approval:** pending Phase 37 execution.
