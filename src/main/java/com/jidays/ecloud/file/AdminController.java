package com.jidays.ecloud.file;

import com.jidays.ecloud.DTO.DownloadTaskDTO;
import com.jidays.ecloud.Entity.DownloadTask;
import com.jidays.ecloud.Entity.EFile;
import com.jidays.ecloud.Entity.User;
import com.jidays.ecloud.file.DownloadService.RemoteDownloadService;
import com.jidays.ecloud.file.FileService.AdminFileService;
import com.jidays.ecloud.file.FileService.FileService;
import com.jidays.ecloud.file.FileService.ShareService;
import com.jidays.ecloud.file.fileMapper.EFileMapper;
import com.jidays.ecloud.user.userService.UserService;
import com.jidays.ecloud.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@RestController
@RequestMapping("/admin/userFile")
public class AdminController {
    @Autowired
    FileService fileService;
    @Autowired
    RemoteDownloadService remoteDownloadService;
    @Autowired
    ShareService shareService;
    @Autowired
    AdminFileService adminFileService;
    @Autowired
    EFileMapper eFileMapper;
    @Autowired
    UserService userService;

    @GetMapping("/getUserFolder")
    public ResponseEntity<ApiResponse> getRoot(@RequestAttribute("user_id") Integer userID) {
        EFile file = fileService.getUserFolder(userID);
        return ResponseEntity.ok(ApiResponse.success("Success", file));
    }

    @GetMapping("/getFilesInFolder")
    public ResponseEntity<ApiResponse> getDirectory(@RequestParam("folderID") Integer folderID,
                                                    @RequestAttribute("user_id") Integer userID) {
        if (userID != 0) return ResponseEntity.badRequest().body(ApiResponse.error("Unauthorized", null));
        if (!fileService.checkIdExist(folderID)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }
        List<EFile> files = adminFileService.getFilesInFolder(folderID, userID);
        return ResponseEntity.ok(ApiResponse.success("Success", files));
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse> deleteFileHard(@RequestParam("fileID") Integer fileID,
                                                      @RequestAttribute("user_id") Integer userID) {
        EFile file = eFileMapper.getFileById(fileID);
        if (file == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }
        if (fileService.deleteById(fileID, file.getUser_id())) {
            return ResponseEntity.ok().body(ApiResponse.success("Success", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }
    }


    @PostMapping("/remoteDownload")
    public ResponseEntity<ApiResponse> offlineDownload(@RequestBody DownloadTaskDTO downloadTaskDTO,
                                                       @RequestAttribute("user_id") Integer userID) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Fail", null));
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
        if (userID != 0) return ResponseEntity.badRequest().body(ApiResponse.error("Unauthorized", null));
        List<DownloadTask> taskList = remoteDownloadService.getAllTaskList();
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

    @GetMapping("/getFileChain")
    public ResponseEntity<ApiResponse> getFileChain(@RequestParam("fileID") Integer fileID,
                                                    @RequestAttribute("user_id") Integer userID) {
        if (!fileService.checkIdExist(fileID)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("File not found", null));
        }
        List<EFile> file = eFileMapper.getFileChainById(fileID);
        return ResponseEntity.ok(ApiResponse.success("Success", file));
    }

    @GetMapping("/getUser")
    public ResponseEntity<ApiResponse> getUser(@RequestParam("userID") Integer userID) {
        User user = userService.getUserByID(userID);
        return ResponseEntity.ok(ApiResponse.success("Success", user));
    }

}
