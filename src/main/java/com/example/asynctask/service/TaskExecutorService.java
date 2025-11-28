package com.example.asynctask.service;

import com.example.asynctask.context.TaskContext;
import com.example.asynctask.context.TaskHandlerResult;

import java.util.List;

/**
 * 任务执行器服务接口
 * 负责任务的实际执行逻辑
 * 
 * @author System
 */
public interface TaskExecutorService {
    
    /**
     * 执行任务
     * 
     * @param taskId 任务ID
     * @return 执行结果
     */
    TaskHandlerResult executeTask(String taskId);
    
    /**
     * 执行任务（带上下文）
     * 
     * @param context 任务上下文
     * @return 执行结果
     */
    TaskHandlerResult executeTask(TaskContext context);
    
    /**
     * 异步执行任务
     * 
     * @param taskId 任务ID
     */
    void executeTaskAsync(String taskId);
    
    /**
     * 异步执行任务（带上下文）
     * 
     * @param context 任务上下文
     */
    void executeTaskAsync(TaskContext context);
    
    /**
     * 批量执行任务
     * 
     * @param taskIds 任务ID列表
     * @return 执行结果列表
     */
    List<TaskHandlerResult> executeTasks(List<String> taskIds);
    
    /**
     * 异步批量执行任务
     * 
     * @param taskIds 任务ID列表
     */
    void executeTasksAsync(List<String> taskIds);
    
    /**
     * 重试任务
     * 
     * @param taskId 任务ID
     * @return 重试结果
     */
    TaskHandlerResult retryTask(String taskId);
    
    /**
     * 停止任务执行
     * 
     * @param taskId 任务ID
     * @return 是否成功停止
     */
    boolean stopTaskExecution(String taskId);
    
    /**
     * 检查任务是否正在执行
     * 
     * @param taskId 任务ID
     * @return 是否正在执行
     */
    boolean isTaskRunning(String taskId);
}