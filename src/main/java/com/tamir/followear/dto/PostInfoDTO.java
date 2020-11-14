package com.tamir.followear.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class PostInfoDTO {

    private long postId;
    private long userId;
    private long storeId;
    private String userProfileImageAddr;
    private String userName;
    private String postImageAddr;
    private String description;
    private String price; //already formatted
    private String salePrice; //already formatted
    private String storeLogoAddr;
    private String storeName;
    private String website; //already formatted
    private String thumbnailAddr;
    private String selfThumbAddr;
    private String link;
    private long numViews;
    private long numLikes;
    private Date createDate;

}
