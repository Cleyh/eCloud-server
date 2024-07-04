package com.jidays.ecloud.file;

import com.jidays.ecloud.DTO.DownloadTaskDTO;
import com.jidays.ecloud.DTO.EFileDTO;
import com.jidays.ecloud.DTO.MoveFileDTO;
import com.jidays.ecloud.DTO.ShareFileDTO;
import com.jidays.ecloud.Entity.DownloadTask;
import com.jidays.ecloud.Entity.EFile;
import com.jidays.ecloud.Entity.Share;
import com.jidays.ecloud.file.DownloadService.RemoteDownloadService;
import com.jidays.ecloud.file.FileService.FileService;
import com.jidays.ecloud.file.FileService.ShareService;
import com.jidays.ecloud.file.fileMapper.EFileMapper;
import com.jidays.ecloud.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/userFile")
public class FileController {
    @Autowired
    FileService fileService;
    @Autowired
    RemoteDownloadService remoteDownloadService;
    @Autowired
    ShareService shareService;
    @Autowired
    EFileMapper efileMapper;

    @GetMapping("/getUserFolder")
    public ResponseEntity<ApiResponse> getRoot(@RequestAttribute("user_id") Integer userID) {
        EFile file = fileService.getUserFolder(userID);
        return ResponseEntity.ok(ApiResponse.success("Success", file));
    }

    @GetMapping("/getFilesInFolder")
    public ResponseEntity<ApiResponse> getDirectory(@RequestParam("folderID") Integer folderID,
                                                    @RequestAttribute("user_id") Integer userID) {
        if (!fileService.checkIdExist(folderID)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }
        List<EFile> files = fileService.getFilesInFolder(folderID, userID);
        return ResponseEntity.ok(ApiResponse.success("Success", files));
    }

    @GetMapping("/getFile")
    public ResponseEntity<ApiResponse> getFile(@RequestParam("fileID") Integer fileID,
                                                @RequestAttribute("user_id") Integer userID) {
        if (!fileService.checkIdExist(fileID)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }
        EFile file = efileMapper.getFileById(fileID);
        return ResponseEntity.ok(ApiResponse.success("Success", file));
    }

    @GetMapping("/getFileChain")
    public ResponseEntity<ApiResponse> getFileChain(@RequestParam("fileID") Integer fileID,
                                               @RequestAttribute("user_id") Integer userID) {
        if (!fileService.checkIdExist(fileID)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }
        List<EFile> file = efileMapper.getFileChainById(fileID);
        return ResponseEntity.ok(ApiResponse.success("Success", file));
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createFile(@RequestBody EFileDTO fileDTO,
                                                  @RequestAttribute("user_id") Integer userID) {
        if (!fileService.checkIdExist(fileDTO.getParentID())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Folder not found", null));
        }
        EFile result;
        if (fileDTO.getFileType().equals("file")) {
            result = fileService.createFile(fileDTO, userID);
        } else {
            result = fileService.createFolder(fileDTO, userID);
        }
        if (result == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File already exists", null));
        } else {
            return ResponseEntity.ok().body(ApiResponse.success("Success", result));
        }
    }

    // 硬删除
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse> deleteFileHard(@RequestParam("fileID") Integer fileID,
                                                      @RequestAttribute("user_id") Integer userID) {
        if (!fileService.checkIdExist(fileID)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }

        if (fileService.deleteById(fileID, userID)) {
            return ResponseEntity.ok().body(ApiResponse.success("Success", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }
    }

    // 重命名
    @PostMapping("/rename")
    public ResponseEntity<ApiResponse> renameFile(@RequestParam("fileID") Integer fileID,
                                                  @RequestParam("newName") String newName,
                                                  @RequestAttribute("user_id") Integer userID) {
        if (!fileService.checkIdExist(fileID)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }

        EFile file = fileService.rename(fileID, newName, userID);
        if (file == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Fail to rename", null));
        } else {
            return ResponseEntity.ok().body(ApiResponse.success("Success to rename", file));
        }
    }

    // 复制
    @PostMapping("/clone")
    public ResponseEntity<ApiResponse> cloneFile(@RequestBody MoveFileDTO moveFileDTO,
                                                 @RequestAttribute("user_id") Integer userID) {
        if (fileService.cloneFile(moveFileDTO, userID)) {
            return ResponseEntity.ok().body(ApiResponse.success("Success", null));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Fail to clone", null));
    }

    // 移动
    @PostMapping("/move")
    public ResponseEntity<ApiResponse> moveFile(@RequestBody MoveFileDTO moveFileDTO,
                                                @RequestAttribute("user_id") Integer userID) {
        if (!fileService.checkIdExist(moveFileDTO.getFileID())) {

        }
        return null;
    }

    @GetMapping("/getDownload")
    public ResponseEntity<ApiResponse> downloadFile(@RequestParam("fileID") Integer fileID,
                                                    @RequestAttribute("user_id") Integer userID) {
        String token = fileService.getFileDownload(fileID, userID);
        if (token == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }
        return ResponseEntity.ok().body(ApiResponse.success("Success", token));
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadFile(@RequestPart("folderID") Integer folderID,
                                                  @RequestPart("file") MultipartFile file,
                                                  @RequestAttribute("user_id") Integer userID) {
        try {
            // 文件属性标注
            EFile fileProperties = new EFile();
            fileProperties.setFile_name(file.getOriginalFilename());
            fileProperties.setParent_id(folderID);
            fileProperties.setUser_id(userID);
            fileProperties.setFile_type("file");
            fileProperties.setFile_size((int) file.getSize());

            // 获取文件内容
            InputStream fileContent = file.getInputStream();

            // 将文件保存到服务器
            if (!fileService.addFile(fileProperties, fileContent)) {
                return ResponseEntity.badRequest().body(ApiResponse.error("文件上传失败", null));
            }

            return ResponseEntity.ok(ApiResponse.success("文件上传成功", null));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("文件上传失败", null));
        }
    }

    @PostMapping("/shareFile")
    public ResponseEntity<ApiResponse> shareFile(@RequestBody ShareFileDTO shareFileDTO,
                                                 @RequestAttribute("user_id") Integer userID) {
        if (shareService.createShare(shareFileDTO, userID)) {
            return ResponseEntity.ok(ApiResponse.success("Success", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Fail", null));
        }
    }

    @GetMapping("/getSharedFiles")
    public ResponseEntity<ApiResponse> getSharedFiles(@RequestAttribute("user_id") Integer userID) {
        List<Share> result = shareService.getSharedFiles(userID);
        return ResponseEntity.ok(ApiResponse.success("Success", result));
    }

    @GetMapping("/getMySharedFiles")
    public ResponseEntity<ApiResponse> getMySharedFiles(@RequestAttribute("user_id") Integer userID) {
        List<Share> result = shareService.getMySharedFiles(userID);
        return ResponseEntity.ok(ApiResponse.success("Success", result));
    }

    @GetMapping("/getShareDownload")
    public ResponseEntity<ApiResponse> getShareDownload(@RequestParam("shareID") Integer shareID,
                                                        @RequestAttribute("user_id") Integer userID) {
        String token = shareService.downloadShare(shareID, userID, 0);
        if (token == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }
        return ResponseEntity.ok().body(ApiResponse.success("Success", token));
    }

    @GetMapping("/getLongShareDownload")
    public ResponseEntity<ApiResponse> getLongShareDownload(@RequestParam("shareID") Integer shareID,
                                                            @RequestAttribute("user_id") Integer userID) {
        String token = shareService.downloadShare(shareID, userID, 1);
        if (token == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }
        return ResponseEntity.ok().body(ApiResponse.success("Success", token));
    }

    @GetMapping("shareSaveToDrive")
    public ResponseEntity<ApiResponse> shareSaveToDrive(@RequestParam("shareID") Integer shareID,
                                                        @RequestAttribute("user_id") Integer userID) {
        if (shareService.saveToDrive(shareID, userID)) {
            return ResponseEntity.ok(ApiResponse.success("Success", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Fail", null));
        }
    }

    @GetMapping("/deleteShare")
    public ResponseEntity<ApiResponse> deleteShare(@RequestParam("shareID") Integer shareID,
                                                   @RequestAttribute("user_id") Integer userID) {
        if (shareService.deleteShare(shareID)) {
            return ResponseEntity.ok(ApiResponse.success("Success", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Fail", null));
        }
    }

    @PostMapping("/remoteDownload")
    public ResponseEntity<ApiResponse> offlineDownload(@RequestBody DownloadTaskDTO downloadTaskDTO,
                                                       @RequestAttribute("user_id") Integer userID) {
        if (remoteDownloadService.createDownloadTask(downloadTaskDTO, userID)) {
            return ResponseEntity.ok(ApiResponse.success("Success", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Fail", null));
        }
    }

    @GetMapping("/pauseRemoteDownload")
    public ResponseEntity<ApiResponse> pauseRemoteDownload(@RequestParam("taskID") String taskID,
                                                           @RequestAttribute("user_id") Integer userID) {
        if (remoteDownloadService.pauseDownloadTask(taskID, userID)) {
            return ResponseEntity.ok(ApiResponse.success("Success", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Fail", null));
        }
    }

    @GetMapping("/resumeRemoteDownload")
    public ResponseEntity<ApiResponse> resumeRemoteDownload(@RequestParam("taskID") String taskID,
                                                            @RequestAttribute("user_id") Integer userID) {
        if (remoteDownloadService.resumeDownloadTask(taskID, userID)) {
            return ResponseEntity.ok(ApiResponse.success("Success", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Fail", null));
        }
    }

    @GetMapping("/getRemoteTaskList")
    public ResponseEntity<ApiResponse> getRemoteTaskList(@RequestAttribute("user_id") Integer userID) {
        List<DownloadTask> taskList = remoteDownloadService.getTaskList(userID);
        return ResponseEntity.ok().body(ApiResponse.success("Success", taskList));
    }

    @GetMapping("/getRemoteTask")
    public ResponseEntity<ApiResponse> getRemoteTask(@RequestParam("taskID") String taskID,
                                                     @RequestAttribute("user_id") Integer userID) {
        DownloadTask task = remoteDownloadService.getTaskByUUID(taskID);
        if (task == null || task.getUser_id() != userID) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Task not found", null));
        }
        return ResponseEntity.ok().body(ApiResponse.success("Success", task));
    }

    @GetMapping("/deleteRemoteDownload")
    public ResponseEntity<ApiResponse> deleteRemoteTask(@RequestParam("taskID") String taskID,
                                                        @RequestAttribute("user_id") Integer userID) {
        if (remoteDownloadService.deleteDownloadTask(taskID)) {
            return ResponseEntity.ok(ApiResponse.success("Success", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Fail", null));
        }
    }


}
