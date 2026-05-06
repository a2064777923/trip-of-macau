---
phase: 45
plan: 01
status: completed
completed_at: 2026-05-06T12:36:04+08:00
requirements_completed: [ADMIN-01, PLAY-01, PLAY-05, OPS-03]
---

# 45-01 Summary

## Completed

- Added admin delete-impact and lifecycle endpoints for storylines, with archive/unpublish as the safe default and guarded hard delete only for dependency-free draft/editing stories.
- Replaced one-step admin story deletion with an impact-aware `下線 / 刪除` drawer that shows public status, dependency counts, recommended action, blocking reasons, warnings, and code-confirmed hard delete only when allowed.
- Corrected traveler story selection so the mini-program prefers `east_west_war_and_coexistence`, no longer treats `macau_fire_route` as a flagship candidate, and clears stale story/session/route state when the public runtime returns `4042 Storyline not found`.
- Added an idempotent UTF-8 SQL correction script that archives `macau_fire_route`, republishes/sorts `east_west_war_and_coexistence`, and never deletes rows.
- Added a local smoke script for public story list/runtime checks plus optional admin delete-impact verification.

## Verification

- PASS: `mvn -q -DskipTests compile` in `packages/admin/aoxiaoyou-admin-backend`.
- PASS: `npm run type-check` in `packages/admin/aoxiaoyou-admin-ui`.
- PASS: `npm run build:weapp` in `packages/client`; Taro reported the existing `pages/story/index.js` asset-size warning.
- PASS: `scripts/local/fix-phase-45-storyline-runtime-selection.sql` applied locally and left story `id=8 code=macau_fire_route` archived while story `id=9 code=east_west_war_and_coexistence` is published and sorted first.
- PASS: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-45-storyline-lifecycle.ps1 -AdminToken <redacted>` verified old duplicate absence, new flagship presence, five runtime chapters, and hard delete blocked for `storylineId=8`.
- PASS: boundary probe verified a dependency-free draft story can be hard-deleted with exact code confirmation, while hard delete of published `storylineId=9` returns business code `4060`.

## Notes

- The old duplicate is intentionally retained as `archived` because impact analysis shows chapters, content relations, exploration elements, user exploration events, user story sessions, and progress rows still reference it.
- Phase 45 did not perform WeChat DevTools/device UAT. It verifies the public/admin runtime chain and mini-program build only.
