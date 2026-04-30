# Phase 36-03 Summary — Package-Scoped Production UI

## Outcome

Extended the existing story material package page instead of creating a global production workbench:

- Added TypeScript contracts and API wrappers for preflight, local import, candidate bind, promote, rollback, and version history.
- Added a package-scoped production toolbar with `生產預檢`, `匯入本地素材`, `生成旁白`, `建立章節短片`, and version reload.
- Added preflight modal showing cost ceilings, estimated cost, target asset kinds, and `requiresSuperAdminConfirmation`.
- Added local import modal using backend-validated `relativeLocalPath` and `forcedCosObjectKey`.
- Added Mandarin narration drawer reusing existing AI voice preview, generation job, refresh, finalize, and Phase 36 bind-candidate paths.
- Added per-item version drawer with current/published version state, parent/crop metadata, prompt/script/subtitle metadata, publish, and rollback actions.

## Verification

- `npm run build --prefix packages/admin/aoxiaoyou-admin-ui`

Build passed locally. Vite still reports the existing large bundle warning.

## Notes

- The UI intentionally keeps `建立章節短片` disabled until Phase 36-05 adds the ffmpeg subtitle gate.
- Audio flows surface `manualImportRequired` / `retry_required` language and do not imply audio is ready for publish when provider output is missing.
- Rollback confirmation states that rollback flips the current pointer while keeping later versions in history.
