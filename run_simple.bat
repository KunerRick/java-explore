@echo off
echo 启动简化的异步任务管理器
cd e:/work/codeOther/java-explore
set CLASSPATH=target/classes;target/dependency/*

echo 运行应用...
java -cp "target/classes;target/dependency/*" com.example.AsyncTaskManagerApplication

pause