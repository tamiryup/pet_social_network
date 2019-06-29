package com.tamir.petsocialnetwork.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class TimelineFeedPostDTO extends UserFeedPostDTO {

    private String userProfileImageAddr;

    private String userName;


    public TimelineFeedPostDTO(long postId, String postImageAddr, String description,
                               String link, String website, String userProfileImageAddr, String userName) {
        super(postId, postImageAddr, description, link, website);
        this.userProfileImageAddr = userProfileImageAddr;
        this.userName = userName;
    }
}
