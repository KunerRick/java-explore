package com.example.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

// @Component
public class HandlerScanTest implements CommandLineRunner {
    
    @Autowired
    private ApplicationContext applicationContext;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 测试Spring组件扫描 ===");
        
        // 检查所有Spring管理的Bean
        String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
        System.out.println("Spring管理的Bean总数: " + beanNames.length);
        
        // 检查特定类型的Bean
        System.out.println("\n=== 检查TaskHandler类型 ===");
        String[] handlerBeans = applicationContext.getBeanNamesForType(com.example.chain.TaskHandler.class);
        System.out.println("TaskHandler类型的Bean数量: " + handlerBeans.length);
        
        for (String beanName : handlerBeans) {
            System.out.println("发现TaskHandler: " + beanName);
        }
        
        // 检查handler包下的组件
        System.out.println("\n=== 检查handler包下的组件 ===");
        for (String beanName : beanNames) {
            if (beanName.contains("handler") || beanName.contains("Handler")) {
                System.out.println("发现handler相关Bean: " + beanName);
                Object bean = applicationContext.getBean(beanName);
                System.out.println("  类型: " + bean.getClass().getName());
            }
        }
        
        System.out.println("\n=== 测试完成 ===");
    }
}