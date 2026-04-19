# Roadmap: Trip of Macau

## Milestones

- [x] **v1.0 Live Backend Cutover** - Shipped 2026-04-13. Archives: `.planning/milestones/v1.0-ROADMAP.md`, `.planning/milestones/v1.0-REQUIREMENTS.md`
- [x] **v2.0 Admin Control-Plane Reconstruction** - Archived 2026-04-15 with accepted gaps. Archives: `.planning/milestones/v2.0-ROADMAP.md`, `.planning/milestones/v2.0-REQUIREMENTS.md`
- [x] **v2.1 Interactive Rules Platform and AI Capability Center** - Archived 2026-04-19 with one accepted future slice for deferred mini-program experiential acceptance. Archives: `.planning/milestones/v2.1-ROADMAP.md`, `.planning/milestones/v2.1-REQUIREMENTS.md`
- [ ] **v3.0 Admin Core Domain Completion and Control-Plane Linkage** - Active milestone

## Active Milestone v3.0: Admin Core Domain Completion and Control-Plane Linkage

**Goal:** Complete the unfinished admin core domains around story/content, user/progress, testing/operations, and cross-domain linkage, then verify them on the live local admin/public stack.

**Scope note:** The mini-program WeChat DevTools and broader frontend experiential acceptance slice remains intentionally deferred beyond `v3.0`.

**Coverage:** 5 phases, 17 requirements, 17 mapped, 0 unmapped

| # | Phase | Goal | Requirements |
|---|-------|------|--------------|
| 28 | Story and Content Control-Plane Completion | Finish dedicated storyline, chapter, and content-block authoring so the narrative domain is no longer partial or placeholder-driven | `STORY-01`, `STORY-02`, `STORY-03`, `STORY-04` |
| 29 | User Progress and Journey Intelligence | Build the missing traveler profile, progress, recomputation, and timeline surfaces needed for real operations | `USER-01`, `USER-02`, `USER-03`, `USER-04` |
| 30 | Testing, Operations, and Lifecycle Control | Build operator-grade health, smoke, lifecycle, and dashboard tooling across the live control plane | `OPS-01`, `OPS-02`, `OPS-03`, `OPS-04` |
| 31 | Cross-domain Linkage and Runtime Contract Alignment | Link the completed domains to maps, POIs, indoor, rewards, media, AI, and public contracts so the system behaves canonically | `LINK-01`, `LINK-02`, `LINK-03` |
| 32 | Seeded Verification and Milestone Closure | Seed meaningful examples and close the milestone with live local verification and truthful acceptance boundaries | `VER-01`, `VER-02` |

## Phase Details

### Phase 28: Story and Content Control-Plane Completion

**Goal:** Finish dedicated storyline, chapter, and content-block authoring so the narrative domain is no longer partial or placeholder-driven.

**Requirements:** `STORY-01`, `STORY-02`, `STORY-03`, `STORY-04`

**Success criteria:**
1. Operators can author storylines against multiple maps, sub-maps, POIs, indoor entities, rewards, and interaction rules from dedicated admin surfaces.
2. Operators can compose ordered chapters with prerequisite logic, completion logic, and effects without falling back to raw placeholder pages or reused modules.
3. Reusable content blocks and asset-driven story sections can be previewed in assembled form before publish.
4. Story-domain persistence and admin screens align to the live backend model instead of ad hoc local forms.

**Depends on:** Archived milestones `v2.0` and `v2.1`

### Phase 29: User Progress and Journey Intelligence

**Goal:** Build the missing traveler profile, progress, recomputation, and timeline surfaces needed for real operations.

**Requirements:** `USER-01`, `USER-02`, `USER-03`, `USER-04`

**Success criteria:**
1. Operators can inspect traveler profiles, preferences, linked content scope, and recent activity from one coherent page set.
2. Operators can drill into progress by city, sub-map, POI, indoor map, story, chapter, task, collectible, and reward domain.
3. Operators can inspect interaction history, movement or route traces, and acquisition events as one traveler timeline.
4. Manual recompute or repair actions are explicit, safe, and audited.

**Depends on:** Phase 28

### Phase 30: Testing, Operations, and Lifecycle Control

**Goal:** Build operator-grade health, smoke, lifecycle, and dashboard tooling across the live control plane.

**Requirements:** `OPS-01`, `OPS-02`, `OPS-03`, `OPS-04`

**Success criteria:**
1. Operators can run key smoke and inspection actions from testing and operations surfaces without relying on legacy placeholder consoles.
2. Operators can schedule publish, unpublish, and remove actions with dependency awareness and impact visibility.
3. Operators can inspect operational dashboards for content, activity, media, AI, and failure state across the relevant domains.
4. Domain status lifecycles are consistent across maps, POIs, indoor, story, chapter, and reward-bearing entities.

**Depends on:** Phases 28 and 29

### Phase 31: Cross-domain Linkage and Runtime Contract Alignment

**Goal:** Link the completed domains to maps, POIs, indoor, rewards, media, AI, and public contracts so the system behaves canonically.

**Requirements:** `LINK-01`, `LINK-02`, `LINK-03`

**Success criteria:**
1. Shared selectors, bindings, and reference models exist across the completed admin domains instead of duplicated local-only selectors.
2. Admin-backend and public-backend contracts are aligned where story, lifecycle, or traveler-progress data needs runtime consumption.
3. Derived availability and progress update consistently when source content changes.

**Depends on:** Phases 28, 29, and 30

### Phase 32: Seeded Verification and Milestone Closure

**Goal:** Seed meaningful examples and close the milestone with live local verification and truthful acceptance boundaries.

**Requirements:** `VER-01`, `VER-02`

**Success criteria:**
1. The completed `v3.0` domains are exercised on the live local admin/public stack with milestone-grade evidence.
2. Seeded examples cover story content, traveler progress, and operations usage so the new control-plane surfaces do not land empty.
3. Milestone artifacts stay honest about the explicitly deferred mini-program acceptance slice and any bounded follow-on work.

**Depends on:** Phases 28, 29, 30, and 31

## Archived Milestones

<details>
<summary>[x] v2.1 Interactive Rules Platform and AI Capability Center (Phases 14-27, archived 2026-04-19)</summary>

- [x] Carryover control-plane closure
- [x] Indoor interaction-rule authoring, workbench, governance, and runtime closure
- [x] AI capability platform and provider/model orchestration
- [x] Reward-domain split and synchronization closure
- [x] Milestone traceability reconciliation with one accepted future slice

</details>

<details>
<summary>[x] v2.0 Admin Control-Plane Reconstruction (Phases 7-12 executed, archived 2026-04-15 with accepted gaps)</summary>

- [x] Traditional Chinese shell cleanup and real-auth baseline alignment
- [x] Multilingual authoring foundations
- [x] Spatial model rebuild, media library, story/activity expansion, and indoor authoring basics
- [ ] Planned Phase 13 never executed as its own phase and later fed into `v2.1`

</details>

<details>
<summary>[x] v1.0 Live Backend Cutover (Phases 1-6, shipped 2026-04-13)</summary>

- [x] Canonical backend foundation
- [x] Admin control-plane completion
- [x] Public read and write API cutover
- [x] COS media pipeline
- [x] Migration, cutover, and hardening

</details>

## Progress

| Milestone | Phase Range | Plans Complete | Status | Completed |
|-----------|-------------|----------------|--------|-----------|
| v1.0 Live Backend Cutover | 1-6 | 19/19 | Complete | 2026-04-13 |
| v2.0 Admin Control-Plane Reconstruction | 7-12 executed, 13 skipped | 15/15 executed plans | Archived with gaps | 2026-04-15 |
| v2.1 Interactive Rules Platform and AI Capability Center | 14-27 | 44/44 | Archived with accepted future slice | 2026-04-19 |
| v3.0 Admin Core Domain Completion and Control-Plane Linkage | 28-32 | 0/0 | Active | In progress |
