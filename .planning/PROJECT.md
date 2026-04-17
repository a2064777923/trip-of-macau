# Trip of Macau

## What This Is

Trip of Macau is a brownfield travel mini-program plus admin platform for story-driven city exploration, map discovery, check-ins, collectibles, tips, indoor navigation, and operations tooling. The live backend cutover and the first admin control-plane reconstruction pass are complete. The current project focus is `v2.1`: close the accepted `v2.0` control-plane gaps while building the indoor interaction-rules platform and the AI capability center.

## Core Value

Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## Requirements

### Validated

- `mini-program shell exists` - `packages/client` already contains the traveler-facing page structure, map UX, tips, profile/settings, and local gameplay state flows.
- `admin platform baseline exists` - `packages/admin/aoxiaoyou-admin-ui` and `packages/admin/aoxiaoyou-admin-backend` already provide map/content/system management surfaces and supporting Java services.
- `local brownfield environment exists` - `docker-compose.local.yml`, MySQL/Mongo init scripts, and local startup helpers already support local service bootstrapping.
- `canonical contract and shared enums exist` - `docs/integration/miniapp-admin-public-contract.md` plus mirrored public/admin enums now lock publish-state, locale, and asset-kind semantics in Phase 1.
- `canonical MySQL foundation exists` - `docs/database/live-backend-schema-mapping.md` and `scripts/local/mysql/init/02-live-backend-foundation.sql` establish the live backend schema and bootstrap scaffold in Phase 1.
- `local backend foundation is runnable` - `packages/server` local profile wiring, `start-public-backend.cmd`, and `smoke-phase-01-foundation.ps1` were verified against real local MySQL/Mongo and both backend health endpoints in Phase 1.
- `admin control plane is live against canonical MySQL domains` - Phase 2 completed real CRUD/read coverage for cities, POIs, storylines, chapters, rewards, runtime settings, assets, tips, notifications, and stamps, then verified them against a live admin backend on port `18081`.
- `public read/write APIs are live for the current mini-program domains` - Phases 3 and 4 replaced the core mock-backed public catalog/runtime/user-write paths with real `packages/server` endpoints verified against live local MySQL on `8080`.
- `backend-managed COS media pipeline is live` - Phase 5 added authenticated admin uploads into Tencent COS, canonical `content_assets` persistence, admin upload UX, public canonical URL reuse, and live COS/MySQL/public verification on `8081` + `8080`.
- `repeatable mock-data migration is live` - Phase 6 added `06-live-backend-mock-migration.sql` and `apply-phase-06-mock-seed.ps1` so the former client mock dataset can be restored into canonical MySQL tables and runtime settings on demand.
- `end-to-end admin/public/client verification is live` - Phase 6 added `smoke-phase-06-live-cutover.ps1` and re-verified admin writes, COS upload, public reads/writes, MySQL persistence, and admin traveler inspection against the running local stack.
- `Traditional Chinese-first admin shell and real-auth baseline landed` - `v2.0` Phase 7 aligned the shell, branding, and real-auth contract foundations across admin/public/client.
- `four-language authoring and translation settings foundation landed` - `v2.0` Phase 8 added `zh-Hant` / `zh-Hans` / `en` / `pt` field patterns plus translation-engine settings groundwork.
- `canonical spatial and media authoring rebuild landed` - `v2.0` Phases 9-10 rebuilt cities, sub-maps, POIs, coordinate normalization, and COS-backed media-library workflows.
- `story, activity, collection, and indoor authoring foundations landed` - `v2.0` Phases 11-12 connected richer content authoring and indoor map basics to public APIs and the mini-program runtime.
- `AI capability center platform surface landed` - `v2.1` Phase 18 delivered provider configuration, secret-safe governance, quota controls, overview visibility, and bounded creative-foundation history on the live admin stack.

### Active

- [ ] `carryover v2.0 control-plane gaps are closed honestly` - collection/reward authoring, user progress, operations/testing alignment, and system settings must stop living as accepted gaps and be brought to a verifiable state.
- [ ] `indoor interaction rules become a real authored platform` - operators need configurable appearance conditions, trigger chains, effects, and path-based behaviors for indoor markers and overlays.
- [ ] `complex indoor rule editing gets its own workbench UX` - the densest rule-authoring flow should move into a dedicated validated editor with naming, apply/save control, and thumbnail-assisted point/path authoring instead of staying inline in the base form.
- [ ] `rule governance is visible across entities, not only per marker` - operators need one place to filter, inspect, enable/disable, and detect overlap/conflict across points, interaction objects, rewards, and trigger chains.
- [ ] `mini-program indoor runtime can evaluate authored rules safely` - public APIs and the mini-program must execute the configured indoor rules predictably without falling back to mock-only logic.
- [ ] `milestone closeout verification becomes stricter than v2.0` - the next milestone needs explicit smoke/UAT/audit closure rather than archive-time assumptions.

### Out of Scope

- Keeping mock data as the primary runtime source after the live backend cutover - mocks may remain as temporary fallback/test fixtures only.
- Committing cloud secrets, COS credentials, or local-only integration files into the repository - secrets must stay in environment/configuration outside tracked docs and source control.
- Reopening `v2.0` as the active milestone - accepted `v2.0` gaps are carried forward into `v2.1` instead of pretending the archived milestone is still in flight.
- Replacing the current Taro / Spring Boot / MySQL brownfield stack - the current milestone is about platform completion, not stack migration.
- Shipping every downstream AI feature page in the same milestone as the AI capability center - `v2.1` focuses on provider/governance infrastructure first, not every future AI experience.
- Fully autonomous external content crawling without operator review - suggested defaults may help, but human-reviewed authoring remains the control-plane model.

## Context

This project starts from an existing codebase, not a blank slate. The mini-program still centralizes much orchestration in `packages/client/src/services/gameService.ts`, but the live runtime now reads and writes through real public APIs backed by MySQL instead of the former `gameMock.ts` catalog fallback. The admin side had meaningful Java and React foundations, and the first milestone extended them into the authoritative control plane for mini-program content, runtime settings, and operator visibility. The public backend in `packages/server` is now the live contract surface for the traveler experience.

The codebase map in `.planning/codebase/` found several issues that directly affected this effort: the public backend had only partial domain coverage, the admin security layer is interceptor-driven, and the public/backend contract and configuration layer had brownfield mismatches that blocked direct cutover. The local environment uses MySQL and MongoDB, but the user's request made MySQL the critical source of truth for live mini-program/admin integration. MongoDB remains optional for document-style or future admin extensions, not the primary source for current mini-program runtime content. Phases 1 through 6 resolved the missing public-backend interceptor dependency, completed the canonical schema/contract scaffold, finished the admin control plane, cut public read/write flows over to live APIs, added the COS media pipeline, migrated the former mock dataset into canonical storage, and closed the final end-to-end verification loop.

The user wants this work to be production-minded: highly configurable, extensible, high-performance, and actually runnable. That raises the bar beyond "expose some endpoints" to include schema quality, publish-state semantics, asset management, migration strategy, smoke checks, and end-to-end verification.

The archived `v2.0` milestone handled the first practical control-plane reconstruction: Traditional Chinese-first admin shell cleanup, four-language authoring foundations, rebuilt spatial/media models, richer story/activity/collection flows, and indoor authoring basics. It also exposed the limits of that pass: some operator workflows still need deeper completion, and milestone closure without a dedicated audit left accepted gaps behind.

The new milestone therefore shifts from "rebuild the authoring surfaces" to "turn the authored model into a real platform". `v2.1` owns two platform-heavy tracks that were already deferred in principle: the indoor interaction-rules runtime and the AI capability center. It also absorbs the unfinished `v2.0` closeout work so the project does not carry ambiguous partial-delivery state forward.

Recent operator feedback also showed that indoor rule authoring itself has split into two distinct jobs: the underlying rule model/persistence, and a much heavier workbench/governance experience needed to make those rules operable at scale. That discovery is now tracked as its own `v2.1` phase instead of being hidden inside the base authoring phase.

## Constraints

- **Tech stack**: Preserve the current brownfield stack: Taro/React mini-program, Spring Boot + MyBatis-Plus public/admin backends, existing admin UI, and current local Docker-based MySQL/Mongo setup.
- **Backend ownership**: `packages/server` remains the primary public API backend for the mini-program and must stay aligned with whatever `/admin` authors in `v2.0`.
- **Admin ownership**: `/admin` must remain the authoritative write/control surface for mini-program-facing content, settings, media, and operator workflows.
- **Database**: MySQL remains the primary operational data store for live content/runtime/admin integration because the user wants seeded content, multilingual fields, and unified management behavior around the existing local MySQL setup.
- **Media**: File/image/audio/video uploads must continue to go through backend APIs into Tencent COS, with automatic key generation, permission-aware processing, and metadata persistence.
- **Security**: Secrets must be consumed from local environment or runtime configuration, never hardcoded into tracked files, docs, or admin-facing responses.
- **Verification**: Interfaces must be runnable and actually tested locally against real services before the work is considered complete.
- **Quality attributes**: High availability, ease of use, extensibility, performance, and operator usability are first-class constraints, not afterthoughts.

## Current State

- `v1.0 Live Backend Cutover` shipped on 2026-04-13.
- `v2.0 後台管理系統的改進與完善` was archived on 2026-04-15 after 6 executed phases (`7-12`) and 15 completed plans.
- `v2.0` was archived with accepted gaps: no dedicated milestone audit artifact, no standalone Phase 13 execution, and unresolved collection/reward plus operator-control-plane depth issues now carried into `v2.1`.
- The admin backend on `8081` and public backend on `8080` remain the live local verification targets; COS-backed media and indoor authoring flows remain the integration base for the next milestone.
- Phase execution history for the archived milestone now lives under `.planning/milestones/v2.0-phases/`.
- Phase 18 completed on 2026-04-17 with backend tests, admin build, local smoke, and browser-driven admin verification for the AI capability center.

## Current Milestone: v2.1 互動規則與 AI 能力平台

**Goal:** Close the accepted `v2.0` control-plane gaps while turning indoor interaction rules and AI provider governance into real admin-managed platforms backed by the live public/runtime stack.

**Target features:**
- Finish the accepted `v2.0` carryover work around collections/rewards, user progress, operations/testing alignment, system settings, and milestone-grade verification.
- Add indoor appearance / trigger / effect rule authoring so operators can configure richer interactive behaviors instead of only static markers and overlays.
- Rebuild the densest rule-editing workflow into a dedicated indoor rule workbench plus a global governance center for cross-entity visibility, conflict checks, and lifecycle control.
- Expose safe public/runtime evaluation of authored indoor rules in the public backend and mini-program runtime.
- Build the AI capability center with provider configuration, fallback, quota/governance controls, usage visibility, and secret-safe storage patterns.

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Use `packages/server` as the mini-program's public API surface | The user explicitly called out `/package/server` as the main backend task and the mini-program already expects a separate public API layer | Phase 1 anchored the contract, schema, local profile, and smoke verification in `packages/server`. |
| Treat `/admin` as the control plane for mini-program content and settings | The user wants all mini-program content/settings to be highly customizable in the admin system | Phase 1 contract and schema artifacts treat admin-managed content/settings as the authoritative upstream source. |
| Make MySQL the source of truth for current live content/runtime data | The user wants mock data seeded into the database and local MySQL is already wired into the brownfield environment | Phase 1 created the canonical MySQL foundation tables and bootstrap scaffold and verified them against real local MySQL. |
| Keep COS secrets out of tracked docs and source | The request included credentials, but secure handling requires using environment/runtime configuration instead of committing them | Maintained through Phases 5 and 6; COS verification used runtime environment variables only. |
| Plan the work as multi-phase backend cutover, not a single giant implementation dump | The scope spans schema design, admin/public contract alignment, uploads, migration, and verification; decomposing it reduces execution risk | Confirmed by the completed six-phase execution path. |
| Align local brownfield tables forward to the canonical schema instead of teaching new admin code to speak legacy fields | Real Phase 2 smoke exposed reward, POI, city, and chapter schema drift in the local MySQL dataset | Phase 2 added local SQL alignment scripts and legacy-to-canonical backfill so admin APIs now exercise the same schema family that later public phases will read. |
| Let Mongo bootstrap fail open during local admin startup | Phase 2 admin HTTP verification depends on MySQL-backed content APIs, while local Mongo auth can drift independently | `MongoConfig` now logs and continues so optional document bootstrap does not block the admin control plane. |
| Keep COS uploads in the admin backend and reuse `content_assets.canonical_url` as the shared asset contract | The public backend already resolved canonical asset URLs, so the missing gap was upload orchestration rather than a second public asset model | Phase 5 added COS upload/delete handling in the admin backend and verified the public backend consumed the uploaded canonical URL without contract changes. |
| Treat MySQL tables plus admin-managed runtime settings as the canonical replacement for former client mock content | The user explicitly rejected a mock runtime and needed admin-authored content/settings to drive the live app | Phase 6 migrated the former mock dataset into canonical storage and re-verified the public catalog/health endpoints against it. |
| Make operator views read the same traveler data the mini-program writes | Live cutover is incomplete if `/admin` still inspects a legacy user table detached from public writes | Phase 6 cut dashboard/user-management/test-console traveler reads over to `user_profiles`, `user_progress`, and `user_checkins`. |
| Split the post-cutover expansion into `v2.0` and `v2.1` | The user's requested redesign spans control-plane reconstruction plus two separate platform efforts (interaction rules and AI provider orchestration), which would make a single milestone roadmap dishonest and unbuildable | `v2.0` now targets the admin/control-plane rebuild and runtime alignment first; `v2.1` will absorb the heaviest platformization work. |
| Treat Traditional Chinese-first admin UX, four-language authoring, and real WeChat-authenticated flows as milestone-level scope | These are not polish items; they change the shape of the admin data model, user gating logic, and operator workflows across the whole product | `v2.0` requirements and roadmap must map these as first-class workstreams, not optional follow-up fixes. |
| Close `v2.0` with explicit carryover instead of pretending full completion | By archive time, the control-plane rebuild had real delivered value, but requirement-level closure and the planned Phase 13 work were not honestly finished | `v2.0` was archived with accepted gaps, and `v2.1` now starts with an explicit carryover phase instead of silently erasing those gaps. |
| Split indoor rule platform work into base authoring and workbench/governance phases | Operator testing showed the interaction-rule workbench, rule naming, conflict visibility, and cross-entity governance are substantial enough to deserve explicit sequencing instead of being buried inside one broad authoring phase | `v2.1` now inserts a dedicated phase between rule authoring and runtime evaluation, and shifts later phases back accordingly. |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? -> Move to Out of Scope with reason
2. Requirements validated? -> Move to Validated with phase reference
3. New requirements emerged? -> Add to Active
4. Decisions to log? -> Add to Key Decisions
5. "What This Is" still accurate? -> Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check -> still the right priority?
3. Audit Out of Scope -> reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-04-17 after Phase 18 completion*
