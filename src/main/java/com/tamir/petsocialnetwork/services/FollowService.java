package com.tamir.petsocialnetwork.services;

import com.tamir.petsocialnetwork.entities.Follow;
import com.tamir.petsocialnetwork.exceptions.InvalidUserException;
import com.tamir.petsocialnetwork.jpaKeys.FollowKey;
import com.tamir.petsocialnetwork.repositories.FollowRepository;
import com.tamir.petsocialnetwork.stream.StreamService;
import org.omg.CORBA.DynAnyPackage.Invalid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

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
}
