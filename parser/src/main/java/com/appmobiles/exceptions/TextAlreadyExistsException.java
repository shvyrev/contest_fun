package com.appmobiles.exceptions;

public class TextAlreadyExistsException extends RuntimeException {
    public TextAlreadyExistsException(String message) {
        super("Text : \n\t'" + message + "'\n already exists in storage.");
    }
}
