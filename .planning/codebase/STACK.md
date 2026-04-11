# Stack

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
