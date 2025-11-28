package com.example.asynctask.config;

import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis缓存配置
 * 
 * @author System
 */
@Configuration
public class MyBatisCacheConfig {
    
    /**
     * 配置MyBatis二级缓存
     * 使用简单的内存缓存实现
     */
    @Bean
    public Cache myBatisCache() {
        // 使用MyBatis自带的PerpetualCache实现，这是一个基于内存的缓存
        return new PerpetualCache("asyncTaskCache");
    }
}