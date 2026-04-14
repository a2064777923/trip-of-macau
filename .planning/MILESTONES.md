# Milestones

## v1.0 Live Backend Cutover (Shipped: 2026-04-13)

**Phases completed:** 6 phases, 19 plans, 11 tasks

**Archive note:** This milestone was archived without a dedicated `.planning/v1.0-MILESTONE-AUDIT.md` artifact. Future milestones should run `/gsd-audit-milestone` before archival.

**Key accomplishments:**

- Canonical mini-program/admin/public ownership rules with mirrored status, locale, and asset-kind enums in both Java backends
- Canonical MySQL foundation tables, deterministic seed provenance, and a schema map that ties mini-program surfaces to live backend storage
- Public-backend boot cleanup, canonical MyBatis scaffolding for new domains, and a local-profile Spring context smoke test
- Aligned local public/admin backend profiles, repeatable start helpers, and a smoke harness that verifies real health endpoints against local datastores
- Canonicalized the existing admin map, story, chapter, POI, and reward stack against the live MySQL schema and repaired the local brownfield database so the APIs can run for real.
- Added the missing admin content/runtime CRUD so the admin backend can manage every remaining mini-program-facing content surface introduced in Phase 1.
- Replaced the placeholder admin UI surfaces with live canonical bindings and closed the loop with real backend smoke verification.
- Canonical public user-state persistence and write-side service logic now live in `packages/server`.
- The public user controller surface is live and backed by a repeatable local smoke script.
- Mini-program gameplay writes now target live public APIs instead of local-only mock mutations.
- The admin backend now owns the real Tencent COS media pipeline.
- The `/admin` asset console now uploads real files instead of faking asset records.
- Phase 5 now has a repeatable live smoke harness and closed-loop verification evidence.
- The former mini-program mock dataset now lives as a repeatable canonical MySQL seed instead of an app-only fallback.
- The admin system now reports real integration health and real traveler activity instead of stale placeholder state.
- The mini-program cutover is now proven end to end against the real backend stack.

---
