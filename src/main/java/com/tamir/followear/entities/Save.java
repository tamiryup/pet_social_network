package com.tamir.followear.entities;

import com.tamir.followear.jpaKeys.SaveKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "saves")
@IdClass(SaveKey.class)
@Getter
@NoArgsConstructor
public class Save {

    @Id
    private long userId;

    @Id
    private long postId;

    @CreationTimestamp
    private Date date;

    public Save(long userId, long postId) {
        this.userId = userId;
        this.postId = postId;
    }

}
