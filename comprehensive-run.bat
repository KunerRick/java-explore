@echo off
echo ===================================
echo Comprehensive Java Compiler and Runner
echo ===================================

echo Current Java version:
java -version
echo.

echo Creating output directory...
if not exist target\classes mkdir target\classes
if not exist target\lib mkdir target\lib

echo Checking for Spring Boot dependencies...
if not exist target\lib\spring-boot-starter-web-*.jar (
    echo WARNING: Spring Boot dependencies not found in target\lib
    echo You need to run 'mvn dependency:copy-dependencies' first
    echo.
    echo Trying to run with classpath from local Maven repository...
    echo.
)

echo Creating classpath...
set CLASSPATH=target\classes
if exist "%USERPROFILE%\.m2\repository\org\springframework\boot\spring-boot-starter-web" (
    for /r "%USERPROFILE%\.m2\repository\org\springframework\boot\spring-boot-starter-web" %%a in (*.jar) do call :addtocp "%%a"
)
if exist "%USERPROFILE%\.m2\repository\org\springframework\boot\spring-boot" (
    for /r "%USERPROFILE%\.m2\repository\org\springframework\boot\spring-boot" %%a in (*.jar) do call :addtocp "%%a"
)
if exist "%USERPROFILE%\.m2\repository\org\springframework\spring-core" (
    for /r "%USERPROFILE%\.m2\repository\org\springframework\spring-core" %%a in (*.jar) do call :addtocp "%%a"
)
if exist "%USERPROFILE%\.m2\repository\org\projectlombok\lombok" (
    for /r "%USERPROFILE%\.m2\repository\org\projectlombok\lombok" %%a in (*.jar) do call :addtocp "%%a"
)

echo Compiling Java files...
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d target\classes -cp "%CLASSPATH%" @sources.txt -Xlint:unchecked -processorpath "%USERPROFILE%\.m2\repository\org\projectlombok\lombok\*\lombok-*.jar"

if %errorlevel% neq 0 (
    echo.
    echo Compilation failed!
    echo.
    echo Trying alternative approach without Lombok processing...
    javac -encoding UTF-8 -d target\classes -cp "%CLASSPATH%" @sources.txt -Xlint:unchecked
    if %errorlevel% neq 0 (
        echo.
        echo Compilation still failed!
        pause
        exit /b 1
    )
)

echo.
echo Compilation successful!
echo.

echo Running application...
cd target\classes
java -cp "%CLASSPATH%" com.example.asynctask.AsyncTaskApplication

cd ..\..
pause
goto :eof

:addtocp
set CLASSPATH=%CLASSPATH%;%~1
goto :eof