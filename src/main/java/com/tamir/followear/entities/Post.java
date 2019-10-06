package com.tamir.followear.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tamir.followear.enums.Category;
import com.tamir.followear.enums.Currency;
import com.tamir.followear.enums.ProductType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Parameter;

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
    @GenericGenerator(
            name = "post_gen",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "post_seq"),
                    @Parameter(name = "initial_value", value = "1"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    private long id;

    private long userId;

    private long storeId;

    private String imageAddr;

    @Column(columnDefinition = "varchar(1000)")
    private String link;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String price;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private Currency currency;

    private String designer;

    private String productId;

    private String thumbnail;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private ProductType productType;

    @CreationTimestamp
    private Date createDate;

    @UpdateTimestamp
    private Date updateDate;

    private long numViews;

    public Post(long userId, String imageAddr, String description) {
        this.userId = userId;
        this.imageAddr = imageAddr;
        this.description = description;
        this.numViews = 0;
    }

    public Post(long userId, long storeId, String imageAddr, String description, String link, String price,
                Currency currency, String designer, String productId, String thumbnail,
                Category category, ProductType productType){
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
    }

    public String getFormattedPrice() {
        return this.currency.getSign() + this.price;
    }

    @Override
    public boolean equals(Object o) {

        if(this.getClass() != o.getClass()) {
            return false;
        }

        Post p = (Post) o;
        return (this.id == p.getId());
    }
}
