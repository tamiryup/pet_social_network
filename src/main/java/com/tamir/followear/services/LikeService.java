package com.tamir.followear.services;

import com.tamir.followear.entities.Like;
import com.tamir.followear.exceptions.InvalidPostException;
import com.tamir.followear.exceptions.InvalidUserException;
import com.tamir.followear.exceptions.LikeException;
import com.tamir.followear.jpaKeys.LikeKey;
import com.tamir.followear.repositories.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class LikeService {

    @Autowired
    LikeRepository likeRepo;

    @Autowired
    UserService userService;

    @Autowired
    PostService postService;

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
}
