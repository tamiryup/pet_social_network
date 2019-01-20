package com.tamir.petsocialnetwork.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@Getter @Setter
public class SignupRequestDTO {

    private String email;
    private String password;
    private String userName;
    private String fullName;

    private Date birthDate;

}
