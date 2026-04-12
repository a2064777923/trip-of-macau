# Trip of Macau

## What This Is

Trip of Macau is a brownfield travel mini-program plus admin platform for story-driven city exploration, map discovery, check-ins, collectibles, tips, and operations tooling. The immediate project is to replace the mini-program's mock-driven runtime with a real public backend in `packages/server`, while making the `/admin` control plane the authoritative place to configure content, runtime settings, and media used by the mini-program.

## Core Value

Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## Requirements

### Validated

- `mini-program shell exists` - `packages/client` already contains the traveler-facing page structure, map UX, tips, profile/settings, and local gameplay state flows.
- `admin platform baseline exists` - `packages/admin/aoxiaoyou-admin-ui` and `packages/admin/aoxiaoyou-admin-backend` already provide map/content/system management surfaces and supporting Java services.
- `local brownfield environment exists` - `docker-compose.local.yml`, MySQL/Mongo init scripts, and local startup helpers already support local service bootstrapping.

### Active

- [ ] Replace mock-first mini-program runtime with real public APIs implemented in `packages/server`.
- [ ] Make mini-program-facing content, settings, and operational rules highly configurable through `/admin`.
- [ ] Expand the MySQL schema so it can store mini-program content, runtime settings, user progress, and media references cleanly.
- [ ] Seed existing mock content into MySQL through repeatable scripts, not one-off manual inserts.
- [ ] Add any missing public/admin interfaces needed to make mini-program and admin logic fully connected.
- [ ] Implement backend-mediated Tencent COS uploads with automatic object naming/path management and stored asset metadata.
- [ ] Prove the full chain locally: admin writes -> MySQL/COS -> public APIs -> mini-program consumption.

### Out of Scope

- Keeping mock data as the primary runtime source after the live backend cutover - mocks may remain as temporary fallback/test fixtures only.
- Committing cloud secrets, COS credentials, or local-only integration files into the repository - secrets must stay in environment/configuration outside tracked docs and source control.
- Adding unrelated product domains before the live backend cutover is complete - new feature scope that does not directly support backend integration, admin configurability, data migration, media handling, or verification is deferred.

## Context

This project starts from an existing codebase, not a blank slate. The mini-program currently centralizes most business behavior in `packages/client/src/services/gameService.ts` and `packages/client/src/services/gameMock.ts`, which means many traveler-facing experiences already exist in UI form but do not yet rely on a real server. The admin side already has meaningful Java and React foundations, but its domain coverage is still incomplete relative to everything the mini-program needs. The public backend in `packages/server` is present but too thin and currently mismatched with parts of the admin/public model.

The codebase map in `.planning/codebase/` found several issues that directly affect this effort: the public backend has only limited domain coverage, the admin security layer is interceptor-driven, and the public backend currently references a missing interceptor class. The local environment uses MySQL and MongoDB, but the user's request makes MySQL the critical source of truth for live mini-program/admin integration. MongoDB remains optional for document-style or future admin extensions, not the primary source for current mini-program runtime content.

The user wants this work to be production-minded: highly configurable, extensible, high-performance, and actually runnable. That raises the bar beyond "expose some endpoints" to include schema quality, publish-state semantics, asset management, migration strategy, smoke checks, and end-to-end verification.

## Constraints

- **Tech stack**: Preserve the current brownfield stack: Taro/React mini-program, Spring Boot + MyBatis-Plus public/admin backends, existing admin UI, and current local Docker-based MySQL/Mongo setup.
- **Backend ownership**: `packages/server` is the primary public API backend for the mini-program and is the main implementation target for this work.
- **Admin ownership**: `/admin` must remain the authoritative write/control surface for mini-program-facing content, settings, and assets.
- **Database**: MySQL is the primary operational data store for this cutover because the user explicitly wants seeded mock data and fully connected admin/public behavior around the local MySQL setup.
- **Media**: File/image uploads must go through backend APIs into Tencent COS, with automatic key generation and metadata persistence.
- **Security**: Secrets must be consumed from local environment or runtime configuration, never hardcoded into tracked files or planning docs.
- **Verification**: Interfaces must be runnable and actually tested locally against real services before the work is considered complete.
- **Quality attributes**: High availability, ease of use, extensibility, and performance are first-class constraints, not afterthoughts.

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Use `packages/server` as the mini-program's public API surface | The user explicitly called out `/package/server` as the main backend task and the mini-program already expects a separate public API layer | - Pending |
| Treat `/admin` as the control plane for mini-program content and settings | The user wants all mini-program content/settings to be highly customizable in the admin system | - Pending |
| Make MySQL the source of truth for current live content/runtime data | The user wants mock data seeded into the database and local MySQL is already wired into the brownfield environment | - Pending |
| Keep COS secrets out of tracked docs and source | The request included credentials, but secure handling requires using environment/runtime configuration instead of committing them | - Pending |
| Plan the work as multi-phase backend cutover, not a single giant implementation dump | The scope spans schema design, admin/public contract alignment, uploads, migration, and verification; decomposing it reduces execution risk | - Pending |

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
*Last updated: 2026-04-12 after initialization from brownfield codebase map and backend integration scope*
