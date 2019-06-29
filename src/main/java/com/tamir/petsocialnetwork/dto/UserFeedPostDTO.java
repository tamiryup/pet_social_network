package com.tamir.petsocialnetwork.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class UserFeedPostDTO {

    private long postId;

    private String postImageAddr;

    private String description;

    private String link;

    private String website;

    public UserFeedPostDTO(long postId, String postImageAddr, String description, String link, String website) {
        this.postId = postId;
        this.postImageAddr = postImageAddr;
        this.description = description;
        this.link = link;
        this.website = website;
    }
}
