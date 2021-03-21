package com.tamir.followear.rest;

import com.tamir.followear.dto.*;
import com.tamir.followear.entities.Post;
import com.tamir.followear.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("social/{id}")
public class SocialController {

    private final Logger logger = LoggerFactory.getLogger(SocialController.class);

    @Autowired
    PostService postService;

    @Autowired
    FollowService followService;

    @Autowired
    FeedService feedService;

    @Autowired
    LikeService likeService;

    @Autowired
    SaveService saveService;

    @Autowired
    ExploreService exploreService;

    @Autowired
    ScrapingService scrapingService;

    @PostMapping("upload-item")
    @ResponseBody
    public long uploadItem(@PathVariable long id, @RequestBody UploadItemDTO item) throws IOException {
        logger.info("starting uploadItem input userId: {}, item: {}", id, item);
        Post post = postService.uploadItemPost(id, item);
        return post.getId();
    }

    @PostMapping("upload-link")
    @ResponseBody
    public long uploadLink(@PathVariable long id, @RequestParam String link) throws IOException {
        logger.info("starting uploadLink input userId: {}, link: {}", id, link);
        Post post = postService.uploadLink(id, link);
        return post.getId();
    }

    @PostMapping("follow")
    @ResponseBody
    public void followUser(@PathVariable("id") long slaveId, @RequestParam long masterId) {
        logger.info("starting followUser input slaveId: {}, masterId: {}", slaveId, masterId);
        followService.follow(masterId, slaveId);
    }

    @PostMapping("unfollow")
    @ResponseBody
    public void unfollowUser(@PathVariable("id") long slaveId, @RequestParam long masterId) {
        logger.info("starting unfollowUser input slaveId: {}, masterId: {}", slaveId, masterId);
        followService.unfollow(masterId, slaveId);
    }

    @PostMapping("timeline-feed")
    @ResponseBody
    public FeedResultDTO getTimelineFeed(@PathVariable long id, @RequestParam int offset,
                                         @RequestBody Optional<FilteringDTO> filters) {
        logger.info("starting getTimelineFeed input userId: {}, offset: {}, filters: {}",
                id, offset, filters);
        FeedResultDTO feedResult = feedService.getTimelineFeed(id, offset, filters);
        return feedResult;
    }

    @PostMapping("user-feed")
    @ResponseBody
    public FeedResultDTO getUserFeed(@PathVariable long id, @RequestParam int offset,
                                     @RequestBody Optional<FilteringDTO> filters) {
        logger.info("starting getUserFeed input userId: {}, offset: {}, filters: {}",
                id, offset, filters);
        FeedResultDTO feedResult = feedService.getUserFeed(id, offset, filters);
        return feedResult;
    }

    @PostMapping("saved-feed")
    @ResponseBody
    public FeedResultDTO getSavedFeed(@PathVariable long id, @RequestParam int offset,
                                      @RequestBody Optional<FilteringDTO> filters) {
        logger.info("starting getSavedFeed input userId: {}, offset: {}, filters: {}",
                id, offset, filters);
        FeedResultDTO feedResult = feedService.getSavedFeed(id, offset, filters);
        return feedResult;
    }

    @PostMapping("explore-feed")
    @ResponseBody
    public FeedResultDTO getExploreFeed(@PathVariable long id, @RequestBody Optional<FilteringDTO> filters) {
        logger.info("starting getExploreFeed input userId: {}, filters: {}", id, filters);
        FeedResultDTO feedResult = feedService.getExploreFeed(id, filters);
        return feedResult;
    }

    @GetMapping("follow-slaves")
    @ResponseBody
    public List<FeedFollowDTO> getUserSlaves(@PathVariable long id, @RequestParam int offset) {
        logger.info("starting getUserSlaves input userId: {}, offset: {}", id, offset);
        return feedService.getUserSlaves(id, offset);
    }

    @GetMapping("follow-masters")
    @ResponseBody
    public List<FeedFollowDTO> getUserMasters(@PathVariable long id, @RequestParam int offset) {
        logger.info("starting getUserMasters input userId: {}, offset: {}", id, offset);
        return feedService.getUserMasters(id, offset);
    }

    @GetMapping("is-following")
    @ResponseBody
    public boolean isFollowing(@PathVariable long id, @RequestParam long masterId) {
        logger.info("starting isFollowing input userId: {}, masterId: {}", id, masterId);
        return followService.isFollowing(masterId, id);
    }

    @GetMapping("post-info")
    @ResponseBody
    public PostInfoDTO getPostInfo(@RequestParam long postId) {
        logger.info("starting getPostInfo input postId: {}", postId);
        return postService.getPostInfo(postId);
    }

    @PostMapping("inc-post-views")
    @ResponseBody
    public void incPostViews(@PathVariable long id, @RequestParam long postId) {
        logger.info("starting incPostViews input userId: {}, postId: {}", id, postId);
        postService.incPostViews(id, postId);
    }

    @PostMapping("inc-post-redirects")
    @ResponseBody
    public void incPostRedirects(@PathVariable long id, @RequestParam long postId) {
        logger.info("starting incPostRedirects input userId: {}, postId: {}", id, postId);
        postService.incPostRedirects(postId);
    }

    @GetMapping("more-from")
    @ResponseBody
    public List<BasicPostDTO> moreFrom(@RequestParam long masterUserId, @RequestParam long currPostId) {
        logger.info("starting moreFrom input masterUserId: {}, currPostId: {}", masterUserId, currPostId);
        return postService.moreFromUser(masterUserId, currPostId, 3);
    }

    @GetMapping("discover-people")
    @ResponseBody
    public List<DiscoverPeopleDTO> getDiscoverPeople(@PathVariable long id) {
        logger.info("starting getDiscoverPeople input userId: {}", id);
        return feedService.getDiscoverPeople(id);
    }

    @DeleteMapping("remove-post")
    @ResponseBody
    public void removePost(@PathVariable long id, @RequestParam long postId) {
        logger.info("starting removePost input userId: {}, postId: {}", id, postId);
        postService.removePost(id, postId);
    }

    @DeleteMapping("hide-post")
    @ResponseBody
    public void hidePost(@PathVariable long id, @RequestParam long postId) {
        logger.info("starting hidePost input userId: {}, postId: {}", id, postId);
        postService.hidePost(id, postId);
    }

    @GetMapping("did-like")
    @ResponseBody
    public boolean didLike(@PathVariable long id, @RequestParam long postId) {
        logger.info("starting didLike input userId: {}, postId: {}", id, postId);
        return likeService.didLike(id, postId);
    }

    @PostMapping("like")
    @ResponseBody
    public void like(@PathVariable long id, @RequestParam long postId) {
        logger.info("starting like input userId: {}, postId: {}", id, postId);
        likeService.like(id, postId);
    }

    @PostMapping("unlike")
    @ResponseBody
    public void unlike(@PathVariable long id, @RequestParam long postId) {
        logger.info("starting unlike input userId: {}, postId: {}", id, postId);
        likeService.unlike(id, postId);
    }

    @PostMapping("save-item")
    @ResponseBody
    public void saveItem(@PathVariable long id, @RequestParam long postId) {
        logger.info("starting saveItem input userId: {}, postId: {}", id, postId);
        saveService.saveItem(id, postId);
    }

    @PostMapping("unsave-item")
    @ResponseBody
    public void unsaveItem(@PathVariable long id, @RequestParam long postId) {
        logger.info("starting unsaveItem input userId: {}, postId: {}", id, postId);
        saveService.unsaveItem(id, postId);
    }

    @GetMapping("did-save-item")
    @ResponseBody
    public boolean didSave(@PathVariable long id, @RequestParam long postId) {
        logger.info("starting didSave input userId: {}, postId: {}", id, postId);
        return saveService.didSaveItem(id, postId);
    }

    @PostMapping("upload-self-thumb")
    @ResponseBody
    public String uploadSelfThumb(@PathVariable long id, @RequestParam long postId,
                                  @RequestParam("image") MultipartFile image) throws IOException {
        logger.info("starting uploadSelfThumb input userId: {}, postId: {}", id, postId);
        return postService.uploadSelfThumb(id, postId, image);
    }

    @PostMapping("remove-self-thumb")
    @ResponseBody
    public void removeSelfThumb(@PathVariable long id, @RequestParam long postId) {
        logger.info("starting removeSelfThumb input userId: {}, postId: {}", id, postId);
        postService.removeSelfThumb(id, postId);
    }


}
