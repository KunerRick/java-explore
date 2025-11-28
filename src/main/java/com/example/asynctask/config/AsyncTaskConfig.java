package com.example.asynctask.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 异步任务配置属性
 * 
 * @author System
 */
@Data
@Component
@ConfigurationProperties(prefix = "async-task")
public class AsyncTaskConfig {
    
    /**
     * 线程池配置
     */
    private ThreadPoolConfig threadPool = new ThreadPoolConfig();
    
    /**
     * 调度器配置
     */
    private SchedulerConfig scheduler = new SchedulerConfig();
    
    /**
     * 重试配置
     */
    private RetryConfig retry = new RetryConfig();
    
    @Data
    public static class ThreadPoolConfig {
        private int corePoolSize = 5;
        private int maxPoolSize = 20;
        private int queueCapacity = 100;
        private int keepAliveSeconds = 60;
        private String threadNamePrefix = "AsyncTask-";
    }
    
    @Data
    public static class SchedulerConfig {
        private boolean enabled = true;
        private int pollInterval = 5000;
        private int batchSize = 10;
    }
    
    @Data
    public static class RetryConfig {
        private int maxAttempts = 3;
        private long initialDelay = 1000;
        private double multiplier = 2.0;
    }
}