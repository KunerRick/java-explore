-- 异步任务主表（单表极简设计）
CREATE TABLE IF NOT EXISTS async_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) NOT NULL UNIQUE COMMENT '业务唯一ID',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型：VIDEO_SCORING',
    task_name VARCHAR(200) COMMENT '任务名称',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING, PROCESSING, COMPLETED, FAILED, PAUSED',
    
    -- 任务执行信息
    progress INT DEFAULT 0 COMMENT '进度 0-100',
    current_step VARCHAR(50) COMMENT '当前步骤',
    
    -- JSON数据字段（核心设计）
    task_config JSON COMMENT '任务配置：处理器链、重试策略、超时等',
    context_data JSON COMMENT '任务上下文：执行过程中的动态数据',
    step_logs JSON COMMENT '步骤执行日志：所有步骤的详细记录',
    
    -- 错误和重试
    error_message TEXT COMMENT '错误信息',
    max_retries INT DEFAULT 3,
    retry_count INT DEFAULT 0,
    
    -- 时间信息
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    start_time DATETIME,
    end_time DATETIME,
    
    -- 控制字段
    priority INT DEFAULT 5,
    timeout_seconds INT DEFAULT 0,
    version INT DEFAULT 0 COMMENT '乐观锁',
    creator VARCHAR(50),
    
    -- 索引优化
    INDEX idx_task_type_status (task_type, status),
    INDEX idx_status_priority (status, priority),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异步任务主表（极简设计）';

-- 任务明细表
CREATE TABLE IF NOT EXISTS task_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    step_name VARCHAR(100) NOT NULL COMMENT '步骤名称',
    step_order INT NOT NULL COMMENT '步骤顺序',
    step_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '步骤状态：PENDING, RUNNING, SUCCESS, FAILED, SKIPPED',
    
    -- 步骤执行结果
    step_result TEXT COMMENT '步骤执行结果',
    context_data TEXT COMMENT '步骤上下文数据',
    error_message TEXT COMMENT '错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    
    -- 时间信息
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration_ms BIGINT COMMENT '耗时（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 索引优化
    INDEX idx_task_id (task_id),
    INDEX idx_task_step (task_id, step_name),
    INDEX idx_status (step_status),
    INDEX idx_create_time (create_time),
    UNIQUE KEY uk_task_step (task_id, step_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务明细表';

-- 任务事件日志表
CREATE TABLE IF NOT EXISTS task_event_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id VARCHAR(64) NOT NULL COMMENT '任务ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型',
    event_data TEXT COMMENT '事件数据',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    -- 索引优化
    INDEX idx_task_id (task_id),
    INDEX idx_event_type (event_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务事件日志表';