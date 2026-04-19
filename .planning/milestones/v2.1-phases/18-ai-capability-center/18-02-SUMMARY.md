## Phase 18.02 Summary

- Replaced the thin AI admin backend service with a real governance service covering overview, provider CRUD, provider test, policy CRUD, quota CRUD, prompt-template CRUD, generation jobs, candidate finalization, and restore flows.
- Added DashScope provider gateway support for text, image, and TTS foundation calls.
- Added local smoke coverage for the Phase 18 admin backend on `8081`.
- Added focused backend tests for secret crypto and governance throttle behavior.
