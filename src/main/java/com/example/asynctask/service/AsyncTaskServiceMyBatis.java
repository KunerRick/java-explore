package com.example.asynctask.service;

import com.example.asynctask.entity.AsyncTask;
import com.example.asynctask.entity.AsyncTask.TaskStatus;
import com.example.asynctask.util.JsonUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 异步任务服务接口（MyBatis实现）
 * 
 * @author System
 */
public interface AsyncTaskServiceMyBatis {
    
    /**
     * 创建任务
     * 
     * @param taskType 任务类型
     * @param taskName 任务名称
     * @param creator 创建人
     * @return 任务对象
     */
    AsyncTask createTask(String taskType, String taskName, String creator);
    
    /**
     * 创建任务（带参数）
     * 
     * @param taskType 任务类型
     * @param taskName 任务名称
     * @param creator 创建人
     * @param parameters 参数
     * @return 任务对象
     */
    AsyncTask createTask(String taskType, String taskName, String creator, Map<String, Object> parameters);
    
    /**
     * 创建任务（带配置）
     * 
     * @param taskType 任务类型
     * @param taskName 任务名称
     * @param creator 创建人
     * @param taskConfig 任务配置
     * @return 任务对象
     */
    AsyncTask createTask(String taskType, String taskName, String creator, JsonUtils.TaskConfig taskConfig);
    
    /**
     * 获取任务
     * 
     * @param taskId 任务ID
     * @return 任务对象
     */
    AsyncTask getTask(String taskId);
    
    /**
     * 获取任务（带悲观锁）
     * 
     * @param taskId 任务ID
     * @return 任务对象
     */
    AsyncTask getTaskWithLock(String taskId);
    
    /**
     * 分页获取任务
     * 
     * @param taskType 任务类型
     * @param status 任务状态
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @return 任务分页结果
     */
    Map<String, Object> getTasksByPage(String taskType, TaskStatus status, int page, int size, String sortBy, String sortDir);
    
    /**
     * 提交任务
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean submitTask(String taskId);
    
    /**
     * 获取待处理任务
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> getPendingTasks(int limit);
    
    /**
     * 获取待处理任务（带悲观锁）
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> getPendingTasksWithLock(int limit);
    
    /**
     * 根据类型获取待处理任务（带悲观锁）
     * 
     * @param taskType 任务类型
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> getPendingTasksByTypeWithLock(String taskType, int limit);
    
    /**
     * 开始任务
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean startTask(String taskId);
    
    /**
     * 完成任务
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean completeTask(String taskId);
    
    /**
     * 完成任务（带结果摘要）
     * 
     * @param taskId 任务ID
     * @param resultSummary 结果摘要
     * @return 是否成功
     */
    boolean completeTask(String taskId, String resultSummary);
    
    /**
     * 任务失败
     * 
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     * @return 是否成功
     */
    boolean failTask(String taskId, String errorMessage);
    
    /**
     * 暂停任务
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean pauseTask(String taskId);
    
    /**
     * 取消任务
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean cancelTask(String taskId);
    
    /**
     * 重试任务
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean retryTask(String taskId);
    
    /**
     * 更新任务进度
     * 
     * @param taskId 任务ID
     * @param progress 进度
     * @return 是否成功
     */
    boolean updateProgress(String taskId, int progress);
    
    /**
     * 更新当前步骤
     * 
     * @param taskId 任务ID
     * @param currentStep 当前步骤
     * @return 是否成功
     */
    boolean updateCurrentStep(String taskId, String currentStep);
    
    /**
     * 更新上下文数据
     * 
     * @param taskId 任务ID
     * @param contextData 上下文数据
     * @return 是否成功
     */
    boolean updateContextData(String taskId, Map<String, Object> contextData);
    
    /**
     * 添加步骤日志
     * 
     * @param taskId 任务ID
     * @param stepLog 步骤日志
     * @return 是否成功
     */
    boolean addStepLog(String taskId, AsyncTask.StepLog stepLog);
    
    /**
     * 更新步骤日志
     * 
     * @param taskId 任务ID
     * @param stepLogs 步骤日志列表
     * @return 是否成功
     */
    boolean updateStepLogs(String taskId, List<AsyncTask.StepLog> stepLogs);
    
    /**
     * 统计各状态任务数量
     * 
     * @return 统计结果
     */
    Map<TaskStatus, Long> countByStatus();
    
    /**
     * 统计指定任务类型的任务状态数量
     * 
     * @param taskType 任务类型
     * @return 统计结果
     */
    Map<TaskStatus, Long> countByStatusAndTaskType(String taskType);
    
    /**
     * 查找超时任务
     * 
     * @param status 任务状态
     * @param currentTime 当前时间
     * @return 任务列表
     */
    List<AsyncTask> findTimeoutTasks(TaskStatus status, LocalDateTime currentTime);
    
    /**
     * 查找需要重试的失败任务
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> findRetryableFailedTasks(int limit);
    
    /**
     * 删除任务
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean deleteTask(String taskId);
    
    /**
     * 获取默认处理链
     * 
     * @param taskType 任务类型
     * @return 处理链名称
     */
    List<String> getDefaultHandlerChain(String taskType);

    /**
     * 分页查询任务
     * 
     * @param taskType 任务类型
     * @param status 任务状态
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param sortDir 排序方向
     * @return 分页结果
     */
    Map<String, Object> getTasksByPage(String taskType, String status, int page, int size, String sortBy, String sortDir);

    /**
     * 按状态统计任务数量
     * 
     * @return 状态统计结果
     */
    Map<String, Long> countTasksByStatus();

    /**
     * 按状态和类型统计任务数量
     * 
     * @param taskType 任务类型
     * @return 统计结果
     */
    Map<String, Long> countTasksByStatusAndType(String taskType);

    /**
     * 更新任务上下文数据
     * 
     * @param taskId 任务ID
     * @param contextData 上下文数据
     * @return 是否成功
     */
    boolean updateTaskContextData(String taskId, Map<String, Object> contextData);

    /**
     * 生成任务ID
     * 
     * @param taskType 任务类型
     * @return 任务ID
     */
    String generateTaskId(String taskType);
}