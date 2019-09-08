package com.tamir.followear.exceptions;

/**
 * thrown when the caller is not authorized to make a request (jwt token invalid or missing)
 */
public class NoAuthException extends RuntimeException {

    public NoAuthException() {
        this("The tokens didn't pass");
    }

    public NoAuthException(String message) {
        super(message);
    }
}
