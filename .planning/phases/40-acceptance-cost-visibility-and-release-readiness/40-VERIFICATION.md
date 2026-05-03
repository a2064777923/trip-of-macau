---
phase: 40-acceptance-cost-visibility-and-release-readiness
status: partial
verified: 2026-05-03
requirements:
  - ACC-01
  - ACC-02
  - ACC-03
  - ACC-04
---

# Phase 40 Verification

## Scope

Phase 40 verifies v3.1 release readiness evidence rather than adding new story gameplay engines. It covers the repeatable smoke bundle, AI monitoring/cost visibility, WeChat UAT checklist readiness, requirements traceability, and explicit deferred-scope documentation.

Status is `partial` because WeChat DevTools/device UAT rows are prepared but still `Pending`. Automated evidence has passed or is explicitly skipped by safe quick-mode policy.

## Automated Evidence

| Check | Result | Evidence |
| --- | --- | --- |
| `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` | Passed | Admin backend compiled after AI observability DTO/service/controller changes. |
| `npm run build` in `packages/admin/aoxiaoyou-admin-ui` | Passed | Admin UI build completed after `監控與成本` page rebuild; existing Vite large chunk warning remains advisory. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-40-release-readiness.ps1 -Quick` | Passed | Consolidated quick smoke wrote `40-SMOKE-REPORT.md` with Phase 36/37/38/39 checks and AI observability safe-field checks. |
| Phase 40 planning-doc sensitive-literal scan | Passed | `40-SMOKE-REPORT.md`, `40-UAT.md`, `40-ACCEPTANCE.md`, and this verification file contain no banned credential, raw prompt/script, or machine-path literals. |

## Admin Cost And History Evidence

- `AdminAiGenerationJobResponse` exposes cost labels, cost type, safe prompt/request summaries, candidate count, and latest asset metadata for operator-friendly job history.
- `AdminAiLogResponse` exposes model code, labeled cost data, and safe output summaries for request logs.
- `AdminAiController` and `AdminAiService` support request log filters for request type, owner, and inventory/model code.
- `/ai/observability` is now labeled `監控與成本`, with summary cards, visible filters, generation job and request log tables, drawer detail, ellipsized long values, and `進階診斷` kept collapsed by default.
- The smoke wrapper calls AI overview/jobs/logs and asserts the safe-field shape without writing raw payloads into the report.

## WeChat UAT Evidence

- `40-UAT.md` exists and contains an operator checklist for 微信開發者工具 or test-device validation.
- Checklist rows cover opening the flagship story, starting story mode, route/current-chapter display, all five chapters, images, Lottie, audio, video, pickup/action cards, baseline tasks, rewards/titles, map handoff, exit, restart, and unsupported AR/photo/speech/puzzle placeholders.
- Current manual UAT result is intentionally `Pending`; no device pass is claimed in this verification.

## Release Readiness

| Requirement | Phase 40 Evidence | Result |
| --- | --- | --- |
| ACC-01 | `scripts/local/smoke-phase-40-release-readiness.ps1` and `40-SMOKE-REPORT.md` provide a repeatable local smoke bundle. | Complete |
| ACC-02 | `40-UAT.md` provides the required WeChat DevTools/device checklist; execution remains pending. | Complete with pending execution caveat |
| ACC-03 | Wave 2 implemented AI monitoring/cost/job-history UI and API safe summaries, with smoke evidence. | Complete |
| ACC-04 | `40-ACCEPTANCE.md` separates automated evidence, manual pending UAT, skipped checks, accepted caveats, and deferred gameplay. | Complete |

## Caveats

- Manual WeChat DevTools/device UAT has not been executed in this run.
- Quick smoke skips live provider/COS mutation checks unless explicitly enabled; prior Phase 36 evidence remains the live asset/COS proof.
- Quick smoke skips full builds by default; Wave 2 separately verified admin backend compile and admin UI build, while Phase 39 holds WeChat build evidence.
- Full AR/photo recognition, indoor visual positioning, speech-input NPC interactions, route-coverage minigames, puzzle/minigame engines, cannon-defense gameplay, and full approval workflow remain future scope.

## Secret Safety

Phase 40 evidence avoids credential literals, raw provider payloads, full prompts, full scripts, and machine-specific absolute paths. Public runtime verification remains traveler-safe, while admin AI monitoring defaults to safe summaries and keeps advanced diagnostics collapsed.
