package com.jidays.ecloud.DTO;

import com.jidays.ecloud.util.RegexConstant;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class RegisterDTO {
    @NotBlank(message = "Username is required")
    //@Size(min = 3, max = 20,
    //        message = "Username must be between 3 and 20 characters")
    private String user_name;

    @NotBlank(message = "Password is required")
    //@Size(min = 8, message = "Password must be at least 8 characters")
    //@Pattern(regexp = RegexConstant.PASSWORD_REGEX,
    //        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
    private String password;

    @NotBlank(message = "Email is required")
    @Pattern(regexp = RegexConstant.EMAIL_REGEX,
            message = "Email is not valid")
    private String email;
}
