package com.example;

import com.example.config.TaskHandlerConfig;
import com.example.config.DebugConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = "com.example")
@MapperScan("com.example.mapper")
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties({TaskHandlerConfig.class, DebugConfig.class})
public class AsyncTaskManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AsyncTaskManagerApplication.class, args);
    }
}