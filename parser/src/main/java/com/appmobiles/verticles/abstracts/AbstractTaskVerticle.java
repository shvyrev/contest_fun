package com.appmobiles.verticles.abstracts;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import static com.appmobiles.Constants.*;

/**
 * An abstract base class that you can extend to write task processing Verticles.
 * <p>
 * In the simplest case, just override {@link #requestNextTask(String)} and {@link #startTask(JsonObject)} methods.
 * </p>
 *
 * @see com.appmobiles.verticles.ParserVerticle
 * @see com.appmobiles.verticles.StortageVerticle
 */
public abstract class AbstractTaskVerticle extends AbstractWorkerVerticle {

    /**
     * Requests next task to process.
     *  If at the moment the task is not available or an error occurred upon request,
     *  it repeats the request through a timeout.
     * @see com.appmobiles.Constants#TIMEOUT
     *  If the task is received successfully, it immediately proceeds to its processing
     *
     * @param type {@link com.appmobiles.Types} job type, not null
     */
    protected void requestNextTask(@NotNull String type) {

        eventBus().<JsonObject>request(type, EMPTY_STRING, DELIVERY_OPTIONS, response -> {
            if (response.failed() || response.result().body() == null) {
                getVertx().setTimer(TIMEOUT, l -> requestNextTask(type));
            } else {
                startTask(response.result().body());
            }
        });
    }

    /**
     * Abstracts the handler of the received task.
     *
     * @param body {@link JsonObject} received task in a format specialized for a specific implementation,
     *                               can be empty, not null
     */
    protected abstract void startTask(@Nullable JsonObject body);

}
