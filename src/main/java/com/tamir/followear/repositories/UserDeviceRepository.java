package com.tamir.followear.repositories;

import com.tamir.followear.entities.UserDevice;
import com.tamir.followear.jpaKeys.UserDeviceKey;
import org.springframework.data.repository.CrudRepository;

public interface UserDeviceRepository extends CrudRepository<UserDevice, UserDeviceKey> {

}
