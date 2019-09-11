package com.tamir.followear.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="stores")
@NoArgsConstructor
@Getter
@ToString
public class Store {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    private String logoAddr;

    private String website;

    @CreationTimestamp
    private Date createDate;

    @UpdateTimestamp
    private Date updateDate;

    public Store(String name, String logoAddr, String website) {
        this.name = name;
        this.logoAddr = logoAddr;
        this.website = website;
    }
}
