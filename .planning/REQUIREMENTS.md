# Requirements: Trip of Macau

**Defined:** 2026-04-19
**Core Value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## v3.0 Requirements

### Story and Content Orchestration

- [x] **STORY-01**: Operators have a canonical foundation for storylines and location experiences that bind maps, sub-maps, POIs, indoor entities, rewards, and interaction rules through shared selectors, relation links, and default experience flows. Phase 29 completes the dedicated POI default experience workbench.
- [x] **STORY-02**: Operators have a canonical foundation for chapters that bind POI, task, marker, overlay, indoor, or manual anchors and inherit, disable, replace, or append target experience flows through structured versioned payloads. Phase 30 completes the storyline mode and chapter override workbench.
- [x] **STORY-03**: Operators can manage reusable story content blocks and Lottie-aware media assets as the shared content substrate; Phase 33 completes the full flagship story content/material package and exposes it through the admin `故事素材包` page.
- [ ] **STORY-04**: Phase 28-owned story, experience, content-block, and media surfaces no longer route to placeholders or reused pages; Phase 29 through Phase 31 split the specialized workbenches.

### User and Progress Intelligence

- [x] **USER-01**: Operators can inspect a traveler profile with identity basics, locale and interface preferences, linked cities and maps, and recent activity context.
- [x] **USER-02**: Operators can inspect exploration and completion progress by city, sub-map, POI, indoor map, storyline, chapter, task, collectible, and reward domain with drill-down detail.
- [x] **USER-03**: Operators can inspect traveler interaction logs, movement or route traces, acquisition history, and major state changes through one coherent timeline.
- [x] **USER-04**: Operators can trigger safe recomputation or repair actions for derived traveler progress and keep an audit trail of those manual operations.

### Testing, Operations, and Lifecycle Control

- [ ] **OPS-01**: Operators can manage test and operations surfaces for runtime health, key smoke actions, and recent failure visibility without relying on placeholder consoles.
- [ ] **OPS-02**: Operators can schedule publish, unpublish, and remove actions for content-bearing entities with dependency awareness and impact preview. Phase 34 verified only the public-runtime lifecycle/status subset and documented the broader publish scheduling gap.
- [ ] **OPS-03**: Operators can inspect operational dashboards for content inventory, traveler activity, media and AI pipeline health, and domain-level exceptions.
- [ ] **OPS-04**: Operators can review domain status lifecycles consistently across maps, sub-maps, POIs, indoor entities, stories, chapters, and reward-bearing content. Phase 34 verified published traveler-eligible runtime filtering; broader cross-domain lifecycle operations remain incomplete.

### Cross-domain Linkage and Runtime Alignment

- [ ] **LINK-01**: Story, user, operations, map, POI, indoor, reward, media, and AI domains share canonical bindings and selectors instead of duplicated local-only models.
- [x] **LINK-02**: The admin backend and public backend expose aligned contracts for story, progress, and lifecycle data where live runtime consumption depends on those domains. Phase 34 completed the public runtime contract and mini-program consumption baseline for the flagship story.
- [x] **LINK-03**: Derived availability and traveler progress update predictably when source content changes, without hidden manual patching or stale counters.

### Verification and Seeded Acceptance

- [x] **VER-01**: `v3.0` domains are verified end to end on the live local admin/public stack with milestone-grade evidence, excluding the explicitly deferred mini-program frontend acceptance slice. Phase 34 evidence includes backend compile, WeApp build, and public runtime smoke; stateful smoke is available when a traveler token is provided.
- [x] **VER-02**: The completed `v3.0` domains ship with meaningful seeded examples so operators can validate real story, user-progress, and operations flows without blank states.

## Future Requirements

- The deferred mini-program WeChat DevTools indoor-runtime acceptance and broader frontend experiential linkage work remain future milestone scope.
- Any publish-approval workflow beyond simple lifecycle controls remains future scope unless it becomes necessary to close a `v3.0` dependency.
- Deep end-user AI feature-page planning remains future scope beyond the existing control-plane AI capability center.
- `.lottie` packages, sprite sheets, sequence-frame animation systems, and complex AR/speech/puzzle gameplay implementations remain future scope unless a later phase explicitly pulls them in.

## Out of Scope

| Feature | Reason |
|---------|--------|
| Reopening archived `v1.0`, `v2.0`, or `v2.1` milestones as active work | Those milestones are already archived and only their explicit follow-on scope should move forward |
| Replacing the current Taro / Spring / MySQL brownfield stack | `v3.0` is about domain completion and linkage, not stack migration |
| Treating mock data as the live source of truth | The live admin/public stack already owns canonical runtime data |
| The deferred mini-program WeChat DevTools and broader frontend experiential acceptance work | The user explicitly chose to postpone that slice beyond `v3.0` |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| STORY-01 | Phase 28 foundation; Phase 29 completion | Complete |
| STORY-02 | Phase 28 foundation; Phase 30 completion | Complete |
| STORY-03 | Phase 28 foundation; Phase 33 completion | Complete |
| STORY-04 | Phase 28 foundation; Phase 29-30 workbenches; Phase 31 specialized governance | In progress |
| USER-01 | Phase 32 | Complete |
| USER-02 | Phase 32 | Complete |
| USER-03 | Phase 32 | Complete |
| USER-04 | Phase 32 | Complete |
| OPS-01 | Phase 31 | Planned |
| OPS-02 | Phase 34 | Partial - runtime lifecycle/status subset verified; broader publish scheduling remains future gap |
| OPS-03 | Phase 31 | Planned |
| OPS-04 | Phase 34 | Partial - public runtime published filtering verified; broader lifecycle workbench consistency remains future gap |
| LINK-01 | Phase 28 foundation; Phase 29 POI integration; Phase 31 governance | In progress |
| LINK-02 | Phase 28 foundation; Phase 30 story runtime contract; Phase 34 runtime baseline | Complete |
| LINK-03 | Phase 32 | Complete |
| VER-01 | Phase 34 | Complete |
| VER-02 | Phase 33 | Complete |

**Coverage:**
- v3.0 requirements: 17 total
- Mapped to phases: 17
- Unmapped: 0

---
*Requirements defined: 2026-04-19*
*Last updated: 2026-04-30 after completing Phase 34 public runtime and mini-program consumption baseline*
