# Roadmap: Trip of Macau

## Milestones

- [x] **v1.0 Live Backend Cutover** - Shipped 2026-04-13. Archives: `.planning/milestones/v1.0-ROADMAP.md`, `.planning/milestones/v1.0-REQUIREMENTS.md`
- [x] **v2.0 Admin Control-Plane Reconstruction** - Archived 2026-04-15 with accepted gaps. Archives: `.planning/milestones/v2.0-ROADMAP.md`, `.planning/milestones/v2.0-REQUIREMENTS.md`
- [x] **v2.1 Interactive Rules Platform and AI Capability Center** - Archived 2026-04-19 with one accepted future slice for deferred mini-program experiential acceptance. Archives: `.planning/milestones/v2.1-ROADMAP.md`, `.planning/milestones/v2.1-REQUIREMENTS.md`
- [x] **v3.0 Admin Core Domain Completion and Control-Plane Linkage** - Shipped 2026-04-30. Archives: `.planning/milestones/v3.0-ROADMAP.md`, `.planning/milestones/v3.0-REQUIREMENTS.md`, `.planning/milestones/v3.0-MILESTONE-AUDIT.md`
- [ ] **v3.1 Material Production and Mini-program Experience Acceptance** - Active milestone

## Active Milestone v3.1: Material Production and Mini-program Experience Acceptance

**Goal:** Produce and govern real flagship story media assets, then validate the mini-program story-mode runtime against those assets and the live public backend.

**Scope note:** v3.1 promotes the Phase 33 planned material manifest into real assets where feasible and verifies the traveler-facing flagship story experience. Full AR recognition, speech-input gameplay, and complex puzzle engines remain future scope unless explicitly pulled in later.

**Coverage:** 5 phases, 22 requirements, 22 mapped, 0 unmapped

| # | Phase | Goal | Requirements |
|---|-------|------|--------------|
| 36 | Material Production Pipeline and Asset Promotion | Generate/import real story images, material boards, audio, and simple motion videos, then upload and promote assets from planned to generated/uploaded/approved/published. | `MAT-01`, `MAT-02`, `MAT-03`, `MAT-04`, `MAT-05` |
| 37 | Material QA Workspace and Reuse Controls | Build the admin workspace for QA, status transitions, replacement/regeneration, manifest-vs-asset consistency checks, and media-picker reuse. | `QA-01`, `QA-02`, `QA-03`, `QA-04` |
| 38 | Public Runtime Asset Consumption | Extend public runtime DTOs and events so generated assets, fallbacks, media completion, pickups, tasks, rewards, and lifecycle filtering are traveler-safe and smoke-verified. | `RUN-01`, `RUN-02`, `RUN-03`, `RUN-04` |
| 39 | Mini-program Story Mode Experience | Complete the mini-program flagship story path: route rendering, current chapter highlighting, media playback, event reporting, pickups, rewards, and graceful unsupported gameplay states. | `MP-01`, `MP-02`, `MP-03`, `MP-04`, `MP-05` |
| 40 | Acceptance, Cost Visibility, and Release Readiness | Add repeatable local smoke, WeChat DevTools/device UAT checklist, generation cost/history visibility, and final evidence of implemented vs deferred gameplay. | `ACC-01`, `ACC-02`, `ACC-03`, `ACC-04` |

## Phase Details

### Phase 36: Material Production Pipeline and Asset Promotion

**Goal:** Generate or import real flagship story assets from the Phase 33 manifest, upload them through the existing backend/COS pipeline, and promote material records through explicit statuses.

**Requirements:** `MAT-01`, `MAT-02`, `MAT-03`, `MAT-04`, `MAT-05`

**Plans:** 5 plans

Plans:
- [x] `36-01-PLAN.md` — Backend lineage schema, package-scoped production APIs, guarded promotion/rollback, and tests.
- [x] `36-02-PLAN.md` — Local UTF-8-safe still-image/audio production tooling, reward-cue gating, and board slicing.
- [x] `36-03-PLAN.md` — Package-scoped production/import/version actions inside the existing story material package page.
- [x] `36-04-PLAN.md` — Smoke verification, provider/COS/ffmpeg preflight, and truthful evidence/state updates.
- [x] `36-05-PLAN.md` — Dedicated ffmpeg-gated MAT-04 chapter video job definitions and import builder.

**Success criteria:**
1. Operators can run explicit, logged generation/import flows for images, material boards, audio, and simple still-image motion videos without committing secrets.
2. Generated files are saved locally under the material package, uploaded through backend asset APIs to COS, and linked back to `content_assets` and `story_material_package_items`.
3. Material-board slicing can turn one generated sheet into multiple child assets while preserving crop metadata and parent provenance.
4. Material statuses can move from `planned` to `generated`, `uploaded`, `approved`, or `published`, with rollback to earlier versions.
5. No Chinese content or prompt/script text is written through unsafe inline PowerShell literals.

**Depends on:** v3.0 Phase 33 material package and Phase 18-24 AI/COS infrastructure

### Phase 37: Material QA Workspace and Reuse Controls

**Goal:** Give operators a Traditional Chinese material production workspace to inspect, approve, replace, regenerate, and reuse generated assets safely.

**Requirements:** `QA-01`, `QA-02`, `QA-03`, `QA-04`

**Success criteria:**
1. The admin can list package materials by status, kind, chapter, usage target, provider, and runtime exposure.
2. Operators can preview images, child crops, Lottie, audio, and videos with metadata and provenance.
3. Reject/regenerate/replace/approve actions do not break existing content block, reward, pickup, POI, story, or runtime bindings.
4. Consistency checks compare manifest entries, local files, COS objects, `content_assets`, and package item rows.
5. Approved assets can be selected through existing media picker components in downstream editors.

**Depends on:** Phase 36

### Phase 38: Public Runtime Asset Consumption

**Goal:** Align public runtime APIs with generated assets and baseline story events while keeping admin-only provenance and provider data private.

**Requirements:** `RUN-01`, `RUN-02`, `RUN-03`, `RUN-04`

**Success criteria:**
1. Story runtime DTOs include generated/published asset URLs, posters, fallback assets, Lottie metadata, audio/video metadata, and usage hints.
2. Unavailable or unpublished assets degrade to fallback assets or clear unsupported-media placeholders.
3. Public event endpoints record media completion, pickup interaction, baseline task completion, reward acquisition, and story session exit with idempotency.
4. Public runtime smoke verifies the flagship story asset chain and lifecycle filtering after asset promotion.

**Depends on:** Phases 36 and 37

### Phase 39: Mini-program Story Mode Experience

**Goal:** Make the mini-program flagship story journey consumable and verifiable against the live public runtime rather than mocks.

**Requirements:** `MP-01`, `MP-02`, `MP-03`, `MP-04`, `MP-05`

**Success criteria:**
1. The mini-program story page renders runtime introduction, chapter list, content blocks, images, Lottie, audio, video, and fallback states from the public backend.
2. Story mode map draws the route, highlights the current chapter, grays inactive route segments, and shows current destination details.
3. The client reports story events with stable idempotency keys and handles anonymous/authenticated gating correctly.
4. Baseline interactive objects can be displayed, tapped, and reflected in pickups/rewards/progress through public APIs.
5. Complex AR/speech/puzzle gameplay placeholders are explicit, Traditional Chinese, and do not block story progression.

**Depends on:** Phase 38

### Phase 40: Acceptance, Cost Visibility, and Release Readiness

**Goal:** Close v3.1 with repeatable verification, generation cost visibility, WeChat DevTools/device UAT, and clear implemented/deferred evidence.

**Requirements:** `ACC-01`, `ACC-02`, `ACC-03`, `ACC-04`

**Success criteria:**
1. A local smoke verifies material generation metadata, COS upload availability, asset promotion, public runtime consumption, and mini-program build compatibility.
2. A WeChat DevTools/device UAT checklist covers route display, chapter progression, media playback, pickups, rewards, exit/restart behavior, and fallback states.
3. Admin can inspect generation job history, provider/model usage, cost estimates, and errors without exposing API keys.
4. Final verification distinguishes fully implemented behavior, manually accepted behavior, and explicitly deferred complex gameplay.

**Depends on:** Phases 36, 37, 38, and 39

## Archived Milestones

<details>
<summary>[x] v3.0 Admin Core Domain Completion and Control-Plane Linkage (Phases 28-35, shipped 2026-04-30)</summary>

- [x] Story experience orchestration, POI default flow, storyline mode, chapter overrides, and interaction/task governance.
- [x] Dynamic exploration and traveler progress with repair/audit operations.
- [x] Flagship story package and public mini-program consumption baseline.
- [x] Dependency-aware lifecycle scheduling and audit history.

</details>

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
| v3.0 Admin Core Domain Completion and Control-Plane Linkage | 28-35 | 33/33 | Shipped | 2026-04-30 |
| v3.1 Material Production and Mini-program Experience Acceptance | 36-40 | 11/11 currently planned plans complete; Phase 38 complete | Active | - |
