package com.tamir.petsocialnetwork.entities;

import com.tamir.petsocialnetwork.CommonBeanConfig;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_gen")
    @SequenceGenerator(name = "user_gen", sequenceName = "user_seq")
    private long id;

    private String username;
    private String fullName;
    private String profileImageAddr;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "varchar(320)" )
    private String email;

    private String hashedPassword;
    private Date birthDate;

    /**
     * default constructor for hibernate
     */
    public User() {

    }

    public User(String email, String hashedPassword, String username, String fullName, Date birthDate) {
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.username = username;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.profileImageAddr = CommonBeanConfig.getDefaultProfileImageAddr();
        this.description = CommonBeanConfig.getDefaultUserDescription();
    }

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getProfileImageAddr() {
        return profileImageAddr;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setProfileImageAddr(String addr){
        this.profileImageAddr = addr;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void nullPassword(){
        this.hashedPassword=null;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", profileImageAddr='" + profileImageAddr + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
