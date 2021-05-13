package com.tamir.followear.services;

import com.tamir.followear.dto.FeedFollowDTO;
import com.tamir.followear.entities.Like;
import com.tamir.followear.entities.User;
import com.tamir.followear.exceptions.InvalidPostException;
import com.tamir.followear.exceptions.InvalidUserException;
import com.tamir.followear.exceptions.LikeException;
import com.tamir.followear.jpaKeys.LikeKey;
import com.tamir.followear.repositories.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LikeService {

    @Autowired
    private LikeRepository likeRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private NotificationService notificationService;

    public boolean didLike(long userId, long postId) {
        return likeRepo.existsById(new LikeKey(userId, postId));
    }

    public Like like(long userId, long postId) {
        if(!userService.existsById(userId))
            throw new InvalidUserException();
        if(!postService.existsById(postId))
            throw new InvalidPostException();
        if(didLike(userId, postId))
            throw new LikeException("User can't like post twice");

        Like like = likeRepo.save(new Like(userId, postId));
        postService.incNumLikes(postId);
        notificationService.sendLikeNotification(userId, postId);
        return like;
    }

    public void unlike(long userId, long postId) {
        if(!userService.existsById(userId))
            throw new InvalidUserException();
        if(!postService.existsById(postId))
            throw new InvalidPostException();
        if(!didLike(userId, postId))
            throw new LikeException("Can't unlike post that you don't already like");

        likeRepo.deleteById(new LikeKey(userId, postId));
        postService.decNumLikes(postId);
    }

    public List<FeedFollowDTO> likeList(long postId, int limit) {
        if(!postService.existsById(postId))
            throw new InvalidPostException();

        List<Like> likes = likeRepo.findByPostIdWithLimit(postId, limit);
        List<Long> userIds = likes.stream().map(Like::getUserId).collect(Collectors.toList());
        List<User> users = userService.findAllById(userIds);


        List<FeedFollowDTO> resultList = new ArrayList<>();
        for(User user : users) {
            FeedFollowDTO dto = new FeedFollowDTO(user.getId(), user.getUsername(),
                    user.getFullName(), user.getProfileImageAddr());
            resultList.add(dto);
        }

        return resultList;
    }

    /**
     * delete all records by userId
     *
     * @param userId - The id of the liking user
     */
    public void deleteAllByUserId(long userId) {
        likeRepo.deleteByUserId(userId);
    }
}
