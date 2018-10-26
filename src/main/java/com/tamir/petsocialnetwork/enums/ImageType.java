package com.tamir.petsocialnetwork.enums;

public enum ImageType {

    PostImage("images/post-images"),
    ProfileImage("images/profile-images");

    private String path;

    private ImageType(String path){
        this.path = path;
    }

    public String getPath(){
        return this.path;
    }
}
