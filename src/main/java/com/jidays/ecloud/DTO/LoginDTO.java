package com.jidays.ecloud.DTO;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginDTO {
    @NotBlank
    public String email;
    @NotBlank
    public String user_name;
    @NotBlank
    public String password;
}
