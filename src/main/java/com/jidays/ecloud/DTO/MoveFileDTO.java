package com.jidays.ecloud.DTO;

import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotBlank;

@Data
public class MoveFileDTO {
    @NotBlank
    Integer fileID;
    @NotBlank
    Integer newParentID;

    public MoveFileDTO() {
    }

    public MoveFileDTO(Integer fileID, Integer newParentID) {
        this.fileID = fileID;
        this.newParentID = newParentID;
    }
}
