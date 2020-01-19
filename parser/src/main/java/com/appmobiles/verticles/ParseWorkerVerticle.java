package com.appmobiles.verticles;

import com.appmobiles.verticles.abstracts.AbstractHttpVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.stream.Stream;

import static com.appmobiles.Addresses.*;
import static com.appmobiles.Constants.*;
import static com.appmobiles.Utils.*;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class ParseWorkerVerticle extends AbstractHttpVerticle {

    private String imageSelector;
    private String videoSelector;
    private String descriptionSelector;
    private String titleSelector;
    private String upsSelector;
    private String contentPageSelector;
    private String contentPageUrlPattern;
    private String seedSelector;

    @Override
    protected void initSuccess(Promise<Void> startPromise) {
        imageSelector = config().getString("imageSelector", EMPTY_STRING);
        videoSelector = config().getString("videoSelector", EMPTY_STRING);
        descriptionSelector = config().getString("descriptionSelector", EMPTY_STRING);
        titleSelector = config().getString("titleSelector", EMPTY_STRING);
        upsSelector = config().getString("upsSelector", EMPTY_STRING);
        contentPageSelector = config().getString("contentPageSelector", EMPTY_STRING);
        contentPageUrlPattern = config().getString("contentPageUrlPattern", EMPTY_STRING);
        seedSelector = config().getString("seedSelector", EMPTY_STRING);

        addListeners();
        startPromise.complete();
    }

    private void addListeners() {

        eventBus().<String>consumer(GET_SEEDS, message ->
                getHtml(message.body()).compose(this::parseSeeds).onComplete(messageHandler(message)));

        eventBus().<String>consumer(GET_CONTENT_PAGE_URLS, message ->
                getHtml(message.body()).compose(this::parseContentPageUrls).onComplete(messageHandler(message)));

        eventBus().<String>consumer(GET_CONTENT_PAGE, message ->
                getHtml(message.body()).compose(this::parseContentPage).onComplete(messageHandler(message)));
    }

    private Future<JsonArray> parseSeeds(String s) {
        final Promise<JsonArray> promise = Promise.promise();
        promise.tryComplete(new JsonArray(
                Jsoup.parse(s).select(seedSelector).stream().map(attrMapper(HREF)).filter(urlPredicate())
                        .distinct().collect(toList()))
        );
        return promise.future();
    }

    private Future<JsonArray> parseContentPageUrls(String html) {
        final Promise<JsonArray> promise = Promise.promise();
        promise.tryComplete(
                new JsonArray(
                        Jsoup.parse(html).select(contentPageSelector).stream().map(attrMapper(HREF))
                                .filter(not(urlPredicate())).map(s -> format(contentPageUrlPattern, s))
                                .distinct().collect(toList()))
        );
        return promise.future();
    }

    private Future<JsonObject> parseContentPage(String html) {

        final Promise<JsonObject> result = Promise.promise();
        final Document page = Jsoup.parse(html);
        final List<String> links = Stream.concat(
                page.select(imageSelector).stream(),page.select(videoSelector).stream()).map(attrMapper(SRC))
                .collect(toList());
        final String description = page.select(descriptionSelector).text();
        final String title = page.select(titleSelector).text();
        final Elements upsElements = page.select(upsSelector);
        int ups = upsElements != null && upsElements.size() > 0 ? parseInt(upsElements.first().text()) : 0;
        result.complete(
                new JsonObject()
                        .put("links", new JsonArray(links))
                        .put("title", title)
                        .put("text", description)
                        .put("language", EMPTY_STRING)
                        .put("ups", ups)
        );
        return result.future();
    }

}
