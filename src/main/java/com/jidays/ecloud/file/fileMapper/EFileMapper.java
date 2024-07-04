package com.jidays.ecloud.file.fileMapper;

import com.jidays.ecloud.Entity.EFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EFileMapper {
    List<EFile> getFilesInFolder(@Param("fileID") Integer fileID,
                                 @Param("userID") Integer userID);

    void addFile(@Param("fileName") String fileName,
                 @Param("fileTrueName") String fileTrueName,
                 @Param("parentId") Integer parentId,
                 @Param("userID") Integer userID);

    void addFolder(@Param("fileName") String fileName,
                   @Param("parentId") Integer parentId,
                   @Param("userID") Integer userID);

    Integer checkExistence(@Param("fileName") String fileName,
                           @Param("parentID") Integer parentID,
                           @Param("userID") Integer userID);

    EFile getFilesByNameID(@Param("fileName") String fileName,
                           @Param("parentID") Integer parentID,
                           @Param("userID") Integer userID);

    Integer checkExistenceByID(@Param("fileID") Integer fileID);

    EFile getUserFolder(@Param("userID") Integer userID);

    EFile getFileById(@Param("fileID") Integer fileID);

    List<EFile> getAllSubfiles(@Param("fileID") Integer fileID);

    void deleteFilesByIds(@Param("idList") List<Integer> idList);

    void deleteFileById(@Param("fileID") Integer fileID);

    void rename(@Param("fileID") Integer fileID,
                @Param("newName") String newName,
                @Param("newTrueName") String newTrueName);

    String getUniqueName(@Param("fileName") String fileName, @Param("parentId") Integer fileParentId);

    List<EFile> getFileChainById(@Param("fileID") Integer fileID);
}
