@echo off
setlocal EnableExtensions

rem Capture SenSketch startup-track and playback logs and copy them to the clipboard.
set "PROJECT_DIR=C:\app\AndroidStudioProjects\SenSketch"
set "PROJECT_NAME=SenSketch"
set "LOG_PREFIX=%PROJECT_NAME%"
set "LOG_FILE=%PROJECT_DIR%\doc\deploy\%LOG_PREFIX%-restore-log.txt"
set "PID_FILE=%TEMP%\%LOG_PREFIX%-restore-log.pid"
set "ADB_EXE="

if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" set "ADB_EXE=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if not defined ADB_EXE (
    for /f "delims=" %%A in ('where.exe adb') do if not defined ADB_EXE set "ADB_EXE=%%A"
)
if not defined ADB_EXE (
    echo [ERROR] adb.exe was not found.
    pause
    exit /b 1
)
"%ADB_EXE%" get-state 1>nul 2>nul
if errorlevel 1 (
    echo [ERROR] No Android device is connected or USB debugging is unavailable.
    pause
    exit /b 1
)
echo === %PROJECT_NAME% restore log capture ===
echo Reproduce the problem, then press any key in this window.
"%ADB_EXE%" logcat -c
powershell -NoProfile -Command "$p=Start-Process -FilePath '%ADB_EXE%' -ArgumentList 'logcat','-v','time','-s','%PROJECT_NAME%','AndroidRuntime','chromium' -RedirectStandardOutput '%LOG_FILE%' -PassThru; Set-Content -LiteralPath '%PID_FILE%' -Value $p.Id"
pause
if exist "%PID_FILE%" (
    for /f "delims=" %%P in (%PID_FILE%) do taskkill /pid %%P /f >nul 2>nul
    del "%PID_FILE%" >nul 2>nul
)
if exist "%LOG_FILE%" (
    powershell -NoProfile -Command "Set-Clipboard -Value (Get-Content -LiteralPath '%LOG_FILE%' -Raw -Encoding UTF8)"
    echo Log copied to the clipboard.
)
echo Log saved to: %LOG_FILE%
pause
endlocal
