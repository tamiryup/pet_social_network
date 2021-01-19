package com.tamir.followear.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class ExplorePostDTO extends FeedPostDTO {

    private String thumbnail;

    private String selfThumb;

    public ExplorePostDTO(long postId, long userId, String postImageAddr, String description, String thumbnail,
                          String selfThumb) {
        super(postId, userId, postImageAddr, description);
        this.thumbnail = thumbnail;
        this.selfThumb = selfThumb;
    }
}
