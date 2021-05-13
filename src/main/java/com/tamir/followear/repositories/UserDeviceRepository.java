package com.tamir.followear.repositories;

import com.tamir.followear.entities.UserDevice;
import com.tamir.followear.jpaKeys.UserDeviceKey;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserDeviceRepository extends CrudRepository<UserDevice, UserDeviceKey> {

    List<UserDevice> findByUserId(long userId);

    long deleteByUserId(long userId);

}
