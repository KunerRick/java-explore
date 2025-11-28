package com.example.asynctask.mapper;

import com.example.asynctask.entity.TaskEventLog;
import com.example.asynctask.entity.TaskEventLog.EventType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务事件日志Mapper接口
 * 
 * @author System
 */
@Mapper
@Repository
public interface TaskEventLogMapper {
    
    /**
     * 插入事件日志
     * 
     * @param log 事件日志对象
     * @return 插入行数
     */
    int insert(TaskEventLog log);
    
    /**
     * 根据ID查询事件日志
     * 
     * @param id 主键ID
     * @return 事件日志对象
     */
    TaskEventLog findById(@Param("id") Long id);
    
    /**
     * 根据任务ID查找事件日志，按创建时间倒序
     * 
     * @param taskId 任务ID
     * @return 事件日志列表
     */
    List<TaskEventLog> findByTaskIdOrderByCreateTimeDesc(@Param("taskId") String taskId);
    
    /**
     * 根据任务ID和事件类型查找事件日志
     * 
     * @param taskId 任务ID
     * @param eventType 事件类型
     * @return 事件日志列表
     */
    List<TaskEventLog> findByTaskIdAndEventTypeOrderByCreateTimeDesc(@Param("taskId") String taskId, @Param("eventType") EventType eventType);
    
    /**
     * 分页查询事件日志
     * 
     * @param eventType 事件类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param operator 操作人
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 事件日志列表
     */
    List<TaskEventLog> findByPage(@Param("eventType") EventType eventType, @Param("startTime") LocalDateTime startTime, 
                                  @Param("endTime") LocalDateTime endTime, @Param("operator") String operator,
                                  @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计事件日志总数
     * 
     * @param eventType 事件类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param operator 操作人
     * @return 总数
     */
    long countByConditions(@Param("eventType") EventType eventType, @Param("startTime") LocalDateTime startTime, 
                           @Param("endTime") LocalDateTime endTime, @Param("operator") String operator);
    
    /**
     * 统计各事件类型数量
     * 
     * @return 统计结果
     */
    List<Object> countByEventType();
    
    /**
     * 统计指定任务的事件类型数量
     * 
     * @param taskId 任务ID
     * @return 统计结果
     */
    List<Object> countByEventTypeAndTaskId(@Param("taskId") String taskId);
    
    /**
     * 删除指定时间之前的事件日志
     * 
     * @param beforeTime 时间点
     * @return 删除行数
     */
    int deleteByCreateTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * 删除指定任务的所有事件日志
     * 
     * @param taskId 任务ID
     * @return 删除行数
     */
    int deleteByTaskId(@Param("taskId") String taskId);
    
    /**
     * 查找最近的任务事件
     * 
     * @param limit 限制数量
     * @return 事件日志列表
     */
    List<TaskEventLog> findRecentEvents(@Param("limit") int limit);
    
    /**
     * 查找任务的关键事件（创建、开始、完成、失败）
     * 
     * @param taskId 任务ID
     * @param eventTypes 事件类型列表
     * @return 事件日志列表
     */
    List<TaskEventLog> findKeyEventsByTaskId(@Param("taskId") String taskId, @Param("eventTypes") List<EventType> eventTypes);
    
    /**
     * 根据ID删除事件日志
     * 
     * @param id 主键ID
     * @return 删除行数
     */
    int deleteById(@Param("id") Long id);
}