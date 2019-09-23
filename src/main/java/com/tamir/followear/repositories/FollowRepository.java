package com.tamir.followear.repositories;

import com.tamir.followear.entities.Follow;
import com.tamir.followear.jpaKeys.FollowKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface FollowRepository extends CrudRepository<Follow, FollowKey> {

    long countByMasterId(long masterId);

    long countBySlaveId(long slaveId);

    boolean existsByMasterIdAndSlaveId(long masterId, long slaveId);

    @Transactional
    @Query(value = "SELECT master_id, COUNT(master_id) as num_followers\n" +
            "FROM follows GROUP BY master_id ORDER BY num_followers DESC LIMIT :limit",
    nativeQuery = true)
    List<Object[]> getPopularUsers(@Param("limit") int limit);
}
