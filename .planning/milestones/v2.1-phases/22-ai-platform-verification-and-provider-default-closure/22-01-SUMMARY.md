# 22-01 Summary

## Outcome

Plan 01 closed the witness-default and provider-truth baseline.

- Applied `scripts/local/mysql/init/36-phase-22-ai-platform-verification.sql` to local MySQL.
- Normalized Bailian witness defaults to:
  - `travel_qa -> dashscope-chat / qwen3.5-flash`
  - `admin_image_generation -> dashscope-image / wan2.6-image`
  - `admin_tts_generation -> dashscope-tts / cosyvoice-v3-flash`
- Restarted the admin backend on `8081` so the latest Java code is the running code path.
- Verified `dashscope-chat` is genuinely live on this workstation.
- Verified the image default is no longer broken by a non-existent model name.

## Verification

- `mvn -Dtest=AdminAiServiceImplTest test`
- `powershell -ExecutionPolicy Bypass -File scripts/local/smoke-phase-22-ai-platform-verification.ps1 -Scope workspace`

## Notes

- Bailian image and TTS are still honest non-pass states because the current vendor response is `Model.AccessDenied`.
- That is a vendor-entitlement limitation, not a local broken default anymore.
