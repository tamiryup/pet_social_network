package com.tamir.followear.exceptions;

public class UserCollisionException extends RuntimeException {

    public UserCollisionException(String message){
        super(message);
    }
}
