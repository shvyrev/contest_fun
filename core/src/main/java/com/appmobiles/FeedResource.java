package com.appmobiles;

import com.appmobiles.data.CacheRepository;
import com.appmobiles.data.model.Feed;
import com.appmobiles.exceptions.NotFoundException;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.Response.ok;

@Path("/feed")
@Produces(MediaType.APPLICATION_JSON)
public class FeedResource {

    @Inject
    CacheRepository cache;

    @Context
    ResourceContext resourceContext;

    @Context
    UriInfo uriInfo;

    @GET
    @Timed(name = "feedRequestTimer", description = "Feed list request timeout.", unit = MetricUnits.MILLISECONDS)
    @Operation(summary = "Feed list", description = "Feed list")
    @APIResponse(responseCode = "200", name = "Feed list", content = @Content(mediaType = "application/json",schema = @Schema(type = SchemaType.ARRAY,implementation = Feed.class)))
    @Retry(maxRetries = 4, delay = 100)
    public Response getAllFeeds(
            @QueryParam("page") @Parameter(description = "Page number",schema = @Schema(implementation = Integer.class)) int page,
            @QueryParam("size") @Parameter(description = "Max number of items per page", schema = @Schema(implementation = Integer.class)) int size){
        return ok(cache.list(page, size)).build();
    }

    @GET
    @Path("{id}")
    @Timed(name = "feedByIdRequestTimer", description = "Mem info request timeout.", unit = MetricUnits.MILLISECONDS)
    @Operation(summary = "Content info", description = "Content info")
    @APIResponse(responseCode = "200", name = "Content ID", content = @Content(mediaType = "application/json", schema = @Schema(type = SchemaType.OBJECT, implementation = Content.class)))
    @APIResponse(responseCode = "404", description = "Exception. Not found feed with id.", content = @org.eclipse.microprofile.openapi.annotations.media.Content(mediaType = "text/plain",schema = @Schema(type = SchemaType.STRING, implementation = String.class)))
    @Retry(maxRetries = 4, delay = 100)
    public Response getContentById(
            @PathParam("id") @Parameter(description = "Feed id", schema = @Schema(implementation = String.class)) final String id){
        final com.appmobiles.data.model.Content content = com.appmobiles.data.model.Content.findById(id);
        if (content == null) {
            throw new NotFoundException(com.appmobiles.data.model.Content.class, id);
        }
        return ok(content).build();
    }

}