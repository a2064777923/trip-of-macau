---
gsd_state_version: 1.0
milestone: v3.1
milestone_name: Material Production and Mini-program Experience Acceptance
current_phase: 36
current_plan: 5
status: executing
stopped_at: Phase 36 Wave 3 completed
last_updated: "2026-04-30T07:46:58.567Z"
last_activity: 2026-04-30 -- Phase 36 Wave 3 ffmpeg-gated video jobs completed with local ffmpeg blocker recorded
progress:
  total_phases: 5
  completed_phases: 0
  total_plans: 5
  completed_plans: 4
  percent: 80
---

# Project State

## Project Reference

See: `.planning/PROJECT.md` (updated 2026-04-30)

**Core value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.
**Current focus:** Phase 36 — material-production-pipeline-and-asset-promotion

## Current Position

Phase: 36 (material-production-pipeline-and-asset-promotion) — EXECUTING
Plan: 5 of 5
Milestone: `v3.1`
Current Phase: 36
Total Phases Planned: 5
Status: Executing Phase 36
Current Plan: 5
Last activity: 2026-04-30 -- Phase 36 Wave 3 ffmpeg-gated video jobs completed with local ffmpeg blocker recorded
Last Activity Description: Phase 36-05 added concrete MAT-04 chapter video job definitions and a fail-closed ffmpeg subtitle/video import builder; live video output remains blocked until ffmpeg with subtitles support is available locally

Progress: [########--] 80% of planned v3.1 plans complete

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

### Pending Todos

- Continue Phase 36 Wave 4 smoke verification and handoff with `/gsd-execute-phase 36`.
- Keep provider and COS secrets outside tracked files while implementing real asset generation/upload.
- Install or expose `ffmpeg` with subtitle filter support before claiming `MAT-04` complete.
- Verify generated assets and package publish/rollback behavior on local services before marking Phase 36 complete.

### Blockers/Concerns

- COS secrets and provider secrets must continue to stay outside tracked files.
- Real `image-2`, CosyVoice, and COS operations may incur external API cost and must remain explicit, logged, and reversible.
- Local Mongo still emits a warning on this workstation, though the admin HTTP stack remains healthy for verified admin flows.
- `spring-boot:run` currently needs `-Dmaven.test.skip=true` on this workstation because older server unit tests still reference stale constructor signatures, although `mvn -q -DskipTests compile -f packages/server/pom.xml` passes.

## Session Continuity

Last session: 2026-04-30T04:03:38.191Z
Stopped at: Phase 36 Wave 3 completed
Resume file: .planning/phases/36-material-production-pipeline-and-asset-promotion/36-04-PLAN.md
