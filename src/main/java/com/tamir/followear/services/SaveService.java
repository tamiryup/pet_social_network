package com.tamir.followear.services;

import com.tamir.followear.entities.Post;
import com.tamir.followear.entities.Save;
import com.tamir.followear.exceptions.CustomStreamException;
import com.tamir.followear.exceptions.InvalidPostException;
import com.tamir.followear.exceptions.InvalidUserException;
import com.tamir.followear.exceptions.SaveException;
import com.tamir.followear.jpaKeys.SaveKey;
import com.tamir.followear.repositories.SaveRepository;
import com.tamir.followear.stream.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class SaveService {

    @Autowired
    SaveRepository saveRepo;

    @Autowired
    UserService userService;

    @Autowired
    PostService postService;

    @Autowired
    StreamService streamService;

    public boolean didSaveItem(long userId, long postId) {
        return saveRepo.existsById(new SaveKey(userId, postId));
    }

    public Save saveItem(long userId, long postId) {
        if(!userService.existsById(userId))
            throw new InvalidUserException();
        if(!postService.existsById(postId))
            throw new InvalidPostException();
        if(didSaveItem(userId, postId))
            throw new SaveException("User can't save post twice");


        Save save = saveRepo.save(new Save(userId, postId));

        try {
            streamService.saveItem(userId, postId, save);
        } catch (CustomStreamException e) {
            saveRepo.delete(save);
            throw e;
        }

        return save;
    }

    public void unsaveItem(long userId, long postId) {
        if(!userService.existsById(userId))
            throw new InvalidUserException();
        if(!postService.existsById(postId))
            throw new InvalidPostException();
        if(!didSaveItem(userId, postId))
            throw new SaveException("Can't unsave a post which is not saved");

        streamService.unsaveItem(userId, postId);
        saveRepo.deleteById(new SaveKey(userId, postId));
    }
}
