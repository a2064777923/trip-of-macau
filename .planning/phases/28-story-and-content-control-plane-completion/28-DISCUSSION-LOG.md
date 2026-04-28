# Phase 28: Story and Content Control-Plane Completion - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-28
**Phase:** 28 - Story and Content Control-Plane Completion
**Areas discussed:** Story experience model, reusable templates, dynamic exploration, admin UX, assets/media, seeded narrative, roadmap realignment

---

## Context Source

This discussion phase used the user's supplied replacement plan and detailed product narrative as the authoritative decision input. No new interactive questions were asked because the user had already locked the direction and invoked `$gsd-next` to advance the workflow.

---

## Story Experience Model

| Option | Description | Selected |
|--------|-------------|----------|
| Linear story/chapter/content-block CRUD | Treat storylines as ordered chapters with media blocks only | |
| Location default experience plus story-mode overrides | Let POI/spatial targets define default flows, then let story chapters inherit and override | ✓ |
| Separate unrelated systems per domain | Keep POI, indoor, story, rewards, and tasks as disconnected editors | |

**User's choice:** Location default experience plus story-mode overrides.
**Notes:** User explicitly described natural POI walk-in behavior and story chapters that reuse, disable, replace, or append parts of that behavior.

---

## Reusable Templates

| Option | Description | Selected |
|--------|-------------|----------|
| Raw JSON-first rules | Store flexible JSON and ask operators to edit it directly | |
| Structured reusable templates | Use operator-facing cards and templates for conditions, triggers, effects, tasks, games, and rewards | ✓ |
| Indoor-only behavior tooling | Keep interaction authoring confined to the indoor system | |

**User's choice:** Structured reusable templates.
**Notes:** Existing indoor rules should be reused or bridged, not discarded.

---

## Dynamic Exploration

| Option | Description | Selected |
|--------|-------------|----------|
| Fixed percentage grants | Each flow step writes direct progress increases like `+10%` | |
| Dynamic weighted exploration elements | Register published elements and calculate progress from completed weights divided by available weights | ✓ |
| Manual progress-only | Let operators adjust progress manually without derived calculation | |

**User's choice:** Dynamic weighted exploration elements.
**Notes:** User specifically said to avoid writing fixed increases and prefer semantic weights such as small/medium/large.

---

## Admin UX

| Option | Description | Selected |
|--------|-------------|----------|
| Table CRUD with JSON fallback | Basic tables and raw JSON editors | |
| Workbench layout | Left structure, middle visual arrangement, right properties, bottom validation/conflict hints | ✓ |
| Custom canvas-first editor only | Heavy bespoke visual editor before basic data model is stable | |

**User's choice:** Workbench layout.
**Notes:** Admin must be usable enough to configure the 媽閣廟 example without hand-writing JSON.

---

## Assets and Media

| Option | Description | Selected |
|--------|-------------|----------|
| Image/video only | Defer animation asset typing | |
| Lottie-first global animation assets | Treat Lottie JSON as first-class shared asset type with GIF/video fallback | ✓ |
| Build custom animation runtime now | Create a custom animation system beyond Lottie | |

**User's choice:** Lottie-first global animation assets.
**Notes:** WeChat official lottie-miniprogram remains the target baseline. First pass is JSON Lottie only.

---

## Seeded Narrative

| Option | Description | Selected |
|--------|-------------|----------|
| Minimal demo data | Only seed enough records to smoke test tables | |
| Complete flagship story package | Prepare "東西方文明的戰火與共生" with five chapters, interactions, rewards, titles, and assets | ✓ |
| No seed data | Leave story surfaces blank until production content exists | |

**User's choice:** Complete flagship story package, but as a later material-production phase after the foundation.
**Notes:** The foundation must model this full package even if image/audio/COS production happens later.

---

## Roadmap Realignment

| Option | Description | Selected |
|--------|-------------|----------|
| Keep old v3.0 Phase 28-32 unchanged | Continue the original roadmap despite the new design | |
| Replace story-experience phase chain | Reframe Phase 28-34 around foundation, POI workbench, story overrides, templates, exploration, content package, and runtime baseline | ✓ |
| Collapse all story work into one phase | Attempt to build the entire story runtime and asset package in Phase 28 | |

**User's choice:** Replace story-experience phase chain.
**Notes:** Context records this replacement so planning can update roadmap artifacts safely.

---

## Deferred Ideas

- Full mini-program experiential acceptance remains future work.
- AI-generated image/audio/Lottie production and COS upload for the entire flagship story package belongs to a later content-production phase.
- Complex gameplay implementations such as AR recognition, speech input, puzzle games, and cannon-defense interactions should be represented in templates first and implemented later.
