# Phase 42: Traveler Gameplay Event Engine - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.  
> Decisions are captured in `42-CONTEXT.md`; this log preserves the alternatives considered.

**Date:** 2026-05-04  
**Phase:** 42-Traveler Gameplay Event Engine  
**Mode:** `gsd-next` routed context capture using existing roadmap and Phase 41 evidence

---

## Areas Analyzed

| Area | Alternatives Considered | Selected |
| --- | --- | --- |
| Gameplay scope | Full advanced gameplay engine vs baseline playable interpreter vs mock-only cards | Baseline playable interpreter |
| State source | Backend-confirmed public runtime/session/event APIs vs local-only progression ledger | Backend-confirmed APIs |
| Auth/session gate | Allow anonymous stateful actions vs read-only anonymous browsing plus auth/session gate | Read-only anonymous browsing plus auth/session gate |
| Unsupported templates | Hide unsupported steps vs crash/disable silently vs visible pending-feature cards | Visible pending-feature cards |
| Duplicate events | Treat duplicate as failure vs treat duplicate as already synced | Already synced |
| Verification | Build-only smoke vs runtime/event/session smoke plus build | Runtime/event/session smoke plus build |

## Captured Decisions

- Phase 42 should make the mini-program story runtime playable from compiled steps without implementing the future AR/speech/puzzle engines.
- Stateful events require authenticated story sessions; read-only story browsing stays available to anonymous users.
- Event ids must remain stable and idempotent so repeat taps do not grant duplicate rewards.
- Traveler-facing copy must be Traditional Chinese and distinguish accepted, duplicate, failed, blocked, and unsupported states.
- Backend state remains authoritative for session, permanent exploration events, and reward/progress durability.

## Deferred Ideas

- Operator progress/reward repair workflows belong to Phase 43.
- Final release IA/UAT consolidation belongs to Phase 44.
- Real advanced gameplay engines remain future scope beyond v3.2 Phase 42.
