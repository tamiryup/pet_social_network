package com.tamir.petsocialnetwork.services;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.tamir.petsocialnetwork.AWS.s3.S3Service;
import com.tamir.petsocialnetwork.entities.Post;
import com.tamir.petsocialnetwork.entities.User;
import com.tamir.petsocialnetwork.enums.ImageType;
import com.tamir.petsocialnetwork.exceptions.InvalidUserException;
import com.tamir.petsocialnetwork.helpers.FileHelper;
import com.tamir.petsocialnetwork.repositories.PostRepository;
import com.tamir.petsocialnetwork.stream.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PostService {

    @Autowired
    PostRepository postRepo;

    @Autowired
    UserService userService;

    @Autowired
    S3Service s3Service;

    @Autowired
    StreamService streamService;

    public Post create(Post post){
        return postRepo.save(post);
    }

    public List<Post> findAllById(Iterable<Long> ids){
        List<Post> postList = Lists.newArrayList(postRepo.findAllById(ids));
        List<Post> posts = new ArrayList<>();
        for(long id : ids){
            Post postById = Iterables.tryFind(postList, post -> id==post.getId()).orNull();
            if(postById!=null) //if null than id doesn't exist in database therefore do nothing
                posts.add(postById);
        }
        return posts;
    }

    public Post uploadPost(long id, MultipartFile image, String description) throws IOException {
        if(!userService.existsById(id))
            throw new InvalidUserException();
        ImageType imageType = ImageType.PostImage;
        String extension = FileHelper.getMultipartFileExtension(image);
        String imageAddr = s3Service.uploadImage(imageType, image, extension);
        Post post = new Post(id, imageAddr, description);
        post = create(post);
        streamService.uploadActivity(id, post.getId());
        return post;
    }

    public long getNumPostsByUserId(long userId){
        User user = userService.findById(userId);
        if(user==null)
            throw new InvalidUserException();
        return postRepo.countByUserId(userId);
    }
}
