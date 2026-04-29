# Quick Task 260429-ocp Summary

**Date:** 2026-04-29
**Status:** Completed

## Outcome

Cleaned local verification noise, documented the current admin control-plane information architecture, removed one confirmed dead routed file, and verified that the retained admin shell still builds and opens in a browser.

## Repository Hygiene

- Added ignore rules for local Playwright screenshots/logs, `output/`, temporary login JSON, phase COS probe output, and one-off local guide files.
- Removed untracked generated artifacts and duplicate local guide documents that are not canonical project documentation.
- Left phase implementation files intact; the worktree still contains active feature work from recent phases and should not be bulk-reverted.
- Excluded local model/provider configuration from this cleanup because it contains workstation-specific API keys.

## Admin IA Cleanup

- Removed the duplicate visible `用戶進度與軌跡` sidebar item and retained `/users/story-progress` as a compatibility redirect to `/users/progress`.
- Renamed the map-space rule entry to `室內互動治理中心` so it does not conflict with the cross-domain experience governance center.
- Switched the sidebar to an accordion-style open behavior to reduce navigation clutter.
- Deleted the tracked dead page `MapSpace/AiCapabilityCenter.tsx`, which is superseded by the dedicated AI capability center.

## Documentation

- Replaced the stale root README with the current v3.0 startup, verification, and documentation entry points.
- Added `docs/admin-control-plane-audit.md` to record retained modules, removed redundant entry points, local artifact cleanup policy, and follow-up candidates.
- Recorded UTF-8/utf8mb4 handling as a project constraint in AGENTS.md.

## Verification

- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` passed; only the existing Vite chunk-size warning remains.
- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` passed.
- Browser walkthrough covered login, dashboard, AI center, POI experience, user progress, and system settings. The final clean session showed no functional console errors.

## Residual Risks

- The repository remains broadly dirty from active phase work. This quick task intentionally scopes cleanup to local artifacts, dead IA, and docs rather than committing or reverting unfinished feature files.
- `.codebuddy/models.json` is still a local dirty tracked file and includes provider keys; it should be handled separately as a secrets hygiene task, not staged in this cleanup.
