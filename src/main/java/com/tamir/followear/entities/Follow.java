package com.tamir.followear.entities;

import com.tamir.followear.jpaKeys.FollowKey;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "follows")
@IdClass(FollowKey.class)
public class Follow {

    @Id
    private long masterId;

    @Id
    private long slaveId;

    @CreationTimestamp
    private Date createDate;

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
