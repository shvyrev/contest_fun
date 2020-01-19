package com.appmobiles.performance;

import io.quarkus.runtime.StartupEvent;

import javax.enterprise.event.Observes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Special resource for performance tests. Implements a test to determine the time of the final loading of
 * the application. Until the moment when the application is fully initialized and will be able to accept requests.
 */
@Path("/performance")
public class PerformanceResource {

    private long firstTimestamp;
    private long finalTimestamp;

    @GET
    @Path("/startup")
    public String startup(@QueryParam("name") String name) {
        finalTimestamp = System.currentTimeMillis();
        long totalTime = finalTimestamp - firstTimestamp;
        System.out.println("first timestamp : " + getDate(firstTimestamp) + "final timestamp : " + getDate(finalTimestamp)
                + "\n" + "startup time : " + totalTime + " milliseconds"
        );
        return "";
    }

    private String getDate(long firstTimestamp) {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(firstTimestamp));
    }

    void onStart(@Observes StartupEvent startup) {
        firstTimestamp = System.currentTimeMillis();
    }
}
