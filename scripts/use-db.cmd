@echo off
if /I "%~1"=="local" goto local
if /I "%~1"=="cloud" goto cloud

echo Usage: call scripts\use-db.cmd local ^| cloud
echo.
echo This verifies the connection and sets DB_PROFILE for the current CMD window.
exit /b 2

:local
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0db-shell.ps1" local -Check
if errorlevel 1 exit /b %errorlevel%
set "DB_PROFILE=local-db"
echo Selected: local-db - scenario writes will target local PostgreSQL.
goto done

:cloud
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0db-shell.ps1" cloud -Check
if errorlevel 1 exit /b %errorlevel%
set "DB_PROFILE=cloud-db"
echo Selected: cloud-db - scenario writes will target CockroachDB Cloud.

:done
echo Run gradlew.bat bootRun from this same CMD window.
