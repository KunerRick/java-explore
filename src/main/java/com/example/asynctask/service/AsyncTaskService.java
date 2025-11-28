package com.example.asynctask.service;

import com.example.asynctask.entity.AsyncTask;
import com.example.asynctask.util.JsonUtils;

import java.util.List;
import java.util.Map;

/**
 * 异步任务服务接口
 * 
 * @author System
 */
public interface AsyncTaskService {
    
    /**
     * 创建任务
     * 
     * @param taskType 任务类型
     * @param taskName 任务名称
     * @param creator 创建人
     * @return 创建的任务
     */
    AsyncTask createTask(String taskType, String taskName, String creator);
    
    /**
     * 创建任务（带参数）
     * 
     * @param taskType 任务类型
     * @param taskName 任务名称
     * @param creator 创建人
     * @param parameters 任务参数
     * @return 创建的任务
     */
    AsyncTask createTask(String taskType, String taskName, String creator, Map<String, Object> parameters);
    
    /**
     * 创建任务（带自定义配置）
     * 
     * @param taskType 任务类型
     * @param taskName 任务名称
     * @param creator 创建人
     * @param taskConfig 任务配置
     * @return 创建的任务
     */
    AsyncTask createTask(String taskType, String taskName, String creator, JsonUtils.TaskConfig taskConfig);
    
    /**
     * 根据任务ID获取任务
     * 
     * @param taskId 任务ID
     * @return 任务对象
     */
    AsyncTask getTask(String taskId);
    
    /**
     * 根据任务ID获取任务（带锁）
     * 
     * @param taskId 任务ID
     * @return 任务对象
     */
    AsyncTask getTaskWithLock(String taskId);
    
    /**
     * 分页查询任务
     * 
     * @param pageable 分页参数
     * @return 任务分页结果
     */
    List<AsyncTask> getTasks();
    
    /**
     * 根据任务类型分页查询任务
     * 
     * @param taskType 任务类型
     * @param pageable 分页参数
     * @return 任务分页结果
     */
    List<AsyncTask> getTasksByType(String taskType);
    
    /**
     * 根据状态分页查询任务
     * 
     * @param status 任务状态
     * @param pageable 分页参数
     * @return 任务分页结果
     */
    List<AsyncTask> getTasksByStatus(AsyncTask.TaskStatus status);
    
    /**
     * 提交任务执行
     * 
     * @param taskId 任务ID
     * @return 是否成功提交
     */
    boolean submitTask(String taskId);
    
    /**
     * 获取待处理任务列表
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> getPendingTasks(int limit);
    
    /**
     * 获取待处理任务列表（带锁）
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> getPendingTasksWithLock(int limit);
    
    /**
     * 根据任务类型获取待处理任务列表（带锁）
     * 
     * @param taskType 任务类型
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> getPendingTasksByTypeWithLock(String taskType, int limit);
    
    /**
     * 开始处理任务
     * 
     * @param taskId 任务ID
     * @return 是否成功开始
     */
    boolean startTask(String taskId);
    
    /**
     * 完成任务
     * 
     * @param taskId 任务ID
     * @return 是否成功完成
     */
    boolean completeTask(String taskId);
    
    /**
     * 完成任务（带结果摘要）
     * 
     * @param taskId 任务ID
     * @param resultSummary 结果摘要
     * @return 是否成功完成
     */
    boolean completeTask(String taskId, String resultSummary);
    
    /**
     * 失败任务
     * 
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     * @return 是否成功设置为失败状态
     */
    boolean failTask(String taskId, String errorMessage);
    
    /**
     * 暂停任务
     * 
     * @param taskId 任务ID
     * @return 是否成功暂停
     */
    boolean pauseTask(String taskId);
    
    /**
     * 取消任务
     * 
     * @param taskId 任务ID
     * @return 是否成功取消
     */
    boolean cancelTask(String taskId);
    
    /**
     * 更新任务进度
     * 
     * @param taskId 任务ID
     * @param progress 进度（0-100）
     * @return 是否成功更新
     */
    boolean updateTaskProgress(String taskId, Integer progress);
    
    /**
     * 更新任务上下文数据
     * 
     * @param taskId 任务ID
     * @param contextData 上下文数据
     * @return 是否成功更新
     */
    boolean updateTaskContextData(String taskId, Map<String, Object> contextData);
    
    /**
     * 添加步骤日志
     * 
     * @param taskId 任务ID
     * @param stepLog 步骤日志
     * @return 是否成功添加
     */
    boolean addStepLog(String taskId, AsyncTask.StepLog stepLog);
    
    /**
     * 获取任务上下文数据
     * 
     * @param taskId 任务ID
     * @return 上下文数据
     */
    Map<String, Object> getTaskContextData(String taskId);
    
    /**
     * 获取任务步骤日志
     * 
     * @param taskId 任务ID
     * @return 步骤日志列表
     */
    List<AsyncTask.StepLog> getTaskStepLogs(String taskId);
    
    /**
     * 获取任务配置
     * 
     * @param taskId 任务ID
     * @return 任务配置
     */
    JsonUtils.TaskConfig getTaskConfig(String taskId);
    
    /**
     * 获取可重试的失败任务
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> getRetryableFailedTasks(int limit);
    
    /**
     * 重试任务
     * 
     * @param taskId 任务ID
     * @return 是否成功重试
     */
    boolean retryTask(String taskId);
    
    /**
     * 获取超时任务
     * 
     * @return 任务列表
     */
    List<AsyncTask> getTimeoutTasks();
    
    /**
     * 统计各状态任务数量
     * 
     * @return 统计结果
     */
    Map<String, Long> countTasksByStatus();
    
    /**
     * 统计指定任务类型的任务状态数量
     * 
     * @param taskType 任务类型
     * @return 统计结果
     */
    Map<String, Long> countTasksByStatusAndType(String taskType);
    
    /**
     * 删除任务
     * 
     * @param taskId 任务ID
     * @return 是否成功删除
     */
    boolean deleteTask(String taskId);
}