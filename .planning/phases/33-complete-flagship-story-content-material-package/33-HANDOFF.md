# Phase 33 Handoff

## Package

- Package code: `east_west_war_and_coexistence_package`
- Storyline code: `east_west_war_and_coexistence`
- Admin route: `/admin/#/content/material-packages`
- Primary inspection page: `故事素材包`

## Seed Files

- `scripts/local/mysql/init/47-phase-33-story-material-package-model.sql`
- `scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql`
- `scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql`

## Content Artifacts

- `docs/content-packages/east-west-war-and-coexistence/content-manifest.json`
- `docs/content-packages/east-west-war-and-coexistence/historical-checklist.md`
- `docs/content-packages/east-west-war-and-coexistence/story-script.md`

## Admin Entry Points

- `/content/material-packages` — package-level provenance, counters, item table, historical basis, literary dramatization, and quick links.
- `/content/media` — deep media asset management.
- `/content/blocks` — reusable story content blocks.
- `/content/storyline-mode` — route and chapter override workbench.
- `/content/experience` — experience flows, templates, bindings, overrides, governance, and exploration elements.
- `/collection/game-rewards` — story rewards, titles, fragments, and reward rules.

## Phase 34 Dependencies

- Public runtime DTOs must consume the seeded package through compiled storyline runtime, not raw admin payloads.
- Mini-program story pages must render content blocks, media fallbacks, Lottie JSON, audio/video metadata, and unsupported gameplay placeholders without blank screens.
- Event reporting should connect story interactions, pickup completion, media completion, and session exit to the Phase 32 progress/event model.

## Intentionally Not Mini-Program Verified

- WeChat DevTools visual journey verification is deferred to Phase 34 or later.
- Complex gameplay implementations such as AR photo recognition, voice input, route coverage, quiz UI, puzzle UI, and fortress-defense gameplay are represented as templates and runtime data only.
- The smoke script verifies admin API and MySQL coherence, not mini-program rendering.
