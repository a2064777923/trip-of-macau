# Phase 22 Verification

## Runtime

- Verification date: `2026-04-19`
- Admin backend: local profile on `http://127.0.0.1:8081`
- Active process after restart: Java PID `42788`
- Database repair applied: `scripts/local/mysql/init/36-phase-22-ai-platform-verification.sql`

## Automated Evidence

### Backend test

Command:

```powershell
mvn -Dtest=AdminAiServiceImplTest test
```

Result:

- PASS
- `5` tests run, `0` failures, `0` errors

Coverage proved by `AdminAiServiceImplTest`:

- witness default resolution uses `dashscope-chat / qwen3.5-flash`
- missing witness inventory fails deterministically
- `finalizeCandidate(...)` inserts canonical `content_assets`
- `restoreCandidate(...)` remains owner-scoped
- super-admin visibility still bypasses owner restriction

### Full smoke

Command:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-22-ai-platform-verification.ps1
```

Observed result:

```text
[TEMPLATE_ONLY] template:openai
[CREDENTIAL_MISSING] platform:hunyuan
[TEMPLATE_ONLY] template:minimax
[TEMPLATE_ONLY] template:volcengine
[TEMPLATE_ONLY] template:custom
[LIVE_VERIFIED] witness:travel_qa
[LIVE_VERIFIED] creative:finalize
[ACCESS_DENIED] witness:admin_image_generation
[LIVE_VERIFIED] witness:admin_tts_generation
Phase 22 AI platform verification smoke passed.
```

## Witness Outcome Summary

| Witness path | Expected default | Outcome | Notes |
| --- | --- | --- | --- |
| `travel_qa` | `dashscope-chat / qwen3.5-flash` | `LIVE_VERIFIED` | Provider test passed on local `8081` and returned non-empty preview |
| `admin_image_generation` | `dashscope-image / wan2.6-image` | honest non-pass | Default was repaired from `wan2.6-t2i-turbo` to `wan2.6-image`; current vendor response is `Model.AccessDenied`, not `Model not exist` |
| `admin_tts_generation` | `dashscope-tts / cosyvoice-v3-flash` | `LIVE_VERIFIED` | Default voice repaired to `longanyang`; witness job completed and candidate audio persisted to COS |

## Creative Finalize Proof

- Smoke created a real `travel_qa` generation job on `8081`.
- Smoke finalized the returned candidate into canonical content asset `#300078`.
- This proves the finalize path is not candidate-only and still writes back a canonical `content_assets` row.

## TTS Proof

- Bailian direct witness reproduced the original failure when `voice=longyang`:
  - `HTTP 400`
  - `"[cosyvoice:]Engine return error code: 418"`
- The same witness succeeded immediately when `voice=longanyang`.
- Backend live verification on `8081` then completed a real `admin_tts_generation` job:
  - job id `26`
  - candidate id `7`
  - persisted COS asset: `miniapp/assets/audio/2026/04/19/zh-Hant/job-26-0434fd3ec503.mp3`
- This proves the fix is not only a unit-level change; the full admin TTS path now runs through provider call, signed asset download, and COS persistence.

## Provider Truth Proof

- `openai`, `minimax`, `volcengine`, and `custom` remain `TEMPLATE_ONLY`.
- `hunyuan` is configured as a provider row but has no usable credential, so it remains `CREDENTIAL_MISSING`.
- Bailian chat is the only current `LIVE_VERIFIED` witness path on this workstation.
- Bailian image remains explicitly non-pass because current model entitlement is denied by the vendor.
- Bailian TTS is now a live-verified witness path on this workstation.

## Route and Authoring Proof

- The AI workspace route inventory is defined directly in `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/catalog.tsx`.
- No visible `traveler-services` ghost route remains in the current AI workspace navigation set.
- POI witness fields are wired in `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement/index.tsx` and `packages/admin/aoxiaoyou-admin-ui/src/components/media/MediaAssetPickerField.tsx` for `coverAssetId`, `mapIconAssetId`, and `audioAssetId`.

## Conclusion

Phase 22 execution is complete for automated verification.

What is closed:

- witness defaults are deterministic and no longer point at the broken image default
- provider truth states are exposed honestly instead of overclaiming live coverage
- the creative finalize path is proven on the live local stack
- Phase 22 now has one canonical smoke entrypoint and one current validation artifact

What remains intentionally honest:

- `admin_image_generation` is not claimed as live-verified on this workstation because the current Bailian key returns `Model.AccessDenied`
- browser-backed manual UAT for `/ai` surfaces and POI field backfill remains the next `/gsd-verify-work` step, not something claimed here without direct browser execution
