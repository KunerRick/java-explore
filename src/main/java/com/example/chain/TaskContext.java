package com.example.chain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TaskContext {
    private String taskId;
    private String taskType;
    private Map<String, Object> data = new HashMap<>();
    private Map<String, String> stepResults = new HashMap<>();
    private boolean shouldContinue = true;
    private String errorMessage;
    
    public void setData(String key, Object value) {
        data.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getData(String key) {
        return (T) data.get(key);
    }
    
    public void setStepResult(String stepName, String result) {
        stepResults.put(stepName, result);
    }
    
    public String getStepResult(String stepName) {
        return stepResults.get(stepName);
    }
}