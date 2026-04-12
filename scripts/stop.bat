@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."
set "COMPOSE_FILE=docker-compose.yml"

echo ==========================================
echo GrowPath Server — остановка
echo ==========================================
echo.

cd /d "%PROJECT_ROOT%"

docker compose version >nul 2>&1
if errorlevel 1 (
    set "DOCKER_COMPOSE=docker-compose"
) else (
    set "DOCKER_COMPOSE=docker compose"
)

echo [INFO] Останавливаю контейнеры...
%DOCKER_COMPOSE% -f "%COMPOSE_FILE%" down --remove-orphans -t 5

if errorlevel 1 (
    echo [ERROR] docker compose down завершился с ошибкой.
    endlocal
    exit /b 1
)

echo.
echo [OK] Контейнеры остановлены
echo.

endlocal
exit /b 0
