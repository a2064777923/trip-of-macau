# Phase 34 Research: Public Runtime and Mini-program Consumption Baseline

**Researched:** 2026-04-30
**Status:** Ready for planning
**Scope:** Public compiled runtime DTOs, mini-program story consumption baseline, authenticated sessions/events, repeatable local verification.

## Executive Summary

Phase 34 should not invent a new story model. The codebase already has the core runtime pieces:

- `ExperienceController` exposes public endpoints for POI runtime, storyline runtime, events, storyline sessions, session events, session exit, and user exploration.
- `PublicExperienceServiceImpl` already merges inherited flows, chapter flows, and published overrides into compiled steps.
- `ExperienceRuntimeResponse` already contains `StorylineRuntime`, `StoryChapterRuntime`, `Flow`, `Step`, `StoryModeConfig`, `Template`, and `OverrideRule`.
- The mini-program already has `StoryContentBlockRenderer` and `LottieAssetPlayer`.
- The mini-program still primarily refreshes story data from `/api/v1/story-lines`, then maps it into local `StorylineItem` state. It does not yet consume `/api/v1/storylines/{id}/runtime` as the story page source of truth.

The best plan is therefore a bridge-and-hardening pass:

1. Harden and enrich public runtime DTOs enough for safe traveler consumption.
2. Add client API/types/mapping for storyline runtime, story sessions, events, and exploration.
3. Upgrade the story page to load runtime for the selected story, render compiled steps and unsupported gameplay cards, and report basic events when authenticated.
4. Add a Phase 34 smoke script and verification docs that prove the seeded flagship story can be read through public runtime and exercised with session/event calls.

## Current Backend Findings

### Public Runtime Endpoints Already Exist

`packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` exposes:

- `GET /api/v1/experience/poi/{poiId}`
- `GET /api/v1/storylines/{storylineId}/runtime`
- `POST /api/v1/experience/events`
- `POST /api/v1/storylines/{storylineId}/sessions/start`
- `POST /api/v1/storylines/{storylineId}/sessions/{sessionId}/events`
- `POST /api/v1/storylines/{storylineId}/sessions/{sessionId}/exit`
- `GET /api/v1/users/me/exploration`

Read-only runtime endpoints do not require bearer auth. Stateful endpoints call `requireUserId(...)` and require bearer JWT.

### Runtime Merge Logic Exists

`PublicExperienceServiceImpl#getStorylineRuntime(...)`:

- loads public story detail through `StoryLineService`
- maps chapters into `StoryChapterRuntime`
- resolves inherited flow by chapter anchor
- resolves chapter flow by `experienceFlowId`
- loads published `ExperienceOverride` rows for `ownerType=story_chapter`
- compiles steps with override modes `append`, `disable`, `inherit`, and `replace`

This means Phase 34 can focus on traveler-safe DTO completeness and frontend consumption, not rebuilding merge semantics.

### DTO Gaps

`ExperienceRuntimeResponse.Step` currently has technical fields such as `stepType`, `triggerType`, configs, media asset, reward ids, exploration weight, template, and sort order.

For mini-program baseline consumption, the DTO should add traveler-safe display metadata:

- `displayCategory`
- `displayCategoryLabel`
- `unsupported`
- `unsupportedReason`
- `travelerActionLabel`
- `eventType`
- `elementCode`
- `elementId`

These can be derived from step/template/effect config where available and defaulted safely when unsupported.

`StorylineRuntime` should also expose summary fields useful to client mapping:

- `runtimeVersion`
- `source`
- `generatedAt`
- `unsupportedStepCount`
- `publishedChapterCount`

### Lifecycle Filtering Risk

Public runtime currently relies on `storyLineService.getDetail(...)` plus published flow/override queries. Planning should verify that:

- draft/non-published storylines or chapters do not surface in public runtime
- unpublished flow steps are excluded
- unpublished overrides are excluded
- content blocks/media assets exposed to public are traveler-safe

If `StoryLineServiceImpl` currently returns non-published nested content, Phase 34 must filter it.

## Current Mini-program Findings

### Existing Story Page

`packages/client/src/pages/story/index.tsx`:

- loads storylines from `gameService.getStorylines()`
- calls `refreshPublicContent()` on mount
- renders storyline tabs, active story overview, chapter list, chapter details, rule chips, and `StoryContentBlockRenderer`
- offers map and stamp navigation actions

It does not:

- call `/storylines/{id}/runtime`
- display compiled runtime steps
- start durable story sessions
- report basic story events
- show unsupported gameplay cards
- use user exploration runtime summary on the story page

### Existing Renderers

`StoryContentBlockRenderer` already handles:

- `quote`
- `image`
- `gallery`
- `audio`
- `video`
- `lottie`
- `attachment_list`

`LottieAssetPlayer` already:

- imports `lottie-miniprogram`
- uses a 2D canvas node
- loads JSON from network URL
- destroys animation on unmount
- falls back to poster/fallback image or friendly text

This is enough for Phase 34; no new animation library is needed.

### API Client Gap

`packages/client/src/services/api.ts` currently has `getPublicStorylines(locale)` for `/story-lines`, but no helpers for:

- `getPublicStorylineRuntime(storylineId, locale)`
- `startPublicStorylineSession(storylineId)`
- `recordPublicExperienceEvent(payload)`
- `recordPublicStorylineSessionEvent(storylineId, sessionId, payload)`
- `exitPublicStorylineSession(storylineId, sessionId)`
- `getPublicUserExploration(params)`

Adding these helpers is the cleanest way to keep story page code from building ad hoc URLs.

## Planning Recommendations

### Plan Split

Use four plans:

1. Backend public runtime contract hardening.
2. Mini-program API/types/runtime mapping.
3. Story page runtime consumption and graceful degradation.
4. Phase 34 smoke, verification docs, and milestone evidence.

This avoids mixing Java DTO/service changes with Taro UI work and keeps the final smoke focused on contract parity.

### Verification Architecture

Minimum automated checks:

- `mvn -q -DskipTests compile -f packages/server/pom.xml`
- `npm run build:weapp --prefix packages/client`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-34-public-runtime.ps1`

Recommended smoke coverage:

- import Phase 28-33 seed SQL files with `--default-character-set=utf8mb4`
- login or dev-bypass a traveler through env-backed credentials/config
- call `GET /api/v1/story-lines` and find `east_west_war_and_coexistence`
- call `GET /api/v1/storylines/{id}/runtime`
- assert runtime has five chapters
- assert compiled steps exist
- assert at least one Lottie/audio/image content asset is present
- assert story-mode config exists
- start a session with bearer token
- record a story/chapter/content event with stable `clientEventId`
- record the same event again and assert idempotent response
- call `/users/me/exploration`
- exit the session
- print `Phase 34 public runtime smoke passed`

### Security Notes

- Keep read-only story runtime public if existing browsing policy allows anonymous read-only content.
- Keep session/event/exploration endpoints authenticated.
- Do not write bearer tokens, admin passwords, WeChat secrets, COS secrets, or provider keys into scripts or docs.
- Smoke scripts must read auth from env vars or ignored local files.
- DTOs should not leak admin-only audit fields or raw unvalidated editor payloads.

### UTF-8 Notes

- All new smoke scripts should set `[Console]::OutputEncoding = [System.Text.Encoding]::UTF8`.
- Any SQL imports must use `--default-character-set=utf8mb4` and `--execute="SOURCE ..."` rather than PowerShell input redirection.
- Do not embed large Chinese payloads in inline PowerShell commands.

## Validation Architecture

| Validation Layer | Command | Purpose |
|------------------|---------|---------|
| Public backend compile | `mvn -q -DskipTests compile -f packages/server/pom.xml` | DTO/service/controller compile safety |
| Mini-program build | `npm run build:weapp --prefix packages/client` | Taro/TypeScript/Webpack integration safety |
| Runtime smoke | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-34-public-runtime.ps1` | Live public runtime, auth/session/event/exploration proof |
| Static pattern checks | `Select-String` assertions in plan acceptance criteria | Ensures exact endpoints/types/UI labels exist |

## Pitfalls

- Do not make the mini-program interpret raw `overridePolicy`, `triggerConfig`, `conditionConfig`, or `effectConfig` as if it knew all admin semantics. Backend should derive traveler-safe display fields.
- Do not remove the existing `/api/v1/story-lines` list/detail path unless all callers are migrated.
- Do not make story browsing require auth; only session and event writes require auth.
- Do not treat build success as runtime success. The smoke must hit real public endpoints.
- Do not silently drop unsupported gameplay. Show explicit degraded cards in the mini-program.

