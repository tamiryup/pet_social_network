package com.tamir.followear.repositories;

import com.tamir.followear.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.PathVariable;

import javax.transaction.Transactional;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    long countByUserId(long id);

    @Transactional
    @Query(value =
            "SELECT * FROM posts p WHERE p.user_id = :userId and p.id != :excludeId " +
            "ORDER BY p.create_date DESC LIMIT :limit",
    nativeQuery = true)
    List<Post> lastPostsByUser(@Param("userId") long userId, @Param("limit") int limit,
                               @Param("excludeId") long excludeId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE Post p SET p.numViews = p.numViews + 1\n" +
            "WHERE p.id = :postId AND p.userId != :userId")
    void incPostViews(@Param("userId") long userId, @Param("postId") long postId);
}
