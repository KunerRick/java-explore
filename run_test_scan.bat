@echo off
echo 测试Spring组件扫描...
cd e:/work/codeOther/java-explore

java -cp "target/classes;target/dependency/*" com.example.AsyncTaskManagerApplication --no-example

pause