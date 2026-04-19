# Trip of Macau

## What This Is

Trip of Macau is a brownfield travel mini-program plus admin platform for story-driven city exploration, map discovery, check-ins, collectibles, indoor navigation, AI-assisted operations, and operator tooling. The live backend cutover and the first two admin-platform milestones are archived; `v3.0` now focuses on completing the remaining unfinished admin core domains and the linkage between them.

## Core Value

Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## Requirements

### Validated

- `live admin/public backend cutover exists` - `v1.0` completed the canonical schema, admin control plane, public API cutover, COS media pipeline, and seeded live runtime verification path.
- `admin reconstruction exists` - `v2.0` rebuilt Traditional Chinese-first shell behavior, multilingual authoring foundations, spatial/media authoring, richer story/activity/collection flows, and indoor authoring basics.
- `carryover control-plane closure exists` - `v2.1` re-verified the carried `v2.0` control-plane gaps on the live stack.
- `indoor interaction-rule platform exists` - `v2.1` closed the indoor authoring, workbench, governance, and public/runtime rule chain.
- `AI capability platform exists` - `v2.1` closed the AI capability center, provider/model orchestration, creative workbench, TTS voice tooling, and verification ownership.
- `reward-domain platform exists` - `v2.1` closed the reward-domain split, shared-rule synchronization, and acquisition-presentation verification path.
- `milestone traceability reconciliation exists` - `v2.1` reconciled the remaining planning-state drift and closed with one explicit accepted future slice.

### Active

- `v3.0` must complete the unfinished story and content management domain so storylines, chapters, unlock logic, content blocks, and narrative bindings are no longer partial or placeholder-driven.
- `v3.0` must complete the unfinished user and progress management domain so traveler profile, progress, logs, and recomputation become first-class operational surfaces.
- `v3.0` must complete the unfinished testing and operations management domain so publish windows, lifecycle actions, operational dashboards, and smoke tooling are operator-usable instead of scattered or missing.
- `v3.0` must align the completed admin domains with the existing map, POI, indoor, reward, media, AI, and public-backend contracts so the control plane behaves as one linked system instead of isolated CRUD screens.
- `v3.0` must ship with meaningful seeded examples and milestone-grade verification on the live local admin/public stack.

### Out of Scope

- Reopening archived milestones as active work instead of carrying their explicit follow-on slices forward.
- Replacing the current Taro / Spring Boot / MySQL brownfield stack.
- Treating mock data as the primary runtime source after the live backend cutover.
- Committing cloud secrets, COS credentials, or provider secrets into tracked files.
- The deferred mini-program WeChat DevTools and broader frontend experiential acceptance slice, which the user explicitly moved to a later milestone.

## Context

This project began from an existing codebase, not a greenfield build. `v1.0` converted that brownfield into a live stack with real admin writes, public reads and writes, COS-backed assets, and seeded MySQL-backed runtime flows.

`v2.0` rebuilt the admin-facing authoring model far enough to support multilingual content, spatial/media workflows, story composition, and indoor authoring basics, but it closed with accepted gaps.

`v2.1` absorbed those accepted gaps and pushed further into platform work: indoor interaction rules, a dedicated workbench and governance center, AI capability orchestration, reward-domain restructuring, and milestone-close verification and traceability reconciliation.

`v3.0` now picks up the still-incomplete admin core domains that the user explicitly called out: story/content management, user/progress management, testing/operations management, and the missing linkage between those domains and the rest of the control plane.

## Constraints

- **Tech stack**: Preserve the current brownfield stack: Taro/React mini-program, Spring Boot + MyBatis-Plus public/admin backends, existing admin UI, and current local Docker-based MySQL/Mongo setup.
- **Backend ownership**: `packages/server` remains the primary public API backend for the mini-program.
- **Admin ownership**: `/admin` remains the authoritative control plane for mini-program-facing content, settings, media, and operator workflows.
- **Database**: MySQL remains the primary operational data store for live content/runtime/admin integration.
- **Media**: File/image/audio/video uploads continue through backend APIs into Tencent COS with canonical asset metadata persistence.
- **Security**: Secrets must stay in environment/runtime configuration, never in tracked files.
- **Verification**: Interfaces must be runnable and actually tested locally before work is considered complete.
- **Quality attributes**: High availability, ease of use, extensibility, performance, and operator usability remain first-class constraints.
- **Milestone boundary**: `v3.0` focuses on admin/public control-plane completion and linked contracts, not the deferred mini-program frontend acceptance pass.

## Current State

- `v1.0 Live Backend Cutover` shipped on 2026-04-13.
- `v2.0 Admin Control-Plane Reconstruction` was archived on 2026-04-15 with accepted gaps that were later carried into `v2.1`.
- `v2.1 Interactive Rules Platform and AI Capability Center` was archived on 2026-04-19 after phases `14-27` closed with milestone-grade verification and one accepted future slice.
- `v3.0` is now the active milestone.
- The next truthful workflow step is `/gsd-plan-phase 28`.

## Current Milestone: v3.0 Admin Core Domain Completion and Control-Plane Linkage

**Goal:** Complete the still-unfinished admin core domains and link them cleanly with the existing map, POI, indoor, reward, AI, media, and public-backend contracts.

**Target features:**
- complete story and content management with narrative composition, unlock logic, multimedia content blocks, and linked bindings
- complete user and progress management with profile drill-down, progress recomputation, journey logs, and operational visibility
- complete testing and operations management with lifecycle control, publish scheduling, health views, and smoke tooling
- complete cross-domain linkage so story, user, ops, and existing authored entities share canonical bindings and runtime contracts
- seed meaningful example data and verify the live local admin/public stack end to end

## Last Shipped Milestone: v2.1 Interactive Rules Platform and AI Capability Center

**Archived:** 2026-04-19

**Delivered:**

- closed the accepted `v2.0` control-plane gaps with explicit live verification
- delivered indoor interaction-rule authoring, workbench UX, governance, and runtime closure
- delivered the AI capability platform, provider/model orchestration, TTS tooling, and consolidated verification ownership
- delivered the reward-domain split, shared-rule synchronization, and acquisition-presentation closure
- reconciled milestone traceability so no requirement remained in a stale partial state

**Accepted future slice:**

- the mini-program WeChat DevTools and broader frontend experiential acceptance work remains intentionally deferred beyond `v3.0`

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Keep `packages/server` as the public API surface | The mini-program backend cutover must stay centered on the existing public service | All live runtime and traveler-facing contracts remain anchored in `packages/server`. |
| Keep `/admin` as the authoritative control plane | The user wants all live content and settings to be authored centrally | Admin remains the write surface for content, media, and runtime settings. |
| Keep MySQL as the primary source of truth | The live stack and seeded content flows must stay aligned to the existing local environment | Canonical runtime and control-plane data remains MySQL-backed. |
| Keep COS uploads backend-owned | Asset governance and canonical URLs must stay consistent across admin and public surfaces | COS-backed media continues to flow through backend APIs and canonical asset metadata. |
| Preserve explicit carryover instead of faking closure | The project already proved that pretending closure is worse than carrying forward explicit gaps | Archived milestones keep explicit carryovers instead of hidden debt. |
| Treat story, user, and operations work as one linked milestone | The user explicitly called out that these remaining domains are still unfinished and not truly integrated | `v3.0` is scoped around domain completion plus linkage, not isolated page-by-page fixes. |
| Defer mini-program experiential acceptance beyond `v3.0` | The user explicitly chose to postpone that slice after defining the new milestone | `v3.0` excludes the deferred WeChat DevTools and broader mini-program frontend acceptance work. |
| Continue phase numbering from the previous milestone | The repo already uses cumulative numbering across milestones | `v3.0` starts at Phase 28. |

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
*Last updated: 2026-04-19 after starting v3.0*
