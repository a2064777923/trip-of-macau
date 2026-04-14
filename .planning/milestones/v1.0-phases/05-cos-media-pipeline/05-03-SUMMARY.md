---
phase: 05-cos-media-pipeline
plan: 03
subsystem: verification
tags: [smoke, verification, public-resolution, cleanup]
requires: [05-01, 05-02]
provides:
  - repeatable Phase 5 smoke coverage for admin login, COS upload, MySQL assertions, and public URL resolution
  - evidence that public responses consume the same canonical URL persisted by the admin upload pipeline
  - verified cleanup path for uploaded assets through the admin delete API
affects: [phase-06-migration-cutover-and-hardening]
tech-stack:
  added: []
  patterns: [live COS smoke, temporary public-link verification, post-smoke cleanup]
key-files:
  created:
    - scripts/local/smoke-phase-05-cos-media.ps1
key-decisions:
  - "Verify the media pipeline by temporarily linking an uploaded published asset into an existing published reward and then restoring the original value."
  - "Clean up smoke assets through the real admin delete API so verification also exercises remote object deletion."
requirements-completed: [MED-01, MED-02]
duration: brownfield pass
completed: 2026-04-12
---

# Phase 5: Plan 03 Summary

**Phase 5 now has a repeatable live smoke harness and closed-loop verification evidence.**

## Accomplishments

- Added `scripts/local/smoke-phase-05-cos-media.ps1` to authenticate against the admin backend, upload a real 1x1 PNG into Tencent COS, assert the resulting `content_assets` row, probe the canonical URL, and verify public reward URL resolution.
- Verified the admin/public contract by temporarily wiring the uploaded asset into `reward 1`, reading `/api/v1/rewards?locale=en`, and confirming the public `coverImageUrl` matched the uploaded canonical URL exactly.
- Exercised the delete path against real smoke assets and confirmed both `content_assets` row removal and `404` from the deleted COS object URL.
- Cleaned the temporary verification assets back out of MySQL and COS after the run.

## Verification

- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-05-cos-media.ps1`
- Manual follow-up delete verification on `DELETE /api/admin/v1/content/assets/{id}`

## Notes

- The running admin backend on `8081` was restarted with `APP_COS_*` variables before final smoke so the verification reflected the current code, not a stale pre-Phase-5 runtime.

---
*Phase: 05-cos-media-pipeline*
*Completed: 2026-04-12*
