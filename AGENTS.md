<!-- GSD:project-start source:PROJECT.md -->
## Project

**Trip of Macau**

Trip of Macau is a brownfield travel mini-program plus admin platform for story-driven city exploration, map discovery, check-ins, collectibles, tips, and operations tooling. The immediate project is to replace the mini-program's mock-driven runtime with a real public backend in `packages/server`, while making the `/admin` control plane the authoritative place to configure content, runtime settings, and media used by the mini-program.

**Core Value:** Admins can configure the live mini-program experience end-to-end, and the mini-program consumes that managed data reliably through real backend APIs instead of mocks.

### Constraints

- **Tech stack**: Preserve the current brownfield stack: Taro/React mini-program, Spring Boot + MyBatis-Plus public/admin backends, existing admin UI, and current local Docker-based MySQL/Mongo setup.
- **Backend ownership**: `packages/server` is the primary public API backend for the mini-program and is the main implementation target for this work.
- **Admin ownership**: `/admin` must remain the authoritative write/control surface for mini-program-facing content, settings, and assets.
- **Database**: MySQL is the primary operational data store for this cutover because the user explicitly wants seeded mock data and fully connected admin/public behavior around the local MySQL setup.
- **Media**: File/image uploads must go through backend APIs into Tencent COS, with automatic key generation and metadata persistence.
- **Security**: Secrets must be consumed from local environment or runtime configuration, never hardcoded into tracked files or planning docs.
- **Verification**: Interfaces must be runnable and actually tested locally against real services before the work is considered complete.
- **Quality attributes**: High availability, ease of use, extensibility, and performance are first-class constraints, not afterthoughts.
<!-- GSD:project-end -->

<!-- GSD:stack-start source:codebase/STACK.md -->
## Technology Stack

## Repository Shape
- Monorepo-style repository without a shared root package manifest or workspace manager.
- Main product areas live in `packages/client`, `packages/server`, and `packages/admin`.
- Supporting material lives in `docs`, local bootstrap scripts live in `scripts/local`, and generated planning output now lives in `.planning/codebase`.
## Package Inventory
| Area | Path | Primary stack | Notes |
| --- | --- | --- | --- |
| Mini-program client | `packages/client` | Taro 3.6.23, React 18, TypeScript, Zustand, SCSS, Webpack 5 | WeChat-oriented front end with mock-heavy gameplay logic and map UI. |
| Public API backend | `packages/server` | Spring Boot 3.2.4, Java 17, MyBatis-Plus, MySQL, Redis, Springdoc | Public API for POIs, stories, users, stats, and trigger logs. |
| Admin UI | `packages/admin/aoxiaoyou-admin-ui` | React 18, Vite 6, TypeScript, Ant Design Pro, React Query, Zustand | Hash-routed SPA mounted under `/admin/`. |
| Admin backend | `packages/admin/aoxiaoyou-admin-backend` | Spring Boot 3.2.4, Java 17, Spring Security, MyBatis-Plus, MySQL, MongoDB, Springdoc | Admin API for map/content/system operations plus AI configuration views. |
## Frontend Runtime
- `packages/client/package.json` uses Taro build targets such as `build:weapp`, `build:h5`, and `dev:weapp`.
- `packages/client/config/index.js` configures `framework: 'react'` and `compiler: 'webpack5'`.
- `packages/client/src/app.config.ts` defines the WeChat page registry, tab bar, location permissions, and background location usage.
- `packages/client/src/app.ts` bootstraps local game state and relies on `wx.*` APIs during launch.
- Styling is primarily SCSS in `packages/client/src/styles` and per-page `index.scss` files.
## Admin UI Runtime
- `packages/admin/aoxiaoyou-admin-ui/package.json` uses Vite and `@vitejs/plugin-react-swc`.
- `packages/admin/aoxiaoyou-admin-ui/src/main.tsx` wraps the app with Ant Design `ConfigProvider`, `HashRouter`, and a custom theme token.
- `packages/admin/aoxiaoyou-admin-ui/vite.config.ts` serves the app at `/admin/` and proxies `/api` in development.
- TypeScript is configured in `packages/admin/aoxiaoyou-admin-ui/tsconfig.json` with `strict: false`.
## Backend Runtime
- Both Java services target Java 17 via `packages/server/pom.xml` and `packages/admin/aoxiaoyou-admin-backend/pom.xml`.
- Both use Spring Boot 3.2.4 and MyBatis-Plus 3.5.6.
- Both expose OpenAPI docs through Springdoc at `/v3/api-docs` and `/swagger-ui.html`.
- `packages/server/src/main/resources/application.yml` configures MySQL, Redis, JWT, multipart upload, and application-specific map/check-in settings.
- `packages/admin/aoxiaoyou-admin-backend/src/main/resources/application.yml` configures MySQL, MongoDB, JWT, multipart upload, and profile grouping for CloudBase deployments.
## Data and Infrastructure Tooling
- Local infrastructure is bootstrapped by `docker-compose.local.yml`.
- MySQL initialization scripts live in `scripts/local/mysql/init`.
- Mongo initialization scripts live in `scripts/local/mongo/init`.
- Container images referenced locally are `mysql:8.0` and `mongo:7.0`.
- `packages/server/Dockerfile` and `packages/admin/aoxiaoyou-admin-backend/Dockerfile` indicate containerized backend deployment paths.
## Dependencies Worth Knowing
- Client map/gameplay code centers on `packages/client/src/services/gameService.ts`.
- Admin data fetching is standardized through Axios in `packages/admin/aoxiaoyou-admin-ui/src/utils/request.ts`.
- JWT handling for admin auth lives in `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/util/JwtUtil.java`.
- MyBatis pagination and audit field filling are configured in `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/config/MybatisPlusConfig.java`.
## Operational Notes
- Source tree size is uneven: `packages/client/src` has about 162 files, `packages/server/src/main/java` about 45 files, `packages/admin/aoxiaoyou-admin-ui/src` about 35 files, and `packages/admin/aoxiaoyou-admin-backend/src/main/java` about 145 files.
- The repo contains checked-in build artifacts and local runtime outputs such as `packages/admin/aoxiaoyou-admin-backend/target`, `packages/admin/aoxiaoyou-admin-ui/dist`, and assorted `*.log` files, which affect repository hygiene.
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

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
- The codebase relies heavily on manual verification flows, especially through the admin test console feature and mock gameplay data.
## Deviations and Inconsistencies
- The mini-program mixes mock-first gameplay state with live-service API wrappers, so the source of truth changes by feature.
- Repository hygiene is inconsistent because build outputs, runtime logs, and local helper files are present alongside source.
- Some displayed Chinese text appeared garbled in terminal reads, which suggests an encoding mismatch in tooling or file-history handling and is worth watching when editing content-heavy files.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

## High-Level Topology
- The repository contains two user-facing application surfaces and two Java backend services.
- `packages/client` is the traveler-facing WeChat mini-program.
- `packages/server` is the public API for the traveler experience.
- `packages/admin/aoxiaoyou-admin-ui` is the operations/admin SPA.
- `packages/admin/aoxiaoyou-admin-backend` is the admin API and management backend.
## Core Architectural Style
- Both Java services follow a conventional Spring layered architecture:
- Both services use MyBatis-Plus for data access rather than Spring Data repositories.
- Both services wrap responses in a common response envelope via `common/api/ApiResponse.java` and `PageResponse.java`.
## Public App Flow
- App bootstrap begins in `packages/client/src/app.ts`.
- Navigation and page registration are centralized in `packages/client/src/app.config.ts`.
- Game state is primarily local-first and centralized in `packages/client/src/services/gameService.ts`.
- UI pages pull derived state from `gameService.ts` instead of directly hitting a backend for every user action.
- `packages/client/src/components/PageShell.tsx` applies cross-page display preferences such as interface mode, font scaling, and high-contrast class names.
## Map and Gameplay Flow
- The map screen in `packages/client/src/pages/map/index.tsx` is the heaviest interaction surface.
- It combines:
- Indoor navigation is a separate page in `packages/client/src/pages/map/indoor/index.tsx` that renders tile images and simulates AR localization.
- Asset indirection for remote marker/tabbar assets is centralized in `packages/client/src/constants/assetUrls.ts`.
## Admin UI Flow
- Admin entry is `packages/admin/aoxiaoyou-admin-ui/src/main.tsx`.
- Routing is defined in `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`.
- The app uses a protected route gate that:
- Layout composition is handled by `packages/admin/aoxiaoyou-admin-ui/src/layouts/DefaultLayout.tsx`.
- Feature screens are grouped by business domain under `packages/admin/aoxiaoyou-admin-ui/src/pages`.
## Admin Backend Flow
- Admin authentication is implemented as a custom MVC interceptor in `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/AdminAuthInterceptor.java`.
- Spring Security exists but is reduced to stateless plus `permitAll()` in `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/SecurityConfig.java`.
- The actual authorization boundary therefore depends on MVC path interception configured in `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/WebConfig.java`.
- Domain services then execute query-and-transform logic, often mapping entities directly to DTOs inside service implementations such as `AdminCityServiceImpl.java` and `AdminAiServiceImpl.java`.
## Public Backend Flow
- Public API controllers in `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller` call service interfaces.
- Service implementations such as `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl/PoiServiceImpl.java` use MyBatis-Plus query wrappers directly against mapper interfaces.
- Cross-cutting concerns include pagination, JSON serialization, global exception handling, CORS, and OpenAPI config under `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common`.
## State Management Patterns
- Mini-program UI state mixes React local state with persisted gameplay state in `gameService.ts`.
- There is also a Zustand store in `packages/client/src/store/userStore.ts`, but much of the current behavior still flows through manual `wx` storage reads/writes.
- Admin UI uses lightweight Zustand auth state in `packages/admin/aoxiaoyou-admin-ui/src/stores/auth.ts`.
- Admin server-side state remains request-scoped and database-backed, with JWT-derived user context attached to the request.
## Architectural Oddities
- The public backend `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/config/WebConfig.java` references `AdminAuthInterceptor`, but no matching class exists under `packages/server/src/main/java`; that coupling appears accidental or incomplete.
- The mini-program currently mixes live API wrappers in `packages/client/src/services/api.ts` with extensive mock/stateful simulation in `packages/client/src/services/gameService.ts`, so the effective runtime architecture is partly local app simulation and partly remote-service-ready scaffolding.
<!-- GSD:architecture-end -->

<!-- GSD:skills-start source:skills/ -->
## Project Skills

No project skills found. Add skills to any of: `.claude/skills/`, `.agents/skills/`, `.cursor/skills/`, or `.github/skills/` with a `SKILL.md` index file.
<!-- GSD:skills-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/gsd-quick` for small fixes, doc updates, and ad-hoc tasks
- `/gsd-debug` for investigation and bug fixing
- `/gsd-execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/gsd-profile-user` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
