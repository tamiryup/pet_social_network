package com.tamir.petsocialnetwork.exceptions;

/**
 * this exception means that there's a problem with the server code
 * regarding a url (can be malfunctioning url or lack of connection)
 *
 * should display some kind of error message to client ("we seem to experience some issues")
 */
public class UrlException extends RuntimeException {

    public UrlException(String message) {
        super(message);
    }

}
