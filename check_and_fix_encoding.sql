-- 检查并修复数据库编码问题
USE test;

-- 1. 检查当前数据库字符集
SHOW VARIABLES LIKE 'character_set_%';

-- 2. 检查表字符集
SHOW TABLE STATUS FROM test LIKE 'async_task';

-- 3. 如果需要，修改数据库字符集
-- ALTER DATABASE test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4. 修改表字符集
ALTER TABLE async_task CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5. 检查列字符集
SHOW FULL COLUMNS FROM async_task;