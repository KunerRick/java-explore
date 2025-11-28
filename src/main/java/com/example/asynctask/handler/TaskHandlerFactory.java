package com.example.asynctask.handler;

import java.util.List;

/**
 * 任务处理器工厂接口
 * 用于创建和管理处理器实例
 * 
 * @author System
 */
public interface TaskHandlerFactory {
    
    /**
     * 根据处理器名称获取处理器实例
     * 
     * @param handlerName 处理器名称
     * @return 处理器实例，如果不存在返回null
     */
    TaskHandler getHandler(String handlerName);
    
    /**
     * 根据处理器名称列表创建处理器链
     * 
     * @param handlerNames 处理器名称列表（按执行顺序）
     * @return 处理器链的第一个处理器
     */
    TaskHandler createChain(List<String> handlerNames);
    
    /**
     * 根据处理器名称数组创建处理器链
     * 
     * @param handlerNames 处理器名称数组（按执行顺序）
     * @return 处理器链的第一个处理器
     */
    TaskHandler createChain(String... handlerNames);
    
    /**
     * 获取所有可用的处理器名称
     * 
     * @return 处理器名称列表
     */
    List<String> getAllHandlerNames();
    
    /**
     * 注册处理器
     * 
     * @param handlerName 处理器名称
     * @param handler 处理器实例
     */
    void registerHandler(String handlerName, TaskHandler handler);
    
    /**
     * 检查指定的处理器是否存在
     * 
     * @param handlerName 处理器名称
     * @return 是否存在
     */
    boolean hasHandler(String handlerName);
}