@echo off
echo 运行应用测试...
cd e:/work/codeOther/java-explore
set JAVA_OPTS=-Dfile.encoding=UTF-8
java %JAVA_OPTS% -cp target/classes com.example.AsyncTaskManagerApplication --no-example
pause