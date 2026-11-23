@echo off
setlocal EnableExtensions

rem Build a signed release Android App Bundle for SenSketch.
set "PROJECT_DIR=C:\app\AndroidStudioProjects\SenSketch"
set "CONFIG_FILE=C:\app\AndroidStudioProjects\_Common\config\aab.conf"
set "GRADLEW=%PROJECT_DIR%\gradlew.bat"

if not exist "%CONFIG_FILE%" (
    echo [ERROR] Signing configuration not found: %CONFIG_FILE%
    pause
    exit /b 1
)
set "KEYSTORE_FILE="
set "KEYSTORE_PASSWORD="
set "KEY_ALIAS="
set "KEY_PASSWORD="
for /f "usebackq eol=# tokens=1,* delims==" %%A in ("%CONFIG_FILE%") do set "%%A=%%B"
if "%KEYSTORE_FILE%"=="" (
    echo [ERROR] Set KEYSTORE_FILE in aab.conf.
    pause
    exit /b 1
)
if "%KEYSTORE_PASSWORD%"=="" (
    echo [ERROR] Set KEYSTORE_PASSWORD in aab.conf.
    pause
    exit /b 1
)
if "%KEY_ALIAS%"=="" (
    echo [ERROR] Set KEY_ALIAS in aab.conf.
    pause
    exit /b 1
)
if "%KEY_PASSWORD%"=="" (
    echo [ERROR] Set KEY_PASSWORD in aab.conf.
    pause
    exit /b 1
)
if "%KEYSTORE_PASSWORD%"=="CHANGE_ME" (
    echo [ERROR] Replace placeholder values in aab.conf.
    pause
    exit /b 1
)
if "%KEY_ALIAS%"=="CHANGE_ME" (
    echo [ERROR] Replace placeholder values in aab.conf.
    pause
    exit /b 1
)
if "%KEY_PASSWORD%"=="CHANGE_ME" (
    echo [ERROR] Replace placeholder values in aab.conf.
    pause
    exit /b 1
)
if not exist "%KEYSTORE_FILE%" (
    echo [ERROR] Keystore file was not found. Check KEYSTORE_FILE in aab.conf.
    pause
    exit /b 1
)
if not exist "%GRADLEW%" (
    echo [ERROR] gradlew.bat was not found: %GRADLEW%
    pause
    exit /b 1
)
set "ANDROID_SIGNING_STORE_FILE=%KEYSTORE_FILE%"
set "ANDROID_SIGNING_STORE_PASSWORD=%KEYSTORE_PASSWORD%"
set "ANDROID_SIGNING_KEY_ALIAS=%KEY_ALIAS%"
set "ANDROID_SIGNING_KEY_PASSWORD=%KEY_PASSWORD%"
echo.
echo === Build signed release AAB ===
pushd "%PROJECT_DIR%" >nul
call "%GRADLEW%" bundleRelease
set "RESULT=%ERRORLEVEL%"
popd >nul
if not "%RESULT%"=="0" (
    echo [ERROR] Gradle bundleRelease failed. Exit code: %RESULT%
    pause
    exit /b %RESULT%
)
echo.
echo AAB created: %PROJECT_DIR%\app\build\outputs\bundle\release\app-release.aab
start "" "%PROJECT_DIR%\app\build\outputs\bundle\release"
endlocal
