package com.tamir.petsocialnetwork.repositories;

import com.tamir.petsocialnetwork.entities.Follow;
import com.tamir.petsocialnetwork.jpaKeys.FollowKey;
import org.springframework.data.repository.CrudRepository;

public interface FollowRepository extends CrudRepository<Follow, FollowKey> {

    long countByMasterId(long masterId);

    long countBySlaveId(long slaveId);

    boolean existsByMasterIdAndSlaveId(long masterId, long slaveId);
}
