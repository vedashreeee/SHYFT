@echo off
set JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot
set ANDROID_HOME=C:\Android\Sdk
set PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\emulator;%PATH%

echo Running unit tests and assembling debug APK...
call gradlew.bat testDebugUnitTest assembleDebug --stacktrace
