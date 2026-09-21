@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot
set ANDROID_HOME=C:\Android\Sdk
set PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\emulator;%ANDROID_HOME%\cmdline-tools\latest\bin;%PATH%

echo Starting Android Emulator with swiftshader_indirect...
start /b "C:\Android\Sdk\emulator\emulator.exe" -avd shyft_avd -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect -no-accel

echo Waiting for ADB device connection...
"C:\Android\Sdk\platform-tools\adb.exe" wait-for-device
echo Emulator connected! Waiting for boot to complete...
:loop
timeout /t 2 /nobreak >nul
for /f "tokens=*" %%i in ('"C:\Android\Sdk\platform-tools\adb.exe" shell getprop sys.boot_completed 2^>nul') do set BOOT=%%i
if not "%BOOT%"=="1" goto loop

echo Emulator booted successfully!
echo Installing SHYFT app...
"C:\Android\Sdk\platform-tools\adb.exe" install -r "c:\Users\Venni\Downloads\shyft\app\build\outputs\apk\debug\app-debug.apk"

echo Launching SHYFT MainActivity...
"C:\Android\Sdk\platform-tools\adb.exe" shell am start -n com.shyft.privacy/.MainActivity
