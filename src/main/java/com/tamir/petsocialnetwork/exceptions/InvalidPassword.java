package com.tamir.petsocialnetwork.exceptions;

public class InvalidPassword extends RuntimeException {

    public InvalidPassword(String message){
        super(message);
    }

    public InvalidPassword() {
        this("password doesn't match criteria");
    }

}
