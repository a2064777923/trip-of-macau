# Milestones

## v3.1 Material Production and Mini-program Experience Acceptance (Shipped: 2026-05-04)

**Phases completed:** 5 phases, 18 plans, 30+ tracked tasks

**Key accomplishments:**

- Generated/imported the `東西方文明的戰火與共生` flagship material package into real images, board slices, narration/audio, videos, COS-backed `content_assets`, and package item versions.
- Added guarded material production APIs, explicit promotion/rollback states, version history, and smoke evidence for image/audio/video/COS chains.
- Built Traditional Chinese material QA and reuse controls over the existing media library and picker, including package/item/version metadata search and consistency checks.
- Extended public runtime APIs so generated image/audio/video/Lottie assets, poster/fallback states, unsupported-media hints, and sanitized usage metadata are traveler-safe.
- Added idempotent public story event/session ingestion for media completion, pickup/action cards, baseline tasks, rewards, and story exit.
- Updated the mini-program story-mode baseline to consume live public runtime data, render generated content/media, hand route context to the map, and degrade unsupported gameplay in Traditional Chinese.
- Added a Phase 40 release-readiness smoke bundle, AI `監控與成本` observability, scoped AI history visibility, acceptance report, and WeChat DevTools/device UAT checklist.

**Accepted caveats:**

- Manual WeChat DevTools/device UAT remains pending in `40-UAT.md`; this milestone archives with that caveat accepted by the user.
- Full AR/photo recognition, speech-input NPC interactions, route-coverage games, puzzle/cannon-defense minigames, and production indoor visual positioning remain future gameplay scope.
- The mini-program story page still has an advisory bundle-size warning.
- Live provider/COS mutation checks stay opt-in because they incur external cost and mutate persistent assets.

---

## v3.0 Admin Core Domain Completion and Control-Plane Linkage (Shipped: 2026-04-30)

**Phases completed:** 8 phases, 33 plans, 54 tasks

**Key accomplishments:**

- Versioned admin experience orchestration plus published-only public runtime DTOs for POI defaults, story overrides, and dynamic exploration events
- Admin experience orchestration workbench with Traditional Chinese routes, A-Ma Temple presets, shared media components, and chapter assembled preview
- Protected POI default experience facade that atomically loads, saves, validates, and templates walk-in flows on top of the canonical experience model
- Traditional Chinese POI 地點體驗工作台 with timeline presets, structured cards, template saving, route wiring, and validation feedback
- A-Ma Temple default POI experience seed plus live admin/public smoke proving template reuse and published runtime alignment
- Admin-only storyline mode facade over shared story chapter and experience-flow records
- Mini-program-facing storyline runtime with route strategy, inherited flows, overrides, and compiled steps
- Traditional Chinese three-panel storyline mode workbench for route strategy, chapter anchors, and override authoring
- UTF-8 seeded first story-mode slice with live admin/public smoke coverage
- Canonical weighted progress parity for public and admin services using published exploration elements, immutable completion events, and retired-element comparison data
- Durable public story-session rows in MySQL with persistent start/event/exit lifecycle fields and immutable exploration-event separation
- Preview-first admin progress repair engine with scoped recompute, immutable event annotation repairs, and dual audit persistence
- Traditional Chinese traveler progress workbench with seeded fixtures and live public/admin smoke coverage
- Authenticated admin traveler workbench and MySQL-backed timeline aggregation with dynamic progress summaries, legacy compatibility snapshots, and explicit route-trace unavailability
- Admin-only traveler progress operations transport with typed preview confirmation, scoped repair DTOs, and paginated audit mapping over the Phase 32-03 core service
- Story material package registry with admin-only CRUD, provenance item tracking, and compile-verified Spring/MyBatis integration
- Traceable five-chapter material manifest with 54 planned assets, COS keys, provenance docs, and deterministic MySQL seed rows
- Five-chapter `東西方文明的戰火與共生` story package seeded across story, content block, experience, reward, and dynamic exploration tables
- Public storyline runtime now exposes versioned traveler-safe metadata, lifecycle-filtered chapters, and unsupported gameplay hints for mini-program consumption.
- Mini-program story runtime contracts now map public compiled flows into story state with live/fallback status and auth-gated event helpers.
- The mini-program story page now fetches public runtime data, renders compiled interaction flow cards, and degrades unsupported gameplay/media safely.
- Phase 34 now has a repeatable public runtime smoke that proves the flagship story runtime, content blocks, compiled steps, and mini-program build baseline.

**Accepted future scope:**

- Full WeChat DevTools/device experiential acceptance and complex mini-program gameplay interactions.
- Real material production from the Phase 33 manifest, including `image-2` generation, slicing, audio/video assembly, COS upload, and asset status promotion.
- Full approval workflow beyond direct lifecycle controls.

---

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
