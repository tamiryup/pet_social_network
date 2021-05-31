package com.tamir.followear.services;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.tamir.followear.AWS.s3.S3Service;
import com.tamir.followear.CommonBeanConfig;
import com.tamir.followear.dto.BasicPostDTO;
import com.tamir.followear.dto.PostInfoDTO;
import com.tamir.followear.dto.UploadItemDTO;
import com.tamir.followear.entities.Post;
import com.tamir.followear.entities.Store;
import com.tamir.followear.entities.User;
import com.tamir.followear.enums.Currency;
import com.tamir.followear.enums.ImageType;
import com.tamir.followear.exceptions.*;
import com.tamir.followear.helpers.FileHelper;
import com.tamir.followear.helpers.ImageHelper;
import com.tamir.followear.helpers.StringHelper;
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
import java.util.concurrent.ExecutionException;

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

    @Autowired
    private AffiliationService affilationService;

    @Autowired
    private CurrencyConverterService currConverterService;

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

    public boolean existsById(long id) {
        return postRepo.existsById(id);
    }

    public long getPostUserId(long id) {
        Post post = findById(id);
        if(post == null) {
            return -1;
        }

        return post.getUserId();
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

    public void validateUploadItem(long userId, UploadItemDTO item) {
        if (!userService.existsById(userId))
            throw new InvalidUserException();
        if ( existsByItem(userId, item.getStoreId(), item.getProductId()) )
            throw new PostAlreadyExistsException();
    }

    private void handleItemPrice(UploadItemDTO item) {
        String price = StringHelper.removeCommas(item.getPrice()); //save price without commas
        String salePrice = StringHelper.removeCommas(item.getSalePrice());
        if(item.getCurrency() == Currency.EUR) { //save price in ILS if currency is EUR
            double priceAsEur = Double.valueOf(price);
            double priceAsIls = currConverterService.convert(Currency.EUR, Currency.ILS, priceAsEur);
            price = "" + priceAsIls;

            if(!salePrice.equals("")) {
                double salePriceAsEur = Double.valueOf(salePrice);
                double salePriceAsIls = currConverterService.convert(Currency.EUR, Currency.ILS, salePriceAsEur);
                salePrice =  "" + salePriceAsIls;
            }

            item.setCurrency(Currency.ILS);
        }

        item.setPrice(price);
        item.setSalePrice(salePrice);
    }

    public Post uploadItemPost(long userId, UploadItemDTO item) throws IOException {
        validateUploadItem(userId, item);

        logger.info("in uploadItemPost input item: {}", item);

        List<String> thumbnails = item.getThumbnails();

        ImageType imageType = ImageType.PostImage;
        InputStream imageInputStream = getFinalImageInputStream(item.getImageAddr(), item.getStoreId());
        String imageAddr = s3Service.uploadImage(imageType, imageInputStream, item.getImgExtension());
        imageInputStream.close();

        //extract thumbnails of item
        List<String> thumbnailAddresses = new ArrayList<>();
        for (int i = 0; i < thumbnails.size() && i < 1; i++) {
            imageInputStream = getFinalImageInputStream(thumbnails.get(i), item.getStoreId());
            String thumbnailAddr = s3Service.uploadImage(imageType, imageInputStream, item.getImgExtension());
            thumbnailAddresses.add(thumbnailAddr);
            imageInputStream.close();
        }
        String thumbnail = (thumbnailAddresses.size() > 0) ? thumbnailAddresses.get(0) : null;

        handleItemPrice(item);

        Post post = new Post(userId, item.getStoreId(), imageAddr, item.getDescription(), item.getLink(),
                item.getPrice(), item.getSalePrice(), item.getCurrency(), item.getDesigner(), item.getProductId(),
                thumbnail, item.getCategory(), item.getProductType());
        post = create(post);

        try {
            streamService.uploadActivity(post);
        } catch (CustomStreamException e) {
            postRepo.delete(post);
            throw e;
        }

        return post;
    }

    private InputStream getFinalImageInputStream(String imageAddr, long storeId) throws IOException {
        InputStream imageInputStream = FileHelper.urlToInputStream(imageAddr);

        //crop image if 24/7
        if(storeId == 18) {
            imageInputStream = ImageHelper.cropImage(imageInputStream, 20, 0, 527, 813);
        }

        return imageInputStream;
    }

    public Post uploadLink(long userId, String link) throws IOException {
        UploadItemDTO item = scrapingService.extractItem(link);
        return uploadItemPost(userId, item);
    }

    public Post rstyleUploadLink(long userId, String link, String rstyleLink) throws IOException {
        UploadItemDTO item = scrapingService.extractItem(link);
        item.setLink(rstyleLink);
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

        //send back affiliated link
        String link = affilationService.getAffiliatedLink(post.getLink(), user.getId(), store.getId());

        PostInfoDTO postInfo = new PostInfoDTO(post.getId(), post.getUserId(), post.getStoreId(),
                user.getProfileImageAddr(), user.getUsername(), post.getImageAddr(), post.getDescription(),
                post.getFormattedPrice(), post.getFormattedSalePrice(), store.getLogoAddr(), store.getName(),
                store.getWebsite(), post.getThumbnail(), post.getSelfThumb(), link,
                post.getNumViews(), post.getNumLikes(), post.getCreateDate());

        return postInfo;
    }

    /**
     * Given a specific user, return {@code numPosts} more posts from that user.
     *
     * @param userId
     * @param currPostId the posts returned must NOT include this post
     * @param numPosts the number of posts to return (if larger than 14 returns 14)
     *
     * @return {@code numPosts} random posts from the user chosen randomly from the user's last 14 posts
     */
    public List<Post> getMorePostsFromUser(long userId, long currPostId, int numPosts) {
        List<Post> last14 = postRepo.lastPostsByUser(userId, 14, currPostId);
        Collections.shuffle(last14);

        List<Post> moreFromList = new ArrayList<>();
        for(int i=0; i<last14.size() && i<numPosts; i++) {
            Post post = last14.get(i);
            moreFromList.add(post);
        }

        return moreFromList;
    }


    public List<BasicPostDTO> moreFromUser(long userId, long currPostId, int numPosts) {
        if(!userService.existsById(userId)) {
            throw new InvalidUserException();
        }

        List<BasicPostDTO> resultList = new ArrayList<>();
        List<Post> posts = getMorePostsFromUser(userId, currPostId, numPosts);
        for(Post post : posts) {
            resultList.add(new BasicPostDTO(post.getId(), post.getImageAddr()));
        }
        return resultList;
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

    /**
     * Increment the numRedirects of a specific post.
     * this time the user who uploaded the post does count as a redirect.
     *
     * @param postId
     */
    public void incPostRedirects(long postId) {
        postRepo.incPostRedirects(postId);
    }

    public void incNumLikes(long postId) {
        postRepo.incNumLikes(postId);
    }

    public void decNumLikes(long postId) {
        postRepo.decNumLikes(postId);
    }

    public List<Post> getMostPopularPosts(int limit) {
        return postRepo.recentMostPopularPosts(limit);
    }

    public List<Post> getMostPopularPostsForUser(long userId, int limit) {
        return postRepo.recentMostPopularPostsForUser(userId, limit);
    }

    public boolean existsByItem(long userId, long storeId, String productId) {
        int itemCount = postRepo.countByItem(userId, storeId, productId);
        if(itemCount > 0) {
            return true;
        }
        return false;
    }

    public void removePost(long userId, long postId) {
        Post post = findById(postId);

        if(post == null)
            throw new InvalidPostException();
        if(post.getUserId() != userId)
            throw new NoAuthException("user does not have permission to delete this post");

        streamService.removeActivity(post);
        postRepo.deleteById(postId);
        s3Service.deleteByKey(post.getSelfThumb());
    }

    public void hidePost(long userId, long postId) {
        if(!userService.existsById(userId))
            throw new InvalidUserException();
        Post post = findById(postId);
        streamService.hideActivity(userId, post);
    }

    public String uploadSelfThumb(long userId, long postId, MultipartFile image) throws IOException {
        Post post = findById(postId);
        String prevSelfThumb = post.getSelfThumb();

        if(post == null)
            throw new InvalidPostException();
        if(post.getUserId() != userId)
            throw new NoAuthException("this user does not own this post");
        if(prevSelfThumb == null || !prevSelfThumb.equals(""))
            return prevSelfThumb;

        ImageType imageType = ImageType.SelfImage;
        String extension = FileHelper.getMultipartFileExtension(image);
        String addr = s3Service.uploadImage(imageType, image, extension);
        postRepo.updateSelfThumbById(postId, addr);

        return addr;
    }

    public void removeSelfThumb(long userId, long postId) {
        Post post = findById(postId);
        String prevSelfThumb = post.getSelfThumb();

        if(post == null)
            throw new InvalidPostException();
        if(post.getUserId() != userId)
            throw new NoAuthException("this user does not own this post");
        if(prevSelfThumb.equals(""))
            return;

        postRepo.updateSelfThumbById(postId, "");
        s3Service.deleteByKey(prevSelfThumb);
    }

    /**
     * delete all records by userId
     *
     * @param userId - The id of the uploading user
     */
    public void deleteAllByUserId(long userId) {
        postRepo.deleteByUserId(userId);
    }

}
