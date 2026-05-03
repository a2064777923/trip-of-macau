# Phase 37 Verification — Material QA Workspace and Reuse Controls

Date: 2026-05-03

## Scope

- QA-01: Material package QA backend overview, item filters, detail, consistency checks, and action APIs.
- QA-02: Admin material QA workspace with health cards, filters, drawers, preview states, and safe actions.
- QA-03: Approved package assets are discoverable and reusable through existing media picker flows.
- QA-04: Repeatable smoke covers QA endpoints, consistency checks, action flow, and picker-discoverable metadata.

## Automated Evidence

- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml`
  - Result: passed during 37-03 backend metadata implementation.
- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`
  - Result: passed on 2026-05-03. Vite emitted the existing large chunk size warning only.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-37-material-qa.ps1`
  - Result: passed on 2026-05-03 and printed `Material QA smoke passed`.

## Browser UAT Checklist

- `/content/material-packages` loads without console errors.
- QA overview and item tabs show nonzero material counts for `east_west_war_and_coexistence_package`.
- Consistency drawer opens and shows finding counters.
- QA detail drawer shows version history and current asset preview state.
- Media picker search can find `story_cover_copper_mirror` by item key and shows package metadata.
- Media library search can find package assets by package code, item key, usage target, chapter code, object key, canonical URL, and filename.

Browser UAT status: 37-02 browser smoke passed before 37-03. 37-03 reuse controls are covered by build plus API smoke; a final browser picker walkthrough remains advisable when doing broader admin UAT.

## Caveats

- The Phase 37 smoke disables COS `HEAD` checks by default to keep local verification independent from Tencent COS credentials.
- QA action smoke uses an idempotent approve flow on a safe current version instead of reject/rollback unless a dedicated disposable test item is introduced later.
- Secrets are read only from environment variables or local login defaults; no bearer token, provider key, or COS credential is written to tracked files.
