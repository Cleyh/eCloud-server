package com.jidays.ecloud.friend;

import com.jidays.ecloud.Entity.Friend;
import com.jidays.ecloud.Entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendMapper {
    List<User> getFriendList(@Param("userID") Integer userID);

    void addFriend(@Param("friendID") Integer friendID, @Param("userID") Integer userID);

    List<Friend> getFriendByID(@Param("friendID") Integer friendID,@Param("userID")Integer userID);

    void acceptFriend(@Param("requestSenderID") Integer requestSenderID, @Param("userID") Integer userID);

    void addAcceptedFriend(@Param("friendID") Integer friendID, @Param("userID") Integer userID);

    void deletedFriend(@Param("friendID") Integer friendID, @Param("userID") Integer userID);

    List<User> getAcceptList(@Param("userID") Integer userID);
}
