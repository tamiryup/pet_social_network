package com.tamir.followear.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class UserFeedPostDTO extends FeedPostDTO {

    private String link;

    private String price; //already formatted ('$33')

    private String website; // already formatted ('asos.com')

    public UserFeedPostDTO(long postId, long userId, String postImageAddr,
                           String description, String link, String price, String website) {
        super(postId, userId, postImageAddr, description);
        this.price = price;
        this.link = link;
        this.website = website;
    }
}
