package com.example.asynctask.repository;

import com.example.asynctask.entity.AsyncTask;
import com.example.asynctask.entity.AsyncTask.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 异步任务数据访问层
 * 
 * @author System
 */
@Repository
public interface AsyncTaskRepository extends JpaRepository<AsyncTask, Long> {
    
    /**
     * 根据任务ID查找任务（带悲观锁）
     * 
     * @param taskId 任务ID
     * @return 任务对象
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM AsyncTask t WHERE t.taskId = :taskId")
    Optional<AsyncTask> findByTaskIdWithLock(@Param("taskId") String taskId);
    
    /**
     * 根据任务ID查找任务
     * 
     * @param taskId 任务ID
     * @return 任务对象
     */
    Optional<AsyncTask> findByTaskId(String taskId);
    
    /**
     * 根据任务类型和状态查找任务
     * 
     * @param taskType 任务类型
     * @param status 任务状态
     * @return 任务列表
     */
    List<AsyncTask> findByTaskTypeAndStatus(String taskType, TaskStatus status);
    
    /**
     * 查找待处理任务，按优先级和创建时间排序（带悲观锁）
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    @Query(value = "SELECT * FROM async_task WHERE status = 'PENDING' ORDER BY priority DESC, created_time ASC LIMIT ?1", 
           nativeQuery = true)
    List<AsyncTask> findPendingTasksWithLock(int limit);
    
    /**
     * 查找待处理任务，按优先级和创建时间排序
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    @Query(value = "SELECT * FROM async_task WHERE status = 'PENDING' ORDER BY priority DESC, created_time ASC LIMIT ?1", 
           nativeQuery = true)
    List<AsyncTask> findPendingTasks(int limit);
    
    /**
     * 根据任务类型查找待处理任务（带悲观锁）
     * 
     * @param taskType 任务类型
     * @param limit 限制数量
     * @return 任务列表
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(value = "SELECT * FROM async_task WHERE task_type = :taskType AND status = 'PENDING' ORDER BY priority DESC, created_time ASC LIMIT :limit", 
           nativeQuery = true)
    List<AsyncTask> findPendingTasksByTaskTypeWithLock(@Param("taskType") String taskType, @Param("limit") int limit);
    
    /**
     * 更新任务状态（乐观锁）
     * 
     * @param taskId 任务ID
     * @param status 新状态
     * @param version 当前版本
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE AsyncTask t SET t.status = :status, t.version = t.version + 1 WHERE t.taskId = :taskId AND t.version = :version")
    int updateStatusWithVersion(@Param("taskId") String taskId, @Param("status") TaskStatus status, @Param("version") Integer version);
    
    /**
     * 更新任务状态和当前步骤（乐观锁）
     * 
     * @param taskId 任务ID
     * @param status 新状态
     * @param currentStep 当前步骤
     * @param version 当前版本
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE AsyncTask t SET t.status = :status, t.currentStep = :currentStep, t.version = t.version + 1 WHERE t.taskId = :taskId AND t.version = :version")
    int updateStatusAndStepWithVersion(@Param("taskId") String taskId, @Param("status") TaskStatus status, 
                                       @Param("currentStep") String currentStep, @Param("version") Integer version);
    
    /**
     * 更新任务状态、当前步骤和上下文数据（乐观锁）
     * 
     * @param taskId 任务ID
     * @param status 新状态
     * @param currentStep 当前步骤
     * @param contextData 上下文数据
     * @param version 当前版本
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE AsyncTask t SET t.status = :status, t.currentStep = :currentStep, t.contextData = :contextData, t.version = t.version + 1 WHERE t.taskId = :taskId AND t.version = :version")
    int updateStatusStepAndContextWithVersion(@Param("taskId") String taskId, @Param("status") TaskStatus status, 
                                              @Param("currentStep") String currentStep, @Param("contextData") String contextData, 
                                              @Param("version") Integer version);
    
    /**
     * 更新进度（乐观锁）
     * 
     * @param taskId 任务ID
     * @param progress 进度
     * @param version 当前版本
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE AsyncTask t SET t.progress = :progress, t.version = t.version + 1 WHERE t.taskId = :taskId AND t.version = :version")
    int updateProgressWithVersion(@Param("taskId") String taskId, @Param("progress") Integer progress, @Param("version") Integer version);
    
    /**
     * 增加重试次数（乐观锁）
     * 
     * @param taskId 任务ID
     * @param version 当前版本
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE AsyncTask t SET t.retryCount = t.retryCount + 1, t.version = t.version + 1 WHERE t.taskId = :taskId AND t.version = :version")
    int incrementRetryCountWithVersion(@Param("taskId") String taskId, @Param("version") Integer version);
    
    /**
     * 更新步骤日志（乐观锁）
     * 
     * @param taskId 任务ID
     * @param stepLogs 步骤日志
     * @param version 当前版本
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE AsyncTask t SET t.stepLogs = :stepLogs, t.version = t.version + 1 WHERE t.taskId = :taskId AND t.version = :version")
    int updateStepLogsWithVersion(@Param("taskId") String taskId, @Param("stepLogs") String stepLogs, @Param("version") Integer version);
    
    /**
     * 更新错误信息（乐观锁）
     * 
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     * @param version 当前版本
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE AsyncTask t SET t.errorMessage = :errorMessage, t.version = t.version + 1 WHERE t.taskId = :taskId AND t.version = :version")
    int updateErrorMessageWithVersion(@Param("taskId") String taskId, @Param("errorMessage") String errorMessage, @Param("version") Integer version);
    
    /**
     * 统计各状态任务数量
     * 
     * @return 统计结果
     */
    @Query("SELECT t.status, COUNT(t) FROM AsyncTask t GROUP BY t.status")
    List<Object[]> countByStatus();
    
    /**
     * 统计指定任务类型的任务状态数量
     * 
     * @param taskType 任务类型
     * @return 统计结果
     */
    @Query("SELECT t.status, COUNT(t) FROM AsyncTask t WHERE t.taskType = :taskType GROUP BY t.status")
    List<Object[]> countByStatusAndTaskType(@Param("taskType") String taskType);
    
    /**
     * 根据任务类型分页查询任务
     * 
     * @param taskType 任务类型
     * @param pageable 分页参数
     * @return 任务分页结果
     */
    Page<AsyncTask> findByTaskType(String taskType, Pageable pageable);
    
    /**
     * 根据状态分页查询任务
     * 
     * @param status 任务状态
     * @param pageable 分页参数
     * @return 任务分页结果
     */
    Page<AsyncTask> findByStatus(AsyncTask.TaskStatus status, Pageable pageable);
    
    /**
     * 查找超时任务
     * 
     * @param status 任务状态
     * @param currentTime 当前时间戳
     * @return 任务列表
     */
    @Query(value = "SELECT * FROM async_task WHERE status = :status AND timeout_seconds > 0 AND " +
           "start_time IS NOT NULL AND TIMESTAMPDIFF(SECOND, start_time, :currentTime) > timeout_seconds", 
           nativeQuery = true)
    List<AsyncTask> findTimeoutTasks(@Param("status") TaskStatus status, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 查找需要重试的失败任务
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    @Query(value = "SELECT * FROM async_task WHERE status = 'FAILED' AND retry_count < max_retries " +
                   "ORDER BY priority DESC, created_time ASC LIMIT ?1", 
           nativeQuery = true)
    List<AsyncTask> findRetryableFailedTasks(int limit);
}