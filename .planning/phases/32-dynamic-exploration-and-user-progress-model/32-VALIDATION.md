---
phase: 32
slug: dynamic-exploration-and-user-progress-model
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-04-29
---

# Phase 32 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Backend: JUnit 5 through Spring Boot test; Admin UI: Vite TypeScript build plus browser smoke because no frontend unit harness is present. |
| **Config file** | Maven defaults in `packages/admin/aoxiaoyou-admin-backend/pom.xml` and `packages/server/pom.xml`; none for admin UI unit tests. |
| **Quick run command** | `mvn -q -Dtest=AdminUserServiceImplTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` |
| **Full suite command** | `mvn -q test -f packages/admin/aoxiaoyou-admin-backend/pom.xml; mvn -q test -f packages/server/pom.xml; npm run build --prefix packages/admin/aoxiaoyou-admin-ui; powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-32-user-progress.ps1` |
| **Estimated runtime** | ~180 seconds locally, excluding first-run dependency download. |

---

## Sampling Rate

- **After every backend task commit:** Run the targeted Maven test class or compile command for the touched service/controller layer.
- **After every admin UI task commit:** Run `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`.
- **After every plan wave:** Run both backend Maven test suites and admin UI build.
- **Before `/gsd-verify-work`:** Full suite and Phase 32 smoke script must be green against local MySQL/admin/public services.
- **Max feedback latency:** 300 seconds for a wave-level check.

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 32-01-01 | 01 | 1 | USER-02, LINK-03 | T32-01 | Schema and Wave 0 test contracts lock the canonical denominator and inactive-element semantics before implementation. | contract/schema | `powershell -NoProfile -Command "if (-not (Select-String -Path 'scripts/local/mysql/init/43-phase-32-progress-engine.sql' -Pattern 'poi_id' -Quiet)) { exit 1 }; if (-not (Select-String -Path 'packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicExperienceServiceImplTest.java' -Pattern 'supportsGlobalAndLocationScopedWeightedProgress' -Quiet)) { exit 1 }; if (-not (Select-String -Path 'packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserProgressCalculatorTest.java' -Pattern 'matchesPublicWeightSemanticsForActiveElements' -Quiet)) { exit 1 }"` | ❌ W0 | ⬜ pending |
| 32-01-02 | 01 | 1 | USER-02, LINK-03 | T32-01 | Public and admin progress calculations use the same published-element denominator and immutable completion events. | unit/service | `mvn -q -Dtest=PublicExperienceServiceImplTest test -f packages/server/pom.xml; mvn -q -Dtest=AdminUserProgressCalculatorTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ❌ W0 | ⬜ pending |
| 32-02-01 | 02 | 2 | USER-03 | T32-02 | Durable session schema and tests prove exit only clears temporary state. | contract/schema | `powershell -NoProfile -Command "if (-not (Select-String -Path 'scripts/local/mysql/init/44-phase-32-story-sessions-and-timeline.sql' -Pattern 'user_storyline_sessions' -Quiet)) { exit 1 }; if (-not (Select-String -Path 'packages/server/src/test/java/com/aoxiaoyou/tripofmacau/StorylineSessionPersistenceTest.java' -Pattern 'exitLeavesExplorationEventsUntouched' -Quiet)) { exit 1 }"` | ❌ W0 | ⬜ pending |
| 32-02-02 | 02 | 2 | USER-03 | T32-02 | Story sessions are durable domain records and session exit does not remove permanent exploration events. | unit/service | `mvn -q -Dtest=StorylineSessionPersistenceTest test -f packages/server/pom.xml` | ❌ W0 | ⬜ pending |
| 32-03-01 | 03 | 2 | USER-04, LINK-03 | T32-04 | Repair schema and tests force preview-first confirmation and non-destructive duplicate/orphan handling. | contract/schema | `powershell -NoProfile -Command "if (-not (Select-String -Path 'scripts/local/mysql/init/45-phase-32-progress-repair-and-audit.sql' -Pattern 'user_progress_operation_audits' -Quiet)) { exit 1 }; if (-not (Select-String -Path 'packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserProgressRepairServiceTest.java' -Pattern 'previewDoesNotPersistChanges' -Quiet)) { exit 1 }"` | ❌ W0 | ⬜ pending |
| 32-03-02 | 03 | 2 | USER-04, LINK-03 | T32-04 | Recompute and repair core service rebuilds cache from source facts and never deletes immutable events. | unit/service | `mvn -q -Dtest=AdminUserProgressRepairServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ❌ W0 | ⬜ pending |
| 32-05-01 | 05 | 3 | USER-01, USER-02, USER-03 | T32-03 | Workbench/timeline contracts lock paginated APIs and explicit route-trace unavailable behavior. | contract/api | `powershell -NoProfile -Command "if (-not (Select-String -Path 'packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserTimelineServiceTest.java' -Pattern 'marksRouteTraceUnavailableWhenNoVerifiedStorageExists' -Quiet)) { exit 1 }; if (-not (Select-String -Path 'packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminTravelerProgressController.java' -Pattern '/progress-workbench' -Quiet)) { exit 1 }"` | ❌ W0 | ⬜ pending |
| 32-05-02 | 05 | 3 | USER-01, USER-02, USER-03 | T32-03 | Timeline aggregation is paginated, filtered, and limited to verified MySQL sources with explicit trace fallback. | unit/service | `mvn -q -Dtest=AdminUserTimelineServiceTest test -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ❌ W0 | ⬜ pending |
| 32-06-01 | 06 | 3 | USER-04 | T32-04 | Admin-only progress-ops routes require scoped preview/confirm DTO contracts before wiring. | contract/api | `powershell -NoProfile -Command "if (-not (Select-String -Path 'packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminTravelerProgressOpsController.java' -Pattern '/recompute-preview' -Quiet)) { exit 1 }; if (-not (Select-String -Path 'packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto/request/AdminUserProgressRepairRequest.java' -Pattern 'reason' -Quiet)) { exit 1 }"` | ❌ W0 | ⬜ pending |
| 32-06-02 | 06 | 3 | USER-04 | T32-04 | Progress-ops HTTP surface compiles against the scoped repair core service without exposing public repair paths. | compile/api | `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | ❌ W0 | ⬜ pending |
| 32-04-01 | 04 | 4 | USER-01, USER-02, USER-03, USER-04 | T32-05 | Smoke automation uses env-backed auth/token inputs and proves no admin repair endpoint leaks into public runtime. | build/smoke-contract | `powershell -NoProfile -Command "if (-not (Select-String -Path 'scripts/local/smoke-phase-32-user-progress.ps1' -Pattern 'PHASE32_ADMIN_BEARER_TOKEN' -Quiet)) { exit 1 }; if (-not (Select-String -Path 'scripts/local/smoke-phase-32-user-progress.ps1' -Pattern 'PHASE32_TRAVELER_BEARER_TOKEN' -Quiet)) { exit 1 }"` | ❌ W0 | ⬜ pending |
| 32-04-02 | 04 | 4 | USER-01, USER-02, USER-03, USER-04, LINK-03 | T32-05 | Admin workbench uses authenticated admin APIs, guarded repair actions, and Traditional Chinese UI copy. | build/smoke | `npm run build --prefix packages/admin/aoxiaoyou-admin-ui; powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-32-user-progress.ps1` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠ flaky*

---

## Wave 0 Requirements

- [ ] `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/PublicExperienceServiceImplTest.java` — weighted scope, inactive-element, and lifecycle recalculation coverage for `USER-02` and `LINK-03`.
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserProgressCalculatorTest.java` — admin/public parity coverage for scope denominators and retired completions.
- [ ] `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/StorylineSessionPersistenceTest.java` — durable story-session start, event attachment, and exit semantics.
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserTimelineServiceTest.java` — timeline fan-in coverage for check-ins, triggers, exploration events, reward history, sessions, and trace-unavailable fallback.
- [ ] `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AdminUserProgressRepairServiceTest.java` — recompute preview, confirm, cache rebuild, orphan-event linking, duplicate marking, and audit safety coverage.
- [ ] `scripts/local/smoke-phase-32-user-progress.ps1` — local end-to-end smoke for admin detail route, public exploration parity, recompute preview, repair audit visibility, and seeded traveler data with env-backed auth/token inputs.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Traveler progress workbench readability and guarded repair UX | USER-01, USER-02, USER-04 | No frontend component/e2e test harness exists in repo. | Open `/admin/#/users/progress/{seededUserId}` or launch the workbench from the user list, verify tabs `身份與偏好`, `進度總覽`, `探索元素明細`, `故事模式 Session`, `互動時間線`, `收集與獎勵來源`, `修復與重算`, and `審計紀錄`; trigger recompute preview and cancel before confirm. |
| Retired-element explanation text | USER-02, LINK-03 | Requires visual confirmation of operator-facing explanatory copy. | Seed or toggle an exploration element to unpublished, refresh detail, and verify completed retired elements show `已退役，不計入目前百分比` or equivalent Traditional Chinese copy. |

---

## Threat References

- **T32-01:** Derived progress tampering through direct cache edits or divergent admin/public calculators.
- **T32-02:** Story-session exit accidentally deleting permanent exploration facts.
- **T32-03:** Timeline payload exposing sensitive raw JSON or fabricated route-trace data.
- **T32-04:** Unauthorized or silent recompute/repair mutating derived state without audit.
- **T32-05:** Admin-only repair capability leaking into public runtime APIs or smoke automation depending on tracked credentials.

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies.
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify.
- [ ] Wave 0 covers all missing references.
- [ ] No watch-mode flags.
- [ ] Feedback latency < 300s.
- [ ] `nyquist_compliant: true` set in frontmatter after Wave 0 tests exist and pass.

**Approval:** pending
