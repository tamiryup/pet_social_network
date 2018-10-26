package com.tamir.petsocialnetwork.dto;

public class TimelineFeedPostDTO {

    private String userProfileImageAddr;

    private String userName;

    private String postImageAddr;

    private String description;

    public TimelineFeedPostDTO(){

    }

    public TimelineFeedPostDTO(String userProfileImageAddr, String userName, String postImageAddr, String description) {
        this.userProfileImageAddr = userProfileImageAddr;
        this.userName = userName;
        this.postImageAddr = postImageAddr;
        this.description = description;
    }

    public String getUserProfileImageAddr() {
        return userProfileImageAddr;
    }

    public void setUserProfileImageAddr(String userProfileImageAddr) {
        this.userProfileImageAddr = userProfileImageAddr;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPostImageAddr() {
        return postImageAddr;
    }

    public void setPostImageAddr(String postImageAddr) {
        this.postImageAddr = postImageAddr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
