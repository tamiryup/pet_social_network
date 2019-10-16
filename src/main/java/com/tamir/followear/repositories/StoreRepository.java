package com.tamir.followear.repositories;

import com.tamir.followear.entities.Store;
import org.springframework.data.repository.CrudRepository;

public interface StoreRepository extends CrudRepository<Store, Long> {

    Store findByWebsite(String website);
}
