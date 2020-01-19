package com.appmobiles.exceptions.handlers;

import com.appmobiles.exceptions.NotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.status;

/**
 * Handler for not found exception. Map not found exception to user friendly format.
 */
@Provider
public class NotFoundExceptionHandler implements ExceptionMapper<NotFoundException> {
    @Override
    public Response toResponse(NotFoundException exception) {
        return status(Response.Status.NOT_FOUND).entity(exception.getMessage()).build();
    }
}