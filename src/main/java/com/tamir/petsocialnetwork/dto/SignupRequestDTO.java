package com.tamir.petsocialnetwork.dto;

import java.util.Date;

public class SignupRequestDTO {

    private String email;
    private String password;
    private String userName;
    private String fullName;

    private Date birthDate;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    public String getFullName() {
        return fullName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setBirthDate(Date birthDate){
        this.birthDate = birthDate;
    }


}
