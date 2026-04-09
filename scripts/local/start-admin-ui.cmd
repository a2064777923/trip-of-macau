@echo off
setlocal
cd /d %~dp0..\..\packages\admin\aoxiaoyou-admin-ui
set "VITE_DEV_PROXY_TARGET=http://127.0.0.1:8081"
echo [admin-ui] Starting dev server on http://127.0.0.1:5173
echo [admin-ui] Proxy target: %VITE_DEV_PROXY_TARGET%
npm run dev -- --host 0.0.0.0 --port 5173
