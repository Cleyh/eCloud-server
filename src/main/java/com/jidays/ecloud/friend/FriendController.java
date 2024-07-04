package com.jidays.ecloud.friend;

import com.jidays.ecloud.Entity.User;
import com.jidays.ecloud.user.userMapper.UserMapper;
import com.jidays.ecloud.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/socialize")
public class FriendController {
    @Autowired
    FriendService friendService;

    @GetMapping("/getFriendList")
    public ResponseEntity<ApiResponse> getFriendList(@RequestAttribute("user_id") Integer userID) {
        List<User> friends = friendService.getFriendList(userID);
        return ResponseEntity.ok(ApiResponse.success("success", friends));
    }

    @GetMapping("/getAcceptList")
    public ResponseEntity<ApiResponse> getAcceptList(@RequestAttribute("user_id") Integer userID) {
        List<User> friends = friendService.getAcceptList(userID);
        return ResponseEntity.ok(ApiResponse.success("success", friends));
    }

    @GetMapping("/addFriend")
    public ResponseEntity<ApiResponse> addFriend(@RequestParam("email") String email,
                                                 @RequestAttribute("user_id") Integer userID) {
        if (friendService.addFriend(email, userID)) {
            return ResponseEntity.ok(ApiResponse.success("success", null));
        } else {
            return ResponseEntity.ok(ApiResponse.error("failed", null));
        }
    }

    @GetMapping("/acceptFriend")
    public ResponseEntity<ApiResponse> acceptFriend(@RequestParam("friendID") Integer friendID,
                                                    @RequestAttribute("user_id") Integer userID) {
        if (friendService.acceptFriend(friendID, userID)) {
            return ResponseEntity.ok(ApiResponse.success("success", null));
        } else {
            return ResponseEntity.ok(ApiResponse.error("failed", null));
        }
    }

    @GetMapping("/rejectFriend")
    public ResponseEntity<ApiResponse> rejectFriend(@RequestParam("friendID") Integer friendID,
                                                    @RequestAttribute("user_id") Integer userID) {
        if (friendService.rejectFriend(friendID, userID)) {
            return ResponseEntity.ok(ApiResponse.success("success", null));
        } else {
            return ResponseEntity.ok(ApiResponse.error("failed", null));
        }
    }
}
