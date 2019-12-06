package com.tamir.followear.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class TimelineFeedPostDTO extends UserFeedPostDTO {

    private String userProfileImageAddr;

    private String userName;

    public TimelineFeedPostDTO(long postId, long userId, String postImageAddr, String description, String link,
                               String price, String website, String userProfileImageAddr, String userName,
                               String thumbnail) {
        super(postId, userId, postImageAddr, description, link, price, website, thumbnail);
        this.userProfileImageAddr = userProfileImageAddr;
        this.userName = userName;
    }
}
