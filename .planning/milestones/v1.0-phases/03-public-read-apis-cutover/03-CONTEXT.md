---
phase: 03-public-read-apis-cutover
created: 2026-04-12
status: active
---

# Phase 3 Context

## Goal

Replace mock-backed mini-program read flows with real `packages/server` endpoints for content, maps, discovery, and published runtime configuration.

## Brownfield Reality

- Phase 2 made `/admin` the canonical write surface, but the public backend still serves only a partial legacy read model.
- `packages/server` still contains legacy `Poi` and `StoryLine` mappings that do not match the canonical MySQL schema now used by admin and local migration scripts.
- The mini-program still reads most traveler-facing content synchronously from `gameMock.ts` through `gameService.ts`.
- The local database has real content for cities, POIs, storylines, rewards, and runtime settings, but `story_chapters`, `tip_articles`, `stamps`, and `notifications` still need seed coverage for a convincing cutover.

## Execution Decision

Phase 3 will execute as one cutover pass across backend, seed data, and mini-program integration:

1. Canonicalize `packages/server` public read entities/services/controllers against the Phase 1/2 schema.
2. Expose real public endpoints for runtime groups, cities, POIs, storylines, tips, rewards, stamps, notifications, and discover cards.
3. Seed the currently empty read domains from the existing mini-program mock dataset where needed so live reads are not blank.
4. Rewire `packages/client` read paths to fetch live public data while leaving later-phase write/gameplay behavior local for now.
5. Verify the public backend and the mini-program read layer against the real local MySQL-backed service.

## Acceptance Focus

- The mini-program no longer relies on `gameMock.ts` for the public read domains covered by Phase 3.
- Public responses honor `published` status, `sort_order`, locale fallback, and publish-window rules.
- Local verification proves the real public backend serves data that the mini-program can consume without mock-only assumptions.
