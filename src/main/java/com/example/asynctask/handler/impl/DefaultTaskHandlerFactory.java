package com.example.asynctask.handler.impl;

import com.example.asynctask.handler.TaskHandler;
import com.example.asynctask.handler.TaskHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 默认任务处理器工厂实现
 * 基于Spring容器管理处理器实例
 * 
 * @author System
 */
@Slf4j
@Component
public class DefaultTaskHandlerFactory implements TaskHandlerFactory {
    
    /**
     * 处理器缓存
     */
    private final Map<String, TaskHandler> handlerCache = new HashMap<>();
    
    @Autowired
    private ApplicationContext applicationContext;
    
    /**
     * 初始化方法
     * 扫描并注册所有TaskHandler类型的Bean
     */
    @PostConstruct
    public void init() {
        Map<String, TaskHandler> handlers = applicationContext.getBeansOfType(TaskHandler.class);
        
        for (Map.Entry<String, TaskHandler> entry : handlers.entrySet()) {
            String beanName = entry.getKey();
            TaskHandler handler = entry.getValue();
            String handlerName = handler.getHandlerName();
            
            handlerCache.put(handlerName, handler);
            
            log.info("注册任务处理器: {} (Bean: {})", handlerName, beanName);
        }
        
        log.info("任务处理器工厂初始化完成，共注册 {} 个处理器", handlerCache.size());
    }
    
    @Override
    public TaskHandler getHandler(String handlerName) {
        return handlerCache.get(handlerName);
    }
    
    @Override
    public TaskHandler createChain(List<String> handlerNames) {
        if (handlerNames == null || handlerNames.isEmpty()) {
            return null;
        }
        
        TaskHandler firstHandler = null;
        TaskHandler prevHandler = null;
        
        for (String handlerName : handlerNames) {
            TaskHandler handler = getHandler(handlerName);
            if (handler == null) {
                log.warn("处理器不存在: {}", handlerName);
                continue;
            }
            
            if (firstHandler == null) {
                firstHandler = handler;
            } else {
                prevHandler.setNext(handler);
            }
            
            prevHandler = handler;
        }
        
        if (firstHandler != null) {
            log.debug("创建处理器链: {}", String.join(" -> ", handlerNames));
        } else {
            log.warn("创建处理器链失败，没有找到有效的处理器: {}", handlerNames);
        }
        
        return firstHandler;
    }
    
    @Override
    public TaskHandler createChain(String... handlerNames) {
        if (handlerNames == null || handlerNames.length == 0) {
            return null;
        }
        
        return createChain(List.of(handlerNames));
    }
    
    @Override
    public List<String> getAllHandlerNames() {
        return handlerCache.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }
    
    @Override
    public void registerHandler(String handlerName, TaskHandler handler) {
        if (handlerName == null || handlerName.trim().isEmpty()) {
            throw new IllegalArgumentException("处理器名称不能为空");
        }
        
        if (handler == null) {
            throw new IllegalArgumentException("处理器实例不能为null");
        }
        
        TaskHandler existingHandler = handlerCache.get(handlerName);
        if (existingHandler != null) {
            log.warn("替换已存在的处理器: {} (旧: {}, 新: {})", 
                    handlerName, existingHandler.getClass().getName(), handler.getClass().getName());
        }
        
        handlerCache.put(handlerName, handler);
        log.info("注册任务处理器: {} ({})", handlerName, handler.getClass().getName());
    }
    
    @Override
    public boolean hasHandler(String handlerName) {
        return handlerCache.containsKey(handlerName);
    }
    
    /**
     * 清空处理器缓存
     * 主要用于测试场景
     */
    public void clearCache() {
        handlerCache.clear();
        log.info("清空处理器缓存");
    }
    
    /**
     * 获取处理器数量
     * 
     * @return 处理器数量
     */
    public int getHandlerCount() {
        return handlerCache.size();
    }
}