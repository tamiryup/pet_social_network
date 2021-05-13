package com.tamir.followear.repositories;

import com.tamir.followear.entities.Like;
import com.tamir.followear.jpaKeys.LikeKey;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface LikeRepository extends CrudRepository<Like, LikeKey> {


    @Transactional
    @Query(value =
            "SELECT * FROM likes WHERE post_id = :postId LIMIT :limit",
    nativeQuery = true)
    List<Like> findByPostIdWithLimit(@Param("postId") long postId, @Param("limit") int limit);

    long deleteByUserId(long userId);

}
