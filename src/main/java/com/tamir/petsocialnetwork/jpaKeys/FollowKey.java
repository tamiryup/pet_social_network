package com.tamir.petsocialnetwork.jpaKeys;

import java.io.Serializable;

public class FollowKey implements Serializable {
    private long masterId;
    private long slaveId;

    public FollowKey(){

    }

    public FollowKey(long masterId, long slaveId) {
        this.masterId = masterId;
        this.slaveId = slaveId;
    }
}
