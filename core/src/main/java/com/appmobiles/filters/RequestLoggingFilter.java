package com.appmobiles.filters;

import io.vertx.core.http.HttpServerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import static java.lang.String.format;

/**
 * Filter for logging requests.
 */
@Provider
public class RequestLoggingFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger( RequestLoggingFilter.class );

    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext context) {
        final String method = context.getMethod();
        final String path = info.getPath();
        final String address = request.remoteAddress().toString();
        log.info(format("Request %s %s from IP %s", method, path, address));
    }
}
