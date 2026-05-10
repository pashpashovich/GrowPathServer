@echo off
setlocal EnableDelayedExpansion

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."

echo ==========================================
echo GrowPath Server — пересборка сервиса
echo ==========================================
echo.

if "%~1"=="" (
    echo [ERROR] Укажите имя сервиса: trainee-service, api-gateway или notification-service
    echo Пример: scripts\rebuild-service.bat trainee-service
    exit /b 1
)

set "SERVICE=%~1"

if not "%SERVICE%"=="trainee-service" if not "%SERVICE%"=="api-gateway" if not "%SERVICE%"=="notification-service" (
    echo [ERROR] Неизвестный сервис: %SERVICE%
    echo Доступные: trainee-service, api-gateway, notification-service
    exit /b 1
)

cd /d "%PROJECT_ROOT%"

echo [INFO] Шаг 1/3: Сборка Gradle проекта %SERVICE%...
call gradlew.bat :%SERVICE%:build -x test
if errorlevel 1 (
    echo [ERROR] Ошибка сборки Gradle
    exit /b 1
)

echo.
echo [INFO] Шаг 2/3: Пересборка Docker образа %SERVICE%...
docker compose build --no-cache %SERVICE%
if errorlevel 1 (
    echo [ERROR] Ошибка сборки Docker образа
    exit /b 1
)

echo.
echo [INFO] Шаг 3/3: Перезапуск контейнера %SERVICE%...
docker compose up -d %SERVICE%
if errorlevel 1 (
    echo [ERROR] Ошибка запуска контейнера
    exit /b 1
)

echo.
echo ==========================================
echo [OK] Сервис %SERVICE% пересобран и запущен
echo ==========================================
echo.
echo Проверить логи: docker compose logs -f %SERVICE%
echo.

endlocal
exit /b 0
