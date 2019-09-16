package com.tamir.followear.entities;

import com.tamir.followear.CommonBeanConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Parameter;

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
    @GenericGenerator(
            name = "user_gen",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "user_seq"),
                    @Parameter(name = "initial_value", value = "1"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
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

    @CreationTimestamp
    private Date createDate; //LocalTime in localhost, UTC in beanstalk env

    @UpdateTimestamp
    private Date updateDate;

    public User(String email, String username, String fullName, Date birthDate) {
        this.email = email;
        this.username = username;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.profileImageAddr = CommonBeanConfig.getDefaultProfileImageAddr();
        this.description = CommonBeanConfig.getDefaultUserDescription();
    }

}
