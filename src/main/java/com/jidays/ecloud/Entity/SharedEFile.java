package com.jidays.ecloud.Entity;

import lombok.Data;

@Data
public class SharedEFile {
    int share_id;

    // 文件id
    int file_id;
    // 文件展示名称
    String file_name;
    // 文件实际名称
    String file_true_name;
    // 父级id
    int parent_id;
    // 是否被放入回收站了
    Boolean is_delete;
    // 用户id
    int user_id;

    int file_size;
    String file_type;

    String upload_time;
    String latest_time;
}
