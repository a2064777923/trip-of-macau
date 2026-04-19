# Phase 20: Carryover Verification and Stability Closure - Context

**Gathered:** 2026-04-18
**Status:** Ready for planning
**Source:** Auto mode from the `v2.1` milestone audit, Phase 14 carryover UAT gaps, and current carryover code/runtime evidence

<domain>
## Phase Boundary

This phase does not reopen the carryover product scope.

It exists to close the remaining truth gap between what Phase 14 claimed to deliver and what the milestone audit can still prove today.

It covers:
- fresh regression-proof verification for collectible, badge, and reward authoring on the live admin stack
- fresh verification for traveler progress, operations or testing visibility, and carryover system settings
- deterministic rerun of the Phase 14 smoke path against current local services and seeded fixtures
- formal verification artifacts that let the milestone audit close `CARRY-01`, `CARRY-02`, and `CARRY-03`

It does not cover:
- new carryover features beyond what is required to make the existing Phase 14 scope stable and verifiable
- indoor-rule platform work
- AI platform verification work
- schema or UX redesigns that belong to a later expansion phase rather than this closure pass

</domain>

<decisions>
## Implementation Decisions

### Closure Standard
- **D-01:** Phase 20 must treat the old collection-authoring freeze as unresolved until it is either reproduced and fixed on current code or explicitly retired through fresh sustained live evidence. Route-render sanity alone is not enough.
- **D-02:** The closure bar is live-stack proof, not archive-time confidence. Admin `8081`, public `8080`, current MySQL fixtures, and the real carryover forms remain the authority.
- **D-03:** This phase must prefer regression-proofing and verifiable closure over adding more carryover authoring depth.

### Verification Scope
- **D-04:** Verification must cover all three carryover requirement areas together:
  - collection, badge, and reward authoring responsiveness plus save durability
  - traveler progress, exploration recomputation visibility, and related operator views
  - carryover system settings ownership and persistence
- **D-05:** Collection verification must exercise longer operator sessions, not only first paint: tab switching, repeated open/edit/save cycles, media picker interaction, indoor binding updates, and navigation between collectible, reward, and badge surfaces.
- **D-06:** Public runtime alignment for carryover content should be rechecked through current public payloads and existing smoke coverage, not through mock-only assumptions.

### Artifact Strategy
- **D-07:** Phase 20 should not leave closure evidence only inside its own summary. It must backfill the missing Phase 14 verification chain so the milestone audit can resolve the original gap directly.
- **D-08:** The preferred artifact shape is:
  - refresh or supplement the old Phase 14 UAT evidence where useful
  - create a formal `14-VERIFICATION.md`
  - create any Phase 20 smoke, UAT, and summary artifacts needed to show how the gap was closed
- **D-09:** Verification text must replace stale or garbled descriptions with readable, operator-meaningful wording where those artifacts are touched.

### Harness And Fixtures
- **D-10:** Reuse and extend the existing Phase 14 fixture and smoke path instead of inventing a second closure harness.
- **D-11:** `scripts/local/smoke-phase-14-carryover.ps1` remains the canonical machine-checkable carryover smoke path and may be tightened if it currently misses the regression or stale verification gaps.
- **D-12:** Existing seeded carryover showcase entities and user fixtures should stay deterministic so repeated local reruns produce comparable evidence.

### Scope Discipline
- **D-13:** Fixes are allowed where they are necessary to satisfy `CARRY-01`, `CARRY-02`, or `CARRY-03`, but the phase should not broaden forms, schemas, or workflows beyond closure needs.
- **D-14:** Browser automation or scripted verification is preferred where it meaningfully reduces ambiguity, but manual proof may still be used for operator-facing stability checks that need real interaction time.

### The Agent's Discretion
- Exact structure of the refreshed verification matrix and whether the freshest evidence lives mainly in Phase 14 artifacts, Phase 20 artifacts, or both
- Exact browser automation tooling or smoke harness layering, as long as the resulting evidence is repeatable and easy to audit
- Whether any non-blocking UI warnings discovered during closure are fixed now or recorded as residual tech debt, provided they do not block carryover acceptance

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Gap source of truth
- `.planning/ROADMAP.md` - Phase 20 goal, requirements, gap-closure wording, and success criteria
- `.planning/REQUIREMENTS.md` - `CARRY-01`, `CARRY-02`, and `CARRY-03` traceability now routed to Phase 20
- `.planning/v2.1-MILESTONE-AUDIT.md` - milestone-close evidence showing why carryover remains open
- `.planning/PROJECT.md` - milestone-level constraints, live-stack expectations, and verification posture

### Original carryover phase evidence
- `.planning/phases/14-carryover-control-plane-closure/14-CONTEXT.md` - original Phase 14 decisions and scope boundary
- `.planning/phases/14-carryover-control-plane-closure/14-UAT.md` - unresolved carryover regression trail and pending checkpoints
- `.planning/phases/14-carryover-control-plane-closure/14-01-SUMMARY.md` - collection, badge, and reward closure claims that now need verification
- `.planning/phases/14-carryover-control-plane-closure/14-02-SUMMARY.md` - traveler progress and system settings closure claims
- `.planning/phases/14-carryover-control-plane-closure/14-03-SUMMARY.md` - smoke and runtime alignment claims

### Carryover admin surfaces
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/CollectionAuthoringShared.tsx` - shared collection authoring logic implicated in the prior rerender regression
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/CollectibleManagement.tsx` - collectible authoring page
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/BadgeManagement.tsx` - badge authoring page
- `packages/admin/aoxiaoyou-admin-ui/src/pages/Collectibles/RewardManagement.tsx` - reward authoring page
- `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPickerField.tsx` - media picker behavior previously implicated in heavy rerender churn
- `packages/admin/aoxiaoyou-admin-ui/src/pages/UserManagement/index.tsx` - traveler progress and detail views
- `packages/admin/aoxiaoyou-admin-ui/src/pages/OperationsManagement/index.tsx` - operations and testing visibility surface
- `packages/admin/aoxiaoyou-admin-ui/src/pages/SystemManagement/index.tsx` - carryover settings ownership surface

### Carryover backend and runtime surfaces
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminCollectibleController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminSystemManagementController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminCollectibleServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminUserServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminSystemManagementServiceImpl.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/CollectibleController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/BadgeController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/RewardController.java`
- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PublicCatalogServiceImpl.java`

### Verification harness and fixtures
- `scripts/local/smoke-phase-14-carryover.ps1` - canonical carryover smoke script
- `scripts/local/mysql/init/19-phase-14-collection-carryover.sql` - carryover collection schema and data alignment seed
- `scripts/local/mysql/init/20-phase-14-collection-showcase-seed.sql` - carryover showcase content seed
- `scripts/local/mysql/init/21-phase-14-progress-and-settings.sql` - traveler progress and settings seed
- `scripts/local/mysql/init/22-phase-14-carryover-verification-seed.sql` - verification-specific carryover fixture seed

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `CollectionAuthoringShared.tsx`: already centralizes the collection binding, preset, and media sections, so one regression fix or verification hook can cover collectible, reward, and badge forms consistently.
- `MediaAssetPickerField.tsx`: already scopes selection through `Form.useWatch(name, form)` and can be used as the focal point for validating whether the prior full-form subscription issue is actually gone.
- `smoke-phase-14-carryover.ps1`: already seeds fixtures, logs into admin `8081`, verifies carryover settings, checks user progress detail, verifies admin and public carryover entities, and runs a mini-program build sanity check.

### Established Patterns
- Carryover verification uses PowerShell smoke scripts plus deterministic MySQL seed files rather than ad hoc shell notes.
- Admin pages are large Ant Design form surfaces where state-subscription scope directly affects responsiveness and perceived stability.
- Verification quality in this repo is stronger when the same truth is proven three ways: admin readback, public payload readback, and repeatable local smoke.

### Integration Points
- Phase 20 will need to touch both the current Phase 20 directory and the original Phase 14 artifact chain so milestone audit traceability closes cleanly.
- Collection, reward, and badge stability checks need to stay aligned with current public catalog payloads, because `CARRY-01` is not purely an admin-only requirement.
- Traveler progress and carryover settings closure depend on the live admin backend on `8081`, seeded local MySQL data, and current public/admin DTO compatibility.

</code_context>

<specifics>
## Specific Ideas

- Treat the prior user-reported freeze as a long-session regression, not merely a page-load regression.
- Prefer a verification run that explicitly records:
  - which forms were opened
  - which edits were saved
  - whether cross-tab switching stayed responsive
  - whether the same edited entities still appeared correctly through public payloads
- If the old UAT wording or encoded issue text is unreadable, repair it where the refreshed verification artifacts are written so the evidence chain is human-auditable.

</specifics>

<deferred>
## Deferred Ideas

- Any additional collection authoring richness beyond closure of the original Phase 14 requirement set belongs to a future milestone.
- Indoor rule acceptance closure belongs to Phase 21.
- AI provider-default and creative-workbench verification belongs to Phase 22.

</deferred>

---

*Phase: 20-carryover-verification-and-stability-closure*
*Context gathered: 2026-04-18 in auto mode*
