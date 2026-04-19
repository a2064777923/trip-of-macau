# Phase 24: AI TTS Voice Library and Voice Cloning - Context

**Gathered:** 2026-04-19
**Status:** Ready for execution

<domain>
## Phase Boundary

Phase 24 extends the existing AI capability platform with a real voice-library control plane for TTS. The admin must be able to:

- choose a TTS model and load all supported system voices for that model
- filter voices by supported language and inspect preview samples
- control TTS output language explicitly instead of relying on prompt guessing
- create, refresh, and delete custom cloned voices
- reuse the selected voice directly inside the AI creative workbench for narration and speech generation

This phase stays inside the admin AI capability center and the admin backend. It does not yet deliver full mini-program AI runtime acceptance, and it does not reopen the broader capability-domain information architecture beyond what is necessary to add a dedicated voice workspace.
</domain>

<decisions>
## Implementation Decisions

- **D-01:** Reuse `ai_provider_inventory` as the canonical registry for both TTS models and voice profiles instead of introducing a second parallel voice registry table.
- **D-02:** Voice profiles are represented as `inventory_type = voice`; model records remain `inventory_type = model`.
- **D-03:** System voices and custom cloned voices must both be queryable from the same admin voice API, but custom voices remain ownership-scoped for non-super-admin operators.
- **D-04:** The Bailian/DashScope voice catalog is the primary live provider path for this phase because the current workstation already uses it for real TTS verification.
- **D-05:** System voice synchronization should come from the official Aliyun voice-list documentation page, not from a hardcoded short list.
- **D-06:** Voice preview must work from the admin UI. Vendor demo URLs may be displayed when available, but the platform must also support on-demand preview synthesis for the selected voice.
- **D-07:** TTS generation requests must support structured fields for `voice`, `language_hints`, `instruction`, `rate`, `pitch`, `volume`, `format`, and `sample_rate`.
- **D-08:** Language choice is an explicit operator control. The UI should offer at least Mandarin, Cantonese, English, Portuguese, and the other languages supported by the selected voice/model metadata.
- **D-09:** Voice cloning is a first-class admin workflow with its own page and modal. Operators should be able to create a custom voice from an uploaded/public audio sample and then reuse it in the workbench.
- **D-10:** The admin AI workspace gets a dedicated subpage for voice operations, separate from the generic models page and separate from the generic creative-studio history page.
</decisions>

<specifics>
## Specific Ideas

- The user wants “choose voice model -> load all supported voices -> preview/listen/select”.
- The user also wants a place for “voice cloning” that stores custom voices as personal/admin-owned assets and lets them be selected later.
- The user explicitly wants language control for Mandarin, Cantonese, English, Portuguese, and similar languages.
- The best product shape is:
  - a dedicated AI workspace page: `音色與聲音工坊`
  - a structured TTS section inside `AiCreativeWorkbenchModal`
  - backend APIs for voice sync, voice list, preview, clone, refresh, and delete
</specifics>

<canonical_refs>
## Canonical References

- `.planning/ROADMAP.md`
- `.planning/STATE.md`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/AdminAiService.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl/AdminAiServiceImpl.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/ai/provider/DashScopeProviderGateway.java`
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity/AiProviderInventory.java`
- `packages/admin/aoxiaoyou-admin-ui/src/components/ai/AiCreativeWorkbenchModal.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/catalog.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/pages/AiCapabilityCenter/AiWorkspaceLayout.tsx`
- `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`
- `scripts/local/mysql/init/35-phase-19-ai-platform-orchestration.sql`

Official references used for this phase:

- `https://help.aliyun.com/zh/model-studio/non-realtime-cosyvoice-api`
- `https://help.aliyun.com/zh/model-studio/multimodal-timbre-list`
- `https://help.aliyun.com/zh/model-studio/cosyvoice-clone-design-api`
</canonical_refs>

<deferred>
## Deferred Ideas

- Full mini-program consumption of these new voice controls remains a later milestone acceptance item.
- Rich voice-design generation beyond clone/create management can be added later if the operator workflow needs it.
- Cross-provider voice-library normalization beyond Bailian can follow after the first live provider path is stable.
</deferred>

---

*Phase: 24-ai-tts-voice-library-and-voice-cloning*
*Context gathered: 2026-04-19*
