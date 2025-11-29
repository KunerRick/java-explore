package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 任务处理器配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "task-handlers")
public class TaskHandlerConfig {
    
    /**
     * 任务类型 -> 处理器配置映射
     */
    private Map<String, TaskChainConfig> taskTypes;
    
    @Data
    public static class TaskChainConfig {
        /**
         * 处理器名称列表
         */
        private List<String> handlers;
        
        /**
         * 处理链描述
         */
        private String description;
    }
}