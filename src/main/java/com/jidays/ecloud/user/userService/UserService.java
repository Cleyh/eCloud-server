package com.jidays.ecloud.user.userService;

import com.jidays.ecloud.DTO.LoginDTO;
import com.jidays.ecloud.DTO.RegisterDTO;
import com.jidays.ecloud.Entity.User;
import com.jidays.ecloud.file.FileService.FileService;
import com.jidays.ecloud.user.userMapper.UserMapper;
import com.jidays.ecloud.util.TokenComp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    FileService fileService;

    public String register(RegisterDTO registerDTO) {
        if (userMapper.getUserByEmail(registerDTO.getEmail()) != null) {
            return "邮箱已被注册";
        }
        if (userMapper.getUserByName(registerDTO.getUser_name()) != null) {
            return "用户名已被占用";
        }

        userMapper.addUser(registerDTO.getUser_name(), registerDTO.getPassword(), registerDTO.getEmail(), "user");
        fileService.initUserFolder(userMapper.getUserByName(registerDTO.getUser_name()).getUser_id());
        return "注册成功";
    }

    public User login(LoginDTO loginDTO) {
        User user;
        if (loginDTO.user_name != null) {
            user = userMapper.getUserByName(loginDTO.getUser_name());
        } else {
            user = userMapper.getUserByEmail(loginDTO.getEmail());
        }
        if (user == null) {
            return null;
        }
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            return null;
        }
        user.setToken(TokenComp.generateToken(user.getUser_id(), user.getEmail(), user.getRole()));
        return user;
    }

    public User getUserByID(long id) {
        return userMapper.getUserByID(id);
    }

    public User getUserByName(String name) {
        return userMapper.getUserByName(name);
    }

    public User getUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }
}
