package com.example.service;

import com.example.chain.TaskContext;
import com.example.model.AsyncTask;

import java.util.List;

public interface TaskService {
    
    /**
     * 创建任务
     * @param taskContext 任务上下文
     * @return 任务ID
     */
    String createTask(TaskContext taskContext);
    
    /**
     * 执行任务
     * @param taskId 任务ID
     */
    void executeTask(String taskId);
    
    /**
     * 异步执行任务
     * @param taskId 任务ID
     */
    void executeTaskAsync(String taskId);
    
    /**
     * 获取任务
     * @param taskId 任务ID
     * @return 任务信息
     */
    AsyncTask getTask(String taskId);
    
    /**
     * 获取所有待处理任务
     * @param limit 限制数量
     * @return 待处理任务列表
     */
    List<AsyncTask> getPendingTasks(int limit);
    
    /**
     * 更新任务进度
     * @param taskId 任务ID
     * @param progress 进度
     * @param currentStep 当前步骤
     * @param contextData 上下文数据
     * @param stepLogs 步骤日志
     */
    void updateTaskProgress(String taskId, int progress, String currentStep, 
                           String contextData, String stepLogs);
    
    /**
     * 更新任务状态
     * @param taskId 任务ID
     * @param status 状态
     * @param errorMessage 错误信息
     */
    void updateTaskStatus(String taskId, String status, String errorMessage);
    
    /**
     * 完成任务
     * @param taskId 任务ID
     * @param status 任务状态
     * @param errorMessage 错误信息
     */
    void completeTask(String taskId, String status, String errorMessage);
    
    /**
     * 获取需要恢复的任务
     * @return 需要恢复的任务列表
     */
    List<AsyncTask> getTasksToRecover();
    
    /**
     * 恢复中断的任务
     */
    void recoverInterruptedTasks();
}