package com.tamir.followear.services;

import com.tamir.followear.entities.Follow;
import com.tamir.followear.entities.UserDevice;
import com.tamir.followear.exceptions.InvalidUserException;
import com.tamir.followear.exceptions.NoFollowKeyException;
import com.tamir.followear.jpaKeys.FollowKey;
import com.tamir.followear.repositories.FollowRepository;
import com.tamir.followear.stream.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FollowService {

    @Autowired
    private FollowRepository followRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private StreamService streamService;

    @Autowired
    private NotificationService notificationService;

    public Follow findById(FollowKey key) {
        Optional<Follow> optFollow =  followRepo.findById(key);
        if(!optFollow.isPresent()) {
            return null;
        }

        return optFollow.get();
    }

    public Follow follow(long masterId, long slaveId) {
        if (!userService.existsById(slaveId) || !userService.existsById(masterId))
            throw new InvalidUserException();
        if(isFollowing(masterId, slaveId))
            return findById(new FollowKey(masterId, slaveId));

        Follow follow = new Follow(masterId, slaveId);
        follow = followRepo.save(follow);
        streamService.follow(masterId, slaveId);
        notificationService.sendFollowNotification(masterId, slaveId);

        return follow;
    }

    public void unfollow(long masterId, long slaveId) {
        if (!userService.existsById(slaveId) || !userService.existsById(masterId))
            throw new InvalidUserException();
        if (!isFollowing(masterId, slaveId))
            throw new NoFollowKeyException();

        streamService.unfollow(masterId, slaveId);
        FollowKey key = new FollowKey(masterId, slaveId);
        followRepo.deleteById(key);
    }

    public long getNumFollowers(long masterId) {
        if (!userService.existsById(masterId))
            throw new InvalidUserException();
        return followRepo.countByMasterId(masterId);
    }

    public long getNumFollowing(long slaveId) {
        if (!userService.existsById(slaveId))
            throw new InvalidUserException();
        return followRepo.countBySlaveId(slaveId);
    }

    public boolean isFollowing(long masterId, long slaveId) {
        return followRepo.existsById(new FollowKey(masterId, slaveId));
    }

    /**
     * Returns a list of the most popular users
     * (popularity is measured by number of followers)
     * The list is in descending order - 0: most popular user, 1: second most popular user, etc.
     *
     * @param limit The limit on the list's size (the list's size might be less than the limit)
     * @return A list of userIds
     */
    public List<Long> mostPopularUsersIds(int limit) {

        List<Long> ids = new ArrayList<>();
        List<Object[]> popularUsersData = followRepo.getPopularUsers(limit);
        for (Object[] userData : popularUsersData) {
            long id = ((BigInteger) userData[0]).longValue();
            ids.add(id);
        }

        return ids;
    }

    /**
     * Returns relevant users for specific user.
     * see {@link FollowRepository#getRelevantSuggestionsForUser(long, int)}
     * In addition to the explanation at the link, a relevant user must be followed by
     * at least 8% of the people I'm following. where that 8% must be larger than 2
     *
     * @param userId
     * @param limit
     * @return A list of relevant users ids
     */
    public List<Long> relevantSuggestionsForUser(long userId, int limit) {
        double numFollowing = getNumFollowing(userId); //type double to evaluate the threshold expression
        long relevancyThreshold = Math.max((long) ((numFollowing * 8) / 100), 2);

        List<Long> ids = new ArrayList<>();
        List<Object[]> relevantUseresData = followRepo.getRelevantSuggestionsForUser(userId, limit);

        for (Object[] userData : relevantUseresData) {
            long id = ((BigInteger) userData[0]).longValue();
            long relevancy = ((BigInteger) userData[1]).longValue();
            if(relevancy >= relevancyThreshold) {
                ids.add(id);
            }
        }

        return ids;
    }

    public List<Long> getUserFollowingIds(long userId) {
        List<Follow> followList = followRepo.findBySlaveId(userId);
        List<Long> followingIds = followList.stream().map(follow -> follow.getMasterId())
                .collect(Collectors.toList());
        return followingIds;
    }

    /**
     * delete all records by userId
     * every row with an occurrence of 'userId', both in master and slave column, will be removed
     *
     * @param userId - The id of the user to be removed
     */
    public void deleteAllByUserId(long userId) {
        followRepo.deleteUserFromFollows(userId);
    }
}
