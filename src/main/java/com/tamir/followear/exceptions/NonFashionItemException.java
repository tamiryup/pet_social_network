package com.tamir.followear.exceptions;

public class NonFashionItemException extends RuntimeException {

    public NonFashionItemException(String message) {
        super(message);
    }

    public NonFashionItemException() { this("Sorry, this product isn't a fashion item"); }

}
