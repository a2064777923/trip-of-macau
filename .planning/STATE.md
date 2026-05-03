---
gsd_state_version: 1.0
milestone: v3.1
milestone_name: Material Production and Mini-program Experience Acceptance
current_phase: 37
current_plan: 2
status: executing
stopped_at: Completed 37-01-PLAN.md
last_updated: "2026-05-03T00:58:07.573Z"
last_activity: 2026-05-03
progress:
  total_phases: 5
  completed_phases: 1
  total_plans: 8
  completed_plans: 6
  percent: 75
---

# Project State

## Project Reference

See: `.planning/PROJECT.md` (updated 2026-04-30)

**Core value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.
**Current focus:** Phase 37 — material-qa-workspace-and-reuse-controls

## Current Position

Phase: 37 (material-qa-workspace-and-reuse-controls) — EXECUTING
Plan: 2 of 3
Milestone: `v3.1`
Current Phase: 37
Total Phases Planned: 5
Status: Executing
Current Plan: 2
Last activity: 2026-05-03
Last Activity Description: Phase 37 execution started

Progress: [████████░░] 75% of v3.1 plans complete; Phase 37 plan 2 ready

## Performance Metrics

**Completed milestones:**

- `v1.0`: 6 phases, 19 plans, live backend cutover shipped on 2026-04-13
- `v2.0`: 6 executed phases, 15 plans, archived with accepted gaps on 2026-04-15
- `v2.1`: 14 phases, 44 plans, archived with one accepted future slice on 2026-04-19
- `v3.0`: 8 phases, 33 plans, admin core domain completion and control-plane linkage shipped on 2026-04-30

## Quick Tasks Completed

| Date | Task | Summary |
| --- | --- | --- |
| 2026-04-29 | `260429-ocp` | Cleaned local verification artifacts, documented admin IA, removed confirmed dead admin entry points, and browser-verified the retained admin control plane. |
| Phase 33 P33-03 | 55 min | 2 tasks | 2 files |
| Phase 33 P33-04 | 35 min | 4 tasks | Admin story material package page, route/sidebar, smoke script, and handoff docs |
| 2026-05-02 | `260502-p19` | Clarified material asset usability states, repaired media preview/error states, and browser-fixed story/experience IA/layout issues. |
| 2026-05-02 | Phase 36 UAT/security | Completed 6/6 UAT checkpoints, created `36-SECURITY.md`, and tightened publish/rollback super-admin authorization. |
| 2026-05-02 | `260502-so0` | Added env-backed custom image endpoint support, generated 37 local PNG story assets, and recorded remaining live COS/audio/video blockers. |
| 2026-05-02 | `260502-urn` | Verified live 百煉 audio generation, backend COS upload, finalized content assets, and published material package versions for 6 Phase 36 audio items. |
| 2026-05-02 | `260502-w21` | Verified live compatible-image import, 35 board-sliced pickup/title icons, COS upload, published versions, rollback smoke, and prepared video closure. |
| 2026-05-03 | Phase 36 video closure | Generated five chapter MP4s from stills and narration with portable ffmpeg, imported them through backend production APIs, promoted them to COS, and passed live smoke with video checks. |
| Phase 37 P37-01 | 35 min | 3 tasks | 6 files |

## Accumulated Context

### Decisions

- Keep `packages/server` as the public API surface for the mini-program.
- Keep `/admin` as the authoritative control plane for mini-program content, settings, media, and operator workflows.
- Keep MySQL as the primary source of truth for live runtime content and admin/public integration.
- Preserve explicit carryover instead of faking milestone closure.
- Treat the deferred mini-program frontend acceptance work as future scope rather than forcing it into `v3.0`.
- Complete story/content, user/progress, and operations/testing as linked domains rather than isolated CRUD pages.
- Phase 34 public runtime smoke is the v3.0 closure evidence for public-runtime linkage, not a substitute for full WeChat device journey UAT.
- Future material production can assemble video from one or several `image-2` stills with pan/zoom/motion and narration/audio stitching; keep using material boards and local slicing to save generation cost.
- v3.1 adopts that future scope as active work: real generated assets, asset QA/promotion, and mini-program story-mode acceptance.
- Phase 36 publish/rollback operations require both `SUPER_ADMIN` / `ROLE_SUPER_ADMIN` and an explicit confirmation flag; a confirmation boolean alone is not authorization.

### Pending Todos

- Keep provider and COS secrets outside tracked files while implementing real asset generation/upload.
- Verify generated assets and package publish/rollback behavior on local services before marking Phase 36 complete.

### Blockers/Concerns

- COS secrets and provider secrets must continue to stay outside tracked files.
- Phase 36 image/board/audio/video/COS is verified. Burned-in subtitles degraded to external caption metadata for chapter videos on this workstation.
- Real `image-2`, CosyVoice, and COS operations may incur external API cost and must remain explicit, logged, and reversible.
- Local Mongo still emits a warning on this workstation, though the admin HTTP stack remains healthy for verified admin flows.
- `spring-boot:run` currently needs `-Dmaven.test.skip=true` on this workstation because older server unit tests still reference stale constructor signatures, although `mvn -q -DskipTests compile -f packages/server/pom.xml` passes.

## Session Continuity

Last session: 2026-05-03T00:58:07.569Z
Stopped at: Completed 37-01-PLAN.md
Resume file: .planning/phases/37-material-qa-workspace-and-reuse-controls/37-02-PLAN.md
