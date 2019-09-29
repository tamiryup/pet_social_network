package com.tamir.followear.services;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tamir.followear.dto.BasicPostDTO;
import com.tamir.followear.dto.DiscoverPeopleDTO;
import com.tamir.followear.entities.User;
import com.tamir.followear.helpers.CollectionsHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

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
        List<Long> popularUsersIds = popularUsers.get();
        List<Long> alreadyFollowingIds = followService.getUserFollowingIds(userId);
        popularUsersIds.removeAll(alreadyFollowingIds);
        return popularUsersIds;
    }

    public List<User> getExploreUsers(long userId) {
        List<Long> popularUsersIds = getPopularUsersForUser(userId);
        List<Long> relevantUsersIds = relevantUsersCache.getUnchecked(userId);
        List<Long> exploreUsersIds = CollectionsHelper
                .mergeListsAlternativelyNoDuplicates(popularUsersIds, relevantUsersIds);

        System.out.println("explore users ids: " + exploreUsersIds);
        List<User> exploreUsers = userService.findAllById(exploreUsersIds);

        return exploreUsers;
    }

    public List<DiscoverPeopleDTO> getDiscoverPeople(long userId) {
        List<DiscoverPeopleDTO> discoverPeopleFeed = new ArrayList<>();

        List<User> exploreUsers = getExploreUsers(userId);
        for(User user : exploreUsers) {
            List<BasicPostDTO> items =
                    postService.getMorePostsFromUser(user.getId(), 0); // 0 to not exclude any post
            long numFollowers = followService.getNumFollowers(user.getId());
            DiscoverPeopleDTO person = new DiscoverPeopleDTO(user.getId(), user.getProfileImageAddr(),
                    user.getUsername(), user.getFullName(), numFollowers, items);
            discoverPeopleFeed.add(person);
        }

        return discoverPeopleFeed;
    }


}
