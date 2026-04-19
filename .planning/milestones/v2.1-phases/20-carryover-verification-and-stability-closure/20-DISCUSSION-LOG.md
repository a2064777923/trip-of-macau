# Phase 20: Carryover Verification and Stability Closure - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in `20-CONTEXT.md`; this log preserves the alternatives considered during auto-mode discussion.

**Date:** 2026-04-18
**Phase:** 20-carryover-verification-and-stability-closure
**Areas discussed:** Closure standard, verification scope, artifact strategy, harness reuse, scope discipline

---

## Closure Standard

| Option | Description | Selected |
|--------|-------------|----------|
| Route-render sanity only | Treat collectible, reward, and badge page render checks as enough to retire the old freeze report. | |
| Fresh live regression proof | Re-verify responsive editing and save behavior on the live stack before closing the gap. | Yes |
| Broaden feature scope first | Add more carryover features now, then close verification later. | |

**User's choice:** `[auto] Fresh live regression proof`
**Notes:** Recommended because the milestone audit still marks `CARRY-01` unsatisfied and explicitly calls out the missing proof that long-session carryover authoring is stable.

---

## Verification Scope

| Option | Description | Selected |
|--------|-------------|----------|
| Collection forms only | Verify collectible, reward, and badge forms, but leave traveler progress and settings to earlier evidence. | |
| Full carryover closure set | Re-verify collection authoring, traveler progress, and carryover settings together on the current live stack. | Yes |
| Smoke only | Rely on the existing smoke script without targeted operator-facing retesting. | |

**User's choice:** `[auto] Full carryover closure set`
**Notes:** Recommended because Phase 20 owns `CARRY-01`, `CARRY-02`, and `CARRY-03`, and the stale gaps span more than the collection forms alone.

---

## Artifact Strategy

| Option | Description | Selected |
|--------|-------------|----------|
| Phase 20 summary only | Record closure in new Phase 20 summaries without backfilling original Phase 14 verification. | |
| Backfill the original chain | Create fresh closure evidence and give Phase 14 a real `14-VERIFICATION.md` so the audit gap resolves cleanly. | Yes |
| Replace old artifacts entirely | Discard prior Phase 14 evidence and keep only new gap-phase artifacts. | |

**User's choice:** `[auto] Backfill the original chain`
**Notes:** Recommended because the roadmap success criteria explicitly require that Phase 14 receives a formal verification artifact.

---

## Harness Reuse

| Option | Description | Selected |
|--------|-------------|----------|
| New closure harness | Build an entirely new smoke and fixture path for Phase 20. | |
| Reuse and tighten Phase 14 harness | Keep the existing seed and smoke path, and extend it only where it misses the current closure bar. | Yes |
| Manual-only proof | Skip machine-checkable closure and rely on screenshots or manual notes. | |

**User's choice:** `[auto] Reuse and tighten Phase 14 harness`
**Notes:** Recommended because the existing `smoke-phase-14-carryover.ps1` already exercises the right carryover domains and is the fastest way to get repeatable closure evidence.

---

## Scope Discipline

| Option | Description | Selected |
|--------|-------------|----------|
| Expand carryover authoring | Use the gap phase to add more authoring depth and redesigns. | |
| Closure-first | Fix only what is necessary to satisfy `CARRY-01` to `CARRY-03` and prove the existing scope honestly. | Yes |
| Defer fixes | Leave remaining instability as milestone tech debt and close with a note. | |

**User's choice:** `[auto] Closure-first`
**Notes:** Recommended because this is a gap-closure phase, not a new product phase.

---

## The Agent's Discretion

- Exact structure of the refreshed verification matrix and how it is split between Phase 14 and Phase 20 artifacts
- Exact automation/manual balance for the carryover stability proof
- Whether non-blocking warnings encountered during carryover closure are fixed now or left as explicit residual debt

## Deferred Ideas

- Further collection/reward feature expansion beyond the original Phase 14 requirement set
- Indoor rule acceptance closure and WeChat DevTools runtime proof
- AI provider-default and creative-workbench verification
