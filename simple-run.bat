@echo off
echo ===================================
echo Simple Java Compiler and Runner
echo ===================================

echo Current Java version:
java -version
echo.

echo Creating output directory...
if not exist target\classes mkdir target\classes

echo Compiling Java files...
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d target\classes -cp "target\classes" @sources.txt

if %errorlevel% neq 0 (
    echo.
    echo Compilation failed!
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo.

echo Running application...
cd target\classes
java -cp ".;..\..\lib\*" com.example.asynctask.AsyncTaskApplication

cd ..\..
pause