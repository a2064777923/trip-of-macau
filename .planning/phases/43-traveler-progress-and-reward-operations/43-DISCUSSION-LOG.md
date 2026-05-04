# Phase 43: Traveler Progress and Reward Operations - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-05-04T16:25:00+08:00  
**Phase:** 43-traveler-progress-and-reward-operations  
**Mode:** gsd-next routed discussion, non-interactive default decisions  
**Areas discussed:** Operator workflow, support actions, rule trace, consistency and safety, verification

---

## Operator Workflow

| Option | Description | Selected |
| --- | --- | --- |
| Single support workbench | One traveler support page with search, detail, sessions, timeline, rewards, rule trace, and repair panels. | ✓ |
| Separate CRUD pages | Keep sessions, rewards, events, and audits in separate pages. | |
| Minimal backend-only ops | Add endpoints only and defer admin UI. | |

**Selected default:** Single support workbench.
**Notes:** This aligns with OPS-01 and the user's repeated preference for usable management-system workflows instead of shell pages or scattered CRUD.

---

## Support Actions

| Option | Description | Selected |
| --- | --- | --- |
| Preview-first audited actions | Preview impact, require explicit confirmation/reason, then write an audit trail. | ✓ |
| Direct mutation buttons | Let operators mutate immediately from rows. | |
| Read-only diagnostics | Only inspect state, no repair tools. | |

**Selected default:** Preview-first audited actions.
**Notes:** Existing `AdminTravelerProgressOpsController` already has preview/confirm patterns, so this should be extended rather than replaced.

---

## Duplicate Events

| Option | Description | Selected |
| --- | --- | --- |
| Mark or compensate | Mark duplicate/ignored/voided or create auditable compensating state without physical deletion. | ✓ |
| Hard delete | Delete duplicate event rows from the database. | |
| Ignore completely | Leave duplicates visible but offer no support action. | |

**Selected default:** Mark or compensate.
**Notes:** Physical deletion conflicts with support traceability and idempotent event evidence.

---

## Reward Resend

| Option | Description | Selected |
| --- | --- | --- |
| Idempotent trace-linked resend | Link resend to rule/event/session and prevent duplicate grants by default. | ✓ |
| Blind corrective grant | Create a new reward grant regardless of prior state. | |
| Defer resend | Only show rule trace and leave grants manual. | |

**Selected default:** Idempotent trace-linked resend.
**Notes:** This keeps admin repairs consistent with Phase 42 duplicate-as-success behavior.

---

## Rule Trace

| Option | Description | Selected |
| --- | --- | --- |
| Runtime-to-rule trace | Show event -> experience step/exploration element -> reward binding/rule -> condition result -> grant result. | ✓ |
| Raw JSON debug only | Show payloads and make operators infer the rule path. | |
| Rule authoring rewrite | Rebuild the rule engine and authoring model. | |

**Selected default:** Runtime-to-rule trace.
**Notes:** OPS-04 requires an explanation of why a reward/title was or was not received; raw JSON alone does not meet that bar.

---

## Verification

| Option | Description | Selected |
| --- | --- | --- |
| Local admin/public smoke | Compile/build relevant surfaces and smoke admin workbench plus public state consistency locally. | ✓ |
| Manual-only check | Depend on browser/manual checks without repeatable commands. | |
| Claim device UAT | Treat automated smoke as WeChat device evidence. | |

**Selected default:** Local admin/public smoke.
**Notes:** Physical-device UAT remains pending unless actually run.

---

## Deferred Ideas

- Phase 44 final IA polish and release acceptance report.
- Production-grade AR/speech/puzzle/cannon-defense/route-coverage engines.
- Full approval workflow for support actions.
