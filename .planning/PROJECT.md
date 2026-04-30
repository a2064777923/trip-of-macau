# Trip of Macau

## What This Is

Trip of Macau is a brownfield WeChat mini-program plus admin platform for story-driven Macau exploration, map discovery, indoor navigation, check-ins, collectibles, rewards, AI-assisted operations, and lifecycle governance.

The live backend cutover and three admin-platform milestones are archived. The current system has an admin-owned control plane for configuring story experiences, POI default flows, interaction/task templates, dynamic progress, seeded flagship content, public runtime DTOs, and operational lifecycle actions.

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

### Active

No active milestone is open.

The next requirements set should be created through `/gsd-new-milestone`.

### Out of Scope Until Planned

- Mini-program WeChat DevTools/device experiential acceptance and deeper traveler-facing interaction polish.
- Real AI material production from the Phase 33 manifest, including `image-2` image generation, material-board slicing, CosyVoice narration/audio, still-image motion video assembly, COS upload, and asset status promotion.
- Full publish approval workflow beyond direct lifecycle controls.
- End-user AI runtime feature pages beyond the existing admin AI capability center.
- `.lottie` packages, sprite sheets, sequence-frame animation systems, and complex AR/speech/puzzle gameplay implementations.

## Context

This project began from an existing codebase, not a greenfield build.

`v1.0` converted the brownfield runtime into a live stack with real admin writes, public reads and writes, COS-backed assets, and seeded MySQL-backed runtime flows.

`v2.0` rebuilt the admin-facing authoring model far enough to support multilingual content, spatial/media workflows, story composition, and indoor authoring basics, but it closed with accepted gaps.

`v2.1` absorbed those accepted gaps and pushed further into indoor interaction rules, a dedicated workbench and governance center, AI capability orchestration, reward-domain restructuring, and milestone-close verification.

`v3.0` completed the remaining admin core domains the user called out: story/content management, user/progress management, testing/operations management, and cross-domain linkage. It intentionally deferred full WeChat device experiential UAT and real generated material production.

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
- No active milestone is currently open.
- The truthful next workflow step is `/gsd-new-milestone`.

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
*Last updated: 2026-04-30 after shipping v3.0*
