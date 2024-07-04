package com.jidays.ecloud.user.userMapper;

import com.jidays.ecloud.Entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM users WHERE email = #{email}")
    User getUserByEmail(String email);

    @Select("SELECT * FROM users WHERE user_name = #{username}")
    User getUserByName(String username);

    @Insert("INSERT INTO users (user_name, password, email, role) VALUES (#{username}, #{password}, #{email}, #{role})")
    void addUser(String username, String password, String email, String role);

    @Select("SELECT * FROM users WHERE users.user_id = #{id}")
    User getUserByID(long id);
}
