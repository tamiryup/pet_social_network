package com.tamir.followear.repositories;

import com.tamir.followear.entities.Save;
import com.tamir.followear.jpaKeys.SaveKey;
import org.springframework.data.repository.CrudRepository;

public interface SaveRepository extends CrudRepository<Save, SaveKey> {

    long deleteByUserId(long userId);
}
