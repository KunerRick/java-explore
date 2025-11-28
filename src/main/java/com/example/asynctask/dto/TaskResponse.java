package com.example.asynctask.dto;

import com.example.asynctask.entity.AsyncTask;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务响应DTO
 * 
 * @author System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    
    /**
     * 任务ID
     */
    private Long id;
    
    /**
     * 业务唯一ID
     */
    private String taskId;
    
    /**
     * 任务类型
     */
    private String taskType;
    
    /**
     * 任务名称
     */
    private String taskName;
    
    /**
     * 任务状态
     */
    private AsyncTask.TaskStatus status;
    
    /**
     * 进度（0-100）
     */
    private Integer progress;
    
    /**
     * 当前步骤
     */
    private String currentStep;
    
    /**
     * 任务配置
     */
    private Map<String, Object> taskConfig;
    
    /**
     * 上下文数据
     */
    private Map<String, Object> contextData;
    
    /**
     * 步骤日志
     */
    private List<AsyncTask.StepLog> stepLogs;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetries;
    
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds;
    
    /**
     * 版本号
     */
    private Integer version;
    
    /**
     * 创建人
     */
    private String creator;
    
    /**
     * 从实体转换
     * 
     * @param task 任务实体
     * @return 响应DTO
     */
    public static TaskResponse fromEntity(AsyncTask task) {
        if (task == null) {
            return null;
        }
        
        return TaskResponse.builder()
                .id(task.getId())
                .taskId(task.getTaskId())
                .taskType(task.getTaskType())
                .taskName(task.getTaskName())
                .status(task.getStatus())
                .progress(task.getProgress())
                .currentStep(task.getCurrentStep())
                .taskConfig(com.example.asynctask.util.JsonUtils.parseMap(task.getTaskConfig()))
                .contextData(com.example.asynctask.util.JsonUtils.parseContextData(task.getContextData()))
                .stepLogs(com.example.asynctask.util.JsonUtils.parseStepLogs(task.getStepLogs()))
                .errorMessage(task.getErrorMessage())
                .maxRetries(task.getMaxRetries())
                .retryCount(task.getRetryCount())
                .createdTime(task.getCreatedTime())
                .updatedTime(task.getUpdatedTime())
                .startTime(task.getStartTime())
                .endTime(task.getEndTime())
                .priority(task.getPriority())
                .timeoutSeconds(task.getTimeoutSeconds())
                .version(task.getVersion())
                .creator(task.getCreator())
                .build();
    }
}