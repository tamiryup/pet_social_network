package com.tamir.followear.services;

import com.tamir.followear.entities.Follow;
import com.tamir.followear.exceptions.InvalidUserException;
import com.tamir.followear.jpaKeys.FollowKey;
import com.tamir.followear.repositories.FollowRepository;
import com.tamir.followear.stream.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FollowService {

    @Autowired
    FollowRepository followRepo;

    @Autowired
    UserService userService;

    @Autowired
    StreamService streamService;

    public Follow follow(long masterId, long slaveId){
        if(!userService.existsById(slaveId) || !userService.existsById(masterId))
            throw new InvalidUserException();
        streamService.follow(masterId, slaveId);
        Follow follow = new Follow(masterId, slaveId);
        follow = followRepo.save(follow);
        return follow;
    }

    public void unfollow(long masterId, long slaveId){
        if(!userService.existsById(slaveId) || !userService.existsById(masterId))
            throw new InvalidUserException();
        streamService.unfollow(masterId, slaveId);
        FollowKey key = new FollowKey(masterId, slaveId);
        followRepo.deleteById(key);
    }

    public long getNumFollowers(long masterId){
        if(!userService.existsById(masterId))
            throw new InvalidUserException();
        return followRepo.countByMasterId(masterId);
    }

    public long getNumFollowing(long slaveId){
        if(!userService.existsById(slaveId))
            throw new InvalidUserException();
        return followRepo.countBySlaveId(slaveId);
    }

    public boolean isFollowing(long masterId, long slaveId){
        return followRepo.existsByMasterIdAndSlaveId(masterId, slaveId);
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
        for(Object[] userData : popularUsersData) {
            long id = ((BigInteger)userData[0]).longValue();
            ids.add(id);
        }

        return ids;
    }
}
