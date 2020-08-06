package com.tamir.followear.dto;

import com.tamir.followear.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class AuthResultDTO {

    private long userId;
    private String userName;

    public AuthResultDTO(User user) {
        this.userId = user.getId();
        this.userName = user.getUsername();
    }
}
