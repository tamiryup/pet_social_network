package com.tamir.followear.services;

import com.tamir.followear.entities.UserDevice;
import com.tamir.followear.exceptions.InvalidUserException;
import com.tamir.followear.jpaKeys.UserDeviceKey;
import com.tamir.followear.repositories.UserDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UserDeviceService {

    @Autowired
    UserDeviceRepository userDeviceRepo;

    @Autowired
    UserService userService;

    public boolean existsById(UserDeviceKey key) {
        return userDeviceRepo.existsById(key);
    }

    public UserDevice findById(UserDeviceKey key) {
        Optional<UserDevice> optUserDevice =  userDeviceRepo.findById(key);
        if(!optUserDevice.isPresent()) {
            return null;
        }

        return optUserDevice.get();
    }

    public UserDevice addUserDevice(long userId, String registrationToken) {
        if(!userService.existsById(userId))
            throw new InvalidUserException();

        UserDeviceKey userDeviceKey = new UserDeviceKey(userId, registrationToken);
        if(existsById(userDeviceKey))
            return findById(userDeviceKey);


        UserDevice userDevice = new UserDevice(userId, registrationToken);
        userDevice = userDeviceRepo.save(userDevice);
        return userDevice;
    }

    public void removeUserDevice(long userId, String registrationToken) {
        if(!userService.existsById(userId))
            throw new InvalidUserException();
        if(!existsById(new UserDeviceKey(userId, registrationToken)))
            return;

        userDeviceRepo.deleteById(new UserDeviceKey(userId, registrationToken));
    }
}
