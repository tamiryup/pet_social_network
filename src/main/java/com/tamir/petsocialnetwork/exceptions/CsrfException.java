package com.tamir.petsocialnetwork.exceptions;

public class CsrfException extends RuntimeException {

    public CsrfException(String message) {
        super(message);
    }
}
