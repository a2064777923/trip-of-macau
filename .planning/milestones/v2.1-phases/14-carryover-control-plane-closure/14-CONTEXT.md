# Phase 14: Carryover Control Plane Closure - Context

**Gathered:** 2026-04-15
**Status:** Ready for planning
**Source:** Archived `v2.0` carryover gaps, roadmap scope, and recorded user feedback

<domain>
## Phase Boundary

This phase closes the accepted `v2.0` carryover gaps instead of introducing a new product pillar.

It covers:
- collectible / badge / reward authoring depth
- traveler progress and exploration visibility
- operations / testing page cleanup against live runtime domains
- system settings closure for translation, upload policy, and map / indoor defaults
- explicit smoke and UAT closure for the archived `v2.0` surfaces

It does not cover:
- the full indoor interaction-rules platform
- AI capability center provider orchestration
- unrelated stack migrations or redesigns outside the carryover scope
</domain>

<decisions>
## Implementation Decisions

### Collection And Reward Closure
- Collectibles, badges, and rewards must support canonical bindings to city, sub-map, storyline, and indoor map contexts instead of stopping at the current thinner relation set.
- Admin forms must offer preset-driven popup / trigger / display configuration. Raw JSON remains an advanced fallback, not the primary operator path.
- Operators should see useful showcase content immediately after seeding instead of only empty forms and blank selectors.
- Public reward / collectible reads and mini-program readers must stay aligned to the richer relation graph introduced here.

### Progress And Operations Closure
- User management must expose richer traveler profiles, collection progress, exploration progress, and recent interaction history.
- Exploration progress must be recomputed from the managed content graph rather than treated as a fixed or stale counter.
- Operations / testing pages must align to live runtime domains and stop showing reused placeholder consoles or wrong ownership surfaces.

### System Settings Closure
- System settings own translation defaults, translation engine order, upload policy defaults, and map / indoor zoom defaults.
- Settings changes must flow through explicit admin endpoints and persisted config keys, not hidden constants only.

### Verification Closure
- Carryover closure must end with repeatable local proof on admin `8081` and public `8080`.
- Smoke coverage must verify both admin write / read surfaces and public / runtime alignment.
- Phase output should leave a concrete UAT path instead of archive-time assumptions.

### The Agent's Discretion
- Exact schema shape for preset fields and relation-link storage
- Whether indoor bindings attach at building level, floor level, or both for each collection type
- Exact admin page layout and DTO naming so long as the carryover truths are preserved
</decisions>

<canonical_refs>
## Canonical References

**Downstream planners and executors should read these first.**

### Archived carryover evidence
- `.planning/milestones/v2.0-phases/11-story-activity-and-collection-composition/11-UAT.md` - explicit user-reported collection / reward carryover gap
- `.planning/milestones/v2.0-phases/11-story-activity-and-collection-composition/11-03-SUMMARY.md` - current collection / reward baseline
- `.planning/milestones/v2.0-phases/12-indoor-map-authoring-basics/12-03-SUMMARY.md` - indoor runtime baseline to align indoor bindings against
- `.planning/milestones/v2.0-ROADMAP.md` - archived milestone scope and accepted-gap note

### Current admin surfaces
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/CollectibleManagement.tsx` - current collectible authoring
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/BadgeManagement.tsx` - current badge authoring
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/RewardManagement.tsx` - current reward authoring
- `packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx` - current traveler management view
- `packages/admin/aoxiaoyou-admin-ui/src/pages/OperationsManagement/index.tsx` - current operations / activity surface
- `packages/admin/aoxiaoyou-admin-ui/src/pages/SystemManagement/index.tsx` - current system settings surface

### Current admin backend surfaces
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminCollectibleController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminUserController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminOperationsController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminSystemManagementController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminCollectibleServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminOperationsServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminSystemManagementServiceImpl.java`

### Current public and mini-program surfaces
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/RewardController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/CollectibleController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/BadgeController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/UserController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/UserServiceImpl.java`
- `packages/client/src/pages/rewards/index.tsx`
- `packages/client/src/pages/profile/index.tsx`
- `packages/client/src/services/api.ts`
- `packages/client/src/services/gameService.ts`
</canonical_refs>

<specifics>
## Specific Ideas

- Seed at least one showcase collectible / badge / reward chain that binds into map, sub-map, and indoor contexts so operators can inspect the final UX immediately.
- Add preset templates for popup, display, and trigger config with a visible "custom JSON" escape hatch instead of JSON-only editing.
- Make Phase 14 verification prove that admin writes affect both admin readback and public responses on the local stack.
</specifics>

<deferred>
## Deferred Ideas

- Full indoor interaction rule authoring and runtime evaluation belongs to Phases 15-16.
- AI provider orchestration, quotas, and overview belong to Phase 17.
- Autonomous content crawling without operator review remains out of scope for this phase.
</deferred>

---

*Phase: 14-carryover-control-plane-closure*
*Context gathered: 2026-04-15 from archived `v2.0` carryover evidence*
