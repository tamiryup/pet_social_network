package com.tamir.followear.services;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.tamir.followear.AWS.s3.S3Service;
import com.tamir.followear.dto.BasicPostDTO;
import com.tamir.followear.dto.PostInfoDTO;
import com.tamir.followear.dto.UploadItemDTO;
import com.tamir.followear.entities.Post;
import com.tamir.followear.entities.Store;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PostService {

    private final Logger logger = LoggerFactory.getLogger(PostService.class);

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private StoreService storeService;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private StreamService streamService;

    @Autowired
    private ScrapingService scrapingService;

    public Post create(Post post) {
        return postRepo.saveAndFlush(post);
    }

    public Post findById(long id) {
        Optional<Post> optPost = postRepo.findById(id);
        if (!optPost.isPresent()) {
            throw new InvalidPostException();
        }

        return optPost.get();
    }

    /**
     * finds all posts with ids from the list of ids (plus duplicates)
     *
     * @param ids A list of post ids to find in the database
     * @return A list of posts with the ids specified including duplicates
     * in case there is a duplicate id. The list returns the posts in the same order
     * as the ids received.
     */
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

        try {
            streamService.uploadActivity(post);
        } catch (Exception e) {
            postRepo.delete(post);
            throw e;
        }

        return post;
    }

    public Post uploadItemPost(long userId, UploadItemDTO item) throws IOException {
        if (!userService.existsById(userId))
            throw new InvalidUserException();

        List<String> thumbnails = item.getThumbnails();
        if (thumbnails == null || thumbnails.size() == 0) {
            thumbnails = scrapingService.getThumbnailImages(item.getStoreId(),
                    item.getLink());
        }

        ImageType imageType = ImageType.PostImage;
        InputStream imageInputStream = FileHelper.urlToInputStream(item.getImageAddr());
        String imageAddr = s3Service.uploadImage(imageType, imageInputStream, item.getImgExtension());

        //extract thumbnails of item
        List<String> thumbnailAddresses = new ArrayList<>();
        for (int i = 0; i < thumbnails.size() && i < 1; i++) {
            imageInputStream = FileHelper.urlToInputStream(thumbnails.get(i));
            String thumbnailAddr = s3Service.uploadImage(imageType, imageInputStream, item.getImgExtension());
            thumbnailAddresses.add(thumbnailAddr);
        }
        String thumbnail = (thumbnailAddresses.size() > 0) ? thumbnailAddresses.get(0) : null;

        Post post = new Post(userId, item.getStoreId(), imageAddr, item.getDescription(), item.getLink(),
                item.getPrice(), Currency.ILS, item.getDesigner(), item.getProductId(),
                thumbnail, item.getCategory(), item.getProductType());
        post = create(post);

        try {
            streamService.uploadActivity(post);
        } catch (Exception e) {
            postRepo.delete(post);
            throw e;
        }

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

    public PostInfoDTO getPostInfo(long postId) {
        Post post = findById(postId);
        User user = userService.findById(post.getUserId());
        Store store = storeService.findById(post.getStoreId());

        PostInfoDTO postInfo = new PostInfoDTO(post.getId(), post.getUserId(), post.getStoreId(),
                user.getProfileImageAddr(), user.getUsername(), post.getImageAddr(), post.getDescription(),
                post.getFormattedPrice(), store.getLogoAddr(), store.getName(), store.getWebsite(),
                post.getThumbnail(), post.getNumViews());

        return postInfo;
    }

    /**
     * Given a specific user, return 3 more posts from that user.
     *
     * @param userId
     * @param currPostId the 3 posts returned must NOT include this post
     * @return 3 random posts from the user chosen randomly from the user's last 14 posts
     */
    public List<BasicPostDTO> getMorePostsFromUser(long userId, long currPostId) {
        List<Post> last14 = postRepo.lastPostsByUser(userId, 14, currPostId);
        Collections.shuffle(last14);

        List<BasicPostDTO> moreFromList = new ArrayList<>();
        for(int i=0; i<last14.size() && i<3; i++) {
            Post post = last14.get(i);
            moreFromList.add(new BasicPostDTO(post.getId(), post.getImageAddr()));
        }

        return moreFromList;
    }

    /**
     * Increment the numViews of specific post iff the user sending the request
     * is not the same user who uploaded the post.
     * Essentially the user who uploaded the post cannot count as a "view".
     *
     * @param userId The user who sent the request (the user who is viewing the post)
     * @param postId
     */
    public void incPostViews(long userId, long postId) {
        postRepo.incPostViews(userId, postId);
    }
}
