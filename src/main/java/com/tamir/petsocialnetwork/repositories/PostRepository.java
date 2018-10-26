package com.tamir.petsocialnetwork.repositories;

import com.tamir.petsocialnetwork.entities.Post;
import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post, Long> {

    long countByUserId(long id);
}
