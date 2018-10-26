package com.tamir.petsocialnetwork.exceptions;

public class UserCollisionException extends RuntimeException {

    public UserCollisionException(String message){
        super(message);
    }
}
