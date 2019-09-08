package com.tamir.followear.repositories;

import com.tamir.followear.entities.Post;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post, Long> {

    long countByUserId(long id);
}
