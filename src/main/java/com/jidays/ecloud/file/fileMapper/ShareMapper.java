package com.jidays.ecloud.file.fileMapper;

import com.jidays.ecloud.Entity.EFile;
import com.jidays.ecloud.Entity.Share;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShareMapper {
    void createShare(@Param("fileID") int fileID,
                     @Param("password") String password,
                     @Param("expireTime") String expireTime,
                     @Param("disposable") boolean disposable);

    void createMultipleShare(@Param("fileId") int fileId,
                             @Param("password") String password,
                             @Param("expireTime") String expireTime,
                             @Param("isDisposable") boolean isDisposable,
                             @Param("userIDs") List<Integer> userIDs);

    Share getShare(@Param("shareID") Integer shareID);

    void deleteShare(@Param("shareID") Integer shareID);

    List<Share> getSharedFiles(@Param("userID")Integer userID);

    List<Share> getMySharedFiles(@Param("userID") Integer userID);

    String getDownloadToken(@Param("shareID") Integer shareID);

    void updateDownloadToken(@Param("shareID") Integer shareID,@Param("token") String token);
}
