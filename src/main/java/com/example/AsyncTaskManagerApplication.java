package com.example;

import com.example.config.TaskHandlerConfig;
import com.example.config.DebugConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = "com.example")
@MapperScan("com.example.mapper")
@EnableAsync
@EnableConfigurationProperties({TaskHandlerConfig.class, DebugConfig.class})
public class AsyncTaskManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AsyncTaskManagerApplication.class, args);
    }
}