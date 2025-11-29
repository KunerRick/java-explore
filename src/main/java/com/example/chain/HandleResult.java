package com.example.chain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandleResult {
    private boolean success;
    private String message;
    private boolean shouldContinue;
    
    public static HandleResult success(String message) {
        return new HandleResult(true, message, true);
    }
    
    public static HandleResult success(String message, boolean shouldContinue) {
        return new HandleResult(true, message, shouldContinue);
    }
    
    public static HandleResult failure(String message) {
        return new HandleResult(false, message, false);
    }
    
    public static HandleResult failure(String message, boolean shouldContinue) {
        return new HandleResult(false, message, shouldContinue);
    }
}