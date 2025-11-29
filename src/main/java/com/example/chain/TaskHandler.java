package com.example.chain;

public interface TaskHandler {
    
    /**
     * 处理任务
     * @param context 任务上下文
     * @return 处理结果
     */
    HandleResult handle(TaskContext context);
    
    /**
     * 获取处理器名称
     * @return 处理器名称
     */
    String getName();
    
    /**
     * 设置下一个处理器
     * @param next 下一个处理器
     */
    void setNext(TaskHandler next);
    
    /**
     * 获取下一个处理器
     * @return 下一个处理器
     */
    TaskHandler getNext();
}