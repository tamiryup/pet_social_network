package com.tamir.followear.entities;

import com.tamir.followear.jpaKeys.LikeKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "likes")
@IdClass(LikeKey.class)
@Getter
@NoArgsConstructor
public class Like {

    @Id
    private long userId;

    @Id
    private long postId;

    @CreationTimestamp
    private Date date;

    public Like(long userId, long postId) {
        this.userId = userId;
        this.postId = postId;
    }
}
