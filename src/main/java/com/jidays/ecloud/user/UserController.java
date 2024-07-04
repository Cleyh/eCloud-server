package com.jidays.ecloud.user;

import com.jidays.ecloud.DTO.LoginDTO;
import com.jidays.ecloud.DTO.RegisterDTO;
import com.jidays.ecloud.Entity.User;
import com.jidays.ecloud.user.userService.UserService;
import com.jidays.ecloud.util.ApiResponse;
import com.jidays.ecloud.util.TokenComp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterDTO registerDTO) {
        String result = userService.register(registerDTO);
        if (result.equals("注册成功")) {
            return ResponseEntity.ok(ApiResponse.success("注册成功", null));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(result, null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginDTO loginDTO) {
        User user = userService.login(loginDTO);
        if(user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("账号或密码错误", null));
        }
        return ResponseEntity.ok(ApiResponse.success("登录成功", user));
    }

    @GetMapping("/getProfile")
    public ResponseEntity<ApiResponse> getUser(@RequestParam String token) {
        long id = TokenComp.getIDFromToken(token);
        User user = userService.getUserByID(id);
        if(user == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("获取用户信息失败", null));
        }
        user.setToken(token);
        return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", user));
    }

    @GetMapping("/checkNameExist")
    public ResponseEntity<ApiResponse> checkNameExist(@RequestParam String name) {
        User user = userService.getUserByName(name);
        if(user == null) {
            return ResponseEntity.ok(ApiResponse.success("用户名不存在", null));
        }
        return ResponseEntity.ok(ApiResponse.error("用户名已存在", null));
    }

    @GetMapping("/checkEmailExist")
    public ResponseEntity<ApiResponse> checkEmailExist(@RequestParam String email) {
        User user = userService.getUserByEmail(email);
        if(user == null) {
            return ResponseEntity.ok(ApiResponse.success("邮箱不存在", null));
        }
        return ResponseEntity.ok(ApiResponse.error("邮箱已存在", null));
    }
}
