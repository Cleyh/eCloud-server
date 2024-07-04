package com.jidays.ecloud.DTO;

import lombok.Data;

import java.util.List;

@Data
public class ShareFileDTO {
    int fileID;
    boolean needPassword;
    String password;

    boolean limited;
    List<Integer> userIDs;

    boolean expandable;
    String expireTime;

    boolean disposable;
}
