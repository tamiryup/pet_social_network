package com.tamir.followear.entities;

import com.tamir.followear.jpaKeys.UserDeviceKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "user_devices")
@IdClass(UserDeviceKey.class)
@Getter
@NoArgsConstructor
public class UserDevice {

    @Id
    private long userId;

    @Id
    private String registrationToken;

    @CreationTimestamp
    private Date createDate;

    public UserDevice(long userId, String registrationToken) {
        this.userId = userId;
        this.registrationToken = registrationToken;
    }

}
