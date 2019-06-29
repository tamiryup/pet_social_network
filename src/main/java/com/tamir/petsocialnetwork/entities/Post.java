package com.tamir.petsocialnetwork.entities;

import lombok.Getter;
import lombok.Setter;

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

    @Getter @Setter
    @Column(columnDefinition = "varchar(max)")
    private String link;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Getter @Setter
    private String price;

    @Getter @Setter
    private String website;

    @Getter @Setter
    private String designer;

    @Getter @Setter
    private String productId;

    public Post() {

    }

    public Post(long userId, String imageAddr, String description) {
        this.userId = userId;
        this.imageAddr = imageAddr;
        this.description = description;
    }

    public Post(long userId, String imageAddr, String description, String link, String price,
                String website, String designer, String productId){
        this(userId, imageAddr, description);
        this.link = link;
        this.price = price;
        this.website = website;
        this.designer = designer;
        this.productId = productId;
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

    public String getLink() {
        return link;
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
