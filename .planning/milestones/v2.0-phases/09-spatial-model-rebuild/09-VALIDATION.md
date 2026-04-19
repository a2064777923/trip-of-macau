---
phase: 09
slug: spatial-model-rebuild
status: planned
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-14
updated: 2026-04-14
---

# Phase 9 - Validation Strategy

> Per-phase validation contract for the spatial hierarchy rebuild, coordinate normalization, spatial authoring UX, and public/client alignment.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test + admin UI build verification + mini-program build verification + PowerShell smoke |
| **Config file** | `packages/server/pom.xml`, `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json`, `packages/client/package.json` |
| **Quick run command** | `mvn test` in the touched Java service plus `npm run build` in the touched frontend package |
| **Full suite command** | `mvn test` in both Java backends, `npm run build` in `packages/admin/aoxiaoyou-admin-ui`, `npm run build:weapp` in `packages/client`, and a Phase 9 spatial smoke script |
| **Estimated runtime** | ~240 seconds |

---

## Sampling Rate

- **After every task commit:** Run the affected package quick command for the changed surface.
- **After every plan wave:** Run the full Phase 9 command set for admin backend, public backend, admin UI, client build, and spatial smoke.
- **Before `/gsd-verify-work`:** Full suite must be green and the spatial smoke script must pass.
- **Max feedback latency:** 240 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 09-01-01 | 01 | 1 | MAP-01, MAP-02 | T-09-01 / T-09-04 | Spatial hierarchy and asset relations cannot drift from canonical schema | unit/migration | `mvn test` in both Java backends | add schema/entity/mapping tests | pending |
| 09-01-02 | 01 | 1 | MAP-03 | T-09-01 / T-09-03 | Raw/source coordinates are preserved and normalized GCJ-02 values are deterministic | unit | `mvn test` in `packages/admin/aoxiaoyou-admin-backend` and `packages/server` | add coordinate-normalization tests | pending |
| 09-02-01 | 02 | 2 | MAP-01, MAP-02 | T-09-05 | City/sub-map authoring surfaces expose the correct hierarchy and validation | build/component | `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | add spatial admin pages/components | pending |
| 09-02-02 | 02 | 2 | MAP-03, MAP-04 | T-09-05 / T-09-06 | POI authoring cannot save invalid spatial bindings or unsupported coordinate metadata | integration/build | `mvn test` in admin backend and `npm run build` in admin UI | add POI/sub-map authoring coverage | pending |
| 09-03-01 | 03 | 3 | MAP-01, MAP-02, MAP-03, MAP-04 | T-09-02 / T-09-07 | Public reads expose only published spatial hierarchy and normalized coordinates | integration | `mvn test` in `packages/server` and `npm run build:weapp` in `packages/client` | add public DTO/service and client alignment tests | pending |
| 09-03-02 | 03 | 3 | MAP-01, MAP-02, MAP-03, MAP-04 | T-09-01 / T-09-07 | Seed migration and runtime compatibility survive the hierarchy reshape | smoke | `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-09-spatial.ps1` | add smoke script | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../CoordinateNormalization*` - deterministic raw/source/normalized conversion tests
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../AdminSubMap*` - admin city/sub-map CRUD and binding tests
- [ ] `packages/server/src/test/java/.../PublicSpatialCatalog*` - published city/sub-map/POI read and filter tests
- [ ] `scripts/local/smoke-phase-09-spatial.ps1` - end-to-end proof for admin spatial write, public read, and client compatibility

---

## Planned Execution Results

- `mvn test` in `packages/server`: pending
- `mvn test` in `packages/admin/aoxiaoyou-admin-backend`: pending
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`: pending
- `npm run build:weapp` in `packages/client`: pending
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-09-spatial.ps1`: pending

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Operator-triggered metadata suggestion for city/sub-map | MAP-01 | Suggestion quality and operator override clarity need visual confirmation | Open the city/sub-map editor, request metadata suggestions for Macau or ECNU, confirm the form previews suggested values without auto-saving and remains editable |
| Coordinate preview clarity in admin | MAP-03 | Need visual confirmation that raw input, source system, and normalized GCJ-02 are distinguishable | Enter WGS84 or BD-09 coordinates in a spatial form, confirm the preview shows source and normalized values side by side before save |
| Mini-program hierarchy UX | MAP-01, MAP-02, MAP-04 | The city/sub-map browsing flow is user-facing and build logs cannot prove the interaction model | Run the mini-program, switch top-level cities, then switch Macau sub-maps, and confirm POI filtering and map focus follow the rebuilt hierarchy |

---

## Validation Sign-Off

- [x] All tasks have an automated verification expectation or an explicit Wave 0 dependency
- [x] Sampling continuity is preserved across backend, admin UI, client, and smoke verification
- [x] Wave 0 captures the currently missing spatial, sub-map, and coordinate-normalization coverage
- [x] No watch-mode flags
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** planned 2026-04-14
