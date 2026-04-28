# Requirements: Trip of Macau

**Defined:** 2026-04-19
**Core Value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

## v3.0 Requirements

### Story and Content Orchestration

- [x] **STORY-01**: Operators can create and edit storylines that bind to one or more maps, sub-maps, POIs, indoor entities, rewards, and interaction rules with schedule and unlock metadata.
- [x] **STORY-02**: Operators can compose chapters against POI, task, marker, or overlay anchors with ordered progression, prerequisite conditions, completion conditions, and completion effects.
- [x] **STORY-03**: Operators can manage reusable story content blocks made of text, images, audio, video, and attached assets, then preview the assembled narrative before publishing.
- [x] **STORY-04**: Story and chapter management no longer routes to placeholder or reused pages; it exposes dedicated admin-owned surfaces aligned to the live persistence model.

### User and Progress Intelligence

- [ ] **USER-01**: Operators can inspect a traveler profile with identity basics, locale and interface preferences, linked cities and maps, and recent activity context.
- [ ] **USER-02**: Operators can inspect exploration and completion progress by city, sub-map, POI, indoor map, storyline, chapter, task, collectible, and reward domain with drill-down detail.
- [ ] **USER-03**: Operators can inspect traveler interaction logs, movement or route traces, acquisition history, and major state changes through one coherent timeline.
- [ ] **USER-04**: Operators can trigger safe recomputation or repair actions for derived traveler progress and keep an audit trail of those manual operations.

### Testing, Operations, and Lifecycle Control

- [ ] **OPS-01**: Operators can manage test and operations surfaces for runtime health, key smoke actions, and recent failure visibility without relying on placeholder consoles.
- [ ] **OPS-02**: Operators can schedule publish, unpublish, and remove actions for content-bearing entities with dependency awareness and impact preview.
- [ ] **OPS-03**: Operators can inspect operational dashboards for content inventory, traveler activity, media and AI pipeline health, and domain-level exceptions.
- [ ] **OPS-04**: Operators can review domain status lifecycles consistently across maps, sub-maps, POIs, indoor entities, stories, chapters, and reward-bearing content.

### Cross-domain Linkage and Runtime Alignment

- [x] **LINK-01**: Story, user, operations, map, POI, indoor, reward, media, and AI domains share canonical bindings and selectors instead of duplicated local-only models.
- [x] **LINK-02**: The admin backend and public backend expose aligned contracts for story, progress, and lifecycle data where live runtime consumption depends on those domains.
- [ ] **LINK-03**: Derived availability and traveler progress update predictably when source content changes, without hidden manual patching or stale counters.

### Verification and Seeded Acceptance

- [ ] **VER-01**: `v3.0` domains are verified end to end on the live local admin/public stack with milestone-grade evidence, excluding the explicitly deferred mini-program frontend acceptance slice.
- [ ] **VER-02**: The completed `v3.0` domains ship with meaningful seeded examples so operators can validate real story, user-progress, and operations flows without blank states.

## Future Requirements

- The deferred mini-program WeChat DevTools indoor-runtime acceptance and broader frontend experiential linkage work remain future milestone scope.
- Any publish-approval workflow beyond simple lifecycle controls remains future scope unless it becomes necessary to close a `v3.0` dependency.
- Deep end-user AI feature-page planning remains future scope beyond the existing control-plane AI capability center.

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
| STORY-01 | Phase 28 | Planned |
| STORY-02 | Phase 28 | Planned |
| STORY-03 | Phase 28 | Planned |
| STORY-04 | Phase 28 | Planned |
| USER-01 | Phase 29 | Planned |
| USER-02 | Phase 29 | Planned |
| USER-03 | Phase 29 | Planned |
| USER-04 | Phase 29 | Planned |
| OPS-01 | Phase 30 | Planned |
| OPS-02 | Phase 30 | Planned |
| OPS-03 | Phase 30 | Planned |
| OPS-04 | Phase 30 | Planned |
| LINK-01 | Phase 31 | Planned |
| LINK-02 | Phase 31 | Planned |
| LINK-03 | Phase 31 | Planned |
| VER-01 | Phase 32 | Planned |
| VER-02 | Phase 32 | Planned |

**Coverage:**
- v3.0 requirements: 17 total
- Mapped to phases: 17
- Unmapped: 0

---
*Requirements defined: 2026-04-19*
*Last updated: 2026-04-19 after creating the v3.0 roadmap*
