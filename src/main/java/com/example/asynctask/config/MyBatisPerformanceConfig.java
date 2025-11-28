package com.example.asynctask.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * MyBatis性能监控配置
 * 
 * @author System
 */
@Slf4j
@Configuration
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})
})
public class MyBatisPerformanceConfig implements Interceptor {
    
    private static final long SLOW_SQL_THRESHOLD_MS = 1000; // 慢SQL阈值，超过1秒记录
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        String sqlId = mappedStatement.getId();
        
        long startTime = System.currentTimeMillis();
        Object result = invocation.proceed();
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // 记录慢SQL
        if (executionTime > SLOW_SQL_THRESHOLD_MS) {
            log.warn("慢SQL警告 - ID: {}, 执行时间: {}ms", sqlId, executionTime);
        } else {
            log.debug("SQL执行 - ID: {}, 执行时间: {}ms", sqlId, executionTime);
        }
        
        return result;
    }
    
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties) {
        // 可以在这里设置一些属性
    }
}