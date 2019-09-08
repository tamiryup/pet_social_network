package com.tamir.followear.entities;

import com.tamir.followear.CommonBeanConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")})
@NoArgsConstructor
@Getter
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_gen")
    @SequenceGenerator(name = "user_gen", sequenceName = "user_seq")
    private long id;

    private String username;
    private String fullName;
    @Setter
    private String profileImageAddr;

    @Column(columnDefinition = "TEXT")
    @Setter
    private String description;

    @Column(columnDefinition = "varchar(320)")
    private String email;

    private Date birthDate;

    private Date registerDate;

    public User(String email, String username, String fullName, Date birthDate) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.profileImageAddr = CommonBeanConfig.getDefaultProfileImageAddr();
        this.description = CommonBeanConfig.getDefaultUserDescription();
        this.registerDate = new Date();
    }

}
