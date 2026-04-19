# Phase 24: AI TTS Voice Library and Voice Cloning - Research

## Confirmed External Facts

- Aliyun non-realtime CosyVoice TTS supports structured request fields including `voice`, `format`, `sample_rate`, `language_hints`, `instruction`, `rate`, `pitch`, and `volume`.
- `language_hints` supports at least `zh`, `en`, `fr`, `de`, `ja`, `ko`, `ru`, `pt`, `th`, `id`, and `vi`; the current documentation notes that only the first array element is processed.
- The official Bailian voice-list page exposes model sections such as `CosyVoice-v3-Flash 大模型` and embeds preview audio URLs in `<audio>` tags.
- The official clone/design API uses `POST https://dashscope.aliyuncs.com/api/v1/services/audio/tts/customization`.
- The clone API supports actions including `create_voice`, `list_voice`, `query_voice`, and `delete_voice`.
- Created clone voices return a `voice_id` that can be used directly as the `voice` parameter in TTS requests, and synthesis must use the same target model that created the voice.

## Local Product Constraints

- Admin backend on `8081` is the live verification target.
- Existing TTS generation already works end-to-end with Bailian after the `longanyang` default fix.
- The current admin workbench still relies on freeform `requestPayloadJson` for TTS and has no voice-library UX.
- `ai_provider_inventory` already stores provider inventory and is the most pragmatic canonical store to extend for voice records.

## Locked Scope For Execution

- Add a dedicated voice-library/admin-voice-workbench page inside AI Capability Center.
- Add backend voice management APIs and reuse provider inventory as the storage layer.
- Upgrade the creative workbench TTS flow into structured fields with dynamic voice loading.
- Verify voice sync, preview, clone, and selected-voice generation on the running admin stack.
