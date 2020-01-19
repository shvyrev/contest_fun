package com.appmobiles;

import com.appmobiles.cache.CacheRepository;
import com.appmobiles.cache.Content;
import com.appmobiles.cache.Feed;
import com.appmobiles.cache.StorageItem;
import com.appmobiles.verticles.*;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.vertx.axle.core.Vertx;
import io.vertx.axle.core.eventbus.EventBus;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static com.appmobiles.Addresses.GET_SEEDS;
import static com.appmobiles.Constants.*;
import static com.appmobiles.Types.CONTENT_PAGE_URL;
import static com.appmobiles.Types.SEED;
import static java.lang.Integer.max;
import static java.lang.String.format;
import static java.util.stream.Collectors.toMap;

/**
 * This class deploy instances for parsing pages and saving media content,
 * workers for processing tasks, receiving events containing content for saving.
 */
public class App {

    private static final Logger log = LoggerFactory.getLogger( App.class );

    @Inject
    Vertx vertx;

    @Inject
    EventBus bus;

    @Inject
    Config config;

    @Inject
    CacheRepository cache;
    private boolean renewing;

    /**
     * Event handler for request with address {@link Addresses#GET_PARSER_TASK}.
     * Returns the first available parsing task from the cache.
     *
     * @param value unused parameter required for message processing, can be null or empty
     *
     * @return {@link JsonObject}
     * @see ParserVerticle#startTask(JsonObject)
     */
    @Counted(name = "parserTaskRequestCounter", description = "Parser task request counter.")
    @Timed(name = "parserTaskRequestTimeout", description = "Parser task request timeout.", unit = MetricUnits.MILLISECONDS)
    @Metered(name = "parserTaskRequestMeter", description = "Parser task request frequency.")
    @ConsumeEvent("get-parser-task")
    public JsonObject onGetParserTask(@Nullable String value){
        final JsonObject task = cache.getParserTask();
        return !task.isEmpty() ? task : renew();
    }


    /**
     * Event handler for request with address {@link Addresses#GET_STORAGE_TASK}.
     * Returns the first available storage task from the cache.
     *
     *
     * @param value unused parameter required for message processing, can be null or empty
     *
     * @return {@link JsonObject}
     * @see StortageVerticle#startTask(JsonObject)
     * @see StorageItem#toJson()
     */
    @Counted(name = "storageTaskRequestCounter", description = "Storage task request counter.")
    @Timed(name = "storageTaskRequestTimeout", description = "Storage task request timeout.", unit = MetricUnits.MILLISECONDS)
    @Metered(name = "storageTaskRequestMeter", description = "Storage task request frequency.")
    @ConsumeEvent("get-storage-task")
    public JsonObject onGetStorageTask(@Nullable String value){
        return cache.getStorageTask();
    }

    /**
     * Event handler for request with address {@link Addresses#CONTENT_PAGE_URLS}.
     * Saves links for content pages.
     * In the future, these links will be used for page passing in order to obtain meta data and links to media content.
     *
     * @param value {@link JsonArray} list with links, not null
     * @see ParserVerticle#startTask(JsonObject)
     */
    @Counted(name = "contentPageUrlsRequestCounter", description = "Content page urls request counter.")
    @Timed(name = "contentPageUrlsRequestTimeout", description = "Content page urls timeout.", unit = MetricUnits.MILLISECONDS)
    @Metered(name = "contentPageUrlsRequestMeter", description = "Content page urls request frequency.")
    @ConsumeEvent("content-page-urls")
    public void onGetContentPageUrls(@NotNull JsonArray value){
        cache.addParserTasks(value.stream().map(Object::toString).collect(toMap(s -> s, s -> CONTENT_PAGE_URL)));
    }

    /**
     * Event handler for request with address {@link Addresses#PARSED_PAGE_CONTENT}.
     * Saves the result of parsing the content page.
     *
     * @param value {@link JsonObject}, not null
     * @see ParserVerticle#startTask(JsonObject)
     * @see StorageItem#toJson()
     */
    @Counted(name = "parsedPageContentRequestCounter", description = "Parsed page content request counter.")
    @Timed(name = "parsedPageContentRequestTimeout", description = "Parsed page content timeout.", unit = MetricUnits.MILLISECONDS)
    @Metered(name = "parsedPageContentRequestMeter", description = "Parsed page content request frequency.")
    @ConsumeEvent("parsed-page-content")
    public void onGetContent(@NotNull JsonObject value){
        if (!value.isEmpty()) {
            cache.add(StorageItem.of(value));
        }
    }

    /**
     * Event handler for request with address {@link Addresses#PARSED_PAGE_CONTENT}.
     * Updates content information for use in client applications.
     *
     * @param value {@link JsonObject}, not null
     * @see StortageVerticle#startTask(JsonObject)
     * @see StorageItem#toJson()
     * @see Content
     * @see Feed
     */
    @Counted(name = "contentReadyRequestCounter", description = "Saving content to stores counter.")
    @Timed(name = "contentReadyRequestTimeout", description = "Saving content to stores timeout.", unit = MetricUnits.MILLISECONDS)
    @Metered(name = "contentReadyRequestMeter", description = "Saving content to stores frequency.")
    @ConsumeEvent("content-ready")
    public void onStorageResult(JsonObject value){
        final Content content = Content.of(value);
        content.persistOrUpdate();
        cache.add(Feed.of(content));
    }

    void onStart(@Observes @Priority(2)StartupEvent event){
        initWorkers()
                .thenAccept(s -> log.info(" $ Application started with : "
                        + ((s != null) ? format(SUCCESS_DEPLOYMENT_TEMPLATE, s) : ERROR_DEPLOYMENT_MMESSAGE)));
    }

    private CompletionStage<String> initWorkers() {
        final int instanceCount = getInstanceCount();

        final DeploymentOptions options = new DeploymentOptions().setWorker(true).setWorkerPoolSize(instanceCount << 4)
                .setConfig(config.toJson());

        return deploy(ParseWorkerVerticle::new, options)
                .thenCompose(s -> deploy(ComparingVerticle::new, options))
                .thenCompose(s -> deploy(HTTPVerticle::new, options))
                .thenCompose(s -> deploy(ParserVerticle::new, options.setInstances(instanceCount)))
                .thenCompose(s -> deploy(StortageVerticle::new, options.setInstances(instanceCount)))
                .exceptionally(Utils::throwableHandler);
    }

    private int getInstanceCount() {
        return max(config.getThreads() <= 0 ? Runtime.getRuntime().availableProcessors() : config.getThreads(), 2) >> 1;
    }

    private JsonObject renew() {
        if (!renewing) {
            renewing = true;
            bus.<JsonArray>request(GET_SEEDS, config.getUrl(), DELIVERY_OPTIONS)
                    .thenCompose(message -> cache.addParserTasks(message.body().stream().map(Object::toString)
                            .collect(toMap(s -> s, s -> SEED))))
                    .exceptionally(Utils::throwableVoidHandler)
                    .thenAccept(aVoid -> renewing = false);
        }

        return new JsonObject();
    }

    private CompletionStage<String> deploy(Supplier<Verticle> supplier, DeploymentOptions options){
        return vertx.deployVerticle(supplier, options);
    }
}
