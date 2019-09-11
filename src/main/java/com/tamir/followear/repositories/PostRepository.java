package com.tamir.followear.repositories;

import com.tamir.followear.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    long countByUserId(long id);
}
