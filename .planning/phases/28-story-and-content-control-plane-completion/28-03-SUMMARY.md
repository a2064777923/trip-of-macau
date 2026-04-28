---
phase: 28-story-and-content-control-plane-completion
plan: 03
subsystem: seed-and-smoke
tags: [mysql, utf8mb4, smoke, admin-api, public-runtime, exploration]
requires:
  - phase: 28-01
    provides: Versioned admin and public experience runtime contract
  - phase: 28-02
    provides: Admin workbench routes and UI contract
provides:
  - UTF-8 safe Phase 28 seed verification
  - Runnable admin/public experience smoke script
  - Idempotent public exploration event verification
affects: [phase-29-poi-experience, phase-30-storyline-mode, phase-32-exploration-progress, phase-34-public-runtime]
key-files:
  created:
    - scripts/local/smoke-phase-28-experience.ps1
  modified:
    - scripts/local/mysql/init/39-phase-28-experience-orchestration.sql
    - packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminExperienceOrchestrationServiceImpl.java
requirements-completed: [STORY-01, STORY-02, STORY-03, LINK-02]
completed: 2026-04-28
---

# Phase 28 Plan 03: Seed And Smoke Summary

Aligned the local Phase 28 data and smoke evidence for the experience orchestration foundation.

## Accomplishments

- Added `SET NAMES utf8mb4` to the experience orchestration seed and made the story override seed safer to rerun by deduplicating override rows and preventing repeated disable override inserts.
- Created `scripts/local/smoke-phase-28-experience.ps1` to verify admin templates, flows, bindings, overrides, exploration elements, governance overview, public POI runtime, storyline runtime, event ingestion, duplicate `client_event_id`, and dynamic exploration progress.
- Fixed a backend bug exposed by the smoke script: optional admin experience query filters could call `normalizeCode(null)` before MyBatis-Plus evaluated the `.eq(...)` condition.

## Verification

- `Select-String` confirmed both Phase 28 SQL files contain `SET NAMES utf8mb4`, `schemaVersion`, Lottie coverage, A-Ma Temple content, and story runtime fields.
- `Select-String` confirmed the smoke script references admin/public endpoints, `zh-Hant`, `client_event_id`, UTF-8 handling, `/experience/poi/9`, `/storylines/8/runtime`, and exploration checks.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-28-experience.ps1` passed against local 8081 and 8080 after restarting both services on freshly compiled classes.
- `mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml` passed.
- `mvn -q -DskipTests compile -f packages/server/pom.xml` passed.
- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui` passed, with the existing Vite chunk-size warning.

## Notes

- The smoke script reads auth from `PHASE28_ADMIN_BEARER`, `PHASE28_ADMIN_USERNAME`/`PHASE28_ADMIN_PASSWORD`, `PHASE28_PUBLIC_BEARER`, or local ignored files. It does not contain tracked secrets.
- Public event idempotency was verified by posting the same `client_event_id` twice and asserting both responses return the same event id.
