@echo off
setlocal

set "SCRIPT_DIR=%~dp0"
set "OUTPUT_DIR=C:\app\AndroidStudioProjects\_Capture"
set "ADB_EXE="

for /f "delims=" %%A in ('where adb 2^>nul') do if not defined ADB_EXE set "ADB_EXE=%%A"
if not defined ADB_EXE if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" set "ADB_EXE=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
if not defined ADB_EXE if exist "C:\J\application\platform-tools-latest-windows\platform-tools\adb.exe" set "ADB_EXE=C:\J\application\platform-tools-latest-windows\platform-tools\adb.exe"

if not defined ADB_EXE (
    echo [ERROR] adb.exe was not found.
    echo Connect Android SDK platform-tools or add adb to PATH.
    pause
    exit /b 1
)

if not exist "%OUTPUT_DIR%" mkdir "%OUTPUT_DIR%"

set "DEVICE_COUNT=0"
for /f "skip=1 tokens=1,2" %%A in ('"%ADB_EXE%" devices') do (
    if "%%B"=="device" set /a DEVICE_COUNT+=1
)

if "%DEVICE_COUNT%"=="0" (
    echo [ERROR] No authorized Android device was found.
    echo Connect a device and allow USB debugging, then try again.
    "%ADB_EXE%" devices
    pause
    exit /b 1
)

for /f %%A in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmmss"') do set "TIMESTAMP=%%A"
set "OUTPUT_FILE=%OUTPUT_DIR%\device-%TIMESTAMP%.png"

echo Capturing the connected Android device screen...
cmd /c ""%ADB_EXE%" exec-out screencap -p > "%OUTPUT_FILE%""
if errorlevel 1 (
    echo [ERROR] Failed to capture the device screen.
    pause
    exit /b 1
)

echo.
echo Saved:
echo %OUTPUT_FILE%
pause
endlocal
