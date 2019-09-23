package com.tamir.followear.services;

import com.google.common.base.Suppliers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class ExploreService {

    private final int mostPopularUsersLimit = 50;

    @Autowired
    FollowService followService;

    //save 50 most popular users in cache and update once every day
    private Supplier<List<Long>> popularUsers = Suppliers.memoizeWithExpiration(
            this::mostPopularUsersSupplier, 1, TimeUnit.DAYS);

    private List<Long> mostPopularUsersSupplier() {
        return followService.mostPopularUsersIds(mostPopularUsersLimit);
    }
}
