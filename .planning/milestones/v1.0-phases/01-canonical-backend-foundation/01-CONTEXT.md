# Phase 1: Canonical Backend Foundation - Context

**Gathered:** 2026-04-12
**Status:** Ready for planning
**Source:** Brownfield scope synthesis from user request plus `.planning/codebase/`

<domain>
## Phase Boundary

Phase 1 establishes the backend foundation required to stop treating the mini-program mocks as the system of record. This phase does not yet complete every public endpoint or admin CRUD flow; instead it creates the canonical shared contract, the core MySQL schema, the `packages/server` persistence/configuration baseline, and the local runtime/smoke infrastructure that later phases rely on.

This phase specifically owns:
- canonical publish/locale/asset semantics across public and admin services
- MySQL schema foundation for mini-program-facing domains
- public-backend configuration cleanup and persistence scaffolding
- local start/smoke workflows for the shared foundation

This phase does not yet own:
- full admin CRUD/UI completion for every domain
- full mini-program read/write API cutover
- complete mock-data migration
- full COS upload implementation

</domain>

<decisions>
## Implementation Decisions

### Public Backend Ownership
- `packages/server` remains the sole public API surface for the mini-program. Do not proxy mini-program traffic through the admin backend.
- Mini-program pages must eventually stop using `packages/client/src/services/gameMock.ts` and `packages/client/src/services/gameService.ts` as their primary runtime data source.
- Public APIs must respect admin-managed publish state, sort order, locale, and asset-reference semantics from the first foundation phase onward.

### Admin as Control Plane
- `/admin` is the write/control plane for mini-program-facing content and runtime configuration.
- If the mini-program needs a setting, content type, or asset reference that the current admin system does not expose, the admin backend/UI must be extended in later phases instead of hardcoding around the gap in the mini-program.
- The canonical contract must be defined from the perspective of admin-managed data flowing outward to public APIs.

### Data and Migration
- MySQL is the source of truth for current live mini-program and admin integration work.
- Existing mock datasets must be converted into deterministic seed or migration scripts; however the full data migration itself is deferred to a later phase after the schema and contracts stabilize.
- MongoDB can remain for document-style admin extensions, but Phase 1 must not split the core live mini-program domains across MySQL and MongoDB.

### Media and Assets
- Uploads go through backend APIs into Tencent COS. Clients never choose raw bucket object keys.
- COS object keys must be generated automatically using deterministic prefixes and collision-safe filenames.
- Credentials must stay in environment or local runtime configuration only; do not put secret values into tracked code, docs, or planning artifacts.

### Quality and Verification
- Every interface added in this effort must be runnable locally against real services.
- The phase must include concrete smoke commands and profile/configuration steps, not just abstract setup notes.
- High availability and performance in this project means health endpoints, validation, pagination/filtering discipline, sensible indexes, idempotent bootstrap behavior, and removal of obvious configuration footguns before later feature expansion.

### Known Brownfield Issues To Resolve Early
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/config/WebConfig.java` currently references a missing `AdminAuthInterceptor` class and needs public-backend-specific correction.
- The public backend lacks the broader domain model required to serve the current mini-program surfaces.
- Current local startup helpers cover admin services but not the public backend with equivalent convenience and smoke coverage.

### the agent's Discretion
- Exact table decomposition, provided it still cleanly supports the required mini-program/admin domains.
- Whether to use MyBatis enum handlers, Jackson annotations, or value-object wrappers to enforce canonical code/value mappings.
- Whether schema bootstrap remains in `scripts/local/mysql/init` or also gains resource-level migration files, as long as the result is repeatable and locally runnable.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Product and Scope
- `.planning/PROJECT.md` - authoritative project scope, constraints, and core value
- `.planning/REQUIREMENTS.md` - requirement IDs and phase traceability
- `.planning/ROADMAP.md` - phase boundaries and success criteria
- `.planning/STATE.md` - current project status and known blockers

### Brownfield Codebase Map
- `.planning/codebase/STACK.md` - current runtime stack and tooling
- `.planning/codebase/ARCHITECTURE.md` - current system boundaries and known architectural oddities
- `.planning/codebase/INTEGRATIONS.md` - current database/service integrations and secrets-handling concerns
- `.planning/codebase/CONCERNS.md` - current backend/security/quality risks

### Existing Specifications and Docs
- `README.md` - current local run expectations and deployment notes
- `docs/数据库设计-v2.0.md` - latest database design reference already present in the repo
- `docs/aoxiaoyou-admin-technical-design-v2.0.md` - admin technical design baseline
- `docs/aoxiaoyou-admin-api-interface-v2.0.md` - admin API/interface intent

### Current Runtime Sources of Truth
- `packages/client/src/services/gameMock.ts` - current mock content dataset that later phases must migrate
- `packages/client/src/services/gameService.ts` - current mock-backed behavior orchestration
- `packages/client/src/types/game.ts` - current mini-program domain shapes
- `packages/server/src/main/resources/application.yml` - public backend base configuration
- `packages/admin/aoxiaoyou-admin-backend/src/main/resources/application.yml` - admin backend base configuration
- `docker-compose.local.yml` - local data-store/runtime orchestration

</canonical_refs>

<specifics>
## Specific Ideas

- Cover the mini-program-facing domains already visible in the current frontend: runtime config, cities, POIs, storylines/chapters, tips, rewards, stamps, notifications, user progress, user preferences, emergency contact, and asset references.
- Establish canonical status and locale semantics up front instead of letting public/admin services drift independently.
- Build the public-backend foundation so later phases can cut pages over incrementally instead of performing one risky big-bang rewrite.
- Keep COS integration requirements visible in the contract and schema, but avoid full secret-bearing implementation in this phase.
- Make smoke verification use real endpoints:
  - public health: `/api/v1/health` and `/actuator/health`
  - admin health: `/api/v1/health`

</specifics>

<deferred>
## Deferred Ideas

- Full mock-data import into live tables is deferred to Phase 6 after schema stabilization.
- Full Tencent COS upload flow is deferred to Phase 5, though schema and asset semantics should anticipate it.
- Full mini-program read/write cutover is deferred to Phases 3 and 4.
- Advanced AI runtime execution, analytics, and higher-order ops features remain outside Phase 1.

</deferred>

---

*Phase: 01-canonical-backend-foundation*
*Context gathered: 2026-04-12 via brownfield scope synthesis*
