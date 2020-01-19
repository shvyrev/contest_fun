package com.appmobiles.exceptions;

/**
 * Custom not found exception.
 */
public class NotFoundException extends RuntimeException {

    public static final String MESSAGE_TEMPLATE = "%s : %s was not found";

    public NotFoundException(Class aClass, String id) {
        super(String.format(MESSAGE_TEMPLATE, aClass.getSimpleName(), id));
    }
}
