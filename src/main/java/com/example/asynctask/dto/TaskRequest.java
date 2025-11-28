package com.example.asynctask.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * 任务请求DTO
 * 
 * @author System
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    
    /**
     * 任务类型
     */
    @NotBlank(message = "任务类型不能为空")
    private String taskType;
    
    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    private String taskName;
    
    /**
     * 创建人
     */
    @NotBlank(message = "创建人不能为空")
    private String creator;
    
    /**
     * 任务参数
     */
    private Map<String, Object> parameters;
    
    /**
     * 优先级（1-10，数字越大优先级越高）
     */
    @NotNull(message = "优先级不能为空")
    private Integer priority;
    
    /**
     * 最大重试次数
     */
    private Integer maxRetries;
    
    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds;
    
    /**
     * 处理器链
     */
    private String handlerChain;
}