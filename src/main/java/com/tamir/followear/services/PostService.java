package com.tamir.followear.services;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.tamir.followear.AWS.s3.S3Service;
import com.tamir.followear.dto.UploadItemDTO;
import com.tamir.followear.entities.Post;
import com.tamir.followear.entities.User;
import com.tamir.followear.enums.Currency;
import com.tamir.followear.enums.ImageType;
import com.tamir.followear.exceptions.InvalidPostException;
import com.tamir.followear.exceptions.InvalidUserException;
import com.tamir.followear.helpers.FileHelper;
import com.tamir.followear.repositories.PostRepository;
import com.tamir.followear.stream.StreamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PostService {

    private final Logger logger = LoggerFactory.getLogger(PostService.class);

    @Autowired
    PostRepository postRepo;

    @Autowired
    UserService userService;

    @Autowired
    S3Service s3Service;

    @Autowired
    StreamService streamService;

    @Autowired
    ScrapingService scrapingService;

    public Post create(Post post) {
        return postRepo.save(post);
    }

    public Post findById(long id) {
        Optional<Post> optPost = postRepo.findById(id);
        if (!optPost.isPresent()) {
            throw new InvalidPostException();
        }

        return optPost.get();
    }

    public List<Post> findAllById(Iterable<Long> ids) {
        List<Post> postList = Lists.newArrayList(postRepo.findAllById(ids));
        List<Post> posts = new ArrayList<>();
        for (long id : ids) {
            Post postById = Iterables.tryFind(postList, post -> id == post.getId()).orNull();
            if (postById != null) //if null than id doesn't exist in database therefore do nothing
                posts.add(postById);
        }
        return posts;
    }

    public Post uploadPost(long userId, MultipartFile image, String description) throws IOException {
        if (!userService.existsById(userId))
            throw new InvalidUserException();
        ImageType imageType = ImageType.PostImage;
        String extension = FileHelper.getMultipartFileExtension(image);
        String imageAddr = s3Service.uploadImage(imageType, image, extension);
        Post post = new Post(userId, imageAddr, description);
        post = create(post);
        streamService.uploadActivity(post);
        return post;
    }

    public Post uploadItemPost(long userId, UploadItemDTO item) throws IOException {
        if (!userService.existsById(userId))
            throw new InvalidUserException();

        List<String> thumbnails = scrapingService.getThumbnailImages(item.getStoreId(),
                item.getLink());

        ImageType imageType = ImageType.PostImage;
        InputStream imageInputStream = FileHelper.urlToInputStream(item.getImageAddr());
        String imageAddr = s3Service.uploadImage(imageType, imageInputStream, item.getImgExtension());

        //extract thumbnails of item
        List<String> thumbnailAddresses = new ArrayList<>();
        for(int i=0; i<thumbnails.size() && i<1; i++) {
            imageInputStream = FileHelper.urlToInputStream(thumbnails.get(i));
            String thumbnailAddr = s3Service.uploadImage(imageType, imageInputStream, item.getImgExtension());
            thumbnailAddresses.add(thumbnailAddr);
        }
        String thumbnail = (thumbnails.size() > 0) ? thumbnails.get(0) : null;

        Post post = new Post(userId, item.getStoreId(), imageAddr, item.getDescription(), item.getLink(),
                item.getPrice(), Currency.ILS, item.getDesigner(), item.getProductId(),
                thumbnail, item.getCategory(), item.getProductType());
        post = create(post);
        streamService.uploadActivity(post);
        return post;
    }

    public Post uploadLink(long userId, String website, String link) throws IOException {
        UploadItemDTO item = scrapingService.extractItem(website, link);
        return uploadItemPost(userId, item);
    }

    public long getNumPostsByUserId(long userId) {
        User user = userService.findById(userId);
        if (user == null)
            throw new InvalidUserException();
        return postRepo.countByUserId(userId);
    }
}
