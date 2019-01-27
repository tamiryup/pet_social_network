package com.tamir.petsocialnetwork.exceptions;

public class InvalidToken extends RuntimeException {

    public InvalidToken(String message){
        super(message);
    }

    public InvalidToken() {

    }

}
