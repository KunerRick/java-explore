package com.example.asynctask.service;

import com.example.asynctask.context.TaskContext;
import com.example.asynctask.context.TaskHandlerResult;
import com.example.asynctask.dto.TaskRequest;
import com.example.asynctask.dto.TaskResponse;
import com.example.asynctask.entity.AsyncTask;
import com.example.asynctask.handler.TaskHandler;
import com.example.asynctask.handler.video.VideoScoringHandlerChain;
import com.example.asynctask.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 视频评分任务服务
 * 负责视频评分任务的创建、执行和结果处理
 * 
 * @author System
 */
@Slf4j
@Service
public class VideoScoringService {
    
    @Autowired
    private AsyncTaskService asyncTaskService;
    
    @Autowired
    private VideoScoringHandlerChain videoScoringHandlerChain;
    
    /**
     * 创建视频评分任务
     * 
     * @param request 任务请求
     * @return 任务响应
     */
    @Transactional
    public TaskResponse createVideoScoringTask(TaskRequest request) {
        try {
            // 验证参数
            validateRequest(request);
            
            // 构建任务配置
            JsonUtils.TaskConfig taskConfig = buildTaskConfig(request);
            
            // 创建异步任务
            AsyncTask savedTask = asyncTaskService.createTask(
                "VIDEO_SCORING",
                request.getTaskName(),
                request.getCreator() != null ? request.getCreator() : "system",
                taskConfig
            );
            
            log.info("创建视频评分任务成功，任务ID: {}", savedTask.getTaskId());
            
            return TaskResponse.fromEntity(savedTask);
            
        } catch (Exception e) {
            log.error("创建视频评分任务失败", e);
            throw new RuntimeException("创建视频评分任务失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行视频评分任务
     * 
     * @param taskId 任务ID
     * @return 执行结果
     */
    @Transactional
    public TaskHandlerResult executeVideoScoringTask(String taskId) {
        try {
            // 获取任务
            AsyncTask task = asyncTaskService.getTask(taskId);
            if (task == null) {
                return TaskHandlerResult.failure("任务不存在: " + taskId);
            }
            
            // 检查任务状态
            if (task.getStatus() != AsyncTask.TaskStatus.PENDING) {
                return TaskHandlerResult.failure("任务状态不正确，当前状态: " + task.getStatus());
            }
            
            // 开始处理任务
            if (!asyncTaskService.startTask(taskId)) {
                return TaskHandlerResult.failure("任务开始失败，可能已被其他线程处理");
            }
            
            // 构建任务上下文
            TaskContext context = buildTaskContext(task);
            
            // 执行处理器链
            TaskHandler handlerChain = videoScoringHandlerChain.getVideoScoringChain();
            if (handlerChain == null) {
                asyncTaskService.failTask(taskId, "视频评分处理器链未初始化");
                return TaskHandlerResult.failure("视频评分处理器链未初始化");
            }
            
            TaskHandlerResult result = handlerChain.handle(context);
            
            // 根据执行结果更新任务状态
            if (result.isSuccess()) {
                asyncTaskService.completeTask(taskId, "视频评分任务执行成功");
                asyncTaskService.updateTaskProgress(taskId, 100);
            } else if (result.getStatus() == TaskHandlerResult.StepStatus.SKIPPED) {
                asyncTaskService.completeTask(taskId, "任务被跳过: " + result.getErrorMessage());
                asyncTaskService.updateTaskProgress(taskId, 100);
            } else {
                asyncTaskService.failTask(taskId, result.getErrorMessage());
                
                // 如果可重试且未超过最大重试次数，则更新重试计数
                if (result.isRetryable() && task.getRetryCount() < task.getMaxRetries()) {
                    asyncTaskService.retryTask(taskId);
                }
            }
            
            log.info("视频评分任务执行完成，任务ID: {}, 结果: {}", taskId, result.getStatus());
            
            return result;
            
        } catch (Exception e) {
            log.error("执行视频评分任务异常，任务ID: {}", taskId, e);
            
            // 更新任务状态为失败
            asyncTaskService.failTask(taskId, "任务执行异常: " + e.getMessage());
            
            return TaskHandlerResult.failure("任务执行异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取视频评分结果
     * 
     * @param taskId 任务ID
     * @return 评分结果
     */
    public Map<String, Object> getScoringResult(String taskId) {
        try {
            AsyncTask task = asyncTaskService.getTask(taskId);
            if (task == null) {
                throw new RuntimeException("任务不存在: " + taskId);
            }
            
            if (task.getStatus() != AsyncTask.TaskStatus.COMPLETED) {
                throw new RuntimeException("任务未完成，当前状态: " + task.getStatus());
            }
            
            // 从上下文数据中获取评分结果
            Map<String, Object> contextData = asyncTaskService.getTaskContextData(taskId);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> scoringResult = (Map<String, Object>) contextData.get("scoringResult");
            
            if (scoringResult == null) {
                throw new RuntimeException("未找到评分结果");
            }
            
            return scoringResult;
            
        } catch (Exception e) {
            log.error("获取视频评分结果失败，任务ID: {}", taskId, e);
            throw new RuntimeException("获取评分结果失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 验证任务请求
     * 
     * @param request 任务请求
     */
    private void validateRequest(TaskRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("任务请求不能为空");
        }
        
        if (request.getTaskName() == null || request.getTaskName().trim().isEmpty()) {
            throw new IllegalArgumentException("任务名称不能为空");
        }
        
        if (request.getParameters() == null || request.getParameters().isEmpty()) {
            throw new IllegalArgumentException("任务参数不能为空");
        }
        
        // 验证必要参数
        Object videoId = request.getParameters().get("videoId");
        if (videoId == null || videoId.toString().trim().isEmpty()) {
            throw new IllegalArgumentException("视频ID不能为空");
        }
    }
    
    /**
     * 构建任务配置
     * 
     * @param request 任务请求
     * @return 任务配置
     */
    private JsonUtils.TaskConfig buildTaskConfig(TaskRequest request) {
        return JsonUtils.TaskConfig.builder()
                .handlerChain(java.util.List.of("SubtitleCheckHandler", "SubtitleFetchHandler", "ScoringHandler"))
                .maxRetries(3)
                .timeoutSeconds(request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() : 3600)
                .priority(request.getPriority() != null ? request.getPriority() : 5)
                .parameters(request.getParameters())
                .build();
    }
    
    /**
     * 构建任务上下文
     * 
     * @param task 任务实体
     * @return 任务上下文
     */
    private TaskContext buildTaskContext(AsyncTask task) {
        TaskContext context = TaskContext.builder()
                .task(task)
                .currentStepName(task.getCurrentStep())
                .build();
        
        // 设置任务配置参数
        JsonUtils.TaskConfig taskConfig = asyncTaskService.getTaskConfig(task.getTaskId());
        if (taskConfig != null && taskConfig.getParameters() != null) {
            taskConfig.getParameters().forEach(context::setData);
        }
        
        // 设置当前上下文数据
        Map<String, Object> contextData = asyncTaskService.getTaskContextData(task.getTaskId());
        if (contextData != null) {
            contextData.forEach(context::setData);
        }
        
        return context;
    }
    
    /**
     * 生成任务ID
     * 
     * @return 任务ID
     */
    private String generateTaskId() {
        return "VIDEO_SCORING_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    /**
     * 检查是否支持指定的任务类型
     * 
     * @param taskType 任务类型
     * @return 是否支持
     */
    public boolean supports(String taskType) {
        return "VIDEO_SCORING".equals(taskType);
    }
}