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

- [ ] **AI-01**: Operator can manage an AI capability center with per-capability provider configuration across mainstream providers and custom endpoints.
- [ ] **AI-02**: Operator can configure provider fallback, manual switching, quotas, suspicious-concurrency throttling, and usage governance without exposing API secrets.
- [ ] **AI-03**: Operator can inspect cross-capability AI usage, health, and status from a central overview.

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
| CARRY-01 | Phase 14 | Pending |
| CARRY-02 | Phase 14 | Pending |
| CARRY-03 | Phase 14 | Pending |
| RULE-01 | Phase 15 | Pending |
| RULE-02 | Phase 15 | Pending |
| RULE-04 | Phase 16 | Pending |
| RULE-05 | Phase 16 | Pending |
| RULE-03 | Phase 17 | Pending |
| AI-01 | Phase 18 | Pending |
| AI-02 | Phase 18 | Pending |
| AI-03 | Phase 18 | Pending |

**Coverage:**
- v2.1 requirements: 11 total
- Mapped to phases: 11
- Unmapped: 0

---
*Requirements defined: 2026-04-15*
*Last updated: 2026-04-16 after inserting the dedicated rule-workbench/governance phase into v2.1*
