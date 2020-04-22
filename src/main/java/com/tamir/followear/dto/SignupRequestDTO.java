package com.tamir.followear.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@NoArgsConstructor
@Getter @Setter
@ToString
public class SignupRequestDTO {

    private String email;
    private String password;
    private String userName;
    private String fullName;

    private Date birthDate;

}
