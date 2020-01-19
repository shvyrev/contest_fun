package com.appmobiles.verticles;

import com.appmobiles.Utils;
import com.appmobiles.exceptions.TextAlreadyExistsException;
import com.appmobiles.verticles.abstracts.AbstractWorkerVerticle;
import com.google.common.net.HostAndPort;
import io.rebloom.client.Client;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.validation.constraints.NotNull;

import static com.appmobiles.Addresses.*;
import static com.appmobiles.Constants.*;
import static com.appmobiles.Utils.*;

public class ComparingVerticle extends AbstractWorkerVerticle {
    private HostAndPort hostAndPort;

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void initSuccess(Promise<Void> startPromise) {
        hostAndPort = HostAndPort.fromString(config().getString("redisUrl", EMPTY_STRING));

        addListeners();

        startPromise.complete();
    }

    private void addListeners() {
        eventBus().<String>consumer(CHECK_TEXT_EXISTS,message -> add(message.body()).onComplete(messageHandler(message)));
    }

    /**
     * Add text to Bloom filter. Return failed future if text already exist.
     *
     * @param text text for comparing, not null
     *
     * @return {@link Future} result of an action. Empty string if succeed, failed if getting exception or already exists.
     */
    @SuppressWarnings("UnstableApiUsage")
    public Future<String> add(@NotNull String text){

        final Promise<String> result = Promise.promise();
        final Client client = new Client(hostAndPort.getHost(), hostAndPort.getPort());

        if (client.exists(BLOOM_FILTER_NAME, text)) {
            result.fail(new TextAlreadyExistsException(text));
        } else {
            client.add(BLOOM_FILTER_NAME, text);
            result.complete(EMPTY_STRING);
        }

        client.close();
        return result.future();
    }

}
