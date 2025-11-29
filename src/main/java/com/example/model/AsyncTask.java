package com.example.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AsyncTask {
    private Long id;
    private String taskId;
    private String taskType;
    private String taskName;
    private String status;
    
    // 任务执行信息
    private Integer progress;
    private String currentStep;
    
    // JSON数据字段
    private String taskConfig;
    private String contextData;
    private String stepLogs;
    
    // 错误和重试
    private String errorMessage;
    private Integer maxRetries;
    private Integer retryCount;
    
    // 时间信息
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    // 控制字段
    private Integer priority;
    private Integer timeoutSeconds;
    private Integer version;
    private String creator;
}