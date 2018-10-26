package com.tamir.petsocialnetwork.rest;

import com.tamir.petsocialnetwork.entities.User;
import com.tamir.petsocialnetwork.exceptions.InvalidUserException;
import com.tamir.petsocialnetwork.services.FollowService;
import com.tamir.petsocialnetwork.services.PostService;
import com.tamir.petsocialnetwork.services.UserService;
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
    public User getUserById(@PathVariable long id){
        User user = userService.findById(id);
        if(user==null)
            throw new InvalidUserException();
        user.nullPassword();
        return user;
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
