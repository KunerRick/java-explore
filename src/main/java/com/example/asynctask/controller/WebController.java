package com.example.asynctask.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web页面控制器
 * 
 * @author System
 */
@Controller
public class WebController {
    
    /**
     * 首页
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    /**
     * 任务列表页面
     */
    @GetMapping("/tasks")
    public String tasks() {
        return "tasks";
    }
    
    /**
     * 任务详情页面
     */
    @GetMapping("/task-detail")
    public String taskDetail() {
        return "task-detail";
    }
}