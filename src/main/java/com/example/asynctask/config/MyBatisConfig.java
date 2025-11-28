package com.example.asynctask.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis配置类
 * 
 * @author System
 */
@Configuration
@MapperScan("com.example.asynctask.mapper")
public class MyBatisConfig {
    // MyBatis配置类，用于扫描Mapper接口
}