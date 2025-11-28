package com.example.asynctask.config;

import com.example.asynctask.service.TaskSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动配置类
 * 
 * @author System
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableScheduling
public class BootstrapConfig {
    
    private final TaskSchedulerService taskSchedulerService;
    
    /**
     * 应用启动后执行
     */
    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            log.info("应用启动完成，初始化任务调度器...");
            
            // 启动任务调度器
            taskSchedulerService.startScheduler();
            
            log.info("任务调度器已启动，应用就绪");
        };
    }
}