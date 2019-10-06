package com.tamir.followear.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class FeedResultDTO {

    private List<? extends FeedPostDTO> feedPosts;

    private int newOffset; // the offset to send with the next request (-1 is no offset)
}
