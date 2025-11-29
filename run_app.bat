@echo off
echo 启动Spring Boot应用...
cd e:/work/codeOther/java-explore

echo 1. 编译项目...
call mvn compile

echo.
echo 2. 启动应用...
call java -cp "target/classes;target/dependency/*" com.example.AsyncTaskManagerApplication --no-example

pause