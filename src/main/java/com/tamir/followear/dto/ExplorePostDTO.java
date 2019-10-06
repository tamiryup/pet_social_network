package com.tamir.followear.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class ExplorePostDTO extends FeedPostDTO {

    public ExplorePostDTO(long postId, long userId, String postImageAddr, String description) {
        super(postId, userId, postImageAddr, description);
    }
}
