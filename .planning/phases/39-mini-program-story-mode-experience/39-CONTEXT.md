# Phase 39: Mini-program Story Mode Experience - Context

**Gathered:** 2026-05-03
**Status:** Ready for planning
**Source:** `/gsd-next` routed to `/gsd-discuss-phase 39`; Default mode fallback used roadmap requirements, prior phase decisions, and targeted mini-program code scout because interactive question routing is unavailable in this mode.

<domain>
## Phase Boundary

Phase 39 completes the traveler-facing mini-program story-mode acceptance slice for the flagship `東西方文明的戰火與共生` story against the live public backend runtime from Phase 38.

This phase owns:

- Mini-program story page consumption of `GET /api/v1/storylines/{id}/runtime` as the source of truth for story introduction, chapters, ordered content blocks, generated media, runtime steps, and fallback/unsupported states.
- Story-mode session lifecycle in the mini-program: start when the user intentionally begins a story journey, report runtime events with stable idempotency keys, and exit without deleting permanent exploration facts.
- Story-mode route presentation: display chapter sequence, current chapter destination, active/inactive route state, and map handoff details based on runtime DTOs.
- Baseline interactive object UX for configured runtime steps: tappable pickup/task/reward/unsupported cards with Traditional Chinese feedback, backend event reporting, and progress refresh where authenticated.
- Graceful degradation for AR recognition, speech input, puzzle minigames, cannon defense, and other complex gameplay placeholders so the story path remains usable and honest.
- Mini-program build and local smoke evidence proving the story journey can run against the Phase 38 public backend.

This phase does not own:

- New admin authoring workbenches, material production, material QA, or public runtime sanitation. Those were handled in Phases 36-38.
- Full production-grade AR/photo recognition, speech-input gameplay, puzzle engines, route-coverage games, or cannon defense gameplay.
- Final release/UAT packaging, cost dashboards, WeChat device checklist closure, or full milestone readiness evidence. Those belong to Phase 40.
- Replacing the existing Taro/React mini-program architecture or rewriting `gameService.ts` wholesale.

</domain>

<decisions>
## Implementation Decisions

### Story Runtime Consumption

- **D39-01:** The mini-program story page must prefer live public runtime data from `GET /api/v1/storylines/{storylineId}/runtime?locale=zh-Hant`; local/mock data is fallback only.
- **D39-02:** The existing `StoryPage`, `refreshStorylineRuntime`, `StoryContentBlockRenderer`, and `LottieAssetPlayer` are the baseline to extend. Do not create a parallel story page or second runtime mapper unless planning finds a hard blocker.
- **D39-03:** Runtime data should be mapped into typed mini-program state once in `gameService.ts` / `api.ts`, then rendered by UI components. UI code should not parse raw backend JSON ad hoc.
- **D39-04:** Story chapters must remain ordered by runtime chapter order and display chapter title, summary, anchor/destination, conditions/effects labels, content blocks, and compiled runtime steps.
- **D39-05:** Story content blocks can include rich text, quote, image, gallery, audio, video, Lottie, and attachment lists. The UI must not assume one chapter has only one media item.
- **D39-06:** `StoryMediaAssetItem` should carry Phase 38 availability/fallback fields so unavailable media renders an explicit placeholder instead of pretending the asset is playable.

### Story Mode Session And Events

- **D39-07:** Starting story mode is an intentional user action, not just opening the read-only story page. Anonymous users may browse, but starting a session or reporting stateful progress requires auth.
- **D39-08:** When authenticated, story-mode start should call `POST /api/v1/storylines/{id}/sessions/start` and store the active `sessionId` in local story state.
- **D39-09:** Events must use stable idempotency keys that distinguish story, chapter, step/block, event type, and session where needed. Repeated taps or retry after network loss must not duplicate progress.
- **D39-10:** Required event coverage for this phase: `story_opened` or current backend-compatible story open event, `chapter_started`, `content_viewed`, `media_completed`, `pickup_interacted`, `task_completed`, `reward_acquired`, `unsupported_viewed`, and `story_session_exit`.
- **D39-11:** Event failures should not crash or block reading the story. Show lightweight Traditional Chinese feedback for stateful action failures and allow retry.
- **D39-12:** Session exit should call the public exit endpoint when a session exists and clearly explain that temporary story-mode route state resets, while already gained permanent exploration/reward facts remain.

### Route And Map Presentation

- **D39-13:** Phase 39 should make story mode visually clear without requiring a full navigation engine. The required baseline is route/chapter sequence, current chapter highlight, inactive chapter/route grey state, and current destination details.
- **D39-14:** The map page should support handoff from the story page with story/chapter context, then select or highlight the current destination where the runtime anchor can be resolved to a POI/location.
- **D39-15:** If precise route geometry is unavailable, render an honest route summary and chapter progression strip instead of fake map polylines.
- **D39-16:** The story-mode map state should be separable from normal map browsing. Exiting story mode clears temporary route emphasis but not check-ins, pickups, rewards, or exploration events.
- **D39-17:** Route UI copy should use traveler language, for example `目前章節`, `下一站`, `主線路線`, `支線稍後開放`, and `已完成章節`.

### Baseline Interactive Objects

- **D39-18:** Runtime steps should be grouped into traveler-facing cards such as `劇情播放`, `地點互動`, `拾取線索`, `任務`, `隱藏挑戰`, `獎勵`, and `稍後開放`.
- **D39-19:** Tappable pickup/task/reward steps should report the configured event type and then refresh exploration progress when authenticated.
- **D39-20:** If a runtime step is configured as unsupported or maps to future complex gameplay, it should remain visible as a disabled/educational card with `稍後開放` copy and should report `unsupported_viewed` at most once per relevant step/session.
- **D39-21:** Reward and pickup feedback should be modest but clear in this phase: toast/card state/progress refresh are enough. Full-screen reward cinematics and complex inventory UI are not required unless already available through existing components.
- **D39-22:** The mini-program should never silently grant rewards locally without backend acceptance. Local optimistic display is allowed only as a pending state and must reconcile with the backend event response or exploration summary.

### Media Playback And Fallbacks

- **D39-23:** Lottie rendering must keep the Phase 28/34 baseline: WeChat canvas, network JSON URL, poster/fallback image, and animation destroy on unmount/page exit.
- **D39-24:** Audio playback should report `media_completed` on actual end where possible, not simply on card render.
- **D39-25:** Video playback should show controls, use poster/fallback when the runtime asset is unavailable, and report completion when supported by Taro events.
- **D39-26:** Unavailable or unpublished media from Phase 38 DTOs should render the backend-provided Traditional Chinese reason or a clear local fallback reason such as `媒體資源暫時未能載入`.

### Auth And Anonymous Browsing

- **D39-27:** Keep the earlier auth contract: anonymous users can browse read-only story content, but cannot start sessions, pick up objects, complete tasks, save progress, or claim rewards.
- **D39-28:** Auth-required story actions should use the existing central auth/token behavior. Do not auto-create guest identities or synthetic `openId`.
- **D39-29:** In local/devtools, dev-bypass may be used only through existing explicit local/dev config. Experience/production builds must use real WeChat auth.

### Verification

- **D39-30:** Verification must include mini-program compile/type/build compatibility and a local story-mode smoke that exercises public runtime load, content rendering data mapping, session start, event reporting, progress refresh, and session exit where auth is available.
- **D39-31:** Browser-like or WeChat DevTools manual checks should be captured as evidence if possible, but final device UAT checklist and release readiness remain Phase 40.
- **D39-32:** The primary acceptance fixture is `東西方文明的戰火與共生`; it must show five chapters, generated media, unavailable video fallback where configured, runtime steps, unsupported placeholders, and progress/event behavior.
- **D39-33:** No provider keys, COS secrets, bearer tokens, or admin-only generation provenance may be printed or committed during mini-program verification.

### the agent's Discretion

- The planner may decide whether to store active story session state inside `gameService.ts`, a small dedicated story runtime store, or component state, provided persistence/retry behavior is clear.
- The planner may decide the exact visual treatment of the route strip and interactive cards, but it must feel intentional, readable on WeChat mobile, and not look like a raw debug console.
- The planner may decide whether Phase 39 smoke is implemented as a Taro build plus script-level API simulation, a mini-program service unit smoke, or both.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project And Milestone Scope

- `AGENTS.md` - project stack, UTF-8/utf8mb4 rule, admin/public ownership, COS/media constraints, security constraints, and verification expectations.
- `.planning/PROJECT.md` - v3.1 active scope and Phase 39/40 boundary.
- `.planning/REQUIREMENTS.md` - `MP-01` through `MP-05`, plus `RUN-01` through `RUN-04` that Phase 39 consumes.
- `.planning/ROADMAP.md` - Phase 39 goal, dependencies, success criteria, and v3.1 milestone sequence.
- `.planning/STATE.md` - current Phase 39 position and local runtime caveats.

### Prior Runtime And Asset Decisions

- `.planning/phases/34-public-runtime-and-mini-program-consumption-baseline/34-CONTEXT.md` - public runtime endpoint family, mini-program baseline rendering, Lottie handling, anonymous read/auth-gated write policy, and deferred full UAT boundary.
- `.planning/phases/36-material-production-pipeline-and-asset-promotion/36-VERIFICATION.md` - generated image/audio/video/COS evidence consumed by the story runtime.
- `.planning/phases/37-material-qa-workspace-and-reuse-controls/37-CONTEXT.md` - material QA status, reuse controls, preview/fallback expectations, and approved asset behavior.
- `.planning/phases/38-public-runtime-asset-consumption/38-CONTEXT.md` - sanitized runtime asset DTOs, event allowlist, fallback/unsupported semantics, privacy rules, and authenticated smoke expectations.
- `.planning/phases/38-public-runtime-asset-consumption/38-VERIFICATION.md` - verified Phase 38 public runtime and event/session behavior.

### Flagship Story Content

- `docs/content-packages/east-west-war-and-coexistence/content-manifest.json` - canonical material item keys, usage targets, local/COS links, and story material references.
- `docs/content-packages/east-west-war-and-coexistence/story-script.md` - five-chapter story structure and traveler-facing narrative expectations.
- `docs/content-packages/east-west-war-and-coexistence/audio-scripts.md` - narration scripts and audio intent.
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-production-report.json` - generated asset evidence.
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-board-slice-report.json` - sliced pickup/title icon evidence.
- `docs/content-packages/east-west-war-and-coexistence/production-runs/phase36-video-jobs.json` - chapter MP4 assembly and caption metadata.

### Mini-program Runtime

- `packages/client/src/pages/story/index.tsx` - current story page, runtime sync, chapter expansion, content block rendering, and baseline event reporting.
- `packages/client/src/pages/story/index.scss` - story page visual system to extend for route/current chapter/interactive card states.
- `packages/client/src/pages/map/index.tsx` - map page destination selection, route summary, story card, and map handoff points.
- `packages/client/src/services/api.ts` - public runtime/session/event/exploration API DTOs and request helpers.
- `packages/client/src/services/gameService.ts` - public content refresh, story runtime mapping, session/event helpers, local fallback state, and game-state persistence.
- `packages/client/src/types/game.ts` - mini-program story, runtime step, media asset, content block, and session types.
- `packages/client/src/components/StoryContentBlockRenderer/index.tsx` - content block rendering for image/gallery/audio/video/Lottie/attachments.
- `packages/client/src/components/LottieAssetPlayer/index.tsx` - Lottie JSON canvas lifecycle and fallback implementation.
- `packages/client/src/store/userStore.ts` - auth state conventions if story actions need central auth gating.
- `packages/client/config/dev.js` - local/dev feature flags and API target behavior.
- `packages/client/config/prod.js` - production feature flags and mock/live behavior.

### Public Backend Runtime Contract

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` - public runtime, story session, event, exit, and exploration endpoints.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - compiled runtime, event idempotency, session updates, and exploration progress behavior.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceRuntimeResponse.java` - public storyline runtime DTO root.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/ExperienceEventRequest.java` - public event request contract.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryMediaAssetResponse.java` - media/fallback/availability DTO consumed by the mini-program.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/UserExplorationResponse.java` - dynamic exploration progress response.

### Smoke And Verification Patterns

- `scripts/local/smoke-phase-34-public-runtime.ps1` - public runtime/event/session smoke pattern.
- `scripts/local/smoke-phase-38-public-runtime-assets.ps1` - Phase 38 generated asset/runtime/event smoke and local dev-bypass convention.
- `packages/client/package.json` - Taro build scripts for WeChat mini-program compatibility checks.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `packages/client/src/pages/story/index.tsx` already loads public catalog data, refreshes the selected storyline runtime, renders chapter cards, shows runtime flow steps, reports content/unsupported events, and uses `StoryContentBlockRenderer`.
- `packages/client/src/services/api.ts` already defines `getPublicStorylineRuntime`, `startPublicStorylineSession`, `recordPublicExperienceEvent`, `recordPublicStorylineSessionEvent`, `exitPublicStorylineSession`, and `getPublicUserExploration`.
- `packages/client/src/services/gameService.ts` already has `refreshStorylineRuntime`, `startStorylineRuntimeSession`, `exitStorylineRuntimeSession`, and `recordStoryRuntimeEvent`, but Phase 39 should harden session usage and event naming/idempotency.
- `StoryContentBlockRenderer` already renders image, gallery, audio, video, Lottie, and attachments with Traditional Chinese missing-media states.
- `LottieAssetPlayer` already uses `lottie-miniprogram`, network JSON fetch, canvas setup, fallback image, loading/error states, and destroy on unmount.
- `packages/client/src/pages/map/index.tsx` already has selected POI, walking route summary, related story card, and map markers, giving Phase 39 a natural map handoff path.

### Established Patterns

- Mini-program live data comes through `api.ts` and is normalized/persisted in `gameService.ts`.
- Anonymous/authenticated distinction is already represented in game state through `authStatus`; stateful actions should no-op or gate when anonymous.
- Story page currently auto-reports some content/unsupported events on render; Phase 39 should refine this so actual media completion and interactive taps use appropriate event timing.
- Existing map route summary is not a full navigation engine; Phase 39 should build a reliable story route baseline before attempting richer map polylines.
- Taro components and WeChat APIs constrain media/Lottie behavior; avoid browser-only APIs.

### Integration Points

- Extend `StoryMediaAssetItem` and `mapStoryMediaAsset` to include Phase 38 `availability`, `unavailableReason`, `usageHint`, and duration/file-size fields if present.
- Add active story session state and current chapter state near `StoryPage` / `gameService.ts`.
- Add story-mode map handoff through route params, storage, or existing game state so `pages/map/index.tsx` can highlight the current destination.
- Add event reporting hooks around explicit user actions and media ended events, not only render-time effects.
- Add a Phase 39 smoke/build script only if it can run without secrets and without printing bearer tokens.

</code_context>

<specifics>
## Specific Ideas

- Traveler experience should feel like entering a story mode: an intro, a visible current chapter, route progression, and cards for what to do next.
- The first acceptance target is not full AR/puzzle fidelity; it is that the story can be opened, read, followed, tapped, progressed, and safely exited without mock assumptions or blank states.
- Unsupported gameplay should be visible and understandable: `此互動玩法已由後台配置，將在後續小程序玩法版本中開放。`
- If a media asset is unavailable, keep the block/step visible with a friendly fallback reason instead of hiding the content.
- Use the `east_west_war_and_coexistence` runtime as the canonical fixture for every Phase 39 test and smoke.

</specifics>

<deferred>
## Deferred Ideas

- Full AR photo recognition, visual positioning, speech-triggered gameplay, puzzle/minigame engines, route-coverage detection, and cannon defense implementation.
- Final WeChat device UAT checklist, release readiness dashboard, and cost/history visibility, which belong to Phase 40.
- Advanced media production or regeneration, which remains in the material pipeline rather than mini-program runtime work.
- Full navigation routing engine with real path geometry if Phase 39 cannot derive safe route geometry from the current DTOs.

</deferred>

---

*Phase: 39-mini-program-story-mode-experience*
*Context gathered: 2026-05-03*
