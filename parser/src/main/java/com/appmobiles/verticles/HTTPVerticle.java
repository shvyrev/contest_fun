package com.appmobiles.verticles;

import com.appmobiles.verticles.abstracts.AbstractHttpVerticle;
import io.vertx.core.Promise;

import java.nio.file.Paths;

import static com.appmobiles.Addresses.UPLOAD_MEDIA_CONTENT;
import static com.appmobiles.Utils.messageHandler;

public class HTTPVerticle extends AbstractHttpVerticle {


    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void initSuccess(Promise<Void> startPromise) {
        addListeners();
        startPromise.complete();
    }
    private void addListeners() {
        eventBus().<String>consumer(UPLOAD_MEDIA_CONTENT, message -> {
            final String url = message.body();
            download(url).compose(s -> upload(s, Paths.get(url).getFileName().toString()))
                    .onComplete(messageHandler(message));
        });
    }


}
