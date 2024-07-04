package com.jidays.ecloud.Entity;

import jdk.jfr.Description;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

@Data
@NoArgsConstructor
public class DownloadTask {
    String task_uuid;
    Integer file_id;

    String file_save_name;
    String url;
    /*
     * processing: 下载中
     * paused: 暂停
     * completed: 已完成
     * stopped: 被停止
     * failed: 下载失败
     * */
    String status;
    int file_parent_id;

    private long downloaded_bytes;
    private long total_bytes;

    String create_time;
    String update_time;

    int user_id;
    String user_name;
    String file_true_name;

    // not in mysql
    String expireTime;
    URL jurl;
    Path filePath;

    public DownloadTask(String file_save_name, String url, int file_parent_id, int user_id) {
        this.task_uuid = java.util.UUID.randomUUID().toString();
        this.file_save_name = file_save_name;
        this.url = url;

        this.file_parent_id = file_parent_id;
        this.user_id = user_id;
        this.status = "paused";

        this.create_time = java.time.LocalDateTime.now().toString();
        this.update_time = java.time.LocalDateTime.now().toString();

        this.total_bytes = 0;
        this.downloaded_bytes = 0;

        try {
            this.jurl = new URI(url).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
