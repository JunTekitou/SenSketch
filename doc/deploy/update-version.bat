@echo off
setlocal EnableExtensions
set "GRADLE_FILE=C:\app\AndroidStudioProjects\SenSketch\app\build.gradle.kts"
set "VERSION_SCRIPT=C:\app\AndroidStudioProjects\_Common\bat\update-version.ps1"
if not exist "%VERSION_SCRIPT%" (
    echo [ERROR] Version update script not found: %VERSION_SCRIPT%
    pause
    exit /b 1
)
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%VERSION_SCRIPT%" "%GRADLE_FILE%"
if errorlevel 1 (
    echo [ERROR] Version update failed.
    pause
    exit /b 1
)
endlocal
