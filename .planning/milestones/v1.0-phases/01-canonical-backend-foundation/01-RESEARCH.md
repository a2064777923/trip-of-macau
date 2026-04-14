# Phase 1 Research: Canonical Backend Foundation

## Objective

Research how to implement Phase 1 so later live-backend phases are built on a stable contract instead of extending today's mock-first behavior ad hoc.

## Current Starting Point

- The mini-program already exposes the target user experience, but its main data orchestration is local-first in `packages/client/src/services/gameService.ts`.
- The admin platform already has meaningful domain coverage, but it does not yet cover every mini-program-facing entity and runtime setting.
- The public backend exists, but only covers a narrow subset of the required domains and currently contains a broken config dependency in `WebConfig.java`.
- Local runtime already has MySQL and Mongo bootstrap support, but no equivalent public-backend startup/smoke helpers to match the admin side.

## What Phase 1 Must Resolve

1. Define canonical publish, locale, sort, and asset semantics across public and admin services.
2. Create a MySQL schema foundation that can support the mini-program domains visible in the current frontend.
3. Remove obvious public-backend boot/config blockers before adding broader API surfaces.
4. Create a reproducible local foundation so later phases can verify real integration incrementally.

## Recommended Architecture

### 1. Control Plane vs Public Plane

- Keep `/admin` as the write/control plane and `packages/server` as the read/write public plane for traveler-facing clients.
- Do not solve missing public behavior by having the mini-program call admin endpoints directly.
- Do not solve missing admin behavior by hardcoding mini-program runtime values.

### 2. Canonical Data Model Strategy

- MySQL should own the operational content and user-state model for the live mini-program.
- Introduce explicit tables for:
  - runtime settings
  - assets
  - cities
  - POIs
  - storylines
  - chapters
  - tips/articles
  - rewards
  - stamps
  - notifications
  - user profile/progress/preferences/check-ins/redemptions
- Use publish-state and sort-order fields consistently across content tables.
- Preserve multilingual support with explicit `name_zh`, `name_en`, `name_zht` style columns or equivalent localization structures aligned with the current brownfield model.

### 3. Public Backend Foundation Strategy

- Fix `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/config/WebConfig.java` first so the public backend stops depending on a missing admin-only interceptor.
- Add canonical enums/value types early:
  - content status
  - locale code
  - asset kind
- Expand the public backend with entity/mapper/service scaffolding before full controller expansion.
- Keep Phase 1 focused on domain/persistence readiness, not complete endpoint cutover.

### 4. Seed and Migration Strategy

- Treat `packages/client/src/services/gameMock.ts` as the source dataset to be transformed, not copied manually.
- Phase 1 should create the schema and deterministic bootstrap scaffolding that Phase 6 will use for actual mock-data import.
- Seed scripts should be idempotent where possible and record what was loaded.

### 5. Media Strategy

- The canonical schema must include asset metadata now even if upload implementation lands later.
- Asset metadata should minimally persist:
  - bucket
  - region
  - object key
  - canonical URL
  - kind
  - mime type
  - dimensions if applicable
  - checksum/etag if available

### 6. Operational Strategy

- Keep health verification anchored to real endpoints already present:
  - public: `/api/v1/health`, `/actuator/health`
  - admin: `/api/v1/health`
- Add a public-backend local profile and startup helper to match the admin-side ergonomics.
- Use scripted smoke checks so later phases can prove regressions quickly.

## Key Risks and Mitigations

### Contract Drift

Risk:
- Public and admin services drift on status/locale/asset semantics.

Mitigation:
- Create a contract matrix plus matching enums/value objects in both services during Phase 1.

### Schema Too Narrow or Too Clever

Risk:
- The schema only matches current endpoints or becomes over-normalized before real cutover needs are proven.

Mitigation:
- Build around current mini-program surfaces and admin control requirements, with straightforward tables and explicit status/order/asset columns.

### Broken Local Verification

Risk:
- Later phases can modify code but cannot prove end-to-end behavior locally.

Mitigation:
- Add local profile wiring and smoke scripts in Phase 1, not at the end of the project.

### Secret Leakage

Risk:
- COS credentials or other cloud secrets get copied into tracked code or docs during the backend buildout.

Mitigation:
- Keep planning docs secret-free, require env/runtime injection, and keep upload implementation backend-mediated.

## Recommendation Summary

- Build Phase 1 as foundation, not feature cutover.
- Prioritize canonical contract definitions, schema coverage, public-backend config cleanup, and local runtime verification.
- Delay full mock-data migration and full endpoint cutover until the foundation is stable.

## Validation Architecture

- Use JUnit 5 + Spring Boot Test in `packages/server` as the Phase 1 automated verification baseline.
- Add targeted smoke tests for:
  - public backend Spring context loading after `WebConfig` cleanup
  - canonical enum/value-object availability
  - presence of schema bootstrap artifacts
- Add a PowerShell smoke script to exercise local MySQL/public/admin health endpoints once services are up.
- Keep feedback loops short:
  - quick: targeted `packages/server` tests
  - full: `packages/server` test suite + local health/smoke script

## Phase 1 Output Implications

Phase 1 plans should therefore include:
- a contract/gap matrix artifact
- canonical enums/value objects
- schema/bootstrap SQL
- public-backend domain/persistence scaffolding
- local profile/startup/smoke support

They should not try to finish:
- full admin CRUD
- full public controller coverage
- full data migration
- full COS upload implementation
