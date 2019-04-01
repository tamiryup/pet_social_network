package com.tamir.petsocialnetwork.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter @Setter
public class UserFeedPostDTO {

    private String postImageAddr;

    private String description;

    private String link;
}
