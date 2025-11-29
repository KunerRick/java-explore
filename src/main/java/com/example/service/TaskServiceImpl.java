package com.example.service;

import com.example.chain.TaskContext;
import com.example.chain.TaskHandlerChain;
import com.example.chain.TaskHandlerFactory;
import com.example.mapper.AsyncTaskMapper;
import com.example.model.AsyncTask;
import com.example.service.TaskMonitorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {
    
    @Autowired
    private AsyncTaskMapper asyncTaskMapper;
    
    @Autowired
    @Lazy
    private TaskHandlerFactory handlerFactory;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private TaskMonitorService taskMonitorService;
    
    @Override
    @Transactional
    public String createTask(TaskContext taskContext) {
        try {
            AsyncTask task = new AsyncTask();
            task.setTaskId(taskContext.getTaskId());
            task.setTaskType(taskContext.getTaskType());
            task.setTaskName(taskContext.getTaskId() + "-" + taskContext.getTaskType());
            task.setStatus("PENDING");
            task.setProgress(0);
            task.setMaxRetries(3);
            task.setRetryCount(0);
            task.setPriority(5);
            task.setTimeoutSeconds(300);
            task.setVersion(0);
            task.setCreatedTime(LocalDateTime.now());
            task.setUpdatedTime(LocalDateTime.now());
            
            // 构建任务配置
            Map<String, Object> config = new HashMap<>();
            config.put("handlers", handlerFactory.getHandlerNames(taskContext.getTaskType()));
            task.setTaskConfig(objectMapper.writeValueAsString(config));
            
            // 初始化上下文数据
            Map<String, Object> contextData = new HashMap<>();
            contextData.putAll(taskContext.getData());
            task.setContextData(objectMapper.writeValueAsString(contextData));
            
            // 初始化步骤日志
            Map<String, Object> stepLogs = new HashMap<>();
            task.setStepLogs(objectMapper.writeValueAsString(stepLogs));
            
            asyncTaskMapper.insert(task);
            
            // 更新监控统计
            taskMonitorService.updateTaskStatus(null, task.getStatus());
            taskMonitorService.updateTaskType(task.getTaskType());
            
            log.info("任务创建成功，任务ID: {}", taskContext.getTaskId());
            
            return taskContext.getTaskId();
        } catch (Exception e) {
            log.error("创建任务失败，任务ID: {}, 错误: ", taskContext.getTaskId(), e);
            throw new RuntimeException("创建任务失败", e);
        }
    }
    
    @Override
    @Transactional
    public void executeTask(String taskId) {
        log.info("开始执行任务: {}", taskId);
        
        AsyncTask task = asyncTaskMapper.selectByTaskId(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }
        
        if (!"PENDING".equals(task.getStatus()) && !"FAILED".equals(task.getStatus())) {
            log.warn("任务状态不正确，无法执行，任务ID: {}, 当前状态: {}", taskId, task.getStatus());
            return;
        }
        
        try {
            String oldStatus = task.getStatus();
            // 更新任务状态为处理中
            updateTaskStatus(taskId, "PROCESSING", null);
            
            // 更新监控统计
            taskMonitorService.updateTaskStatus(oldStatus, "PROCESSING");
            
            task.setStartTime(LocalDateTime.now());
            
            // 加载任务上下文
            TaskContext context = loadTaskContext(task);
            
            // 构建处理链
            TaskHandlerChain chain = handlerFactory.buildChain(task.getTaskType());
            log.info("责任链构建完成，任务ID: {}, 处理器数量: {}", taskId, chain.getHandlerNames().size());
            
            // 执行处理链
            com.example.chain.HandleResult result = chain.execute(context);
            log.info("责任链执行完成，任务ID: {}, 执行结果: {}", taskId, result.isSuccess() ? "成功" : "失败");
            
            // 完成任务
            String finalStatus = result.isSuccess() ? "COMPLETED" : "FAILED";
            
            // 更新监控统计
            taskMonitorService.updateTaskStatus("PROCESSING", finalStatus);
            
            completeTask(taskId, finalStatus, result.isSuccess() ? null : result.getMessage());
            
            log.info("任务执行完成，任务ID: {}, 状态: {}", taskId, finalStatus);
        } catch (Exception e) {
            log.error("任务执行异常，任务ID: {}", taskId, e);
            updateTaskStatus(taskId, "FAILED", "执行异常: " + e.getMessage());
        }
    }
    
    @Override
    public void executeTaskAsync(String taskId) {
        // 异步执行任务
        new Thread(() -> executeTask(taskId)).start();
    }
    
    @Override
    public AsyncTask getTask(String taskId) {
        return asyncTaskMapper.selectByTaskId(taskId);
    }
    
    @Override
    public List<AsyncTask> getPendingTasks(int limit) {
        return asyncTaskMapper.selectPendingTasks(limit);
    }
    
    @Override
    @Transactional
    public void updateTaskProgress(String taskId, int progress, String currentStep, 
                                  String contextData, String stepLogs) {
        AsyncTask task = asyncTaskMapper.selectByTaskId(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }
        
        asyncTaskMapper.updateProgressAndStatus(
            taskId, progress, "PROCESSING", currentStep, 
            contextData, stepLogs, null, task.getVersion()
        );
    }
    
    @Override
    @Transactional
    public void updateTaskStatus(String taskId, String status, String errorMessage) {
        AsyncTask task = asyncTaskMapper.selectByTaskId(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }
        
        asyncTaskMapper.updateProgressAndStatus(
            taskId, task.getProgress(), status, task.getCurrentStep(), 
            task.getContextData(), task.getStepLogs(), errorMessage, task.getVersion()
        );
    }
    
    @Override
    @Transactional
    public void completeTask(String taskId, String status, String errorMessage) {
        AsyncTask task = asyncTaskMapper.selectByTaskId(taskId);
        if (task == null) {
            log.error("任务不存在: {}", taskId);
            return;
        }
        
        task.setStatus(status);
        task.setProgress(status.equals("COMPLETED") ? 100 : task.getProgress());
        task.setEndTime(LocalDateTime.now());
        task.setErrorMessage(errorMessage);
        task.setUpdatedTime(LocalDateTime.now());
        
        if (status.equals("FAILED")) {
            task.setRetryCount(task.getRetryCount() + 1);
            if (task.getRetryCount() < task.getMaxRetries()) {
                task.setStatus("PENDING");
                log.info("任务执行失败，将在下次重试，任务ID: {}, 重试次数: {}/{}", 
                        taskId, task.getRetryCount(), task.getMaxRetries());
            }
        }
        
        asyncTaskMapper.updateByTaskId(task);
    }
    
    @Override
    public List<AsyncTask> getTasksToRecover() {
        return asyncTaskMapper.selectTasksToRecover();
    }
    
    @Override
    public void recoverInterruptedTasks() {
        List<AsyncTask> tasksToRecover = getTasksToRecover();
        if (tasksToRecover.isEmpty()) {
            return;
        }
        
        log.info("发现{}个需要恢复的任务", tasksToRecover.size());
        
        for (AsyncTask task : tasksToRecover) {
            log.info("恢复任务: {}", task.getTaskId());
            task.setStatus("PENDING");
            task.setErrorMessage("任务恢复，重新执行");
            asyncTaskMapper.updateByTaskId(task);
            
            // 异步重新执行
            executeTaskAsync(task.getTaskId());
        }
    }
    
    private TaskContext loadTaskContext(AsyncTask task) {
        try {
            TaskContext context = new TaskContext();
            context.setTaskId(task.getTaskId());
            context.setTaskType(task.getTaskType());
            
            if (task.getContextData() != null && !task.getContextData().isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = objectMapper.readValue(task.getContextData(), Map.class);
                context.getData().putAll(data);
            }
            
            return context;
        } catch (Exception e) {
            log.error("加载任务上下文失败，任务ID: {}", task.getTaskId(), e);
            throw new RuntimeException("加载任务上下文失败", e);
        }
    }
}