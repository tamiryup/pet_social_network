package com.tamir.followear.repositories;

import com.tamir.followear.entities.Like;
import com.tamir.followear.jpaKeys.LikeKey;
import org.springframework.data.repository.CrudRepository;

public interface LikeRepository extends CrudRepository<Like, LikeKey> {

}
