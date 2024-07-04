package com.jidays.ecloud.file.fileMapper;

import com.jidays.ecloud.Entity.DownloadTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DownloadTaskMapper {
    void addDownloadTask(@Param("task") DownloadTask downloadTask);

    void updateTask(@Param("task") DownloadTask task);

    DownloadTask getTaskByUUID(@Param("taskID") String taskID);

    List<DownloadTask> getTaskList(@Param("userID") Integer userID);

    List<DownloadTask> getAllTaskList();

    void deleteTask(@Param("task") DownloadTask task);
}
