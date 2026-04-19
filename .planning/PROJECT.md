# Trip of Macau

## What This Is

Trip of Macau is a brownfield travel mini-program plus admin platform for story-driven city exploration, map discovery, check-ins, collectibles, indoor navigation, AI-assisted operations, and operator tooling. The live backend cutover and two follow-on platform milestones are now archived, and the project is waiting for the next milestone definition.

## Core Value

Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## Requirements

### Validated

- `live admin/public backend cutover exists` - `v1.0` completed the canonical schema, admin control plane, public API cutover, COS media pipeline, and seeded live runtime verification path.
- `admin reconstruction exists` - `v2.0` rebuilt Traditional Chinese-first shell behavior, multilingual authoring foundations, spatial/media authoring, richer story/activity/collection flows, and indoor authoring basics.
- `carryover control-plane closure exists` - `v2.1` Phase 20 formally re-verified the carried `v2.0` control-plane gaps on the live stack.
- `indoor interaction-rule platform exists` - `v2.1` Phases 15, 16, and 21 closed the indoor authoring, workbench, governance, and public/runtime rule chain.
- `AI capability platform exists` - `v2.1` Phases 18, 19, 22, 24, and 26 closed the AI capability center, provider/model orchestration, creative workbench, TTS voice tooling, and verification ownership.
- `reward-domain platform exists` - `v2.1` Phases 23 and 25 closed the reward-domain split, shared-rule synchronization, and acquisition-presentation verification path.
- `milestone traceability reconciliation exists` - `v2.1` Phase 27 closed the remaining milestone-state drift and formalized the accepted carryover for `RULE-03`.

### Active

- No new milestone requirement set is defined yet.
- The next milestone must carry forward the bounded WeChat DevTools experiential acceptance slice for the mini-program indoor runtime.
- The next milestone should decide whether to batch that acceptance work with broader mini-program frontend/runtime linkage validation.

### Out of Scope

- Reopening archived milestones as active work instead of carrying forward explicit unresolved slices.
- Replacing the current Taro / Spring Boot / MySQL brownfield stack.
- Treating mock data as the primary runtime source after the live backend cutover.
- Committing cloud secrets, COS credentials, or provider secrets into tracked files.
- Shipping every future end-user AI experience page inside the same milestone as the control-plane AI platform.

## Context

This project began from an existing codebase, not a greenfield build. `v1.0` converted that brownfield into a live stack with real admin writes, public reads and writes, COS-backed assets, and seeded MySQL-backed runtime flows.

`v2.0` rebuilt the admin-facing authoring model far enough to support multilingual content, spatial/media workflows, story composition, and indoor authoring basics, but it closed with accepted gaps.

`v2.1` absorbed those accepted gaps and pushed further into platform work: indoor interaction rules, a dedicated workbench and governance center, AI capability orchestration, reward-domain restructuring, and milestone-close verification and traceability reconciliation.

The only deferred slice after `v2.1` archival is the accepted carryover for real WeChat DevTools experiential acceptance on the mini-program indoor runtime.

## Constraints

- **Tech stack**: Preserve the current brownfield stack: Taro/React mini-program, Spring Boot + MyBatis-Plus public/admin backends, existing admin UI, and current local Docker-based MySQL/Mongo setup.
- **Backend ownership**: `packages/server` remains the primary public API backend for the mini-program.
- **Admin ownership**: `/admin` remains the authoritative control plane for mini-program-facing content, settings, media, and operator workflows.
- **Database**: MySQL remains the primary operational data store for live content/runtime/admin integration.
- **Media**: File/image/audio/video uploads continue through backend APIs into Tencent COS with canonical asset metadata persistence.
- **Security**: Secrets must stay in environment/runtime configuration, never in tracked files.
- **Verification**: Interfaces must be runnable and actually tested locally before work is considered complete.
- **Quality attributes**: High availability, ease of use, extensibility, performance, and operator usability remain first-class constraints.

## Current State

- `v1.0 Live Backend Cutover` shipped on 2026-04-13.
- `v2.0 Admin Control-Plane Reconstruction` was archived on 2026-04-15 with accepted gaps that were later carried into `v2.1`.
- `v2.1 Interactive Rules Platform and AI Capability Center` was archived on 2026-04-19 after phases `14-27` closed with milestone-grade verification and one accepted carryover.
- There is currently no active milestone.
- The next truthful workflow step is `/gsd-new-milestone`.

## Last Shipped Milestone: v2.1 Interactive Rules Platform and AI Capability Center

**Archived:** 2026-04-19

**Delivered:**

- closed the accepted `v2.0` control-plane gaps with explicit live verification
- delivered indoor interaction-rule authoring, workbench UX, governance, and runtime closure
- delivered the AI capability platform, provider/model orchestration, TTS tooling, and consolidated verification ownership
- delivered the reward-domain split, shared-rule synchronization, and acquisition-presentation closure
- reconciled milestone traceability so no requirement remains in stale partial state

**Accepted carryover:**

- WeChat DevTools experiential acceptance for the mini-program indoor runtime moves to the next milestone

## Next Milestone Goals

- Define the next milestone requirement set and roadmap.
- Carry forward the bounded WeChat DevTools experiential acceptance slice for the mini-program indoor runtime.
- Decide how much mini-program frontend/runtime linkage acceptance should be grouped into that same milestone.

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Keep `packages/server` as the public API surface | The mini-program backend cutover must stay centered on the existing public service | All live runtime and traveler-facing contracts remain anchored in `packages/server`. |
| Keep `/admin` as the authoritative control plane | The user wants all live content and settings to be authored centrally | Admin remains the write surface for content, media, and runtime settings. |
| Keep MySQL as the primary source of truth | The live stack and seeded content flows must stay aligned to the existing local environment | Canonical runtime and control-plane data remains MySQL-backed. |
| Keep COS uploads backend-owned | Asset governance and canonical URLs must stay consistent across admin and public surfaces | COS-backed media continues to flow through backend APIs and canonical asset metadata. |
| Archive imperfect milestones honestly | The project already proved that pretending closure is worse than carrying forward explicit gaps | `v2.0` was archived with accepted gaps, and `v2.1` carried those gaps into formal closure work. |
| Split indoor rule delivery into authoring, workbench, runtime, and closure phases | Operator-fit and runtime proof were too large to hide in one implementation pass | `v2.1` carried indoor work through phased platformization and later closure. |
| Consolidate fragmented AI closure into a later verification phase | AI delivery landed across multiple phases and needed one final ownership point | Phase 26 became the canonical closure artifact for `AI-04` through `AI-08`. |
| Treat the remaining mini-program experiential slice as accepted carryover | The user explicitly deferred the WeChat DevTools acceptance pass rather than blocking milestone sequencing | Phase 27 closed `RULE-03` through accepted carryover and moved that slice to the next milestone. |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition:**
1. Move validated work from Active to Validated.
2. Record new scope, decisions, or constraints that materially change the project.
3. Keep milestone and carryover status honest.

**After each milestone:**
1. Re-check the core value and active scope.
2. Audit accepted carryovers and move only bounded deferred work forward.
3. Refresh the current-state and next-goals sections before reopening planning.

---
*Last updated: 2026-04-19 after archiving v2.1*
