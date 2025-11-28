@echo off
echo Checking Java environment...

REM 显示当前Java版本
echo.
echo Current Java version:
java -version

echo.
echo Java Home:
echo %JAVA_HOME%

echo.
echo Available Java installations (common locations):
dir "C:\Program Files\Java" /b 2>nul
dir "C:\Program Files (x86)\Java" /b 2>nul

echo.
echo If you see "java: command not found" or incorrect version, please:
echo 1. Install Java 11 from https://adoptium.net/
echo 2. Set JAVA_HOME environment variable to your Java 11 installation
echo 3. Add %JAVA_HOME%\bin to your PATH environment variable

pause