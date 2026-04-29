---
phase: 29-poi-default-experience-workbench
status: passed
verified: 2026-04-29
requirements:
  accounted:
    - STORY-01
    - LINK-01
  missing: []
automated_checks:
  passed: 7
  failed: 0
human_verification: []
---

# Phase 29 Verification: POI Default Experience Workbench

## Verdict

Passed. Phase 29 achieved the POI default experience workbench goal: the admin system can load and author a POI default `walk_in` flow through a dedicated structured workbench, seed the A-Ma Temple acceptance flow, save a step as a reusable template, and expose the same published flow through the public POI runtime.

## Requirement Coverage

| Requirement | Verification result |
| --- | --- |
| STORY-01 | Passed. The admin control plane now has a dedicated POI default experience workbench and protected backend facade for POI natural walk-in flows. |
| LINK-01 | Passed. The implementation reuses Phase 28 experience tables, reusable templates, reward references, semantic exploration weights, and public runtime DTOs. |

## Must-Haves

- Passed: POI default experience authoring persists through canonical `experience_flows`, `experience_flow_steps`, `experience_bindings`, and `experience_templates`.
- Passed: The admin facade enforces `ownerType=poi`, `bindingRole=default_experience_flow`, `flowType=default_poi`, and `mode=walk_in`.
- Passed: Structured cards compile to `schemaVersion: 1` trigger, condition, and effect JSON.
- Passed: The POI workbench exposes timeline, condition, effect, media, reward, template-save, validation, and public runtime areas in Traditional Chinese.
- Passed: A-Ma Temple has a seeded seven-step default POI experience with semantic exploration weights.
- Passed: Live smoke verifies the admin facade, save-template behavior, and public runtime alignment.

## Automated Checks

- Passed: `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`.
- Passed: `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`; only the existing Vite chunk-size warning remains.
- Passed: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-29-poi-experience.ps1`.
- Passed: Source checks found POI facade routes, canonical binding vocabulary, `schemaVersion`, save-template support, and canonical step types.
- Passed: Source checks found POI workbench routes, sidebar entry, POI row action, structured card labels, invalid-submit focus handling, and public runtime path display.
- Passed: Source checks found seed and smoke coverage for `SET NAMES utf8mb4`, all seven canonical steps, semantic weights, `PHASE29_ADMIN_BEARER`, `PHASE29_PUBLIC_BEARER`, `HttpClient`, `Assert-NoStatusField`, and `Phase 29 POI experience smoke passed`.
- Passed: `node .codex/get-shit-done/bin/gsd-tools.cjs verify schema-drift 29` returned `drift_detected=false`.

## Live Stack Evidence

- Admin backend was restarted on `8081` after compile so `AdminPoiExperienceController` was loaded.
- The smoke script read the admin POI snapshot at `/api/admin/v1/pois/9/experience/default`.
- The smoke script saved `tap_intro` as `phase29_smoke_saved_template` and found it through `/api/admin/v1/experience/templates`.
- The smoke script read `/api/v1/experience/poi/9?locale=zh-Hant` from the public backend and verified all seven canonical step codes.
- The smoke script asserted public runtime flow and steps do not expose admin-only `status` fields.

## Review Gate

Passed. `29-REVIEW.md` status is `clean`. One UI submit bug was found and fixed during review before final verification was accepted.

## Residual Risks

- Phase 29 does not claim story chapter overrides, cross-domain governance, dynamic progress UI, flagship material production, or mini-program UX acceptance.
- The admin UI production build still emits the existing Vite chunk-size warning. This is not Phase 29-specific, but future UI phases should consider route-level code splitting.
- The local stack depends on valid ignored admin auth material or equivalent `PHASE29_*` environment variables for the smoke script.
