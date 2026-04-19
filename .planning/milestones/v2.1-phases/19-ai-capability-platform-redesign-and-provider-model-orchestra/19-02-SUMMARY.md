## Phase 19.02 Summary

- Added provider-platform templates, normalized inventory records, sync-job history, and masked credential handling for the AI platform backend.
- Implemented provider onboarding, provider test, and inventory sync flows for the supported template set, with local MySQL seed repair for live defaults.
- Fixed DashScope compatible-mode handling so provider reads and inventory defaults now align with the real Bailian model inventory.
- Removed tracked fallback secrets for AI secret encryption and made the backend consume `APP_AI_SECRET_ENCRYPTION_PASSWORD` and `APP_AI_SECRET_ENCRYPTION_SALT` from runtime environment instead.
