package com.tamir.petsocialnetwork.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name="stores")
@NoArgsConstructor
@Getter
@ToString
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_gen")
    @SequenceGenerator(name = "user_gen", sequenceName = "user_seq")
    private long id;

    private String name;

    private String logoAddr;

    private String website;

    public Store(String name, String logoAddr, String website) {
        this.name = name;
        this.logoAddr = logoAddr;
        this.website = website;
    }
}
