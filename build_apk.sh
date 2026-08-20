#!/usr/bin/env bash
set -e

echo "==================================================="
echo "  SkinSafe Android APK Build Tool"
echo "==================================================="
echo ""

cd android
chmod +x gradlew

echo "[1/3] Checking Gradle configuration..."
./gradlew --version

echo ""
echo "[2/3] Building Debug APK (assembleDebug)..."
./gradlew assembleDebug

echo ""
echo "==================================================="
echo " SUCCESS: Debug APK Generated!"
echo "==================================================="
echo "Location: android/app/build/outputs/apk/debug/app-debug.apk"
echo ""
echo "To install on your connected device/emulator, run:"
echo "adb install -r android/app/build/outputs/apk/debug/app-debug.apk"
echo ""

cd ..
