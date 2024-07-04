package com.jidays.ecloud.Entity;

import lombok.Data;

@Data
public class Share {
    int share_id;
    int file_id;
    String password;
    String expire_time;
    boolean disposable;
    int user_id;
    boolean deleted;

    String file_name;
}
