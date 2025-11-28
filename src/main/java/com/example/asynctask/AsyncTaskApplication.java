package com.example.asynctask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 异步任务框架应用程序主类
 * 
 * @author System
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class AsyncTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(AsyncTaskApplication.class, args);
    }
}