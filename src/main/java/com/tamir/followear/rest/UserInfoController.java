package com.tamir.followear.rest;

import com.tamir.followear.dto.UserInfoDTO;
import com.tamir.followear.dto.UserProfileInfoDTO;
import com.tamir.followear.entities.User;
import com.tamir.followear.exceptions.InvalidUserException;
import com.tamir.followear.services.FollowService;
import com.tamir.followear.services.PostService;
import com.tamir.followear.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user-info/{id}")
public class UserInfoController {

    private final Logger logger = LoggerFactory.getLogger(UserInfoController.class);

    @Autowired
    UserService userService;

    @Autowired
    PostService postService;

    @Autowired
    FollowService followService;

    @GetMapping("profile-info")
    @ResponseBody
    public UserProfileInfoDTO profileInfo(@PathVariable long id) {
        logger.info("starting profileInfo input userId: {}", id);

        User user = userService.findById(id);
        if(user==null)
            throw new InvalidUserException();
        UserProfileInfoDTO ret = new UserProfileInfoDTO(user.getId(), user.getUsername(), user.getFullName(),
                user.getProfileImageAddr(), user.getDescription(), user.getInstagramLink());
        return ret;
    }

    @GetMapping("/num-posts")
    @ResponseBody
    public long getNumPosts(@PathVariable long id){
        logger.info("starting getNumPosts input userId: {}", id);
        long numPosts = postService.getNumPostsByUserId(id);
        return numPosts;
    }

    @GetMapping("/num-following")
    @ResponseBody
    public long getNumFollowing(@PathVariable long id){
        logger.info("starting getNumFollowing input userId: {}", id);
        long numFollowing = followService.getNumFollowing(id);
        return numFollowing;
    }

    @GetMapping("num-followers")
    @ResponseBody
    public long getNumFollowers(@PathVariable long id){
        logger.info("starting getNumFollowers input userId: {}", id);
        long numFollowers = followService.getNumFollowers(id);
        return numFollowers;
    }

}
