# Phase 33 Verification

## Prerequisites

- Local MySQL is running with database `aoxiaoyou`.
- Admin backend is running at `http://127.0.0.1:8081`.
- Admin auth is available from one of:
  - `PHASE33_ADMIN_BEARER`
  - ignored `tmp-admin-login.json`
  - `PHASE33_ADMIN_USERNAME` and `PHASE33_ADMIN_PASSWORD`
  - local fallback `admin / admin123`

## Seed Import

Use `SOURCE` instead of PowerShell input redirection:

```powershell
$env:MYSQL_PWD='Abc123456'
D:\Software\mysql-8.0.41-winx64\bin\mysql.exe --default-character-set=utf8mb4 --user=root --host=127.0.0.1 --port=3306 --database=aoxiaoyou --execute="SOURCE D:/Archive/trip-of-macau/scripts/local/mysql/init/47-phase-33-story-material-package-model.sql"
D:\Software\mysql-8.0.41-winx64\bin\mysql.exe --default-character-set=utf8mb4 --user=root --host=127.0.0.1 --port=3306 --database=aoxiaoyou --execute="SOURCE D:/Archive/trip-of-macau/scripts/local/mysql/init/48-phase-33-flagship-material-assets.sql"
D:\Software\mysql-8.0.41-winx64\bin\mysql.exe --default-character-set=utf8mb4 --user=root --host=127.0.0.1 --port=3306 --database=aoxiaoyou --execute="SOURCE D:/Archive/trip-of-macau/scripts/local/mysql/init/49-phase-33-east-west-flagship-story.sql"
```

## Build And Compile

```powershell
mvn -q -DskipTests compile -f packages/admin/aoxiaoyou-admin-backend/pom.xml
npm run build --prefix packages/admin/aoxiaoyou-admin-ui
```

Expected:

- Maven exits `0`.
- Vite build exits `0`.
- Large chunk warning is acceptable for the current admin SPA.

## Live Smoke

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/local/smoke-phase-33-flagship-package.ps1
```

Expected final line:

```text
Phase 33 flagship package smoke passed
```

The smoke verifies:

- Admin API returns `east_west_war_and_coexistence_package`.
- Package detail has at least 40 items and includes Lottie, audio, and image/icon entries.
- Package detail exposes `historicalBasisZht` or `historicalBasisZh`.
- Package detail exposes `literaryDramatizationZht` or `literaryDramatizationZh`.
- MySQL has exactly five published chapters for `east_west_war_and_coexistence`.
- Every chapter has `experience_flow_id`.
- The storyline has at least 20 published exploration elements.
- Required finale reward/title rows have non-empty Traditional Chinese display text.

## Manual Admin Check

Open `/admin/#/content/material-packages`.

Expected:

- The sidebar contains `故事素材包`.
- The seeded package `東西方文明的戰火與共生` is visible.
- Counters for `素材`, `資產`, `故事物件`, `章節`, and `探索元素` render.
- The detail table includes `素材鍵`, `類型`, `章節`, `用途`, `資產 ID`, `COS 路徑`, `狀態`, and `來源`.
- Long paths are truncated with ellipsis and show full text on hover.
- `史實依據` and `文學演繹` are separate sections.
