package com.tamir.followear.exceptions;

public class CsrfException extends RuntimeException {

    public CsrfException(String message) {
        super(message);
    }
}
