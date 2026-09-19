@echo off
REM Cake Delight End-to-End Authenticated API Verification Script (Windows)
REM
REM Verifies the CURRENT authenticated application flow: register -> login ->
REM JWT Bearer usage -> catalog -> basket -> checkout -> order history ->
REM ratings -> 401/403 RBAC -> admin CRUD -> notifications.
REM
REM The actual HTTP/JSON logic lives in test-flow.ps1 (companion script, uses
REM Invoke-WebRequest so JSON parsing is robust). The gateway derives trusted
REM user identity from the JWT itself; this script never sends spoofed
REM X-User-Id/X-User-Role/X-User-Name headers.

if "%BASE_URL%"=="" set "BASE_URL=http://localhost:8080"
if "%ADMIN_USERNAME%"=="" set "ADMIN_USERNAME=admin"
if "%ADMIN_PASSWORD%"=="" set "ADMIN_PASSWORD=Admin@12345"

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0test-flow.ps1"
exit /b %ERRORLEVEL%
