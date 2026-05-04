# Phase 42: Traveler Gameplay Event Engine - Context

**Gathered:** 2026-05-04  
**Status:** Ready for planning

<domain>
## Phase Boundary

Phase 42 turns the already-loaded public story runtime into a baseline playable mini-program event engine. It should interpret compiled experience-flow steps for story progression, content completion, proximity/check-in-style interactions, pickups, tasks, rewards, titles, session exit/re-entry, and unsupported-feature fallback states.

This phase must not rebuild the admin authoring model, add the Phase 43 operator support console, or implement production-grade AR/photo recognition, speech input, puzzle/cannon-defense engines, route-coverage games, or indoor visual positioning. Those advanced templates may be represented as safe pending/fallback gameplay cards only.
</domain>

<decisions>
## Implementation Decisions

### Gameplay Scope
- **D-01:** Build a baseline step interpreter inside the mini-program story/runtime path rather than another mock-only gameplay system.
- **D-02:** Supported step categories for this phase are story/content, location/proximity/check-in, pickup, task/challenge completion, reward/title acquisition, and unsupported/pending-feature fallback.
- **D-03:** Advanced templates such as AR photo recognition, speech input, puzzle restoration, cannon defense, route coverage, and indoor visual positioning remain non-crashing fallback cards unless they can already be expressed as simple click/content/task events.
- **D-04:** All traveler-facing event feedback must be Traditional Chinese and explain whether the action was accepted, already completed, failed/retryable, blocked by auth/session, or pending a future gameplay version.

### Public Runtime Contract
- **D-05:** Reuse the existing `packages/server` public runtime/session/event APIs as the source of truth. Do not create a parallel client-only progression ledger.
- **D-06:** Stateful events must require a real authenticated user and an active story session; anonymous users may view read-only story/runtime content only.
- **D-07:** Event submissions must keep stable idempotency keys derived from storyline, session, chapter, step/block/element, and normalized event type so duplicate taps do not grant duplicate rewards.
- **D-08:** The client should treat backend duplicate responses as a successful "already synced" state, not as a traveler-visible failure.
- **D-09:** Session exit/re-entry must follow backend state: temporary story-mode progress may clear on exit, while permanent exploration events and earned outcomes remain durable.

### Traveler Feedback and State
- **D-10:** The mini-program should show local action state for each runtime step: not started, syncing, synced, duplicate/already done, failed/retryable, and unsupported.
- **D-11:** Feedback should mention concrete gameplay outcomes when the backend returns enough information: collected clues/items, task progress, medals, titles, coins, chapter progress, or exploration summary changes.
- **D-12:** When backend responses are still generic, the client may use the compiled runtime step metadata to show truthful generic copy such as "線索已同步" or "任務進度已記錄"; it must not invent a specific reward that the backend did not confirm.
- **D-13:** After accepted stateful events, refresh the story exploration summary and persisted story session context so route highlighting and progress remain aligned with backend state.

### Verification
- **D-14:** Phase 42 verification should include backend event/session smoke plus `npm run build:weapp`; it may reuse the Phase 41 WeChat UAT harness but must add event-engine assertions.
- **D-15:** Physical-device UAT remains a separate evidence item and must not be marked passed unless actually performed.
- **D-16:** All scripted Chinese payloads, fixture JSON, SQL, and test data must be UTF-8/utf8mb4 and should not be written through inline PowerShell Chinese literals.

### the agent's Discretion
- The planner may decide whether to keep the event interpreter in `pages/story/index.tsx` temporarily or extract it into a dedicated client service/helper if that reduces page complexity.
- The planner may add small backend response fields or DTO enrichment if needed for truthful client feedback, but should avoid broad reward/backpack/admin workflows that belong to Phase 43.
- The exact visual treatment of feedback chips, toasts, inline cards, and unsupported states can follow the current story page design as long as Traditional Chinese copy and clear state transitions are preserved.
</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### v3.2 Planning
- `.planning/PROJECT.md` - Project constraints, admin/public ownership, real-backend cutover principles, UTF-8 rule, and verification expectations.
- `.planning/REQUIREMENTS.md` - Requirements `PLAY-02`, `PLAY-03`, and `PLAY-04`.
- `.planning/ROADMAP.md` - Phase 42 goal and success criteria.
- `.planning/STATE.md` - Current v3.2 state and carryover caveats.

### Phase 41 Baseline and UAT
- `.planning/phases/41-wechat-runtime-uat-harness-and-story-entry-hardening/41-CONTEXT.md` - Phase 41 boundary and deferred gameplay scope.
- `.planning/phases/41-wechat-runtime-uat-harness-and-story-entry-hardening/41-VERIFICATION.md` - Verified story runtime/media baseline and pending physical-device UAT caveat.
- `.planning/phases/41-wechat-runtime-uat-harness-and-story-entry-hardening/41-UAT.md` - Local backend/runtime/media/build/DevTools evidence produced by the Phase 41 harness.
- `docs/mini-program-wechat-uat.md` - WeChat DevTools, backend target, environment matrix, and physical-device caveats.

### Mini-program Runtime
- `packages/client/src/pages/story/index.tsx` - Current story page, runtime step cards, action state handling, session start/exit, content event reporting, and media completion hooks.
- `packages/client/src/services/gameService.ts` - Story runtime mapping, session persistence, auth gate, idempotent event id builder, and public event submission wrapper.
- `packages/client/src/services/api.ts` - Public API wrappers for runtime, sessions, events, and exploration summary.
- `packages/client/src/types/game.ts` - Story runtime, media, event, session, reward, and exploration DTO types used by the mini-program.
- `packages/client/package.json` - `build:weapp` and Phase 41 smoke script entrypoints.

### Public Backend Runtime
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/ExperienceController.java` - Public runtime, session start/event/exit, and user exploration endpoints.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/PublicExperienceService.java` - Public experience service contract.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicExperienceServiceImpl.java` - Runtime compilation, event validation, idempotency, session updates, and exploration summary logic.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/request/ExperienceEventRequest.java` - Allowed public event request fields and event type contract.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/ExperienceEventResponse.java` - Accepted/duplicate/current-chapter/message response contract.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/StorylineSessionResponse.java` - Story session state returned to the mini-program.
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto/response/UserExplorationResponse.java` - Dynamic exploration summary returned after accepted events.

### Testing and Constraints
- `.planning/codebase/TESTING.md` - Current low automated coverage and expected verification commands.
- `.planning/codebase/CONVENTIONS.md` - Frontend/backend conventions and encoding cautions.
- `.planning/codebase/ARCHITECTURE.md` - Public backend, mini-program, and state-management topology.
</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `recordStoryRuntimeEvent` in `packages/client/src/services/gameService.ts` already normalizes event names, gates stateful events, builds idempotent client event ids, and routes session events to the public backend.
- `startStoryModeSession`, `exitStoryModeSession`, and `refreshStoryExplorationSummary` in `packages/client/src/services/gameService.ts` already provide the session lifecycle primitives Phase 42 should build on.
- `renderRuntimeFlow` and `handleRuntimeStepAction` in `packages/client/src/pages/story/index.tsx` already render runtime step cards and submit simple pickup/task/reward/unsupported events.
- `PublicExperienceServiceImpl` already records events, detects duplicate client event ids, updates story sessions, and computes user exploration summaries from published exploration elements.

### Established Patterns
- Mini-program behavior currently blends live public API calls with local state helpers in `gameService.ts`; Phase 42 should narrow that gap by making story gameplay use backend-confirmed state wherever possible.
- Anonymous browsing is allowed, but interactive/stateful actions should route through auth/session gates.
- Runtime media fallback and unsupported-state visibility were hardened in Phase 41; Phase 42 should extend that truthful-fallback approach to gameplay actions.
- Automated coverage remains light, so changes need targeted smoke scripts or deterministic command evidence close to the touched feature.

### Integration Points
- Client event interpreter connects through `packages/client/src/pages/story/index.tsx`, `packages/client/src/services/gameService.ts`, `packages/client/src/services/api.ts`, and `packages/client/src/types/game.ts`.
- Backend contract changes, if any, connect through `ExperienceController`, `PublicExperienceServiceImpl`, and the `ExperienceEvent*`/`StorylineSession*`/`UserExploration*` DTOs.
- Verification can extend `scripts/local/smoke-phase-41-wechat-runtime-uat.ps1` or add a Phase 42-specific smoke that checks runtime steps, session start, event record, duplicate submission, exploration refresh, and session exit.
</code_context>

<specifics>
## Specific Ideas

- Keep the first pass playable with tappable cards and deterministic simulated proximity/check-in buttons rather than pretending unavailable phone sensors or AR engines are complete.
- Use backend `duplicate=true` as a positive state such as "已記錄過，不會重複發放".
- For unsupported advanced steps, show the configured step/template type and a clear copy such as "此玩法已配置，將於後續小程序玩法版本開放".
- Preserve the flagship `東西方文明的戰火與共生` as the main runtime verification story.
</specifics>

<deferred>
## Deferred Ideas

- Phase 43 owns admin/operator support for traveler sessions, exploration events, backpack, rewards, titles, rule traces, and audit-backed repairs.
- Phase 44 owns final admin IA polish, broader release acceptance packaging, and real-device/manual UAT consolidation.
- Production-grade AR/photo recognition, speech input, puzzle/cannon-defense minigames, route coverage, and indoor visual positioning remain future gameplay phases.
</deferred>

---

*Phase: 42-traveler-gameplay-event-engine*  
*Context gathered: 2026-05-04*
