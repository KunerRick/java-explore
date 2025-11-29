@echo off
echo 测试数据库编码问题解决方案

echo.
echo 1. 检查当前数据库字符集...
mysql -uroot -pewqazxck -e "SHOW VARIABLES LIKE 'character_set%';"

echo.
echo 2. 修改数据库连接字符串，添加UTF-8编码参数
echo 已更新 application.yml 配置文件，添加了以下参数：
echo useUnicode=true&characterEncoding=UTF-8&connectionCollation=utf8mb4_unicode_ci

echo.
echo 3. 确保数据库表使用正确的字符集
mysql -uroot -pewqazxck test -e "ALTER TABLE async_task CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo.
echo 4. 如果仍然有乱码，可以尝试以下解决方案：
echo a) 修改MySQL配置文件(my.cnf或my.ini)，设置默认字符集
echo b) 重启MySQL服务
echo c) 重新创建数据库表

echo.
echo 请重新启动应用并测试：
echo java -jar target/async-task-manager-1.0.0.jar

pause