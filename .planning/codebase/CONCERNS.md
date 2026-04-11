# Concerns

## Security Risks

- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/SecurityConfig.java` permits all requests at the Spring Security layer.
- Actual admin protection depends on `AdminAuthInterceptor` in `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/AdminAuthInterceptor.java`, which is narrower and easier to bypass accidentally than explicit security rules.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/WebConfig.java` allows CORS from any origin with all common methods and headers.
- An untracked local file, `packages/client/opencode.json`, contains a third-party API credential; it should stay ignored and should not be copied into any tracked docs, examples, or automation output.

## Architectural Fragility

- `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/config/WebConfig.java` references `AdminAuthInterceptor`, but no corresponding class exists under `packages/server/src/main/java`. This suggests either a compile-time issue, stale code, or an incomplete refactor.
- The mini-program currently mixes local mock gameplay orchestration in `packages/client/src/services/gameService.ts` with real HTTP wrappers in `packages/client/src/services/api.ts`, which makes it unclear which features are authoritative.
- There is no shared package for common DTOs, auth primitives, or infrastructure helpers across the two Java services, so duplication risk is high.

## Quality and Maintainability

- Automated test coverage is effectively absent beyond context-load smoke tests.
- Large UI files such as `packages/client/src/pages/map/index.tsx` combine rendering, geolocation, business rules, and interaction orchestration in a single component, which raises regression risk.
- The admin backend service layer often performs direct field-by-field entity mapping and mutation inside service methods, which is workable now but will become repetitive and error-prone as domains grow.
- `packages/admin/aoxiaoyou-admin-ui/tsconfig.json` has `strict: false`, reducing type safety in the admin surface.

## Repository Hygiene

- The repo contains generated/build output and runtime noise such as `packages/admin/aoxiaoyou-admin-backend/target`, `packages/admin/aoxiaoyou-admin-ui/dist`, package-local `node_modules`, and multiple log files.
- The working tree already contains unrelated local modifications and untracked assets outside the mapping output, which means future automation must avoid broad staging commands.
- No root workspace tool or shared build graph was found, so cross-package maintenance costs will rise as the repo grows.

## Operational Gaps

- Redis is configured in the public backend but no inspected implementation showed a clear active usage path.
- MongoDB is bootstrapped in the admin backend, but based on inspected files it still looks partly preparatory for future document-style features.
- No in-repo CI workflows were found, so there is no visible automated guardrail for linting, type-checking, or backend tests on change.

## Content and Encoding Risks

- Some Chinese strings appeared garbled when read in the current terminal environment. That may be a terminal encoding mismatch rather than corrupted source, but it increases editing risk for content-heavy files and documentation.
- Several environment and deployment details are spread across README text, `.env` files, Vite config, YAML profiles, and docs, which increases drift risk between intended and actual runtime behavior.
