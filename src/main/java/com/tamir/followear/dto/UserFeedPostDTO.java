package com.tamir.followear.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class UserFeedPostDTO {

    private long postId;

    private long userId;

    private String postImageAddr;

    private String description;

    private String link;

    private String website;

    public UserFeedPostDTO(long postId, long userId, String postImageAddr, String description, String link, String website) {
        this.postId = postId;
        this.userId = userId;
        this.postImageAddr = postImageAddr;
        this.description = description;
        this.link = link;
        this.website = website;
    }
}
