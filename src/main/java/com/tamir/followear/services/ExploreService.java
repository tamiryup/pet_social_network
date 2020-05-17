package com.tamir.followear.services;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tamir.followear.entities.Post;
import com.tamir.followear.entities.User;
import com.tamir.followear.helpers.CollectionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class ExploreService {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    private final int mostPopularUsersLimit = 50;

    private final int relevantUsersLimit = 50;

    //save 50 most popular users in cache and update once every day
    private Supplier<List<Long>> popularUsers;

    //cache to save 50 relevant users for a specific user
    private LoadingCache<Long, List<Long>> relevantUsersCache;

    @PostConstruct
    private void init() {
        popularUsers = Suppliers.memoizeWithExpiration(
                this::mostPopularUsersSupplier, 1, TimeUnit.DAYS);

        relevantUsersCache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(1, TimeUnit.DAYS)
                .build(new CacheLoader<Long, List<Long>>() {
                    @Override
                    public List<Long> load(Long userId) {
                        return loadRelevantUsers(userId);
                    }
                });
    }

    private List<Long> mostPopularUsersSupplier() {
        return followService.mostPopularUsersIds(mostPopularUsersLimit);
    }

    private List<Long> loadRelevantUsers(long userId) {
        return followService.relevantSuggestionsForUser(userId, relevantUsersLimit);
    }

    /**
     * Just like the popular users only without the users that the given user is already following
     *
     * @param userId
     * @return A list of popular users
     * without the ones the user with the specific userId is already following
     */
    private List<Long> getPopularUsersForUser(long userId) {
        List<Long> popularUsersIds = new ArrayList<>(popularUsers.get());
        List<Long> alreadyFollowingIds = followService.getUserFollowingIds(userId);
        popularUsersIds.removeAll(alreadyFollowingIds);
        popularUsersIds.remove(userId);
        return popularUsersIds;
    }

    public List<User> getExploreUsers(long userId) {
        List<Long> popularUsersIds = getPopularUsersForUser(userId);
        List<Long> relevantUsersIds = relevantUsersCache.getUnchecked(userId);
        List<Long> exploreUsersIds = CollectionsHelper
                .mergeListsAlternativelyNoDuplicates(popularUsersIds, relevantUsersIds);

        List<User> exploreUsers = userService.findAllByIdWithDuplicates(exploreUsersIds);

        return exploreUsers;
    }

    /**
     * General explore users for an unsigned user.
     * The users in the explore for an unsigned user are just the popular users.
     *
     * @return
     */
    public List<User> getExploreUsers() {
        List<Long> popularUsersIds = popularUsers.get();
        List<User> exploreUsers = userService.findAllByIdWithDuplicates(popularUsersIds);
        return exploreUsers;
    }

    private List<Post> getExplorePostsFromUserList(List<User> exploreUsers) {
        List<Post> posts = new ArrayList<>();
        List<Integer> coeffs = Arrays.asList(5,4,3,2,1);

        for(int i=0; i<50 && i<exploreUsers.size(); i++) {
            long currId = exploreUsers.get(i).getId();
            int numPosts = coeffs.get(i/10);
            posts.addAll(postService.getMorePostsFromUser(currId, 0, numPosts));
        }

        return posts;
    }

    private List<Post> getExploreMostPopularPosts(long userId, int limit) {
        if(userId == -1)
            return postService.getMostPopularPosts(limit);

        return postService.getMostPopularPostsForUser(userId, limit);
    }

    private List<Post> getExplorePosts(long userId, List<User> exploreUsers) {
        List<Post> explorePostsByUsers = getExplorePostsFromUserList(exploreUsers);
        List<Post> explorePostsByNumViews = getExploreMostPopularPosts(userId, 75);

        List<Post> explorePosts = new ArrayList<>();
        explorePosts.addAll(explorePostsByUsers);
        explorePosts.addAll(explorePostsByNumViews);
        explorePosts = explorePosts.stream().distinct().collect(Collectors.toList()); //remove duplicates

        Collections.shuffle(explorePosts);
        return explorePosts;
    }

    public List<Post> getExplorePosts() {
        List<User> exploreUsers = getExploreUsers();
        return getExplorePosts(-1, exploreUsers);
    }

    public List<Post> getExplorePosts(long userId) {
        List<User> exploreUsers = getExploreUsers(userId);
        return getExplorePosts(userId, exploreUsers);
    }

}
