# Trip of Macau

## What This Is

Trip of Macau is a brownfield WeChat mini-program plus admin platform for story-driven Macau exploration, map discovery, indoor navigation, check-ins, collectibles, rewards, AI-assisted operations, lifecycle governance, and operator-supported traveler gameplay.

The live backend cutover and five major platform milestones are archived or ready for archival through v3.2. The current system has an admin-owned control plane for configuring story experiences, POI default flows, interaction/task templates, dynamic progress, seeded flagship content, public runtime DTOs, generated media packages, operational lifecycle actions, traveler gameplay events, and support operations. The next milestone should be chosen around concrete device feedback, advanced gameplay depth, release governance, or production readiness.

## Core Value

Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## Requirements

### Validated

- `live admin/public backend cutover exists` - `v1.0` completed the canonical schema, admin control plane, public API cutover, COS media pipeline, and seeded live runtime verification path.
- `admin reconstruction exists` - `v2.0` rebuilt Traditional Chinese-first shell behavior, multilingual authoring foundations, spatial/media authoring, richer story/activity/collection flows, and indoor authoring basics.
- `interactive rules, AI, and rewards platform exists` - `v2.1` closed carryover control-plane gaps, indoor interaction-rule authoring, AI capability orchestration, reward-domain split, and milestone traceability.
- `story experience orchestration exists` - `v3.0` delivered shared experience templates, flows, bindings, overrides, Lottie-aware content/media substrate, POI default experience workbench, storyline mode, chapter overrides, and interaction/task governance.
- `dynamic traveler progress exists` - `v3.0` delivered weighted exploration progress, durable story sessions, safe recompute/repair operations, audit trails, and a Traditional Chinese traveler progress workbench.
- `flagship story package exists` - `v3.0` seeded the five-chapter `東西方文明的戰火與共生` story package across story, content block, experience, reward, and dynamic exploration tables, with a traceable planned material manifest.
- `public runtime consumption baseline exists` - `v3.0` exposed traveler-safe runtime DTOs and updated the mini-program story page to consume story runtime data, render content blocks/Lottie/audio/video, and degrade unsupported gameplay safely.
- `operations lifecycle control exists` - `v3.0` delivered dependency-aware lifecycle scheduling, canonical cross-domain status labels, publish/unpublish/remove operations, run-due execution, public filtering verification, and audit/history.
- `material production and QA pipeline exists` - `v3.1` Phases 36-37 generated/imported flagship story images, audio, videos, material-board slices, COS-backed assets, versioned promotion/rollback records, and Traditional Chinese QA/reuse controls.
- `public runtime generated-asset consumption exists` - `v3.1` Phase 38 enriched public runtime asset DTOs with availability/fallback/unsupported states, sanitized usage hints, privacy checks, and authenticated baseline story event/session smoke coverage.
- `mini-program story-mode baseline exists` - `v3.1` Phase 39 connected the story page and map handoff to live public runtime data, generated media assets, idempotent story events, pickups/rewards/progress, and Traditional Chinese fallback states.
- `release readiness and AI cost visibility exist` - `v3.1` Phase 40 added the release-readiness smoke bundle, AI monitoring/cost/job-history UI, scoped history visibility, acceptance report, and WeChat DevTools/device UAT checklist.
- `traveler gameplay runtime baseline exists` - `v3.2` Phases 41-42 hardened the mini-program story entry path and added baseline event handling for runtime steps, pickups, tasks, rewards, titles, sessions, and unsupported-feature fallbacks.
- `traveler support operations exist` - `v3.2` Phase 43 connected progress, sessions, events, rewards, titles, annotations, reward resend, and rule trace into admin support workflows.
- `management-system release polish exists` - `v3.2` Phase 44 cleaned story/gameplay IA, media/material preview states, operator helper copy, and release-readiness evidence structure.
- `storyline lifecycle safety exists` - `v3.2` Phase 45 added impact-aware archive/delete handling and made the mini-program prefer the canonical `east_west_war_and_coexistence` runtime over the archived duplicate.

### Active

- [ ] Collect concrete WeChat DevTools or physical-device flagship story smoke evidence for the v3.2 story journey.
- [ ] Define the next milestone scope from real mini-program feedback, advanced gameplay priorities, release approval workflow, or production-readiness needs.

### Out of Scope Until Planned

- Full publish approval workflow beyond direct lifecycle controls.
- End-user AI runtime feature pages beyond the existing admin AI capability center.
- Full production-grade AR recognition, speech-input gameplay, and complex puzzle engines beyond baseline placeholders.
- `.lottie` packages, sprite sheets, and sequence-frame animation systems.

## Context

This project began from an existing codebase, not a greenfield build.

`v1.0` converted the brownfield runtime into a live stack with real admin writes, public reads and writes, COS-backed assets, and seeded MySQL-backed runtime flows.

`v2.0` rebuilt the admin-facing authoring model far enough to support multilingual content, spatial/media workflows, story composition, and indoor authoring basics, but it closed with accepted gaps.

`v2.1` absorbed those accepted gaps and pushed further into indoor interaction rules, a dedicated workbench and governance center, AI capability orchestration, reward-domain restructuring, and milestone-close verification.

`v3.0` completed the remaining admin core domains the user called out: story/content management, user/progress management, testing/operations management, and cross-domain linkage. It intentionally deferred full WeChat device experiential UAT and real generated material production.

`v3.1` promoted the flagship story material package from planned records into generated/imported COS-backed assets, added QA/reuse controls, aligned public runtime asset/event contracts, and connected the mini-program story-mode baseline to the live public backend. It archived with an accepted caveat that manual WeChat DevTools/device UAT remains pending.

`v3.2` started from that caveat and shipped the planned runtime/support/IA/lifecycle work. It keeps WeChat DevTools and physical-device flagship-story evidence as an accepted caveat instead of falsely marking device acceptance complete.

## Constraints

- **Tech stack**: Preserve the current brownfield stack: Taro/React mini-program, Spring Boot + MyBatis-Plus public/admin backends, existing admin UI, and current local Docker-based MySQL/Mongo setup.
- **Backend ownership**: `packages/server` remains the primary public API backend for the mini-program.
- **Admin ownership**: `/admin` remains the authoritative control plane for mini-program-facing content, settings, media, and operator workflows.
- **Database**: MySQL remains the primary operational data store for live content/runtime/admin integration.
- **Media**: File/image/audio/video uploads continue through backend APIs into Tencent COS with canonical asset metadata persistence.
- **Security**: Secrets must stay in environment/runtime configuration, never in tracked files.
- **Encoding**: All multilingual content, SQL seed data, CSV imports, JSON payloads, and scripted writes must use UTF-8/utf8mb4 end-to-end; avoid inline PowerShell Chinese writes.
- **Verification**: Interfaces must be runnable and actually tested locally before work is considered complete.
- **Quality attributes**: High availability, ease of use, extensibility, performance, and operator usability remain first-class constraints.

## Current State

- `v1.0 Live Backend Cutover` shipped on 2026-04-13.
- `v2.0 Admin Control-Plane Reconstruction` archived on 2026-04-15 with accepted gaps.
- `v2.1 Interactive Rules Platform and AI Capability Center` archived on 2026-04-19 with one accepted future slice.
- `v3.0 Admin Core Domain Completion and Control-Plane Linkage` shipped on 2026-04-30.
- `v3.1 Material Production and Mini-program Experience Acceptance` shipped on 2026-05-04 with an accepted WeChat UAT caveat.
- `v3.2 Traveler Gameplay Runtime and Operations Acceptance` shipped on 2026-05-06 with an accepted WeChat DevTools/physical-device UAT caveat.

## Between Milestones

v3.2 planned implementation is complete. The next step is to start a new milestone or collect concrete WeChat DevTools/physical-device UAT evidence before deciding the next scope.

**Open caveat:**
- `UAT-02`: concrete DevTools or physical-device flagship story smoke evidence is still pending in `44-UAT.md`.

**Likely next milestone themes:**
- Real device UAT and mini-program UX/gameplay polish.
- Advanced gameplay engines for AR/photo, speech, puzzle, route coverage, cannon defense, and indoor positioning.
- Release governance such as approval workflow and runtime snapshot comparison.
- Performance hardening, especially story-page bundle splitting.

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Keep `packages/server` as the public API surface | The mini-program backend cutover must stay centered on the existing public service | All traveler-facing runtime contracts remain anchored in `packages/server`. |
| Keep `/admin` as the authoritative control plane | The user wants all live content and settings authored centrally | Admin remains the write surface for content, media, runtime settings, progress tooling, and lifecycle operations. |
| Keep MySQL as the primary source of truth | The live stack and seeded content flows must stay aligned to the existing local environment | Canonical runtime and control-plane data remains MySQL-backed. |
| Keep COS uploads backend-owned | Asset governance and canonical URLs must stay consistent across admin and public surfaces | COS-backed media continues through backend APIs and canonical asset metadata. |
| Preserve explicit carryover instead of faking closure | Pretending closure caused planning drift in earlier milestones | Archived milestones keep explicit carryovers and future slices instead of hidden debt. |
| Treat story, user, and operations as linked domains | These domains were incomplete when isolated as CRUD pages | `v3.0` shipped them as connected control-plane systems. |
| Defer mini-program experiential UAT beyond `v3.0` | The user explicitly postponed that slice | `v3.0` validates contracts and baseline rendering, not full device gameplay. |
| Keep generated materials as planned until a dedicated production phase | Real generation touches external API cost, asset selection, slicing, upload, and DB promotion | Phase 33 records prompts, COS keys, and asset IDs; actual `image-2`/audio/video production remains future scope. |
| Use `v3.1` for material production and mini-program acceptance | This is a bounded follow-on to v3.0 rather than a new platform rewrite | Phases 36-38 completed material production, QA, and public runtime consumption; Phase 39+ now targets mini-program and release acceptance. |
| Archive v3.1 with pending manual WeChat UAT caveat | The backend/runtime/build evidence passed, but device UAT was not honestly completed | v3.1 is shipped with `40-UAT.md` pending; future milestones should not treat full gameplay as already delivered. |
| Use `v3.2` for traveler gameplay runtime and operations acceptance | The admin configuration substrate exists, but the user-facing gameplay loop still needs real device validation and deeper runtime state handling | v3.2 starts at Phase 41 and prioritizes mini-program playability plus operator support over more admin-only scaffolding. |
| Keep complex AR/speech/puzzle engines behind safe runtime templates until proven on device | These features affect device APIs, permissions, latency, and UX; implementing them as fully production-grade engines prematurely would create brittle scope | v3.2 can implement baseline playable engines and fallbacks, while production AR/speech/advanced minigames remain explicitly gated by device UAT evidence. |
| Archive duplicate storylines rather than hard-delete them | Storylines can still be referenced by chapters, content relations, exploration events, sessions, and progress rows | Phase 45 archives `macau_fire_route`, blocks hard delete for dependency-bearing stories, and keeps `east_west_war_and_coexistence` as the canonical runtime. |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition:**
1. Move validated work from Active to Validated.
2. Record new scope, decisions, or constraints that materially change the project.
3. Keep milestone and carryover status honest.

**After each milestone:**
1. Re-check the core value and active scope.
2. Audit accepted carryovers and move only bounded deferred work forward.
3. Refresh the current-state and milestone sections before reopening planning.

---
*Last updated: 2026-05-06 after shipping v3.2 traveler gameplay runtime and operations acceptance*
