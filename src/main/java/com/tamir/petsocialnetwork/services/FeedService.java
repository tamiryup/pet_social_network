package com.tamir.petsocialnetwork.services;

import com.tamir.petsocialnetwork.dto.FeedFollowDTO;
import com.tamir.petsocialnetwork.dto.TimelineFeedPostDTO;
import com.tamir.petsocialnetwork.dto.UserFeedPostDTO;
import com.tamir.petsocialnetwork.entities.Post;
import com.tamir.petsocialnetwork.entities.User;
import com.tamir.petsocialnetwork.exceptions.InvalidUserException;
import com.tamir.petsocialnetwork.helpers.StreamHelper;
import com.tamir.petsocialnetwork.stream.PostActivity;
import com.tamir.petsocialnetwork.stream.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FeedService {

    @Autowired
    StreamService streamService;

    @Autowired
    PostService postService;

    @Autowired
    UserService userService;

    public List<TimelineFeedPostDTO> getTimelineFeed(long userId, int offset){
        if(!userService.existsById(userId))
            throw new InvalidUserException();

        List<PostActivity> streamFeed = streamService.getStreamTimelineFeed(userId, offset);
        List<Long> objects = StreamHelper.extractObjectsFromActivities(streamFeed);
        List<Long> actors = StreamHelper.extractActorsFromActivites(streamFeed);
        List<Post> posts = postService.findAllById(objects);
        List<User> users = userService.findAllById(actors);

        List<TimelineFeedPostDTO> feedPostDTOS = new ArrayList<>();
        if(posts.size() != users.size())
            return feedPostDTOS;

        for(int i=0; i<posts.size(); i++){
            User user = users.get(i);
            Post post = posts.get(i);
            TimelineFeedPostDTO dto = new TimelineFeedPostDTO(user.getProfileImageAddr(), user.getUsername(),
                    post.getImageAddr(), post.getDescription());
            feedPostDTOS.add(dto);
        }

        return feedPostDTOS;
    }

    public List<UserFeedPostDTO> getUserFeed(long userId, int offset){
        if(!userService.existsById(userId))
            throw new InvalidUserException();
        List<PostActivity> streamFeed = streamService.getStreamUserFeed(userId, offset);
        List<Long> objects = StreamHelper.extractObjectsFromActivities(streamFeed);
        Iterable<Post> posts = postService.findAllById(objects);
        List<UserFeedPostDTO> feedPostDTOS = new ArrayList<>();
        for(Post post: posts){
            feedPostDTOS.add(new UserFeedPostDTO(post.getImageAddr(), post.getDescription()));
        }
        return feedPostDTOS;
    }

    public List<FeedFollowDTO> getUserSlaves(long userId, int offset){
        if(!userService.existsById(userId))
            throw new InvalidUserException();
        List<Long> ids = streamService.getUserFollowers(userId, offset);
        return getFeedFollowDTOsFromIds(ids);
    }

    public List<FeedFollowDTO> getUserMasters(long userId, int offset){
        if(!userService.existsById(userId))
            throw new InvalidUserException();
        List<Long> ids = streamService.getUserFollowing(userId, offset);
        return getFeedFollowDTOsFromIds(ids);
    }

    private List<FeedFollowDTO> getFeedFollowDTOsFromIds(List<Long> ids){
        List<User> users = userService.findAllById(ids);
        List<FeedFollowDTO> dtos = new ArrayList<>();
        for(User user : users){
            dtos.add(new FeedFollowDTO(user.getProfileImageAddr(), user.getUsername()));
        }
        return dtos;
    }
}
