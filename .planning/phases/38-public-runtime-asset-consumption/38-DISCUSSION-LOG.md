# Phase 38: Public Runtime Asset Consumption - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md. This log preserves the alternatives considered.

**Date:** 2026-05-03
**Phase:** 38 - Public Runtime Asset Consumption
**Mode:** `/gsd-next` routed to discuss phase; Default mode fallback used pragmatic defaults based on prior locked decisions.
**Areas discussed:** Runtime DTO shape, asset filtering and fallback policy, event ingestion scope, lifecycle/privacy, verification depth.

---

## Runtime DTO Shape

| Option | Description | Selected |
| --- | --- | --- |
| Extend existing runtime DTOs | Keep `/api/v1/storylines/{id}/runtime` and existing DTO roots, add safe asset metadata. | ✓ |
| Add a parallel public asset-runtime API | New endpoint family dedicated to generated materials. | |
| Push asset resolution to mini-program | Return raw ids/config and let client decide fallbacks. | |

**Selected default:** Extend existing runtime DTOs.
**Notes:** This follows Phase 34. It avoids duplicate contracts before Phase 39 and keeps `packages/server` responsible for compiling traveler-safe runtime data.

---

## Asset Filtering and Fallback Policy

| Option | Description | Selected |
| --- | --- | --- |
| Server-side sanitation and fallback | Public backend filters unpublished/rejected/problem assets and returns fallback/placeholder DTOs. | ✓ |
| Client-side fallback only | Backend returns whatever is configured, mini-program handles unavailable assets. | |
| Hard fail missing media | Runtime errors if required assets are unavailable. | |

**Selected default:** Server-side sanitation and fallback.
**Notes:** Missing media should not white-screen or remove a whole story chapter. Runtime should degrade the specific block/step while preserving story flow.

---

## Event Ingestion Scope

| Option | Description | Selected |
| --- | --- | --- |
| Baseline story event set | Record media completion, content viewed, pickups, baseline tasks, rewards, chapter progression, unsupported viewed, and session exit. | ✓ |
| Full gameplay side effects | Implement complete pickup inventory, reward grants, puzzles, AR, speech, and route coverage now. | |
| No event expansion | Defer all new event types to mini-program phase. | |

**Selected default:** Baseline story event set.
**Notes:** Phase 38 should prepare the backend for Phase 39 without implementing complex gameplay engines. Events remain authenticated and idempotent.

---

## Lifecycle and Privacy

| Option | Description | Selected |
| --- | --- | --- |
| Traveler-safe public DTOs only | Return only published delivery metadata and stable debug-safe ids/codes. | ✓ |
| Expose QA/provenance for debugging | Include prompts, local paths, provider/model/cost, QA notes, and version internals in runtime. | |
| Rely on admin preview contract | Reuse admin preview payloads publicly. | |

**Selected default:** Traveler-safe public DTOs only.
**Notes:** Admin preview and public runtime have different trust boundaries. Public runtime must not leak secrets, local paths, prompts, or rejected-version details.

---

## Verification Depth

| Option | Description | Selected |
| --- | --- | --- |
| Backend compile plus Phase 38 smoke | Verify flagship story asset chain, lifecycle filtering, fallback, banned fields, event idempotency, exploration, and session exit. | ✓ |
| Full mini-program/device UAT | Include WeChat DevTools/device story journey acceptance now. | |
| Compile only | Defer smoke until Phase 39. | |

**Selected default:** Backend compile plus Phase 38 smoke.
**Notes:** Phase 39 owns full mini-program journey acceptance. Phase 38 must still prove runtime data and events are locally usable.

---

## the agent's Discretion

- Exact DTO field naming may follow current Java DTO patterns.
- The planner may choose whether material usage hints are joined from package/version rows or backfilled through existing relation links.
- Placeholder labels and smoke script organization are implementation choices, as long as the public contract stays safe and verifiable.

## Deferred Ideas

- Full mini-program route rendering, current chapter map highlighting, pickup/reward UI, and device UAT.
- Cost/history visibility and release readiness dashboard.
- Complex AR, speech, puzzle, route-coverage, and cannon-defense engines.
