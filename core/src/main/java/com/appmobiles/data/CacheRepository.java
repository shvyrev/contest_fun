package com.appmobiles.data;

import com.appmobiles.data.model.Content;
import com.appmobiles.data.model.Feed;
import io.quarkus.infinispan.client.Remote;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.dsl.SortOrder;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.min;

/**
 * Custom implementation repository pattern. Implements access to cached store in Infinispan data grid.
 */
@ApplicationScoped
public class CacheRepository {

    private static final String FEEDS_STORE_NAME = "feeds";

    @ConfigProperty(name = "rest.max-items-per-page", defaultValue = "25")
    Integer maxItemAmount;

    @ConfigProperty(name = "rest.feed.sort.field", defaultValue = "timestamp")
    String sortField;

    @Inject
    @Remote(value = FEEDS_STORE_NAME)
    RemoteCache<String, Feed> feedsStore;

    @Inject
    RemoteCacheManager cacheManager;

    private static final String CACHE_CONFIG =
            "<infinispan><cache-container>" +
                    "<distributed-cache name=\"%s\"></distributed-cache>" +
                    "</cache-container></infinispan>";

    void onStart(@Observes @Priority(value = 1) StartupEvent ev){
        getOrCreateCache(FEEDS_STORE_NAME);
    }

    @CircuitBreaker(requestVolumeThreshold = 4)
    public List<Feed> list(int page, int size){
        final int amount = size == 0 ? maxItemAmount : min(size, maxItemAmount);
        QueryFactory queryFactory = Search.getQueryFactory(feedsStore);
        return queryFactory.from(Feed.class)
                .orderBy(sortField, SortOrder.DESC)
                .startOffset(page * amount)
                .maxResults(amount)
                .build().list();
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
}
