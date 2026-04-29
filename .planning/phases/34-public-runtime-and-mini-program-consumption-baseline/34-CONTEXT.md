# Phase 34: Public Runtime and Mini-program Consumption Baseline - Context

**Gathered:** 2026-04-30
**Status:** Ready for planning
**Source:** `/gsd-next` routed to `/gsd-discuss-phase 34`; Default mode fallback used prior locked decisions because interactive questions are unavailable.

<domain>
## Phase Boundary

Phase 34 is the final v3.0 linkage phase. It aligns the public backend runtime contract and the mini-program story consumption baseline with the admin-authored experience system delivered in Phases 28-33.

This phase owns:

- Public runtime APIs that expose compiled POI and storyline experience flows, not raw admin editor JSON.
- Public storyline runtime DTOs that include storyline intro, route strategy, chapters, anchors, inherited/overridden steps, content blocks, media assets, Lottie metadata, exploration hooks, and clear unsupported-gameplay markers.
- Public story session and event-reporting flows that remain authenticated, idempotent, and compatible with the Phase 32 dynamic exploration model.
- Mini-program story-page consumption of the real public runtime baseline for the seeded `東西方文明的戰火與共生` route.
- Mini-program rendering for storyline introduction, chapter list, content blocks, Lottie, image, audio, video, current chapter route state, and basic event reporting.
- Smoke verification across local MySQL, public backend, admin-authored seed data, and mini-program build/type checks.

This phase does not own:

- Full WeChat DevTools experiential acceptance, broader mini-program UX acceptance, or complete story-mode gameplay polish. The user explicitly deferred that slice beyond v3.0.
- AR/photo recognition, voice input, route-coverage games, puzzle gameplay, cannon defense, or fully interactive map gameplay. These must degrade through template/runtime metadata instead of blank-screening.
- Rebuilding admin authoring workbenches already delivered in Phases 28-33.
- Deep publish-approval workflows beyond the lifecycle/runtime fields required for this baseline.

</domain>

<decisions>
## Implementation Decisions

### Public Runtime Contract

- **D34-01:** Public runtime endpoints are the mini-program source of truth. The mini-program must consume compiled runtime DTOs from `packages/server`, not admin-only forms or local mock-only story state.
- **D34-02:** The canonical storyline runtime endpoint remains `GET /api/v1/storylines/{storylineId}/runtime`, with optional locale support.
- **D34-03:** The canonical POI default experience endpoint remains `GET /api/v1/experience/poi/{poiId}`, with optional locale support.
- **D34-04:** Runtime DTOs must include only traveler-safe fields. They must not expose admin-only audit data, draft-only payloads, raw secret-bearing config, or unvalidated editor JSON.
- **D34-05:** Compiled runtime flows should normalize inherited target steps plus chapter overrides into an ordered flow that the mini-program can render without reimplementing admin merge semantics.
- **D34-06:** DTOs should keep enough provenance for debugging, such as stable codes, step ids, chapter ids, asset ids, and unsupported template type labels.

### Storyline Runtime Shape

- **D34-07:** Storyline runtime must include the seeded `東西方文明的戰火與共生` introduction, five-chapter route, story-mode map strategy, chapter anchors, content blocks, media assets, Lottie assets, and exploration element summaries.
- **D34-08:** Chapter runtime must distinguish permanent exploration facts from temporary story-session state, following Phase 32.
- **D34-09:** Chapter content blocks are ordered and reusable. The mini-program should not assume one chapter equals one text body or one media asset.
- **D34-10:** Lottie runtime assets use JSON URLs plus poster/fallback metadata. If the Lottie URL fails, the mini-program must show poster/fallback or a friendly unavailable state, not a white screen.
- **D34-11:** Unsupported complex gameplay templates must remain visible as explicit cards or warnings such as `此互動玩法稍後開放`, with enough metadata for future implementation.
- **D34-12:** Runtime contract should include story-mode route/current-chapter state enough for a basic route status view, but full map path rendering can remain a future milestone.

### Mini-program Consumption Baseline

- **D34-13:** The story page should prefer live public data after `refreshPublicContent`, while preserving a local fallback if the backend is unavailable.
- **D34-14:** Mini-program story consumption should render storyline tabs/list, active story overview, intro copy, chapter list, chapter detail, conditions/effects labels, and ordered content blocks from the runtime contract.
- **D34-15:** Existing `StoryContentBlockRenderer` and `LottieAssetPlayer` are the rendering baseline. Planning should extend them only where needed instead of replacing them.
- **D34-16:** The story page may remain read-oriented for v3.0, but it must support basic event reporting for at least story open, chapter open, content read/complete, and unsupported-interaction viewed.
- **D34-17:** Anonymous browsing policy from earlier auth work still applies: read-only story browsing can be anonymous, but stateful event reporting and story sessions require an authenticated token.
- **D34-18:** If a stateful action requires auth, the mini-program must route through the existing central auth guard rather than silently failing.

### Sessions, Events, And Exploration

- **D34-19:** `POST /api/v1/storylines/{id}/sessions/start`, session event reporting, and session exit must use the durable session model from Phase 32 where available.
- **D34-20:** Event reporting must stay idempotent through client event ids. Repeated taps or retries must not create duplicate exploration completions.
- **D34-21:** Storyline session exit may clear temporary story-session progress but must not delete permanent exploration events, check-ins, rewards, pickups, titles, or collectibles.
- **D34-22:** Public `GET /api/v1/users/me/exploration` must stay aligned with Phase 32 weighted progress semantics and be usable by the story page for progress summaries.

### Admin/Public Alignment

- **D34-23:** Phase 34 should not introduce a second story material model. It must consume Phase 33 package data through storylines, chapters, content assets, content blocks, experience flows, reward links, and exploration elements.
- **D34-24:** If public and admin DTOs diverge, the public DTO wins for mini-program behavior and should be documented as the runtime contract.
- **D34-25:** Lifecycle/status filtering must ensure public runtime only returns published traveler-eligible content while admin preview can keep seeing drafts through existing admin endpoints.
- **D34-26:** Any missing seed/runtime link discovered while compiling the public DTO should be fixed through UTF-8-safe seed/schema updates, not frontend hardcoding.

### Verification Scope

- **D34-27:** Verification must include backend compile, mini-program type/build check, SQL seed/import if needed, public API smoke, authenticated event/session smoke, and a story-page data mapping check.
- **D34-28:** Browser or WeChat DevTools full experiential UAT is not required for v3.0 closure, but the mini-program code must build and the data contract must be exercised through automated/local smoke where practical.
- **D34-29:** Phase 34 should update milestone evidence for `LINK-02`, `OPS-02`, `OPS-04`, and `VER-01` only to the extent actually verified.

### the agent's Discretion

- The exact DTO class names and mapper/service decomposition can follow existing `ExperienceRuntimeResponse`, `StoryLineResponse`, and `PublicExperienceServiceImpl` patterns.
- The planner may choose whether to add new mini-program service helpers or extend `gameService.ts`, provided live runtime consumption remains clear and fallback behavior is maintained.
- The planner may decide whether the initial smoke uses admin login plus public login/dev-bypass tokens, as long as secrets are not committed and local verification is reproducible.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project Rules And Phase Scope

- `AGENTS.md` - project stack, UTF-8/utf8mb4 rule, security constraints, GSD workflow requirement, and verification expectations.
- `.planning/PROJECT.md` - v3.0 milestone boundary, admin/public ownership, and deferred mini-program acceptance slice.
- `.planning/REQUIREMENTS.md` - `OPS-02`, `OPS-04`, `LINK-02`, and `VER-01` definitions and traceability.
- `.planning/ROADMAP.md` - Phase 34 goal, dependencies, success criteria, and v3.0 closure position.
- `.planning/STATE.md` - current GSD state and Phase 33 completion handoff.

### Prior v3.0 Decisions And Evidence

- `.planning/phases/28-story-and-content-control-plane-completion/28-CONTEXT.md` - experience orchestration, compiled runtime, Lottie, exploration, and mini-program boundary decisions.
- `.planning/phases/29-poi-default-experience-workbench/29-CONTEXT.md` - POI default flow and A-Ma Temple acceptance pattern.
- `.planning/phases/30-storyline-mode-and-chapter-override-workbench/30-CONTEXT.md` - story-mode route strategy, chapter inheritance, override semantics, and runtime endpoint expectations.
- `.planning/phases/31-interaction-task-template-library-and-governance-center/31-CONTEXT.md` - reusable template and unsupported/complex gameplay governance expectations.
- `.planning/phases/32-dynamic-exploration-and-user-progress-model/32-CONTEXT.md` - durable sessions, idempotent events, weighted exploration, and permanent-vs-temporary progress separation.
- `.planning/phases/33-complete-flagship-story-content-material-package/33-CONTEXT.md` - flagship story package scope, story codes, five chapters, material package, and historical/literary separation.
- `.planning/phases/33-complete-flagship-story-content-material-package/33-VERIFICATION.md` - Phase 33 smoke prerequisites and expected seeded package/story evidence.

### Public Backend Runtime

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` - public runtime endpoints for POI experience, storyline runtime, events, sessions, and user exploration.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/PublicExperienceService.java` - public experience service contract.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - compiled runtime merge, event recording, session, and exploration behavior.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceRuntimeResponse.java` - current public runtime DTO root.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/ExperienceEventRequest.java` - public event input contract.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceEventResponse.java` - public event response contract.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java` - public storyline session response shape.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/UserExplorationResponse.java` - public dynamic exploration response.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/StoryLineController.java` - existing public story list/detail endpoint.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/StoryLineServiceImpl.java` - existing public story list/detail mapping.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryLineResponse.java` - public story response to align or bridge.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StoryChapterResponse.java` - public chapter response to align or bridge.

### Admin Runtime Preview And Seed Sources

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStorylineModeController.java` - admin runtime preview endpoint and story-mode workbench contract.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminStorylineModeServiceImpl.java` - admin story runtime preview behavior to compare against public runtime.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminExperienceOrchestrationController.java` - admin experience flow, binding, override, and exploration APIs.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminStoryMaterialPackageController.java` - Phase 33 material package API.
- `scripts/local/mysql/init/39-phase-28-experience-orchestration.sql` - public/admin experience foundation tables.
- `scripts/local/mysql/init/40-phase-29-poi-default-experience.sql` - POI default flow seed.
- `scripts/local/mysql/init/41-phase-30-storyline-mode-overrides.sql` - story-mode override seed.
- `scripts/local/mysql/init/43-phase-32-progress-engine.sql` - dynamic exploration and session foundation.
- `scripts/local/mysql/init/47-phase-33-story-material-package-model.sql` - material package schema.
- `scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql` - flagship material asset seed.
- `scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql` - five-chapter flagship story seed.

### Mini-program Runtime

- `packages/client/src/pages/story/index.tsx` - current story page that must consume the runtime baseline.
- `packages/client/src/services/gameService.ts` - current public content refresh, story mapping, fallback state, and local gameplay store.
- `packages/client/src/services/api.ts` - mini-program public API client DTOs and request helpers.
- `packages/client/src/types/game.ts` - story/content/media runtime types used by the UI.
- `packages/client/src/components/StoryContentBlockRenderer/index.tsx` - current story content block renderer for rich text, quote, image, gallery, audio, video, Lottie, and attachments.
- `packages/client/src/components/LottieAssetPlayer/index.tsx` - current Lottie JSON player with canvas lifecycle and fallback image behavior.
- `packages/client/src/app.config.ts` - WeChat page registry and story page availability.

### Smoke And Verification Patterns

- `scripts/local/smoke-phase-28-experience.ps1` - public/admin experience smoke pattern.
- `scripts/local/smoke-phase-29-poi-experience.ps1` - POI runtime and auth smoke pattern.
- `scripts/local/smoke-phase-30-storyline-mode.ps1` - storyline mode runtime smoke pattern.
- `scripts/local/smoke-phase-32-user-progress.ps1` - durable sessions and exploration smoke pattern.
- `scripts/local/smoke-phase-33-flagship-package.ps1` - flagship package data smoke and local auth conventions.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets

- `ExperienceController` already exposes the exact public endpoint family Phase 34 needs: POI runtime, storyline runtime, experience events, story sessions, session events, session exit, and user exploration.
- `PublicExperienceServiceImpl` is the primary place to align compiled runtime DTO assembly, filtering, event idempotency, session persistence, and dynamic exploration behavior.
- `StoryLineController` and `StoryLineServiceImpl` still expose classic story list/detail under `/api/v1/story-lines`; planning should either bridge this to runtime DTOs or make the mini-program intentionally use the runtime endpoint for rich story consumption.
- `StoryContentBlockRenderer` already handles rich story content block types, including `lottie`, `audio`, `video`, `gallery`, and attachments.
- `LottieAssetPlayer` already implements `lottie-miniprogram` JSON loading, canvas setup, destroy on unmount, and poster/fallback behavior.
- `gameService.ts` already maps public story content blocks into mini-program `StorylineItem` state and preserves local fallback behavior.

### Established Patterns

- Public backend controllers return `ApiResponse<T>` and use path `/api/v1`.
- Public backend auth for stateful experience APIs uses bearer JWT through `JwtUtil`; read-only list/detail endpoints may remain public.
- Mini-program story page is currently read-oriented and relies on `refreshPublicContent()` plus local `getStorylines()` state.
- Mini-program renders Taro components and avoids browser-only APIs; Lottie uses WeChat canvas through `lottie-miniprogram`.
- Local verification relies on PowerShell smoke scripts with UTF-8-safe SQL imports and env/file-based credentials.

### Integration Points

- `ExperienceRuntimeResponse.StorylineRuntime` is the likely compiled runtime DTO root for the mini-program story baseline.
- `PublicStoryContentBlockDto` and `StoryContentBlockItem` must remain aligned so content blocks from backend render without manual frontend patching.
- Basic event reporting should be added near story page interactions and should use existing API helpers rather than direct ad hoc `Taro.request` calls where possible.
- Admin runtime preview and public runtime should be compared in smoke tests to catch admin/public contract drift.
- The Phase 33 `east_west_war_and_coexistence` seed should be the main acceptance fixture.

</code_context>

<specifics>
## Specific Ideas

- Use `東西方文明的戰火與共生` as the end-to-end acceptance story because it has five chapters, content/material package rows, Lottie/audio/image entries, exploration elements, rewards, and chapter experience flows.
- A successful story page baseline should show the story introduction, route/chapter list, chapter details, ordered media/content blocks, Lottie fallback-safe rendering, and unsupported gameplay cards instead of empty placeholders.
- Runtime flow steps should be presented in traveler language such as `劇情播放`, `地點互動`, `拾取線索`, `隱藏挑戰`, `獎勵發放`, and `稍後開放的互動玩法`.
- Public smoke should verify that the compiled runtime includes at least five chapters, content blocks, Lottie/audio/image assets, story-mode config, ordered flow steps, and safe unsupported-step metadata.
- Authenticated smoke should verify start session, record chapter/content event with a stable `clientEventId`, duplicate event idempotency, exploration summary, and session exit.
- Mini-program build verification should be enough for v3.0; full WeChat visual journey testing remains explicitly deferred.

</specifics>

<deferred>
## Deferred Ideas

- Full mini-program story-mode map rendering with live route drawing, current chapter highlight, grey inactive route overlays, and near-range reveal.
- Full WeChat DevTools experiential UAT and device journey testing.
- AR/photo recognition, voice input, puzzle games, cannon defense, route coverage detection, and other complex gameplay implementations.
- Full publish approval chain and cross-domain lifecycle scheduling UX beyond the runtime-safe lifecycle fields needed here.
- Advanced Lottie package formats, sprite sheets, and sequence-frame animation pipelines.

</deferred>

---

*Phase: 34-public-runtime-and-mini-program-consumption-baseline*
*Context gathered: 2026-04-30*
