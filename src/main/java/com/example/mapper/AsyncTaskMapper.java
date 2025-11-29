package com.example.mapper;

import com.example.model.AsyncTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AsyncTaskMapper {
    
    int insert(AsyncTask asyncTask);
    
    int updateById(AsyncTask asyncTask);
    
    int updateProgressAndStatus(@Param("taskId") String taskId, 
                                @Param("progress") Integer progress, 
                                @Param("status") String status,
                                @Param("currentStep") String currentStep,
                                @Param("contextData") String contextData,
                                @Param("stepLogs") String stepLogs,
                                @Param("errorMessage") String errorMessage,
                                @Param("version") Integer version);
    
    int updateByTaskId(AsyncTask asyncTask);
    
    AsyncTask selectByTaskId(@Param("taskId") String taskId);
    
    AsyncTask selectById(@Param("id") Long id);
    
    List<AsyncTask> selectByStatus(@Param("status") String status);
    
    List<AsyncTask> selectByTypeAndStatus(@Param("taskType") String taskType, 
                                          @Param("status") String status);
    
    List<AsyncTask> selectByStatusAndPriority(@Param("status") String status, 
                                             @Param("priority") Integer priority);
    
    List<AsyncTask> selectPendingTasks(@Param("limit") Integer limit);
    
    List<AsyncTask> selectTasksToRecover();
}