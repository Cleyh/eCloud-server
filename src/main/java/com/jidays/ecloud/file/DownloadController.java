package com.jidays.ecloud.file;

import com.jidays.ecloud.Entity.EFile;
import com.jidays.ecloud.file.DownloadService.DownloadService;
import com.jidays.ecloud.file.DownloadService.RemoteDownloadService;
import com.jidays.ecloud.file.FileService.FileSystemService;
import com.jidays.ecloud.file.FileService.ShareService;
import com.jidays.ecloud.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.nio.charset.StandardCharsets;

@RestController
public class DownloadController {
    @Autowired
    FileSystemService fileSystemService;
    @Autowired
    DownloadService downloadService;
    @Autowired
    RemoteDownloadService remoteDownloadService;
    @Autowired
    ShareService shareService;

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> download(@RequestParam("token") String token) {
        EFile file = downloadService.useToken(token);
        InputStreamResource fileResource = downloadService.getFileResource(file);
        if (file == null || fileResource == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFile_name() + "\"");
//        return ResponseEntity.ok()
//                .headers(headers)
//                .contentType(MediaType.parseMediaType("application/octet-stream"))
//                .body(fileResource);
        // 使用ContentDisposition构建头部并指定UTF-8编码
        ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                .filename(file.getFile_name(), StandardCharsets.UTF_8)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileResource);
    }

    @GetMapping("/shareDownload")
    public ResponseEntity<ApiResponse> shareDownload(@RequestParam("shareID") Integer shareID,
                                                     @RequestHeader("authorization") Integer userID) {
        String downloadToken = shareService.downloadShare(shareID, userID, 0);
        if (downloadToken != null) {
            return ResponseEntity.ok(ApiResponse.success("Success", downloadToken));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Failed", null));
    }

    @GetMapping("/remoteTaskProgress")
    public SseEmitter progress(@RequestParam("userID") Integer userID) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        remoteDownloadService.addEmitter(emitter, userID);
        remoteDownloadService.notifyProgress();
        return emitter;
    }

    @GetMapping("/admin/remoteTaskProgress")
    public SseEmitter progressAdmin() {
        Integer userID = 0;
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        remoteDownloadService.addEmitter(emitter, userID);
        remoteDownloadService.notifyProgress();
        return emitter;
    }

}
