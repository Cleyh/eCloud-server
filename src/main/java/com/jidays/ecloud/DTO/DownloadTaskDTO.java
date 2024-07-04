package com.jidays.ecloud.DTO;

import lombok.Data;

@Data
public class DownloadTaskDTO {
    String fileName;
    String url;
    int parentID;
}
