package com.tamir.followear.rest;

import com.tamir.followear.dto.UserInfoDTO;
import com.tamir.followear.entities.User;
import com.tamir.followear.exceptions.InvalidUserException;
import com.tamir.followear.services.FollowService;
import com.tamir.followear.services.PostService;
import com.tamir.followear.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user-info/{id}")
public class UserInfoController {

    @Autowired
    UserService userService;

    @Autowired
    PostService postService;

    @Autowired
    FollowService followService;

    @GetMapping("/details")
    @ResponseBody
    public UserInfoDTO getUserById(@PathVariable long id){
        User user = userService.findById(id);
        if(user==null)
            throw new InvalidUserException();
        UserInfoDTO ret = new UserInfoDTO(user.getId(), user.getUsername(), user.getFullName(),
                user.getProfileImageAddr(), user.getDescription(), user.getEmail(), user.getBirthDate());
        return ret;
    }

    @GetMapping("/num-posts")
    @ResponseBody
    public long getNumPosts(@PathVariable long id){
        long numPosts = postService.getNumPostsByUserId(id);
        return numPosts;
    }

    @GetMapping("/num-following")
    @ResponseBody
    public long getNumFollowing(@PathVariable long id){
        long numFollowing = followService.getNumFollowing(id);
        return numFollowing;
    }

    @GetMapping("num-followers")
    @ResponseBody
    public long getNumFollowers(@PathVariable long id){
        long numFollowers = followService.getNumFollowers(id);
        return numFollowers;
    }

}
