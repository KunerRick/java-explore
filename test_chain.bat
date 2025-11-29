@echo off
echo 测试责任链构建...
cd e:/work/codeOther/java-explore

set CLASSPATH=target/classes;target/dependency/*
echo 类路径: %CLASSPATH%

echo 运行测试...
java -cp "target/classes;target/dependency/*" -Dfile.encoding=UTF-8 com.example.AsyncTaskManagerApplication --no-example

pause