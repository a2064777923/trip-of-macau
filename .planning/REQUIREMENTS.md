# Requirements: Trip of Macau

**Defined:** 2026-04-12
**Core Value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## v1 Requirements

### Data Model

- [ ] **DATA-01**: MySQL stores the canonical mini-program content model, including cities, POIs, storylines, chapters, tips, rewards, stamps, notifications, runtime settings, media references, and user progress records.
- [ ] **DATA-02**: Existing mock data from the mini-program is transformed into repeatable seed or migration scripts and loaded into MySQL with deterministic identifiers.
- [ ] **DATA-03**: Public and admin services use consistent publish-state, locale, sort-order, and asset-reference semantics for shared domains.

### Public APIs

- [ ] **PUB-01**: The mini-program can fetch home/discover/runtime configuration data from `packages/server` instead of local mock state.
- [ ] **PUB-02**: The mini-program can fetch map, city, POI, storyline, tips, rewards, stamps, and notifications data from `packages/server` instead of local mock state.
- [ ] **PUB-03**: The mini-program can read and write user progress, check-ins, unlocks, preferences, emergency-contact data, and reward redemption through `packages/server`.
- [ ] **PUB-04**: Public APIs expose only published/admin-approved content and honor configured sort/filter/pagination rules.

### Admin Control Plane

- [ ] **ADM-01**: Admin users can create, edit, and publish all mini-program-facing content entities and runtime configuration records required by the live app.
- [ ] **ADM-02**: Admin users can manage multilingual copy, publish state, asset references, sort order, and runtime rules consumed by the mini-program.
- [ ] **ADM-03**: Admin users can inspect seeded content state and integration health for the live mini-program backend.

### Media and Assets

- [ ] **MED-01**: Admin uploads store images/files in Tencent COS through backend APIs with automatic object-key generation and persisted asset metadata.
- [ ] **MED-02**: Public and admin APIs can resolve stored media metadata into canonical client-usable URLs.

### Operations and Verification

- [ ] **OPS-01**: The local environment can boot the public backend, admin backend, MySQL, MongoDB, and required supporting services with documented environment variables.
- [ ] **OPS-02**: Scripted smoke checks or integration tests prove admin writes are visible through public APIs and consumable by the mini-program.
- [ ] **OPS-03**: The live backend includes baseline health, logging, validation, and performance safeguards needed for high availability and expansion.

## v2 Requirements

### Advanced AI and Operations

- **AIOP-01**: AI scenario execution and provider orchestration move from admin visibility/configuration into production-grade runtime execution paths.
- **AIOP-02**: Operators receive richer analytics dashboards and automated alerting for content freshness, integration failures, and traveler behavior.

### Platform Expansion

- **PLAT-01**: Support additional public clients beyond the WeChat mini-program without duplicating domain logic.
- **PLAT-02**: Introduce more advanced caching, background jobs, and rollout controls for higher traffic volumes.

## Out of Scope

| Feature | Reason |
|---------|--------|
| Direct manual COS object management from frontend clients | Upload security and naming rules must stay in backend-controlled flows |
| New payment, checkout, or unrelated commerce flows | Not required to replace mocks and fully connect the current mini-program |
| Hardcoded mock-only traveler experience as the long-term runtime path | Conflicts with the core value of a live admin-driven backend |
| Committing cloud secrets into source or planning docs | Violates basic operational security and maintainability |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| DATA-01 | Phase 1 | Pending |
| DATA-02 | Phase 6 | Pending |
| DATA-03 | Phase 1 | Pending |
| PUB-01 | Phase 3 | Pending |
| PUB-02 | Phase 3 | Pending |
| PUB-03 | Phase 4 | Pending |
| PUB-04 | Phase 3 | Pending |
| ADM-01 | Phase 2 | Pending |
| ADM-02 | Phase 2 | Pending |
| ADM-03 | Phase 6 | Pending |
| MED-01 | Phase 5 | Pending |
| MED-02 | Phase 5 | Pending |
| OPS-01 | Phase 1 | Pending |
| OPS-02 | Phase 6 | Pending |
| OPS-03 | Phase 6 | Pending |

**Coverage:**
- v1 requirements: 15 total
- Mapped to phases: 15
- Unmapped: 0

---
*Requirements defined: 2026-04-12*
*Last updated: 2026-04-12 after initial definition*
