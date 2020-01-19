package com.appmobiles.verticles.abstracts;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base class that you can extend to write Verticles for workers that process HTTP requests.
 * <p>
 * In the simplest case, just override {@link #initSuccess(Promise)} for instantiation your Verticle.
 * </p>
 *
 */
public abstract class AbstractWorkerVerticle extends AbstractVerticle {

    /**
     * This is called by Vert.x when the verticle instance is deployed. Don't call it yourself.
     *
     * @param startPromise {@inheritDoc} promise which should be called when verticle start-up is complete.
     *
     * @throws Exception if an error occurred while instantiating Verticle deploying
     */
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        initSuccess(startPromise);
    }

    /**
     * Get the event bus object. There is a single instance of EventBus per Vertx instance.
     *
     * @return {@link EventBus} the event bus object
     */
    protected EventBus eventBus(){
        return getVertx().eventBus();
    }

    /**
     * The method initializes the Verticle when deploying.
     *
     * @param startPromise a future which should be called when verticle start-up is complete.
     */
    protected abstract void initSuccess(Promise<Void> startPromise);
}
