package com.appmobiles.cache;

import com.appmobiles.Utils;
import com.google.common.hash.Hashing;
import io.quarkus.infinispan.client.Remote;
import io.quarkus.runtime.StartupEvent;
import io.vertx.core.json.JsonObject;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Custom implementation repository pattern. Implements access to cached store in Infinispan data grid.
 */
@ApplicationScoped
public class CacheRepository {

    private static final String FEEDS_STORE_NAME = "feeds";
    private static final String PARSER_CACHE = "parser-cache";
    private static final String STORAGE_STORE_NAME = "storage-cache";

    @Inject
    RemoteCacheManager cacheManager;

    @Inject
    @Remote(value = FEEDS_STORE_NAME)
    RemoteCache<String, Feed> feedsStore;

    @Inject
    @Remote(value = STORAGE_STORE_NAME)
    RemoteCache<String, StorageItem> storageStore;

    @Inject
    @Remote(value = CacheRepository.PARSER_CACHE)
    RemoteCache<String, String> parserCache;

    private static final Logger log = LoggerFactory.getLogger( CacheRepository.class );

    private static final String CACHE_CONFIG =
            "<infinispan><cache-container>" +
                    "<distributed-cache name=\"%s\"></distributed-cache>" +
                    "</cache-container></infinispan>";

    void onStart(@Observes @Priority(0) StartupEvent ev) {
//        cleanupCaches(STORAGE_STORE_NAME, FEEDS_STORE_NAME, STORAGE_STORE_NAME, PARSER_CACHE);
        getOrCreateCache(STORAGE_STORE_NAME, FEEDS_STORE_NAME, STORAGE_STORE_NAME, PARSER_CACHE);
    }

    /**
     * Removes all of caches that listen in method signature (non blocking async optional operation).
     * All caches will be empty after this call returns.
     *
     * @param storeNames array on cache names, can be empty, not be null
     *
     * @return async computation always empty
     * @see CompletionStage
     */
    private CompletionStage<Void> cleanupCaches(String ... storeNames) {
        return CompletableFuture.allOf(
                Stream.of(storeNames).map(s -> cacheManager.getCache(s).clearAsync()).toArray(CompletableFuture[]::new));
    }

    /**
     * Creates all of caches that listen in method signature (non blocking async optional operation).
     *
     * @param storeNames array on cache names, can be empty, not be null
     *
     * @return list of created caches
     * @see RemoteCache
     */
    private List<RemoteCache<Object, Object>> getOrCreateCache(String ... storeNames) {
        return Stream.of(storeNames).map(s -> cacheManager.administration()
                .getOrCreateCache(s, new XMLStringConfiguration(String.format(CACHE_CONFIG, s))))
                .collect(Collectors.toList());
    }

    /**
     * Add feed to cache.
     * To eliminate duplicates, the corresponding identifiers are used.
     * For id used feed id (eq. {@link Content#id})
     *
     * @param feed {@link Feed} not null
     *
     * @return async computation with cache response, can be empty
     */
    public CompletionStage<Feed> add(@NotNull Feed feed){
        return feedsStore.putIfAbsentAsync(feed.getId(), feed);
    }

    /**
     * Add storage item to cache.
     * To eliminate duplicates, the corresponding identifiers are used.
     * For id used the hash {@link Hashing#murmur3_32()} of storage item value converted to json format without "ups"
     * {@link StorageItem#getUps()}.
     *
     * @param storageItem {@link StorageItem} not null
     *
     * @return {@link StorageItem} can be null
     */
    public StorageItem add(@NotNull StorageItem storageItem){
        final JsonObject json = storageItem.toJson();
        json.remove("ups");
        return storageStore.putIfAbsent(Utils.hashString(json.encode()), storageItem);
    }

    /**
     * Return first task for parsing converted to json format.
     *
     * @return {@link JsonObject}
     */
    public JsonObject getParserTask() {
        final Optional<Map.Entry<String, String>> first = parserCache.entrySet().stream().limit(1).findFirst();
        if (first.isPresent()) {
            final Map.Entry<String, String> entry = first.get();
            parserCache.remove(entry.getKey());
            return toMessage(entry);
        }
        return new JsonObject();
    }

    /**
     * Return first storage task converted to json format.
     *
     * @return {@link JsonObject}
     */
    public JsonObject getStorageTask() {
        final Optional<Map.Entry<String, StorageItem>> first = storageStore.entrySet().stream().limit(1).findFirst();
        if (first.isPresent()) {
            final Map.Entry<String, StorageItem> entry = first.get();
            final JsonObject result = entry.getValue().toJson();
            storageStore.remove(entry.getKey());
            return result;
        }
        return new JsonObject();
    }

    /**
     * Add parser task to cache.
     *
     * @param map {@link Map} not null
     * @return
     */
    public CompletionStage<Void> addParserTasks(Map<String, String> map) {
        return parserCache.putAllAsync(map);
    }

    private JsonObject toMessage(Map.Entry<String, String> entry) {
        return toMessage(entry.getKey(), entry.getValue());
    }

    private JsonObject toMessage(String type, String value){
        return new JsonObject().put("url", type).put("type", value);
    }

}
