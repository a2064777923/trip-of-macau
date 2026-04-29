---
phase: 29-poi-default-experience-workbench
status: foundation-handoff
created: 2026-04-29
source:
  - 29-CONTEXT.md
  - 29-RESEARCH.md
  - 29-VALIDATION.md
  - 29-01-SUMMARY.md
  - 29-02-SUMMARY.md
  - 29-03-SUMMARY.md
---

# Phase 29 Handoff: POI Default Experience Workbench

Phase 29 turns the Phase 28 generic experience foundation into a dedicated POI default experience authoring path. Operators can now enter a POI-context workbench, edit a default `walk_in` flow through structured cards, save steps as reusable templates, and verify that the published public runtime consumes the same data.

## Delivered Artifacts

- Protected admin facade at `/api/admin/v1/pois/{poiId}/experience/**`.
- Frontend types and API helpers for POI experience snapshots, flow drafts, step payloads, validation findings, and save-template requests.
- Dedicated Traditional Chinese `POI 地點體驗工作台` route at `/space/poi-experience` and `/space/pois/:poiId/experience`.
- POI management row action `地點體驗` that routes directly to the selected POI's workbench.
- UTF-8 / utf8mb4 seed for the A-Ma Temple `poi_ama_default_walk_in` flow, reusable templates, seven canonical steps, and semantic exploration elements.
- Live smoke script covering admin snapshot, step save-template, template search, public runtime, canonical step codes, and public no-status leakage.

## Follow-On Contract

| Delivered capability | Follow-on consumer |
| --- | --- |
| POI default flow authoring | Phase 30 storyline chapters can inherit it. |
| POI step template saving | Phase 31 governance can aggregate usage. |
| semantic exploration weights | Phase 32 progress model can calculate denominator. |
| A-Ma Temple seeded default flow | Phase 33 flagship content can reuse or override it. |
| public POI runtime path | Phase 34 mini-program baseline can consume it. |

## Decisions Carried Forward

- POI default flows are persisted only through Phase 28 canonical `experience_*` tables.
- POI facade semantics are fixed as `ownerType=poi`, `bindingRole=default_experience_flow`, `flowType=default_poi`, and `mode=walk_in`.
- Structured card fields compile to `schemaVersion: 1` JSON for trigger, condition, and effect payloads.
- Reward rule IDs remain aligned with the existing non-versioned reward ID array behavior.
- Advanced JSON is available only as explicit fallback, not the primary UI.
- Exploration progress must continue to use semantic weights such as `tiny`, `small`, `medium`, `large`, and `core`.

## Requirement Coverage

| Requirement | Phase 29 coverage |
| --- | --- |
| STORY-01 | Passed. POI default experience authoring now exists as an admin-facing workbench and writes canonical bindings/flows consumed by the public runtime. |
| LINK-01 | Passed. The POI workbench reuses shared experience flows, templates, content assets, reward-rule references, and public runtime vocabulary instead of creating a parallel schema. |

## Non-Claims

- Phase 29 does not implement storyline chapter inheritance, disable, replace, or append overrides; that remains Phase 30.
- Phase 29 does not implement the cross-domain interaction/task governance center; that remains Phase 31.
- Phase 29 does not implement the full user progress UI or dynamic denominator management UI; that remains Phase 32.
- Phase 29 does not generate or upload the complete flagship story material package; that remains Phase 33.
- Phase 29 does not claim mini-program story-mode UX acceptance; that remains Phase 34 or later.

## Operational Notes

- The local admin backend on `8081` must be restarted after compiling new controller classes; otherwise `/api/admin/v1/pois/{poiId}/experience/default` can be handled by stale static-resource fallback.
- The Phase 29 smoke reads admin auth from `PHASE29_ADMIN_BEARER`, `PHASE29_ADMIN_USERNAME` / `PHASE29_ADMIN_PASSWORD`, or ignored local `tmp-admin-login.json`.
- The seed was imported locally with utf8mb4 and the active A-Ma flow steps were verified as exactly `tap_intro`, `start_route_guidance`, `arrival_intro_media`, `release_checkin_tasks`, `pickup_side_clues`, `hidden_dwell_achievement`, and `completion_reward_title`.
