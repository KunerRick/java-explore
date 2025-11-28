package com.example.asynctask.mapper;

import com.example.asynctask.entity.AsyncTask;
import com.example.asynctask.entity.AsyncTask.TaskStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 异步任务Mapper接口
 * 
 * @author System
 */
@Mapper
@Repository
public interface AsyncTaskMapper {
    
    /**
     * 插入任务
     * 
     * @param task 任务对象
     * @return 插入行数
     */
    int insert(AsyncTask task);
    
    /**
     * 根据ID查询任务
     * 
     * @param id 主键ID
     * @return 任务对象
     */
    Optional<AsyncTask> findById(@Param("id") Long id);
    
    /**
     * 根据任务ID查询任务
     * 
     * @param taskId 任务ID
     * @return 任务对象
     */
    Optional<AsyncTask> findByTaskId(@Param("taskId") String taskId);
    
    /**
     * 根据任务ID查询任务（带悲观锁）
     * 
     * @param taskId 任务ID
     * @return 任务对象
     */
    Optional<AsyncTask> findByTaskIdWithLock(@Param("taskId") String taskId);
    
    /**
     * 根据任务类型和状态查询任务
     * 
     * @param taskType 任务类型
     * @param status 任务状态
     * @return 任务列表
     */
    List<AsyncTask> findByTaskTypeAndStatus(@Param("taskType") String taskType, @Param("status") TaskStatus status);
    
    /**
     * 查找待处理任务，按优先级和创建时间排序（带悲观锁）
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> findPendingTasksWithLock(@Param("limit") int limit);
    
    /**
     * 查找待处理任务，按优先级和创建时间排序
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> findPendingTasks(@Param("limit") int limit);
    
    /**
     * 根据任务类型查找待处理任务（带悲观锁）
     * 
     * @param taskType 任务类型
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> findPendingTasksByTaskTypeWithLock(@Param("taskType") String taskType, @Param("limit") int limit);
    
    /**
     * 更新任务
     * 
     * @param task 任务对象
     * @return 更新行数
     */
    int update(AsyncTask task);
    
    /**
     * 更新任务状态（乐观锁）
     * 
     * @param taskId 任务ID
     * @param status 新状态
     * @param version 当前版本
     * @return 更新行数
     */
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
    int updateProgressWithVersion(@Param("taskId") String taskId, @Param("progress") Integer progress, @Param("version") Integer version);
    
    /**
     * 增加重试次数（乐观锁）
     * 
     * @param taskId 任务ID
     * @param version 当前版本
     * @return 更新行数
     */
    int incrementRetryCountWithVersion(@Param("taskId") String taskId, @Param("version") Integer version);
    
    /**
     * 更新步骤日志（乐观锁）
     * 
     * @param taskId 任务ID
     * @param stepLogs 步骤日志
     * @param version 当前版本
     * @return 更新行数
     */
    int updateStepLogsWithVersion(@Param("taskId") String taskId, @Param("stepLogs") String stepLogs, @Param("version") Integer version);
    
    /**
     * 更新错误信息（乐观锁）
     * 
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     * @param version 当前版本
     * @return 更新行数
     */
    int updateErrorMessageWithVersion(@Param("taskId") String taskId, @Param("errorMessage") String errorMessage, @Param("version") Integer version);
    
    /**
     * 统计各状态任务数量
     * 
     * @return 统计结果
     */
    List<Object> countByStatus();
    
    /**
     * 统计指定任务类型的任务状态数量
     * 
     * @param taskType 任务类型
     * @return 统计结果
     */
    List<Object> countByStatusAndTaskType(@Param("taskType") String taskType);
    
    /**
     * 分页查询任务
     * 
     * @param taskType 任务类型
     * @param status 任务状态
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> findByPage(@Param("taskType") String taskType, @Param("status") TaskStatus status, 
                               @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计任务总数
     * 
     * @param taskType 任务类型
     * @param status 任务状态
     * @return 总数
     */
    long countByTaskTypeAndStatus(@Param("taskType") String taskType, @Param("status") TaskStatus status);
    
    /**
     * 查找超时任务
     * 
     * @param status 任务状态
     * @param currentTime 当前时间
     * @return 任务列表
     */
    List<AsyncTask> findTimeoutTasks(@Param("status") TaskStatus status, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 查找需要重试的失败任务
     * 
     * @param limit 限制数量
     * @return 任务列表
     */
    List<AsyncTask> findRetryableFailedTasks(@Param("limit") int limit);
    
    /**
     * 根据ID删除任务
     * 
     * @param id 主键ID
     * @return 删除行数
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据任务ID删除任务
     * 
     * @param taskId 任务ID
     * @return 删除行数
     */
    int deleteByTaskId(@Param("taskId") String taskId);
    
    /**
     * 批量更新任务状态
     * 
     * @param taskIds 任务ID列表
     * @param status 新状态
     * @return 更新行数
     */
    int batchUpdateStatus(@Param("taskIds") List<String> taskIds, @Param("status") TaskStatus status);
    
    /**
     * 批量查询任务
     * 
     * @param taskIds 任务ID列表
     * @return 任务列表
     */
    List<AsyncTask> findByTaskIds(@Param("taskIds") List<String> taskIds);
}