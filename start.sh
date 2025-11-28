#!/bin/bash

echo "Starting Async Task Framework with Java 11..."

# 设置JAVA_HOME（请根据您的Java 11安装路径修改）
# 如果已经在系统环境变量中设置了JAVA_HOME，可以注释掉下面这行
# export JAVA_HOME=/usr/lib/jvm/java-11-openjdk

# 检查Java版本
echo "Checking Java version..."
java -version

# 运行Maven命令
echo "Running Maven clean install..."
mvn clean install

# 启动应用
echo "Starting application..."
mvn spring-boot:run