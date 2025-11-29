package com.example.chain;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractTaskHandler implements TaskHandler {
    
    protected TaskHandler next;
    
    @Override
    public HandleResult handle(TaskContext context) {
        log.info("开始执行处理器: {}, 任务ID: {}", getName(), context.getTaskId());
        
        try {
            HandleResult result = doHandle(context);
            
            if (!result.isSuccess()) {
                log.error("处理器执行失败: {}, 错误信息: {}", getName(), result.getMessage());
                context.setErrorMessage(result.getMessage());
                return result;
            }
            
            log.info("处理器执行成功: {}, 结果: {}", getName(), result.getMessage());
            
            if (result.isShouldContinue() && next != null) {
                return next.handle(context);
            }
            
            return result;
        } catch (Exception e) {
            log.error("处理器执行异常: {}, 异常信息: {}", getName(), e.getMessage(), e);
            context.setErrorMessage("处理器异常: " + e.getMessage());
            return HandleResult.failure("处理器异常: " + e.getMessage());
        }
    }
    
    /**
     * 具体的处理逻辑，由子类实现
     * @param context 任务上下文
     * @return 处理结果
     */
    protected abstract HandleResult doHandle(TaskContext context);
    
    @Override
    public void setNext(TaskHandler next) {
        this.next = next;
    }
    
    @Override
    public TaskHandler getNext() {
        return this.next;
    }
}