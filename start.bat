@echo off
echo ===================================
echo Async Task Framework
echo ===================================
echo.

REM 检查Java版本
echo Checking Java version...
java -version
echo.
echo Continuing with Java environment check...
echo.

REM 显示Java版本详情
java -version
echo.

REM 设置JAVA_HOME（如果系统未设置）
if "%JAVA_HOME%"=="" (
    echo WARNING: JAVA_HOME is not set. Attempting to detect Java 11...
    for /f "tokens=3" %%a in ('java -version 2^>^&1 ^| findstr "11"') do (
        echo Detected Java 11 at: %%a
        echo Continuing with Java 11 environment...
    )
    echo If you encounter issues, please set JAVA_HOME to your Java 11 installation.
)

REM 运行Maven命令
echo Running Maven clean install...
mvn clean install

if %errorlevel% neq 0 (
    echo.
    echo Build failed! Please check the error messages above.
    pause
    exit /b 1
)

REM 启动应用
echo.
echo Starting application...
mvn spring-boot:run