package com.example.asynctask.util;

import com.example.asynctask.entity.AsyncTask;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类
 * 用于处理任务配置、上下文数据和步骤日志
 * 
 * @author System
 */
@Slf4j
public class JsonUtils {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 将对象转换为JSON字符串
     * 
     * @param obj 对象
     * @return JSON字符串，转换失败返回null
     */
    public static String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 将JSON字符串转换为指定类型的对象
     * 
     * @param json JSON字符串
     * @param clazz 目标类型
     * @param <T> 泛型类型
     * @return 对象，转换失败返回null
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON转对象失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 将JSON字符串转换为指定类型的对象（带泛型）
     * 
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @param <T> 泛型类型
     * @return 对象，转换失败返回null
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("JSON转对象失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 将JSON字符串转换为Map
     * 
     * @param json JSON字符串
     * @return Map对象，转换失败返回空Map
     */
    public static Map<String, Object> parseMap(String json) {
        if (!StringUtils.hasText(json)) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("JSON转Map失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }
    
    /**
     * 将Map转换为JSON字符串
     * 
     * @param map Map对象
     * @return JSON字符串，转换失败返回null
     */
    public static String toJsonString(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("Map转JSON失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从JSON字符串中获取指定路径的值
     * 
     * @param json JSON字符串
     * @param path JSON路径，如"handlerChain[0]"
     * @return 值
     */
    public static String getValue(String json, String path) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode node = rootNode.at(path);
            return node.asText();
        } catch (JsonProcessingException e) {
            log.error("从JSON获取值失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从JSON字符串中设置指定路径的值
     * 
     * @param json JSON字符串
     * @param path JSON路径
     * @param value 新值
     * @return 修改后的JSON字符串，失败返回原字符串
     */
    public static String setValue(String json, String path, Object value) {
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            if (rootNode instanceof ObjectNode) {
                ObjectNode objectNode = (ObjectNode) rootNode;
                objectNode.put(path, value.toString());
                return objectMapper.writeValueAsString(objectNode);
            }
        } catch (JsonProcessingException e) {
            log.error("设置JSON值失败: {}", e.getMessage());
        }
        return json;
    }
    
    /**
     * 解析任务配置
     * 
     * @param taskConfig 任务配置JSON
     * @return 任务配置对象
     */
    public static TaskConfig parseTaskConfig(String taskConfig) {
        return parseObject(taskConfig, TaskConfig.class);
    }
    
    /**
     * 序列化任务配置
     * 
     * @param config 任务配置对象
     * @return JSON字符串
     */
    public static String toJsonString(TaskConfig config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            log.error("任务配置转JSON失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 解析上下文数据
     * 
     * @param contextData 上下文数据JSON
     * @return 上下文数据Map
     */
    public static Map<String, Object> parseContextData(String contextData) {
        return parseMap(contextData);
    }
    
    /**
     * 序列化上下文数据
     * 
     * @param contextData 上下文数据Map
     * @return JSON字符串
     */
    public static String toJsonStringContextData(Map<String, Object> contextData) {
        try {
            return objectMapper.writeValueAsString(contextData);
        } catch (Exception e) {
            log.error("Failed to serialize context data: {}", e.getMessage());
            return "{}";
        }
    }
    
    /**
     * 解析步骤日志
     * 
     * @param stepLogs 步骤日志JSON
     * @return 步骤日志列表
     */
    public static List<AsyncTask.StepLog> parseStepLogs(String stepLogs) {
        if (!StringUtils.hasText(stepLogs)) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(stepLogs, new TypeReference<List<AsyncTask.StepLog>>() {});
        } catch (JsonProcessingException e) {
            log.error("解析步骤日志失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 序列化步骤日志
     * 
     * @param stepLogs 步骤日志列表
     * @return JSON字符串
     */
    public static String toJsonString(List<AsyncTask.StepLog> stepLogs) {
        try {
            return objectMapper.writeValueAsString(stepLogs);
        } catch (Exception e) {
            log.error("步骤日志转JSON失败: {}", e.getMessage());
            return "[]";
        }
    }
    
    /**
     * 添加步骤日志
     * 
     * @param stepLogs 原有步骤日志JSON
     * @param stepLog 新步骤日志
     * @return 更新后的步骤日志JSON
     */
    public static String addStepLog(String stepLogs, AsyncTask.StepLog stepLog) {
        try {
            List<AsyncTask.StepLog> logs = parseStepLogs(stepLogs);
            logs.add(stepLog);
            return toJsonString(logs);
        } catch (Exception e) {
            log.error("添加步骤日志失败: {}", e.getMessage());
            return stepLogs;
        }
    }
    
    /**
     * 任务配置类
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TaskConfig {
        private List<String> handlerChain;
        private Integer maxRetries;
        private Integer timeoutSeconds;
        private Integer priority;
        private Map<String, Object> parameters;
    }
    
    /**
     * 创建默认任务配置
     * 
     * @param handlerChain 处理器链
     * @return 任务配置
     */
    public static TaskConfig createDefaultTaskConfig(List<String> handlerChain) {
        return TaskConfig.builder()
                .handlerChain(handlerChain)
                .maxRetries(3)
                .timeoutSeconds(300)
                .priority(5)
                .parameters(new HashMap<>())
                .build();
    }
}