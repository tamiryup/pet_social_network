package com.tamir.petsocialnetwork.dto;

public class RegisterResponseDTO {

    private long userId;
    private String userName;

    public RegisterResponseDTO(){

    }

    public RegisterResponseDTO(long userId, String userName){
        this.userId = userId;
        this.userName = userName;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
