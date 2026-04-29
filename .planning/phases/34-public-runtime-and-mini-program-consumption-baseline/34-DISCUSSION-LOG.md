# Phase 34: Public Runtime and Mini-program Consumption Baseline - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in `34-CONTEXT.md`; this log preserves the routing and fallback choices.

**Date:** 2026-04-30
**Phase:** 34-public-runtime-and-mini-program-consumption-baseline
**Areas discussed:** Public runtime contract, mini-program consumption baseline, sessions/events/exploration, verification boundary

---

## Routing

`$gsd-next` inspected `.planning/STATE.md`, `.planning/ROADMAP.md`, and phase directories. Phase 33 is complete and Phase 34 exists in the roadmap but had no phase directory or context file, so the next workflow was `/gsd-discuss-phase 34`.

Interactive question UI was unavailable in Default mode. The workflow fallback used locked decisions from prior Phase 28-33 context files and the roadmap instead of asking new questions.

---

## Public Runtime Contract

| Option | Description | Selected |
|--------|-------------|----------|
| Compiled runtime DTOs | Mini-program consumes traveler-safe compiled runtime from `packages/server`. | ✓ |
| Raw admin payloads | Mini-program interprets admin editor JSON and merge semantics itself. | |
| Local mock-first | Mini-program keeps mock story state as the primary runtime source. | |

**Captured decision:** Phase 34 must expose compiled public DTOs, not raw admin forms or mock-only state.

---

## Mini-program Consumption

| Option | Description | Selected |
|--------|-------------|----------|
| Story baseline only | Story page consumes introduction, chapters, content blocks, Lottie/media, route state, and basic events. | ✓ |
| Full WeChat journey UAT | Complete experiential acceptance across map/story gameplay in WeChat DevTools. | |
| Backend only | Stop after public endpoints and defer all client wiring. | |

**Captured decision:** v3.0 closes the story consumption baseline but keeps full mini-program experiential acceptance deferred as previously agreed.

---

## Unsupported Gameplay

| Option | Description | Selected |
|--------|-------------|----------|
| Explicit degraded cards | Complex gameplay appears as friendly unsupported/coming-soon cards with metadata. | ✓ |
| Hide unsupported steps | Runtime omits complex steps until implemented. | |
| Attempt partial gameplay | Implement partial AR/puzzle/voice/cannon mechanics in this phase. | |

**Captured decision:** Unsupported complex gameplay must not white-screen or silently disappear; it should degrade clearly.

---

## Verification

| Option | Description | Selected |
|--------|-------------|----------|
| Local contract smoke + builds | Backend compile, mini-program build/type check, public runtime smoke, auth/session/event smoke. | ✓ |
| Manual-only verification | Rely on browser/DevTools inspection without repeatable smoke. | |
| Full production-like E2E | Build a full WeChat experiential test suite now. | |

**Captured decision:** Phase 34 verification must be local and repeatable, with full WeChat experiential UAT deferred.

---

## the agent's Discretion

- Exact DTO names and Java service decomposition may follow existing `ExperienceRuntimeResponse` and `PublicExperienceServiceImpl` patterns.
- Planner may decide whether mini-program mapping lives in `gameService.ts` or additional service helpers, provided live runtime consumption and fallback behavior remain clear.

## Deferred Ideas

- Full mini-program story-mode map rendering and WeChat DevTools experiential UAT.
- AR/photo recognition, voice input, puzzle games, cannon defense, and route coverage gameplay.
- Full publish approval chain beyond runtime-safe lifecycle behavior.
