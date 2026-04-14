---
phase: 06-migration-cutover-and-hardening
created: 2026-04-12
status: active
---

# Phase 6 Context

## Goal

Seed the remaining mini-program mock dataset into MySQL, finish the live mini-program cutover, and add the operational visibility needed to keep the admin/public/client stack reliable after launch.

## Brownfield Reality

- Phase 1 through Phase 5 already established the canonical public/admin schema, live CRUD flows, public traveler APIs, and Tencent COS-backed media upload pipeline.
- The mini-program now talks to live public APIs by default, but `packages/client/src/services/gameService.ts` still imports `gameMock.ts` for guest defaults, recommendation fallback, arrival fallback, and legacy check-in fallback behavior.
- The current MySQL seed state is only partial: `scripts/local/mysql/init/03-live-backend-seed-scaffold.sql` populates a subset of cities, chapters, tips, stamps, rewards, notifications, and runtime settings, but it does not migrate the full content model represented by `packages/client/src/services/gameMock.ts`.
- The canonical admin/public story model already lives in `storylines`, `story_chapters`, `pois`, `cities`, `stamps`, `rewards`, `tip_articles`, `notifications`, and `app_runtime_settings`, but the brownfield database still contains earlier sample data and mixed legacy tables such as `story_lines` alongside the canonical `storylines`.
- The admin dashboard exists, but its system status is still optimistic rather than probe-based; it does not surface seed health, public-backend reachability, content completeness, or COS readiness.
- Local runtime verification from earlier phases proved the public backend on `8080`, admin backend on `8081`, local MySQL on `3306`, and real COS upload path can work together, but Phase 6 still needs a final end-to-end smoke that proves seeded content and live mini-program runtime behavior are coherent.

## Execution Decision

Phase 6 will execute as three linked tracks:

1. Replace the scaffold seed with a deterministic mock-data migration that upserts the real mini-program content into the canonical schema, including resolving mock-data inconsistencies where a live canonical mapping is required.
2. Add admin-side integration health and backend health probes so operators can see whether seed data, database connectivity, public API reachability, and COS configuration are actually healthy.
3. Remove the mini-program's remaining runtime dependency on `gameMock.ts`, prefer live/public or admin-authored runtime data everywhere, and run a repeatable smoke pass across admin, public, MySQL, and the mini-program build.

## Acceptance Focus

- Canonical MySQL tables contain the full live mini-program content required by the current app flows, seeded repeatably from the former mock dataset.
- `/admin` can inspect seeded content and real integration health without relying on hardcoded dashboard booleans.
- The mini-program no longer needs mock content fallbacks during normal live runtime flows.
- Repeatable verification proves admin visibility, public reads/writes, asset delivery semantics, and mini-program build/runtime integration all work together.
