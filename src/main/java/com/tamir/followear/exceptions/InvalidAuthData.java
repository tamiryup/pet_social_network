package com.tamir.followear.exceptions;

public class InvalidAuthData extends RuntimeException {

    public InvalidAuthData(String message){
        super(message);
    }
}
