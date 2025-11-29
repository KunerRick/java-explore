package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 任务处理器信息注解
 * 用于标注处理器的基本信息和支持的任务类型
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskHandlerInfo {
    
    /**
     * 处理器名称
     * 默认使用类名
     */
    String name() default "";
    
    /**
     * 处理器描述
     */
    String description() default "";
    
    /**
     * 支持的任务类型
     * 如果为空，则需要通过配置文件配置
     */
    String[] supportedTaskTypes() default {};
    
    /**
     * 处理器执行顺序
     * 数值越小越优先执行
     */
    int order() default 0;
}