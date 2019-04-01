package com.tamir.petsocialnetwork.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class TimelineFeedPostDTO {

    private String userProfileImageAddr;

    private String userName;

    private String postImageAddr;

    private String description;

    private String link;
}
