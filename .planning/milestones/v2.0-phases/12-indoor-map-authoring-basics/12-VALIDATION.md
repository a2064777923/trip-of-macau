---
phase: 12
slug: indoor-map-authoring-basics
status: executed
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-14
updated: 2026-04-14
---

# Phase 12 - Validation Strategy

> Per-phase validation contract for indoor building/floor authoring, tile import/slicing, marker import/editing, and live mini-program indoor consumption.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test + admin UI build verification + mini-program build verification + PowerShell smoke |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json`, `packages/client/package.json` |
| **Quick run command** | `mvn test` in `packages/admin/aoxiaoyou-admin-backend`, `npm run build` in `packages/admin/aoxiaoyou-admin-ui`, `npm run build:weapp` in `packages/client` |
| **Full suite command** | backend tests + admin build + mini-program build + a dedicated indoor smoke script |
| **Estimated runtime** | ~360 seconds |

---

## Sampling Rate

- **After every task commit:** run the touched package quick command
- **After every plan wave:** run the relevant backend/UI/client command set for that wave
- **Before `/gsd-verify-work`:** full suite plus indoor smoke proof must be green
- **Max feedback latency:** 360 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 12-01-01 | 01 | 1 | INDO-01, INDO-02 | T-12-01 / T-12-02 | Indoor buildings/floors persist canonical bindings, locale fields, and media refs without falling back to raw code/URL-only fields | unit/migration | `mvn test` in `packages/admin/aoxiaoyou-admin-backend` | add indoor schema/service tests | pending |
| 12-01-02 | 01 | 1 | INDO-01, INDO-02 | T-12-02 | Admin UI correctly edits building/floor metadata, required binding rules, and shared media fields | build/component | `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | add indoor admin pages | pending |
| 12-02-01 | 02 | 2 | INDO-03 | T-12-03 / T-12-04 | Zip import rejects unsafe entries and image slicing writes a stable tile manifest with backend-owned COS paths | unit/integration | `mvn test` in `packages/admin/aoxiaoyou-admin-backend` | add import/slicing tests | pending |
| 12-02-02 | 02 | 2 | INDO-02, INDO-03 | T-12-04 | Zoom bounds are derived predictably from floor metadata and remain operator-editable | unit/build | `mvn test` in `packages/admin/aoxiaoyou-admin-backend` and `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | add derivation tests and UI wiring | pending |
| 12-03-01 | 03 | 3 | INDO-04 | T-12-05 | CSV marker preview validates before confirmation and direct minimap picking stores normalized coordinates | unit/integration/build | `mvn test` in `packages/admin/aoxiaoyou-admin-backend` and `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | add CSV/direct-editor coverage | pending |
| 12-03-02 | 03 | 3 | INDO-01 through INDO-04 | T-12-06 | Public indoor-read APIs and mini-program indoor runtime consume real authored payloads instead of local-only manifests | integration/smoke | `npm run build:weapp` in `packages/client` plus a dedicated indoor smoke script | add smoke script | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] backend tests for indoor building/floor validation and persistence
- [ ] backend tests for zip validation and image slicing
- [ ] backend tests for CSV preview / confirm import
- [x] public server compile safety
- [x] admin backend compile safety
- [x] admin UI build safety for indoor pages and direct editor components
- [x] mini-program build safety for indoor-page runtime cutover
- [x] `scripts/local/smoke-phase-12-indoor.ps1` - end-to-end proof for admin write -> COS/MySQL -> public read -> mini-program build/runtime fetch

---

## Planned Execution Results

- `mvn -q -DskipTests compile` in `packages/server`: green on 2026-04-14
- `mvn -q -DskipTests compile` in `packages/admin/aoxiaoyou-admin-backend`: green on 2026-04-14
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`: green on 2026-04-14
- `npm run build:weapp` in `packages/client`: green on 2026-04-14
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-12-indoor.ps1`: green on 2026-04-14

### Execution Notes

- The end-to-end smoke created a new floor under indoor building `4` (`galaxy_macau`), ran ZIP preview, completed image slicing import into COS, created one manual marker, confirmed two CSV-imported markers, and then read the authored floor back from the public API with `tileEntryCount = 4` and `publicMarkerCount = 3`.
- Public verification used live reads from `GET /api/v1/indoor/floors/{floorId}` and `GET /api/v1/indoor/buildings/{buildingId}` after the admin write path completed.
- Targeted JUnit coverage for the indoor foundation, tile pipeline, and marker import flows is still a follow-up gap; this phase closed with compile, build, and smoke coverage instead of new backend test classes for every indoor surface.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Building binding UX | INDO-01 | Operators must understand when coordinates are required versus POI-bound optional | Create one building bound to city/sub-map and one bound to POI; confirm the form guides required fields clearly |
| Floor tile import clarity | INDO-03 | Build success cannot prove that zip/image import results are understandable | Import one zip tile set and one full image; confirm the UI shows source type, manifest result, and generated preview details |
| Marker minimap editing | INDO-04 | Click-picking accuracy and edit discoverability need visual confirmation | Open a floor editor, click the minimap to place a marker, and confirm the stored preview aligns with the intended spot |
| CSV preview diagnostics | INDO-04 | Operators need actionable row-level feedback for bad imports | Upload a mixed-validity CSV and confirm warnings/errors are specific before confirmation |
| Mini-program indoor live rendering | INDO-01 through INDO-04 | Runtime fit and tile/marker rendering require real client behavior | Open the authored indoor building/floor in the mini-program dev build and verify live tiles, markers, and floor switching render from backend data |

---

## Validation Sign-Off

- [x] All tasks have automated verification expectations or explicit Wave 0 dependencies
- [x] Sampling continuity covers admin backend, admin UI, public read alignment, and mini-program runtime
- [x] High-risk surfaces such as zip parsing, image slicing, and CSV preview have dedicated verification expectations
- [x] No watch-mode flags
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** planned 2026-04-14
