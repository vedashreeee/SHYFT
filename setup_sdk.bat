@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot
set ANDROID_HOME=C:\Android\Sdk
set PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\cmdline-tools\latest\bin;%PATH%

echo Accepting licenses...
powershell -Command "(1..20) | ForEach-Object { 'y' } | & 'C:\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat' --sdk_root=C:\Android\Sdk --licenses"

echo Installing SDK packages...
powershell -Command "(1..20) | ForEach-Object { 'y' } | & 'C:\Android\Sdk\cmdline-tools\latest\bin\sdkmanager.bat' --sdk_root=C:\Android\Sdk 'platform-tools' 'platforms;android-34' 'build-tools;34.0.0' 'emulator' 'system-images;android-34;google_apis;x86_64'"
