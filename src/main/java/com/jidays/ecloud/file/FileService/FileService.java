package com.jidays.ecloud.file.FileService;

import com.jidays.ecloud.DTO.EFileDTO;
import com.jidays.ecloud.DTO.MoveFileDTO;
import com.jidays.ecloud.Entity.EFile;
import com.jidays.ecloud.Entity.User;
import com.jidays.ecloud.file.DownloadService.DownloadService;
import com.jidays.ecloud.file.fileMapper.EFileMapper;
import com.jidays.ecloud.user.userMapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class FileService {

    @Autowired
    EFileMapper eFileMapper;
    @Autowired
    FileSystemService fileSystemService;
    @Autowired
    UserMapper userMapper;
    @Autowired
    DownloadService downloadService;

    public EFile initUserFolder(Integer userID) {
        User user = userMapper.getUserByID(userID);
        try {
            if (eFileMapper.checkExistence(user.getUser_name(), 0, userID) != 0 ||
                    fileSystemService.checkUserFolderExist(userID)) {
                return eFileMapper.getFilesByNameID(user.getUser_name(), 0, userID);
            }
            // 创建文件夹
            fileSystemService.iniUserFolder(userID);
            // 创建数据库文件索引
            eFileMapper.addFolder(user.getUser_name(), 0, userID);
            return eFileMapper.getFilesByNameID(user.getUser_name(), 0, userID);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<EFile> getFilesInFolder(Integer fileID, Integer userID) {
        return eFileMapper.getFilesInFolder(fileID, userID);
    }

    public EFile createFile(EFileDTO eFileDTO, Integer userID) {
        int parentID = eFileDTO.getParentID();
        String fileName = eFileDTO.getFileName();

        // 默认文件名
        if (fileName.lastIndexOf(".") == -1) {
            fileName += ".noname";
        }

        // 检查文件名是否重复
        if (eFileMapper.checkExistence(fileName, parentID, userID) > 0) {
            return null;
        }

        // 创建文件
        try {
            String fileTureName = fileSystemService.createFile(fileName, userID);
            eFileMapper.addFile(fileName, fileTureName, parentID, userID);
            return eFileMapper.getFilesByNameID(fileName, parentID, userID);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public EFile createFolder(EFileDTO eFileDTO, Integer userID) {
        int parentID = eFileDTO.getParentID();
        String folderName = eFileDTO.getFileName();

        String regex = "[\\\\/:*?\"<>|]";
        if (folderName.matches(".*" + regex + ".*")) {
            return null;
        }

        if (eFileMapper.checkExistence(folderName, parentID, userID) > 0) {
            return null;
        }

        eFileMapper.addFolder(folderName, parentID, userID);
        return eFileMapper.getFilesByNameID(folderName, parentID, userID);
    }

    public EFile getUserFolder(Integer userID) {
        return eFileMapper.getUserFolder(userID);
    }

    public Boolean checkIdExist(Integer fileID) {
        return eFileMapper.checkExistenceByID(fileID) > 0;
    }

    public boolean deleteById(Integer fileID, Integer userID) {
        EFile file = eFileMapper.getFileById(fileID);
        if (file == null) {
            return false;
        }

        try {
            if (file.getFile_type().equals("folder")) {
                List<EFile> files = eFileMapper.getAllSubfiles(fileID);
                fileSystemService.deleteFiles(files, userID);
                List<Integer> idList = files.stream().map(EFile::getFile_id).toList();
                eFileMapper.deleteFilesByIds(idList);
            } else {
                fileSystemService.deleteFile(file, userID);
                eFileMapper.deleteFileById(file.getFile_id());
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public EFile rename(Integer fileID, String newName, Integer userID) {
        // 检查文件名是否合格
        String regex = "[\\\\/:*?\"<>|]";
        if (newName.matches(".*" + regex + ".*")) {
            return null;
        }

        // 检查文件是否存在
        EFile file = eFileMapper.getFileById(fileID);
        if (file == null) return null;

        if (file.getFile_type().equals("file") && newName.lastIndexOf(".") == -1) {
            newName += ".noname";
        }

        if (eFileMapper.getFilesByNameID(newName, file.getParent_id(), userID) != null) {
            return null;
        }

        String newTrueName = "";
        if (file.getFile_type().equals("file")) {
            try {
                newTrueName = fileSystemService.renameFile(file, newName);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        eFileMapper.rename(fileID, newName, newTrueName);
        return eFileMapper.getFileById(fileID);
    }

    public String getFileDownload(Integer fileID, Integer userID) {
        EFile file = eFileMapper.getFileById(fileID);
        if (file == null ||
                file.getFile_type().equals("folder")) {
            return null;
        }

        return downloadService.createToken(file);
    }

    public boolean addFile(EFile fileProperties, InputStream fileContent) {
        if (fileProperties.getFile_type().equals("folder") ||
                !checkIdExist(fileProperties.getParent_id())) {
            return false;
        }
        try {
            String fileTrueName = fileSystemService.createFileFromStream(
                    fileProperties.getUser_id(),
                    fileProperties.getFile_name(),
                    fileContent);
            eFileMapper.addFile(
                    fileProperties.getFile_name(),
                    fileTrueName,
                    fileProperties.getParent_id(),
                    fileProperties.getUser_id());
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean cloneFile(MoveFileDTO moveFileDTO, Integer userID) {
        EFile targetFile = eFileMapper.getFileById(moveFileDTO.getFileID());
        EFile newParentFile = eFileMapper.getFileById(moveFileDTO.getNewParentID());
        if (targetFile == null || newParentFile == null){
            return false;
        }
        try {
            String fileTrueName = fileSystemService.copyFileToPath(targetFile, newParentFile);
            eFileMapper.addFile(
                    targetFile.getFile_name(),
                    fileTrueName,
                    newParentFile.getFile_id(),
                    userID);
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
