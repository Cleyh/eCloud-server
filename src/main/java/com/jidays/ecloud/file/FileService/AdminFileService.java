package com.jidays.ecloud.file.FileService;

import com.jidays.ecloud.Entity.EFile;
import com.jidays.ecloud.file.fileMapper.EFileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminFileService {
    @Autowired
    private EFileMapper eFileMapper;
    public List<EFile> getFilesInFolder(Integer fileID, Integer userID) {
        return eFileMapper.getFilesInFolder(fileID, userID);
    }
}
