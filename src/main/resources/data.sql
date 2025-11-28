-- 插入示例任务配置
INSERT INTO async_task (task_id, task_type, task_name, status, progress, max_retries, retry_count, timeout_seconds, priority, task_config, context_data, step_logs, created_time, updated_time, creator)
VALUES 
('video_scoring_sample_001', 'VIDEO_SCORING', '视频评分任务-示例001', 'COMPLETED', 100, 3, 0, 300, 5, 
'{"handlerChain":["SubtitleCheckHandler","SubtitleFetchHandler","ScoringHandler"],"maxRetries":3,"timeoutSeconds":300,"priority":5,"parameters":{"videoId":"video_001"}}',
'{"videoId":"video_001","hasSubtitle":true,"subtitleContent":{"videoId":"video_001","language":"zh-CN","duration":3600,"textLength":8000,"hasKeyInfo":true}}',
'[{"stepName":"SubtitleCheckHandler","status":"SUCCESS","startTime":"2023-11-28T10:00:00","endTime":"2023-11-28T10:00:01","durationMs":100,"result":"字幕检查完成，视频ID: video_001, 字幕可用"},{"stepName":"SubtitleFetchHandler","status":"SUCCESS","startTime":"2023-11-28T10:00:01","endTime":"2023-11-28T10:00:03","durationMs":200,"result":"字幕获取完成，视频ID: video_001, 字幕语言: zh-CN"},{"stepName":"ScoringHandler","status":"SUCCESS","startTime":"2023-11-28T10:00:03","endTime":"2023-11-28T10:00:08","durationMs":500,"result":"评分完成，视频ID: video_001, 总分: 85.50"}]',
'2023-11-28 10:00:00', '2023-11-28 10:00:08', 'system');

-- 插入待处理任务
INSERT INTO async_task (task_id, task_type, task_name, status, progress, max_retries, retry_count, timeout_seconds, priority, task_config, context_data, step_logs, created_time, updated_time, creator)
VALUES 
('video_scoring_pending_001', 'VIDEO_SCORING', '视频评分任务-待处理001', 'PENDING', 0, 3, 0, 300, 7, 
'{"handlerChain":["SubtitleCheckHandler","SubtitleFetchHandler","ScoringHandler"],"maxRetries":3,"timeoutSeconds":300,"priority":7,"parameters":{"videoId":"video_002"}}',
'{}',
'[]',
'2023-11-28 10:05:00', '2023-11-28 10:05:00', 'admin');

-- 插入处理中任务
INSERT INTO async_task (task_id, task_type, task_name, status, progress, max_retries, retry_count, timeout_seconds, priority, task_config, context_data, step_logs, start_time, created_time, updated_time, creator)
VALUES 
('video_scoring_processing_001', 'VIDEO_SCORING', '视频评分任务-处理中001', 'PROCESSING', 30, 3, 0, 300, 6, 
'{"handlerChain":["SubtitleCheckHandler","SubtitleFetchHandler","ScoringHandler"],"maxRetries":3,"timeoutSeconds":300,"priority":6,"parameters":{"videoId":"video_003"}}',
'{"videoId":"video_003","hasSubtitle":true}',
'[{"stepName":"SubtitleCheckHandler","status":"SUCCESS","startTime":"2023-11-28T10:10:00","endTime":"2023-11-28T10:10:01","durationMs":100,"result":"字幕检查完成，视频ID: video_003, 字幕可用"}]',
'2023-11-28 10:10:01', '2023-11-28 10:10:00', '2023-11-28 10:10:01', 'test_user');

-- 插入失败任务
INSERT INTO async_task (task_id, task_type, task_name, status, progress, max_retries, retry_count, timeout_seconds, priority, task_config, context_data, step_logs, error_message, end_time, created_time, updated_time, creator)
VALUES 
('video_scoring_failed_001', 'VIDEO_SCORING', '视频评分任务-失败001', 'FAILED', 30, 3, 2, 300, 4, 
'{"handlerChain":["SubtitleCheckHandler","SubtitleFetchHandler","ScoringHandler"],"maxRetries":3,"timeoutSeconds":300,"priority":4,"parameters":{"videoId":"video_004"}}',
'{"videoId":"video_004","hasSubtitle":true}',
'[{"stepName":"SubtitleCheckHandler","status":"SUCCESS","startTime":"2023-11-28T10:15:00","endTime":"2023-11-28T10:15:01","durationMs":100,"result":"字幕检查完成，视频ID: video_004, 字幕可用"},{"stepName":"SubtitleFetchHandler","status":"FAILED","startTime":"2023-11-28T10:15:01","endTime":"2023-11-28T10:15:03","durationMs":200,"result":"字幕获取失败","errorMessage":"网络超时"}]',
'字幕获取失败，已达到最大重试次数',
'2023-11-28 10:15:03', '2023-11-28 10:15:00', '2023-11-28 10:15:03', 'guest');