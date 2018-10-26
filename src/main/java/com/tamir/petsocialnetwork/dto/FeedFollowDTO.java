package com.tamir.petsocialnetwork.dto;

public class FeedFollowDTO {

    private String profileImageAddr;

    private String username;

    public FeedFollowDTO() {

    }

    public FeedFollowDTO(String profileImageAddr, String username) {
        this.profileImageAddr = profileImageAddr;
        this.username = username;
    }

    public String getProfileImageAddr() {
        return profileImageAddr;
    }

    public void setProfileImageAddr(String profileImageAddr) {
        this.profileImageAddr = profileImageAddr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
