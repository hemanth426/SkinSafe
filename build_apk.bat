@echo off
echo ===================================================
echo   SkinSafe Android APK Build Tool
echo ===================================================
echo.

cd android

echo [1/3] Checking Gradle configuration...
call gradlew.bat --version

echo.
echo [2/3] Building Debug APK (assembleDebug)...
call gradlew.bat assembleDebug

if %ERRORLEVEL% equ 0 (
    echo.
    echo ===================================================
    echo  SUCCESS: Debug APK Generated!
    echo ===================================================
    echo Location: android\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo To install on your connected device/emulator, run:
    echo adb install -r android\app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo [ERROR] Build failed. Please ensure Android SDK and Java 17+ are installed.
    exit /b %ERRORLEVEL%
)

cd ..
