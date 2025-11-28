package com.example.asynctask.handler;

import com.example.asynctask.context.TaskContext;
import com.example.asynctask.context.TaskHandlerResult;
import lombok.extern.slf4j.Slf4j;

/**
 * 任务处理器抽象基类
 * 实现责任链模式的核心接口
 * 
 * @author System
 */
@Slf4j
public abstract class TaskHandler {
    
    /**
     * 下一个处理器
     */
    protected TaskHandler nextHandler;
    
    /**
     * 处理器名称（默认使用类名）
     */
    private final String handlerName;
    
    protected TaskHandler() {
        this.handlerName = this.getClass().getSimpleName();
    }
    
    /**
     * 设置下一个处理器
     * 
     * @param nextHandler 下一个处理器
     * @return 下一个处理器
     */
    public TaskHandler setNext(TaskHandler nextHandler) {
        this.nextHandler = nextHandler;
        return nextHandler;
    }
    
    /**
     * 执行处理
     * 
     * @param context 任务上下文
     * @return 处理结果
     */
    public final TaskHandlerResult handle(TaskContext context) {
        String taskId = context.getTaskId();
        String stepName = context.getCurrentStepName();
        
        log.info("开始执行处理器: {}, 任务ID: {}, 步骤: {}", handlerName, taskId, stepName);
        
        TaskHandlerResult result = null;
        long startTime = System.currentTimeMillis();
        
        try {
            // 检查是否需要跳过当前步骤
            if (shouldSkip(context)) {
                String reason = getSkipReason(context);
                log.info("跳过处理器: {}, 任务ID: {}, 原因: {}", handlerName, taskId, reason);
                result = TaskHandlerResult.skip(reason);
                context.skipCurrent(reason);
                return result;
            }
            
            // 执行前置处理
            TaskHandlerResult preResult = preHandle(context);
            if (preResult != null && !preResult.isSuccess()) {
                log.warn("处理器: {}, 前置处理失败, 任务ID: {}, 错误: {}", 
                        handlerName, taskId, preResult.getErrorMessage());
                return preResult;
            }
            
            // 执行具体业务逻辑
            result = doHandle(context);
            
            // 执行后置处理
            TaskHandlerResult postResult = postHandle(context, result);
            if (postResult != null && !postResult.isSuccess()) {
                log.warn("处理器: {}, 后置处理失败, 任务ID: {}, 错误: {}", 
                        handlerName, taskId, postResult.getErrorMessage());
                return postResult;
            }
            
            // 如果后置处理改变了结果，使用后置处理的结果
            if (postResult != null) {
                result = postResult;
            }
            
        } catch (Exception e) {
            log.error("处理器执行异常: {}, 任务ID: {}, 错误: {}", 
                    handlerName, taskId, e.getMessage(), e);
            result = handleException(context, e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            if (result != null) {
                result.setDurationMs(duration);
            }
            
            log.info("处理器执行完成: {}, 任务ID: {}, 耗时: {}ms, 结果: {}", 
                    handlerName, taskId, duration, result != null ? result.getStatus() : "null");
        }
        
        // 如果当前处理器成功执行且没有中断链路，则继续执行下一个处理器
        if (result != null && result.isSuccess() && !context.isBreakChain() && nextHandler != null) {
            log.debug("继续执行下一个处理器: {}", nextHandler.getHandlerName());
            return nextHandler.handle(context);
        }
        
        return result;
    }
    
    /**
     * 子类需要实现的具体业务处理逻辑
     * 
     * @param context 任务上下文
     * @return 处理结果
     * @throws Exception 处理异常
     */
    protected abstract TaskHandlerResult doHandle(TaskContext context) throws Exception;
    
    /**
     * 前置处理
     * 子类可以重写此方法实现前置逻辑
     * 
     * @param context 任务上下文
     * @return 前置处理结果，返回null表示继续执行
     * @throws Exception 处理异常
     */
    protected TaskHandlerResult preHandle(TaskContext context) throws Exception {
        return null;
    }
    
    /**
     * 后置处理
     * 子类可以重写此方法实现后置逻辑
     * 
     * @param context 任务上下文
     * @param result 处理结果
     * @return 后置处理结果，返回null表示使用原有结果
     * @throws Exception 处理异常
     */
    protected TaskHandlerResult postHandle(TaskContext context, TaskHandlerResult result) throws Exception {
        return null;
    }
    
    /**
     * 异常处理
     * 子类可以重写此方法实现自定义异常处理逻辑
     * 
     * @param context 任务上下文
     * @param exception 异常
     * @return 处理结果
     */
    protected TaskHandlerResult handleException(TaskContext context, Exception exception) {
        String taskId = context.getTaskId();
        String errorMessage = String.format("处理器执行异常: %s, 错误: %s", 
                handlerName, exception.getMessage());
        
        // 根据异常类型判断是否可重试
        boolean retryable = isRetryableException(exception);
        
        return TaskHandlerResult.builder()
                .status(TaskHandlerResult.StepStatus.FAILED)
                .errorMessage(errorMessage)
                .exception(exception)
                .retryable(retryable)
                .build();
    }
    
    /**
     * 检查是否需要跳过当前步骤
     * 子类可以重写此方法实现自定义跳过逻辑
     * 
     * @param context 任务上下文
     * @return 是否跳过
     */
    protected boolean shouldSkip(TaskContext context) {
        return false;
    }
    
    /**
     * 获取跳过原因
     * 子类可以重写此方法提供自定义跳过原因
     * 
     * @param context 任务上下文
     * @return 跳过原因
     */
    protected String getSkipReason(TaskContext context) {
        return "默认跳过逻辑";
    }
    
    /**
     * 判断异常是否可重试
     * 子类可以重写此方法实现自定义重试逻辑
     * 
     * @param exception 异常
     * @return 是否可重试
     */
    protected boolean isRetryableException(Exception exception) {
        // 网络异常、超时异常等通常可重试
        // 业务异常、参数错误等通常不可重试
        String className = exception.getClass().getSimpleName();
        return !className.contains("Business") && 
               !className.contains("Argument") && 
               !className.contains("Validation");
    }
    
    /**
     * 获取处理器名称
     * 
     * @return 处理器名称
     */
    public String getHandlerName() {
        return handlerName;
    }
    
    /**
     * 检查是否支持指定的任务类型
     * 默认支持所有任务类型，子类可以重写此方法实现特定支持逻辑
     * 
     * @param taskType 任务类型
     * @return 是否支持
     */
    public boolean supports(String taskType) {
        return true;
    }
    
    /**
     * 获取处理器版本
     * 子类可以重写此方法实现版本管理
     * 
     * @return 版本号
     */
    public String getVersion() {
        return "1.0.0";
    }
    
    /**
     * 获取处理器描述
     * 子类可以重写此方法提供处理器描述信息
     * 
     * @return 描述信息
     */
    public String getDescription() {
        return "任务处理器: " + handlerName;
    }
}