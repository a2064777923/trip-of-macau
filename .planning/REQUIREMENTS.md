# Requirements: Trip of Macau

**Defined:** 2026-04-15
**Core Value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## v2.1 Requirements

### Carryover Control Plane Closure

- [x] **CARRY-01**: Operator can create and edit collectibles, badges, and rewards with complete map / sub-map / indoor-map bindings, richer example content, and easier preset-driven popup / trigger / display configuration.
- [x] **CARRY-02**: Operator can inspect traveler progress, exploration recomputation, operations/testing surfaces, and system settings that remained incomplete or ambiguous at the end of `v2.0`.
- [x] **CARRY-03**: The archived `v2.0` domains can be re-verified through repeatable smoke and UAT coverage so accepted gaps are closed explicitly instead of by assumption.

### Indoor Runtime Rules

- [x] **RULE-01**: Operator can define structured appearance conditions, trigger conditions, trigger chains, and effects for indoor overlays and markers.
- [x] **RULE-02**: Operator can configure animated path-based appearance / effect behaviors for indoor overlays and markers with persisted geometry and runtime-safe metadata.
- [ ] **RULE-03**: The public backend and mini-program indoor runtime can evaluate the configured indoor trigger / effect rules safely and predictably against live authored data, or the deferred mini-program DevTools slice must be formally carried out of `v2.1` with no ambiguous partial state left in milestone closure artifacts.
- [x] **RULE-04**: Complex indoor behavior editing must move into a dedicated validated workbench with explicit apply/save semantics, operator-defined behavior naming, and map-assisted point/path tooling.
- [x] **RULE-05**: Operators must have a cross-entity rule governance surface to filter rule-bearing objects, inspect overlaps or conflicts, follow trigger chains, and enable or disable rules centrally.

### AI Capability Platform

- [x] **AI-01**: Operator can manage an AI capability center with per-capability provider configuration across mainstream providers and custom endpoints.
- [x] **AI-02**: Operator can configure provider fallback, manual switching, quotas, suspicious-concurrency throttling, and usage governance without exposing API secrets.
- [x] **AI-03**: Operator can inspect cross-capability AI usage, health, and status from a central overview.

### AI Capability Platform Redesign

- [ ] **AI-04**: The admin shell must expose AI Capability Center as a top-level control-plane workspace with dedicated sub-pages for overview, provider registry, model inventory, capability routing, observability, creative tooling, and settings rather than one overloaded CRUD page.
- [ ] **AI-05**: Operators can onboard OpenAI, Bailian, Hunyuan, MiniMax, Volcengine, and custom providers through provider templates, encrypted credential storage, base URL overrides, connectivity tests, and provider-specific model or endpoint inventory sync.
- [ ] **AI-06**: Operators can configure primary models, fallback models, scenario presets, and advanced parameters for each AI capability through structured forms first, with expert JSON available only as an optional advanced mode.
- [ ] **AI-07**: Operators can inspect provider health, sync status, usage, latency, and estimated cost at provider, model, and capability levels using normalized backend metrics rather than raw request logs alone.
- [ ] **AI-08**: Content editors can launch an AI creative workbench from authoring forms, generate candidate copy or media, keep COS-backed history, restore earlier versions, and finalize selected outputs back into the target asset field.

### Reward Domain Split and Acquisition Presentation

- [ ] **REWARD-01**: The control plane must split `兌換獎勵物品管理` from `遊戲內獎勵配置`, so redeemable prizes and in-game rewards stop sharing one overloaded reward model and one overloaded admin page.
- [ ] **REWARD-02**: Operators can define redeemable-prize conditions through grouped logic (`全部滿足`, `任意滿足`, `至少滿足 N 項`) across stamps, exploration progress, unlocked scenes, owned fragments, interaction history, and similar gameplay state, together with fulfillment modes such as offline pickup, mail delivery, and virtual issuance.
- [ ] **REWARD-03**: Operators can manage in-game rewards through one canonical model that covers badges, titles, city currencies, city fragments, unlock passes, voice packs, and comparable reward assets, including map/sub-map/indoor bindings and reward-specific inventory or stacking rules where applicable.
- [ ] **REWARD-04**: Reward-trigger rules must be shared references between reward management and interaction-behavior authoring, so adding, editing, or deleting an interaction-driven reward condition remains synchronized across both surfaces instead of duplicating JSON.
- [ ] **REWARD-05**: Reward acquisition must support configurable presentation flows including popup cards, full-screen animation, full-screen video, SFX, voice-over, playback priority, skip policy, and first-time-only behavior, with contracts that can be consumed by the public backend and mini-program runtime.

## Out of Scope

| Feature | Reason |
|---------|--------|
| Reopening `v2.0` as the active milestone | `v2.0` has already been archived; accepted gaps are now explicit carryover scope inside `v2.1` |
| Replacing the current Taro / Spring / MySQL brownfield stack | The milestone is about platform completion, not stack migration |
| Shipping every downstream AI end-user feature page together with the capability center | `v2.1` focuses on provider/governance infrastructure and overview first |
| Fully autonomous external content filling without operator review | The admin remains the authoritative control plane and must keep human-reviewed authoring |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| CARRY-01 | Phase 20 | Complete |
| CARRY-02 | Phase 20 | Complete |
| CARRY-03 | Phase 20 | Complete |
| RULE-01 | Phase 21 | Complete |
| RULE-02 | Phase 21 | Complete |
| RULE-04 | Phase 21 | Complete |
| RULE-05 | Phase 21 | Complete |
| RULE-03 | Phase 27 | Pending |
| AI-01 | Phase 18 | Complete |
| AI-02 | Phase 18 | Complete |
| AI-03 | Phase 18 | Complete |
| AI-04 | Phase 26 | Pending |
| AI-05 | Phase 26 | Pending |
| AI-06 | Phase 26 | Pending |
| AI-07 | Phase 26 | Pending |
| AI-08 | Phase 26 | Pending |
| REWARD-01 | Phase 25 | Pending |
| REWARD-02 | Phase 25 | Pending |
| REWARD-03 | Phase 25 | Pending |
| REWARD-04 | Phase 25 | Pending |
| REWARD-05 | Phase 25 | Pending |

**Coverage:**
- v2.1 requirements: 21 total
- Mapped to phases: 21
- Unmapped: 0

---
*Requirements defined: 2026-04-15*
*Last updated: 2026-04-19 after Phase 21 execution and verification backfill*
