package com.tamir.followear.repositories;

import com.tamir.followear.entities.Follow;
import com.tamir.followear.jpaKeys.FollowKey;
import org.springframework.data.repository.CrudRepository;

public interface FollowRepository extends CrudRepository<Follow, FollowKey> {

    long countByMasterId(long masterId);

    long countBySlaveId(long slaveId);

    boolean existsByMasterIdAndSlaveId(long masterId, long slaveId);
}
