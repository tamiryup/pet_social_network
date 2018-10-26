package com.tamir.petsocialnetwork.dto;


public class UserFeedPostDTO {

    private String postImageAddr;

    private String description;

    public UserFeedPostDTO(String postImageAddr, String description) {
        this.postImageAddr = postImageAddr;
        this.description = description;
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
