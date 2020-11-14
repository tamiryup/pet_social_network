package com.tamir.followear.enums;

public enum ImageType {

    PostImage("images/post-images"),
    ProfileImage("images/profile-images"),
    SelfImage("images/self-images");

    private String path;

    private ImageType(String path){
        this.path = path;
    }

    public String getPath(){
        return this.path;
    }
}
