# Quick Task 260429-ocp: dirty worktree / admin IA cleanup

**Date:** 2026-04-29
**Status:** Completed

## Scope

Clean local dirty-tree noise, keep project documentation truthful, remove clearly unused admin UI surfaces, and verify the admin flow in a real browser.

## Tasks

1. Repository hygiene
   - Add ignore rules for local Playwright/output/probe artifacts.
   - Delete untracked generated screenshots, logs, temporary login/probe files, and duplicated ad-hoc top-level docs.
   - Do not revert or discard phase implementation files.

2. Admin information architecture cleanup
   - Remove dead admin page components that are not routed or imported.
   - Remove the duplicate visible `用戶進度與軌跡` placeholder from the sidebar now that the user progress workbench exists.
   - Keep compatibility redirects for old URLs where useful.
   - Rename ambiguous menu labels where a retained function is useful but currently unclear.

3. Documentation and verification
   - Replace stale README startup notes with the current milestone/admin-first state.
   - Add a concise admin control-plane audit document that records retained modules, removed dead modules, and known follow-up candidates.
   - Build the admin UI and run a browser walkthrough across dashboard, story/experience, POI, user progress, AI, and system settings surfaces.
