package com.jidays.ecloud.file.FileService;

import com.jidays.ecloud.Entity.EFile;
import com.jidays.ecloud.Entity.User;
import com.jidays.ecloud.user.userMapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.*;
import java.io.IOException;
import java.util.List;

@Service
public class FileSystemService {

    @Autowired
    UserMapper userMapper;

    private static final Logger logger = LoggerFactory.getLogger(FileSystemService.class);

    private static final String ROOT_DIR = System.getProperty("user.dir") + "/ecloudDir";

    // 基于userID和路径构造完整路径
    public Path constructPath(Integer userID) throws IOException {
        User user = userMapper.getUserByID(userID);
        if (user == null) {
            String message = "ID为 " + userID + " 的用户不存在。";
            logger.error(message);
            throw new IllegalArgumentException(message);
        }
        String userDir = user.getUser_name();
        Path path = Paths.get(ROOT_DIR, userDir);
        System.out.println(path);
        return path;
    }

    public void iniUserFolder(Integer userID) throws IOException {
        Path userPath = constructPath(userID);
        if (!Files.exists(userPath)) {
            Files.createDirectories(userPath);
        }
    }

    public String createFile(String fileName, Integer userID) throws IOException {
        Path userPath = constructPath(userID);

        String filePureName = fileName.substring(0, fileName.lastIndexOf("."));
        String typeName = fileName.substring(fileName.lastIndexOf(".") + 1);

        Path filePath = Paths.get(userPath.toString(), fileName);
        for (int i = 0; ; i++) {
            if (Files.exists(filePath)) {
                filePath = userPath.resolve(
                        String.format("%s(%d).%s", filePureName, i, typeName));
            } else break;
        }

        Files.createFile(filePath);
        return filePath.getFileName().toString();
    }

    public boolean checkUserFolderExist(Integer userID) throws IOException {
        Path userPath = constructPath(userID);
        return Files.exists(userPath);
    }

    public void deleteFiles(List<EFile> files, Integer userID) throws IOException {
        Path userPath = constructPath(userID);
        for (EFile file : files) {
            if (file.getFile_type().equals("folder")) {
                continue;
            }

            Path filePath = Paths.get(userPath.toString(), file.getFile_true_name());
            Files.delete(filePath);
        }
    }

    public void deleteFile(EFile file, Integer userID) throws IOException {
        Path filePath = constructPath(userID).resolve(file.getFile_true_name());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        } else {
            logger.error("文件不存在:{}", filePath);
            throw new IllegalArgumentException("文件不存在");
        }
    }

    public String renameFile(EFile file, String newName) throws IOException {
        // 检查文件类型
        if (file.getFile_type().equals("folder")) {
            throw new IllegalArgumentException("文件夹不能重命名");
        }

        // 构造用户目录路径
        Path userPath = constructPath(file.getUser_id());

        // 检查文件是否存在
        Path oldFilePath = userPath.resolve(file.getFile_true_name());
        if (!Files.exists(oldFilePath)) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 分离新文件名的名称和扩展名部分
        String newNamePure;
        String newNameType;
        int lastDotIndex = newName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            newNamePure = newName.substring(0, lastDotIndex);
            newNameType = newName.substring(lastDotIndex + 1);
        } else {
            newNamePure = newName;
            newNameType = "noname"; // 设置默认扩展名为 noname
        }

        // 构造新的文件名，检查是否存在同名文件
        String newFileName = String.format("%s.%s", newNamePure, newNameType);
        Path newFilePath = userPath.resolve(newFileName);
        for (int i = 1; Files.exists(newFilePath); i++) {
            newFileName = String.format("%s(%d).%s", newNamePure, i, newNameType);
            newFilePath = userPath.resolve(newFileName);
        }

        // 重命名文件
        Files.move(oldFilePath, newFilePath);
        logger.info("File renamed from {} to {}", oldFilePath, newFilePath);
        return newFilePath.getFileName().toString();
    }

    public InputStreamResource getFileResource(EFile file) throws IOException {
        Path filePath = constructPath(file.getUser_id()).resolve(file.getFile_true_name());
        return new InputStreamResource(Files.newInputStream(filePath));
    }

    public String createFileFromStream(int userId, String fileName, InputStream fileContent) throws IOException {
        Path userPath = constructPath(userId);
        Path filePath = userPath.resolve(fileName);
        // 分离新文件名，并进行检查
        String namePure;
        String nameType;
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            namePure = fileName.substring(0, lastDotIndex);
            nameType = fileName.substring(lastDotIndex + 1);
        } else {
            namePure = fileName;
            nameType = "noname"; // 设置默认扩展名为 noname
        }
        // 构造新的文件名，检查是否存在同名文件
        String newFileName = String.format("%s.%s", namePure, nameType);
        for (int i = 1; Files.exists(filePath); i++) {
            newFileName = String.format("%s(%d).%s", namePure, i, nameType);
            filePath = userPath.resolve(newFileName);
        }
        // 写入文件
        Files.copy(fileContent, filePath, StandardCopyOption.REPLACE_EXISTING);
        return newFileName;
    }

    public String copyFileToPath(EFile targetFile, EFile newParentFile) throws IOException {
        if (targetFile.getFile_type().equals("folder") || !newParentFile.getFile_type().equals("folder")) {
            return null;
        }

        // 检查源文件
        Path userPath = constructPath(newParentFile.getUser_id());
        Path sourcePath = userPath.resolve(targetFile.getFile_true_name());
        if (!Files.exists(sourcePath)) {
            throw new IllegalArgumentException("文件不存在");
        }

        // 准备存入该路径
        String fileName = targetFile.getFile_name();
        Path targetPath = userPath.resolve(fileName);
        // 分离文件名，并进行检查
        String namePure;
        String nameType;
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            namePure = fileName.substring(0, lastDotIndex);
            nameType = fileName.substring(lastDotIndex + 1);
        } else {
            namePure = fileName;
            nameType = "noname"; // 设置默认扩展名为 noname
        }
        // 构造新的文件名，检查是否存在同名文件
        String newFileName = String.format("%s.%s", namePure, nameType);
        for (int i = 1; Files.exists(targetPath); i++) {
            newFileName = String.format("%s(%d).%s", namePure, i, nameType);
            targetPath = userPath.resolve(newFileName);
        }

        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.getFileName().toString();
    }
}
