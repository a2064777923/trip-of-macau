# Roadmap: Trip of Macau

## Milestones

- [x] **v1.0 Live Backend Cutover** - Shipped 2026-04-13. Archives: `.planning/milestones/v1.0-ROADMAP.md`, `.planning/milestones/v1.0-REQUIREMENTS.md`
- [x] **v2.0 Admin Control-Plane Reconstruction** - Archived 2026-04-15 with accepted gaps. Archives: `.planning/milestones/v2.0-ROADMAP.md`, `.planning/milestones/v2.0-REQUIREMENTS.md`
- [x] **v2.1 Interactive Rules Platform and AI Capability Center** - Archived 2026-04-19 with one accepted future slice for deferred mini-program experiential acceptance. Archives: `.planning/milestones/v2.1-ROADMAP.md`, `.planning/milestones/v2.1-REQUIREMENTS.md`
- [x] **v3.0 Admin Core Domain Completion and Control-Plane Linkage** - Shipped 2026-04-30. Archives: `.planning/milestones/v3.0-ROADMAP.md`, `.planning/milestones/v3.0-REQUIREMENTS.md`, `.planning/milestones/v3.0-MILESTONE-AUDIT.md`
- [x] **v3.1 Material Production and Mini-program Experience Acceptance** - Shipped 2026-05-04 with accepted WeChat UAT caveat. Archives: `.planning/milestones/v3.1-ROADMAP.md`, `.planning/milestones/v3.1-REQUIREMENTS.md`
- [ ] **v3.2 Traveler Gameplay Runtime and Operations Acceptance** - Active. Phases 41-42 completed with automated runtime/gameplay acceptance; physical-device journey UAT remains for later acceptance.

## Current Milestone

**v3.2 Traveler Gameplay Runtime and Operations Acceptance**

**Goal:** Turn the configured story/material/runtime platform into a device-verifiable mini-program gameplay loop, with operator support paths for progress, rewards, media, and runtime issues.

## Phase Roadmap

| Phase | Name | Goal | Requirements | Status |
|-------|------|------|--------------|--------|
| 41 | WeChat Runtime UAT Harness and Story Entry Hardening | Make the live flagship story route open reliably in WeChat DevTools/local runtime, with correct config, media fallback, and build/UAT harnesses. | UAT-01, PLAY-01, PLAY-05 | Complete |
| 42 | Traveler Gameplay Event Engine | Convert compiled experience-flow steps into mini-program gameplay interactions for story progression, pickups, tasks, rewards, titles, session exit/re-entry, and unsupported-feature fallbacks. | PLAY-02, PLAY-03, PLAY-04 | Complete |
| 43 | Traveler Progress and Reward Operations | 3/6 | In Progress|  |
| 44 | Management-System IA Polish and Release Acceptance | Remove/merge misleading admin entries, polish story/gameplay operational pages, repair preview/detail UX, and produce final automated plus manual UAT evidence. | UAT-02, UAT-03, ADMIN-01, ADMIN-02, ADMIN-03, ADMIN-04 | Pending |

## Phase Details

### Phase 41: WeChat Runtime UAT Harness and Story Entry Hardening

**Goal:** Make the real mini-program story route runnable against the local public backend and generated media assets before deeper gameplay engines are added.

**Requirements:** UAT-01, PLAY-01, PLAY-05

**Success criteria:**
1. `npm run build:weapp` succeeds or records only accepted warnings, and the generated project opens with the documented WeChat DevTools command.
2. The flagship `東西方文明的戰火與共生` story route loads from `packages/server` runtime data, not mock-only state.
3. Story introduction, chapter list, current route, content blocks, image/audio/video/Lottie assets, and fallback posters render without blank-screen failures.
4. Environment configuration for local/devtools/experience builds is documented and prevents accidental production/dev-bypass mismatch.
5. A repeatable UAT harness records backend health, runtime API response, build output, and DevTools launch outcome.

### Phase 42: Traveler Gameplay Event Engine

**Goal:** Make baseline story gameplay actually playable from compiled runtime steps instead of showing static cards only.

**Requirements:** PLAY-02, PLAY-03, PLAY-04

**Success criteria:**
1. Mini-program runtime step interpreter handles click, proximity, content-completion, pickup, task-progress, reward, and title events through public APIs.
2. Traveler sees Traditional Chinese feedback for collected items, task progress, medals, titles, coins, and unsupported advanced features.
3. Story session start/event/exit/re-entry behavior matches backend state and keeps permanent exploration/reward events durable.
4. Duplicate event submissions are idempotent and do not grant duplicate rewards.
5. Unsupported AR/speech/puzzle templates degrade into clear pending-feature states rather than crashing or silently doing nothing.

### Phase 43: Traveler Progress and Reward Operations

**Goal:** Make live traveler progress and rewards supportable from admin without direct database inspection.

**Requirements:** OPS-01, OPS-02, OPS-03, OPS-04

**Success criteria:**
1. Admin can inspect traveler story sessions, exploration events, pickups, rewards, titles, and backpack state from one coherent workflow.
2. Admin filters support user, story, chapter, POI/map, status, event type, reward type, and time range.
3. Support actions such as recompute progress, resend reward, void duplicate event, and issue annotation are preview-first and audit-backed.
4. Reward/title rule trace explains why a traveler did or did not receive a configured outcome.
5. Public and admin state remain consistent after support actions and are covered by smoke verification.

### Phase 44: Management-System IA Polish and Release Acceptance

**Goal:** Close the operator-facing rough edges and produce truthful release evidence for v3.2.

**Requirements:** UAT-02, UAT-03, ADMIN-01, ADMIN-02, ADMIN-03, ADMIN-04

**Success criteria:**
1. Story/gameplay/admin navigation has no misleading placeholder, duplicate, wrong-redirect, or shell-only entries for v3.2-owned workflows.
2. Runtime operations pages have readable columns, stable responsive panels, sane empty states, and Traditional Chinese labels.
3. Media/material/detail drawers preview valid assets, clearly mark unavailable assets, truncate long URLs cleanly, and do not link to missing package versions.
4. Interaction templates, governance checks, and runtime-state actions include concise inline explanations and avoid JSON-first operator workflows.
5. Final acceptance report separates automated smoke, browser/admin checks, WeChat DevTools/device checks, accepted caveats, and future gameplay scope.

## Archived Milestones

<details>
<summary>[x] v3.1 Material Production and Mini-program Experience Acceptance (Phases 36-40, shipped 2026-05-04)</summary>

- [x] Generated/imported flagship story stills, material boards, audio, videos, COS-backed assets, and package-version promotion/rollback records.
- [x] Built Traditional Chinese material QA/reuse controls and package-aware media picker/search metadata.
- [x] Exposed traveler-safe public story runtime assets, fallback/unsupported states, and idempotent baseline story events.
- [x] Connected the mini-program story-mode baseline to live public runtime data, route handoff, media rendering, action cards, and rewards/progress events.
- [x] Added Phase 40 release-readiness smoke, AI cost observability, acceptance report, and WeChat UAT checklist.
- [ ] Manual WeChat DevTools/device UAT remains pending and accepted as a caveat.

</details>

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
| v3.1 Material Production and Mini-program Experience Acceptance | 36-40 | 18/18 | Shipped with accepted UAT caveat | 2026-05-04 |
| v3.2 Traveler Gameplay Runtime and Operations Acceptance | 41-44 | 6/6 Phase 41-42 plans | Active | - |
