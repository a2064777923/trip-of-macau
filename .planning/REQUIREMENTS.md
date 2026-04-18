# Requirements: Trip of Macau

**Defined:** 2026-04-15
**Core Value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## v2.1 Requirements

### Carryover Control Plane Closure

- [ ] **CARRY-01**: Operator can create and edit collectibles, badges, and rewards with complete map / sub-map / indoor-map bindings, richer example content, and easier preset-driven popup / trigger / display configuration.
- [ ] **CARRY-02**: Operator can inspect traveler progress, exploration recomputation, operations/testing surfaces, and system settings that remained incomplete or ambiguous at the end of `v2.0`.
- [ ] **CARRY-03**: The archived `v2.0` domains can be re-verified through repeatable smoke and UAT coverage so accepted gaps are closed explicitly instead of by assumption.

### Indoor Runtime Rules

- [ ] **RULE-01**: Operator can define structured appearance conditions, trigger conditions, trigger chains, and effects for indoor overlays and markers.
- [ ] **RULE-02**: Operator can configure animated path-based appearance / effect behaviors for indoor overlays and markers with persisted geometry and runtime-safe metadata.
- [ ] **RULE-03**: The public backend and mini-program indoor runtime can evaluate the configured indoor trigger / effect rules safely and predictably against live authored data.
- [ ] **RULE-04**: Complex indoor behavior editing must move into a dedicated validated workbench with explicit apply/save semantics, operator-defined behavior naming, and map-assisted point/path tooling.
- [ ] **RULE-05**: Operators must have a cross-entity rule governance surface to filter rule-bearing objects, inspect overlaps or conflicts, follow trigger chains, and enable or disable rules centrally.

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
| CARRY-01 | Phase 20 | Pending |
| CARRY-02 | Phase 20 | Pending |
| CARRY-03 | Phase 20 | Pending |
| RULE-01 | Phase 21 | Pending |
| RULE-02 | Phase 21 | Pending |
| RULE-04 | Phase 21 | Pending |
| RULE-05 | Phase 21 | Pending |
| RULE-03 | Phase 21 | Pending |
| AI-01 | Phase 18 | Complete |
| AI-02 | Phase 18 | Complete |
| AI-03 | Phase 18 | Complete |
| AI-04 | Phase 22 | Pending |
| AI-05 | Phase 22 | Pending |
| AI-06 | Phase 22 | Pending |
| AI-07 | Phase 22 | Pending |
| AI-08 | Phase 22 | Pending |

**Coverage:**
- v2.1 requirements: 16 total
- Mapped to phases: 16
- Unmapped: 0

---
*Requirements defined: 2026-04-15*
*Last updated: 2026-04-18 after v2.1 milestone audit gap closure planning*
