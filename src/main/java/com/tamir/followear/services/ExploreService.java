package com.tamir.followear.services;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class ExploreService {

    @Autowired
    FollowService followService;

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
                .maximumSize(1000)
                .build(new CacheLoader<Long, List<Long>>() {
                    @Override
                    public List<Long> load(Long userId) throws Exception {
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
}
