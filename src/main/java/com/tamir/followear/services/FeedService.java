package com.tamir.followear.services;

import com.tamir.followear.CommonBeanConfig;
import com.tamir.followear.dto.*;
import com.tamir.followear.entities.Post;
import com.tamir.followear.entities.Store;
import com.tamir.followear.entities.User;
import com.tamir.followear.exceptions.InvalidUserException;
import com.tamir.followear.exceptions.NoMoreActivitiesException;
import com.tamir.followear.helpers.StreamHelper;
import com.tamir.followear.stream.PostActivity;
import com.tamir.followear.stream.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeedService {

    private final Logger logger = LoggerFactory.getLogger(FeedService.class);

    @Autowired
    StreamService streamService;

    @Autowired
    PostService postService;

    @Autowired
    UserService userService;

    @Autowired
    StoreService storeService;

    public FeedResultDTO getTimelineFeed(long userId, int offset, Optional<FilteringDTO> filters) {
        if (!userService.existsById(userId)) {
            throw new InvalidUserException();
        }

        List<PostActivity> streamFeed;
        int numFeedRequests = 0;
        List<UserFeedPostDTO> feedPostDTOS = new ArrayList<>();
        int streamFeedRequestLimit = filters.isPresent() ?
                CommonBeanConfig.getMaxStreamActivitiesPerFeedRequest() : CommonBeanConfig.getNumPostsPerFeedRequest();

        while (numFeedRequests < CommonBeanConfig.getStreamFeedRequestsLimit()
                && feedPostDTOS.size() < CommonBeanConfig.getNumPostsPerFeedRequest()) {

            try {
                streamFeed = streamService.getStreamTimelineFeed(userId, offset, streamFeedRequestLimit);
            } catch (NoMoreActivitiesException e) { // catch no more activities when filtering
                if (feedPostDTOS.size() == 0) {
                    throw e;
                }
                break; // return the feed result since there are no more activities
            }

            List<Long> objects = StreamHelper.extractObjectsFromActivities(streamFeed);
            List<Post> posts = postService.findAllById(objects);
            posts = filterPosts(posts, filters);

            List<Long> actors = posts.stream().map(Post::getUserId).collect(Collectors.toList());
            Map<Long, User> userMap = userService.makeMapFromIds(actors);

            List<Long> storesIds = posts.stream().map(Post::getStoreId).collect(Collectors.toList());
            Map<Long, Store> storeMap = storeService.makeMapFromIds(storesIds);

            for(Post post : posts) {
                User user = userMap.get(post.getUserId());
                Store store = storeMap.get(post.getStoreId());

                if(user == null) {
                    logger.error("Post's userId does not exist in database");
                    continue;
                }

                String price = post.getCurrency().getSign() + post.getPrice();
                feedPostDTOS.add(new TimelineFeedPostDTO(post.getId(), post.getUserId(), post.getImageAddr(),
                        post.getDescription(), post.getLink(), price, store.getWebsite(),
                        user.getProfileImageAddr(), user.getUsername()));
            }

            offset += streamFeed.size();
            numFeedRequests++;

            if(!filters.isPresent()) {
                break;
            }

        }

        return new FeedResultDTO(feedPostDTOS, offset);
    }

    public FeedResultDTO getUserFeed(long userId, int offset, Optional<FilteringDTO> filters) {
        if (!userService.existsById(userId)) {
            throw new InvalidUserException();
        }

        List<PostActivity> streamFeed;
        int numFeedRequests = 0;
        List<UserFeedPostDTO> feedPostDTOS = new ArrayList<>();
        int streamFeedRequestLimit = filters.isPresent() ?
                CommonBeanConfig.getMaxStreamActivitiesPerFeedRequest() : CommonBeanConfig.getNumPostsPerFeedRequest();

        while (numFeedRequests < CommonBeanConfig.getStreamFeedRequestsLimit()
                && feedPostDTOS.size() < CommonBeanConfig.getNumPostsPerFeedRequest()) {

            try {
                streamFeed = streamService.getStreamUserFeed(userId, offset, streamFeedRequestLimit);
            } catch (NoMoreActivitiesException e) { // catch no more activities when filtering
                if(feedPostDTOS.size() == 0){
                    throw e;
                }
                break; // return the feed result since there are no more activities
            }
            List<Long> objects = StreamHelper.extractObjectsFromActivities(streamFeed);
            List<Post> posts = postService.findAllById(objects);
            posts = filterPosts(posts, filters);

            List<Long> storesIds = posts.stream().map(Post::getStoreId).collect(Collectors.toList());
            Map<Long, Store> storeMap = storeService.makeMapFromIds(storesIds);

            for(Post post : posts) {
                Store store = storeMap.get(post.getStoreId());
                String price = post.getCurrency().getSign() + post.getPrice();
                feedPostDTOS.add(new UserFeedPostDTO(post.getId(), post.getUserId(), post.getImageAddr(),
                        post.getDescription(), post.getLink(), price, store.getWebsite()));
            }

            offset += streamFeed.size(); //increment the offset for the next request
            numFeedRequests++;

            if(!filters.isPresent()) {
                break;
            }
        }

        return new FeedResultDTO(feedPostDTOS, offset);
    }

    private List<Post> filterPosts(List<Post> posts, Optional<FilteringDTO> filters) {
        if(!filters.isPresent()) {
            return posts;
        }

        List<Post> filteredPosts = posts.stream()
                .filter(post -> filterPost(post, filters.get()))
                .collect(Collectors.toList());

        return filteredPosts;
    }

    /**
     * checks if the post passes the filters
     *
     * @param post The post to filter
     * @param filters The filtering rules
     * @return true - iff the post fits the filters
     */
    private boolean filterPost(Post post, FilteringDTO filters) {

        //check category
        if(filters.getCategory() != null && post.getCategory() != filters.getCategory()) {
            return false;
        }

        //check product type
        if(!CollectionUtils.isEmpty(filters.getProductTypes())
                && !filters.getProductTypes().contains(post.getProductType())) {
            return false;
        }

        //check designer
        if(!CollectionUtils.isEmpty(filters.getDesigners())
                && !filters.getDesigners().contains(post.getDesigner())) {
            return false;
        }

        //check stores
        if(!CollectionUtils.isEmpty(filters.getStores())
                && !filters.getStores().contains(post.getStoreId())) {
            return false;
        }

        //TODO: change price into ILS (right now prices come in all currencies)

        double price = Double.valueOf(post.getPrice());

        //check min price
        if(filters.getMinPrice() != 0 &&  price < filters.getMinPrice()) {
            return false;
        }

        //check max price
        if(filters.getMaxPrice() != 0 && price > filters.getMaxPrice()){
            return false;
        }

        return true;
    }

    public List<FeedFollowDTO> getUserSlaves(long userId, int offset) {
        if (!userService.existsById(userId))
            throw new InvalidUserException();
        List<Long> ids = streamService.getUserFollowers(userId, offset);
        return getFeedFollowDTOsFromIds(ids);
    }

    public List<FeedFollowDTO> getUserMasters(long userId, int offset) {
        if (!userService.existsById(userId))
            throw new InvalidUserException();
        List<Long> ids = streamService.getUserFollowing(userId, offset);
        return getFeedFollowDTOsFromIds(ids);
    }

    private List<FeedFollowDTO> getFeedFollowDTOsFromIds(List<Long> ids) {
        List<User> users = userService.findAllById(ids);
        List<FeedFollowDTO> dtos = new ArrayList<>();
        for (User user : users) {

            FeedFollowDTO dto = new FeedFollowDTO();
            dto.setProfileImageAddr(user.getProfileImageAddr());
            dto.setUsername(user.getUsername());
            dto.setId(user.getId());
            dtos.add(dto);
        }
        return dtos;
    }
}
