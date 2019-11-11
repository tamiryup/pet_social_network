package com.tamir.followear.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@AllArgsConstructor
@Getter @Setter
public class UserInfoDTO {

    private long id;
    private String username;
    private String fullName;
    private String profileImageAddr;
    private String description;
    private String email;
    private Date birthDate;

}
