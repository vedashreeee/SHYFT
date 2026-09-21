@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot
set ANDROID_HOME=C:\Android\Sdk
set PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\emulator;%ANDROID_HOME%\cmdline-tools\latest\bin;%PATH%

echo Creating AVD shyft_avd...
echo no | "C:\Android\Sdk\cmdline-tools\latest\bin\avdmanager.bat" create avd -n shyft_avd -k "system-images;android-34;google_apis;x86_64" --force

echo Starting Android Emulator...
start /b "C:\Android\Sdk\emulator\emulator.exe" -avd shyft_avd -no-window -no-audio -gpu off

echo Waiting for ADB device connection...
"C:\Android\Sdk\platform-tools\adb.exe" wait-for-device
"C:\Android\Sdk\platform-tools\adb.exe" shell getprop sys.boot_completed
