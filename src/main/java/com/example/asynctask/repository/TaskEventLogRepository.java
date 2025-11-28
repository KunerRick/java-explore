package com.example.asynctask.repository;

import com.example.asynctask.entity.TaskEventLog;
import com.example.asynctask.entity.TaskEventLog.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务事件日志数据访问层
 * 
 * @author System
 */
@Repository
public interface TaskEventLogRepository extends JpaRepository<TaskEventLog, Long> {
    
    /**
     * 根据任务ID查找事件日志，按创建时间倒序
     * 
     * @param taskId 任务ID
     * @return 事件日志列表
     */
    List<TaskEventLog> findByTaskIdOrderByCreateTimeDesc(String taskId);
    
    /**
     * 根据任务ID和事件类型查找事件日志
     * 
     * @param taskId 任务ID
     * @param eventType 事件类型
     * @return 事件日志列表
     */
    List<TaskEventLog> findByTaskIdAndEventTypeOrderByCreateTimeDesc(String taskId, EventType eventType);
    
    /**
     * 根据事件类型查找事件日志，分页
     * 
     * @param eventType 事件类型
     * @param pageable 分页参数
     * @return 事件日志分页结果
     */
    Page<TaskEventLog> findByEventTypeOrderByCreateTimeDesc(EventType eventType, Pageable pageable);
    
    /**
     * 根据时间范围查找事件日志，分页
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 事件日志分页结果
     */
    Page<TaskEventLog> findByCreateTimeBetweenOrderByCreateTimeDesc(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 统计各事件类型数量
     * 
     * @return 统计结果
     */
    @Query("SELECT tel.eventType, COUNT(tel) FROM TaskEventLog tel GROUP BY tel.eventType")
    List<Object[]> countByEventType();
    
    /**
     * 统计指定任务的事件类型数量
     * 
     * @param taskId 任务ID
     * @return 统计结果
     */
    @Query("SELECT tel.eventType, COUNT(tel) FROM TaskEventLog tel WHERE tel.taskId = :taskId GROUP BY tel.eventType")
    List<Object[]> countByEventTypeAndTaskId(@Param("taskId") String taskId);
    
    /**
     * 删除指定时间之前的事件日志
     * 
     * @param beforeTime 时间点
     * @return 删除行数
     */
    @Modifying
    @Query("DELETE FROM TaskEventLog tel WHERE tel.createTime < :beforeTime")
    int deleteByCreateTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * 删除指定任务的所有事件日志
     * 
     * @param taskId 任务ID
     * @return 删除行数
     */
    @Modifying
    @Query("DELETE FROM TaskEventLog tel WHERE tel.taskId = :taskId")
    int deleteByTaskId(@Param("taskId") String taskId);
    
    /**
     * 查找最近的任务事件
     * 
     * @param limit 限制数量
     * @return 事件日志列表
     */
    @Query(value = "SELECT * FROM task_event_log ORDER BY create_time DESC LIMIT ?1", nativeQuery = true)
    List<TaskEventLog> findRecentEvents(int limit);
    
    /**
     * 根据操作人查找事件日志，分页
     * 
     * @param operator 操作人
     * @param pageable 分页参数
     * @return 事件日志分页结果
     */
    Page<TaskEventLog> findByOperatorOrderByCreateTimeDesc(String operator, Pageable pageable);
    
    /**
     * 查找任务的关键事件（创建、开始、完成、失败）
     * 
     * @param taskId 任务ID
     * @return 事件日志列表
     */
    @Query("SELECT tel FROM TaskEventLog tel WHERE tel.taskId = :taskId AND tel.eventType IN :eventTypes ORDER BY tel.createTime")
    List<TaskEventLog> findKeyEventsByTaskId(@Param("taskId") String taskId, @Param("eventTypes") List<EventType> eventTypes);
}