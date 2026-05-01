@echo off
setlocal EnableDelayedExpansion

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%.."

echo ==========================================
echo GrowPath Server — развертывание (Windows)
echo ==========================================
echo.

where docker >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker не установлен.
    exit /b 1
)

docker compose version >nul 2>&1
if errorlevel 1 (
    docker-compose version >nul 2>&1
    if errorlevel 1 (
        echo [ERROR] Docker Compose не найден.
        exit /b 1
    )
    set "DOCKER_COMPOSE=docker-compose"
) else (
    set "DOCKER_COMPOSE=docker compose"
)

if not exist "%PROJECT_ROOT%\.env" (
    echo [WARNING] Нет .env — копирую из .env.example...
    if exist "%PROJECT_ROOT%\.env.example" (
        copy /Y "%PROJECT_ROOT%\.env.example" "%PROJECT_ROOT%\.env" >nul
        echo [OK] Создан .env — проверьте настройки.
        pause
    ) else (
        echo [ERROR] Нет .env.example
        exit /b 1
    )
)

cd /d "%PROJECT_ROOT%"
set "COMPOSE_FILE=docker-compose.yml"

set "DO_CLEAN=0"
set "DO_REBUILD=0"
set "DO_WAIT=1"
set "SKIP_TESTS=0"
for %%A in (%*) do (
    if /I "%%~A"=="clean" set "DO_CLEAN=1"
    if /I "%%~A"=="rebuild" set "DO_REBUILD=1"
    if /I "%%~A"=="no-wait" set "DO_WAIT=0"
    if /I "%%~A"=="skip-tests" set "SKIP_TESTS=1"
)

if "!DO_CLEAN!"=="1" (
    echo [INFO] Остановка контейнеров ^(clean^)...
    %DOCKER_COMPOSE% -f "%COMPOSE_FILE%" down
)

if "!SKIP_TESTS!"=="1" (
    echo [INFO] Пропуск тестов при сборке...
    set "BUILD_ARGS=--build-arg GRADLE_ARGS=-x test"
) else (
    set "BUILD_ARGS="
)

if "!DO_REBUILD!"=="1" (
    echo [INFO] Сборка без кэша ^(rebuild — долго^)...
    %DOCKER_COMPOSE% -f "%COMPOSE_FILE%" build --no-cache !BUILD_ARGS!
    echo [INFO] Запуск контейнеров из свежих образов...
    if "!DO_WAIT!"=="1" (
        %DOCKER_COMPOSE% -f "%COMPOSE_FILE%" up -d --wait
        if errorlevel 1 (
            echo [WARN] --wait не поддерживается. Запуск без ожидания...
            %DOCKER_COMPOSE% -f "%COMPOSE_FILE%" up -d
        )
    ) else (
        %DOCKER_COMPOSE% -f "%COMPOSE_FILE%" up -d
    )
) else (
    echo [INFO] Сборка с кэшем + запуск ^(быстрый режим^). Полная пересборка: deploy.bat rebuild
    if "!DO_WAIT!"=="1" (
        %DOCKER_COMPOSE% -f "%COMPOSE_FILE%" up -d --build !BUILD_ARGS! --wait
        if errorlevel 1 (
            echo [WARN] --wait не поддерживается. Запуск без ожидания...
            %DOCKER_COMPOSE% -f "%COMPOSE_FILE%" up -d --build !BUILD_ARGS!
        )
    ) else (
        %DOCKER_COMPOSE% -f "%COMPOSE_FILE%" up -d --build !BUILD_ARGS!
    )
)

echo.
echo ==========================================
echo [OK] Готово
echo ==========================================
echo   API Gateway:   http://localhost:8080
echo   Trainee:       http://localhost:8081
echo   Notification:  http://localhost:8082
echo   Keycloak:      http://localhost:8090
echo.
echo Подсказки:
echo   Быстрый цикл:      scripts\deploy.bat
echo   Без тестов:        scripts\deploy.bat skip-tests
echo   С нуля контейнеры: scripts\deploy.bat clean
echo   Полная пересборка: scripts\deploy.bat rebuild
echo   Старый Compose:    scripts\deploy.bat no-wait
echo   Статус:  %DOCKER_COMPOSE% -f %COMPOSE_FILE% ps
echo.

endlocal
exit /b 0
