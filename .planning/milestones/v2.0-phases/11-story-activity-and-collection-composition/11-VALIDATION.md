---
phase: 11
slug: story-activity-and-collection-composition
status: planned
nyquist_compliant: true
wave_0_complete: false
created: 2026-04-14
updated: 2026-04-14
---

# Phase 11 - Validation Strategy

> Per-phase validation contract for storylines, chapter composition, authored activities, and the collection/reward content graph.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 + Spring Boot Test + admin UI build verification + mini-program build + PowerShell composition smoke |
| **Config file** | `packages/admin/aoxiaoyou-admin-backend/pom.xml`, `packages/server/pom.xml`, `packages/admin/aoxiaoyou-admin-ui/package.json`, `packages/client/package.json` |
| **Quick run command** | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend` and `packages/server`, plus `npm run build` in `packages/admin/aoxiaoyou-admin-ui` |
| **Full suite command** | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend`, `mvn test -q` in `packages/server`, `npm run build` in `packages/admin/aoxiaoyou-admin-ui`, `npm run build:weapp` in `packages/client`, and `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-11-composition.ps1` |
| **Estimated runtime** | ~420 seconds |

---

## Sampling Rate

- **After every task commit:** run the touched package quick command
- **After every plan wave:** run the full Phase 11 command set
- **Before `/gsd-verify-work`:** full suite must be green and the Phase 11 smoke script must pass against the current admin and public runtimes
- **Max feedback latency:** 420 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 11-01-01 | 01 | 1 | STORY-01, STORY-03 | T-11-01 / T-11-02 | Storyline multi-bindings and chapter relation metadata persist through canonical relation records instead of raw ad hoc fields | unit/migration | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend` | add schema/service tests | pending |
| 11-01-02 | 01 | 1 | STORY-01, STORY-02, STORY-03 | T-11-03 | Dedicated chapter composition route and admin APIs expose storyline bindings, anchor selection, and structured prerequisite/completion/reward metadata | build/component | `npm run build` in `packages/admin/aoxiaoyou-admin-ui` and `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend` | add composition page and endpoint tests | pending |
| 11-01-03 | 01 | 1 | STORY-01, STORY-03 | T-11-04 | Public storyline contracts and mini-program mappers accept new bindings and chapter metadata without breaking the live catalog path | integration/build | `mvn test -q` in `packages/server` and `npm run build:weapp` in `packages/client` | add public story tests and client contract checks | pending |
| 11-02-01 | 02 | 2 | ACT-01 | T-11-05 / T-11-06 | Activities are authored through real CRUD, schedule windows are enforced, and publish visibility does not leak drafts | unit/integration | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend` and `packages/server` | add activity authoring and publish-window tests | pending |
| 11-02-02 | 02 | 2 | ACT-01 | T-11-06 | Admin activity authoring UI and public discover activity cards are both sourced from the same canonical activity records | build/smoke | `npm run build` in `packages/admin/aoxiaoyou-admin-ui`, `npm run build:weapp` in `packages/client`, and `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-11-composition.ps1 -Scenario activity` | add admin/client/discover smoke coverage | pending |
| 11-03-01 | 03 | 3 | COLL-01 | T-11-07 / T-11-08 | Collectibles, badges, and rewards store canonical asset IDs plus storyline/spatial relation metadata and expose safe CRUD/update behavior | unit/integration | `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend` and `packages/server` | add collection graph tests | pending |
| 11-03-02 | 03 | 3 | COLL-01 | T-11-08 | Collection authoring surfaces, reward IA ownership, and public reward/collection catalog reads align end-to-end | build/smoke | `npm run build` in `packages/admin/aoxiaoyou-admin-ui`, `npm run build:weapp` in `packages/client`, and `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-11-composition.ps1 -Scenario collection` | add reward/collection smoke coverage | pending |

*Status: pending / green / red / flaky*

---

## Wave 0 Requirements

- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../StoryComposition*` - storyline binding and chapter metadata tests
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../ActivityAuthoring*` - activity CRUD and publish-window tests
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/.../CollectionGraph*` - collectible, badge, and reward relation tests
- [ ] `packages/server/src/test/java/.../PublicStoryCatalog*` - public storyline contract tests
- [ ] `packages/server/src/test/java/.../PublicActivityCatalog*` - public activity/discover sourcing tests
- [ ] `packages/server/src/test/java/.../PublicCollectionCatalog*` - public reward and collection catalog tests
- [ ] `scripts/local/smoke-phase-11-composition.ps1` - end-to-end proof for story, activity, and collection composition

---

## Planned Execution Results

- `mvn test -q` in `packages/admin/aoxiaoyou-admin-backend`: pending
- `mvn test -q` in `packages/server`: pending
- `npm run build` in `packages/admin/aoxiaoyou-admin-ui`: pending
- `npm run build:weapp` in `packages/client`: pending
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-11-composition.ps1`: pending

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Dedicated chapter composition usability | STORY-02, STORY-03 | Build success cannot prove that chapter ordering, anchor selection, and metadata editing are understandable | Open the chapter composition page for one storyline, create at least two chapters, reorder them, and confirm anchor and reward editors are usable without opening raw JSON first |
| Activity HTML content authoring clarity | ACT-01 | Operators need to understand how raw HTML, preview, and publish windows interact | Create one activity with HTML content, preview it, set publish and signup windows, and confirm the saved detail remains understandable on reload |
| Reward ownership in collection IA | COLL-01 | Route placement and form grouping are information-architecture behaviors, not just DTO changes | Open the collection module, confirm rewards live there instead of under system ownership, and verify collectible/badge/reward bindings are visible together |
| Story/activity/collection public alignment | STORY-01, ACT-01, COLL-01 | End-to-end authoring quality depends on live reads, not only admin saves | Create one storyline, one activity, and one reward relation, then verify their public read outputs match the admin-authored values |

---

## Validation Sign-Off

- [x] All tasks have automated verification expectations or explicit Wave 0 dependencies
- [x] Sampling continuity is preserved across admin backend, public backend, admin UI, and mini-program build verification
- [x] Wave 0 captures the missing story/activity/collection smoke and contract coverage required by Phase 11
- [x] No watch-mode flags
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** planned 2026-04-14
