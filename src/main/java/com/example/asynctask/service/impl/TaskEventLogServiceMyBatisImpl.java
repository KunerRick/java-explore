package com.example.asynctask.service.impl;

import com.example.asynctask.entity.TaskEventLog;
import com.example.asynctask.entity.TaskEventLog.EventType;
import com.example.asynctask.mapper.TaskEventLogMapper;
import com.example.asynctask.service.TaskEventLogServiceMyBatis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务事件日志服务实现类（MyBatis实现）
 * 
 * @author System
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class TaskEventLogServiceMyBatisImpl implements TaskEventLogServiceMyBatis {
    
    @Autowired
    private TaskEventLogMapper taskEventLogMapper;
    
    @Override
    @Transactional
    public TaskEventLog logEvent(String taskId, EventType eventType, String eventData, String operator) {
        if (!StringUtils.hasText(taskId)) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        
        if (eventType == null) {
            throw new IllegalArgumentException("事件类型不能为空");
        }
        
        TaskEventLog log = TaskEventLog.builder()
                .taskId(taskId)
                .eventType(eventType)
                .eventData(eventData)
                .operator(operator)
                .createTime(LocalDateTime.now())
                .build();
        
        taskEventLogMapper.insert(log);
        return log;
    }
    
    @Override
    public TaskEventLog getEventLog(Long id) {
        if (id == null) {
            return null;
        }
        
        return taskEventLogMapper.findById(id);
    }
    
    @Override
    public List<TaskEventLog> getEventLogsByTaskId(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return new ArrayList<>();
        }
        
        return taskEventLogMapper.findByTaskIdOrderByCreateTimeDesc(taskId);
    }
    
    @Override
    public List<TaskEventLog> getEventLogsByTaskIdAndEventType(String taskId, EventType eventType) {
        if (!StringUtils.hasText(taskId) || eventType == null) {
            return new ArrayList<>();
        }
        
        return taskEventLogMapper.findByTaskIdAndEventTypeOrderByCreateTimeDesc(taskId, eventType);
    }
    
    @Override
    public Map<String, Object> getEventLogsByPage(EventType eventType, LocalDateTime startTime, LocalDateTime endTime, String operator, int page, int size) {
        int offset = page * size;
        
        List<TaskEventLog> logs = taskEventLogMapper.findByPage(eventType, startTime, endTime, operator, offset, size);
        long total = taskEventLogMapper.countByConditions(eventType, startTime, endTime, operator);
        
        Map<String, Object> result = new HashMap<>();
        result.put("content", logs);
        result.put("totalElements", total);
        result.put("totalPages", (total + size - 1) / size);
        result.put("size", size);
        result.put("number", page);
        result.put("first", page == 0);
        result.put("last", offset >= total - size);
        
        return result;
    }
    
    @Override
    public Map<EventType, Long> countByEventType() {
        List<Object> results = taskEventLogMapper.countByEventType();
        Map<EventType, Long> countMap = new HashMap<>();
        
        for (Object result : results) {
            Object[] row = (Object[]) result;
            EventType eventType = (EventType) row[0];
            Long count = (Long) row[1];
            countMap.put(eventType, count);
        }
        
        return countMap;
    }
    
    @Override
    public Map<EventType, Long> countByEventTypeAndTaskId(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return new HashMap<>();
        }
        
        List<Object> results = taskEventLogMapper.countByEventTypeAndTaskId(taskId);
        Map<EventType, Long> countMap = new HashMap<>();
        
        for (Object result : results) {
            Object[] row = (Object[]) result;
            EventType eventType = (EventType) row[0];
            Long count = (Long) row[1];
            countMap.put(eventType, count);
        }
        
        return countMap;
    }
    
    @Override
    @Transactional
    public int deleteEventLogsBefore(LocalDateTime beforeTime) {
        if (beforeTime == null) {
            return 0;
        }
        
        int rows = taskEventLogMapper.deleteByCreateTimeBefore(beforeTime);
        
        if (rows > 0) {
            log.info("删除事件日志: 时间点={}, 删除行数={}", beforeTime, rows);
        }
        
        return rows;
    }
    
    @Override
    @Transactional
    public int deleteEventLogsByTaskId(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return 0;
        }
        
        int rows = taskEventLogMapper.deleteByTaskId(taskId);
        
        if (rows > 0) {
            log.info("删除任务事件日志: taskId={}, 删除行数={}", taskId, rows);
        }
        
        return rows;
    }
    
    @Override
    public List<TaskEventLog> getRecentEvents(int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        
        return taskEventLogMapper.findRecentEvents(limit);
    }
    
    @Override
    public List<TaskEventLog> getKeyEventsByTaskId(String taskId) {
        if (!StringUtils.hasText(taskId)) {
            return new ArrayList<>();
        }
        
        List<EventType> eventTypes = Arrays.asList(
                EventType.TASK_CREATED,
                EventType.TASK_STARTED,
                EventType.TASK_FINISHED,
                EventType.TASK_FAILED,
                EventType.TASK_CANCELLED
        );
        
        return taskEventLogMapper.findKeyEventsByTaskId(taskId, eventTypes);
    }
    
    @Override
    @Transactional
    public int deleteEventLogById(Long id) {
        if (id == null) {
            return 0;
        }
        
        int rows = taskEventLogMapper.deleteById(id);
        
        if (rows > 0) {
            log.info("删除事件日志: id={}", id);
        }
        
        return rows;
    }
    
    @Override
    @Transactional
    public TaskEventLog logTaskCreated(String taskId, String eventData, String creator) {
        if (!StringUtils.hasText(eventData)) {
            eventData = "任务创建";
        }
        
        return logEvent(taskId, EventType.TASK_CREATED, eventData, creator);
    }
    
    @Override
    @Transactional
    public TaskEventLog logTaskStarted(String taskId, String eventData, String operator) {
        if (!StringUtils.hasText(eventData)) {
            eventData = "任务开始执行";
        }
        
        return logEvent(taskId, EventType.TASK_STARTED, eventData, operator);
    }
    
    @Override
    @Transactional
    public TaskEventLog logTaskFinished(String taskId, String eventData, String operator) {
        if (!StringUtils.hasText(eventData)) {
            eventData = "任务完成";
        }
        
        return logEvent(taskId, EventType.TASK_FINISHED, eventData, operator);
    }
    
    @Override
    @Transactional
    public TaskEventLog logTaskFailed(String taskId, String eventData, String operator) {
        if (!StringUtils.hasText(eventData)) {
            eventData = "任务失败";
        }
        
        return logEvent(taskId, EventType.TASK_FAILED, eventData, operator);
    }
    
    @Override
    @Transactional
    public TaskEventLog logStepStarted(String taskId, String stepName, String eventData, String operator) {
        if (!StringUtils.hasText(eventData)) {
            eventData = "步骤开始: " + stepName;
        }
        
        return logEvent(taskId, EventType.STEP_STARTED, eventData, operator);
    }
    
    @Override
    @Transactional
    public TaskEventLog logStepFinished(String taskId, String stepName, String eventData, String operator) {
        if (!StringUtils.hasText(eventData)) {
            eventData = "步骤完成: " + stepName;
        }
        
        return logEvent(taskId, EventType.STEP_FINISHED, eventData, operator);
    }
    
    @Override
    @Transactional
    public TaskEventLog logStepFailed(String taskId, String stepName, String eventData, String operator) {
        if (!StringUtils.hasText(eventData)) {
            eventData = "步骤失败: " + stepName;
        }
        
        return logEvent(taskId, EventType.STEP_FAILED, eventData, operator);
    }
    
    @Override
    @Transactional
    public TaskEventLog logTaskCancelled(String taskId, String eventData, String operator) {
        if (!StringUtils.hasText(eventData)) {
            eventData = "任务取消";
        }
        
        return logEvent(taskId, EventType.TASK_CANCELLED, eventData, operator);
    }
}