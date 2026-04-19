---
phase: 24
slug: ai-tts-voice-library-and-voice-cloning
status: verified
threats_open: 0
asvs_level: 2
created: 2026-04-19
---

# Phase 24 - Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Admin UI <-> Admin Backend voice workbench | Operators use `/ai/voices` and the creative workbench to sync voices, preview TTS, and submit clone jobs. | providerId, modelCode, voiceCode, scriptText, languageCode, sourceAssetId, sourceUrl, previewText |
| Admin Backend <-> Bailian / DashScope endpoints | The backend calls official voice catalog, TTS preview, clone create, clone refresh, and clone delete endpoints. | encrypted provider API key at runtime, voice payload JSON, clone source URL, provider response payloads |
| Admin Backend <-> COS / MySQL | Preview audio and generated assets are persisted to COS while provider inventory and clone ownership are stored in MySQL. | audio bytes, canonical asset URL, ownerAdminId, clone status, masked provider metadata |
| Admin Backend <-> Admin observability surfaces | Overview, logs, and generation-job detail pages expose operational traces back to operators. | adminOwnerId/adminOwnerName, masked userOpenid, input hash, output summary, prompt text for owned jobs |

---

## Threat Register

Phase 24 plan files did not provide a dedicated `<threat_model>` block, so this register is derived from the implemented control plane, plan intent, and verified code paths.

| Threat ID | Category | Component | Disposition | Mitigation | Status |
|-----------|----------|-----------|-------------|------------|--------|
| AI24-SSRF-01 | SSRF / outbound URL abuse | provider base URL, provider runtime calls, asset downloads, voice clone source URL | mitigate | `AiOutboundUrlGuard` validates configured provider base URLs and runtime outbound asset/API URLs as public HTTPS only, blocks loopback/private/special-use destinations, and `DashScopeProviderGateway` disables redirects and enforces download size limits. This pass also closes the clone-source gap: `AdminAiServiceImpl.resolveVoiceSourceUrl(...)` now routes both `sourceAssetId -> canonicalUrl` and direct `sourceUrl` through `AiOutboundUrlGuard.validatePublicSourceUrl(...)` before the URL is forwarded to provider clone payloads. Verified in `AiOutboundUrlGuard.java`, `AdminAiServiceImpl.java`, `AiOutboundUrlGuardTest.java`, `AdminAiServiceImplTest.java`, and targeted backend test run `mvn "-Dtest=AiOutboundUrlGuardTest,AdminAiServiceImplTest,DashScopeProviderGatewayTest,AiSecretCryptoServiceTest" test`. | closed |
| AI24-AUTHZ-02 | Authorization / tenant isolation | cloned voice inventory and generation-job visibility | mitigate | `AdminAiServiceImpl.listVoices(...)` filters custom voices to super admins or the owning admin, `assertVoiceAccessible(...)` blocks refresh/delete on another operator's clone, and generation-job responses are filtered through `canAccessJob(...)`. Verified in `AdminAiServiceImpl.java`, plus ownership regression tests in `AdminAiServiceImplTest.java` for cross-owner rejection and super-admin override. | closed |
| AI24-SECRETS-03 | Secret exposure | provider credential storage and provider read responses | mitigate | `fillProvider(...)` encrypts API key / secret with `AiSecretCryptoService` and stores only masked variants for responses; `mapProvider(...)` returns `apiKeyMasked` / `apiSecretMasked` instead of raw credentials; `decryptRequiredApiKey(...)` is only used server-side when making provider calls. Verified in `AdminAiServiceImpl.java`, `AiSecretCryptoService.java`, `AdminAiProviderResponse.java`, and `AiSecretCryptoServiceTest.java`. | closed |

*Status: open or closed*
*Disposition: mitigate (implementation required) or accept (documented risk) or transfer (third-party)*

---

## Accepted Risks Log

No accepted risks.

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-04-19 | 3 | 2 | 1 | Codex (`/gsd-secure-phase 24`) |
| 2026-04-19 | 3 | 3 | 0 | Codex (`sourceUrl` guard closure + focused backend tests) |

Supplemental notes:
- Local code evidence was used directly because this phase had no prior SECURITY artifact and no explicit threat-model block in `24-01-PLAN.md`.
- The previously open item on clone `sourceUrl` handling is now closed by backend-side public-URL validation before provider submission.

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-04-19
