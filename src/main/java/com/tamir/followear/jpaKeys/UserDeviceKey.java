package com.tamir.followear.jpaKeys;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class UserDeviceKey implements Serializable {

    private long userId;
    private String registrationToken;

}
