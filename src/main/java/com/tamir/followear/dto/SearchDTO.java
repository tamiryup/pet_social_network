package com.tamir.followear.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class SearchDTO {

    private long id;
    private String username;
    private String fullName;
    private String userProfileImageAddr;

}
