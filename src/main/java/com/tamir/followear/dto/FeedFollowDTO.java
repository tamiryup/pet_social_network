package com.tamir.followear.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class FeedFollowDTO {

    private long id;
    private String username;
    private String fullName;
    private String profileImageAddr;

}
