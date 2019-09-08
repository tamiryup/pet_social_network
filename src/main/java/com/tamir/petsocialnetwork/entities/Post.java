package com.tamir.petsocialnetwork.entities;

import com.tamir.petsocialnetwork.enums.Currency;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "posts")
@Getter
@ToString
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_gen")
    @SequenceGenerator(name = "post_gen", sequenceName = "post_seq")
    private long id;

    private long userId;

    private long storeId;

    private String imageAddr;

    @Column(columnDefinition = "varchar(1000)")
    private String link;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String price;

    private Currency currency;

    private String designer;

    private String productId;

    private String thumbnail;

    private String category;

    private String productType;

    private Date uploadDate;

    private long numViews;

    public Post(long userId, String imageAddr, String description) {
        this.userId = userId;
        this.imageAddr = imageAddr;
        this.description = description;
        this.numViews = 0;
    }

    public Post(long userId, long storeId, String imageAddr, String description, String link, String price,
                Currency currency, String designer, String productId, String thumbnail,
                String category, String productType){
        this(userId, imageAddr, description);
        this.storeId = storeId;
        this.link = link;
        this.price = price;
        this.currency = currency;
        this.designer = designer;
        this.productId = productId;
        this.thumbnail = thumbnail;
        this.category = category;
        this.productType = productType;
        this.uploadDate = new Date();
    }
}
