# Integrations

## User-Facing Service Boundaries

- The mini-program client talks to the public API base defined in `packages/client/src/services/api.ts`.
- Development defaults in `packages/client/config/dev.js` set `USE_MOCK: 'true'`, so many client flows run against local mock state instead of live services.
- Production defaults in `packages/client/config/prod.js` point the client to `https://api.tripofmacau.com/api/v1`.
- The admin UI points to `VITE_API_BASE_URL` from `packages/admin/aoxiaoyou-admin-ui/.env.local` or `packages/admin/aoxiaoyou-admin-ui/.env.production`.

## Databases

### MySQL

- Public backend datasource is configured in `packages/server/src/main/resources/application.yml`.
- Admin backend datasource is configured per environment in:
  - `packages/admin/aoxiaoyou-admin-backend/src/main/resources/application.yml`
  - `packages/admin/aoxiaoyou-admin-backend/src/main/resources/application-local.yml`
  - `packages/admin/aoxiaoyou-admin-backend/src/main/resources/application-cloudbase.yml`
  - `packages/admin/aoxiaoyou-admin-backend/src/main/resources/application-prod.yml`
- Local bootstrap SQL lives under `scripts/local/mysql/init`.

### Redis

- Redis is configured only in the public backend via `packages/server/src/main/resources/application.yml` and `packages/server/src/main/resources/application-dev.yml`.
- No tracked service implementation inspected during mapping showed an active Redis usage site, so Redis currently looks provisioned ahead of fuller use.

### MongoDB

- MongoDB is configured only in the admin backend.
- `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/MongoConfig.java` creates collections such as `storyline_documents`, `poi_rule_documents`, `ai_policy_documents`, `user_event_logs`, `campaign_documents`, and `indoor_anchor_documents`.
- Local Mongo initialization scripts live under `scripts/local/mongo/init`.

## Cloud and CDN Services

- `packages/client/src/constants/assetUrls.ts` points mini-program assets to Tencent COS under `https://tripofmacau-1301163924.cos.ap-hongkong.myqcloud.com/miniapp/assets`.
- `README.md` and `packages/admin/aoxiaoyou-admin-ui/.env.production` reference a CloudBase-hosted admin API and static admin deployment.
- `packages/server/cloudbaserc.json` and `packages/admin/aoxiaoyou-admin-backend/cloudbaserc.json` suggest CloudBase deployment metadata for the Java services.

## Platform APIs

- The mini-program depends heavily on WeChat runtime APIs through `wx.*` and Taro wrappers.
- `packages/client/src/app.config.ts` requests `scope.userLocation` plus background location mode.
- `packages/client/src/app.ts` reads device/window/app metadata via WeChat APIs.
- `packages/client/src/pages/map/index.tsx` and `packages/client/src/pages/map/indoor/index.tsx` use location, compass, phone, toast, action sheet, and audio behaviors exposed through Taro/WeChat.

## HTTP API Surfaces

- Public API controllers live in `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller`.
- Example public endpoints:
  - `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/PoiController.java`
  - `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/StoryLineController.java`
  - `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller/UserController.java`
- Admin API controllers live in `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller`.
- Example admin endpoint groups:
  - Map management in `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminMapController.java`
  - AI provider/policy/log management in `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller/AdminAiController.java`
  - RBAC, collectibles, indoor maps, operations, storylines, and test tools under adjacent controller classes in the same package.

## Auth and Session Flows

- Mini-program HTTP requests add a bearer token from local storage in `packages/client/src/services/api.ts`.
- Admin UI attaches bearer tokens in `packages/admin/aoxiaoyou-admin-ui/src/utils/request.ts`.
- Admin backend validates JWTs through `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config/AdminAuthInterceptor.java`.
- JWT creation and verification logic lives in `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/util/JwtUtil.java`.

## Documentation and API Discovery

- Public backend OpenAPI config lives in `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/config/OpenApiConfig.java`.
- Both Java backends enable Swagger UI endpoints in their Spring configuration.
- The repo also contains product and interface specifications under `docs`, including admin API and technical design documents.

## Sensitive or Local-Only Integration Files

- An untracked local config file at `packages/client/opencode.json` contains third-party model provider settings and an API credential; it should be treated as local-only and excluded from generated documentation and commits.
- Admin UI environment files at `packages/admin/aoxiaoyou-admin-ui/.env.local` and `packages/admin/aoxiaoyou-admin-ui/.env.production` define backend targets and represent deployment coupling that is outside typed source code.
