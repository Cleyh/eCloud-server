package com.jidays.ecloud.friend;

import com.jidays.ecloud.Entity.Friend;
import com.jidays.ecloud.Entity.User;
import com.jidays.ecloud.user.userMapper.UserMapper;
import com.jidays.ecloud.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendService {
    @Autowired
    FriendMapper friendMapper;
    @Autowired
    UserMapper userMapper;

    public List<User> getFriendList(Integer userID) {
        return friendMapper.getFriendList(userID);
    }

    public boolean addFriend(String email, Integer userID) {
        User friend = userMapper.getUserByEmail(email);
        if (friend == null) {
            return false;
        }
        List<Friend> friends = friendMapper.getFriendByID(friend.getUser_id(), userID);
        if (!friends.isEmpty()) {
            return false;
        }
        friendMapper.addFriend(friend.getUser_id(), userID);
        return true;
    }

    public boolean acceptFriend(Integer requestSenderID, Integer userID) {
        friendMapper.addAcceptedFriend(requestSenderID, userID);
        friendMapper.acceptFriend(requestSenderID, userID);
        return true;
    }

    public boolean rejectFriend(Integer friendID, Integer userID) {
        friendMapper.deletedFriend(friendID, userID);
        return true;
    }

    public List<User> getAcceptList(Integer userID) {
        return friendMapper.getAcceptList(userID);
    }
}
