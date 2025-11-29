package com.example.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 调试配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "debug")
public class DebugConfig {
    private boolean printAllBeans = false;
    private boolean printHandlerBeans = true;
}