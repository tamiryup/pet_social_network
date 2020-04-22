package com.tamir.followear.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter @Setter
@ToString
public class ChangePasswordDTO {

    private String oldPassword;
    private String newPassword;

}
