---
phase: 28-story-and-content-control-plane-completion
status: foundation-handoff
created: 2026-04-28
source:
  - 28-CONTEXT.md
  - 28-RESEARCH.md
  - 28-VALIDATION.md
  - 28-01-SUMMARY.md
  - 28-02-SUMMARY.md
  - 28-03-SUMMARY.md
---

# Phase 28 Handoff: Experience Orchestration Foundation

Phase 28 is the shared foundation for the user-approved v3.0 redesign: default location experiences, storyline-mode overrides, reusable templates, Lottie-aware media/content blocks, and dynamic exploration primitives. It is not the old linear storyline/chapter CRUD phase.

## Delivered Foundation

- Backend schema and APIs now cover experience templates, flows, flow steps, bindings, overrides, exploration elements, user exploration events, and compiled public runtime DTOs.
- Admin UI exposes protected Traditional Chinese foundation workbench routes for templates, flows, bindings, overrides, exploration rules, governance, content blocks, and media resources.
- Seed data and smoke verification prove the A-Ma Temple default POI flow, first story-chapter override, Lottie/media story content, public runtime, duplicate `client_event_id`, and dynamic exploration progress.
- Phase 28 keeps later POI, story-mode, template-governance, user-progress, material-production, and mini-program runtime work as explicit follow-on scope.

## Decision Audit

| Decision | Phase 28 foundation artifact | Next owner |
| --- | --- | --- |
| D-01 POI or spatial targets define default experience flows | `experience_flows`, `experience_bindings`, public `GET /api/v1/experience/poi/{poiId}`, seeded `poi_ama_default_walk_in` | Phase 29 |
| D-02 Story chapters bind anchors and inherit target experience | `story_chapters.experience_flow_id`, `override_policy_json`, `experience_overrides`, public storyline runtime compiler | Phase 30 |
| D-03 Storyline mode is orchestrated, not just content pages | `story_mode_config_json`, `StoryModeConfig`, compiled storyline runtime | Phase 30 and Phase 34 |
| D-04 Overrides support inherit, disable, replace, append | Canonical override modes in SQL, backend validation, admin workbench options, seeded disable override | Phase 30 |
| D-05 Interaction behavior is reusable across domains | `experience_templates` and admin template tab with canonical template types | Phase 31 |
| D-06 Indoor node behavior should be bridged, not discarded | Foundation owner vocabulary includes `indoor_building`, `indoor_floor`, and `indoor_node`; no replacement of indoor rules | Phase 31 |
| D-07 Advanced JSON is fallback, not primary authoring | Admin workbench exposes presets, cards, and validation rather than JSON-only editing | Phase 29 through Phase 31 |
| D-08 Persisted JSON must be schema-versioned and validated | Backend validation requires versioned object JSON for core config payloads | Phase 29 through Phase 34 |
| D-09 Exploration avoids fixed percentage grants | `exploration_elements.weight_level` and semantic weights `tiny/small/medium/large/core` | Phase 32 |
| D-10 Progress is completed weight divided by published available weight | Public exploration response calculates available/completed weights from published elements | Phase 32 |
| D-11 Content lifecycle changes can change percentages without deleting events | Immutable `user_exploration_events` plus published-element denominator model | Phase 32 |
| D-12 Storyline session progress is separate from permanent exploration | Public session endpoints exist separately from permanent event ingestion | Phase 34 |
| D-13 Workbenches use three-panel orchestration layout | `ExperienceOrchestrationWorkbench.tsx` establishes the foundation layout | Phase 29 through Phase 31 |
| D-14 Admin UI copy stays Traditional Chinese | Phase 28-owned routes and labels use Traditional Chinese | All later phases |
| D-15 Invalid forms scroll/focus/shake | Shared validation feedback is used by Phase 28 admin surfaces | All later admin phases |
| D-16 A-Ma Temple example must be configurable without hand-writing JSON | Seeded templates and A-Ma preset flow cover intro, routing, proximity media, tasks, pickups, hidden achievement, rewards/titles | Phase 29 and Phase 30 |
| D-17 Lottie is preferred animation type with fallback compatibility | `asset_kind='lottie'`, media picker/preview support, public media DTO animation fields | Phase 33 and Phase 34 |
| D-18 First Lottie pass supports JSON only | SQL and admin/media scope intentionally support Lottie JSON, not `.lottie` packages or frame sequences | Future if needed |
| D-19 Lottie assets are globally reusable | Shared content assets and media center expose Lottie as a first-class reusable asset | Phase 33 |
| D-20 Generated assets need manifest traceability | Seed rows include local/COS/provenance-style fields; full manifest production deferred | Phase 33 |
| D-21 Flagship story is "東西方文明的戰火與共生" | Seeded storyline and first-chapter foundation use the approved flagship story | Phase 33 |
| D-22 Preserve historical basis versus literary dramatization | Content model and seed scope keep authored story fields distinct; full evidence pack deferred | Phase 33 |
| D-23 Chapters express media, interactions, pickups, challenges, rewards, titles, anchors | Foundation schema and runtime DTOs can represent these through flows, steps, blocks, reward links, and exploration elements | Phase 30 and Phase 33 |
| D-24 Full asset generation package is later, but must be representable | Foundation model supports media, Lottie, audio/video, relation links, and material provenance | Phase 33 |
| D-25 Replace the next story-experience phase chain | Roadmap and requirements now name the approved Phase 29-34 sequence | Phase 29 through Phase 34 |
| D-26 Reconcile stale roadmap wording | This handoff, ROADMAP.md, and REQUIREMENTS.md replace the stale story-only framing | Completed by 28-04 |

## Follow-On Boundaries

| Phase | Boundary |
| --- | --- |
| Phase 29 | POI default experience workbench |
| Phase 30 | Storyline mode and chapter override workbench |
| Phase 31 | Interaction/task template library and governance center |
| Phase 32 | Dynamic exploration and user progress model |
| Phase 33 | Complete flagship story content/material package |
| Phase 34 | Public runtime and mini-program consumption baseline |

## Requirement Coverage

| Requirement | Phase 28 coverage | Remaining owner |
| --- | --- | --- |
| STORY-01 | Canonical bindings and foundation admin workbench exist for story/location experience relationships. | Phase 29 and Phase 30 deepen operator-specific authoring. |
| STORY-02 | Chapter anchors, inherited flows, override policies, completion/effect payloads, and runtime compiler foundation exist. | Phase 30 completes story-mode and chapter override authoring. |
| STORY-03 | Reusable content blocks, Lottie-aware media assets, and assembled chapter preview foundation exist. | Phase 33 completes the flagship content/material package. |
| STORY-04 | Phase 28-owned story/experience/media/content routes are no longer placeholders. | Phase 29 through Phase 31 split specialized workbenches. |
| LINK-01 | Shared relation, owner, binding, template, and media vocabulary is established across admin domains. | Phase 31 aggregates governance across existing indoor/reward systems. |
| LINK-02 | Admin and public runtime contracts align for POI default flows, story runtime, events, sessions, and exploration summaries. | Phase 34 completes mini-program consumption baseline. |

## Non-Claims

- Phase 28 does not complete the full POI-specific operator workbench.
- Phase 28 does not complete the full story route editor or chapter override workbench.
- Phase 28 does not generate and upload the complete five-chapter image/audio/Lottie asset package.
- Phase 28 does not complete the mini-program story gameplay runtime; it provides the backend/public DTO baseline for later consumption.
