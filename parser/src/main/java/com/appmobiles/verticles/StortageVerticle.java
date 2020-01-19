package com.appmobiles.verticles;

import com.appmobiles.cache.StorageItem;
import com.appmobiles.verticles.abstracts.AbstractTaskVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.appmobiles.Addresses.*;
import static com.appmobiles.Constants.DELIVERY_OPTIONS;
import static com.appmobiles.Constants.EMPTY_JSON_ARRAY;
import static com.appmobiles.Types.STORAGE_TASK_TYPE;
import static com.appmobiles.Utils.messageHandler;

public class StortageVerticle extends AbstractTaskVerticle {
    private static final Logger log = LoggerFactory.getLogger( StortageVerticle.class );

    @Override
    protected void initSuccess(Promise<Void> startPromise) {
        requestNextTask(STORAGE_TASK_TYPE);
        startPromise.complete();
    }

    @Override
    protected void startTask(JsonObject json) {
        log.info(" $ startTask : " + json.encodePrettily());

        if (!json.isEmpty() && json.getJsonArray("links", EMPTY_JSON_ARRAY).size() > 0) {
            final StorageItem storageItem = StorageItem.of(json);
            compare(storageItem)
                    .compose(s -> uploadMany(storageItem))
                    .onComplete(ar -> {
                        if (ar.succeeded()) {
                            eventBus().send(CONTENT_READY, storageItem.toJson().put("links", ar.result()),
                                    DELIVERY_OPTIONS);
                        } else {
                            ar.cause().printStackTrace();
                        }
                        requestNextTask(STORAGE_TASK_TYPE);
                    });
        } else{
            requestNextTask(STORAGE_TASK_TYPE);
        }
    }

    private Future<List<String>> uploadMany(StorageItem storageItem) {
        return new UploadMany(storageItem.getLinks()).getFuture();
    }

    private Future<String> compare(StorageItem storageItem) {
        final Promise<String> promise = Promise.promise();
        eventBus().request(CHECK_TEXT_EXISTS, storageItem.getText(), DELIVERY_OPTIONS, messageHandler(promise));
        return promise.future();
    }

    private Future<String> upload(String url) {
        final Promise<String> promise = Promise.promise();
        eventBus().request(UPLOAD_MEDIA_CONTENT, url, messageHandler(promise));
        return promise.future();
    }

    private class UploadMany {

        private final Promise<List<String>> promise;
        private final Iterator<String> iterator;
        private List<String> results = new ArrayList<>();

        public UploadMany(List<String> links) {
            promise = Promise.promise();
            iterator = links.iterator();
            next();
        }

        private void next() {
            if (iterator.hasNext()) {
                upload(iterator.next()).onComplete(ar -> {
                    if (ar.succeeded()) {
                        results.add(ar.result());
                    }
                    next();
                });
            } else {
                promise.complete(results);
            }
        }

        public Future<List<String>> getFuture() {
            return promise.future();
        }
    }
}
