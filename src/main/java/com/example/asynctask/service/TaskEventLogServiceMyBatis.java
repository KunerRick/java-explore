package com.example.asynctask.service;

import com.example.asynctask.entity.TaskEventLog;
import com.example.asynctask.entity.TaskEventLog.EventType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务事件日志服务接口（MyBatis实现）
 * 
 * @author System
 */
public interface TaskEventLogServiceMyBatis {
    
    /**
     * 记录事件日志
     * 
     * @param taskId 任务ID
     * @param eventType 事件类型
     * @param eventData 事件数据
     * @param operator 操作人
     * @return 事件日志对象
     */
    TaskEventLog logEvent(String taskId, EventType eventType, String eventData, String operator);
    
    /**
     * 获取事件日志
     * 
     * @param id 主键ID
     * @return 事件日志对象
     */
    TaskEventLog getEventLog(Long id);
    
    /**
     * 根据任务ID获取事件日志，按创建时间倒序
     * 
     * @param taskId 任务ID
     * @return 事件日志列表
     */
    List<TaskEventLog> getEventLogsByTaskId(String taskId);
    
    /**
     * 根据任务ID和事件类型获取事件日志
     * 
     * @param taskId 任务ID
     * @param eventType 事件类型
     * @return 事件日志列表
     */
    List<TaskEventLog> getEventLogsByTaskIdAndEventType(String taskId, EventType eventType);
    
    /**
     * 分页查询事件日志
     * 
     * @param eventType 事件类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param operator 操作人
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 事件日志分页结果
     */
    Map<String, Object> getEventLogsByPage(EventType eventType, LocalDateTime startTime, LocalDateTime endTime, String operator, int page, int size);
    
    /**
     * 统计各事件类型数量
     * 
     * @return 统计结果
     */
    Map<EventType, Long> countByEventType();
    
    /**
     * 统计指定任务的事件类型数量
     * 
     * @param taskId 任务ID
     * @return 统计结果
     */
    Map<EventType, Long> countByEventTypeAndTaskId(String taskId);
    
    /**
     * 删除指定时间之前的事件日志
     * 
     * @param beforeTime 时间点
     * @return 删除行数
     */
    int deleteEventLogsBefore(LocalDateTime beforeTime);
    
    /**
     * 删除指定任务的所有事件日志
     * 
     * @param taskId 任务ID
     * @return 删除行数
     */
    int deleteEventLogsByTaskId(String taskId);
    
    /**
     * 获取最近的任务事件
     * 
     * @param limit 限制数量
     * @return 事件日志列表
     */
    List<TaskEventLog> getRecentEvents(int limit);
    
    /**
     * 获取任务的关键事件（创建、开始、完成、失败）
     * 
     * @param taskId 任务ID
     * @return 事件日志列表
     */
    List<TaskEventLog> getKeyEventsByTaskId(String taskId);
    
    /**
     * 根据ID删除事件日志
     * 
     * @param id 主键ID
     * @return 删除行数
     */
    int deleteEventLogById(Long id);
    
    /**
     * 记录任务创建事件
     * 
     * @param taskId 任务ID
     * @param eventData 事件数据
     * @param creator 创建人
     * @return 事件日志对象
     */
    TaskEventLog logTaskCreated(String taskId, String eventData, String creator);
    
    /**
     * 记录任务开始事件
     * 
     * @param taskId 任务ID
     * @param eventData 事件数据
     * @param operator 操作人
     * @return 事件日志对象
     */
    TaskEventLog logTaskStarted(String taskId, String eventData, String operator);
    
    /**
     * 记录任务完成事件
     * 
     * @param taskId 任务ID
     * @param eventData 事件数据
     * @param operator 操作人
     * @return 事件日志对象
     */
    TaskEventLog logTaskFinished(String taskId, String eventData, String operator);
    
    /**
     * 记录任务失败事件
     * 
     * @param taskId 任务ID
     * @param eventData 事件数据
     * @param operator 操作人
     * @return 事件日志对象
     */
    TaskEventLog logTaskFailed(String taskId, String eventData, String operator);
    
    /**
     * 记录步骤开始事件
     * 
     * @param taskId 任务ID
     * @param stepName 步骤名称
     * @param eventData 事件数据
     * @param operator 操作人
     * @return 事件日志对象
     */
    TaskEventLog logStepStarted(String taskId, String stepName, String eventData, String operator);
    
    /**
     * 记录步骤完成事件
     * 
     * @param taskId 任务ID
     * @param stepName 步骤名称
     * @param eventData 事件数据
     * @param operator 操作人
     * @return 事件日志对象
     */
    TaskEventLog logStepFinished(String taskId, String stepName, String eventData, String operator);
    
    /**
     * 记录步骤失败事件
     * 
     * @param taskId 任务ID
     * @param stepName 步骤名称
     * @param eventData 事件数据
     * @param operator 操作人
     * @return 事件日志对象
     */
    TaskEventLog logStepFailed(String taskId, String stepName, String eventData, String operator);
    
    /**
     * 记录任务取消事件
     * 
     * @param taskId 任务ID
     * @param eventData 事件数据
     * @param operator 操作人
     * @return 事件日志对象
     */
    TaskEventLog logTaskCancelled(String taskId, String eventData, String operator);
}