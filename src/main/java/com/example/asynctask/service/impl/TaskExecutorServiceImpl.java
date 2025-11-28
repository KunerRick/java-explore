package com.example.asynctask.service.impl;

import com.example.asynctask.context.TaskContext;
import com.example.asynctask.context.TaskHandlerResult;
import com.example.asynctask.entity.AsyncTask;
import com.example.asynctask.handler.TaskHandler;
import com.example.asynctask.handler.TaskHandlerFactory;
import com.example.asynctask.service.AsyncTaskService;
import com.example.asynctask.service.TaskExecutorService;
import com.example.asynctask.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 任务执行器服务实现类
 * 
 * @author System
 */
@Slf4j
@Service
public class TaskExecutorServiceImpl implements TaskExecutorService {
    
    @Autowired
    private AsyncTaskService asyncTaskService;
    
    @Autowired
    private TaskHandlerFactory taskHandlerFactory;
    
    @Autowired
    private Executor asyncTaskExecutor;
    
    /**
     * 正在执行的任务缓存
     */
    private final Map<String, Boolean> runningTasks = new ConcurrentHashMap<>();
    
    @Override
    public TaskHandlerResult executeTask(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return TaskHandlerResult.failure("任务ID不能为空");
        }
        
        // 获取任务
        AsyncTask task = asyncTaskService.getTaskWithLock(taskId);
        if (task == null) {
            return TaskHandlerResult.failure("任务不存在: " + taskId);
        }
        
        // 检查任务状态
        if (task.getStatus() != AsyncTask.TaskStatus.PENDING) {
            return TaskHandlerResult.failure("任务状态不是PENDING，无法执行: " + taskId + ", 状态: " + task.getStatus());
        }
        
        // 标记任务为运行中
        if (!asyncTaskService.startTask(taskId)) {
            return TaskHandlerResult.failure("启动任务失败: " + taskId);
        }
        
        // 标记为正在执行
        runningTasks.put(taskId, true);
        
        try {
            // 创建任务上下文
            TaskContext context = createTaskContext(task);
            
            // 执行任务
            return executeTaskInternal(context);
        } catch (Exception e) {
            log.error("执行任务异常: {}", taskId, e);
            return TaskHandlerResult.failure("执行任务异常: " + e.getMessage(), e);
        } finally {
            // 清除执行标记
            runningTasks.remove(taskId);
        }
    }
    
    @Override
    public TaskHandlerResult executeTask(TaskContext context) {
        if (context == null || context.getTask() == null) {
            return TaskHandlerResult.failure("任务上下文或任务对象不能为空");
        }
        
        String taskId = context.getTaskId();
        
        // 标记为正在执行
        runningTasks.put(taskId, true);
        
        try {
            // 执行任务
            return executeTaskInternal(context);
        } catch (Exception e) {
            log.error("执行任务异常: {}", taskId, e);
            return TaskHandlerResult.failure("执行任务异常: " + e.getMessage(), e);
        } finally {
            // 清除执行标记
            runningTasks.remove(taskId);
        }
    }
    
    @Override
    @Async("asyncTaskExecutor")
    public void executeTaskAsync(String taskId) {
        log.info("异步执行任务: {}", taskId);
        executeTask(taskId);
    }
    
    @Override
    @Async("asyncTaskExecutor")
    public void executeTaskAsync(TaskContext context) {
        String taskId = context.getTaskId();
        log.info("异步执行任务: {}", taskId);
        executeTask(context);
    }
    
    @Override
    public List<TaskHandlerResult> executeTasks(List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return new ArrayList<>();
        }
        
        return taskIds.parallelStream()
                .map(this::executeTask)
                .collect(Collectors.toList());
    }
    
    @Override
    public void executeTasksAsync(List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return;
        }
        
        for (String taskId : taskIds) {
            executeTaskAsync(taskId);
        }
    }
    
    @Override
    public TaskHandlerResult retryTask(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return TaskHandlerResult.failure("任务ID不能为空");
        }
        
        // 获取任务
        AsyncTask task = asyncTaskService.getTaskWithLock(taskId);
        if (task == null) {
            return TaskHandlerResult.failure("任务不存在: " + taskId);
        }
        
        // 检查任务状态
        if (task.getStatus() != AsyncTask.TaskStatus.FAILED) {
            return TaskHandlerResult.failure("任务状态不是FAILED，无法重试: " + taskId + ", 状态: " + task.getStatus());
        }
        
        // 检查重试次数
        if (task.getRetryCount() >= task.getMaxRetries()) {
            return TaskHandlerResult.nonRetryableFailure("已达到最大重试次数: " + task.getMaxRetries());
        }
        
        // 重置任务状态
        if (!asyncTaskService.retryTask(taskId)) {
            return TaskHandlerResult.failure("重置任务状态失败: " + taskId);
        }
        
        // 执行任务
        return executeTask(taskId);
    }
    
    @Override
    public boolean stopTaskExecution(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return false;
        }
        
        // 只有正在执行的任务才能停止
        if (!isTaskRunning(taskId)) {
            return false;
        }
        
        // 暂停任务
        boolean result = asyncTaskService.pauseTask(taskId);
        if (result) {
            // 清除执行标记
            runningTasks.remove(taskId);
            log.info("任务已停止: {}", taskId);
        }
        
        return result;
    }
    
    @Override
    public boolean isTaskRunning(String taskId) {
        return runningTasks.containsKey(taskId);
    }
    
    /**
     * 内部执行任务方法
     * 
     * @param context 任务上下文
     * @return 执行结果
     */
    private TaskHandlerResult executeTaskInternal(TaskContext context) {
        String taskId = context.getTaskId();
        AsyncTask task = context.getTask();
        
        // 记录开始时间
        LocalDateTime startTime = LocalDateTime.now();
        task.setStartTime(startTime);
        
        try {
            // 获取任务配置
            JsonUtils.TaskConfig taskConfig = JsonUtils.parseTaskConfig(task.getTaskConfig());
            if (taskConfig == null || CollectionUtils.isEmpty(taskConfig.getHandlerChain())) {
                return TaskHandlerResult.failure("任务配置或处理器链为空: " + taskId);
            }
            
            // 创建处理器链
            TaskHandler handlerChain = taskHandlerFactory.createChain(taskConfig.getHandlerChain());
            if (handlerChain == null) {
                return TaskHandlerResult.failure("创建处理器链失败: " + taskId);
            }
            
            log.info("开始执行任务处理器链: {}, 任务: {}", 
                    String.join(" -> ", taskConfig.getHandlerChain()), taskId);
            
            // 执行处理器链
            TaskHandlerResult result = handlerChain.handle(context);
            
            // 更新任务状态
            if (result.isSuccess()) {
                asyncTaskService.completeTask(taskId);
                log.info("任务执行成功: {}", taskId);
            } else if (result.isSkipped()) {
                // 跳过整个任务
                asyncTaskService.completeTask(taskId, "任务被跳过: " + result.getMessage());
                log.info("任务被跳过: {}, 原因: {}", taskId, result.getMessage());
            } else {
                // 任务失败
                String errorMessage = result.getErrorMessage();
                asyncTaskService.failTask(taskId, errorMessage);
                
                // 如果可以重试，则自动重试
                if (result.isRetryable() && task.getRetryCount() < task.getMaxRetries()) {
                    log.info("任务失败，将进行重试: {}, 重试次数: {}/{}", 
                            taskId, task.getRetryCount() + 1, task.getMaxRetries());
                    
                    // 延迟重试
                    CompletableFuture.delayedExecutor(result.getRetryDelay(), java.util.concurrent.TimeUnit.MILLISECONDS)
                            .execute(() -> retryTask(taskId));
                } else {
                    log.warn("任务失败且无法重试: {}, 错误: {}", taskId, errorMessage);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("任务执行异常: {}", taskId, e);
            
            // 记录异常
            asyncTaskService.failTask(taskId, "任务执行异常: " + e.getMessage());
            
            return TaskHandlerResult.failure("任务执行异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 创建任务上下文
     * 
     * @param task 任务对象
     * @return 任务上下文
     */
    private TaskContext createTaskContext(AsyncTask task) {
        // 解析上下文数据
        Map<String, Object> contextData = JsonUtils.parseContextData(task.getContextData());
        
        // 创建任务上下文
        TaskContext context = TaskContext.builder()
                .task(task)
                .data(contextData)
                .build();
        
        // 设置上下文数据到任务对象
        context.setData("taskId", task.getTaskId());
        context.setData("taskType", task.getTaskType());
        context.setData("taskName", task.getTaskName());
        context.setData("creator", task.getCreator());
        context.setData("maxRetries", task.getMaxRetries());
        context.setData("retryCount", task.getRetryCount());
        
        // 解析任务配置并添加到上下文
        JsonUtils.TaskConfig taskConfig = JsonUtils.parseTaskConfig(task.getTaskConfig());
        if (taskConfig != null) {
            context.setData("handlerChain", taskConfig.getHandlerChain());
            context.setData("parameters", taskConfig.getParameters());
        }
        
        return context;
    }
}