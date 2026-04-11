# Testing

## Current Automated Coverage

- Automated coverage is minimal.
- The only tracked tests found during mapping are Spring Boot context smoke tests:
  - `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/TripOfMacauServerApplicationTests.java`
  - `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AoxiaoyouAdminApplicationTests.java`
- No tracked frontend unit tests, component tests, Playwright specs, or end-to-end suites were found.
- No `.github` workflow files were found, so there is no visible CI pipeline in-repo.

## How Verification Appears To Happen Today

- Public mini-program behavior is verified largely by running Taro builds and exercising pages manually.
- Admin UI behavior is likely verified by running `npm run dev` in `packages/admin/aoxiaoyou-admin-ui` and using the live screens.
- Admin backend and public backend can be run locally with Maven and validated through Swagger UI or frontend consumers.
- `scripts/local/start-admin-backend.cmd` and `scripts/local/start-admin-ui.cmd` exist to streamline local manual startup.

## Package-Level Commands

### Mini-program client

- Build and dev commands are defined in `packages/client/package.json`.
- Common commands include:
  - `npm run dev:weapp`
  - `npm run build:weapp`
  - `npm run build:h5`

### Admin UI

- Commands live in `packages/admin/aoxiaoyou-admin-ui/package.json`.
- Common commands include:
  - `npm run dev`
  - `npm run build`
  - `npm run lint`
  - `npm run type-check`

### Java backends

- Both backend packages use Maven via their `pom.xml` files.
- Standard verification would be based on:
  - `mvn test`
  - `mvn spring-boot:run`
  - package-specific smoke checks against Swagger or calling endpoints from the UI

## Manual Test Affordances In Product Code

- The public backend includes a `TestAccountController` in `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/TestAccountController.java`.
- The admin backend includes dedicated test-console request DTOs and controller endpoints under `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminTestConsoleController.java`.
- The admin UI exposes a `TestAccount` page with feature-specific components in `packages/admin/aoxiaoyou-admin-ui/src/pages/TestAccount`.
- The mini-program itself relies on seeded mock content in `packages/client/src/services/gameMock.ts` and simulation logic in `packages/client/src/services/gameService.ts`.

## Testing Gaps

- No automated contract tests exist between frontend clients and the Java APIs.
- No repository-level smoke script validates all four runtime surfaces together.
- No backend persistence integration tests were found for MySQL, MongoDB, Redis, or JWT auth paths.
- No frontend snapshot or unit tests were found for map interactions, page state, or admin routing/auth flows.

## Practical Implication

- Regressions are currently most likely to surface only through manual QA.
- The richest current verification surface is probably the admin test console plus the mock-heavy mini-program runtime, not true automated coverage.
- Any future refactor should assume a low safety net and should add tests close to the touched area before broader change waves.
