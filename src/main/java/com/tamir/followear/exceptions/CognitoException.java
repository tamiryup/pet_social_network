package com.tamir.followear.exceptions;

public class CognitoException extends RuntimeException {

    public CognitoException() {

    }

    public CognitoException(String message) {
        super(message);
    }
}
