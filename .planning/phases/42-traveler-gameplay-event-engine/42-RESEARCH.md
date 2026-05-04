# Phase 42 Research: Traveler Gameplay Event Engine

**Researched:** 2026-05-04

## Phase Goal

Make baseline story gameplay actually playable from compiled runtime steps instead of showing static cards only. Phase 42 must cover `PLAY-02`, `PLAY-03`, and `PLAY-04` without expanding into Phase 43 admin support tooling or future advanced gameplay engines.

## Current Implementation Facts

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` already exposes public runtime, story session start, story session event, story session exit, and user exploration endpoints under `/api/v1`.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` already compiles inherited POI/default flows, story chapter flows, and overrides into `compiledSteps`.
- `PublicExperienceServiceImpl` already records `UserExplorationEvent` rows, validates `payloadJson`, handles `clientEventId` idempotency, updates `UserStorylineSession`, and computes dynamic exploration summaries.
- `ExperienceEventRequest` currently supports `elementId`, `elementCode`, `eventType`, `eventSource`, `storylineSessionId`, `clientEventId`, `payloadJson`, and `occurredAt`.
- `ExperienceEventResponse` currently returns `accepted`, event identity, `duplicate`, `acceptedAt`, `currentChapterId`, and `message`; the TypeScript DTO does not yet expose all of these fields.
- Backend allowed event types currently include story/content/media/pickup/task/reward/unsupported/session-exit events, but not explicit click, proximity, or check-in events required by `PLAY-02`.
- `packages/client/src/pages/story/index.tsx` already renders runtime step cards through `renderRuntimeFlow`, maps categories through `getStepCardCategory`, reports actions through `handleRuntimeStepAction`, reports media completion, starts/exits story mode, and refreshes exploration.
- `packages/client/src/services/gameService.ts` already provides `startStoryModeSession`, `exitStoryModeSession`, `refreshStoryExplorationSummary`, `recordStoryRuntimeEvent`, and `buildStoryRuntimeClientEventId`.
- Current mini-program action state is too small for Phase 42: it has only pending/accepted/failed and does not represent duplicate/already-synced, unsupported, retryable, or auth/session-blocked states.
- Current client feedback is mostly generic toast text, so it cannot satisfy `PLAY-03` for visible Traditional Chinese feedback across pickups, task progress, medals, titles, coins, and unsupported advanced gameplay.

## Planning Implications

### What Phase 42 Should Build

- Backend event contract enrichment for explicit `click_interacted`, `proximity_reached`, and `checkin_completed` baseline events.
- Backend response fields for truthful Traditional Chinese feedback, duplicate/already-synced state, current chapter, and optional exploration summary.
- Mini-program runtime event interpreter that classifies compiled steps, builds deterministic event payloads, gates stateful events behind auth/session, treats duplicates as success, and refreshes backend-backed story progress.
- Traveler-visible feedback cards or chips for synced, already synced, retryable failure, auth/session blocked, and unsupported/pending-feature states.
- A Phase 42 smoke script that verifies runtime load, authenticated session start, event submit, duplicate idempotency, exploration refresh, session exit, and optional `npm run build:weapp`.
- Documentation and verification evidence that clearly separates automated smoke, DevTools checks, and physical-device UAT.

### What Phase 42 Should Not Build

- Phase 43 admin/operator pages for inspecting traveler sessions, backpack, rewards, titles, rule traces, or support repairs.
- Real AR/photo recognition, speech input, puzzle restoration, cannon defense, route coverage, or indoor visual positioning engines.
- A parallel client-only progression ledger that can diverge from `packages/server`.
- A new admin authoring model or story configuration workflow.

## Recommended Technical Approach

- Keep `packages/server` as the source of truth for event acceptance, duplicate detection, session state, and exploration summary.
- Extend existing DTOs and service helpers instead of adding new public event endpoints.
- Add explicit public event types for click/proximity/check-in while preserving backward compatibility with existing event names.
- Keep reward/title/coin feedback truthful: backend can confirm that an event was synced and can echo sanitized configured outcome labels from payload/runtime metadata, but it must not claim a durable grant that no backend rule has persisted.
- Move mini-program step classification and feedback formatting into a small helper service if it reduces `pages/story/index.tsx` complexity.
- Use stable `clientEventId` keys based on storyline, session, chapter, step/block/element, and normalized event type.
- Treat `duplicate=true` as `already_synced` and show Traditional Chinese copy such as `已記錄過，不會重複發放`.
- Continue allowing anonymous read-only story browsing, but block stateful events with visible copy until the user starts an authenticated story session.

## Technical Risks

- **False gameplay completion:** If the client shows specific medals/titles/coins from step metadata while the backend has not granted them, the UI may overclaim. Mitigation: use generic synced copy unless backend returns confirmed or configured labels, and phrase labels as synced outcomes rather than a final wallet/backpack grant.
- **Duplicate reward risk:** Duplicate taps must not grant duplicate rewards. Mitigation: deterministic `clientEventId`, backend idempotency, and duplicate-as-success UI.
- **Session mismatch:** Exited or missing sessions could leave the route highlighted locally while backend state is cleared. Mitigation: refresh session/exploration after accepted events and clear temporary local action state on exit.
- **Auth confusion:** Anonymous users may browse content but cannot perform stateful actions. Mitigation: central stateful event check in `recordStoryRuntimeEvent` and visible story page lock copy.
- **Encoding regression:** Phase 42 includes Traditional Chinese copy and docs. Mitigation: write files as UTF-8 and avoid inline PowerShell Chinese payload writes.

## Validation Architecture

Phase 42 validation should have four layers:

1. **Backend compile smoke:** `mvn -q -DskipTests compile -f packages/server/pom.xml` verifies Java DTO/service/controller changes.
2. **Client build smoke:** `npm run build:weapp` from `packages/client` verifies Taro/TypeScript integration and generated WeChat output.
3. **Runtime event smoke:** `scripts/local/smoke-phase-42-gameplay-event-engine.ps1` verifies authenticated story session start, event submit, duplicate submit, exploration refresh, and session exit against local `packages/server`.
4. **Manual UAT checklist:** `docs/mini-program-wechat-uat.md` and `42-VERIFICATION.md` record DevTools or physical-device status without claiming real-device pass unless actually performed.

## Recommended Plan Shape

- **Plan 42-01:** Backend public event contract, idempotent response enrichment, and Java compile verification.
- **Plan 42-02:** Mini-program runtime step interpreter, state model, Traditional Chinese feedback, and session exit/re-entry behavior.
- **Plan 42-03:** Phase 42 smoke script, npm alias, UAT documentation, and verification scaffolding.

## Verification Commands

- `mvn -q -DskipTests compile -f packages/server/pom.xml`
- `npm run build:weapp` in `packages/client`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-42-gameplay-event-engine.ps1 -Quick`
- `npm run smoke:phase42:gameplay-event-engine` in `packages/client`

## RESEARCH COMPLETE
