# Milestones

## v2.1 Interactive Rules Platform and AI Capability Center (Archived: 2026-04-19)

**Phases executed:** 14 phases, 44 plans, 127 tasks

**Archive note:** This milestone closed with accepted carryover after Phase 27 reconciled the last traceability drift. The canonical archival-readiness decision is preserved in `.planning/milestones/v2.1-MILESTONE-AUDIT.md` and `.planning/milestones/v2.1-MILESTONE-CLOSEOUT.md`.

**Key accomplishments:**

- Closed the accepted `v2.0` control-plane gaps with fresh live verification and milestone-grade carryover proof.
- Built and verified the indoor interaction-rule platform, dedicated workbench, governance center, and public/runtime closure chain.
- Rebuilt the AI capability center into a provider/model orchestration workspace with creative workbench flows and TTS voice tooling.
- Closed reward-domain split and shared-rule synchronization with formal live verification instead of diagnosed-only evidence.
- Reconciled milestone-state drift so requirements, roadmap, audits, and retrospective all describe the same final `v2.1` truth.

**Accepted carryover into the next milestone:**

- `RULE-03`: the WeChat DevTools experiential acceptance slice for the mini-program indoor runtime.

---

## v2.0 寰屽彴绠＄悊绯荤当鐨勬敼閫茶垏瀹屽杽 (Archived: 2026-04-15)

**Phases executed:** 6 phases, 15 plans, 9 tasks

**Archive note:** This milestone was closed at user request with accepted gaps. No dedicated `.planning/v2.0-MILESTONE-AUDIT.md` artifact was completed before archival, and the planned Phase 13 closeout work was not executed as its own phase.

**Key accomplishments:**

- Traditional Chinese admin shell cleanup, shared mini-program branding reuse, and real-auth baseline alignment landed across admin/public/mini-program surfaces.
- Four-language authoring and translation-settings groundwork now exists for `zh-Hant`, `zh-Hans`, `en`, and `pt`.
- The spatial model was rebuilt around canonical cities, sub-maps, POIs, coordinate normalization, and richer popup / attachment authoring.
- The admin now has a real COS-backed media intake pipeline, policy-aware upload handling, and a reusable media library.
- Storylines, chapters, activities, and collection/reward authoring were expanded and aligned with real public contracts and smoke verification.
- Indoor building, floor, tile, and marker authoring basics now flow through admin, COS, public APIs, and the mini-program indoor runtime.

**Known gaps carried into `v2.1`:**

- Collection and reward authoring remain thinner than the intended final control-plane design, especially around richer bindings, examples, and operator-friendly trigger configuration.
- The planned user-progress / operations / system-control-plane closure from Phase 13 was not executed as a standalone phase.
- Requirement-by-requirement milestone audit and final closeout verification were not completed before archival.

---

## v1.0 Live Backend Cutover (Shipped: 2026-04-13)

**Phases completed:** 6 phases, 19 plans, 11 tasks

**Archive note:** This milestone was archived without a dedicated `.planning/v1.0-MILESTONE-AUDIT.md` artifact. Future milestones should run `/gsd-audit-milestone` before archival.

**Key accomplishments:**

- Canonical mini-program/admin/public ownership rules with mirrored status, locale, and asset-kind enums in both Java backends
- Canonical MySQL foundation tables, deterministic seed provenance, and a schema map that ties mini-program surfaces to live backend storage
- Public-backend boot cleanup, canonical MyBatis scaffolding for new domains, and a local-profile Spring context smoke test
- Aligned local public/admin backend profiles, repeatable start helpers, and a smoke harness that verifies real health endpoints against local datastores
- Canonicalized the existing admin map, story, chapter, POI, and reward stack against the live MySQL schema and repaired the local brownfield database so the APIs can run for real.
- Added the missing admin content/runtime CRUD so the admin backend can manage every remaining mini-program-facing content surface introduced in Phase 1.
- Replaced the placeholder admin UI surfaces with live canonical bindings and closed the loop with real backend smoke verification.
- Canonical public user-state persistence and write-side service logic now live in `packages/server`.
- The public user controller surface is live and backed by a repeatable local smoke script.
- Mini-program gameplay writes now target live public APIs instead of local-only mock mutations.
- The admin backend now owns the real Tencent COS media pipeline.
- The `/admin` asset console now uploads real files instead of faking asset records.
- Phase 5 now has a repeatable live smoke harness and closed-loop verification evidence.
- The former mini-program mock dataset now lives as a repeatable canonical MySQL seed instead of an app-only fallback.
- The admin system now reports real integration health and real traveler activity instead of stale placeholder state.
- The mini-program cutover is now proven end to end against the real backend stack.

---
