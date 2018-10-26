package com.tamir.petsocialnetwork.entities;

import javax.persistence.*;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_gen")
    @SequenceGenerator(name = "post_gen", sequenceName = "post_seq")
    private long id;

    private long userId;
    private String imageAddr;

    @Column(columnDefinition = "TEXT")
    private String description;

    public Post() {

    }

    public Post(long userId, String imageAddr, String description) {
        this.userId = userId;
        this.imageAddr = imageAddr;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public long getUserId() {
        return userId;
    }

    public String getImageAddr() {
        return imageAddr;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", userId=" + userId +
                ", imageAddr='" + imageAddr + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
