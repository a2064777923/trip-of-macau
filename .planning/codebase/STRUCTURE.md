# Structure

## Top-Level Layout

```text
trip-of-macau/
  .codex/                      Codex skills, workflows, and local tooling
  .planning/codebase/          Generated codebase map documents
  docs/                        Product, API, UI, and technical design docs
  packages/
    client/                    WeChat mini-program
    server/                    Public Java backend
    admin/
      aoxiaoyou-admin-ui/      Admin SPA
      aoxiaoyou-admin-backend/ Admin Java backend
  scripts/local/               Local database init and startup helpers
  docker-compose.local.yml     Local MySQL and Mongo orchestration
```

## Mini-Program Layout

- App entry and config:
  - `packages/client/src/app.ts`
  - `packages/client/src/app.config.ts`
- Feature pages:
  - `packages/client/src/pages/index`
  - `packages/client/src/pages/map`
  - `packages/client/src/pages/discover`
  - `packages/client/src/pages/story`
  - `packages/client/src/pages/stamps`
  - `packages/client/src/pages/rewards`
  - `packages/client/src/pages/profile`
  - `packages/client/src/pages/settings`
  - `packages/client/src/pages/senior`
  - `packages/client/src/pages/tips`
- Reusable support code:
  - `packages/client/src/components`
  - `packages/client/src/services`
  - `packages/client/src/store`
  - `packages/client/src/utils`
  - `packages/client/src/types`
  - `packages/client/src/constants`
- Static assets:
  - `packages/client/src/assets/indoor`
  - `packages/client/src/assets/poi`
  - `packages/client/src/assets/tabbar`
- Build configuration:
  - `packages/client/config/index.js`
  - `packages/client/config/dev.js`
  - `packages/client/config/prod.js`

## Public Backend Layout

- Main application entry: `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/TripOfMacauServerApplication.java`
- Shared support packages:
  - `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/api`
  - `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/config`
  - `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/common/exception`
- Business packages:
  - `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/controller`
  - `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service`
  - `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/service/impl`
  - `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/mapper`
  - `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/entity`
  - `packages/server/src/main/java/com/aoxiaoyou/tripofmacau/dto`
- Config and tests:
  - `packages/server/src/main/resources`
  - `packages/server/src/test/java`

## Admin UI Layout

- Entry files:
  - `packages/admin/aoxiaoyou-admin-ui/src/main.tsx`
  - `packages/admin/aoxiaoyou-admin-ui/src/App.tsx`
- Layout and infrastructure:
  - `packages/admin/aoxiaoyou-admin-ui/src/layouts`
  - `packages/admin/aoxiaoyou-admin-ui/src/services`
  - `packages/admin/aoxiaoyou-admin-ui/src/stores`
  - `packages/admin/aoxiaoyou-admin-ui/src/utils`
  - `packages/admin/aoxiaoyou-admin-ui/src/types`
- Feature folders:
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/Dashboard`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/MapSpace`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/POIManagement`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/StorylineManagement`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/OperationsManagement`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/System`
  - `packages/admin/aoxiaoyou-admin-ui/src/pages/TestAccount`

## Admin Backend Layout

- Main entry: `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/AoxiaoyouAdminApplication.java`
- Shared support:
  - `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/api`
  - `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/config`
  - `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/common/exception`
- Business/domain areas:
  - `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/controller`
  - `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service`
  - `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/service/impl`
  - `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/mapper`
  - `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/entity`
  - `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/dto`
  - `packages/admin/aoxiaoyou-admin-backend/src/main/java/com/aoxiaoyou/admin/util`
- Resources and tests:
  - `packages/admin/aoxiaoyou-admin-backend/src/main/resources`
  - `packages/admin/aoxiaoyou-admin-backend/src/test/java`

## Supporting and Generated Content

- Product and architecture references live in `docs`, including admin API specs, UI design specs, SQL, and prototype artifacts.
- Local helper scripts live in `scripts/local/start-admin-backend.cmd` and `scripts/local/start-admin-ui.cmd`.
- Checked-in or local-generated artifacts currently visible in the repo include:
  - `packages/admin/aoxiaoyou-admin-backend/target`
  - `packages/admin/aoxiaoyou-admin-ui/dist`
  - package-local `node_modules`
  - assorted `*.log` files near package roots

## Naming and Grouping Tendencies

- Java packages follow package-per-layer naming rather than bounded-context submodules.
- React feature folders in the admin UI are business-domain named.
- Mini-program pages generally follow Taro conventions with `pages/<feature>/index.tsx` and matching `index.scss`.
- There is no evidence of a shared internal library package; duplication across public and admin Java backends is handled by parallel implementations rather than extraction.
