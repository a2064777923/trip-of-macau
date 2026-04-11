# Conventions

## General Style

- The repo is polyglot but consistently domain-oriented: React/Taro on the frontend and Spring layered code on the backend.
- There is no enforced repository-wide formatter or shared lint entrypoint at the root.
- Each package carries its own conventions and toolchain.

## Mini-Program Conventions

- Taro pages follow `pages/<feature>/index.tsx` with sibling `index.scss`.
- Shared UI wrappers live in `packages/client/src/components`, for example `PageShell.tsx`.
- Domain state and business helpers are concentrated in `packages/client/src/services/gameService.ts` rather than split into many small service modules.
- Type definitions live under `packages/client/src/types`.
- Persistence frequently uses `wx.setStorageSync` and `wx.getStorageSync` directly.
- Components use functional React style with hooks; some screens are very large and mix view logic with orchestration logic.

## Admin UI Conventions

- The admin UI uses functional React components and colocates feature pages under `packages/admin/aoxiaoyou-admin-ui/src/pages`.
- API access is centralized through `packages/admin/aoxiaoyou-admin-ui/src/utils/request.ts`.
- Auth state is kept minimal in Zustand via `packages/admin/aoxiaoyou-admin-ui/src/stores/auth.ts`.
- Routing is explicit in `packages/admin/aoxiaoyou-admin-ui/src/App.tsx` instead of file-system routing.
- TypeScript is present but not strict, which encourages looser DTO handling and some nullable assumptions.

## Java Backend Conventions

- Controllers return `ApiResponse<T>` and paginated endpoints return `PageResponse<T>`.
- DTOs are clearly separated into request and response packages.
- Services are declared as interfaces in `service` and implemented in `service/impl`.
- MyBatis-Plus mapper interfaces are thin and service implementations build most query logic with `LambdaQueryWrapper`.
- Lombok is used pervasively for constructor injection and data models.
- Naming is conventional Spring Boot naming: `XController`, `XService`, `XServiceImpl`, `XMapper`, `XResponse`, `XRequest`.

## Error Handling

- Public backend uses a typed `BusinessException` and a `GlobalExceptionHandler` under `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/exception`.
- Admin backend mirrors the same exception structure under `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/exception`.
- Frontend code often handles failure with toasts, console warnings, and storage fallback rather than richer typed error recovery.

## Configuration Conventions

- Spring services prefer environment-variable-backed YAML configuration with sensible local defaults.
- Admin backend splits runtime config by profile using `application-local.yml`, `application-cloudbase.yml`, and `application-prod.yml`.
- Taro client toggles mock/live behavior through `defineConstants` in `packages/client/config/dev.js` and `packages/client/config/prod.js`.
- Admin UI reads Vite env vars for API targets and base routing behavior.

## Security and Auth Conventions

- Admin auth is bearer-token based, with token persistence handled in browser storage utilities and request interceptors.
- Security enforcement is interceptor-driven rather than annotation-heavy or role-policy-heavy in the inspected files.
- Public client API wrapper also assumes bearer-token storage, but the inspected mini-program flow still leans heavily on local mock state.

## Testing and Verification Norms

- Automated tests are minimal and currently limited to context-load tests in:
  - `packages/server/src/test/java/com/aoxiaoyou/tripofmacau/TripOfMacauServerApplicationTests.java`
  - `packages/admin/aoxiaoyou-admin-backend/src/test/java/com/aoxiaoyou/admin/AoxiaoyouAdminApplicationTests.java`
- The codebase relies heavily on manual verification flows, especially through the admin test console feature and mock gameplay data.

## Deviations and Inconsistencies

- The mini-program mixes mock-first gameplay state with live-service API wrappers, so the source of truth changes by feature.
- Repository hygiene is inconsistent because build outputs, runtime logs, and local helper files are present alongside source.
- Some displayed Chinese text appeared garbled in terminal reads, which suggests an encoding mismatch in tooling or file-history handling and is worth watching when editing content-heavy files.
