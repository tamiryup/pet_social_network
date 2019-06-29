package com.tamir.petsocialnetwork.rest;

import com.tamir.petsocialnetwork.dto.FeedFollowDTO;
import com.tamir.petsocialnetwork.dto.TimelineFeedPostDTO;
import com.tamir.petsocialnetwork.dto.UploadItemDTO;
import com.tamir.petsocialnetwork.dto.UserFeedPostDTO;
import com.tamir.petsocialnetwork.entities.Post;
import com.tamir.petsocialnetwork.services.FeedService;
import com.tamir.petsocialnetwork.services.FollowService;
import com.tamir.petsocialnetwork.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("social/{id}")
public class SocialController {

    @Autowired
    PostService postService;

    @Autowired
    FollowService followService;

    @Autowired
    FeedService feedService;

    @PostMapping("upload")
    @ResponseBody
    public long uploadPost(@PathVariable long id, @RequestParam MultipartFile image,
                           @RequestParam String description) throws IOException {
        Post post = postService.uploadPost(id, image, description);
        return post.getId();
    }

    @PostMapping("upload-item")
    @ResponseBody
    public long uploadItem(@PathVariable long id, @RequestBody UploadItemDTO item) throws IOException {
        Post post = postService.uploadItemPost(id, item);
        return post.getId();
    }

    @PostMapping("follow")
    @ResponseBody
    public void followUser(@PathVariable("id") long slaveId, @RequestParam long masterId) {
        followService.follow(masterId, slaveId);
    }

    @PostMapping("unfollow")
    @ResponseBody
    public void unfollowUser(@PathVariable("id") long slaveId, @RequestParam long masterId) {
        followService.unfollow(masterId, slaveId);
    }

    @GetMapping("timeline-feed")
    @ResponseBody
    public List<TimelineFeedPostDTO> getTimelineFeed(@PathVariable long id, @RequestParam int offset){
        List<TimelineFeedPostDTO> feedPostDTOS = feedService.getTimelineFeed(id, offset);
        return feedPostDTOS;
    }

    @GetMapping("user-feed")
    @ResponseBody
    public List<UserFeedPostDTO> getUserFeed(@PathVariable long id, @RequestParam int offset){
        List<UserFeedPostDTO> feedPostDTOS = feedService.getUserFeed(id, offset);
        return feedPostDTOS;
    }

    @GetMapping("follow-slaves")
    @ResponseBody
    public List<FeedFollowDTO> getUserSlaves(@PathVariable long id, @RequestParam int offset){
        return feedService.getUserSlaves(id, offset);
    }

    @GetMapping("follow-masters")
    @ResponseBody
    public List<FeedFollowDTO> getUserMasters(@PathVariable long id, @RequestParam int offset){
        return feedService.getUserMasters(id, offset);
    }

    @GetMapping("is-following")
    @ResponseBody
    public boolean isFollowing(@PathVariable long id, @RequestParam long masterId){
        return followService.isFollowing(masterId, id);
    }

    @GetMapping("post-info")
    @ResponseBody
    public Post getPostInfo(@RequestParam long postId) {
        return postService.findById(postId);
    }

}
