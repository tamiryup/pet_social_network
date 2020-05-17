package com.tamir.followear.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class UserFeedPostDTO extends FeedPostDTO {

    private String link;

    private String price; //already formatted ('$33')

    private String salePrice; //empty string if item isn't on sale (already formatted)

    private String website; // already formatted ('asos.com')

    private String thumbnail;

    public UserFeedPostDTO(long postId, long userId, String postImageAddr, String description,
                           String link, String price, String salePrice, String website, String thumbnail) {
        super(postId, userId, postImageAddr, description);
        this.price = price;
        this.salePrice = salePrice;
        this.link = link;
        this.website = website;
        this.thumbnail = thumbnail;
    }
}
