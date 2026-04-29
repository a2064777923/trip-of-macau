# Roadmap: Trip of Macau

## Milestones

- [x] **v1.0 Live Backend Cutover** - Shipped 2026-04-13. Archives: `.planning/milestones/v1.0-ROADMAP.md`, `.planning/milestones/v1.0-REQUIREMENTS.md`
- [x] **v2.0 Admin Control-Plane Reconstruction** - Archived 2026-04-15 with accepted gaps. Archives: `.planning/milestones/v2.0-ROADMAP.md`, `.planning/milestones/v2.0-REQUIREMENTS.md`
- [x] **v2.1 Interactive Rules Platform and AI Capability Center** - Archived 2026-04-19 with one accepted future slice for deferred mini-program experiential acceptance. Archives: `.planning/milestones/v2.1-ROADMAP.md`, `.planning/milestones/v2.1-REQUIREMENTS.md`
- [ ] **v3.0 Admin Core Domain Completion and Control-Plane Linkage** - Active milestone

## Active Milestone v3.0: Admin Core Domain Completion and Control-Plane Linkage

**Goal:** Complete the unfinished admin core domains around story/content, user/progress, testing/operations, and cross-domain linkage, starting with the user-approved story experience orchestration foundation and then extending it through POI workbenches, story-mode overrides, reusable interaction governance, dynamic exploration, seeded flagship content, and public runtime alignment.

**Scope note:** The mini-program WeChat DevTools and broader frontend experiential acceptance slice remains intentionally deferred beyond `v3.0`.

**Coverage:** 7 phases, 17 requirements, 17 mapped, 0 unmapped

| # | Phase | Goal | Requirements |
|---|-------|------|--------------|
| 28 | Story Experience Orchestration Foundation | Complete on 2026-04-28 (4/4 plans); shared experience orchestration foundation and runnable admin/public baseline delivered | `STORY-01`, `STORY-02`, `STORY-03`, `STORY-04`, `LINK-01`, `LINK-02` |
| 29 | POI Default Experience Workbench | Complete on 2026-04-29 (3/3 plans); dedicated POI 地點體驗工作台, A-Ma Temple seed, and admin/public smoke verification delivered | `STORY-01`, `LINK-01` |
| 30 | Storyline Mode and Chapter Override Workbench | Complete on 2026-04-29 (4/4 plans); storyline mode, route strategy, chapter anchor inheritance, override workbench, seed, and admin/public smoke verification delivered | `STORY-02`, `STORY-04`, `LINK-02` |
| 31 | 4/4 | Complete    | 2026-04-29 |
| 32 | Dynamic Exploration and User Progress Model | Build dynamic exploration, traveler progress inspection, recomputation, and user timeline surfaces | `USER-01`, `USER-02`, `USER-03`, `USER-04`, `LINK-03` |
| 33 | Complete Flagship Story Content/Material Package | Produce and seed the complete "東西方文明的戰火與共生" content, media, Lottie, audio, pickups, challenges, rewards, and titles | `STORY-03`, `VER-02` |
| 34 | Public Runtime and Mini-program Consumption Baseline | Align public compiled runtime DTOs and mini-program story consumption baseline with the admin-authored experience model | `OPS-02`, `OPS-04`, `LINK-02`, `VER-01` |

## Phase Details

### Phase 28: Story Experience Orchestration Foundation

**Goal:** Deliver the shared experience orchestration foundation: default POI/spatial flows, story chapter inheritance and overrides, reusable template vocabulary, Lottie-aware media/content blocks, dynamic exploration elements, admin APIs, public compiled runtime DTOs, seed data, and smoke verification.

**Requirements:** `STORY-01`, `STORY-02`, `STORY-03`, `STORY-04`, `LINK-01`, `LINK-02`

**Success criteria:**
1. Backend schema and DTOs establish `experience_templates`, `experience_flows`, `experience_flow_steps`, `experience_bindings`, `experience_overrides`, exploration elements, user exploration events, and public compiled runtime responses.
2. Admin foundation surfaces expose templates, flows, bindings, overrides, exploration elements, governance, content blocks, and media without placeholder routing.
3. Local seeds and smoke checks prove the A-Ma Temple default flow, first flagship-story override, Lottie/media content blocks, `client_event_id` idempotency, and dynamic exploration calculation.
4. Planning artifacts clearly separate shipped foundation work from Phase 29-34 implementation ownership.

**Depends on:** Archived milestones `v2.0` and `v2.1`

### Phase 29: POI Default Experience Workbench

**Goal:** Build the dedicated POI default experience workbench.

**Requirements:** `STORY-01`, `LINK-01`

**Success criteria:**
1. Operators can configure a POI natural walk-in and tap-to-explore flow using a timeline, condition cards, effect cards, media cards, and reward cards.
2. The A-Ma Temple default experience can be authored without raw JSON for intro modal, route guidance, proximity media, check-in task release, pickups, hidden dwell achievement, and reward/title grant.
3. POI experience steps can save reusable templates for later story, indoor, reward, and activity use.
4. POI default experience bindings remain aligned with the Phase 28 public runtime contract.

**Depends on:** Phase 28

### Phase 30: Storyline Mode and Chapter Override Workbench

**Goal:** Build storyline mode and chapter override authoring.

**Requirements:** `STORY-02`, `STORY-04`, `LINK-02`

**Success criteria:**
1. Operators can configure storyline overview, route sequence, chapter anchors, chapter inherited flows, and per-step disable/replace/append overrides.
2. Chapter editors can add story-specific media, overlays, pickups, hidden challenges, reward/title grants, and completion effects without raw JSON as the primary path.
3. Story mode map strategy can hide unrelated content, reveal nearby content, highlight current chapter routes, mute inactive routes, and clear temporary story-session progress on exit.
4. Admin-authored overrides compile into public storyline runtime DTOs.

**Depends on:** Phases 28 and 29

### Phase 31: Interaction/Task Template Library and Governance Center

**Goal:** Build the reusable interaction/task template library and governance center.

**Requirements:** `OPS-01`, `OPS-03`, `LINK-01`

**Success criteria:**
1. Operators can author and reuse display, appearance-condition, trigger-condition, trigger-effect, task-gameplay, and reward-presentation templates.
2. Governance center shows all template and instance usage across city, sub-map, POI, indoor building, storyline, chapter, reward, and activity scopes.
3. Conflict checks flag overlapping full-screen effects, duplicate reward grants, invalid story overrides, and shared pickup/reward conflicts.
4. Existing indoor rule governance and reward rule centers are aggregated rather than replaced by a parallel model.

**Depends on:** Phases 28, 29, and 30

### Phase 32: Dynamic Exploration and User Progress Model

**Goal:** Build the dynamic exploration and user progress model.

**Requirements:** `USER-01`, `USER-02`, `USER-03`, `USER-04`, `LINK-03`

**Success criteria:**
1. Operators can inspect profiles, preferences, linked city/map/story scope, permanent exploration events, and story sessions.
2. Progress by city, sub-map, POI, indoor map, storyline, chapter, task, collectible, reward, and media completion is calculated from published exploration elements and semantic weights.
3. Manual recompute or repair actions are explicit, safe, and audited.
4. Content additions or lifecycle changes update percentages predictably without deleting completed user events.

**Plans:** 6 plans

Plans:
- [ ] `32-01-PLAN.md` — Canonical weighted progress engine and admin/public scope parity
- [ ] `32-02-PLAN.md` — Durable storyline session persistence for the public runtime
- [ ] `32-03-PLAN.md` — Preview-first recompute/repair core rebuild and audit persistence
- [ ] `32-04-PLAN.md` — Traditional Chinese workbench UI, seeded traveler fixtures, and end-to-end smoke
- [ ] `32-05-PLAN.md` — Traveler workbench read model and paginated timeline
- [ ] `32-06-PLAN.md` — Admin progress-ops API surface and audit response contracts

**Depends on:** Phases 28, 29, 30, and 31

### Phase 33: Complete Flagship Story Content/Material Package

**Goal:** Produce and seed the complete "東西方文明的戰火與共生" content and material package.

**Requirements:** `STORY-03`, `VER-02`

**Success criteria:**
1. The five-chapter flagship story contains the approved chapters, mainline interactions, side pickups, hidden challenges, rewards, honor titles, and final challenge.
2. Generated or prepared images, Lottie JSON, audio, video, pickup icons, badges, and fallback assets are tracked through a manifest with local path, COS key, content asset id, prompt/script provenance, and usage target.
3. Historical basis and literary dramatization fields remain distinguishable in the admin content package.
4. The material package is reusable by the admin media/content/experience workbenches.

**Depends on:** Phases 28, 29, 30, and 31

### Phase 34: Public Runtime and Mini-program Consumption Baseline

**Goal:** Align public runtime contracts and the mini-program story consumption baseline with the admin-authored experience model.

**Requirements:** `OPS-02`, `OPS-04`, `LINK-02`, `VER-01`

**Success criteria:**
1. Public APIs expose compiled POI and storyline runtime flows rather than raw admin payloads.
2. Mini-program story consumption can render the storyline introduction, chapter list, content blocks, Lottie, audio, video, current chapter route state, and basic event reporting.
3. Unsupported complex gameplay degrades clearly instead of blank-screening.
4. The local stack can verify the admin/public/mini-program baseline while keeping full WeChat experiential acceptance as future scope when explicitly deferred.

**Depends on:** Phases 28, 29, 30, 31, 32, and 33

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
| v3.0 Admin Core Domain Completion and Control-Plane Linkage | 28-34 | Phases 28-30 complete (11/11 executed plans); 3/7 phases complete | Active | In progress |
