package com.tamir.followear.rest;

import com.tamir.followear.dto.*;
import com.tamir.followear.entities.Post;
import com.tamir.followear.services.ExploreService;
import com.tamir.followear.services.FeedService;
import com.tamir.followear.services.FollowService;
import com.tamir.followear.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("social/{id}")
public class SocialController {

    @Autowired
    PostService postService;

    @Autowired
    FollowService followService;

    @Autowired
    FeedService feedService;

    @Autowired
    ExploreService exploreService;

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

    @PostMapping("upload-link")
    @ResponseBody
    public long uploadLink(@PathVariable long id, @RequestParam String link) throws IOException {
        //TODO: make it work
        //Post post = postService.uploadLink(id, website, link);
        return 1;
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
    public FeedResultDTO getTimelineFeed(@PathVariable long id, @RequestParam int offset,
                                         @RequestBody Optional<FilteringDTO> filters){
        FeedResultDTO feedResult= feedService.getTimelineFeed(id, offset, filters);
        return feedResult;
    }

    @GetMapping("user-feed")
    @ResponseBody
    public FeedResultDTO getUserFeed(@PathVariable long id, @RequestParam int offset,
                                             @RequestBody Optional<FilteringDTO> filters){
        FeedResultDTO feedResult = feedService.getUserFeed(id, offset, filters);
        return feedResult;
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
    public PostInfoDTO getPostInfo(@RequestParam long postId) {
        return postService.getPostInfo(postId);
    }

    @GetMapping("more-from")
    @ResponseBody
    public List<BasicPostDTO> moreFrom(@RequestParam long masterUserId, @RequestParam long currPostId) {
        return postService.getMorePostsFromUser(masterUserId, currPostId);
    }

    @GetMapping("discover-people")
    @ResponseBody
    public List<DiscoverPeopleDTO> getDiscoverPeople(@PathVariable long id) {
        return exploreService.getDiscoverPeople(id);
    }

}