package com.jidays.ecloud.Entity;

import lombok.Data;

@Data
public class User {
    int user_id;
    String password;
    String email;
    String user_name;
    String avatar;
    String role;
    String token;

    String status;
}
