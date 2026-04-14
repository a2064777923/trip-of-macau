# Trip of Macau

## What This Is

Trip of Macau is a brownfield travel mini-program plus admin platform for story-driven city exploration, map discovery, check-ins, collectibles, tips, and operations tooling. The live backend cutover is complete, and the current project focus is now to evolve `/admin` into the real control plane for multilingual content, richer spatial/story composition, and authenticated traveler operations that drive the mini-program experience end-to-end.

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

### Active

- [ ] `admin control plane is Traditional Chinese-first and structurally coherent` - `/admin` uses Traditional Chinese labels, mini-program branding assets, and a cleaner information architecture instead of the current mixed-language/operator-hostile surfaces.
- [ ] `mini-program functionality requires real WeChat-authenticated sessions` - traveler-facing features stop relying on guest/mock behavior and enforce real login alignment across frontend, `packages/server`, and admin inspection flows.
- [ ] `canonical four-language content authoring exists` - live UI copy and content fields support `zh-Hant`, `zh-Hans`, `en`, and `pt`, with admin-side multilingual editing plus configurable translation-engine-assisted autofill.
- [ ] `map, POI, indoor, story, chapter, task, reward, and media models are rebuilt for richer orchestration` - the admin must be able to author the expanded spatial and narrative runtime the user described, not just edit the old cutover-era fields.
- [ ] `operator visibility and runtime controls are materially stronger` - media search, user progress/exploration tracking, operations views, and system settings must become practical for real ongoing management.

### Out of Scope

- Keeping mock data as the primary runtime source after the live backend cutover - mocks may remain as temporary fallback/test fixtures only.
- Committing cloud secrets, COS credentials, or local-only integration files into the repository - secrets must stay in environment/configuration outside tracked docs and source control.
- Adding unrelated product domains before the live backend cutover is complete - new feature scope that does not directly support backend integration, admin configurability, data migration, media handling, or verification is deferred.
- Shipping the full indoor overlay trigger/effect rules engine in `v2.0` - advanced appearance conditions, chained triggers, animation paths, and event scripting are deferred to `v2.1` because they require a dedicated interaction-rules platform beyond this milestone's data-model/control-plane rebuild.
- Shipping full AI provider platformization in `v2.0` - per-capability provider switching, quota orchestration, suspicious-concurrency throttling, and usage governance are deferred to `v2.1` so `v2.0` can focus on the admin/control-plane reconstruction first.

## Context

This project starts from an existing codebase, not a blank slate. The mini-program still centralizes much orchestration in `packages/client/src/services/gameService.ts`, but the live runtime now reads and writes through real public APIs backed by MySQL instead of the former `gameMock.ts` catalog fallback. The admin side had meaningful Java and React foundations, and the first milestone extended them into the authoritative control plane for mini-program content, runtime settings, and operator visibility. The public backend in `packages/server` is now the live contract surface for the traveler experience.

The codebase map in `.planning/codebase/` found several issues that directly affected this effort: the public backend had only partial domain coverage, the admin security layer is interceptor-driven, and the public/backend contract and configuration layer had brownfield mismatches that blocked direct cutover. The local environment uses MySQL and MongoDB, but the user's request made MySQL the critical source of truth for live mini-program/admin integration. MongoDB remains optional for document-style or future admin extensions, not the primary source for current mini-program runtime content. Phases 1 through 6 resolved the missing public-backend interceptor dependency, completed the canonical schema/contract scaffold, finished the admin control plane, cut public read/write flows over to live APIs, added the COS media pipeline, migrated the former mock dataset into canonical storage, and closed the final end-to-end verification loop.

The user wants this work to be production-minded: highly configurable, extensible, high-performance, and actually runnable. That raises the bar beyond "expose some endpoints" to include schema quality, publish-state semantics, asset management, migration strategy, smoke checks, and end-to-end verification.

The next milestone is driven by a large management-system redesign request. The center of gravity moves from "cut over mocks to live services" to "make the admin a genuinely usable control plane" for Traditional Chinese-first operators who must manage multilingual content, richer map/spatial structures, story/chapter orchestration, media policies, and live traveler progress. Although the emphasis is admin-side, every control-plane change must still stay aligned with mini-program frontend behavior and the public backend contract.

The requested expansion is too large for a single honest roadmap, so the work is intentionally split. `v2.0` covers the admin/control-plane reconstruction, core data-model expansion, real-login alignment, and the first practical pass of user progress and operations visibility. A later `v2.1` is reserved for the heaviest platformization work such as the full indoor interaction rules engine and full AI provider orchestration.

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
- All six v1 phases are complete: canonical schema/contract foundation, admin control plane completion, public read/write cutover, COS media handling, mock-data migration, and full-stack hardening are implemented and verified locally.
- The admin backend on `8081` now reports real database/public API/COS/seed status and supports authenticated COS uploads and delete cleanup; the public backend on `8080` serves the seeded live catalog/runtime and traveler write flows.
- Phase 6 established a repeatable seed/import path for the former mock dataset and a repeatable smoke harness that proves the full chain locally: admin writes -> MySQL/COS -> public reads/writes -> mini-program runtime assumptions.
- The project is now entering `v2.0`, where the main risk is not basic connectivity but whether the admin/control-plane model is expressive enough for the richer product the user wants to run.

## Current Milestone: v2.0 後台管理系統的改進與完善

**Goal:** Turn `/admin` into a Traditional Chinese-first control plane that can author the live mini-program's multilingual content, spatial structures, media, and story/task orchestration end-to-end while aligning the mini-program and public backend with real authenticated behavior.

**Target features:**
- Admin UI language and branding cleanup, including Traditional Chinese labels, reuse of the mini-program icon, and corrected navigation/grouping in the management console.
- Real WeChat login enforcement across the mini-program frontend, public backend, and admin-facing traveler visibility, with remaining guest/mock access paths removed from functional flows.
- Four-language content modeling and editing for `zh-Hant`, `zh-Hans`, `en`, and `pt`, plus configurable translation engines, primary-language settings, and engine fallback behavior.
- AMap-compatible coordinate handling and map-space rebuilding across cities, sub-maps, POIs, media attachments, and spatial metadata authoring.
- Rebuilt authoring flows for storylines, chapters, tasks/activities, collectibles, badges/rewards, indoor building basics, media resources, and system settings so operators can compose richer narrative experiences.
- Stronger operator tooling for media upload policy, media search, user progress and interaction logs, test/operations surfaces, and clearer system configuration management.

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
*Last updated: 2026-04-13 after starting the v2.0 milestone*
