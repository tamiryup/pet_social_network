package com.tamir.petsocialnetwork.entities;

import com.tamir.petsocialnetwork.jpaKeys.FollowKey;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@Table(name = "follows")
@IdClass(FollowKey.class)
public class Follow {

    @Id
    private long masterId;

    @Id
    private long slaveId;

    public Follow(){

    }

    public Follow(long masterId, long slaveId){
        this.masterId = masterId;
        this.slaveId = slaveId;
    }

    public long getMasterId() {
        return masterId;
    }

    public long getSlaveId() {
        return slaveId;
    }
}
