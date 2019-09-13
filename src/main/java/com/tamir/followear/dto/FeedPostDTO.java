package com.tamir.followear.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public abstract class FeedPostDTO {

    private long postId;

    private long userId;

    private String postImageAddr;

    private String description;

    public FeedPostDTO(long postId, long userId, String postImageAddr, String description) {
        this.postId = postId;
        this.userId = userId;
        this.postImageAddr = postImageAddr;
        this.description = description;
    }
}
