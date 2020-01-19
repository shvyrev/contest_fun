package com.appmobiles.exceptions.handlers;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.status;

/**
 * Handler for application exceptions. Map application exception to user friendly format.
 */
@Provider
public class DefaultExceptionHandler implements ExceptionMapper<Exception> {
    @Override
    public Response toResponse(Exception exception) {
        return status(BAD_REQUEST).entity(exception.getMessage()).build();
    }
}
