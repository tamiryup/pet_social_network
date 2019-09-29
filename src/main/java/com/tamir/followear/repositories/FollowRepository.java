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

    List<Follow> findBySlaveId(long slaveId);

    @Transactional
    @Query(value =
            "SELECT master_id, COUNT(master_id) as num_followers\n" +
            "FROM follows\n" +
            "GROUP BY master_id\n" +
            "ORDER BY num_followers DESC\n" +
            "LIMIT :limit",
    nativeQuery = true)
    List<Object[]> getPopularUsers(@Param("limit") int limit);

    /**
     * Selects relevant users for a specific user.
     * A relevant user is a user which is followed by someone I'm following,
     * but is not followed by me.
     *
     * @param userId The user
     * @param limit
     * @return A list of relevant users (ids) and their relevancy
     *          (relevancy - how many people I'm following follow this "relevant user")
     */
    @Transactional
    @Query(value =
            "SELECT  m.master_id as relevant_id, COUNT(m.master_id) as relevancy\n" +
            "FROM follows s\n" +
            "INNER JOIN follows m on s.master_id = m.slave_id\n" +
            "WHERE s.slave_id = :userId AND m.master_id != :userId\n" +
            "AND m.master_id NOT IN (SELECT master_id FROM follows WHERE slave_id = :userId)\n" +
            "GROUP BY relevant_id\n" +
            "ORDER BY relevancy DESC\n" +
            "LIMIT :limit",
    nativeQuery = true)
    List<Object[]> getRelevantSuggestionsForUser(@Param("userId") long userId, @Param("limit") int limit);
}
